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
	 Image image;       // Image of antenna for ArrDisp
	 // We need this because the antenna is plotted wrt
	 // the top-left (not centre) of the image
	 int xoff = 0;      // Image X-offset due to top-left (not centre) positioning
	 int yoff = 0;      // Image Y-offset due to top-left (not centre) positioning
	 vriObservatory obs;  // Observatory being used
	 vriLocation pick;    // Antenna selected by the mouse
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

		  addMouseListener(new Mousey());
		  addMouseMotionListener(new MoveyMousey());
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

	 public void stationLock() {
		  int s = 0;
		  double dist;

		  for (int i = 0; i < obs.antennas.length; i++) {
				vriLocation a = obs.antennas[i];
				double bestdist = Double.MAX_VALUE;
				for (int j = 0; j < obs.stations.length; j++) {
					 vriLocation station = obs.stations[j];
					 dist = (Math.pow((a.NS - station.NS), 2.0) + 
								Math.pow((a.EW - station.EW), 2.0));
					 if (dist < bestdist) {
						  s = j;
						  bestdist = dist;
					 }
				}
				obs.antennas[i].NS = obs.stations[s].NS;
				obs.antennas[i].EW = obs.stations[s].EW;
		  }
		  repaint();
	 }

	 public void update(Graphics g) {
		  paint(g);
	 }

	 private void paintBackground(Graphics g) {
		  g.setColor(bg);
		  Rectangle r = getBounds();
		  g.fillRect(0, 0, r.width-1, r.height-1);
		  plotFocus(g);

		  g.setColor(Color.black);
		  obs.ref.x = displayCentre.x;
		  obs.ref.y = displayCentre.y;
		  obs.ref.xyz2NEU(obs.ref, displayScale, displaySize);
		  // Plot observatory centre
		  g.drawLine(obs.ref.x, obs.ref.y+2, obs.ref.x, obs.ref.y-2);
		  g.drawLine(obs.ref.x+2, obs.ref.y, obs.ref.x-2, obs.ref.y);

	 }		  

	 private void paintTracks(Graphics g) {
		  g.setColor(Color.black);
		  for (int j = 0; j < obs.trk.length; j++) {
				vriTrack track = obs.trk[j];
				track.start.NEU2xyz(obs.ref, displayScale, displaySize);
				track.end.NEU2xyz(obs.ref, displayScale, displaySize);
				g.drawLine(track.start.x, track.start.y, track.end.x, track.end.y);
		  }
	 }
	 private void paintStations(Graphics g) {
		  // Recall: Stations are configuration specific 
 		  // pre-allocated positions for antennas.
		  // Not to be confused with antennas themselves.
		  for (int j = 0; j < obs.stations.length; j++) {
				vriLocation station = obs.stations[j];
				station.NEU2xyz(obs.ref,displayScale,displaySize);
				g.drawOval(station.x-2, station.y-2, 4, 4);
		  }
		  
	 }

	 private void paintScale(Graphics g) {
		  // Draw a scale
		  Rectangle r = getBounds();
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
		  g.drawString(s, 10, r.height-12);
	 }

	 private void paintAntennas(Graphics g) {
		  // Calculate antenna image sizes
		  // use one image; all antennas look the same.
		  imgw = image.getWidth(this);
		  imgh = image.getHeight(this);
		  xoff = imgw/2;
		  yoff = imgw/2;
		  for(int i = 0; i < obs.antennas.length; i++) {
				obs.antennas[i].NEU2xyz(obs.ref, displayScale, displaySize);
				g.drawImage(image, obs.antennas[i].x-xoff, obs.antennas[i].y-yoff, this);
		  }
	 }

	 public void paint(Graphics g) {
		  Rectangle r = getBounds();
		  
		  paintBackground(g);
		  paintTracks(g);
		  paintStations(g);
		  paintAntennas(g);
		  paintScale(g);

	 }

	 class Mousey extends MouseAdapter {
		  public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				double bestdist = Double.MAX_VALUE;
				for (int i = 0; i < obs.antennas.length; i++) {
					 vriLocation a = obs.antennas[i];
					 double dist = (a.x - x) * (a.x - x) + (a.y - y) * (a.y - y);
					 if (dist < bestdist) {
						  pick = a;
						  bestdist = dist;
					 }
				}
				pick.x = x;
				pick.y = y;
				pick.xyz2NEU(obs.ref, displayScale, displaySize);
				repaint();
				propChanges.firePropertyChange("active", null, pick);
				edit.config.setSelectedItem("custom");
		  }

		  public void mouseReleased(MouseEvent e) {
				pick.x = e.getX();
				pick.y = e.getY();
				pick.xyz2NEU(obs.ref, displayScale, displaySize);
				repaint();
				propChanges.firePropertyChange("active", pick, null);
		  }
	 }

	 class MoveyMousey extends MouseMotionAdapter {
		  public void mouseDragged(MouseEvent e) {
				pick.x = e.getX();
				pick.y = e.getY();
				pick.xyz2NEU(obs.ref, displayScale, displaySize);
				repaint();
				propChanges.firePropertyChange("active", null, pick); 
				// The above used to be ("active", pick, pick), but nothing seemed to happen
				// I suspect smartalecry that checks it is actually a change
		  }
	 }
}
