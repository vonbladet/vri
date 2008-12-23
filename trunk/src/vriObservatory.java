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
import nl.jive.earth.*;
import java.beans.*;

abstract class vriObservatory 
{
	 String menu_name;      // Name in observatory pop-up menu
	 String full_name;      // Proper name of obs.
	 double lengthScale;    // of observatory, in m
	 double latitude;
	 double longitude;
	 double ant_diameter;
	 double ant_el_limit;
	 String[] cfg_name;

	 ArrayList<Component> components;

	 abstract Baseline[] getBaselines();
	 double getLengthScale() {
		  return lengthScale;
	 }

	 abstract String defaultConfig();
	 abstract boolean setConfig(String cfg_str);  // BOOlean?!
	 abstract void selectNumAnt(int i);
	 abstract void stationLock();
	 abstract vriArrDisp getArrDisp(vriArrEdit ae);
	 abstract vriUVcDisp getUVcDisp(vriAuxiliary aux);
}

class vriSmallObservatory extends vriObservatory {
	 private static PropertyChangeSupport propChanges;

	 vriLocation[] stations; 
	 vriTrack[] trk; 
	 vriLocation ref;
	 vriConfig[] cfg;       // Set of "standard array configurations"
	 int[][] cfg_stations;  // Array of station numbers of configs
	 vriLocation[] antennas;      
		  
	 vriSmallObservatory() {
	 }

	 vriArrDisp getArrDisp(vriArrEdit ae) {
		  return new vriNSEWArrDisp(this, ae);
	 }

	 vriUVcDisp getUVcDisp(vriAuxiliary a) {
		  return new vriNSEWUVcDisp(this, a);
	 }

	 Baseline[] getBaselines() {
		  int n = antennas.length;
		  Baseline[] baselines = new NSEWBaseline[n*(n-1)/2];

		  int count = 0;
		  for(int i = 0; i < (antennas.length-1); i++) {
				vriLocation ant1 = antennas[i];
				for(int j = i+1; j < (antennas.length); j++) {
					 vriLocation ant2 = antennas[j];
					 baselines[count] = new NSEWBaseline(ant1, ant2, latitude);
					 count++;
				}
		  }
		  return baselines;
	 }
	 
	 void makeAntennas(int num_antennas) {
		  antennas = new vriLocation[num_antennas];
		  for (int i = 0; i < antennas.length; i++) 
				antennas[i] = new vriLocation();
	 }

