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


class vriObservatory {
	 public String menu_name;      // Name in observatory pop-up menu
	 public String full_name;      // Proper name of obs.
	 public double latitude;       // Latitude (radians)
	 public double longitude;      // Longitude (radians) (not used)
	 public double ant_diameter;   // Diameter of antennas (metres)
	 public double ant_el_limit;   // Antenna lower elevation limit (degrees)
	 public vriConfig[] cfg;       // Set of "standard array configurations"
	 public vriStation[] stn;      // Set of array stations
	 public vriAntenna[] ant;      // Set of antennas
	 public vriTrack[] trk;        // Set of defined road/rail tracks
	 public vriLocation ref;       // Reference point of observatory.
	 public int cfg_stations[][];  // Array of station numbers of configs
	 public String cfg_name[];     // Array of names of configs

	 static HashMap<String, vriObservatory> observatories;

	 static vriAntenna[] makeAntennas(int num_antennas) {
		  antennas = new vriAntenna[num_antennas];
		  for (int i = 0; i < atca.ant.length; i++) 
				antennas.ant[i] = new vriAntenna();
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
		  atca.stn = new vriStation[num_stations];
		  for (int i = 0; i < atca.stn.length; i++) {
				double space = (6000.0/392.0);
				int incr = csi[i];
				int ref = csi[34];
				// atca.stn[i] = new vriStation(incr, ref, space); 
				atca.stn[i] = new vriStation(0.0, (ref-incr])*space, 0.0)
				// cs[0]=Stn1, cs[34]=Stn35
		  }

		  // Track
		  atca.trk = new vriTrack[2];
		  atca.trk[0] = new vriTrack(atca.stn[0], atca.stn[34]);
		  atca.trk[1] = new vriTrack(atca.stn[35], atca.stn[36]);

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


		  // Antennas
		  num_antennas = atca.cfg_stations[0].length();
		  atca.ant = makeAntennas(num_antennas);

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
		  natca.stn = new vriStation[num_stations];
		  for (int i = 0; i < natca.stn.length; i++) {
				// 4 doubles form of constructor
				double space = (6000.0/392.0);
				double ref = cs[15];
				double EWincr = cs[i];
				double NSincr = cu[i];
				// natca.stn[i] = new vriStation(EWincr, NSincr, ref, space); 
				natca.stn[i] = new vriStation(NSincr*space, (ref-EWincr)*space);
		  }

		  // Track
		  natca.trk = new vriTrack[3];
		  natca.trk[0] = new vriTrack(natca.stn[0], natca.stn[34]);
		  natca.trk[1] = new vriTrack(natca.stn[35], natca.stn[36]);
		  natca.trk[2] = new vriTrack(natca.stn[38], natca.stn[48]);

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

		  // Antennas
		  num_antennas = natca.cfg_stations[0].length();
		  natca.ant = makeAntennas(num_antennas);

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
		  wsrt.stn = new vriStation[num_stations];
		  for(int i = 0; i < wsrt.stn.length; i++) {
				wsrt.stn[i] = new vriStation(csi[i], csi[10], -72.0);   
		  }

		  // Track
		  wsrt.trk = new vriTrack[2];
		  wsrt.trk[0] = new vriTrack(wsrt.stn[10], wsrt.stn[11]);
		  wsrt.trk[1] = new vriTrack(wsrt.stn[12], wsrt.stn[13]);

		  // Configurations
		  wsrt.cfg_stations = new int[][] {
				{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}    // Default
		  };

		  wsrt.cfg_name = new String[] {
				"default"
		  };

		  // Antennas
		  num_antennas = wsrt.cfg_stations[0].length();
		  wsrt.ant = makeAntennas(num_antennas); 


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
		  merlin.stn = new vriStation[num_stations];

