/**
 * @(#)vri.java V.R.I.
 *
 * The Virtual Radio Interferometer
 *
 * This is a java applet (also runnable as a standalone java program) which
 * allows the simulation of various aspects of a radio interferometer.
 *
 * uvTest.java
 *
 * v1.0  05/Dec/1996  Derek McKay
 * v1.1  12/Dec/1996  Derek McKay & Nuria McKay
 * v1.2  13/Dec/1996  Derek McKay & Nuria McKay
 * v1.3  26/Dec/1996  Derek McKay, Mark Wieringa & Nuria McKay
 *
 * vri.java
 *
 * v2.0 06/Jan/1997   Derek McKay
 * v2.1 17/Mar/1997   Derek McKay
 * v2.2 07/Apr/1997   Derek McKay & Nuria McKay
 * v2.3 29/Jul/1997   Derek McKay & Nuria McKay
 * v2.4 10/Sep/1997   Nuria McKay (removed grid for ADASS)
 * v2.5 11/Sep/1997   Derek McKay (added new ATCA stations for JLC)
 *
 */


// http://jop46.jive.nl:8080/VRI/vri.jnlp

package nl.jive.vri;

import java.lang.*;
import java.lang.Math;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;
import java.util.Date;
import java.util.*;
import java.beans.*;

/**
 * "vri" is the main class for the VRI applet. It has the capability
 * to allow the program to be run standalone or as an applet. When it
 * is being run as an applet, it should be called with:
 * <PRE>
 * &ltAPPLET codebase="vri" code="vri.class" width=612 height=692&gt&lt/APPLET&gt
 * </PRE>
 *
 * It also has methods for returning information about the applet, such as
 * the overloading of the getAppletInfo class.
 *
 * @author Derek J. McKay
 */
