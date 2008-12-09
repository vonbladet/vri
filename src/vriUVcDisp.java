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
import java.util.*;
import java.awt.geom.*;


class vriUVcDisp extends vriDisplay 
implements PropertyChangeListener {
	 vriObservatory obs;
	 vriAuxiliary aux;
	 vriLocation selectedAnt;

	 public vriUVcDisp(vriObservatory o, vriAuxiliary a) {
		  super();
		  selectedAnt = null;
		  obs = o;
		  aux = a;
		  aff = new AffineTransform();
		  aff.scale(10000.0, 10000.0);   // Expressed in kilowavelengths
		  defaultTransform = (AffineTransform) aff.clone();
	 }

	 public void setObservatory(vriObservatory aobs) {
		  obs = aobs;
	 }

	 public void propertyChange(PropertyChangeEvent e) {
		  String s = e.getPropertyName();
		  if (s=="active") {
				selectedAnt = (vriLocation) e.getNewValue();
				repaint();
		  }
	 }

	 double roundPower(double l) {	
		  l = Math.log(l)/Math.log(10.0);
		  l = Math.pow(10.0, Math.floor(l));
		  return l;
	 }	  

	 void paintScale(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  double displayScale = getDisplayScale();

		  g2.setColor(Color.white);
		  double l = roundPower(displayScale * (width - 20.0) / width);
		  int m = (int) Math.round(l * width / displayScale);
		  g.drawLine(10, r.height-10, 10+m, r.height-10);
		  String str = new String();
		  if(l <= 1.0) {
				str = Double.toString(l*1000.0) + "lambda";
		  } else {
				str = Double.toString(l) + "klambda";
		  }
		  g2.drawString(str, 10, r.height-12);
	 }


	 public void paint(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  // g2.setTransform(aff);
		  Rectangle r = getBounds();
		  int tx, ty;
		  int tw, th;            // Used for the shadow plot

		  double displayScale = getDisplayScale(); 
		  // Do the grid
		  g2.setColor(Color.black);
		  g2.fillRect(1, 1, r.width-2, r.height-2);
		  g2.setColor(Color.darkGray);
		  g2.drawRect(0, 0, r.width-1, r.height-1);
		  int width = getWidth();
		  // Calculate the metres-to-wavelengths-to-pixels conversion scale factor
		  double s = aux.freq / 299.792458 / 1000.0 * width / displayScale;

		  // Do the shadow zone (use the tx/ty/tw/th parameters temporarily)
		  tx = ty = (int) (-s * obs.ant_diameter);
		  tw = th = (int) (s * 2.0 * obs.ant_diameter);
		  g2.fillOval(tx, ty, tw, th);

		  // Plot the tracks

		  System.err.println(String.format("Hour angles: %.2f, %.2f", aux.ha1, aux.ha2));
		  g2.translate(getWidth()/2.0, getHeight()/2.0);
		  // Plot baselines from the current array
		  Baseline[] baselines = obs.getBaselines();
		  for  (int i = 0; i<baselines.length; i++) {
				Baseline bl = baselines[i];
				plotBaseline(bl, aux.ha1, aux.ha2, aux.dec, s, g);
		  } 
		  g2.translate(-getWidth()/2.0, -getHeight()/2.0);
		  paintScale(g);
		  plotFocus(g);

		  propChanges.firePropertyChange("UVcDisp", null, this);
	 }  

	 public void plotBaseline(Baseline bl,
									  double h1, double h2, double dec, 
									  double s, Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;

		  ArrayList<UV> uvTrack = bl.makeUVTrack(h1, h2, dec, s);

		  if (selectedAnt != null && (selectedAnt==bl.ant1 || selectedAnt==bl.ant2)) {
				g2.setColor(Color.blue);
		  } else {
				g2.setColor(Color.red);
		  }

		  if (uvTrack.size()==1) {
				UV uv = uvTrack.get(0);
				g2.draw(new Rectangle((int)uv.u, (int)uv.v, 1, 1));
				g2.draw(new Rectangle((int)-uv.u, (int)-uv.v, 1, 1));
		  } else {
				int xs[] = new int[uvTrack.size()];
				int ys[] = new int[uvTrack.size()];
				GeneralPath path1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xs.length);
				GeneralPath path2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xs.length);
				
				UV uv = uvTrack.get(0);
				path1.moveTo((int)uv.u, (int)uv.v);
				path2.moveTo((int)-uv.u, (int)-uv.v);
				for (int i=1; i<uvTrack.size(); i++) {
					 uv = uvTrack.get(i);
					 path1.lineTo( (int)uv.u, (int)uv.v);
					 path2.lineTo( (int)-uv.u, (int)-uv.v);
				}   
				g2.draw(path1);
				g2.draw(path2);
		  }
		  g2.setColor(Color.red);
	 }  


	 public float[] uvCoverage(int size) {
		  Rectangle r = getBounds();
		  double displayScale = getDisplayScale();
		  float[] uvcov = new float[size*size];
		  int tx, ty;
		  int tw, th;          // Used for the shadow plot

		  System.out.print("Applying UV coverage... ");
		  System.out.flush();

		  // Calculate the metres-to-wavelengths-to-pixels conversion scale factor
		  double s = aux.freq / 299.792458 / 1000.0 * width / displayScale;

		  // Plot baselines
		  Baseline[] baselines = obs.getBaselines();
		  for (int i = 0; i<baselines.length; i++) {
				Baseline bl = baselines[i];
				applyUV(bl, aux.ha1, aux.ha2, aux.dec, s, size, uvcov);
		  } 
		  System.out.println("done.");
		  return uvcov;
	 }  // uvCoverage()

	 public void applyUV(Baseline bl,
								double h1, double h2,
								double dec, double s,
								int size, float[] uvcov) {

		  double uvscale = (double) size / 200.0;

		  ArrayList<UV> uvTrack = bl.makeUVTrack(h1, h2, dec, s);

		  for (int i=0; i<uvTrack.size(); i++) {
				UV uv = uvTrack.get(i);
				int uc = (int)(uv.u*uvscale);
				int vc = (int)(uv.v*uvscale);
				if (uc > -size/2 && uc < size/2 && vc > -size/2 && vc < size/2) {
					 uvcov[(vc+size/2)*size+uc+size/2]  = 1;
					 uvcov[(-vc+size/2)*size-uc+size/2] = 1;
				}
		  }   
	 }  

}
