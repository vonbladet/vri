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
    FFTArray dat;

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
        setFullScale(ai.scale);
        String str = ai.filename;
        loadImage(str);
        try {
            int pix[] = imgToPix(img);
            int imh = img.getHeight(this);
            int imw = img.getWidth(this);
            imsize = vriUtils.getImsize(imh, imw);
            // System.err.println("Imsize: "+imsize);
            vriUtils.greyPix(pix, imh, imw);
            dat = new FFTArray(imsize, 
                               vriUtils.pixToDat(pix, imh, imw, imsize));
            vriUtils.scaleDat(dat.data);
            pix = vriUtils.datToPix(dat.data, dat.imsize);
            pixToImg(pix, dat.imsize);
            message = null;
            repaint();
            propChanges.firePropertyChange("dat", null, dat);
            propChanges.firePropertyChange("fftsize", 0, dat.imsize);
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
        FFTArray dat = fft.invfft();
        int pix[] = vriUtils.datToPix(dat.data, fft.imsize);
        pixToImg(pix, fft.imsize);
        message = null;
        repaint();
    }
}