public class vri extends JApplet
	 implements ActionListener
{
	 public vriArrDisp ArrDisp;
	 public vriImgDisp ImgDisp;
	 public vriImgDisp ImgDisp2;
	 public vriUVcDisp UVcDisp;
	 public vriUVpDisp UVpDisp;
	 public vriUVpDisp UVpConvDisp;
	 public vriArrEdit ArrEdit;
	 public vriImgEdit ImgEdit;
	 public vriUVpEdit UVpEdit;
	 public vriAuxEdit AuxEdit;
	 public vriObsEdit ObsEdit;
	 public vriObservatory obs;
	 public vriAuxiliary aux;
	 public vriDisplayCtrl ArrCtrl;
	 public vriDisplayCtrl UVcCtrl;
	 public vriDisplayCtrl ImgCtrl;
	 public vriDisplayCtrl UVpCtrl;

	 public static void main(String args[]) {
		  System.out.println("Standalone java program");

		  JFrame f = new JFrame("VRI");
		  vri vriTest = new vri();
		  vriTest.init();
		  f.add("Center", vriTest);
		  f.setSize(900, 900);
		  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  f.show();
	 }

	 /**
	  * Main routine to setup the screen
	  *
	  * @param (none)
	  * @return void
	  * @exception none
	  * @author Derek J. McKay
	  */

	 JPanel captionDisp(String s, vriDisplay d) 
	 {
		  JPanel top = new JPanel();
		  top.setLayout(new BorderLayout());		  
		  JPanel title = new JPanel();
		  title.add(new JLabel(s));
		  top.add(title, BorderLayout.NORTH);
		  top.add(d, BorderLayout.SOUTH);
		  return top;
	 }  

	 public void init() {

		  System.out.println("VRI, Virtual Radio Interferometer");

		  setLayout(null);
		  setBackground(Color.lightGray);

		  obs = vriObservatory.select("WSRT");

		  aux = new vriAuxiliary();

		  setLayout(new FlowLayout());

		  ObsEdit = new vriObsEdit(this); 
		  ImgEdit = new vriImgEdit(this); 
		  ArrEdit = new vriArrEdit(this, obs); 
		  UVpEdit = new vriUVpEdit(this); 
		  AuxEdit = new vriAuxEdit(this, aux, obs);


		  // Place the components on the screen

		  add(ObsEdit);
		  add(ImgEdit);

		  add(ArrEdit);
		  // add(UVpEdit);

		  add(AuxEdit);

		  JPanel allImgPanel = new JPanel();
		  allImgPanel.setLayout(new GridLayout(2,3));

		  Container ImgPanel = Box.createVerticalBox();
		  ImgDisp = new vriImgDisp(this);
		  ImgCtrl = new vriDisplayCtrl("?", ImgDisp);
		  JPanel imgDispPanel = captionDisp("Source Image", ImgDisp);
		  ImgPanel.add(imgDispPanel);
		  ImgPanel.add(ImgCtrl);
		  allImgPanel.add(ImgPanel);

		  Container ArrPanel = Box.createVerticalBox();
		  ArrDisp = new vriArrDisp(this, obs, ArrEdit );
		  ArrCtrl = new vriDisplayCtrl("Station\nlock", ArrDisp); 
		  JPanel arrDispPanel = captionDisp("Array", ArrDisp);
		  ArrPanel.add(arrDispPanel);
		  ArrPanel.add(ArrCtrl);
		  allImgPanel.add(ArrPanel);

		  Container ImgPanel2 = Box.createVerticalBox();
		  ImgDisp2 = new vriImgDisp(this );
		  vriDisplayCtrl ImgCtrl2 = new vriDisplayCtrl("?", ImgDisp2);
		  JPanel imgDispPanel2 = captionDisp("Reconstructed Image", ImgDisp2);
		  ImgPanel2.add(imgDispPanel2);
		  ImgPanel2.add(ImgCtrl2);
		  allImgPanel.add(ImgPanel2);
		  
		  // Second row: UV equivalents of first row

		  Container UVpPanel = Box.createVerticalBox();
		  UVpDisp = new vriUVpDisp(this );
		  JPanel uvpDispPanel = captionDisp("UV source", UVpDisp);
		  UVpCtrl = new vriDisplayCtrl("?", UVpDisp); //   0, y4-w3*4, w3-1, w3*4-1, "?", UVpDisp);
		  UVpPanel.add(uvpDispPanel);
		  UVpPanel.add(UVpCtrl);
		  allImgPanel.add(UVpPanel);

		  Container UVcPanel = Box.createVerticalBox();
		  UVcDisp = new vriUVcDisp(obs, aux);
		  JPanel uvcDispPanel = captionDisp("Array UV coverage", UVcDisp);
		  UVcCtrl = new vriDisplayCtrl("?", UVcDisp); 
		  UVcPanel.add(uvcDispPanel);
		  UVcPanel.add(UVcCtrl);
		  allImgPanel.add(UVcPanel);

		  Container UVpPanel2 = Box.createVerticalBox();
		  UVpConvDisp = new vriUVpDisp(this);
		  JPanel uvpDispPanel2 = captionDisp("UV detection", UVpConvDisp);
		  vriDisplayCtrl UVpCtrl2 = new vriDisplayCtrl("?", UVpConvDisp); 
		  UVpPanel2.add(uvpDispPanel2);
		  UVpPanel2.add(UVpCtrl2);
		  allImgPanel.add(UVpPanel2);
		  
		  add(allImgPanel);


		  ImgDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  System.err.println("** Image changed");
						  UVpDisp.fft(ImgDisp.dat);
						  // FIXME: would be nicer to check UVp is set
						  float uvcov[] = UVcDisp.uvCoverage(ImgDisp.dat.imsize);
						  UVpConvDisp.applyUVc(uvcov, UVpDisp.fft);
					 }
				});
		  
		  UVcDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  System.err.println("** UVc changed");
						  float uvcov[] = UVcDisp.uvCoverage(ImgDisp.dat.imsize);
						  UVpConvDisp.applyUVc(uvcov, UVpDisp.fft);
					 }
				});

		  UVpConvDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  System.err.println("** UVpConv changed");
						  ImgDisp2.invfft(UVpConvDisp.fft);
					 }
				});

		  ArrDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  System.err.println(String.format("** ArrDisp changed: %s", e.getPropertyName()));
						  UVcDisp.repaint();
					 }
				});

		  // this initialization stuff should be 
		  // pushed down to the actual objects
 		  String c = obs.defaultConfig();
 		  ArrEdit.config.setSelectedItem(c);

		  UVpEdit.init();
		  show();
		  ImgEdit.src_choice.setSelectedItem("Wide double");

	 }
	 
	 // Obsolete event handler
	 public void actionPerformed(ActionEvent e) {
		  System.out.println("Got an event");
		  // We need this to handle the events that affect things more globally
		  JComponent source = (JComponent) e.getSource();
		  if (source == ObsEdit.el_field) {
				Double d = new Double(ObsEdit.el_field.getText());
				obs.ant_el_limit = d.doubleValue() * Math.PI / 180.0;
				ObsEdit.site_choice.setSelectedItem("custom");
				// AuxEdit.setDecRange(); //FIXME
				UVcDisp.repaint();
		  } else if (source == ObsEdit.dia_field) {
				Double d = new Double(ObsEdit.dia_field.getText());
				obs.ant_diameter = d.doubleValue();
				UVcDisp.repaint();
				ObsEdit.site_choice.setSelectedItem("custom");
		  } else if (source == ObsEdit.lat_field) {
				Double d = new Double(ObsEdit.lat_field.getText());
				obs.latitude = d.doubleValue() * Math.PI / 180.0;
				// AuxEdit.setDecRange(); // FIXME
				UVcDisp.repaint();
		  } else if (source == ObsEdit.ant_field) {
				Integer n = Integer.valueOf(ObsEdit.ant_field.getText());
				obs.selectNumAnt(n.intValue());
				ObsEdit.site_choice.setSelectedItem("custom");
				UVcDisp.repaint();
				ArrDisp.repaint();
		  }
	 }

	 /**
	  * A method to set the array configuration to "custom"
	  *
	  * @return void
	  * @author Derek J. McKay
	  */
	 public void setCustomConfig() {
		  ArrEdit.config.setSelectedItem("custom");
	 }
}


