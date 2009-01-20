/*
 * vriImgDisp.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 05/Jan/1998 Nuria McKay - Moved to separate file from vriGreyDisp.java
 *
 */

package nl.jive.vri;

import java.applet.Applet;
import java.beans.*;
import nl.jive.utils.*;
import java.awt.*;

class vriImgDisp extends vriGreyDisp {
	 FFTArray dat;

	 public vriImgDisp(Applet app) {
		  super(app);
		  setUnit("as");
		  propChanges = new PropertyChangeSupport(this);
		  message = new String("No current image");
	 }

	 public void loadAstroImage(AstroImage ai) {
		  setFullScale(ai.scale);
		  String str = ai.filename;
		  System.out.println("ImgDisp: begin load "+str);
		  loadImage(str);
		  System.out.println("image loaded");
		  try {
				int pix[] = imgToPix(img);
				int imh = img.getHeight(this);
				int imw = img.getWidth(this);
				imsize = vriUtils.getImsize(imh, imw);
				System.err.println("Imsize: "+imsize);
				vriUtils.greyPix(pix, imh, imw);
				dat = new FFTArray(imsize, 
										 vriUtils.pixToDat(pix, imh, imw, imsize));
				vriUtils.scaleDat(dat.data);
				pix = vriUtils.datToPix(dat.data, dat.imsize);
				pixToImg(pix, dat.imsize);
				System.out.println("ImgDisp: end operation ----");
				message = null;
				repaint();
				propChanges.firePropertyChange("dat", null, dat);
		  } catch (EmptyImageException e) {
				System.err.println("ImgDisp: Empty image");
		  }
		  repaint();
	 }

	 public void invfft(FFTArray fft) {
		  if (fft.data == null) {
				System.err.println("ImgDisp fft not set");
				return;
		  }
		  message = new String("Inverse fourier transforming...");
		  repaint();
		  int nn[] = {imsize, imsize};
		  System.out.print("Doing inverse transform... ");
		  float[] dat = new float[fft.data.length];
		  for (int i = 0; i < dat.length; i++) 
				dat[i] = fft.data[i]/imsize/imsize;
		  Fourier.fourn(dat, nn, 2, -1);
		  System.out.println("done.");
		  int pix[] = vriUtils.datToPix(dat, fft.imsize);
		  pixToImg(pix, fft.imsize);
		  message = null;
		  repaint();
	 }
}