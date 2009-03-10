/*
 * vriGreyDisp.java
 *
 * Used in the Virtual Radio Interferometer to transform between images
 * and the U-V plane. Based on FFTTool by Mark Wieringa, 26/Dec/1996.
 *
 * 16/Jan/1997  Derek McKay
 *
 */

package nl.jive.vri;

import java.applet.Applet;
import java.lang.Math;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.net.*;
import javax.swing.*;
import nl.jive.utils.*;

//####################################################################//

class EmptyImageException extends Exception {};


class FFTArray {
	 int imsize;
	 float data[];

	 public FFTArray(int i) {
		  imsize = i;
		  data = new float[2*imsize*imsize];
	 }

	 static FFTArray fromSquareArray(SquareArray sa) {
		  FFTArray result = new FFTArray(sa.size);
		  for (int i=0; i<sa.size; i++) {
				for (int j=0; j<sa.size; j++) {
					 result.set(i, j, sa.get(i, j), 0);
				}
		  }
		  return result;
	 }

	 public FFTArray(int i, float[] d) {
		  imsize = i;
		  data = d;
	 }

	 public void set(int i, int j, float real, float im) {
		  data[j*imsize*2 + i*2] = real;
		  data[j*imsize*2 + i*2 + 1] = im;
	 }
	 // y1*imsize*2 + x1*2 = real
	 // y1*imsize*2 + x1*2 + 1 = imag
}

class SquareArray {
	 int size;
	 float data[];

	 public SquareArray(int i) {
		  size = i;
		  data = new float[size*size];
	 }

	 public SquareArray(int i, float[] d) {
		  size = i;
		  data = d;
	 }
	 public void set(int i, int j, float val) {
		  data[j*size+i] = val;
	 }

	 public float get(int i, int j) {
		  return data[j*size+i];
	 }
}

class vriUtils {

	 public static double logn(double x, double n) {
		  return Math.log(x)/Math.log(n);
	 }

	 static float[] minmax(float arr[]) {
		  float min = arr[0];
		  float max = arr[0];
		  for (int i=1; i<arr.length; i++) {
				if (arr[i] < min) min = arr[i];
				if (arr[i] > max) max = arr[i];
		  }
		  float[] res = {min, max};
		  return res;
	 }


	 static void scaleDat(float dat[]) {
		  float[] minmaxv = vriUtils.minmax(dat);
		  float min = minmaxv[0];
		  float max = minmaxv[1];
		  int mean=0;
		  for (int i = 0; i < dat.length; i++) mean += dat[i];
		  // System.out.print("Limits = ["+min+","+max+"], sum = "+mean);
		  mean /= (float) dat.length;
		  // System.out.print(", mean = "+mean);
		  for (int i = 0; i < dat.length; i++) {
				dat[i] = (dat[i] - mean) / (max - min);
		  }
		  minmaxv = vriUtils.minmax(dat);
		  min = minmaxv[0];
		  max = minmaxv[1];
		  // System.out.println(", new limits = ["+min+","+max+"]");
	 }

	 static public int getImsize(int imh, int imw) {

		  // Determine minimum sized box that image could fit into
		  int imsize = Math.max(imw, imh);
		  int i = (int)logn(imsize, 2);
		  if (i>=10) { // Abandon exercise if it is too big
				System.err.println("Error: image too large (max = 1024x1024)");
				return 0;
		  } else {
				imsize = (int) Math.pow(2,i);
		  }
		  return imsize;
	 }

	 static int[] datToPix(float dat[], int imsize) {
		  // System.out.println("Converting (datToPix)");

		  // determine the scale of the data
		  float[] minmaxv = minmax(dat);
		  float min = minmaxv[0];
		  float max = minmaxv[1];
		  // System.out.println("Data minimum="+min+", maximum="+max);

		  int pix[] = new int[imsize * imsize];
		  for (int y = 0; y < imsize; y++) {
				for  (int x = 0; x < imsize; x++) {
					 int x1,y1;
					 x1 = x; y1 = y;
					 int grey = (int) (
											 255 * (dat[y1*imsize*2+x1*2] - min) /
											 (max - min)
											 );
					 pix[y*imsize+x] = 0xff000000 | (grey << 16) | (grey << 8) | grey;
				}
		  }
		  return pix;
	 }

