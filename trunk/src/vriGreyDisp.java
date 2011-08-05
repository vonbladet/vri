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
import edu.emory.mathcs.jtransforms.fft.*;
import java.util.Arrays;

//####################################################################//

enum PlotTypes {PHASE, REAL, IMAG, AMPL, COLOUR};

class EmptyImageException extends Exception {};

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

    float[] minmax() {
        float min = data[0];
        float max = data[0];
        for (int i=1; i < data.length; i++) {
            if (data[i] < min) min = data[i];
            if (data[i] > max) max = data[i];
        }
        float[] res = {min, max};
        return res;
    }

    void scale() {
        float[] minmaxv = minmax();
        float min = minmaxv[0];
        float max = minmaxv[1];
        int mean=0;
        for (int i = 0; i < data.length; i++) mean += data[i];
        // System.out.print("Limits = ["+min+","+max+"], sum = "+mean);
        mean /= (float) data.length;
        // System.out.print(", mean = "+mean);
        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] - mean) / (max - min);
        }
    }

    public PixArray toPix() {
        // System.out.println("Converting (datToPix)");

        // determine the scale of the data
        float[] minmaxv = vriUtils.minmax(data);
        float min = minmaxv[0];
        float max = minmaxv[1];

        PixArray pix = new PixArray(size, size);
        for (int y = 0; y < size; y++) {
            for  (int x = 0; x < size; x++) {
                int x1, y1;
                x1 = x; y1 = y;
                int grey = (int) (255 * (get(x1, y1) - min) / (max - min));
                pix.set(x, y, 0xff000000 | (grey << 16) | (grey << 8) | grey);
            }
        }
        return pix;
    }




}

class FFTArray {
    int size;
    float data[];

    public FFTArray(int i) {
        size = i;
        data = new float[2*size*size];
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
        size = i;
        data = d;
    }

    public void set(int i, int j, float real, float im) {
        data[j*size*2 + i*2] = real;
        data[j*size*2 + i*2 + 1] = im;
    }

    public float getReal(int i, int j) {
        return data[j*size*2 + i*2];
    }
    
    public float getImag(int i, int j) {
        return data[j*size*2 + i*2 + 1];
    }

    public float getPhase(int i, int j) {
        return (float) Math.atan2(data[j*size*2+i*2],data[j*size*2+i*2+1]);
    }

    public float getAmpl(int i, int j) {
        return (float) Math.sqrt(data[j*size*2+i*2] * data[j*size*2+i*2] +
                                 data[j*size*2+i*2+1] * data[j*size*2+i*2+1]);
    }

    public FFTArray fft() {
        // System.out.print("FFTArray: Doing NEW forward transform... ");
        float[] fft = Arrays.copyOf(data, data.length);
        FloatFFT_2D f = new FloatFFT_2D(size, size);
        f.complexForward(fft);
        return new FFTArray(size, fft);
    }

    public FFTArray invfft() {
        float[] dat2 = new float[data.length];
        for (int i = 0; i < dat2.length; i++) {
            dat2[i] = data[i]/size/size;
        }
        FloatFFT_2D f = new FloatFFT_2D(size, size);
        f.complexInverse(dat2, false);
        return new FFTArray(size, dat2);
    }

    public FFTArray multiply(SquareArray covArray) {
        float[] cov = covArray.data;
        float[] fft2 = new float[2*size*size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int x1, y1;
                x1 = x - size/2; 
                y1 = y - size/2;
                if (x1 < 0) x1 += size;
                if (y1 < 0) y1 += size;
                fft2[y1*size*2 + x1*2] =  
                    data[y1*size*2 + x1*2] * cov[y*size+x];  // Real
                fft2[y1*size*2 + x1*2 + 1] = 
                    data[y1*size*2 + x1*2 + 1]*cov[y*size+x];  // Imaginary
            }
        }
        FFTArray fftconv = new FFTArray(size, fft2);
        return fftconv;
    }    

    public SquareArray extractReals() {
        SquareArray sa = new SquareArray(size);
        for (int i=0; i < size; i++) {
            for (int j=0; j<size; j++) {
                sa.set(i, j, getReal(i,j));
            }
        }
        return sa;
    }
}


class PixArray {
    int height, width;
    int[] data;
    
    public PixArray(int i, int j) {
        width=i;
        height=j;
        data = new int[width*height];
    }

    public void set(int i, int j, int val) {
        data[j*width+i] = val;
    }