	 void selectNumAnt(int n) {
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

	 String defaultConfig() {
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

	 boolean setConfig(String cfg_str) {
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

	 void stationLock() {
		  int s = 0;
		  double dist;

		  for (int i = 0; i < antennas.length; i++) {
				vriLocation a = antennas[i];
				double bestdist = Double.MAX_VALUE;
				for (int j = 0; j < stations.length; j++) {
					 vriLocation station = stations[j];
					 dist = (Math.pow((a.NS - station.NS), 2.0) + 
								Math.pow((a.EW - station.EW), 2.0));
					 if (dist < bestdist) {
						  s = j;
						  bestdist = dist;
					 }
				}
				antennas[i].NS = stations[s].NS;
				antennas[i].EW = stations[s].EW;
		  }
	 }


	 void report() {

		  System.out.println ("\nObservatory report... "+full_name);
		  for(int i = 0; i < antennas.length; i++) {
				System.out.println ("Antenna "+i+
										  " NS = "+antennas[i].NS+" EW = "+antennas[i].EW+" UD = "+antennas[i].UD );
		  }
	 }

}

class vriBigObservatory extends vriObservatory {
	 
	 TelescopeList antennas;

	 vriBigObservatory() {

	 }

	 vriArrDisp getArrDisp(vriArrEdit ae) {
		  return new vriLatLonArrDisp(this, ae);
	 }

	 vriUVcDisp getUVcDisp(vriAuxiliary aux) {
		  return new vriLatLonUVcDisp(this, aux);
	 }


	 Baseline[] getBaselines() {
		  int n = antennas.size();
		  Baseline[] baselines = new LatLonBaseline[n*(n-1)/2];

		  int count = 0;
		  for(int i = 0; i < (antennas.size()-1); i++) {
				LatLon ant1 = antennas.get(i).position;
				for(int j = i+1; j < (antennas.size()); j++) {
					 LatLon ant2 = antennas.get(j).position;
					 baselines[count] = new LatLonBaseline(ant1, ant2);
					 count++;
				}
		  }
		  return baselines;
	 }
	 
	 String defaultConfig() {
		  return "Default";
	 }

	 boolean setConfig(String cfg_str) {
		  return true;
	 }

	 void selectNumAnt(int i) {
	 }

	 void stationLock() {

	 }
}


class vriObservatoryManager {
	 NationReader nr;
	 HashMap<String, vriObservatory> observatories;
	 
	 vriObservatoryManager()
	 {
		  try {
				nr = new NationReader("gis.json");
		  } catch (org.json.JSONException e) {
				;
		  } catch (java.io.FileNotFoundException e) {
				;
		  } catch (java.io.IOException e) {
				;
		  }
		  if (nr == null) {
				System.err.println("Geometries not loaded");
		  } else {
				System.err.println("Geometries loaded");
		  }
		  double[] cs, cu;
		  int[] csi;
		  int num_antennas, num_stations;
		  observatories = new HashMap<String, vriObservatory>();
	 
		  vriSmallObservatory atca = new vriSmallObservatory();
		  atca.menu_name = "ATCA";
		  atca.full_name = "Australia Telescope Compact Array";
		  atca.latitude = -0.529059644;
		  atca.longitude = Math.toRadians(149.581328);

		  atca.ant_diameter = 22.0;
		  atca.ant_el_limit = 12.0 * Math.PI / 180.0;
		  atca.ref = new vriLocation();
		  atca.lengthScale = 20e3;
		  try {
				atca.components = nr.getNation("Australia").getComponents();
		  } catch (NameNotFoundException e) {
				System.err.println("Australia not found");
				atca.components = null;
		  }

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
		  atca.makeAntennas(num_antennas);

		  observatories.put(atca.menu_name, atca);
		  // End ATCA

		  vriSmallObservatory natca = new vriSmallObservatory();
		  
		  natca.menu_name = "New ATCA";
		  natca.full_name = "Modified Australia Telescope Compact Array";
		  natca.latitude = -0.529059644;
		  natca.longitude = Math.toRadians(149.581328);
		  natca.lengthScale = 20e3;
		  natca.ant_diameter = 22.0;
		  natca.ant_el_limit = 12.0 * Math.PI / 180.0;
		  natca.ref = new vriLocation();

		  try {
				natca.components = nr.getNation("Australia").getComponents();
		  } catch (NameNotFoundException e) {
				natca.components = null;
		  }

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
		  natca.makeAntennas(num_antennas);

		  observatories.put(natca.menu_name, natca);
		  // End New ATCA
		  
		  vriSmallObservatory wsrt = new vriSmallObservatory();
		  wsrt.menu_name = "WSRT";
		  wsrt.full_name = "Westerbork Synthesis Radio Telescope";
		  wsrt.latitude = +0.923574484;
		  wsrt.lengthScale = 5e3;
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
		  wsrt.makeAntennas(num_antennas); 
		  observatories.put(wsrt.menu_name, wsrt);
		  // End WSRT

		  vriSmallObservatory merlin = new vriSmallObservatory();
		  merlin.menu_name = "MERLIN";
		  merlin.full_name = "Multi Element Radio Linked Interferometer Network";
		  
		  merlin.latitude = +0.929160933;
		  merlin.lengthScale = 500.0e3;
		  //merlin.longitude = 0;
		  //merlin.latitude = Math.toRadians(53.0 + 30.0/60);
		  merlin.longitude = Math.toRadians(-1.0);
		  merlin.ant_diameter = 25.0;
		  merlin.ant_el_limit = 0.0 * Math.PI / 180.0;
		  merlin.ref = new vriLocation();
// 		  try {
// 				merlin.components = nr.getNation("United Kingdom").getComponents();
// 		  } catch (NameNotFoundException e) {
// 				System.err.println("UK not found");
// 				merlin.components = null;
// 		  }
		  Set<String> nations = nr.getNationNamesInRegion("Europe");
		  System.err.println("# nations: "+nations.size());
		  nations.remove("Russia");
		  merlin.components = nr.getSelectedNations(nations);
		  System.err.println("# nations: "+nations.size());
		  
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
		  merlin.makeAntennas(num_antennas);

		  observatories.put(merlin.menu_name, merlin);
		  // End MERLIN

		  vriBigObservatory evn = new vriBigObservatory();
		  TelescopeReader tr = new TelescopeReader("evn.json");
		  TelescopeList tl = tr.getTelescopes();
		  
		  evn.antennas = tl;
		  evn.menu_name = "EVN";
		  evn.full_name = "EVN";
		  Set<String> evn_nations = nr.getNationNamesInRegion("Europe");
		  evn_nations.remove("Russia");
		  evn.components = nr.getSelectedNations(evn_nations);
		  evn.lengthScale = 5e6;
		  // FIXME
		  evn.latitude = Math.toRadians(50);
		  evn.longitude = Math.toRadians(10);
		  String[] cfg_name = new String[]{"default"};
		  
		  observatories.put(evn.menu_name, evn);
		  
		  // Kn and Da are mostly used in combination with the MkII at Jodrell
		  // shanghai, atca, mopra, hobart, kashima, tigo, westford, 
		  // arecibo and metsahovi

		  // EVN: ira.cnr.it (Bologna=Medicina)
		  // Effelsburg, WSRT, Torun
		  // chalmers.se = Onsala
		  // bkg.bund.de = Wettzell(!)
		  // Metsähovi
		  // Also, Jodrell and Cambridge, with Kn as a possible extra if we feel Merlincasty
	 }
	 
	 vriObservatory select(String selection) {
		  // When a selection is made from the observatory pop-up menu,
		  // this method is called to load the values (from file or otherwise)
		  
		  System.out.println(String.format("Selected %s", selection));
		  vriObservatory obs = observatories.get(selection);
		  return obs;
	 }
	 
}
