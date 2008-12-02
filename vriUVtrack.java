/*
 * vriUVtrack.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vri.java
 *
 */

package nl.jive.vri;

class vriUVtrack {
  public double bx[];
  public double by[];
  public double bz[];
  public double h1[];
  public double h2[];
  public int num_tracks;

  public vriUVtrack() {
    num_tracks = 0;
  }

  public void add(double new_bx, double new_by, double new_bz,
                  double new_h1, double new_h2) {


    double bx_temp[] = new double[num_tracks+1];
    double by_temp[] = new double[num_tracks+1];
    double bz_temp[] = new double[num_tracks+1];
    double h1_temp[] = new double[num_tracks+1];
    double h2_temp[] = new double[num_tracks+1];

    for (int i = 0; i < num_tracks; i++) bx_temp[i] = bx[i];
    bx_temp[num_tracks] = new_bx;
    bx = bx_temp;

    for (int i = 0; i < num_tracks; i++) by_temp[i] = by[i];
    by_temp[num_tracks] = new_by;
    by = by_temp;

    for (int i = 0; i < num_tracks; i++) bz_temp[i] = bz[i];
    bz_temp[num_tracks] = new_bz;
    bz = bz_temp;

    for (int i = 0; i < num_tracks; i++) h1_temp[i] = h1[i];
    h1_temp[num_tracks] = new_h1;
    h1 = h1_temp;

    for (int i = 0; i < num_tracks; i++) h2_temp[i] = h2[i];
    h2_temp[num_tracks] = new_h2;
    h2 = h2_temp;

    num_tracks++;
  }

  public void clear() {
    num_tracks = 0;
  }
}
