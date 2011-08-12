package nl.jive.earth;

import java.util.*;
import java.lang.Math.*;
import java.awt.geom.Point2D;

public class Component {
	 LinkedList<LatLon> points2D;
	 LinkedList<Point3D> points3D;
	 Component(LinkedList<LatLon> apoints2D) {
		  points2D = apoints2D;
		  points3D = Projectable.make3D(points2D);
	 }

	 public LinkedList<Point2D.Float> getPoints2D() {
		  LinkedList<Point2D.Float> list = new LinkedList<Point2D.Float>();
		  for (LatLon ll : points2D) {
				list.add(new Point2D.Float((float)ll.lon, 
													(float)ll.lat));
		  }
		  return list;
	 }

	 LinkedList<Point2D.Float> drawHorizon(Point2D.Float p0, Point2D.Float p1) {
		  LinkedList<Point2D.Float> pts = 
				new LinkedList<Point2D.Float>();
		  double theta0 = Math.atan2(p0.y, p0.x);
		  double theta1 = Math.atan2(p1.y, p1.x);
		  double Dtheta = theta1-theta0;
		  if (Dtheta > Math.PI && (Math.signum(theta0)*Math.signum(theta1) <= 0)) {
				if (theta0 < 0) {
					 theta0 += 2*Math.PI;
				} else {
					 theta1 += 2*Math.PI;
				}
				Dtheta = theta1-theta0;
		  }

		  double da = Math.toRadians(1.0); 
		  int n = (int)Math.abs(Math.round(Dtheta/da));
		  for (int j=1; j<n; j++) {
				double theta = theta0 + (float)j/(float)n * Dtheta;
				double x = Constants.r_earth*Math.cos(theta);
				double y = Constants.r_earth*Math.sin(theta);
						  
				pts.add(new Point2D.Float((float)x, (float)y));
		  }
		  return pts;
	 }

 	 public ArrayList<Contour2D> rotateAndProject(double lon, double lat) {
		  ArrayList<Contour2D> res = new ArrayList<Contour2D>();
		  LinkedList<Point3D> rpoints = Projectable.rotate(lon, lat, points3D); 
		  // filter and split
		  LinkedList<Point2D.Float> splits = new LinkedList<Point2D.Float>();
		  ArrayList<LinkedList<Point2D.Float>> conts = 
				new ArrayList<LinkedList<Point2D.Float>>();
		  boolean closed = true;
		  Point3D pfirst = rpoints.get(0);
		  boolean startsInside = (pfirst.y >= 0);
		  for (Point3D p : rpoints) {
				if (p.y >= 0.0) {
					 splits.add(new Point2D.Float((float)p.x, (float)p.z));
				} else if (!splits.isEmpty()) {
					 closed = false;
					 conts.add(splits);
					 splits = new LinkedList<Point2D.Float>();
				}
		  }
		  if (!splits.isEmpty()) {
				conts.add(splits);
		  }
		  if (conts.size() == 0) {
				;
		  }
		  else if (closed) {
				for (LinkedList<Point2D.Float> c : conts)
					 res.add(new Contour2D(c, closed));
		  }
		  else {// ! closed
				if (startsInside) {
					 int n = conts.size();
					 LinkedList<Point2D.Float> l1 = conts.get(0);
					 conts.remove(0);
					 conts.get(conts.size()-1).addAll(l1);
// 					 System.err.println(String.format("Resized from %d to %d", n, conts.size()));
				} 
				if (conts.size()==1) {	
// 					 System.err.println(String.format("Single contour"));
					 LinkedList<Point2D.Float> pl = conts.get(0);
					 Point2D.Float p0 = pl.getFirst();
					 Point2D.Float p1 = pl.getLast();
					 pl.addAll(drawHorizon(p1, p0));
					 pl.add(p0);
					 res.add(new Contour2D(pl, true));
				} else {
					 conts.add(conts.get(0)); // make it circular
					 for (int i=0; i<conts.size()-1; i+=1) {
						  LinkedList<Point2D.Float> pl = 
								new LinkedList<Point2D.Float>();
						  LinkedList<Point2D.Float> c0 = conts.get(i);
						  pl.addAll(c0);
						  pl.addAll(drawHorizon(c0.getLast(), c0.getFirst()));
						  res.add(new Contour2D(pl, true));
					 }									 
				}
		  }
		  return res;
	 }			 
}
