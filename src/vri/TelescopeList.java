package nl.jive.vri;

import java.util.*;
import java.awt.geom.*;
import nl.jive.earth.*;

abstract class TList<T> extends ArrayList<T> {
	 
	 HashMap<Baseline, Integer[]> bl2ants = 
		  new HashMap<Baseline, Integer[]>();

	 abstract Baseline makeBaseline(T ant1, T ant2);

	 ArrayList<Baseline> getBaselines() {
		  ArrayList<Baseline> baselines = new ArrayList<Baseline>();
		  for (int i = 0; i < (this.size()-1); i++) {
				T ant1 = this.get(i);
				for (int j = i+1; j < (this.size()); j++) {
					 T ant2 = this.get(j);
					 Baseline bl = makeBaseline(ant1, ant2);
					 bl2ants.put(bl, new Integer[] {i, j});
					 baselines.add(bl);
				}
		  }
		  return baselines;
	 }

	 boolean isAntennaInBaseline(int i, Baseline bl) {
		  Integer[] ind = bl2ants.get(bl);
		  if (ind==null) {
				return false;
		  } else {
				return (ind[0] == i || ind[1] == i);
		  }
	 }
}


class NSEWTelescopeList extends TList<vriLocation>
{
	 double latitude;

	 NSEWTelescopeList(double aLatitude) {
		  latitude = aLatitude;
	 }
	 
	 Baseline<vriLocation> makeBaseline(vriLocation ant1, vriLocation ant2) {
		  double bx = (ant1.NS - ant2.NS) * Math.sin(latitude);
		  double by = (ant1.EW - ant2.EW);
		  double bz = (ant1.NS - ant2.NS) * Math.cos(latitude);
		  return new Baseline<vriLocation>(ant1, ant2, bx, by, bz);
	 }
}

class LatLonTelescopeList extends TList<Telescope> {
	 double lonRef, latRef;

	 public ArrayList<ProjectedTelescope> rotateAndProject(double lon, 
																			 double lat) {
		  ArrayList<ProjectedTelescope> result = 
				new ArrayList<ProjectedTelescope>();
		  for (Telescope t : this) {
				Point3D p1 = Projectable.make3D(t.position);
				Point3D p2 = Projectable.rotate(lon, lat, p1);
				if (p2.y <= 0) continue;
				else {
					 Point2D.Double proj = new Point2D.Double(p2.x, p2.z);
					 result.add(new ProjectedTelescope(t, proj));
				}
		  }
		  return result;
	 }
	 
	 Baseline<Telescope> makeBaseline(Telescope ant1, Telescope ant2) {
		  LatLon ll1 = ant1.position;
		  LatLon ll2 = ant2.position;
				
		  double rlon1 = Math.toRadians(ll1.lon);
		  double rlat1 = Math.toRadians(ll1.lat);
		  double rlon2 = Math.toRadians(ll2.lon);
		  double rlat2 = Math.toRadians(ll2.lat);

		  double bx = Constants.r_earth*(Math.sin(rlon1)*Math.cos(rlat1) - 
													Math.sin(rlon2)*Math.cos(rlat2));
		  double by = Constants.r_earth*(Math.cos(rlon2)*Math.cos(rlat2) - 
													Math.cos(rlon1)*Math.cos(rlat1));
		  double bz = Constants.r_earth*(Math.sin(rlat2) - Math.sin(rlat1));
		  return new Baseline<Telescope>(ant1, ant2, bx, by, bz);
	 }

}
