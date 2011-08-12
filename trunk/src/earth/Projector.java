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



class ProjectedComponent {
	 Nation nation;
	 Contour2D projection;
	 ProjectedComponent(Nation anation, Contour2D aprojection) {
		  nation = anation;
		  projection = aprojection;
	 }
}

class NationList extends ArrayList<Nation> {
	 ArrayList<ProjectedComponent> rotateAndProject(double lon, double lat) {
		  ArrayList<ProjectedComponent> result = new ArrayList<ProjectedComponent>();
		  for (Nation n: this) {
				for (Component component: n.getComponents()) {
					 for (Contour2D con: component.rotateAndProject(lon, lat)) {
						  result.add(new ProjectedComponent(n, con));
					 }
				}
		  }
		  return result;
	 }
}

