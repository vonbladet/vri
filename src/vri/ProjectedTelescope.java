package nl.jive.vri;

import java.awt.geom.Point2D;

public class ProjectedTelescope
{
	 public Telescope telescope;
	 public Point2D.Double projection;
	 ProjectedTelescope(Telescope atelescope, Point2D.Double aprojection) {
		  telescope = atelescope;
		  projection = aprojection;
	 }
}