    public int get(int i, int j) {
        return data[j*width+i];
    }

    public Image toImage(Applet applet, int size) {
        return applet.createImage(new MemoryImageSource(width, height, data, 0, size));
    }

    public float meanEdge() {
        float mean = 0;
        int count = 0;
        for (int h = 0; h < height; h++) {
            int inc = width-1;
            if (h == 0 || h == (height-1)) inc = 1;
            for (int w = 0; w < width; w += inc) {
                mean += (float)(get(w, h) & 0x000000ff);  
                // Assume already greyscale -
                // i.e. red = green = blue
                count++;                                       
            }
        }
        mean /= (float)count;
        return mean;
    }

    public SquareArray toDat() {
        // System.out.println("Converting (PixArray::toDat): "+imh+","+imw+","+size);
        int size = vriUtils.getImsize(height, width);
        // find mean value along the edge (to make the padding realistic)
        float mean = meanEdge();
        SquareArray dat = new SquareArray(size);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                // Re-centre image.
                int i = (size/2 + w - width/2);
                int j = (size/2 + h - height/2);
                dat.set(i, j, (get(w, h) & 0x000000ff)-mean);
            }
        }
        return dat;
    }

    public void makeGrey() {
        // System.out.println("PixArray::greyPix: "+height+"x"+width);
        for (int i=0; i<height*width; i++) {
            int red   = (data[i] & 0x00ff0000) >> 16;
            int green = (data[i] & 0x0000ff00 >> 8);
            int blue  = (data[i] & 0x000000ff);
            int grey  = (red+green+blue)/3;
            data[i] = (data[i] & 0xff000000) | (grey << 16) | (grey << 8) | grey;
        }
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


    static PixArray fftToPix(FFTArray fft, PlotTypes type) {
        float value;        // Quantity that is plotted to a pixel
        // determine the scale of the data (use a forced -180:180 range for phase
        int size = fft.size;
        float[] minmaxv = vriUtils.minmax(fft.data);
        float min = minmaxv[0];
        float max = minmaxv[1];
        if (type == PlotTypes.PHASE) {
            min = (float)(-Math.PI / 2.0);
            max = (float)( Math.PI / 2.0);
        }

        PixArray pix = new PixArray(size, size);
        for (int y = 0; y < size; y++) {
            for  (int x = 0; x < size; x++) {
                int x1, y1;
                // shift origin to center of image
                x1 = (x + size/2) % size;
                y1 = (y + size/2) % size;

                // Depending on the "type" of display selected, we extract the 
                // relevant components of the fourier transform.
                switch (type) {
                case REAL: 
                    value = fft.getReal(x1, y1);
                    break;
                case IMAG:
                    value = fft.getImag(x1, y1);
                    break;
                case PHASE:
                    value = fft.getPhase(x1, y1); 
                    break;
                default:  // Used for Ampl., Colour and others that aren't handled
                    value = fft.getAmpl(x1, y1);
                }

                int grey = (int) (255.0 * (value - min) / (max - min) );
                pix.set(x, y, 0xff000000 | (grey << 16) | (grey << 8) | grey);

                if (type.equals("Colour")) {
                    double h = fft.getPhase(x1, y1) / Math.PI / 2.0;
                    if (h < 0.0) h += 1.0;
                    pix.set(x, y, Color.HSBtoRGB( (float) h, (float) 1.0,
                                                  (float) grey/(float)255.0));
                }  // End if(colour)
            }  // End for(x)
        }  // End for(y)
        return pix;
    }  
}

class vriGreyDisp extends vriDisplay {
    Applet applet;
    //  boolean replot = false;
    URL imgURL;
    Image img;
    static int imsize;     // Size of the "squared" image
    String message = null; // Message to print on the Display
    PlotTypes type = PlotTypes.AMPL;    // Used to select real/imag/amp/phase display
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

    public PixArray imgToPix(Image img) 
        throws EmptyImageException
    {
        if (img == null) {
            throw new EmptyImageException();
        } 
        int imh = img.getHeight(this);
        int imw = img.getWidth(this);
        // System.out.println("Got "+this+"image: size = "+imw+"x"+imh);
        PixArray pix = new PixArray(imw, imh);
        try {
            PixelGrabber pg = new PixelGrabber(img, 0, 0, imw, imh, pix.data, 0, imw);
            pg.grabPixels(100);
        } catch (InterruptedException e){
            System.err.println("imgToPix failed");
        }
        return pix;
    }
}