		  merlin.stn[0] = new vriStation(   0.0,        0.0,        0.0 ); // Jb
		  merlin.stn[1] = new vriStation( -98.663272, 180.934874,  13.0 ); // Cm
		  merlin.stn[2] = new vriStation(  11.670062, -24.278806,  88.0 ); // Da
		  merlin.stn[3] = new vriStation( -28.996605, -67.835530, 109.0 ); // Kn
		  merlin.stn[4] = new vriStation(  26.381173, -14.973939,  77.0 ); // Ta
		  merlin.stn[5] = new vriStation(-105.641049,  12.157448,  67.0 ); // De

		  for (int i = 0; i < num_stations; i++) 
				merlin.stn[i].EW -= 60000.0;  // Centre the array

		  // Track
		  merlin.trk = new vriTrack[5];
		  merlin.trk[0] = new vriTrack(merlin.stn[0], merlin.stn[2]);
		  merlin.trk[1] = new vriTrack(merlin.stn[0], merlin.stn[3]);
		  merlin.trk[2] = new vriTrack(merlin.stn[0], merlin.stn[4]);
		  merlin.trk[3] = new vriTrack(merlin.stn[0], merlin.stn[5]);
		  merlin.trk[4] = new vriTrack(merlin.stn[5], merlin.stn[1]);

		  // Configurations
		  merlin.cfg_stations = new int[][] {
				{1, 2, 3, 4, 5, 6}    // Default
		  };

		  merlin.cfg_name = new String[] {
				"default"
		  };

		  // Antennas
		  num_antennas = merlin.cfg_stations[0].length();
		  merlin.ant = makeAntennas(num_antennas);

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

		  if (selection.compareTo("ATCA") == 0) {
				System.out.println("Selected ATCA");
				vriObservatory obs = observatories.get("ATCA");
				return obs;
				
		  } else if (selection.compareTo("New ATCA") == 0) {
				System.out.println("Selected modified ATCA");
				vriObservatory obs = observatories.get("New ATCA");
				return obs;
		  } else if (selection.compareTo("WSRT") == 0) {
				System.out.println("Selected WSRT");
				vriObservatory obs = observatories.get("WSRT");
				return obs;

		  } else { // if (selection.compareTo("MERLIN") == 0) {
				System.out.println("Selected MERLIN");
				vriObservatory obs = observatories.get("MERLIN");
				return obs;
		  } 
	 }

	 public void selectNumAnt(int n) {
		  num_antennas = n;
		  vriAntenna a[] = new vriAntenna[num_antennas];
		  for (int i = 0; i < a.length; i++) {
				a[i] = new vriAntenna();
				if (i < ant.length) {
					 a[i].EW = ant[i].EW;
					 a[i].NS = ant[i].NS;
					 a[i].UD = ant[i].UD;
				}
		  }
		  ant = a;
	 }

	 public String defaultConfig() {
		  for(int j = 0; j < ant.length; j++) {
				if(j < cfg_stations[0].length) {
					 station = stn[cfg_stations[0][j]-1];
					 ant[j].EW = station.EW;
					 ant[j].NS = station.NS;
					 ant[j].UD = station.UD;
				}
		  }
		  return cfg_name[0];
	 }

	 public boolean setConfig(String cfg_str) {
		  for(int i = 0; i < cfg_stations.length; i++) {
				if (cfg_name[i].equals(cfg_str)) {
					 for (int j = 0; j < ant.length; j++) {
						  if (j < cfg_stations[i].length) {
								station = stn[cfg_stations[i][j]-1];
								ant[j].EW = station.EW;
								ant[j].NS = station.NS;
								ant[j].UD = station.UD;
						  }
					 }
					 return true;
				} 
		  }
		  return false;
	 }

	 public void report() {

		  System.out.println ("\nObservatory report... "+full_name);
		  for(int i = 0; i < ant.length; i++) {
				System.out.println ("Antenna "+i+
										  " NS = "+ant[i].NS+" EW = "+ant[i].EW+" UD = "+ant[i].UD );
		  }
	 }

}
