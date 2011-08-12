package nl.jive.earth;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.lang.Math;
import org.json.*;

import java.awt.*;
import java.awt.Shape.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.awt.Font.*;


public class Projectable {
	 public static Point3D make3D(LatLon ll) {
		  double lon = Math.toRadians(ll.lon);
		  double lat = Math.toRadians(ll.lat);
		  double x = Constants.r_earth*Math.cos(lat)*Math.sin(lon);
		  double y = Constants.r_earth*Math.cos(lat)*Math.cos(lon);
		  double z = Constants.r_earth*Math.sin(lat);
		  return new Point3D(x,y,z);

	 }
	 public static LinkedList<Point3D> make3D(List<LatLon> points2D) {
		  LinkedList<Point3D> result = new LinkedList<Point3D>();
		  for (LatLon ll : points2D) {
				result.addLast(make3D(ll));
		  }
		  return result;
	 }
	 public static Point3D rotate(double lon, double lat, Point3D p) {
		  double rlon = Math.toRadians(lon);
		  double x1 = Math.cos(rlon)*p.x + Math.sin(rlon)*p.y;
		  double y1 = -Math.sin(rlon)*p.x + Math.cos(rlon)*p.y;
		  Point3D p1 = new Point3D(x1, y1, p.z);
		  
		  double rlat = Math.toRadians(lat);
		  double y2 = Math.cos(rlat)*p1.y - Math.sin(rlat)*p1.z;
		  double z2 = Math.sin(rlat)*p1.y + Math.cos(rlat)*p1.z;
		  return new Point3D(p1.x, y2, z2);
	 }
	 public static LinkedList<Point3D> rotate(double lon, double lat, List<Point3D> points3D) {
		  LinkedList<Point3D> result = new LinkedList<Point3D>();
		  for (Point3D p : points3D) {
				result.add(Projectable.rotate(lon, lat, p));
		  }
		  return result;
	 }
}

