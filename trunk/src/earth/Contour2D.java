package nl.jive.earth;

import java.util.*;
import java.awt.geom.Point2D;

public class Contour2D {
	 LinkedList<Point2D.Float> points;
	 boolean closed;
	 Contour2D(LinkedList<Point2D.Float> apoints, boolean aclosed) {
		  points = apoints;
		  closed = aclosed;
	 }
	 public LinkedList<Point2D.Float> getPoints() {
		  return points;
	 }
	 public boolean isClosed() {
		  return closed;
	 }
}

