package nl.jive.vri;
import java.lang.*;
import java.lang.Math;

import java.beans.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;

class vriArrDisp extends vriDisplay {
	 Color bg;      // Background "grass" colour - could be obs. dependent :-)
	 Applet applet;     // Applet identity (for image loading)
	 int imgw = -1;    // Width of antenna images
	 int imgh = -1;    // Height of antenna images
	 double im_scale = 1.00;    // 0.64 is nicer
	 Image image;       // Image of antenna for ArrDisp
	 int scaled_imgw = -1;   // Width of image after scaling
	 int scaled_imgh = -1;   // Height of image after scaling
	 // We need this because the antenna is plotted wrt
	 // the top-left (not centre) of the image
	 int xoff = 0;      // Image X-offset due to top-left (not centre) positioning
	 int yoff = 0;      // Image Y-offset due to top-left (not centre) positioning
	 vriObservatory obs;  // Observatory being used
	 vriAntenna pick;    // Antenna selected by the mouse
	 vriArrEdit edit;    // Edit panel associated with this instance

	 public vriArrDisp(Applet app, vriObservatory o,
							 vriArrEdit e) {
		  super();
		  propChanges = new PropertyChangeSupport(this);
		  applet = app;
		  obs = o;
		  edit = e;

		  setObservatory(obs);
		  bg = new Color(174,255,81);

		  // Now load in the antenna images/icons for display on the site map
		  // Note that we wait for them to finish loading before proceding
		  try {
				URL u = getClass().getResource("antenna.gif");
				image = Toolkit.getDefaultToolkit().getImage(u);
		  } catch (Exception exc) {
				System.err.println("Error with antenna icon load");
		  }

		  displayScale = 300000.0;
		  defaultScale = 300000.0;
		  repaint();
	 }


	 public void setObservatory(vriObservatory aobs) {
		  obs = aobs;
		  Dimension d = getSize();
		  int w = d.width;
		  int h = d.height;

		  obs.ref.x = w/2;    // Set obs. reference point
		  obs.ref.y = h/2;
	 }

	 public void grow() {
		  im_scale *= 1.25;
		  if (im_scale > 2.0) im_scale = 2.0;
		  scaled_imgh = scaled_imgw = -1;
		  repaint();
	 }

	 public void shrink() {
		  im_scale /= 1.25;
		  if (im_scale < 0.1) im_scale = 0.1;
		  scaled_imgh = scaled_imgw = -1;
		  repaint();
	 }

	 public void randomize() {
		  Rectangle r = bounds();
		  for(int i = 0; i < obs.ant.length; i++) {
				obs.ant[i].x = (int)(10.0 + (r.width-20)*Math.random());
				obs.ant[i].y = (int)(10.0 + (r.height-20)*Math.random());
				obs.ant[i].xyz2NEU(obs.ref, displayScale, displaySize);
		  }
		  repaint();
	 }

	 public void stationLock() {
		  int s = 0;
		  double dist;

		  for (int i = 0; i < obs.ant.length; i++) {
				vriAntenna a = obs.ant[i];
				double bestdist = Double.MAX_VALUE;
				for (int j = 0; j < obs.stn.length; j++) {
					 dist = (Math.pow((a.NS - obs.stn[j].NS), 2.0) + 
								Math.pow((a.EW - obs.stn[j].EW), 2.0));
					 if (dist < bestdist) {
						  s = j;
						  bestdist = dist;
					 }
				}
				obs.ant[i].NS = obs.stn[s].NS;
				obs.ant[i].EW = obs.stn[s].EW;
		  }
		  repaint();
	 }

	 public void update(Graphics g) {
		  paint(g);
	 }

