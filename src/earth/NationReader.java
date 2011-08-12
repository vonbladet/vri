package nl.jive.earth;

import java.util.*;
import java.io.*;
import org.json.*;

public class NationReader extends MyReader
{
	 NationList nations;

	 public NationReader(String gisFilename) 
 		  throws org.json.JSONException, java.io.FileNotFoundException, java.io.IOException
	 {
		  JSONArray ja = new JSONArray(readResourceFile(gisFilename).toString());
		  nations = new NationList();

		  for (int i=0; i<ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				String name = jo.getString("name");
				String region = jo.getString("region");
				ArrayList<Component> comps = new ArrayList<Component>();
				JSONArray allComps = jo.getJSONArray("components");
				for (int j=0; j < allComps.length(); j++) {
					 JSONArray comp = allComps.getJSONArray(j);
					 LinkedList<LatLon> plist = new LinkedList<LatLon>();
					 for (int k=0; k < comp.length(); k++) {
						  JSONArray jpoint = comp.getJSONArray(k);
						  LatLon p = new LatLon(jpoint.getDouble(0), 
														jpoint.getDouble(1));
						  plist.addLast(p);
					 }
					 comps.add(new Component(plist));
				}
				Nation n = new Nation(name, region, comps);
				nations.add(n);
		  }
		  // I should really make telescope reader, but i haven't yet
		  // 		  telescopes = new TelescopeList();
		  // 		  JSONArray ta = new JSONArray(readResourceFile(telFilename).toString());
		  // 		  for (int i=0; i<ta.length(); i++) {
		  // 				JSONObject jo = ta.getJSONObject(i);
		  // 				String code = jo.getString("code");
		  // 				String name = jo.getString("name");
		  // 				JSONArray p = jo.getJSONArray("position");
		  // 				double lon = p.getDouble(0);
		  // 				double lat = p.getDouble(1);
		  // 				telescopes.add(new Telescope(code, name, lon, lat));
		  // 		  }
	 }
	 public NationList getNations() {
		  return nations;
	 }

	 public ArrayList<Component> getSelectedNations(Set<String> names) {
		  ArrayList<Component> res = new ArrayList<Component>();
		  for (Nation n: nations) {
				if (names.contains(n.name)) {
					 res.addAll(n.getComponents());
				}
		  }
		  return res;
	 }

	 public Nation getNation(String name) 
	 throws NameNotFoundException {
		  for (Nation n: nations) {
				if (n.name.equals(name)) {
					 return n;
				} 
		  }
		  throw(new NameNotFoundException(String.format("Nation %s not found", name)));
	 }

	 public Set<String> getNationNamesInRegion(String name) {
		  Set<String> res = new HashSet<String>();
		  for (Nation n: nations) {
				if (name.equals(n.region)) {
					 res.add(n.name);
				}
		  }
		  return res;
	 }

	 public ArrayList<Component> getRegion(String name) 
		  throws NameNotFoundException {
		  ArrayList<Component> res = new ArrayList<Component>();
		  for (Nation n: nations) {
				if (n.region == null) {
					 System.err.println(n.name + " has null region");
					 continue;
				}
				if (n.region.equals(name)) {
					 res.addAll(n.getComponents());
				}
		  }
		  if (res.size() == 0) {
				throw(new NameNotFoundException(String.format("Region %s not found", name)));
		  }
		  return res;
	 }
}
