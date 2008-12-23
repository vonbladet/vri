package nl.jive.vri;

import java.util.*;
import java.lang.*;
import java.lang.Math;
import java.beans.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
//import java.applet.Applet;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;

import nl.jive.earth.*;
import nl.jive.earth.Component;
import nl.jive.earth.Point2D;

abstract class vriArrDisp extends vriDisplay {
	 AffineTransform trans;
	 vriObservatory obs;  // Observatory being used
	 vriArrEdit edit;    // Edit panel associated with this instance
	 Image image;       // Image of antenna for ArrDisp
	 Color bg = new Color(174, 255, 81);

	 vriArrDisp(vriObservatory o, vriArrEdit e) {
		  super();
		  setObservatory(o);
		  loadImage();
		  edit = e;
		  propChanges = new PropertyChangeSupport(this);
	 }

	 void loadImage() {
		  // Now load in the antenna images/icons for display on the site map
		  // Note that we wait for them to finish loading before proceding
		  try {
				URL u = getClass().getResource("antenna.gif");
				image = Toolkit.getDefaultToolkit().getImage(u);
		  } catch (Exception exc) {
				System.err.println("Error with antenna icon load");
		  }
	 }		  

	 void setImage(Image i) {
		  image = i;
	 }

	 static Polygon pointsToPolygon(LinkedList<Point2D> pts, 
											  AffineTransform t) {
		  double r_earth = 6378137.0;
		  int[] xs = new int[pts.size()];
		  int[] ys = new int[pts.size()];
		  int i=0;
		  for (Point2D p : pts) {
				Point p1 = new Point((int)(r_earth*p.x), 
											(int)(r_earth*p.y));
				Point p2 = new Point();
				t.transform(p1, p2);
				xs[i] = p2.x;
				ys[i] = p2.y;
				i++;
		  }
		  Polygon poly = new Polygon(xs, ys, pts.size());
		  return poly;
	 }


	 abstract void paintBackground(Graphics g);

	 abstract void paintAntennas(Graphics g);

	 void paintScale(Graphics g) {
		  // Draw a scale
		  Rectangle r = getBounds();
		  double displayScale = trans.getScaleX();
		  System.err.println("** Affscale: "+aff.getScaleX());
		  int viewWidth = getWidth();
		  double l = roundPower((viewWidth - 20.0) / displayScale); 
		  int m = (int) Math.round(l * displayScale); //
		  System.err.println("** m: "+m+" l: "+l);
		  double a = aff.getScaleX();
		  System.err.println("** Ratio: "+m/l/a);

		  g.drawLine(10, r.height-10, 10+m, r.height-10);
		  String s = new String();
		  if (l >= 1000.0) {
				s = Double.toString(l/1000.0) + "km";
		  } else {
				s = Double.toString(l) + "m";
		  }
		  g.drawString(s, 10, r.height-12);
	 }

	 abstract void setObservatory(vriObservatory o);
	 
}

class vriNSEWArrDisp extends vriArrDisp 
{
	 vriSmallObservatory obs;  // Observatory being used
	 vriLocation pick;    // Antenna selected by the mouse

	 vriNSEWArrDisp(vriObservatory o, vriArrEdit e) {
		  super(o, e);
		  addMouseListener(new Mousey());
		  addMouseMotionListener(new MoveyMousey());
		  repaint();
	 }

	 void setObservatory(vriObservatory aobs) {
		  obs = (vriSmallObservatory) aobs;
		  Dimension d = getSize();
		  int w = d.width;
		  int h = d.height;
		  if (aff==null) {
				System.err.println("Null affine transform");
		  }
		  if (obs.ref==null) {
				System.err.println("Null obs.ref");				
				System.err.println("Latitude: "+obs.latitude);				
		  }
		  aff.translate((int)obs.ref.EW, (int)obs.ref.NS);
		  defaultTransform = (AffineTransform) aff.clone();
		  repaint();
	 }

	 ArrayList<Contour2D> processComponents(ArrayList<Component> components) 
	 {
		  ArrayList<Contour2D> contours2D = new ArrayList<Contour2D>();
		  for (Component comp : components) {
				contours2D.addAll(comp.rotateAndProject(Math.toDegrees(-obs.longitude),
																	 Math.toDegrees(-obs.latitude)));
		  }
		  return contours2D;
	 }


