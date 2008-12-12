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
	 public vriArrDisp arrDisp;
	 public vriImgDisp imgDisp;
	 public vriImgDisp imgDisp2;
	 public vriUVcDisp UVcDisp;
	 public vriUVpDisp UVpDisp;
	 public vriUVpDisp UVpConvDisp;
	 public vriArrEdit arrEdit;
	 public vriImgEdit imgEdit;
	 public vriUVpEdit UVpEdit;
	 public vriAuxEdit auxEdit;
	 public vriObsEdit obsEdit;
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
		  f.setVisible(true);
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

		  obs = vriObservatory.select("MERLIN");

		  aux = new vriAuxiliary();

		  setLayout(new FlowLayout());

		  obsEdit = new vriObsEdit(this); 
		  imgEdit = new vriImgEdit(this); 
		  arrEdit = new vriArrEdit(this, obs); 
		  UVpEdit = new vriUVpEdit(this); 
		  auxEdit = new vriAuxEdit(this, aux, obs);

		  // Place the components on the screen
		  
		  JTabbedPane tabbedPane = new JTabbedPane();
		  JPanel obsEditPane = new JPanel();
		  obsEditPane.add(obsEdit);
		  tabbedPane.addTab("Observatory", obsEditPane);
		  JPanel imgEditPane = new JPanel();
		  imgEditPane.add(imgEdit);
		  tabbedPane.add("Image", imgEditPane);

		  JPanel arrEditPane = new JPanel();
		  arrEditPane.add(arrEdit);
		  tabbedPane.add("Arr", arrEditPane);

		  JPanel auxEditPane = new JPanel();
		  auxEditPane.add(auxEdit);
		  tabbedPane.add("Aux", auxEdit);

		  add(tabbedPane);

		  JPanel allImgPanel = new JPanel();
		  allImgPanel.setLayout(new GridLayout(2,3));

		  Container ImgPanel = Box.createVerticalBox();
		  imgDisp = new vriImgDisp(this);
		  ImgCtrl = new vriDisplayCtrl("?", imgDisp);
		  JPanel imgDispPanel = captionDisp("Source Image", imgDisp);
		  ImgPanel.add(imgDispPanel);
		  ImgPanel.add(ImgCtrl);
		  allImgPanel.add(ImgPanel);

		  Container ArrPanel = Box.createVerticalBox();
		  arrDisp = new vriArrDisp(this, obs, arrEdit);
		  ArrCtrl = new vriDisplayCtrl("Station\nlock", arrDisp); 
		  JPanel arrDispPanel = captionDisp("Array", arrDisp);
		  ArrPanel.add(arrDispPanel);
		  ArrPanel.add(ArrCtrl);
		  allImgPanel.add(ArrPanel);

		  Container ImgPanel2 = Box.createVerticalBox();
		  imgDisp2 = new vriImgDisp(this);
		  vriDisplayCtrl ImgCtrl2 = new vriDisplayCtrl("?", imgDisp2);
		  JPanel imgDispPanel2 = captionDisp("Reconstructed Image", imgDisp2);
		  ImgPanel2.add(imgDispPanel2);
		  ImgPanel2.add(ImgCtrl2);
		  allImgPanel.add(ImgPanel2);
		  
		  // Second row: UV equivalents of first row

		  Container UVpPanel = Box.createVerticalBox();
		  UVpDisp = new vriUVpDisp(this);
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

		  imgDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  UVpDisp.fft(imgDisp.dat);
						  // FIXME: would be nicer to check UVp is set
						  float uvcov[] = UVcDisp.uvCoverage(imgDisp.dat.imsize);
						  UVpConvDisp.applyUVc(uvcov, UVpDisp.fft);
					 }
				});
		  
		  UVcDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  float uvcov[] = UVcDisp.uvCoverage(imgDisp.dat.imsize);
						  UVpConvDisp.applyUVc(uvcov, UVpDisp.fft);
					 }
				});

		  UVpConvDisp.addPropertyChangeListener(new PropertyChangeListener() {
					 public void propertyChange(PropertyChangeEvent e) {
						  imgDisp2.invfft(UVpConvDisp.fft);
					 }
				});

		  arrDisp.addPropertyChangeListener(UVcDisp);

		  // this initialization stuff should be 
		  // pushed down to the actual objects

		  vriObservatory o = obs.select("MERLIN");
		  arrDisp.setObservatory(o);
		  arrDisp.repaint();

 		  String c = obs.defaultConfig();
 		  arrEdit.config.setSelectedItem(c);

		  UVpEdit.init();
		  setVisible(true);
		  imgEdit.src_choice.setSelectedItem("Wide double");

	 }
	 
	 // Obsolete event handler
	 public void actionPerformed(ActionEvent e) {
		  System.out.println("Got an event");
		  // We need this to handle the events that affect things more globally
		  JComponent source = (JComponent) e.getSource();
		  if (source == obsEdit.el_field) {
				Double d = new Double(obsEdit.el_field.getText());
				obs.ant_el_limit = d.doubleValue() * Math.PI / 180.0;
				obsEdit.site_choice.setSelectedItem("custom");
				// auxEdit.setDecRange(); //FIXME
				UVcDisp.repaint();
		  } else if (source == obsEdit.dia_field) {
				Double d = new Double(obsEdit.dia_field.getText());
				obs.ant_diameter = d.doubleValue();
				UVcDisp.repaint();
				obsEdit.site_choice.setSelectedItem("custom");
		  } else if (source == obsEdit.lat_field) {
				Double d = new Double(obsEdit.lat_field.getText());
				obs.latitude = d.doubleValue() * Math.PI / 180.0;
				// auxEdit.setDecRange(); // FIXME
				UVcDisp.repaint();
		  } else if (source == obsEdit.ant_field) {
				Integer n = Integer.valueOf(obsEdit.ant_field.getText());
				obs.selectNumAnt(n.intValue());
				obsEdit.site_choice.setSelectedItem("custom");
				UVcDisp.repaint();
				arrDisp.repaint();
		  }
	 }

	 /**
	  * A method to set the array configuration to "custom"
	  *
	  * @return void
	  * @author Derek J. McKay
	  */
	 public void setCustomConfig() {
		  arrEdit.config.setSelectedItem("custom");
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
								parent.arrDisp.repaint();
						  }
					 }
				});
				
		  add(stn_lock = new JButton("Station lock"));
		  stn_lock.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  System.err.println("Station lock button pressed");
						  parent.arrDisp.stationLock();
					 }
				});
		  Graphics gc = getGraphics();
		  paintAll(gc);
	 }
}