	 static public void greyPix(int[] pix, int imh, int imw) {
		  // System.out.println("greyPix: "+imh+"x"+imw);
		  if (pix != null) {
				for (int i=0; i<imh*imw; i++) {
					 int red   = (pix[i] & 0x00ff0000) >> 16;
					 int green = (pix[i] & 0x0000ff00 >> 8);
					 int blue  = (pix[i] & 0x000000ff);
					 int grey  = (red+green+blue)/3;
					 pix[i] = (pix[i] & 0xff000000) |
						  (grey << 16) | (grey << 8) | grey;
				}
		  } else {
				System.err.println("greyPix: pix empty");
		  }    
	 }

	 static public float[] pixToDat(int[] pix, int imh, int imw, int imsize) {
		  // System.out.println("Converting (pixToDat): "+imh+","+imw+","+imsize);
		  if (pix == null) {
				System.err.println("pixToDat: pix empty");
				return new float[]{(float)0.0};
		  }

		  // find mean value along the edge (to make the padding realistic)
		  float mean = 0;
		  int count = 0;
		  for (int h = 0; h < imh; h++) {
				int inc = imw-1;
				if(h == 0 || h == (imh-1)) inc = 1;
				for(int w = 0; w < imw; w += inc) {
					 mean += (float)(pix[h*imw + w] & 0x000000ff);  
					 // Assume already greyscale -
					 // i.e. red = green = blue
					 count++;                                       
				}
		  }
		  //		  System.out.print("Edge sum = "+mean);
		  mean /= (float)count;
		  // System.out.println("; edge mean = "+mean);
		  // for now use full complex transform
		  float dat[] = new float[imsize*2*imsize];
		  for (int h = 0; h < imh; h++) {
				for (int w = 0; w < imw; w++) {
					 dat[(imsize/2+h-imh/2)*imsize*2 + (imsize/2+w-imw/2)*2] = 
						  (pix[h*imw + w] & 0x000000ff)-mean;
				}
		  }
		  // scale to range of one around zero
		  return dat;
	 }


	 static int[] fftToPix(float fft[], String type, int imsize) {
		  double value;        // Quantity that is plotted to a pixel
		  // System.out.println("Converting (fftToPix)");

		  // determine the scale of the data (use a forced -180:180 range for phase
		  float[] minmaxv = minmax(fft);
		  float min = minmaxv[0];
		  float max = minmaxv[1];
		  if(type.equals("Phase")) {
				min = (float)(-Math.PI / 2.0);
				max = (float)( Math.PI / 2.0);
		  }
		  // System.out.println("FFT minumum="+min+", maximum="+max);

		  int pix[] = new int[imsize * imsize];
		  for (int y = 0; y < imsize; y++) {
				for  (int x = 0; x < imsize; x++) {
					 int x1, y1;
					 // shift origin to center of image
					 x1 = x - imsize/2;
					 y1 = y - imsize/2;
					 if (x1 < 0) x1 += imsize;
					 if (y1 < 0) y1 += imsize;

					 // Depending on the "type" of display selected, we extract the 
					 // relevant components of the fourier transform.
					 if(type.equals("Real")) {
						  value = fft[y1*imsize*2+x1*2];
					 } else if (type.equals("Imag.")) {
						  value = fft[y1*imsize*2+x1*2+1];
					 } else if (type.equals("Phase")) {
						  value = Math.atan2(fft[y1*imsize*2+x1*2],fft[y1*imsize*2+x1*2+1]);
					 } else {
						  // Used for Ampl., Colour and others that aren't handled
						  value = Math.sqrt(fft[y1*imsize*2+x1*2]*fft[y1*imsize*2+x1*2] +
												  fft[y1*imsize*2+x1*2+1]*fft[y1*imsize*2+x1*2+1]);
					 }

					 int grey = (int) (255.0 * (value - min) / (max - min) );
					 pix[y*imsize+x] = 0xff000000 | (grey << 16) | (grey << 8) | grey;

					 if(type.equals("Colour")) {
						  double h = Math.atan2(fft[y1*imsize*2+x1*2],fft[y1*imsize*2+x1*2+1])
								/ Math.PI / 2.0;
						  if(h < 0.0) h += 1.0;
						  pix[y*imsize+x] = Color.HSBtoRGB( (float) h, (float) 1.0,
																		(float) grey/(float)255.0);
					 }  // End if(colour)
				}  // End for(x)
		  }  // End for(y)
		  return pix;
	 }  

	 // fft was originally in vriUVpDisp
	 // and a stub remains there


