package nl.jive.vri;

import java.util.*;
import java.io.*;
import org.json.*;
import nl.jive.earth.*;

public class TelescopeReader extends MyReader
{
	 LatLonTelescopeList telescopes;

	 public TelescopeReader(String telFilename) 
	 {
		  telescopes = new LatLonTelescopeList();
 		  try { 
				JSONArray ta = new JSONArray(Util.readResourceFile(telFilename).toString());
				for (int i=0; i<ta.length(); i++) {
					 JSONObject jo = ta.getJSONObject(i);
					 String code = jo.getString("code");
					 String name = jo.getString("name");
					 JSONArray p = jo.getJSONArray("position");
					 double lon = p.getDouble(0);
					 double lat = p.getDouble(1);
					 telescopes.add(new Telescope(code, name, lon, lat));
				}
		  } catch (org.json.JSONException e) {
				System.err.println("Misformed json in "+telFilename);
		  } catch (java.io.FileNotFoundException e) {
				System.err.println(telFilename + " not found");
		  } catch (java.io.IOException e) {
				System.err.println("Error reading "+telFilename);
		  }
	 }
	 public LatLonTelescopeList getTelescopes() {
		  return telescopes;
	 }
}
