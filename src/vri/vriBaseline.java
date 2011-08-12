package nl.jive.vri;

import java.util.*;
import nl.jive.earth.*;
import java.beans.*;
import java.awt.geom.*;

class UV {
	 double u, v;
	 UV(double au, double av) {
		  u = au;
		  v = av;
	 }
}

class Baseline<T extends isVisible> { // parameterize by telescope type
	 T ant1 = null;
	 T ant2 = null;
	 double bx, by, bz;
	 Baseline() {
		  bx = 0.0;
		  by = 0.0;
		  bz = 0.0;
	 }

	 Baseline(T aant1, T aant2, double abx, double aby, double abz) {
		  ant1 = aant1;
		  ant2 = aant2;
		  bx = abx;
		  by = aby;
		  bz = abz;
	 }

	 double size() {
		  return Math.sqrt(bx*bx+by*by+bz*bz);
	 }

	 boolean isVisible(double ha, double dec) {
 		  return (ant1.isVisible(ha, dec) &&
 					 ant2.isVisible(ha, dec));
	 }

	 UV project(double ha, double dec, double scale) {
		  double cd = Math.cos(dec);
		  double sd = Math.sin(dec);
		  double sh = Math.sin(ha);
		  double ch = Math.cos(ha);

		  // The minus sign allows for english-order in antennas[i].y value (as 
		  // read from screen), i.e. positive is down.

		  double u = scale*(bx * sh + by * ch);
		  double v = scale*(-bx * sd * ch + by * sd * sh + bz * cd);
		  return new UV(u, v);
	 }

	 ArrayList<UV> makeUVPoints(double h1, double h2, 
                                    double dec, double scale) {
		  ArrayList<UV> res = new ArrayList<UV>();
		  
		  for (double h = h1; h <= h2; h += 0.06) {
				if (!isVisible(h, dec)) 
					 continue;
				UV uv = project(h, dec, scale);
				res.add(uv);
		  }
		  if (isVisible(h2, dec)) {
				UV uv = project(h2, dec, scale);
				res.add(uv);
		  }
		  return res;
	 }


	 GeneralPath makeUVGeneralPath(double h1, double h2, 
										double dec, double scale) {
		  
		  GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		  boolean first = true;
		  for (double h = h1; h <= h2+0.06; h += 0.06) {
				UV uv = project(h, dec, scale);
				if (!isVisible(h, dec)) {
					 first = true;
				} else {
					 if (first) {
						  gp.moveTo((float)uv.u, (float)uv.v);
						  first = false;
					 } else {
						  gp.lineTo((float)uv.u, (float)uv.v);
					 }
				}
		  }
		  GeneralPath gp2 = (GeneralPath) gp.clone();
		  gp2.transform(AffineTransform.getScaleInstance(-1.0, -1.0));
		  gp.append(gp2, false);
		  return gp;
	 }
}