class vriDisplayCtrl extends JPanel {
	 vriDisplay disp;
	 public JButton function;
	 public JButton zoomIn;
	 public JButton zoomOut;
	 public JButton zoomReset;

	 public vriDisplayCtrl(String s, vriDisplay d) {
		  disp = d;
		  //setLayout(new GridLayout(0, 1));
		  //add(new JLabel());
		  add(new JLabel("Zoom", JLabel.CENTER));
		  add(zoomIn = new JButton("In"));
		  zoomIn.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent ae) {
						  disp.zoomIn();
					 }
				});
		  add(zoomOut = new JButton("Out"));
		  zoomOut.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent ae) {
						  disp.zoomOut();
					 }
				});
		  add(zoomReset = new JButton("Reset"));
		  zoomReset.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent ae) {
						  disp.zoomReset();
					 }
				});
	 }
}





class vriArrEdit extends JPanel {
	 public JComboBox config;
	 public JButton stn_lock;
	 vri parent;
	 public vriArrEdit(vri p, vriObservatory obs) {
		  parent = p;
		  setLayout(new FlowLayout());

		  setupConfigMenu(obs);
	 }

	 public void setupConfigMenu(final vriObservatory obs) {
		  removeAll();
		  setLayout(new FlowLayout());
		  add(new JLabel("Configuration:", JLabel.RIGHT));
		  add(config = new JComboBox(obs.cfg_name));
		  config.setSelectedIndex(0);
		  config.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = config.getSelectedItem().toString();
						  if (obs.setConfig(s)) {
								parent.ArrDisp.repaint();
						  }
					 }
				});
				
		  add(stn_lock = new JButton("Station lock"));
		  stn_lock.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  System.err.println("Station lock button pressed");
						  parent.ArrDisp.stationLock();
					 }
				});
		  Graphics gc = getGraphics();
		  paintAll(gc);
	 }
}


class vriUVcEdit extends JPanel {
	 public JButton add;
	 public JButton clear;
	 public JComboBox colour;

	 public vriUVcEdit(final vri parent) {
		  setLayout(new GridLayout(0, 2));

		  add(add = new JButton("Add"));
		  add.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  parent.UVcDisp.addTracks();
					 }
				});
		  add(new JLabel("Accumulate", JLabel.LEFT));
		  add(clear = new JButton("Clear"));
		  clear.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  parent.UVcDisp.clearTracks();
					 }
				});
		  String[] cols = {"Blue", "Red", "Hide"};
		  add(colour = new JComboBox(cols));
		  colour.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = colour.getSelectedItem().toString();
						  System.err.println("Setting UVcDisp colour to "+s);
						  if (s.equals("Blue")) parent.UVcDisp.setColour(Color.blue);
						  else if (s.equals("Red"))  parent.UVcDisp.setColour(Color.red);
						  else if (s.equals("Hide")) parent.UVcDisp.setColour(Color.black);

					 }
				});
	 }
}