	 void paintBackground(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  double displayScale = getDisplayScale();
		  if (obs.components != null) {
				g2.setColor(Color.blue);
				g2.fillRect(0, 0, r.width-1, r.height-1);
				g2.setColor(bg);
				AffineTransform cache = g2.getTransform();
				g2.translate(getWidth()/2, getHeight()/2);
				ArrayList<Contour2D> contours2D = processComponents(obs.components);

				for (Contour2D cont : contours2D) {
					 if (cont.isClosed()) {
						  LinkedList<Point2D> pts = cont.getPoints();
						  Polygon poly = pointsToPolygon(pts, trans);
						  Rectangle rec = poly.getBounds();
						  // System.err.println(String.format("*** x %d y %d width %d height %d",
						  //									  rec.x, rec.y, rec.width, rec.height));
						  g2.fill(poly);
					 } else {
						  System.err.println("*** Unclosed contour, skipping");
					 }

				}
				g2.setTransform(cache);
		  } else {
				System.err.println("No geography");
				g2.setColor(bg);
				g2.fillRect(0, 0, r.width-1, r.height-1);
		  }
		  plotFocus(g);
	 }
		  
	 void paintRef(Graphics g) {
		  Graphics2D g2 = (Graphics2D)g;
		  g.setColor(Color.black);
		  // Plot observatory centre
		  Point p1 = new Point((int)obs.ref.EW, (int)obs.ref.NS);
		  Point p2 = new Point();
		  trans.transform(p1, p2);
		  g2.drawLine(p2.x, p2.y+2, p2.x, p2.y-2);
		  g2.drawLine(p2.x+2, p2.y, p2.x-2, p2.y);

	 }
	 
	 void paintStations(Graphics g) {
		  // Recall: Stations are configuration specific 
 		  // pre-allocated positions for antennas.
		  // Not to be confused with antennas themselves.

		  Graphics2D g2 = (Graphics2D) g;

		  // System.err.println("Obs EW: "+obs.ref.EW+" NS: "+obs.ref.NS);

		  for (int j = 0; j < obs.stations.length; j++) {
				vriLocation station = obs.stations[j];
				Point p1 = new Point((int)station.EW, (int)station.NS);
				Point p2 = new Point();
				trans.transform(p1, p2);
				g2.drawOval(p2.x-2, p2.y-2, 4, 4);
		  }
	 }

	 void paintTracks(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  g.setColor(Color.black);
		  for (int j = 0; j < obs.trk.length; j++) {
				vriTrack track = obs.trk[j];
				Point p1 = new Point((int)track.start.EW, (int)track.start.NS);
				Point p2 = new Point((int)track.end.EW, (int)track.end.NS);
				Point p3 = new Point();
				Point p4 = new Point();
				trans.transform(p1, p3);
				trans.transform(p2, p4);
				
				g2.drawLine(p3.x, p3.y, p4.x, p4.y);
		  }
	 }

	 void paintAntennas(Graphics g) {
		  // Calculate antenna image sizes
		  // use one image; all antennas look the same.
		  Graphics2D g2 = (Graphics2D) g;

		  double displayScale = getDisplayScale();
		  int imgw = image.getWidth(this);
		  int imgh = image.getHeight(this);
		  double xoff = displayScale*imgw/2;
		  double yoff = displayScale*imgw/2;
		  for (int i = 0; i < obs.antennas.length; i++) {
				vriLocation ant = obs.antennas[i];
				Point p1 = new Point((int)ant.EW, (int)ant.NS);
				Point p2 = new Point();
				// System.err.println(String.format("Antenna %d EW: %f NS: %f", i, ant.EW, ant.NS));
				trans.transform(p1, p2);
				g2.drawImage(image, p2.x-imgw/2, p2.y-imgh/2, this);
		  }
	 }

