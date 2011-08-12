/*
 * vriLocation.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;


class vriLocation 
	 implements isVisible
{
	 public double NS;    // Plane coordinates wrt the Observatory longitude,
	 public double EW;    // latitude and altitude (metres). NS = North-South,
	 public double UD;    // EW = East-West, UD = Up-Down

	 public vriLocation(double aNS, double aEW, double aUD) {
		  NS = aNS;
		  EW = aEW;
		  UD = aUD;
	 }

	 public vriLocation() {
		  this.NS = 0.0;
		  this.EW = 0.0;
		  this.UD = 0.0;
	 }

	 static vriLocation fromKM(double aNS, double aEW, double aUD) {
		  return new vriLocation(1000*aNS, 1000*aEW, 1000*aUD);
	 }


	 public void moveTo(vriLocation l) {
		  this.NS = l.NS;
		  this.EW = l.EW;
		  this.UD = l.UD;
	 }
	 
	 public boolean isVisible(double ha, double dec) {
		  return true;
	 }

	 static double dist2(vriLocation l1, vriLocation l2) {
		  double dist = (Math.pow((l1.NS - l2.NS), 2.0) + 
					 Math.pow((l1.EW - l2.EW), 2.0));
		  return dist;
	 }

}