class vriImgEdit extends JPanel {
	 public JComboBox src_choice;
	 public JLabel src_label;

	 public vriImgEdit(final vri parent) {
		  setLayout(new FlowLayout());

		  add(src_label = new JLabel("Source:",Label.RIGHT));
		  String[] src_data = {"Point", "Offset point",
									  "Wide double", "Narrow double",
									  "Wide gaussian", "Narrow gaussian", "Disc",
									  "Crux", "Radio galaxy"};
		  final Map<String, String> imageMap = new HashMap<String, String>();
		  // FIXME: There's a way of doing class-level initialisers;
		  // should we use that instead?
		  imageMap.put("Point", "point.gif");
		  imageMap.put("Offset point", "offset_point.gif");
		  imageMap.put("Wide double", "wide_double.gif");
		  imageMap.put("Narrow double", "narrow_double.gif");
		  imageMap.put("Wide gaussian", "wide_gauss.gif");
		  imageMap.put("Narrow gaussian", "narrow_gauss.gif");
		  imageMap.put("Disc", "disc.gif");
		  imageMap.put("Crux", "crux.gif");
		  imageMap.put("Radio galaxy", "radio_galaxy.gif");

		  add(src_choice = new JComboBox(src_data));
		  src_choice.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = src_choice.getSelectedItem().toString();
						  System.out.println("Loading "+imageMap.get(s));
						  parent.ImgDisp.load(imageMap.get(s));
					 }
				});
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
						  parent.UVpDisp.type = type.getSelectedItem().toString();
						  parent.UVpDisp.fftToImg(parent.UVpDisp.fft);
					 }
				});
	 }
	 public void init() {
		  parent.UVpDisp.type = "Ampl.";
		  parent.UVpConvDisp.type = "Ampl.";
	 }
}



class HourAngleDec extends JPanel {
	 JLabel ha1_label;
	 JLabel ha2_label;
	 JLabel dec_label;
	 JSlider ha1;
	 JSlider ha2;
	 JSlider dec;
	 double ha_limit1;     // The Hour angle limit imposed by the Ant.El.limt
	 double ha_limit2;     // and the choice of declination.	 
	 double ha1_val; // needed to cache old value for firePropertyChanges
	 double ha2_val;
	 int dec_val;
	 private PropertyChangeSupport propChanges;

	 vriObservatory obs;
	 
	 float boundValue(float val, float min, float max) {
		  float res;
		  if (val>max) 
				res = max;
		  else if (val<min)
				res = min;
		  else
				res = val;
		  return val;
	 }
	 int boundValue(int val, int min, int max) {
		  int res;
		  if (val>max) 
				res = max;
		  else if (val<min)
				res = min;
		  else
				res = val;
		  return val;
	 }

	 double boundValue(double val, double min, double max) {
		  double res;
		  if (val>max) 
				res = max;
		  else if (val<min)
				res = min;
		  else
				res = val;
		  return val;
	 }


	 double intToHours(int h) {
		  return Math.toRadians(1.5 * (double)h);
	 }

	 int hoursToInt(double ha) {
		  return (int) Math.round(Math.toDegrees(ha/1.5));    
	 }

