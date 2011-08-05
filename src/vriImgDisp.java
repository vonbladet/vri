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

class vriImgDisp extends vriGreyDisp 
    implements PropertyChangeListener 
{
    SquareArray dat;

    public vriImgDisp(Applet app) {
        super(app);
        setUnit("as");
        propChanges = new PropertyChangeSupport(this);
        message = new String("No current image");
    }

    public void propertyChange(PropertyChangeEvent e) {
        String pname = e.getPropertyName();
        if (pname=="fftconv") {
            System.err.println("vriImgDisp: UVpConvDisp fft changed");
            FFTArray fft = (FFTArray) e.getNewValue();
            invfft(fft);
        } else {
            System.err.println("vriImgDisp: unknown property "+
                               pname);
        }
    }

    public void loadAstroImage(AstroImage ai) {
        PixArray pix;
        setFullScale(ai.scale);
        String str = ai.filename;
        loadImage(str);
        try {
            PixArray pix2;
            PixArray pix1 = imgToPix(img);
            pix1.makeGrey();
            dat = pix1.toDat();
            dat.scale();
            pix2 = dat.toPix();
            pix2.toImage(applet, dat.size);
            message = null;
            repaint();
            propChanges.firePropertyChange("dat", null, dat);
            propChanges.firePropertyChange("fftsize", 0, imsize);
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
        System.err.println("vriImgDisp: Inverse fourier transforming...");
        message = new String("Inverse fourier transforming...");
        repaint();
        // Original image was real, then FFT image multiplied by a real
        // => dat is actually FFT of a real thing.
        FFTArray cdat = fft.invfft(); 
        SquareArray dat = cdat.extractReals();
        PixArray pix = dat.toPix();
        pix.toImage(applet, fft.size);
        message = null;
        repaint();
    }
}
