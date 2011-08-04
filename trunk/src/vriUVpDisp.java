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

class vriUVpDisp extends vriGreyDisp 
    implements PropertyChangeListener
{
    FFTArray fft;
    SquareArray uvcov;	 

    public vriUVpDisp(Applet app) {
        super(app);
        setUnit("lambda");
        message = new String("No current transform");
        // hasAxes = true;
        setHasAxes(true);
        axesLabels = new String[] {"U", "V"};
        propChanges = new PropertyChangeSupport(this);
    }

    void setFullScale(double s){
        fullScale = s;
        repaint();
    }

    public void propertyChange(PropertyChangeEvent e) {
        // uvcov and fft are for UVpConvDisp
        // dat is from the source image, for UVpDisp
        String pname = e.getPropertyName();
        if (pname=="uvcov") {
            System.err.println("vriUVpDisp: "+
                               "UV coverage changed - updating convolution");
            uvcov =  (SquareArray) e.getNewValue(); 
            if (fft!=null) {
                applyUVc(uvcov, fft);
            } else {
                System.err.println("vriUVpDisp: Null fft");
            }
        } else if (pname=="fft") {
            System.err.println("vriUVp(Conv)Disp: UVp fft changed");
            fft = (FFTArray) e.getNewValue();
            if (uvcov!=null) {
                applyUVc(uvcov, fft);
            } else {
                System.err.println("vriUVpDisp: Null uvcov");
            }
        } else if (pname=="dat") {
            System.err.println("vriUVpDisp: img disp dat changed");
            FFTArray dat = (FFTArray) e.getNewValue();
            if (dat != null) {
                fft(dat);
            } else {
                System.err.println("vriUVpDisp: got null dat!");
            }
        } else {
            System.err.println("vriUVpDisp got propertyChange for "+
                               e.getPropertyName()+
                               " ; ignoring it.");
        }
    }

    public void fft(FFTArray dat) {
        if (dat.data == null) {
            System.err.println("vriUVpDisp: dat not set");
            return;
        }
        System.err.println("vriUVpDisp: Fourier transforming...");
        message = new String("Fourier transforming...");
        repaint();
        fft = dat.fft();
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
        message = new String("Applying UV coverage...");
        repaint();
        System.err.println("vriUVpDisp: Applying UV coverage...");

        FFTArray fftconv = fft0.multiply(cov);
        fftToImg(fftconv);
        propChanges.firePropertyChange("fftconv", null, fftconv);
    }
}
