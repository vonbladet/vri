/*
 * vriLocation.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

// class Scaler {
// 	 Scalar(vriLocation ref, double displayScale, double dispalySize);

//   public void xyz2NEU(vriLocation l) {
//     EW = ref.EW + (double)(l.x-ref.x)*scale/size;
//     NS = ref.NS - (double)(l.y-ref.y)*scale/size;
//     UD = 0.0;
//   }

//   public void NEU2xyz(vriLocation l) {
//     x = ref.x + (int) ( (EW-ref.EW)*size/scale );
//     y = ref.y - (int) ( (NS-ref.NS)*size/scale );
//     z = 0;
//   }
// }

class vriLocation {
	 public double NS;    // Plane coordinates wrt the Observatory longitude,
	 public double EW;    // latitude and altitude (metres). NS = North-South,
	 public double UD;    // EW = East-West, UD = Up-Down
	 public int x;        // Screen coordinates for plotting (pixels)
	 public int y;
	 public int z;
	 
	 

	 public vriLocation() {
		  this.x = 0;
		  this.y = 0;
		  this.z = 0;
		  this.NS = 0.0;
		  this.EW = 0.0;
		  this.UD = 0.0;
	 }

	 static vriLocation fromKM(double aNS, double aEW, double aUD) {
		  return new vriLocation(1000*aNS, 1000*aEW, 1000*aUD);
	 }

	 public vriLocation(double aNS, double aEW, double aUD) {
		  NS = aNS;
		  EW = aEW;
		  UD = aUD;
		  x = y = z = 0;
	 }

	 public void xyz2NEU(vriLocation ref, double scale, double size) {
		  EW = ref.EW + (double)(x-ref.x)*scale/size;
		  NS = ref.NS - (double)(y-ref.y)*scale/size;
		  UD = 0.0;
	 }

	 public void NEU2xyz(vriLocation ref, double scale, double size) {
		  x = ref.x + (int) ( (EW-ref.EW)*size/scale );
		  y = ref.y - (int) ( (NS-ref.NS)*size/scale );
		  z = 0;
	 }
}