	 public HourAngleDec() {
		  propChanges = new PropertyChangeSupport(this);
		  setLayout(new GridLayout(3,0));
		  add(ha1_label = new JLabel("Hour Angle (-6.0h):"));
		  add(ha1 = new JSlider(JSlider.HORIZONTAL, -120, 120, -60));
		  add(ha2_label = new JLabel("Hour Angle (+6.0h):"));
		  add(ha2 = new JSlider(JSlider.HORIZONTAL, -120, 120, 60));
		  add(dec_label = new JLabel("Declination (90°):"));
		  add(dec = new JSlider(JSlider.HORIZONTAL, -90, 90, 90));

		  dec.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  int old_dec_val = dec_val;
						  dec_val = dec.getValue();
						  dec_label.setText(String.format("Declination (%d)°:", dec_val));
						  double dec = Math.toRadians((double)dec_val);
						  propChanges.firePropertyChange("dec", old_dec_val, dec_val);
						  limitHArange();
					 }
				});
		  
		  ha1.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  double old_ha1_val = ha1_val;
						  int h1 = ha1.getValue();
						  ha1_val = boundValue(intToHours(h1), 
													  ha_limit1, ha_limit2);
						  setHAlabel(ha1_label, hoursToInt(ha1_val));
						  propChanges.firePropertyChange("ha1", old_ha1_val, ha1_val);
						  if (ha1_val > ha2_val) {
								double old_ha2_val = ha2_val;
								ha2_val = ha1_val;
								int h2 = hoursToInt(ha2_val);
								ha2.setValue(h2);
								setHAlabel(ha2_label, h2);
								propChanges.firePropertyChange("ha2", old_ha2_val, ha2_val);
						  }
					 }
				});
		  
		  ha2.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  double old_ha2_val = ha2_val;
						  int h2 = ha2.getValue();
						  ha2_val = boundValue(intToHours(h2), ha_limit1, ha_limit2);
						  setHAlabel(ha2_label, h2);
						  propChanges.firePropertyChange("ha2", old_ha2_val, ha2_val);
						  if (ha2_val < ha1_val) {
								double old_ha1_val = ha1_val;
								ha1_val = ha2_val;
								int h1 = hoursToInt(ha1_val);
								ha1.setValue(h1);
								setHAlabel(ha1_label, h1);
								propChanges.firePropertyChange("ha1", old_ha1_val, ha1_val);
						  }
					 }
				});

		  setHAlabel(ha1_label, -60);
		  setHAlabel(ha2_label,  60);
	 }

	 void limitHArange() {
		  double ha = (Math.sin(obs.ant_el_limit) - 
							Math.sin(obs.latitude) * Math.sin(Math.toRadians(dec_val))) /
				(Math.cos(obs.latitude) * Math.cos(Math.toRadians(dec_val)) );
		  System.err.println(String.format("Obs properties: ant_el_limit %.2f, latitude %.2f", 
															obs.ant_el_limit, obs.latitude));
		  System.err.println(String.format("Dec val %d", dec_val));
		  System.err.println(String.format("ha %.2f", ha));
				
		  if (ha >= 1.0) {
				ha_limit1 = ha_limit2 = 0.0;
		  } else if (ha <= -1.0) {
				ha_limit1 = -12.0;
				ha_limit2 = 12.0;
		  } else {
				ha = Math.abs(Math.acos(ha));
				ha_limit1 = -ha;
				ha_limit2 = ha;
		  }
		  if (ha1_val < ha_limit1) {
				double old_ha1_val = ha1_val;
				ha1_val = quantizeHA(ha_limit1);
				int h1 = hoursToInt(ha1_val);
				setHAlabel(ha1_label, h1);
				ha1.setValue(h1);
				propChanges.firePropertyChange("ha1", old_ha1_val, ha1_val);
		  }
		  if (ha2_val > ha_limit2) {
				double old_ha2_val = ha2_val;
				ha2_val = quantizeHA(ha_limit2);
				int h2 = hoursToInt(ha2_val);
				setHAlabel(ha2_label, h2);
				ha2.setValue(h2);
				propChanges.firePropertyChange("ha2", old_ha2_val, ha2_val);
		  }
	 }

	 public void setDecRange() {
		  int el = (int) Math.round(Math.toDegrees(obs.ant_el_limit));
		  int lat = (int) Math.round(Math.toDegrees(obs.latitude));

		  int dec_max = Math.min( 90 + lat - el,  90);    
		  int dec_min = Math.max(-90 + lat + el, -90);
		  int old_dec_val = dec_val;
		  int dec_val = boundValue(dec.getValue(), dec_min, dec_max);

		  dec.setValue(dec_val);
		  dec.setMinimum(dec_min);
		  dec.setMaximum(dec_max);
		  propChanges.firePropertyChange("dec", old_dec_val, dec_val);
	 };

	 double quantizeHA(double ha) {
		  ha *= 12.0 / Math.PI;             // Convert to hours
		  ha = Math.rint(ha*10.0) / 10.0;   // Round to 1 decimal place
		  ha *= Math.PI / 12.0;             // Convert back to radians
		  return ha;
	 }

	 void setHAlabel(JLabel label, int h) {
		  String s = String.format("Hour Angle (%+.1f)", h/10.0);
		  label.setText(s);
	 }

	 void setObservatory(vriObservatory o) {
		  obs = o;
		  limitHArange();
	 }
	 
  	 public void addPropertyChangeListener(PropertyChangeListener l)
	 {
		  propChanges.addPropertyChangeListener(l);
	 }
	 public void removePropertyChangeListener(PropertyChangeListener l)
	 {
		  propChanges.removePropertyChangeListener(l);
	 }
}



