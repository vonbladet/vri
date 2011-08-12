package nl.jive.vri;

import nl.jive.earth.*;
import java.util.*; // Math

interface isVisible {
	 boolean isVisible(double ha, double dec);
}

public class Telescope
	 implements isVisible
{
	 public String code, name;
	 public LatLon position;

	 Telescope(String acode, String aname, double alon, double alat) {
		  code = acode;
		  aname = aname;
		  position = new LatLon(alon, alat);
	 }

	 public boolean isVisible(double ha, double dec) {
		  Point3D p1 = Projectable.make3D(position);
		  Point3D p2 = Projectable.rotate(ha, dec, p1);
		  return (p2.y>0);
	 }
}