	 public void paint(Graphics g) {
		  double geoScale = getWidth()/obs.getLengthScale();
		  AffineTransform geoTrans = AffineTransform.getScaleInstance(geoScale, -geoScale);
		  trans = (AffineTransform)geoTrans.clone();
		  // obs.ref is guaranteed to be (0,0), but still:
		  trans.concatenate(AffineTransform.getTranslateInstance(-obs.ref.EW, -obs.ref.NS)); 
		  trans.preConcatenate(aff);

		  Graphics2D g2 = (Graphics2D) g;	 
		  paintBackground(g);
		  AffineTransform a = g2.getTransform();
 		  g2.translate(getWidth()/2, getHeight()/2);

		  paintRef(g);
		  paintTracks(g);
		  paintStations(g);
		  paintAntennas(g);
		  g2.setTransform(a);
		  paintScale(g);

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

	 class Mousey extends MouseAdapter {
		  public void mousePressed(MouseEvent e) {
				System.err.println("** MousePressed");
				Graphics2D g2 = (Graphics2D) getGraphics();
				Point p1 = e.getPoint();
				try {
					 Point p2 = screenToGeom(p1);
					 double bestdist = Double.MAX_VALUE;
					 for (int i = 0; i < obs.antennas.length; i++) {
						  vriLocation ant = obs.antennas[i];
						  double dist = ((ant.EW - p2.x) * (ant.EW - p2.x) +
											  (ant.NS - p2.y) * (ant.NS - p2.y));
						  if (dist < bestdist) {
								pick = ant;
								bestdist = dist;
						  }
					 }
					 pick.EW = p2.x;
					 pick.NS = p2.y;
					 repaint();
					 propChanges.firePropertyChange("active", null, pick);
					 edit.config.setSelectedItem("custom");
				} catch (NoninvertibleTransformException ex) {
					 System.err.println("Transformation not invertible");
				}
		  }

		  public void mouseReleased(MouseEvent e) {
				System.err.println("** MouseReleased");
				repaint();
				propChanges.firePropertyChange("active", pick, null);
		  }
	 }

	 class MoveyMousey extends MouseMotionAdapter {
		  public void mouseDragged(MouseEvent e) {
				System.err.println("** MouseDragged");
				Graphics2D g2 = (Graphics2D) getGraphics();
				Point p1 = e.getPoint();
				try {
					 Point p2 = screenToGeom(p1);
					 pick.EW = p2.x;
					 pick.NS = p2.y;
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

class vriLatLonArrDisp extends vriArrDisp 
{
	 vriBigObservatory obs;

	 vriLatLonArrDisp(vriObservatory o, vriArrEdit e) {
		  super(o, e);
	 }

	 void setObservatory(vriObservatory aobs) {
		  obs = (vriBigObservatory) aobs;
	 }

	 ArrayList<Contour2D> processComponents(ArrayList<Component> components) 
	 {
		  ArrayList<Contour2D> contours2D = new ArrayList<Contour2D>();
		  for (Component comp : components) {
				contours2D.addAll(comp.rotateAndProject(Math.toDegrees(-obs.longitude),
																	 Math.toDegrees(-obs.latitude)));
		  }
		  return contours2D;
	 }

	 void paintBackground(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  double displayScale = getDisplayScale();
		  if (obs.components != null) {
				g2.setColor(Color.blue);
				g2.fillRect(0, 0, r.width-1, r.height-1);
				g2.setColor(bg);
				AffineTransform cache = g2.getTransform();
				g2.translate(getWidth()/2, getHeight()/2);
				ArrayList<Contour2D> contours2D = processComponents(obs.components);

				for (Contour2D cont : contours2D) {
					 if (cont.isClosed()) {
						  LinkedList<Point2D> pts = cont.getPoints();
						  Polygon poly = pointsToPolygon(pts, trans);
						  Rectangle rec = poly.getBounds();
						  g2.fill(poly);
					 } else {
						  System.err.println("*** Unclosed contour, skipping");
					 }
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
		  double r_earth = 6378137.0;
		  Graphics2D g2 = (Graphics2D) g;
		  AffineTransform cache = g2.getTransform();
		  g2.translate(getWidth()/2, getHeight()/2);

		  ArrayList<ProjectedTelescope> ptl = 
				obs.antennas.rotateAndProject(Math.toDegrees(-obs.longitude), 
														Math.toDegrees(-obs.latitude));
		  int imgw = image.getWidth(this);
		  int imgh = image.getHeight(this);
		  for (ProjectedTelescope pt : ptl) {
				Point2D p = pt.projection;
				System.err.println(String.format("Antenna at (%f, %f)", p.x, p.y));
				Point p1 = new Point((int)(r_earth*p.x), (int)(r_earth*p.y));
				Point p2 = new Point();
				trans.transform(p1, p2);

				// System.err.println(String.format("Transformed antenna at (%d, %d)", 
				// 											p2.x-imgw/2, p2.y-imgh/2));
				g2.drawImage(image, p2.x-imgw/2, p2.y-imgh/2, this);

		  }
		  g2.setTransform(cache);
	 }

	 public void paint(Graphics g) {
		  double geoScale = getWidth()/obs.getLengthScale();
		  AffineTransform geoTrans = AffineTransform.getScaleInstance(geoScale, -geoScale);
		  trans = (AffineTransform)geoTrans.clone();
		  trans.preConcatenate(aff);

		  Graphics2D g2 = (Graphics2D) g;	 
		  paintBackground(g);
		  paintAntennas(g);
		  paintScale(g);

	 }

}	 
