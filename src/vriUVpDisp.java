/*
 * vriUVpDisp.java
 *
 * Used in the Virtual Radio Interferometer.
 *
 * 06/Jan/1998 Nuria McKay - Extracted from vriGreyDisp.java
 *
 */
package nl.jive.vri;

import nl.jive.utils.*;
import java.applet.Applet;
import java.beans.*;

class vriUVpDisp extends vriGreyDisp {
	 FFTArray fft;

	 public vriUVpDisp(Applet app) {
		  super(app);
		  setUnit("lambda");
		  message = new String("No current transform");
	 }

	 public void fft(FFTArray dat) {
		  if (dat.data == null) {
				System.err.println("UVpDisp dat not set");
				return;
		  }
		  message = new String("Fourier transforming...");
		  repaint();
		  fft = new FFTArray(dat.imsize, vriUtils.fft(dat.data, dat.imsize));
		  fftToImg(fft);
		  propChanges.firePropertyChange("fft", null, fft);
	 }

	 public void fftToImg(FFTArray fft) {
		  int pix [] = vriUtils.fftToPix(fft.data, type, fft.imsize);
		  pixToImg(pix, fft.imsize);
		  message = null;
		  repaint();
	 }

	 // sets fft if this is the convolved class
	 public void applyUVc(SquareArray cov, FFTArray fft0) {
		  // Applies the UV coverage (from the UVcDisp class) to the FFT
		  // (the fft[] array).

		  if (fft0.data == null) {
				System.err.println("applyUVc: fft[] array empty");
				return;
		  }
		  message = new String("Applying UV coverage...");
		  repaint();

		  float a[] = vriUtils.applyUVc(cov.data, fft0.data, fft0.imsize);
		  fft = new FFTArray(fft0.imsize, a);

		  fftToImg(fft);
		  propChanges.firePropertyChange("fft", null, fft);
	 }
}
