package nl.jive.vri;

import java.util.*;
import java.lang.Math.*;
import nl.jive.earth.*;
import java.awt.geom.Point2D;


class EarthExpt
{
	 public static void main(String[] args) throws Exception {
		  final NationReader nr = new NationReader("gis.json");
		  double latitude = Math.toDegrees(+0.929160933);
		  double longitude = 0; // we don't have a value for this
		  try {
				Nation n = nr.getNation("United Kingdom");
				ArrayList<Contour2D> contours = new ArrayList<Contour2D>();
				for (Component comp : n.getComponents()) {
					 ArrayList<Contour2D> conts = comp.rotateAndProject(-longitude, -latitude);
					 contours.addAll(conts);
				}
				for (Contour2D cont : contours) {
					 LinkedList<Point2D.Float> pts = cont.getPoints();
					 for (Point2D.Float p : pts) {
						  System.out.println(String.format("%f %f", p.x, p.y));
					 }
					 System.out.println();
				}
		  } catch (NameNotFoundException e) {
				System.err.println("Can't find nation");
		  }
	 }
}
