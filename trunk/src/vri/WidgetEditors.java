package nl.jive.vri;

import java.lang.*;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*; // ActionListener
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.beans.*;


class AstroImage {
    String name;
    String filename;
    double scale; // arc seconds
    int size;

    AstroImage(String n, String fn, double s, int aSize) {
        name = n;
        filename = fn;
        scale = s;
        size = aSize;
    }
    double getScale() {
        return scale;
    }
    double getSize() {
        return size;
    }
    double getUVScale() {
        double radians = scale*Math.PI/(180.0*60.0*60.0);
        /* highest frequency is 1/(2*delta) and this scales half the uv
           plane */
        return size/radians;
    }
}


class vriImgEdit extends JPanel {
    JComboBox src_choice;
    JLabel src_label;
    vri parent;
    Map<String, AstroImage> imageMap;


    public vriImgEdit(vri p) {
        setLayout(new FlowLayout());
        parent = p;
        imageMap = new HashMap<String, AstroImage>();
        ArrayList<AstroImage> images = new ArrayList<AstroImage>();
        String[] src_data;
		  
        images.add(new AstroImage("Wide double", "wide_double_256.gif", 
                                  2.0, 256));
        images.add(new AstroImage("Medium double", "wide_double_256.gif",
                                  0.2, 256));
        images.add(new AstroImage("Narrow double", "wide_double_256.gif", 
                                  0.02, 256));
        images.add(new AstroImage("Radio galaxy", "radio_galaxy_256.gif",
                                  0.2, 256));
        images.add(new AstroImage("m82", "m82_1.6GHz_MERLIN_256.png", 
                                  20.0, 256));
        // images.add(new AstroImage("m82-small", "m82_1.6GHz_MERLIN_256.png",
        //                           6.7, 256));
        images.add(new AstroImage("CSO", "CSO_5GHz_EVN_256.png", 
                                  0.2, 256));
        images.add(new AstroImage("ss433", "ss433_1.6GHz_global_256.png", 
                                  0.1, 256));

        src_data = new String[images.size()];
        for (int i=0; i<images.size(); i++) {
            AstroImage ai = images.get(i);
            src_data[i] = ai.name;
            imageMap.put(ai.name, ai);
        }

        add(src_label = new JLabel("Source:",Label.RIGHT));
        add(src_choice = new JComboBox(src_data));
        src_choice.setSelectedItem("Medium double");
        src_choice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String s = src_choice.getSelectedItem().toString();
                    setImage(s);
                }
            });
    }

    String getImage() {
        return src_choice.getSelectedItem().toString();
    }

    void setImage(String s) {
        AstroImage ai = imageMap.get(s);
        if (parent==null) {
            System.err.println("setImage: parent is null");
        } else if (parent.imgDisp==null) {
            System.err.println("setImage: parent.imgDisp is null");
        }

        firePropertyChange("astroimage", null, ai);
        parent.imgDisp.loadAstroImage(ai);
        System.err.println("Loaded "+ai.filename);
        parent.imgDisp2.setFullScale(ai.scale);

        double uvScale = ai.getUVScale();

        System.err.println("vriImgEdit: Setting scales");
        parent.UVpDisp.setFullScale(uvScale);  
        parent.UVpConvDisp.setFullScale(uvScale);
        parent.UVcDisp.setConvScale(uvScale);
        parent.UVcDisp.repaint();
        // FIXME: this should *definitely* be somewhere else!
        if (parent.UVpConvDisp.fft!=null && parent.UVpConvDisp.uvcov!=null) {
            System.err.println("vriImgEdit: recalculating UVcDisp uvCoverage");
            parent.UVcDisp.uvCoverage();
        } else {
            System.err.println("vriImgEdit: one of UVpConvDisp.fft and UVpConvDisp.uvcov is null");
        }
    }

}

class vriAuxEdit extends JPanel 
{
    vriAuxiliary aux;
    vriObservatory obs;
    simpleHourAngleDec had;
    vri parent;

    public vriAuxEdit() { 
        // so that we can subclass without the real constructor
        ;
    }

