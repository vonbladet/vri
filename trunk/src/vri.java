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

public class vri extends JApplet
{
	 public vriArrDisp arrDisp;
	 PropertyChangeListener arrDispListener;
	 public vriImgDisp imgDisp;
	 public vriImgDisp imgDisp2;
	 public vriUVcDisp UVcDisp;
	 public vriUVpDisp UVpDisp;
	 public vriUVpDisp UVpConvDisp;
	 public vriArrEdit arrEdit;
	 public vriImgEdit imgEdit;
	 public vriAuxEdit auxEdit;
	 public vriObsEdit obsEdit;
	 public vriObservatory obs;
	 public vriObservatoryManager obsman;
	 public vriAuxiliary aux;
	 public vriDisplayCtrl arrCtrl;
	 public vriUVcZoomChooser UVcCtrl;
	 JPanel allImgPanel;
	 double astroScale = 1; // arcseconds
	 double uvScale = 1000; // lambda (or klambda?)

	 public static void main(String args[]) {
		  System.out.println("Standalone java program");

		  JFrame f = new JFrame("VRI");
		  vri vriTest = new vri();
		  vriTest.init();
		  f.add("Center", vriTest);
		  f.setSize(900, 700);
		  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  f.setVisible(true);
	 }

	 public void setAstroScale(double s) {
		  astroScale = s;
		  if (imgDisp2!=null) 
				imgDisp2.setFullScale(s);
	 }

	 public double getAstroScale() {
		  return astroScale;
	 }

	 public void setUVScale(double s) {
		  uvScale = s;
		  if (UVpDisp!=null) {
				UVpDisp.setFullScale(s);
		  }
		  if (UVpConvDisp!=null) {
				UVpConvDisp.setFullScale(s);
		  }
		  if (UVcDisp!=null) {
				UVcDisp.setConvScale(s);
		  }
	 }

	 public double getUVScale() {
		  return uvScale;
	 }

