package nl.jive.vri;

import java.util.*;
import java.lang.*;
import java.lang.Math;
import java.beans.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;

import nl.jive.earth.*;
import nl.jive.earth.Component;

class vriFlatLatLonArrDisp extends vriArrDisp 
{
	 vriBigObservatory obs;
	 int pick;

	 vriFlatLatLonArrDisp(vriObservatory o) {
		  super(o);
		  // otherwise it gets set to something small
		  // remember to flip y!
		  trans = AffineTransform.getScaleInstance(1.0, -1.0); 
		  addMouseListener(new Mousey());
		  addMouseMotionListener(new MoveyMousey());
	 }

	 public Dimension getPreferredSize() {
		  int width, height;
		  width = 3*256+48;
		  height = 256;
		  return new Dimension(width, height);
	 }

	 void setObservatory(vriObservatory aobs) {
		  obs = (vriBigObservatory) aobs;
		  // new, to propagate to UVcDisp
		  System.err.println("FlatLatLonArrDisp changing observatory.");
		  propChanges.firePropertyChange("Observatory", null, obs);
	 }

	 void setLongitude(double l) {
		  longitude = l;
	 }

	 void paintBackground(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  double displayScale = getDisplayScale();
		  if (geometry != null) {
				g2.setColor(Color.blue);
				g2.fillRect(0, 0, r.width-1, r.height-1);
				AffineTransform cache = g2.getTransform();

				g2.translate(getWidth()/2, getHeight()/2);				
				g2.setColor(bg);
				for (Component c : geometry) {
					 LinkedList<Point2D.Float> pts = c.getPoints2D();
					 GeneralPath poly = pointsToPolygon(pts, trans);
					 g2.fill(poly);
				}
				g2.setTransform(cache);
		  } else {
				System.err.println("No geography");
				g2.setColor(bg);
				g2.fillRect(0, 0, r.width-1, r.height-1);
		  }
		  plotFocus(g);
	 }

	 void paintAntennas(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  AffineTransform cache = g2.getTransform();
		  g2.translate(getWidth()/2, getHeight()/2);

		  LatLonTelescopeList telescopes = obs.antennas;
		  int imgw = image.getWidth(this);
		  int imgh = image.getHeight(this);
		  for (Telescope t : telescopes) {
				LatLon ll = t.position;
				Point2D.Float p1 = new Point2D.Float((float)ll.lon, 
																 (float)ll.lat);
				Point2D.Float p2 = new Point2D.Float();
				trans.transform(p1, p2);
				g2.drawImage(image, (int)(p2.x-imgw/2), (int)(p2.y-imgh/2), this);

		  }
		  g2.setTransform(cache);
	 }

	 public void paint(Graphics g) {
		  System.err.println("FlatLatLonArrDisp::paint called");
		  trans = AffineTransform.getScaleInstance((float)getWidth()/360.0,
																 (float)-getHeight()/180.0);
		  trans.preConcatenate(aff);

		  Graphics2D g2 = (Graphics2D) g;	 
		  paintBackground(g);
		  paintAntennas(g);
	 }

	 Point screenToGeom(Point p1) 
	 throws NoninvertibleTransformException 
	 {
		  AffineTransform shiftOrigin = AffineTransform.getTranslateInstance(getWidth()/2, 
																									getHeight()/2);
		  Point p2 = new Point();				
		  Point p3 = new Point();
		  shiftOrigin.inverseTransform(p1, p2);
		  trans.inverseTransform(p2, p3);
		  return p3;
	 }

	 int pointToAntenna(Point p2) 
	 {
		  int ind = 0;
		  double bestdist = Double.MAX_VALUE;
		  for (int i = 0; i < obs.antennas.size(); i++) {
				LatLon pos = obs.antennas.get(i).position;
				double dist = ((pos.lon - p2.x) * (pos.lon - p2.x) +
									(pos.lat - p2.y) * (pos.lat - p2.y));
				if (dist < bestdist) {
					 ind = i;
					 bestdist = dist;
				}
		  }
		  return ind;
	 }

	 class Mousey extends MouseAdapter {
		  public void mousePressed(MouseEvent e) {
				System.err.println("** MousePressed");
				Graphics2D g2 = (Graphics2D) getGraphics();
				Point p1 = e.getPoint();
				try {
					 Point p2 = screenToGeom(p1);
					 pick = pointToAntenna(p2);
					 LatLon pos = obs.antennas.get(pick).position;
					 pos.lon = p2.x;
					 pos.lat = p2.y;
					 repaint();
					 propChanges.firePropertyChange("active", -1, pick);
				} catch (NoninvertibleTransformException ex) {
					 System.err.println("Transformation not invertible");
				}
		  }

		  public void mouseReleased(MouseEvent e) {
				System.err.println("** MouseReleased");
				repaint();
				propChanges.firePropertyChange("active", pick, -1);
		  }
	 }

	 class MoveyMousey extends MouseMotionAdapter {
		  public void mouseDragged(MouseEvent e) {
				System.err.println("** MouseDragged");
				Graphics2D g2 = (Graphics2D) getGraphics();
				Point p1 = e.getPoint();
				try {
					 Point p2 = screenToGeom(p1);
					 LatLon pos = obs.antennas.get(pick).position;
					 pos.lon = p2.x;
					 pos.lat = p2.y;
					 repaint();
					 propChanges.firePropertyChange("active", null, pick); 
					 // The above used to be ("active", pick, pick), but nothing seemed to happen
					 // I suspect smartalecry that checks it is actually a change
				} catch (NoninvertibleTransformException ex) {
					 System.err.println("Transformation not invertible");
				}
		  }
	 }
}	 
