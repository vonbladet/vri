/*
 * vriUVcDisp.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

import java.awt.*;
import java.beans.*;

class vriUVcDisp extends vriDisplay {
	 //  double ha;
	 //  double dec;           // Declination of source
	 public static double grid_scale = 100;
	 public static double track_scale = 0.40;
	 public static boolean first_time = true;
	 vriObservatory obs;
	 vriAuxiliary aux;
	 vriUVtrack accum;
	 Color colour;

	 public vriUVcDisp(vriObservatory o, vriAuxiliary a) {

		  super();
		  obs = o;
		  aux = a;
		  accum = new vriUVtrack();
		  displayScale = 10000.0;   // Expressed in kilowavelengths
		  defaultScale = 10000.0;   // Expressed in kilowavelengths
		  colour = Color.blue;
	 }

	 public void setObservatory(vriObservatory aobs) {
		  obs = aobs;
	 }

	 public void addTracks() {
		  // Adds the "current" UV tracks to the accumulator
		  double bx;
		  double by;
		  double bz;                // X, Y and Z coords of baseline

		  for(int i = 0; i < (obs.ant.length-1); i++) {
				for(int j = i+1; j < (obs.ant.length); j++) {
					 by = obs.ant[i].EW - obs.ant[j].EW;
					 bz = (obs.ant[i].NS - obs.ant[j].NS) * Math.cos(obs.latitude);
					 bx = (obs.ant[i].NS - obs.ant[j].NS) * Math.sin(obs.latitude);

					 accum.add(bx, by, bz, aux.ha1, aux.ha2);
				}  // 2nd ant
		  }  // 1st ant
		  repaint();
	 }

	 public void clearTracks() {
		  // Clears the accumulator of any UV tracks
		  accum.clear();
		  repaint();
	 }

	 public void setColour(Color c) {
		  colour = c;
	 }

	 public void paint(Graphics g) {
		  Rectangle r = bounds();
		  int tx;
		  int ty;
		  int tw;
		  int th;            // Used for the shadow plot
		  double bx;
		  double by;
		  double bz;                // X, Y and Z coords of baseline

		  // Plot the focus border
		  plotFocus(g);

		  // Do the grid
		  g.setColor(Color.black);
		  g.fillRect(1, 1, r.width-2, r.height-2);
		  g.setColor(Color.darkGray);
		  g.drawRect(0, 0, r.width-1, r.height-1);
		  g.translate(displayCentre.x, displayCentre.y);
		  grid_scale = displaySize / displayScale * 10.0;
		  int mx = (int)(25.0 * grid_scale);
		  // Calculate the metres-to-wavelengths-to-pixels conversion scale factor
		  double s = aux.freq / 299.792458 / 1000.0 * displaySize / displayScale;

		  // Do the shadow zone (use the tx/ty/tw/th parameters temporarily)
		  tx = ty = (int) (-s * obs.ant_diameter);
		  tw = th = (int) (s * 2.0 * obs.ant_diameter);
		  g.fillOval(tx, ty, tw, th);

		  // Plot the tracks

		  double cd = Math.cos(aux.dec);
		  double sd = Math.sin(aux.dec);

		  if(!(colour.equals(Color.black))) {
				g.setColor(colour);

				// Plot accumulated baselines
				for(int i = 0; i < accum.num_tracks; i++) {
					 plotBaseline(accum.bx[i], accum.by[i], accum.bz[i], 
									  accum.h1[i], accum.h2[i], sd, cd, s, g);
				}
		  }

		  g.setColor(Color.red);
		  System.err.println(String.format("Hour angles: %.2f, %.2f", aux.ha1, aux.ha2));
		  // Plot baselines from the current array
		  for(int i = 0; i < (obs.ant.length-1); i++) {
				for(int j = i+1; j < (obs.ant.length); j++) {

					 by = obs.ant[i].EW - obs.ant[j].EW;
					 bz = ( obs.ant[i].NS - obs.ant[j].NS ) * Math.cos(obs.latitude);
					 bx = ( obs.ant[i].NS - obs.ant[j].NS ) * Math.sin(obs.latitude);

					 plotBaseline(bx, by, bz, aux.ha1, aux.ha2, sd, cd, s, g);

				}  // 2nd ant
		  }  // 1st ant
		  // Draw a scale
		  g.translate(-displayCentre.x, -displayCentre.y);
		  g.setColor(Color.white);
		  double l = displayScale * (displaySize - 20.0) / displaySize;
		  l = Math.log(l)/Math.log(10.0);
		  l = Math.pow(10.0, Math.floor(l));
		  int m = (int) Math.round(l * displaySize / displayScale);
		  g.drawLine(10, r.height-10, 10+m, r.height-10);
		  String str = new String();
		  if(l <= 1.0) {
				str = Double.toString(l*1000.0) + "lambda";
		  } else {
				str = Double.toString(l) + "klambda";
		  }
		  g.drawString(str, 10,r.height-12);
		  propChanges.firePropertyChange("UVcDisp", null, this);
	 }  // paint()

	 public void plotBaseline(double bx, double by, double bz,
									  double h1, double h2,
									  double sd, double cd, double s,
									  Graphics g) {
		  double old_u;
		  double old_v;
		  double u;
		  double v;         // u,v coordinates (working variables)

		  bx = bx * s;
		  by = by * s;
		  bz = -bz * s;
		  // The minus sign allows for english-order in ant[i].y value (as 
		  // read from screen), i.e. positive is south. Not sure we fully
		  // understand this, but it seems to work okay at this stage.

		  // Determine the starting point
		  double sh = Math.sin(h1);
		  double ch = Math.cos(h1);
		  old_u = bx * sh + by * ch;
		  old_v = -bx * sd * ch + by * sd * sh + bz * cd;

		  for (double h = h1; h <= h2; h += 0.06) {
				sh = Math.sin(h);
				ch = Math.cos(h);

				u = bx * sh + by * ch;
				v = -bx * sd * ch + by * sd * sh + bz * cd;
				g.drawLine( (int)old_u, (int)old_v, (int)u, (int)v);
				g.drawLine( (int)-old_u, (int)-old_v, (int)-u, (int)-v);
				old_u = u;
				old_v = v;
		  }   // Points along track

		  // Complete the track all the way to h2
		  sh = Math.sin(h2);
		  ch = Math.cos(h2);
		  u = bx * sh + by * ch;
		  v = -bx * sd * ch + by * sd * sh + bz * cd;
		  g.drawLine( (int)old_u, (int)old_v, (int)u, (int)v);
		  g.drawLine( (int)-old_u, (int)-old_v, (int)-u, (int)-v);

		  if(aux.ha1 == aux.ha2) {
				g.drawRect( (int)u, (int)v, 1, 1);
				g.drawRect( (int)-u, (int)-v, 1, 1);
		  }
	 }  // plotBaseline()


	 public float[] uvCoverage(int size) {
		  Rectangle r = bounds();
		  float[] uvcov = new float[size*size];
		  int tx;
		  int ty;
		  int tw;
		  int th;            // Used for the shadow plot
		  double bx;
		  double by;
		  double bz;                // X, Y and Z coords of baseline

		  System.out.print("Applying UV coverage... ");
		  System.out.flush();

		  // Calculate the metres-to-wavelengths-to-pixels conversion scale factor
		  double s = aux.freq / 299.792458 / 1000.0 * displaySize / displayScale;

		  // Plot the tracks
		  double cd = Math.cos(aux.dec);
		  double sd = Math.sin(aux.dec);

		  // Plot accumulated baselines
		  if(!(colour.equals(Color.black))) {
				for (int i = 0; i < accum.num_tracks; i++) {
					 applyUV(accum.bx[i], accum.by[i], accum.bz[i], 
								accum.h1[i], accum.h2[i], sd, cd, s, size, uvcov);
				}
		  }

		  // Plot baselines from the current array
		  for(int i = 0; i < (obs.ant.length-1); i++) {
				for(int j = i+1; j < (obs.ant.length); j++) {

					 by = obs.ant[i].EW - obs.ant[j].EW;
					 bz = ( obs.ant[i].NS - obs.ant[j].NS ) * Math.cos(obs.latitude);
					 bx = ( obs.ant[i].NS - obs.ant[j].NS ) * Math.sin(obs.latitude);
					 applyUV(bx, by, bz, aux.ha1, aux.ha2, sd, cd, s, size, uvcov);

				}  
		  }  
		  System.out.println("done.");
		  return uvcov;
	 }  // uvCoverage()


	 public void applyUV(double bx, double by, double bz,
								double h1, double h2,
								double sd, double cd, double s,
								int size, float[] uvcov) {
		  double uvscale = (double) size / 200.0;
		  double old_u;
		  double old_v;
		  double u;
		  double v;         // u,v coordinates (working variables)
		  int uc, vc;    // UV coordinates scaled to FFT size

		  bx = bx * s;
		  by = by * s;
		  bz = -bz * s;
		  // The minus sign allows for english-order in ant[i].y value (as 
		  // read from screen), i.e. positive is south.

		  // Determine the starting point
		  double sh = Math.sin(h1);
		  double ch = Math.cos(h1);
		  old_u = bx * sh + by * ch;
		  old_v = -bx * sd * ch + by * sd * sh + bz * cd;
		  // System.err.println(String.format("applyUV: Actually applying UV coverage: %f.2, %f.2", h1, h2));
		  for (double h = h1; h <= h2; h += 0.06) {
				sh = Math.sin(h);
				ch = Math.cos(h);

				u = bx * sh + by * ch;
				v = -bx * sd * ch + by * sd * sh + bz * cd;
				uc = (int)(u*uvscale);
				vc = (int)(v*uvscale);
				if (uc > -size/2 && uc < size/2 && vc > -size/2 && vc < size/2) {
					 uvcov[(vc+size/2)*size+uc+size/2]  = 1;
					 uvcov[(-vc+size/2)*size-uc+size/2] = 1;
				}
		  }   // Points along track

		  // Complete the track all the way to h2
		  sh = Math.sin(h2);
		  ch = Math.cos(h2);
		  u = bx * sh + by * ch;
		  v = -bx * sd * ch + by * sd * sh + bz * cd;
		  uc = (int)(u*uvscale);
		  vc = (int)(v*uvscale);
		  if (uc > -size/2 && uc < size/2 && vc > -size/2 && vc < size/2) {
				uvcov[(vc+size/2)*size+uc+size/2]  = 1;
				uvcov[(-vc+size/2)*size-uc+size/2] = 1;
		  }
	 }  // plotBaseline()

}