	 void setArrDisp(vriObservatory obs) {
		  allImgPanel.remove(arrDisp);
		  arrDisp.removePropertyChangeListener(UVcDisp);
		  arrDisp.removePropertyChangeListener(arrDispListener);
		  allImgPanel.remove(UVcDisp);

		  GridBagConstraints gbc = new GridBagConstraints();
		  gbc.gridx = 1;
		  gbc.gridy = 1;
		  arrDisp = obs.getArrDisp(arrEdit);

		  allImgPanel.add(arrDisp, gbc);

		  gbc.gridx = 1;
		  gbc.gridy = 4;
		  UVcDisp = obs.getUVcDisp(aux);
		  allImgPanel.add(UVcDisp, gbc);
		  System.err.println("Changing UVc  ");
		  System.err.println("Visible  "+UVcDisp.isVisible());
		  System.err.println("Showing  "+UVcDisp.isShowing());
		  System.err.println();

		  System.err.println("Setting UVc scale to "+astroScale);
		  UVcDisp.setConvScale(uvScale);
		  UVcCtrl.setDisplay(UVcDisp);
		  UVcDisp.repaint();

		  allImgPanel.repaint();

		  arrCtrl.setDisplay(arrDisp);
		  arrDisp.addPropertyChangeListener(UVcDisp); // handles active antenna
		  arrDisp.addPropertyChangeListener(arrDispListener = new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  System.err.println("** [setArrayDisp] arrDisp changed - updating uvcoverage(2)");
						  int size = UVpDisp.fft.imsize;
						  UVcDisp.uvCoverage(size);
					 } 
				});

		  UVcDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  if (e.getPropertyName()=="uvcov") {
								System.err.println("** [setArrayDisp] UV coverage changed - updating convolution");
								SquareArray uvcov =  (SquareArray)e.getNewValue(); 
								UVpConvDisp.applyUVc(uvcov, UVpDisp.fft);
						  }
 					 }
				});

		  arrDisp.propChanges.firePropertyChange("this", null, this);
		  allImgPanel.repaint();
	 }


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
		  obsman = new vriObservatoryManager();
		  obs = obsman.select("EVN");
		  aux = new vriAuxiliary();

		  setLayout(new FlowLayout());

		  obsEdit = new vriObsEdit(this); 
		  imgEdit = new vriImgEdit(this); 
		  arrEdit = new vriArrEdit(this, obs); 
		  auxEdit = new vriAuxEdit(this, aux, obs);

		  // Place the components on the screen
		  
		  JTabbedPane tabbedPane = new JTabbedPane();
		  JPanel obsEditPane = new JPanel();
		  obsEditPane.add(obsEdit);
		  obsEditPane.add(imgEdit);
		  tabbedPane.addTab("Observatory", obsEditPane);

		  JPanel auxEditPane = new JPanel();
		  auxEditPane.add(auxEdit);
		  tabbedPane.add("Observation", auxEdit);

		  add(tabbedPane);

		  allImgPanel = new JPanel();
		  GridBagLayout gbl = new GridBagLayout();
		  GridBagConstraints gbc = new GridBagConstraints();
		  allImgPanel.setLayout(gbl);

		  // First Column
		  gbc.gridx = 0;
		  gbc.gridy = 0;
		  allImgPanel.add(new JLabel("Source Image"), gbc);

		  gbc.gridx = 0;
		  gbc.gridy = 1;
		  allImgPanel.add(imgDisp = new vriImgDisp(this), gbc);
		  
		  gbc.gridx = 0;
		  gbc.gridy = 3;
		  allImgPanel.add(new JLabel("UV source"), gbc);

		  gbc.gridx = 0;
		  gbc.gridy = 4;
		  allImgPanel.add(UVpDisp = new vriUVpDisp(this), gbc);

		  // Second column
		  gbc.gridx = 1;
		  gbc.gridy = 0;
		  allImgPanel.add(new JLabel("Array"));
		  
		  arrDisp = obs.getArrDisp(arrEdit);
		  gbc.gridx = 1;
		  gbc.gridy = 1;
		  allImgPanel.add(arrDisp, gbc);

		  gbc.gridx = 1;
		  gbc.gridy = 2;
		  allImgPanel.add(arrCtrl=new vriDisplayCtrl(arrDisp), gbc);

		  gbc.gridx = 1;
		  gbc.gridy = 3;
		  allImgPanel.add(new JLabel("Array UV Coverage"), gbc);


		  UVcDisp = obs.getUVcDisp(aux);
		  gbc.gridx = 1;
		  gbc.gridy = 4;
		  allImgPanel.add(UVcDisp, gbc);

		  gbc.gridx = 1;
		  gbc.gridy = 5;
		  allImgPanel.add(UVcCtrl=new vriUVcZoomChooser("?", UVcDisp), gbc);

		  // Third column

		  gbc.gridx = 2;
		  gbc.gridy = 0;
		  allImgPanel.add(new JLabel("Reconstructed Image"), gbc); 

		  gbc.gridx = 2;
		  gbc.gridy = 1;
		  allImgPanel.add(imgDisp2 = new vriImgDisp(this), gbc);
		  
		  gbc.gridx = 2;
		  gbc.gridy = 3;
		  allImgPanel.add(new JLabel("UV Detection"), gbc);

		  gbc.gridx = 2;
		  gbc.gridy = 4;
		  allImgPanel.add(UVpConvDisp=new vriUVpDisp(this), gbc);
		  // End of gridbagging
		  
		  add(allImgPanel);

		  imgDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  if (e.getPropertyName()=="dat") {
								System.err.println("** img disp dat changed");
								FFTArray dat = (FFTArray) e.getNewValue();
								if (dat != null && UVpDisp !=null) {
									 UVpDisp.fft(dat);
								}
						  }
					 }
				});

		  arrDisp.addPropertyChangeListener(UVcDisp); // handles active antenna

		  arrDisp.addPropertyChangeListener(arrDispListener = new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  System.err.println("** arrDisp changed - updating uv coverage(1)");
						  int size = UVpDisp.fft.imsize;
						  UVcDisp.uvCoverage(size);
					 }
				});

		  UVpDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  if (e.getPropertyName()=="fft") {
								System.err.println("** UVp fft changed");
								FFTArray fft = (FFTArray) e.getNewValue();
								SquareArray uvcov = UVcDisp.getUVCoverage();
								if (uvcov!=null) {
									 UVpConvDisp.applyUVc(uvcov, fft);
								} else {
									 System.err.println("** Null uvcov");
								}
						  }
					 }
				});

		  UVcDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  if (e.getPropertyName()=="uvcov") {
								System.err.println("** UV coverage changed - updating convolution");
								SquareArray uvcov =  (SquareArray)e.getNewValue(); 
								UVpConvDisp.applyUVc(uvcov, UVpDisp.fft);
						  }
 					 }
				});

		  UVpConvDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  if (e.getPropertyName()=="fft") {
								System.err.println("** UVpConvDisp fft changed");
								FFTArray fft = (FFTArray) e.getNewValue();
								imgDisp2.invfft(fft);
						  }
					 }
				});

		  // this initialization stuff should be 
		  // pushed down to the actual objects


		  vriObservatory o = obsman.select("EVN");
		  arrDisp.setObservatory(o);
		  arrDisp.repaint();

 		  String c = obs.defaultConfig();
		  System.err.println("Default configuration: "+c);
		  arrEdit.config.setSelectedItem(c);
		  setVisible(true);
		  imgEdit.setImage("Wide double");
	 }
}


class vriDisplayCtrl extends JPanel {
	 vriDisplay disp;
	 JButton zoomIn;
	 JButton zoomOut;
	 JButton zoomReset;

