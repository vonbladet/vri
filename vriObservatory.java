/*
 * vriObservatory.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

import java.util.*;

class UV {
	 double u, v;
	 UV(double au, double av) {
		  u = au;
		  v = av;
	 }
}

class Baseline {
	 double bx, by, bz;
	 vriLocation ant1, ant2;
	 Baseline(double abx, double aby, double abz) {
		  ant1 = null; // Unknown in this case
		  ant2 = null;
		  bx = abx;
		  by = aby;
		  bz = abz;
	 }
	 Baseline(vriLocation aant1, vriLocation aant2, double latitude) {
		  ant1 = aant1;
		  ant2 = aant2;
		  bx = (ant1.NS - ant2.NS ) * Math.sin(latitude);
		  by = ant1.EW - ant2.EW;
		  bz = (ant1.NS - ant2.NS ) * Math.cos(latitude);
	 }

	 UV project(double ha, double dec, double scale) {
		  double cd = Math.cos(dec);
		  double sd = Math.sin(dec);
		  double sh = Math.sin(ha);
		  double ch = Math.cos(ha);

		  double sbx = bx * scale;  		  
		  double sby = by * scale;
		  double sbz = -bz * scale;
		  // The minus sign allows for english-order in antennas[i].y value (as 
		  // read from screen), i.e. positive is south.

		  double u = sbx * sh + sby * ch;
		  double v = -sbx * sd * ch + sby * sd * sh + sbz * cd;
		  return new UV(u, v);
	 }

	 ArrayList<UV> makeUVTrack(double h1, double h2, 
										double dec, double scale) {
		  ArrayList<UV> res = new ArrayList<UV>();
		  
		  UV old_uv = project(h1, dec, scale);
		  res.add(old_uv);
		  for (double h = h1; h <= h2; h += 0.06) {
				UV uv = project(h, dec, scale);
				res.add(uv);
				old_uv = uv;
		  }
		  UV uv = project(h2, dec, scale);
		  res.add(uv);
		  return res;
	 }
		  
	 ArrayList<TrackPoint> makeTrack(double h1, double h2, 
												double dec, double scale) {
		  ArrayList<TrackPoint> res = new ArrayList<TrackPoint>();
		  
		  UV old_uv = project(h1, dec, scale);
		  res.add(new TrackPoint(h1, old_uv));
		  for (double h = h1; h <= h2; h += 0.06) {
				UV uv = project(h, dec, scale);
				res.add(new TrackPoint(h1, uv));
				old_uv = uv;
		  }
		  UV uv = project(h2, dec, scale);
		  res.add(new TrackPoint(h2, uv));
		  return res;
	 }
		  
}


class TrackPoint {
	 double ha;
	 UV uvPoint;
	 TrackPoint(double aha, UV auvPoint) {
		  ha = aha;
		  uvPoint = auvPoint;
	 }
}



class vriObservatory {
	 public String menu_name;      // Name in observatory pop-up menu
	 public String full_name;      // Proper name of obs.
	 public double latitude;       // Latitude (radians)
	 public double longitude;      // Longitude (radians) (not used)
	 public double ant_diameter;   // Diameter of antennas (metres)
	 public double ant_el_limit;   // Antenna lower elevation limit (degrees)
	 public vriConfig[] cfg;       // Set of "standard array configurations"
	 public vriLocation[] stations;      // Set of array stations
	 public vriLocation[] antennas;      // Set of antennas
	 public vriTrack[] trk;        // Set of defined road/rail tracks
	 public vriLocation ref;       // Reference point of observatory.
	 public int cfg_stations[][];  // Array of station numbers of configs
	 public String cfg_name[];     // Array of names of configs

	 static HashMap<String, vriObservatory> observatories;

	 Baseline[] getBaselines() {
		  int n = antennas.length;
		  Baseline[] baselines = new Baseline[n*(n-1)/2];

		  int count = 0;
		  for(int i = 0; i < (antennas.length-1); i++) {
				vriLocation ant1 = antennas[i];
				for(int j = i+1; j < (antennas.length); j++) {
					 vriLocation ant2 = antennas[j];
					 baselines[count] = new Baseline(ant1, ant2, latitude);
					 count++;
				}
		  }
		  return baselines;
	 }


	 static vriLocation[] makeAntennas(int num_antennas) {
		  vriLocation[] antennas = new vriLocation[num_antennas];
		  for (int i = 0; i < antennas.length; i++) 
				antennas[i] = new vriLocation();
		  return antennas;
	 }

	 static {
		  double[] cs, cu;
		  int[] csi;
		  int num_antennas, num_stations;
		  observatories = new HashMap<String, vriObservatory>();
	 
		  vriObservatory atca = new vriObservatory();
		  atca.menu_name = "ATCA";
		  atca.full_name = "Australia Telescope Compact Array";
		  atca.latitude = -0.529059644;
		  atca.ant_diameter = 22.0;
		  atca.ant_el_limit = 12.0 * Math.PI / 180.0;
		  atca.ref = new vriLocation();

		  // Stations
		  csi = new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16, 32, 45, 64, 84, 98, 100, 
								 102, 109, 110, 111, 112, 113, 128, 129, 140, 147, 148, 
								 163, 168, 172, 173, 182, 189, 190, 195, 196, 388, 392};
		  num_stations = csi.length;
		  atca.stations = new vriLocation[num_stations];
		  for (int i = 0; i < atca.stations.length; i++) {
				double space = (6000.0/392.0);
				int incr = csi[i];
				int ref = csi[34];
				// atca.stations[i] = new vriLocation(incr, ref, space); 
				atca.stations[i] = new vriLocation(0.0, (ref-incr)*space, 0.0);
				// cs[0]=Stations1, cs[34]=Stations35
		  }

		  // Track
		  atca.trk = new vriTrack[2];
		  atca.trk[0] = new vriTrack(atca.stations[0], atca.stations[34]);
		  atca.trk[1] = new vriTrack(atca.stations[35], atca.stations[36]);

		  // Configurations
		  atca.cfg_stations = new int[][] {
				{ 3, 11, 16, 30, 34, 37},     // 6A     
				{ 2, 12, 25, 31, 35, 37},     // 6B     
				{ 1,  6, 21, 24, 31, 37},     // 6C     
				{ 5, 10, 13, 28, 30, 37},     // 6D     
				{15, 18, 25, 28, 35, 37},     // 1.5A  
				{19, 21, 27, 31, 34, 37},     // 1.5B  
				{14, 22, 30, 33, 34, 37},     // 1.5C  
				{16, 17, 24, 31, 35, 37},     // 1.5D  
				{25, 27, 29, 33, 34, 37},     // 750A  
				{14, 17, 21, 24, 26, 37},     // 750B  
				{12, 13, 15, 18, 21, 37},     // 750C  
				{15, 16, 22, 24, 25, 37},     // 750D  
				{ 2,  6,  8,  9, 10, 37},     // 375    
				{14, 15, 16, 17, 20, 37},     // 210   
				{ 5,  6,  7,  8,  9, 37}      // 122B   
		  };

		  atca.cfg_name = new String[] {
				"6A", "6B", "6C", "6D",
				"1.5A", "1.5B", "1.5C", "1.5D",
				"750A", "750B", "750C", "750D",
				"375", "210", "122B"
		  };


		  num_antennas = atca.cfg_stations[0].length;
		  atca.antennas = makeAntennas(num_antennas);

		  observatories.put(atca.menu_name, atca);
		  // End ATCA

		  vriObservatory natca = new vriObservatory();
		  
		  natca.menu_name = "New ATCA";
		  natca.full_name = "Modified Australia Telescope Compact Array";
		  natca.latitude = -0.529059644;
		  natca.ant_diameter = 22.0;
		  natca.ant_el_limit = 12.0 * Math.PI / 180.0;
		  natca.ref = new vriLocation();

		  // Stations
		  cs = new double[] {0.0,   2.0,   4.0,   6.0,   8.0,  // CS01-05
									10.0,  12.0,  14.0,  16.0,  32.0,  // CS06-10
									45.0,  64.0,  84.0,  98.0, 100.0,  // CS11-15
									102.0, 109.0, 110.0, 111.0, 112.0,  // CS16-20
									113.0, 128.0, 129.0, 140.0, 147.0,  // CS21-25
									148.0, 163.0, 168.0, 172.0, 173.0,  // CS26-30
									182.0, 189.0, 190.0, 195.0, 196.0, 388.0, 392.0,  // -37
									104.0, 106.0, 124.0, 125.0,         // CS38-41
									106.0, 106.0, 106.0, 106.0,         // CS42-45
									106.0, 106.0, 106.0, 106.0};        // CS46-49

		  cu = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
									0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
									0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
									0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
									0.0, 0.0, 0.0, 0.0, 2.0, 5.0, 7.0, 11.0,
									14.0, 19.0, 23.0, 24.0 };

		  num_stations = cs.length;
		  natca.stations = new vriLocation[num_stations];
		  for (int i = 0; i < natca.stations.length; i++) {
				// 4 doubles form of constructor
				double space = (6000.0/392.0);
				double ref = cs[15];
				double EWincr = cs[i];
				double NSincr = cu[i];
				// natca.stations[i] = new vriLocation(EWincr, NSincr, ref, space); 
				natca.stations[i] = new vriLocation(NSincr*space, (ref-EWincr)*space, 0.0);
		  }

		  // Track
		  natca.trk = new vriTrack[3];
		  natca.trk[0] = new vriTrack(natca.stations[0], natca.stations[34]);
		  natca.trk[1] = new vriTrack(natca.stations[35], natca.stations[36]);
		  natca.trk[2] = new vriTrack(natca.stations[38], natca.stations[48]);

		  // Configurations
		  natca.cfg_stations = new int[][] {
				{14, 16, 38, 17, 20, 37},     // EWuc    New East-West configs
				{16, 38, 17, 20, 41, 37},     // EWvc1
				{38, 18, 21, 40, 22, 37},     // EWvc2

				{39, 42, 43, 44, 45, 37},     // NSuc1   New North-South configs
				{39, 42, 43, 45, 46, 37},     // NSuc2
				{39, 43, 44, 47, 48, 37},     // NSvc1
				{42, 43, 45, 47, 49, 37},     // NSvc2

				{38, 39, 18, 42, 43, 37},     // H75     New Hybrid configs
				{15, 38, 19, 44, 45, 37},     // H168
				{15, 38, 19, 44, 46, 37},     // H214
				{15, 17, 40, 46, 48, 37},     // H368

				{ 5,  6,  7,  8,  9, 37},     // 122B    Existing configs
				{14, 15, 16, 17, 20, 37},     // 210   
				{ 2,  6,  8,  9, 10, 37},     // 375    
				{25, 27, 29, 33, 34, 37},     // 750A  
				{14, 17, 21, 24, 26, 37},     // 750B  
				{12, 13, 15, 18, 21, 37},     // 750C  
				{15, 16, 22, 24, 25, 37},     // 750D  
				{15, 18, 25, 28, 35, 37},     // 1.5A  
				{19, 21, 27, 31, 34, 37},     // 1.5B  
				{14, 22, 30, 33, 34, 37},     // 1.5C  
				{16, 17, 24, 31, 35, 37},     // 1.5D  
				{ 3, 11, 16, 30, 34, 37},     // 6A     
				{ 2, 12, 25, 31, 35, 37},     // 6B     
				{ 1,  6, 21, 24, 31, 37},     // 6C     
				{ 5, 10, 13, 28, 30, 37}      // 6D     
		  };

		  natca.cfg_name = new String [] {
				"EWuc", "EWvc1", "EWvc2",
				"NSuc1", "NSuc2", "NSvc1", "NSvc2",
				"H75", "H168", "H214", "H368",
				"122B", "210", "375",
				"750A", "750B", "750C", "750D",
				"1.5A", "1.5B", "1.5C", "1.5D",
				"6A", "6B", "6C", "6D"
		  };

		  num_antennas = natca.cfg_stations[0].length;
		  natca.antennas = makeAntennas(num_antennas);

		  observatories.put(natca.menu_name, natca);
		  // End New ATCA
		  
		  vriObservatory wsrt = new vriObservatory();
		  wsrt.menu_name = "WSRT";
		  wsrt.full_name = "Westerbork Synthesis Radio Telescope";
		  wsrt.latitude = +0.923574484;
		  wsrt.ant_diameter = 25.0;
		  wsrt.ant_el_limit = 0.0 * Math.PI / 180.0;
		  wsrt.ref = new vriLocation();

		  // Stations
		  csi = new int[] {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 19, 20, 37, 38};
		  num_stations = csi.length;
		  wsrt.stations = new vriLocation[num_stations];
		  for(int i = 0; i < wsrt.stations.length; i++) {
				int incr = csi[i];
				int ref = csi[10];
				double space = -72.0;
				// wsrt.stations[i] = new vriLocation(incr, ref, space);   
				wsrt.stations[i] = new vriLocation(0.0, (double)(ref-incr)*space, 0.0);
		  }

		  // Track
		  wsrt.trk = new vriTrack[2];
		  wsrt.trk[0] = new vriTrack(wsrt.stations[10], wsrt.stations[11]);
		  wsrt.trk[1] = new vriTrack(wsrt.stations[12], wsrt.stations[13]);

		  // Configurations
		  wsrt.cfg_stations = new int[][] {
				{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}    // Default
		  };

		  wsrt.cfg_name = new String[] {
				"default"
		  };

		  num_antennas = wsrt.cfg_stations[0].length;
		  wsrt.antennas = makeAntennas(num_antennas); 
		  observatories.put(wsrt.menu_name, wsrt);
		  // End WSRT

		  vriObservatory merlin = new vriObservatory();
		  merlin.menu_name = "MERLIN";
		  merlin.full_name = "Multi Element Radio Linked Interferometer Network";
		  merlin.latitude = +0.929160933;
		  merlin.ant_diameter = 25.0;
		  merlin.ant_el_limit = 0.0 * Math.PI / 180.0;
		  merlin.ref = new vriLocation();


		  // Stations
		  num_stations = 6;
		  merlin.stations = new vriLocation[num_stations];

		  merlin.stations[0] = vriLocation.fromKM(   0.0,        0.0,        0.0 ); // Jb
		  merlin.stations[1] = vriLocation.fromKM( -98.663272, 180.934874,  13.0 ); // Cm
		  merlin.stations[2] = vriLocation.fromKM(  11.670062, -24.278806,  88.0 ); // Da
		  merlin.stations[3] = vriLocation.fromKM( -28.996605, -67.835530, 109.0 ); // Kn
		  merlin.stations[4] = vriLocation.fromKM(  26.381173, -14.973939,  77.0 ); // Ta
		  merlin.stations[5] = vriLocation.fromKM(-105.641049,  12.157448,  67.0 ); // De

		  for (int i = 0; i < num_stations; i++) 
				merlin.stations[i].EW -= 60000.0;  // Centre the array

		  // Track
		  merlin.trk = new vriTrack[5];
		  merlin.trk[0] = new vriTrack(merlin.stations[0], merlin.stations[2]);
		  merlin.trk[1] = new vriTrack(merlin.stations[0], merlin.stations[3]);
		  merlin.trk[2] = new vriTrack(merlin.stations[0], merlin.stations[4]);
		  merlin.trk[3] = new vriTrack(merlin.stations[0], merlin.stations[5]);
		  merlin.trk[4] = new vriTrack(merlin.stations[5], merlin.stations[1]);

		  // Configurations
		  merlin.cfg_stations = new int[][] {
				{1, 2, 3, 4, 5, 6}    // Default
		  };

		  merlin.cfg_name = new String[] {
				"default"
		  };
		  num_antennas = merlin.cfg_stations[0].length;
		  merlin.antennas = makeAntennas(num_antennas);

		  observatories.put(merlin.menu_name, merlin);
		  // End MERLIN
	 }

	 public vriObservatory() {
		  //  Check for *.observatory files.
		  //  If found, put an entry into the observatory pop-up menu
	 }

	 public static vriObservatory select(String selection) {
		  // When a selection is made from the observatory pop-up menu,
		  // this method is called to load the values (from file or otherwise)
		  
		  System.out.println(String.format("Selected %s", selection));
		  vriObservatory obs = observatories.get(selection);
		  return obs;
	 }

	 public void selectNumAnt(int n) {
		  int num_antennas = n;
		  vriLocation a[] = new vriLocation[num_antennas];
		  for (int i = 0; i < a.length; i++) {
				a[i] = new vriLocation();
				if (i < antennas.length) {
					 a[i].EW = antennas[i].EW;
					 a[i].NS = antennas[i].NS;
					 a[i].UD = antennas[i].UD;
				}
		  }
		  antennas = a;
	 }

	 public String defaultConfig() {
		  for(int j = 0; j < antennas.length; j++) {
				if(j < cfg_stations[0].length) {
					 vriLocation station = stations[cfg_stations[0][j]-1];
					 antennas[j].EW = station.EW;
					 antennas[j].NS = station.NS;
					 antennas[j].UD = station.UD;
				}
		  }
		  return cfg_name[0];
	 }

	 public boolean setConfig(String cfg_str) {
		  for(int i = 0; i < cfg_stations.length; i++) {
				if (cfg_name[i].equals(cfg_str)) {
					 for (int j = 0; j < antennas.length; j++) {
						  if (j < cfg_stations[i].length) {
								vriLocation station = stations[cfg_stations[i][j]-1];
								antennas[j].EW = station.EW;
								antennas[j].NS = station.NS;
								antennas[j].UD = station.UD;
						  }
					 }
					 return true;
				} 
		  }
		  return false;
	 }

	 public void report() {

		  System.out.println ("\nObservatory report... "+full_name);
		  for(int i = 0; i < antennas.length; i++) {
				System.out.println ("Antenna "+i+
										  " NS = "+antennas[i].NS+" EW = "+antennas[i].EW+" UD = "+antennas[i].UD );
		  }
	 }

}
