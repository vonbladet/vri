package nl.jive.earth;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.lang.Math;
import org.json.*;


public class Nation {
	 public String name;
	 public String region;
	 ArrayList<Component> components;
	 

	 public Nation(String aname, String aregion, ArrayList<Component> acomponents) {
		  name = aname;
		  region = aregion;
		  components = acomponents;
	 }

	 public ArrayList<Component> getComponents() {
		  return components;
	 }
}