	 public vriDisplayCtrl(vriDisplay d) {
		  disp = d;

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
	 void setDisplay(vriDisplay d) {
		  disp = d;
	 }
}

class vriUVcZoomChooser extends JPanel {
	 vriUVcDisp disp;

	 void setDisplay(vriUVcDisp d) {
		  disp = d;
	 }

	 public vriUVcZoomChooser(String s, vriUVcDisp d) {
		  disp = d;

		  setLayout(new GridLayout(0, 1));
		  String arrayString = "Array scale";
		  String earthString = "Earth scale";
		  String spaceString = "Space scale";


		  JRadioButton arrayButton = new JRadioButton(arrayString);
		  add(arrayButton);
		  JRadioButton earthButton = new JRadioButton(earthString);
		  add(earthButton);
		  JRadioButton spaceButton = new JRadioButton(spaceString);
		  spaceButton.setSelected(true);
		  add(spaceButton);

		  ButtonGroup group = new ButtonGroup();
		  group.add(arrayButton);
		  group.add(earthButton);
		  group.add(spaceButton);

		  arrayButton.addActionListener(new ActionListener() 
				{
					 public void actionPerformed(ActionEvent ae) {
						  System.err.println("Array button pressed");
						  disp.setPlotScale(Scale.ARRAY);
						  disp.repaint();
					 }
				});
		  earthButton.addActionListener(new ActionListener() 
				{
					 public void actionPerformed(ActionEvent ae) {
						  System.err.println("Earth button pressed");
						  disp.setPlotScale(Scale.EARTH);
						  disp.repaint();
					 }
				});
		  spaceButton.addActionListener(new ActionListener() 
				{
					 public void actionPerformed(ActionEvent ae) {
						  System.err.println("Space button pressed");
						  disp.setPlotScale(Scale.SPACE);
						  disp.repaint();
					 }
				});
	 }
}

class AstroImage {
	 String name;
	 String filename;
	 double scale; // arc seconds

	 AstroImage(String n, String fn, double s) {
		  name = n;
		  filename = fn;
		  scale = s;
	 }
	 double getScale() {
		  return scale;
	 }
	 double getUVScale() {
		  return 360.0*60.0*60.0/scale;
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
		  
		  images.add(new AstroImage("Wide double", "wide_double.gif", 1.0));
		  images.add(new AstroImage("Wide double smaller", "wide_double.gif", 0.2));
		  images.add(new AstroImage("Wide double even smaller", "wide_double.gif", 0.005));
		  images.add(new AstroImage("Radio galaxy", "radio_galaxy.gif", 0.2));

		  src_data = new String[images.size()];
		  for (int i=0; i<images.size(); i++) {
				AstroImage ai = images.get(i);
				src_data[i] = ai.name;
				imageMap.put(ai.name, ai);
		  }

		  add(src_label = new JLabel("Source:",Label.RIGHT));
		  add(src_choice = new JComboBox(src_data));
		  src_choice.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = src_choice.getSelectedItem().toString();
						  setImage(s);
					 }
				});
	 }

	 void setImage(String s) {
		  AstroImage ai = imageMap.get(s);
		  System.out.println("Loading "+ai.filename);
		  parent.imgDisp.loadAstroImage(ai);
		  parent.setAstroScale(ai.scale);
		  parent.setUVScale(ai.getUVScale());
		  // FIXME: this should *definitely* be somewhere else!
		  parent.UVcDisp.uvCoverage(parent.UVpDisp.fft.imsize);
	 }

}


class vriAuxEdit extends JPanel 
{
	 vriAuxiliary aux;
	 vriObservatory obs;
	 HourAngleDec had;
	 vri parent;

	 public vriAuxEdit(vri p, vriAuxiliary a, vriObservatory o) {
		  parent = p;
		  aux = a;
		  obs = o;

		  setLayout(new FlowLayout());
		  
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
						  parent.UVcDisp.repaint();
						  int size = parent.UVpDisp.fft.imsize;
						  parent.UVcDisp.uvCoverage(size); 
					 }
				});
	 }

	 void setObservatory(vriObservatory o) {
		  obs = o;
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

		  String[] site_choice_data = {"MERLIN",
												 "EVN",
												 "IYA"};
		  add(new JLabel("Obs:",Label.RIGHT));

		  add(site_choice = new JComboBox(site_choice_data));
		  site_choice.setSelectedItem("EVN");

		  site_choice.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = site_choice.getSelectedItem().toString();
						  parent.obs = parent.obsman.select(s);
						  String c = parent.obs.defaultConfig();
						  parent.setArrDisp(parent.obs);
						  parent.auxEdit.setObservatory(parent.obs);
						  parent.auxEdit.had.setDecRange();
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
}