	 static float[] fft(float dat[], int imsize) {
		  int[] nn = new int[] {imsize, imsize};
		  // System.out.print("Doing forward transform... ");
		  float[] fft = new float[dat.length];
		  for(int i = 0; i < dat.length; i++) 
				fft[i] = dat[i];
		  Fourier.fourn(fft, nn, 2, 1);
		  // System.out.println("done.");
		  return fft;
	 }

	 static public float[] dummyApplyUVc(float cov[], float fft[], int imsize) {
		  // Pretends to apply the UV coverage (from the UVcDisp class) to the FFT
		  // (the fft[] array).

		  // Get square array of floating point numbers with the taper
		  // function on pixels with UV coverage and 0 on those without
		  float[] fft2 = new float[2*imsize*imsize];
		  for(int y = 0; y < imsize; y++) {
				for(int x = 0; x < imsize; x++) {
					 int x1, y1;
					 x1 = x - imsize/2; y1 = y - imsize/2;
					 if (x1 < 0) x1 += imsize;
					 if (y1 < 0) y1 += imsize;
					 fft2[y1*imsize*2 + x1*2] =  cov[y*imsize+x];  // Real
					 fft2[y1*imsize*2 + x1*2 + 1] = cov[y*imsize+x];  // Imaginary
				}
		  }
		  return fft2;
	 }

	 static public float[] dummyApplyUVc2(float cov[], float fft[], int imsize) {
		  // Pretends to apply the UV coverage (from the UVcDisp class) to the FFT
		  // (the fft[] array).

		  // Get square array of floating point numbers with the taper
		  // function on pixels with UV coverage and 0 on those without
		  float[] fft2 = new float[2*imsize*imsize];
		  for(int y = 0; y < imsize; y++) {
				for(int x = 0; x < imsize; x++) {
					 int x1, y1;
					 x1 = x - imsize/2; y1 = y - imsize/2;
					 if (x1 < 0) x1 += imsize;
					 if (y1 < 0) y1 += imsize;
					 fft2[y1*imsize*2 + x1*2] =  fft[y1*imsize*2 + x1*2];  // Real
					 fft2[y1*imsize*2 + x1*2 + 1] = fft[y1*imsize*2 + x1*2 + 1];  // Imaginary
				}
		  }
		  return fft2;
	 }


	 // applyUVc from vriUVpDisp
	 static public float[] applyUVc(float cov[], float fft[], int imsize) {
		  // Applies the UV coverage (from the UVcDisp class) to the FFT
		  // (the fft[] array).

		  // Get square array of floating point numbers with the taper
		  // function on pixels with UV coverage and 0 on those without
		  float[] fft2 = new float[2*imsize*imsize];
		  for (int y = 0; y < imsize; y++) {
				for (int x = 0; x < imsize; x++) {
					 int x1, y1;
					 x1 = x - imsize/2; 
					 y1 = y - imsize/2;
					 if (x1 < 0) x1 += imsize;
					 if (y1 < 0) y1 += imsize;
					 fft2[y1*imsize*2 + x1*2] =  
						  fft[y1*imsize*2 + x1*2] * cov[y*imsize+x];  // Real
					 fft2[y1*imsize*2 + x1*2 + 1] = 
						  fft[y1*imsize*2 + x1*2 + 1]*cov[y*imsize+x];  // Imaginary
				}
		  }
		  return fft2;
	 }
}

class vriGreyDisp extends vriDisplay {
	 public enum Types {PHASE, REAL, IMAG, AMPL, COLOUR};
	 Applet applet;
	 //  boolean replot = false;
	 URL imgURL;
	 Image img;
	 static int imsize;     // Size of the "squared" image
	 String message = null; // Message to print on the Display
	 String type = "ampl";    // Used to select real/imag/amp/phase display
	 double fullScale = 73000.0;
	 String unit = "lobster";
	 boolean hasAxes = false;
	 String[] axesLabels = new String[] {"x", "y"};

	 public vriGreyDisp(Applet app) {
		  super();
		  applet = app;
	 }

	 void setHasAxes(boolean b) {
		  hasAxes = b;
	 }
	 
	 boolean getHasAxes() {
		  return hasAxes;
	 }

	 void setUnit(String u){
		  unit = u;
	 }
	 String getUnit() {
		  return unit;
	 }
	 void setFullScale(double s){
		  fullScale = s;
	 }
	 double getFullScale(){
		  return fullScale;
	 }

	 void paintScale(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  int width = img.getWidth(this); // getWidth();
		  double displayScale = getDisplayScale();
		  double l = roundPower(fullScale / displayScale * (width - 20.0) / width);
		  int m = (int) Math.round(l * width * displayScale /  fullScale);
		  String str = roundUnit(l, unit);
		  paintRealScale(g2, r, str, m);
	 }

