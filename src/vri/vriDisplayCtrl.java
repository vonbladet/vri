package nl.jive.vri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.beans.*;



abstract class vriDisplayCtrl extends JPanel {
	 abstract void addListeners();
}

class vriDisplayZoomCtrl extends vriDisplayCtrl {
	 vriDisplay disp;
	 JButton zoomIn;
	 JButton zoomOut;
	 JButton zoomReset;

	 vriDisplayZoomCtrl() {
		  makeButtons();
	 }

	 vriDisplayZoomCtrl(vriDisplay d) {
		  makeButtons();
		  disp = d;
		  addListeners();
	 }

	 void makeButtons() {
		  add(zoomIn = new JButton("In"));
		  add(zoomOut = new JButton("Out"));
		  add(zoomReset = new JButton("Reset"));

	 }

	 void addListeners() {
		  zoomIn.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent ae) {
						  disp.zoomIn();
					 }
				});
		  zoomOut.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent ae) {
						  disp.zoomOut();
					 }
				});
		  zoomReset.addActionListener(new ActionListener() {
					 public void actionPerformed(ActionEvent ae) {
						  disp.zoomReset();
					 }
				});
	 }

}

class vriDisplayRotateCtrl extends vriDisplayCtrl {
	 vriFlatLatLonArrDisp disp;
	 JSlider longitude;

	 vriDisplayRotateCtrl(vriArrDisp d) {
		  makeControls();
		  disp = (vriFlatLatLonArrDisp)d;
		  addListeners();
	 }

	 void makeControls() {
		  add(longitude = new JSlider(0, 360, 40));
	 }

	 void addListeners() {
		  longitude.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent ae) {
						  double l = (double)longitude.getValue();
						  System.err.println("Long:"+l);
						  disp.setLongitude(Math.toRadians(l));
						  disp.repaint();
					 }
				});
	 }

	 void setDisplay(vriArrDisp d) {
		  boolean noDisp = (disp==null);
		  disp = (vriFlatLatLonArrDisp) d;
		  if (noDisp) {
				addListeners();
		  }
	 }
}