class AstroImage {
	 String filename;
	 double scale; // arc seconds
	 AstroImage(String fn, double s) {
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
	 public JComboBox src_choice;
	 public JLabel src_label;

	 public vriImgEdit(final vri parent) {
		  setLayout(new FlowLayout());

		  add(src_label = new JLabel("Source:",Label.RIGHT));
		  ArrayList<String> sd = new ArrayList<String>();
		  final Map<String, AstroImage> imageMap = new HashMap<String, AstroImage>();
		  // FIXME: There's a way of doing class-level initialisers;
		  // should we use that instead?
		  imageMap.put("Wide double", 
							new AstroImage("wide_double.gif", 1.0));
		  sd.add("Wide double");
		  imageMap.put("Wide double smaller", 
							new AstroImage("wide_double.gif", 0.2));
		  sd.add("Wide double smaller");
		  imageMap.put("Radio galaxy", 
							new AstroImage("radio_galaxy.gif", 0.2));
		  sd.add("Radio galaxy");
// 		  imageMap.put("Narrow double", 
// 							new AstroImage("narrow_double.gif", 100e-3));
// 		  imageMap.put("Point",
// 							new AstroImage("point.gif", 1e-3));
// 		  imageMap.put("Offset point", 
// 							new AstroImage("offset_point.gif", 10e-3));
// 		  imageMap.put("Wide gaussian", 
// 							new AstroImage("wide_gauss.gif", 10e-3));
// 		  imageMap.put("Narrow gaussian", 
// 							new AstroImage("narrow_gauss.gif", 10e-3));
// 		  imageMap.put("Disc", 
// 							new AstroImage("disc.gif", 10e-3));
// 		  imageMap.put("Crux", 
// 							new AstroImage("crux.gif", 10e-3));

		  String[] src_data = new String[sd.size()];
		  sd.toArray(src_data);


		  add(src_choice = new JComboBox(src_data));
		  src_choice.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
						  String s = src_choice.getSelectedItem().toString();
						  AstroImage ai = imageMap.get(s);
						  System.out.println("Loading "+ai.filename);
						  parent.imgDisp.loadAstroImage(ai);
						  // FIXME: this should surely be somewhere else?
						  parent.imgDisp2.setFullScale(ai.scale);
						  parent.UVpDisp.setFullScale(ai.getUVScale());
						  parent.UVpConvDisp.setFullScale(ai.getUVScale());
						  parent.UVcDisp.setConvScale(ai.getUVScale());
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




class vriAuxEdit extends JPanel 
implements ActionListener {
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

		  fr_field.addActionListener(this);

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
						  parent.UVcDisp.repaint();
					 }
				});
					 		  
		  bw_field.setEditable(false);
	 }



	 public void actionPerformed(ActionEvent e) {
		  Double d = new Double(fr_field.getText());
		  aux.freq = d.doubleValue();
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
						  parent.arrEdit.setupConfigMenu(parent.obs);
						  String c = parent.obs.defaultConfig();
						  parent.arrEdit.config.setSelectedItem(c);
						  parent.arrDisp.setObservatory(parent.obs);
						  parent.arrDisp.repaint();
						  parent.UVcDisp.setObservatory(parent.obs);
						  parent.UVcDisp.repaint();
						  parent.auxEdit.had.setDecRange();
						  parent.obsEdit.setFields(parent.obs);
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
		  ant_field.setText(String.format("%d", parent.obs.antennas.length));
		  dia_field.setText(String.format("%.3f", parent.obs.ant_diameter));
		  el_field.setText(String.format("%.3f", parent.obs.ant_el_limit * 180.0 / Math.PI));
	 }
}

//####################################################################//