	 void paintAxes(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Color oldColor = g2.getColor();
		  g2.setColor(Color.orange);
		  int imh = img.getHeight(this);
		  int imw = img.getWidth(this);
		  int offset = 10;
		  g2.draw(new Line2D.Float((float)offset, imh/2.0f, 
											(float)imw-offset, imh/2.0f));
		  g2.draw(new Line2D.Float(imw/2.0f, (float)offset, 
											imw/2.0f, (float)imh-offset));
		  // Arrow heads
		  int arrowWidth = 6;
		  int arrowHeight = 10;
		  Font font = g2.getFont();
		  FontRenderContext frc = g2.getFontRenderContext();
		  // top of y-axis
		  g2.draw(new Line2D.Float(imw/2.0f, (float)offset,
											imw/2.0f+arrowWidth, (float)offset+arrowHeight));
		  g2.draw(new Line2D.Float(imw/2.0f, (float)offset,
											imw/2.0f-arrowWidth, (float)offset+arrowHeight));
		  // and text for y-axis
		  Rectangle2D bounds = font.getStringBounds(axesLabels[1], frc);
		  g2.drawString(axesLabels[1], 
							 imw/2.0f+(float)arrowWidth+2.0f, 
							 (float)offset+(float)bounds.getHeight()/2.0f);
		  // right of x-axis
		  g2.draw(new Line2D.Float((float)imh-offset, imh/2.0f,
											(float)imh-offset-arrowHeight, imh/2.0f+arrowWidth));
		  g2.draw(new Line2D.Float((float)imh-offset, imh/2.0f,
											(float)imh-offset-arrowHeight, imh/2.0f-arrowWidth));
		  // text for x-axis
		  g2.drawString(axesLabels[0], 
							 (float)imh-(float)offset-(float)bounds.getWidth(), 
							 imw/2.0f+(float)bounds.getHeight()/2.0f+(float)arrowWidth+2.0f);


		  g2.setColor(oldColor);
	 }

	 public void paint(Graphics g) {
		  Graphics2D g2 = (Graphics2D) g;
		  Rectangle r = getBounds();
		  if (message != null) {
				g.setColor(Color.red);
				g.drawString(message, 20,20);
		  } else if (img != null) {
				// Get current image scale
				int imh = img.getHeight(this);
				int imw = img.getWidth(this);
				AffineTransform a = new AffineTransform();
				a.translate(-imw/2.0, -imh/2.0);
				a.preConcatenate(aff);
				a.preConcatenate(AffineTransform.getTranslateInstance(getWidth()/2.0,
																						getHeight()/2.0));
				g2.drawImage(img, a, applet);
				if (hasAxes) {
					 // System.err.println("Has axes");
					 paintAxes(g);
				} else {
					 //System.err.println("Not has axes");
				}
				paintScale(g);
		  }
		  plotFocus(g);
	 }

	 public void loadImage(String filename) {
		  System.out.println("Loading "+filename);
		  try {
				imgURL = getClass().getResource(filename);
				System.err.println("imgURL:"+imgURL);
				MediaTracker tracker = new MediaTracker(this);
				img = Toolkit.getDefaultToolkit().getImage(imgURL);
				tracker.addImage(img, 0);
				tracker.waitForID(0);
		  } 
		  catch (NullPointerException e) {
				System.err.println("Error with image load");
		  }
		  catch (Exception e) {
				System.err.println("Error with image load");
		  }
	 }

	 public int[] imgToPix(Image img) 
	 throws EmptyImageException
	 {
		  // System.out.println("Converting (imgToPix)");

		  if (img == null) {
				throw new EmptyImageException();
		  } 
		  int imh = img.getHeight(this);
		  int imw = img.getWidth(this);
		  // System.out.println("Got "+this+"image: size = "+imw+"x"+imh);
		  int pix[] = new int[imh*imw];
		  try {
				PixelGrabber pg = new PixelGrabber(img, 0, 0, imw, imh, pix, 0, imw);
				pg.grabPixels(100);
		  } catch (InterruptedException e){
				System.err.println("imgToPix failed");
		  }
		  return pix;
	 }

	 public void pixToImg(int pix[], int imsize) {
		  //System.out.println("Converting (pixToImg): "+
		  // imsize+"x"+imsize);

		  img = applet.createImage(new MemoryImageSource(imsize, imsize, pix, 0, imsize));
		  repaint();
	 }

}

