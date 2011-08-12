package nl.jive.earth;
import java.util.*;
import java.io.*;
import org.json.*;

public class Util
{
	 public static StringBuffer readResourceFile(String filename) 
		  throws  java.io.FileNotFoundException,
					 java.io.UnsupportedEncodingException,
					 java.io.IOException
	 {
		  
		  InputStreamReader in = 
				new InputStreamReader(Util.class.getResourceAsStream(filename), "UTF-8");
		  StringBuffer s = new StringBuffer();
		  int CHUNKSIZE = 4096;
		  char buff[] = new char[CHUNKSIZE];
		  while (true) {
				int read = in.read(buff);
				s.append(buff);
				if (read < CHUNKSIZE) break;
		  }
		  return s;
	 }
}

