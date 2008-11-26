/*
 * vriAuxiliary.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

class vriAuxiliary {
  public double ha1;   // Starting hour angle of observation
  public double ha2;   // Ending hour angle of observation
  public double dec;   // Declination of source
  public double freq;  // Frequency of observation
  public double bw;    // Bandwidth of observation

  public vriAuxiliary() {
    ha1 = -Math.PI/2.0;
    ha2 = Math.PI/2.0;
    dec = Math.PI/2.0;
    freq = 4800.0;
    bw = 100.0;
  }

  public void report() {
    System.out.println(" ha1 = "+ha1);
    System.out.println(" ha2 = "+ha2);
    System.out.println(" dec = "+dec);
    System.out.println("freq = "+freq);
    System.out.println("  bw = "+bw);
  }
}