class vriAuxEdit extends JPanel {
	 JTextField fr_field;
	 JTextField bw_field;
	 vriAuxiliary aux;
	 vriObservatory obs;
	 HourAngleDec had;
	 vri parent;

	 public vriAuxEdit(vri p, vriAuxiliary a, vriObservatory o) {
		  parent = p;
		  aux = a;
		  obs = o;

		  setLayout(new FlowLayout());

		  Container vb = Box.createVerticalBox();
		  JPanel fr_panel = new JPanel();
		  
		  fr_panel.add(new JLabel("Frequency:", Label.RIGHT));
		  fr_panel.add(fr_field = new JTextField("4800.0", 8));
		  fr_panel.add(new JLabel("MHz", Label.LEFT));
		  vb.add(fr_panel);

		  JPanel bw_panel = new JPanel();
		  bw_panel.add(new JLabel("Bandwidth:"));
		  bw_panel.add(bw_field = new JTextField("100.0", 8));
		  bw_panel.add(new JLabel("MHz"));
		  vb.add(bw_panel);
		  add(vb);
		  
		  had = new HourAngleDec();
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
						  parent.UVcDisp.clearTracks();
						  parent.UVcDisp.repaint();
					 }
				});
					 		  
		  bw_field.setEditable(false);
	 }

	 public boolean handleEvent(Event e) {
		  if (e.target == fr_field && e.id == Event.KEY_PRESS) {
				if (e.key == 10) {
					 Double d = new Double(fr_field.getText());
					 aux.freq = d.doubleValue();
				} else {
					 return false;
				}
		  } else if (e.target == fr_field && e.id == Event.LOST_FOCUS) {
				Double d = new Double(fr_field.getText());
				aux.freq = d.doubleValue();
		  } else {
				return false;   // Nothing we can do here - pass the buck to the parent!
		  }
		  return true;
	 }

}


class vriObsEdit extends JPanel {
	 public JComboBox site_choice;
	 public JLabel lat_field;
	 public JLabel ant_field;
	 public JLabel dia_field;
	 public JLabel el_field;
	 vri parent;

	 public vriObsEdit(vri p) {
		  parent = p;
		  setLayout(new FlowLayout(FlowLayout.LEFT));

		  add(new JLabel("Obs:",Label.RIGHT));
		  String[] site_choice_data = {"MERLIN",
												 "ATCA",
												 "WSRT",
												 "New ATCA",
												 "custom"};

		  add(site_choice = new JComboBox(site_choice_data));
		  site_choice.setSelectedItem("MERLIN");
		  site_choice.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = site_choice.getSelectedItem().toString();
						  parent.obs = vriObservatory.select(s);
						  parent.ArrEdit.setupConfigMenu(parent.obs);
						  String c = parent.obs.defaultConfig();
						  parent.ArrEdit.config.setSelectedItem(c);
						  parent.ArrDisp.setObservatory(parent.obs);
						  parent.ArrDisp.repaint();
						  parent.UVcDisp.setObservatory(parent.obs);
						  parent.UVcDisp.repaint();
						  parent.AuxEdit.had.setDecRange();
						  parent.ObsEdit.setFields(parent.obs);
					 }
				});

		  add(new JLabel("Lat:", JLabel.RIGHT));
		  add(lat_field = new JLabel("+53.0517"));
		  add(new JLabel("Ants:", JLabel.RIGHT));
		  add(ant_field = new JLabel("6"));
		  add(new JLabel("Dia:", JLabel.RIGHT));
		  add(dia_field = new JLabel("25.0"));
		  add(new JLabel("El lim:", JLabel.RIGHT));
		  add(el_field = new JLabel("5.0"));

		  // reshape(x, y, w, h);
		  //    System.out.println(this);
	 }

	 public void setFields(vriObservatory obs) {
		  lat_field.setText(String.format("%.3f", parent.obs.latitude * 180.0 / Math.PI));
		  ant_field.setText(String.format("%d", parent.obs.num_antennas));
		  dia_field.setText(String.format("%.3f", parent.obs.ant_diameter));
		  el_field.setText(String.format("%.3f", parent.obs.ant_el_limit * 180.0 / Math.PI));
	 }
}

//####################################################################//

