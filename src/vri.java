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
	 Container box;

	 JPanel allImgPanel;
	 double astroScale = 1; // arcseconds
	 double uvScale = 1000; // lambda (or klambda?)

	 public static void main(String args[]) {
		  System.out.println("Standalone java program");

		  JFrame f = new JFrame("VRI");
		  vri vriTest = new vri();
		  vriTest.init();
		  f.add("Center", vriTest);
		  f.setSize(900, 800);
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
		  double l = UVcDisp.getLambda();
		  if (UVpDisp!=null) {
				UVpDisp.setFullScale(s);  // FIXME! URGENTLY!
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
		  allImgPanel.remove(UVcDisp);
		  allImgPanel.remove(arrCtrl);
		  arrDisp.removePropertyChangeListener(UVcDisp);
		  arrDisp.removePropertyChangeListener(arrDispListener);
		  
		  GridBagConstraints gbc = new GridBagConstraints();
		  arrDisp = obs.getArrDisp(arrEdit);
		  gbc.gridx = 1;
		  gbc.gridy = 1;
		  allImgPanel.add(arrDisp, gbc);

		  arrCtrl = obs.getDispCtrl(arrDisp);
		  gbc.gridx = 1;
		  gbc.gridy = 2;
		  allImgPanel.add(arrCtrl, gbc);


		  UVcDisp = obs.getUVcDisp(aux);
		  gbc.gridx = 1;
		  gbc.gridy = 4;
		  allImgPanel.add(UVcDisp, gbc);


		  UVcDisp.setConvScale(uvScale);
		  UVcCtrl.setDisplay(UVcDisp);
		  UVcDisp.setPlotScale(UVcCtrl.current);

		  UVcDisp.repaint();

		  allImgPanel.repaint();

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


	 void makeImagePane() {
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

		  arrCtrl = obs.getDispCtrl(arrDisp);
		  gbc.gridx = 1;
		  gbc.gridy = 2;
		  allImgPanel.add(arrCtrl, gbc);

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
	 }

	 void makeControls() {
		  JPanel obsEditPane = new JPanel();
		  obsEditPane.add(imgEdit);
		  obsEditPane.add(obsEdit);
		  obsEditPane.add(auxEdit);
		  add(obsEditPane);
	 }

	 void makeListeners() {
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
	 }

	 public void init() {
		  System.out.println("VRI, Virtual Radio Interferometer");
		  setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		  setBackground(Color.lightGray);
		  obsman = new vriObservatoryManager();
		  obs = obsman.select("EVN");
		  aux = new vriAuxiliary();


		  obsEdit = new vriObsEdit(this); 
		  imgEdit = new vriImgEdit(this); 
		  arrEdit = new vriArrEdit(this, obs); 
		  auxEdit = new vriAuxEdit(this, aux, obs);

		  String obsname = obsEdit.getObservatory();
		  String imgname = imgEdit.getImage();

		  // Place the components on the screen
		  
		  makeControls();
		  makeImagePane();
		  makeListeners();

		  arrDisp.setObservatory(obs);
		  arrDisp.repaint();

 		  String c = obs.defaultConfig();
		  System.err.println("Default configuration: "+c);
		  arrEdit.config.setSelectedItem(c);
		  imgEdit.setImage("Medium double");
		  setVisible(true);
	 }
}



class vriUVcZoomChooser extends JPanel {
	 vriUVcDisp disp;
	 Scale current;

	 void setDisplay(vriUVcDisp d) {
		  disp = d;
	 }

	 public vriUVcZoomChooser(String s, vriUVcDisp d) {
		  disp = d;
		  current = Scale.SPACE;
		  
		  setLayout(new GridLayout(0, 1));
		  String arrayString = "Array scale";
		  String earthString = "Earth scale";
		  String spaceString = "Source scale";


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
						  current = Scale.ARRAY;
						  disp.setPlotScale(current);
						  disp.repaint();
					 }
				});
		  earthButton.addActionListener(new ActionListener() 
				{
					 public void actionPerformed(ActionEvent ae) {
						  System.err.println("Earth button pressed");
						  current = Scale.EARTH;
						  disp.setPlotScale(current);
						  disp.repaint();
					 }
				});
		  spaceButton.addActionListener(new ActionListener() 
				{
					 public void actionPerformed(ActionEvent ae) {
						  System.err.println("Space button pressed");
						  current = Scale.SPACE;
						  disp.setPlotScale(current);
						  disp.repaint();
					 }
				});
	 }
}

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
		  
		  images.add(new AstroImage("Wide double", "wide_double_256.gif", 2.0, 256));
		  images.add(new AstroImage("Medium double", "wide_double_256.gif", 0.2, 256));
		  images.add(new AstroImage("Narrow double", "wide_double_256.gif", 0.02, 256));
		  images.add(new AstroImage("Radio galaxy", "radio_galaxy_256.gif", 0.2, 256));

		  images.add(new AstroImage("m82", "m82_1.6GHz_MERLIN_256.png", 20.0, 256));
		  // images.add(new AstroImage("m82-small", "m82_1.6GHz_MERLIN_256.png", 6.7, 256));
		  images.add(new AstroImage("CSO", "CSO_5GHz_EVN_256.png", 0.2, 256));
		  images.add(new AstroImage("ss433", "ss433_1.6GHz_global_256.png", 0.1, 256));

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
		  System.out.println("Loading "+ai.filename);
		  parent.imgDisp.loadAstroImage(ai);
		  parent.setAstroScale(ai.scale);
		  parent.setUVScale(ai.getUVScale());
		  parent.UVcDisp.repaint();
		  // FIXME: this should *definitely* be somewhere else!
		  if (parent.UVpDisp.fft!=null) {
				parent.UVcDisp.uvCoverage(parent.UVpDisp.fft.imsize);
		  }
	 }

}


class vriAuxEdit extends JPanel 
{
	 vriAuxiliary aux;
	 vriObservatory obs;
	 simpleHourAngleDec had;
	 vri parent;

	 public vriAuxEdit(vri p, vriAuxiliary a, vriObservatory o) {
		  parent = p;
		  aux = a;
		  obs = o;

		  setLayout(new FlowLayout());
		  
		  had = new simpleHourAngleDec();
		  aux.ha1 = had.getHa1(); 
		  aux.ha2 = had.getHa2();
		  aux.dec = had.getDec();

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
								int size = parent.UVpDisp.fft.imsize;
								parent.UVcDisp.uvCoverage(size); 
						  }
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
		  add(new JLabel("Array:",Label.RIGHT));

		  add(site_choice = new JComboBox(site_choice_data));
		  site_choice.setSelectedItem("EVN");

		  site_choice.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = site_choice.getSelectedItem().toString();
						  setObservatory(s);
					 }
				});
	 }

	 void setObservatory(String s) {
		  parent.obs = parent.obsman.select(s);
		  String c = parent.obs.defaultConfig();
		  parent.setArrDisp(parent.obs);
		  parent.allImgPanel.validate();
		  parent.auxEdit.setObservatory(parent.obs);
		  parent.auxEdit.had.setDecRange();
		  System.err.println("Observatory set");
		  
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
						  parent.UVpDisp.type = type.getSelectedItem().toString();
						  parent.UVpDisp.fftToImg(parent.UVpDisp.fft);
					 }
				});
	 }
}

