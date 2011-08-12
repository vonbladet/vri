/*
 * vriConfig.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

class vriConfig {
  public String name;
  public int spot[];

  public vriConfig() {
  }

  public vriConfig(int stn[], String s) {
    name = s;
    for(int i = 0; i < stn.length; i++) spot[i] = stn[i];
  }
}
