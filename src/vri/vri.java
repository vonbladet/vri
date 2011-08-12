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
import nl.jive.earth.Component;

public class vri extends JApplet
{

	 Geometry geom;
	 public vriArrDisp arrDisp;
	 PropertyChangeListener arrDispListener;
	 public vriImgDisp imgDisp;
	 public vriImgDisp imgDisp2;
	 public vriUVcDisp UVcDisp;
	 public vriUVpDisp UVpDisp;
	 public vriUVpDisp UVpConvDisp;
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

	 vri() {
		  geom = new Geometry();
		  System.err.println("Geometry installed in big vri");
	 }		  

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


	 void setArrDisp(vriObservatory obs) {
		  allImgPanel.remove(arrDisp);
		  allImgPanel.remove(arrCtrl);
		  arrDisp.removePropertyChangeListener(UVcDisp);
		  arrDisp.removePropertyChangeListener(arrDispListener);
		  
		  GridBagConstraints gbc = new GridBagConstraints();
		  arrDisp = obs.getArrDisp();
		  ArrayList<Component> g = geom.geomap.get(obs.menu_name);
		  arrDisp.setGeometry(g);
									 
		  gbc.gridx = 1;
		  gbc.gridy = 1;
		  allImgPanel.add(arrDisp, gbc);

		  arrCtrl = obs.getDispCtrl(arrDisp);
		  gbc.gridx = 1;
		  gbc.gridy = 2;
		  allImgPanel.add(arrCtrl, gbc);

		  UVcDisp.setObservatory(obs);
		  UVcDisp.repaint();

		  allImgPanel.repaint();

		  arrDisp.addPropertyChangeListener(UVcDisp); // handles active antenna
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

                  gbc.gridwidth = 3;
		  // First Column
		  gbc.gridx = 0;
		  gbc.gridy = 0;
		  allImgPanel.add(new JLabel("Source Image"), gbc);

		  imgDisp = new vriImgDisp(this);
		  gbc.gridx = 0;
		  gbc.gridy = 1;
		  allImgPanel.add(imgDisp, gbc);
		  
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
		  
		  arrDisp = obs.getArrDisp();
		  ArrayList<Component> g = geom.geomap.get(obs.menu_name);
		  if (g==null) {
				System.err.println("Geometry is null for "+obs.menu_name+"!");
		  } else {
				arrDisp.setGeometry(g);
		  }
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

		  UVcDisp = new vriUVcDisp(obs, aux);
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
		  imgDisp.addPropertyChangeListener(UVpDisp); // -> dat, fftsize
		  imgDisp.addPropertyChangeListener(UVcDisp); // -> fftsize
		  arrDisp.addPropertyChangeListener(UVcDisp); // -> active antenna
		  UVpDisp.addPropertyChangeListener(UVpConvDisp); // -> FFT
		  UVcDisp.addPropertyChangeListener(UVpConvDisp); // -> uvcov
		  UVpConvDisp.addPropertyChangeListener(imgDisp2); // -> fftconv
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
		  auxEdit = new vriAuxEdit(this, aux, obs);

		  String obsname = obsEdit.getObservatory();
		  String imgname = imgEdit.getImage();

		  // Place the components on the screen
		  
		  makeControls();
		  makeImagePane();
		  makeListeners();

		  arrDisp.setObservatory(obs);
                  auxEdit.setObservatory(obs);
		  arrDisp.repaint();

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