	 public void paint(Graphics g) {
		  Rectangle r = bounds();
		  System.err.println(String.format("Painting %s", obs.menu_name));
		  // Do background grass and plot the focus border
		  g.setColor(bg);
		  g.fillRect(0, 0, r.width-1, r.height-1);
		  plotFocus(g);

		  // Determine observatory reference point (for plotting)
		  obs.ref.x = displayCentre.x;
		  obs.ref.y = displayCentre.y;
		  obs.ref.xyz2NEU(obs.ref, displayScale, displaySize);

		  // Plot road/rail tracks
		  g.setColor(Color.black);
		  for(int j = 0; j < obs.trk.length; j++) {
				obs.trk[j].start.NEU2xyz(obs.ref, displayScale, displaySize);
				obs.trk[j].end.NEU2xyz(obs.ref, displayScale, displaySize);
				g.drawLine(obs.trk[j].start.x, obs.trk[j].start.y,
							  obs.trk[j].end.x, obs.trk[j].end.y);
		  }

		  // Plot antenna stations
		  for(int j = 0; j < obs.stn.length; j++) {
				obs.stn[j].NEU2xyz(obs.ref,displayScale,displaySize);
				g.drawOval(obs.stn[j].x-2, obs.stn[j].y-2, 4, 4);
		  }

		  // Plot observatory centre
		  g.drawLine(obs.ref.x, obs.ref.y+2, obs.ref.x, obs.ref.y-2);
		  g.drawLine(obs.ref.x+2, obs.ref.y, obs.ref.x-2, obs.ref.y);

		  // Calculate antenna image sizes
		  Image curimage = image;   // Allows us to scale curimage later. We only 
		  // use one image; all antennas look the same.
		  if (imgw < 0) {
				imgw = curimage.getWidth(this);
				imgh = curimage.getHeight(this);
				if (imgw < 0 || imgh < 0) {
					 return;
				}
		  }

		  // Determine scale based on zoom, etc.
		  if (scaled_imgw < 0) {
				scaled_imgw = (int)(imgw*im_scale);
				scaled_imgh = (int)(imgh*im_scale);
				xoff = (scaled_imgw) / 2;
				yoff = (scaled_imgh) / 2;
		  }

		  // Plot antennas
		  for(int i = 0; i < obs.ant.length; i++) {
				obs.ant[i].NEU2xyz(obs.ref, displayScale, displaySize);
				if (imgw != scaled_imgw || imgh != scaled_imgh) {
					 g.drawImage(curimage, obs.ant[i].x-xoff, obs.ant[i].y-yoff,
									 scaled_imgw, scaled_imgh, this);
				} else {
					 g.drawImage(curimage, obs.ant[i].x-xoff, obs.ant[i].y-yoff, this);
				}
		  }

		  // Draw a scale
		  double l = displayScale * (displaySize - 20.0) / displaySize;
		  l = Math.log(l)/Math.log(10.0);
		  l = Math.pow(10.0, Math.floor(l));
		  int m = (int) Math.round(l * displaySize / displayScale);
		  g.drawLine(10, r.height-10, 10+m, r.height-10);
		  String s = new String();
		  if (l >= 1000.0) {
				s = Double.toString(l/1000.0) + "km";
		  } else {
				s = Double.toString(l) + "m";
		  }
		  g.drawString(s, 10,r.height-12);

	 }
	 public boolean mouseDown(Event e, int x, int y) {
		  double bestdist = Double.MAX_VALUE;
		  for (int i = 0; i < obs.ant.length; i++) {
				vriAntenna a = obs.ant[i];
				double dist = (a.x - x) * (a.x - x) + (a.y - y) * (a.y - y);
				if (dist < bestdist) {
					 pick = a;
					 bestdist = dist;
				}
		  }
		  pick.x = x;
		  pick.y = y;
		  pick.xyz2NEU(obs.ref, displayScale, displaySize);
		  propChanges.firePropertyChange("obs", null, obs);
		  repaint();
		  edit.config.setSelectedItem("custom");
		  return true;
	 }

	 public boolean mouseDrag(Event e, int x, int y) {
		  // System.err.println("Mouse drag in ArrDisp");
		  pick.x = x;
		  pick.y = y;
		  pick.xyz2NEU(obs.ref, displayScale, displaySize);
		  propChanges.firePropertyChange("obs", null, obs);
		  repaint();
		  return true;
	 }

	 public boolean mouseUp(Event e, int x, int y) {
		  pick.x = x;
		  pick.y = y;
		  pick.xyz2NEU(obs.ref, displayScale, displaySize);
		  propChanges.firePropertyChange("obs", null, obs);
		  repaint();
		  return true;
	 }

}
