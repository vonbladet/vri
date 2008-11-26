/*
 * vriTrack.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

class vriTrack {
  public vriLocation start;    // One extreme of the track
  public vriLocation end;      // The other extreme of the track
  double rise;
  double run;
  double length;

  public vriTrack() {
  }

  public vriTrack(vriStation s, vriStation e) {
    start = s;
    end = e;
    rise = end.NS - start.NS;
    run = end.EW - start.EW;
    length = Math.sqrt( run*run + rise*rise );
  }

  public double distance(vriAntenna a) {
    return rise * (a.NS - start.NS) - run * (a.EW - start.EW) / length;
  }
}
