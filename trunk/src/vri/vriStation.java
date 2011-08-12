/*
 * vriStation.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

class vriStation extends vriLocation {

  public vriStation() {
    super();              // Invoke superclass's constructor
  }

  /**
   * Creates a vriStation instance from an NS, EW and UD
   * triplet. For general purpose antenna locations.
   *
   * @param ns
   * @param ew
   * @param ud
   * @return void
   * @exception none
   * @author Derek J. McKay
   */
  public vriStation(double ns, double ew, double ud) {
//    super();
    this.NS = ns * 1000.0;   // Need to convert km to m
    this.EW = ew * 1000.0;   // Need to convert km to m
    this.UD = ud;
  }

  /**
   * Creates a vriStation instance for an East-West antenna location.
   * The station is at a certain number of integer increments away from
   * the reference point. Each increment (space) is described as a
   * double precision number in metres. Used for incremental arrays like
   * the Australia Telescope Compact Array.
   * 
   * @param incr The number of multiples of the base spacing
   * @param ref The "incr" number of the reference point
   * @param space The size of a single increment in metres
   * @return void
   * @exception none
   * @author Derek J. McKay
   */
  public vriStation(int incr, int ref, double space) {
    super();              // Invoke superclass's constructor
    this.EW = (double)(ref - incr)*space;
  }

  /**
   * Creates a vriStation instance for an extension of an East-West
   * incremental antenna location system. The station is at a certain
   * number of non-integer increments away from the reference point (which
   * still remains on the East-West track. Each increment (space) is
   * described as a double precision number in metres. Useful for modified
   * incremental arrays like the ATCA's North-South spur extension.
   *
   * @param incr The number of multiples of the base spacing              
   * @param ref The "incr" number of the reference point
   * @param space The size of a single increment in metres
   * @return void   
   * @exception none
   * @author Derek J. McKay
   */
  public vriStation(double EWincr, double NSincr, double ref, double space) {
    super();              // Invoke superclass's constructor
    this.EW = (ref - EWincr) * space;
    this.NS = NSincr * space;
  }

}