    public vriAuxEdit(vri p, vriAuxiliary a, vriObservatory o) {
        parent = p;
        aux = a;
        obs = o;

        setLayout(new FlowLayout());
		  
        had = new simpleHourAngleDec();
        aux.ha1 = had.getHa1(); 
        aux.ha2 = had.getHa2();
        aux.dec = Math.toRadians(had.getDec());

        had.setObservatory(obs);
        add(had);
		 
        had.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    String pn = e.getPropertyName();
                    if (pn=="ha1") {
                        System.err.println("ha1: "+ e.getNewValue());
                        aux.ha1 = (Double) e.getNewValue();
                    } else if (pn=="ha2") {
                        System.err.println("ha2: "+e.getNewValue());
                        aux.ha2 = (Double) e.getNewValue();
                    } else if (pn=="dec"){ 
                        System.err.println("dec: "+e.getNewValue());
                        aux.dec = Math.toRadians((Integer)e.getNewValue());
                    }
                    parent.UVcDisp.repaint();
                    if (parent.UVpDisp.fft!=null) {
                        parent.UVcDisp.uvCoverage(); 
                    }
                }
            });
    }

    void setObservatory(vriObservatory o) {
        obs = o;
        had.setDecRange();
    }
}

class vriAuxEditJr extends vriAuxEdit 
{
    vriAuxiliary aux;
    vriObservatory obs;
    vri parent;
    double ha_val;
    JSlider ha;
    JLabel ha_label;
    String ha_format = "Hour Angle (%+.1f)";

    public vriAuxEditJr(vri p, vriAuxiliary a, vriObservatory o) {
        parent = p;
        aux = a;
        obs = o;

        setLayout(new FlowLayout());
		  

        int ha_int_val = 60;
        ha_val = intToHours(ha_int_val);

        ha_label = new JLabel(String.format(ha_format, ha_val));
        ha = new JSlider(JSlider.HORIZONTAL, 0, 240, ha_int_val);


        add(ha_label);
        add(ha);

        aux.ha1 = 0.0; 
        aux.ha2 = ha_val;
        aux.dec = Math.toRadians(70.0);

        ha.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int h = ha.getValue();
                    ha_val = intToHours(h);
                    ha_label.setText(String.format(ha_format, h/10.0));
                    aux.ha2 = ha_val;
                    // Ugh. FIXME
                    // should be in UVcDisp
                    parent.UVcDisp.repaint();
                    if (parent.UVpDisp.fft!=null) {
                        parent.UVcDisp.uvCoverage(); 
                    }
                }
            });
    }

    // stolen from SimpleHourAngleDec
    double intToHours(int h) {
        return Math.toRadians(1.5 * (double)h);
    }

    int hoursToInt(double hour) {
        return (int) Math.round(Math.toDegrees(hour/1.5));    
    }

    void setObservatory(vriObservatory o) {
        obs = o;
    }

}




class vriObsEdit extends JPanel {
    JComboBox site_choice;
    JLabel lat_field;
    JLabel ant_field;
    JLabel dia_field;
    JLabel el_field;
    vri parent;

    public vriObsEdit(vri p, String[] site_choice_data) {
        parent = p;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(site_choice = new JComboBox(site_choice_data));
        site_choice.setSelectedItem("EVN");

        site_choice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String s = site_choice.getSelectedItem().toString();
                    setObservatory(s);
                }
            });
    }

    public vriObsEdit(vri p) {
        this(p, new String[] {"MERLIN", "EVN", "IYA"});
    }

    void setObservatory(String s) {
        parent.obs = parent.obsman.select(s);
        String c = parent.obs.defaultConfig();
        parent.setArrDisp(parent.obs);
        parent.allImgPanel.validate();
        parent.auxEdit.setObservatory(parent.obs);
        System.err.println("vriObsEdit: Observatory set");
		  
    }
    String getObservatory() {
        return site_choice.getSelectedItem().toString();
    }
}



class vriUVpEdit extends JPanel {
    public JComboBox type;
    vri parent;

    public vriUVpEdit(vri p) {
        parent = p;
        setLayout(new FlowLayout());
        add(new JLabel("Display:", JLabel.RIGHT));
        String[] type_data = {"Ampl.", "Real", "Imag.",
                              "Phase", "Colour"};
        add(type = new JComboBox(type_data));
        type.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String s = type.getSelectedItem().toString();
                    PlotTypes t;
                    if (s.equals("Ampl.")) {
                        t = PlotTypes.AMPL;
                    } else if (s.equals("Real")) {
                        t = PlotTypes.REAL;
                    } else if (s.equals("Imag.")) {
                        t = PlotTypes.IMAG;
                    } else if (s.equals("Phase")) {
                        t = PlotTypes.PHASE;
                    } else {
                        t = PlotTypes.COLOUR;
                    }
                    parent.UVpDisp.type = t;
                    parent.UVpDisp.fftToImg(parent.UVpDisp.fft);
                }
            });
    }
}

