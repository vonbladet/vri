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
import nl.jive.earth.*;

abstract class vriUVcDisp extends vriDisplay 
implements PropertyChangeListener 
{
	 String unit = "lambda";
	 double c = 299.792458; // in km/s because freq is in kHz
	 vriObservatory obs;
	 vriAuxiliary aux;
	 double earthScale;
	 double spaceScale;
	 boolean useEarthScale;
	 AffineTransform aff, defaultTransform;
	 SquareArray uvcov;

	 vriUVcDisp(vriObservatory o, vriAuxiliary a) {
		  super();
		  aux = a;
		  setObservatory(o);
		  double lengthScale = obs.getLengthScale(); // in m
		  double scale = roundUpPower(lengthScale/(1000*getReferenceLambda()));
		  aff = new AffineTransform();
		  defaultTransform = (AffineTransform) aff.clone();
		  System.err.println("Default scale: "+scale);
		  System.err.println("Default lambda: "+getReferenceLambda());
		  earthScale = scale;
		  spaceScale = scale; // just in case it isn't taken care of.
		  useEarthScale = false;
		  propChanges = new PropertyChangeSupport(this);
	 }

	 public void setObservatory(vriObservatory aobs) {
		  obs = aobs;
		  double lengthScale = obs.getLengthScale(); // in m
		  double scale = roundUpPower(lengthScale/(1000*getReferenceLambda()));
		  earthScale = scale;
		  repaint();
	 }
	 
	 public void setUseEarthScale(boolean b) {
		  useEarthScale = b;
	 }

	 public double getConvScale() {
		  return spaceScale;
	 }

	 public void setConvScale(double cs) {
		  spaceScale = cs;
	 }

	 double getLambda() {
		  return c/aux.freq;
	 }

	 double getReferenceLambda() {
		  return c/4800.0;
	 }

	 double getActualScale() {
		  double s;
		  int width = getWidth();
		  if (useEarthScale) {
				s = (1 / (1000.0*getLambda()) * 
					  width / earthScale * aff.getScaleX());
		  } else {
				s = 1/getLambda() * width / spaceScale * aff.getScaleX();
		  }
		  return s;
	 }

	 void paintScale(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  int width = getWidth();
		  g2.setColor(Color.white);

		  double displayScale;
		  if (useEarthScale) {
				displayScale = earthScale/aff.getScaleX();
		  } else {
				displayScale = 1e-3*spaceScale/aff.getScaleX();
		  }
		  double l = roundPower(displayScale * (width - 20.0) / width);
		  int m = (int) Math.round(l * width / displayScale);
		  g.drawLine(10, r.height-10, 10+m, r.height-10);
		  String str = roundUnit(1000*l, "lambda");
		  g2.drawString(str, 10, r.height-12);
	 }

	 public SquareArray getUVCoverage() {
		  return uvcov;
	 }

	 public void uvCoverage(int size) {
		  Rectangle r = getBounds();
		  uvcov = new SquareArray(size);

		  double s = 1/(getLambda())*(size/spaceScale);
		  System.err.println("spaceScale: " + spaceScale);
		  System.err.println("earthScale: " + earthScale);
		  System.err.println("s: "+s);
		  // Plot baselines
		  Baseline[] baselines = obs.getBaselines();
		  System.err.println("Obs.baselines length: "+baselines.length);
		  for (int i = 0; i<baselines.length; i++) {
				Baseline bl = baselines[i];
				applyUV(bl, aux.ha1, aux.ha2, aux.dec, s, size, uvcov);
		  } 
		  boolean nonzero = false;
		  for (int i=0; i<size*size; i++) {
				if (uvcov.data[i] != 0) {
					 nonzero=true;
					 break;
				}
		  }
		  if (!nonzero) {
				System.err.println("uvcov is zero");
		  }
		  propChanges.firePropertyChange("uvcov", null, uvcov);
		  if (uvcov==null) {
				System.err.println("uvcov still null");
		  } else {
				System.err.println("** UV coverage changed!");
		  }
	 }  // uvCoverage()

	 public void applyUV(Baseline bl,
								double h1, double h2,
								double dec, double s,
								int size, SquareArray uvcov) {

		  //FIXME : I don't understand uvscale
		  //double uvscale = (double) size / 200.0;
		  double uvscale = 1;

		  ArrayList<UV> uvTrack = bl.makeUVTrack(h1, h2, dec, s);

		  for (int i=0; i<uvTrack.size(); i++) {
				UV uv = uvTrack.get(i);
 				int uc = (int)(uv.u*uvscale);
 				int vc = (int)(uv.v*uvscale);
				if (uc > -size/2 && uc < size/2 && vc > -size/2 && vc < size/2) {
					 uvcov.set(vc+size/2, uc+size/2, 1);
					 uvcov.set(-vc+size/2, -uc+size/2, 1);
				}
		  }   
	 }  
	  
	 abstract boolean isSelected(Baseline bl);

	 public void plotBaseline(Baseline bl,
									  double h1, double h2, double dec, 
									  double s, Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;

		  ArrayList<UV> uvTrack = bl.makeUVTrack(h1, h2, dec, s);

		  if (isSelected(bl)) {
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


	 public void paint(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  // g2.setTransform(aff);
		  Rectangle r = getBounds();
		  int tx, ty;
		  int tw, th;  // Used for the shadow plot

		  double displayScale = getDisplayScale(); 
		  g2.setColor(Color.black);
		  g2.fillRect(1, 1, r.width-2, r.height-2);
		  g2.setColor(Color.darkGray);
		  g2.drawRect(0, 0, r.width-1, r.height-1);
		  // Calculate the metres-to-wavelengths-to-pixels conversion scale factor

		  double s = getActualScale();
		  System.err.println("Actual scale: "+s);
		  // Do the shadow zone (use the tx/ty/tw/th parameters temporarily)
		  tx = ty = (int) (-s * obs.ant_diameter);
		  tw = th = (int) (s * 2.0 * obs.ant_diameter);
		  g2.fillOval(tx, ty, tw, th);


		  g2.translate(getWidth()/2.0, getHeight()/2.0);
		  Baseline[] baselines = obs.getBaselines();
		  for  (int i = 0; i<baselines.length; i++) {
				Baseline bl = baselines[i];
				plotBaseline(bl, aux.ha1, aux.ha2, aux.dec, s, g);
		  } 
		  g2.translate(-getWidth()/2.0, -getHeight()/2.0);
		  paintScale(g);
		  plotFocus(g);
	 }  
}

class vriNSEWUVcDisp extends vriUVcDisp 
{
	 vriLocation selectedAnt;

	 public vriNSEWUVcDisp(vriObservatory o, vriAuxiliary a) {
		  super(o, a);
		  selectedAnt = null;
	 }

	 public void propertyChange(PropertyChangeEvent e) {
		  String s = e.getPropertyName();
		  if (s=="active") {
				selectedAnt = (vriLocation) e.getNewValue();
				repaint();
		  } else {
				System.err.println("** UVcDisp: Got propertyChange for "+e.getPropertyName());
		  }
	 }

	 boolean isSelected(Baseline bl) {
		  NSEWBaseline nbl = (NSEWBaseline) bl;
		  return selectedAnt != null && (selectedAnt==nbl.ant1 || selectedAnt==nbl.ant2);
	 }
}

class vriLatLonUVcDisp extends vriUVcDisp 
{
	 LatLon selectedAnt;

	 public vriLatLonUVcDisp(vriObservatory o, vriAuxiliary a) {
		  super(o, a);
		  selectedAnt = null;
	 }

	 public void propertyChange(PropertyChangeEvent e) {
		  String s = e.getPropertyName();
		  if (s=="active") {
				selectedAnt = (LatLon) e.getNewValue();
				repaint();
		  }
	 }

	 boolean isSelected(Baseline bl) {
		  LatLonBaseline nbl = (LatLonBaseline) bl;
		  return selectedAnt != null && (selectedAnt==nbl.ant1 || selectedAnt==nbl.ant2);
	 }
}



