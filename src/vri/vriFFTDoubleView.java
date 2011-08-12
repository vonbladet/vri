package nl.jive.vri;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.color.*;
import javax.swing.*;
import java.util.*;
import java.net.*;

class ImageViewer extends JComponent
{
	 public BufferedImage bi;
	 Image img;
	 Graphics2D gbi;

	 public void addImage(Image i) {
		  img = i;
	 }

 	 public void paint(Graphics g)
 	 {
		  Graphics2D gbi;
		  System.err.println("In paint");
		  System.err.println(img);
// 		  bi = (BufferedImage) this.createImage(img.getWidth(this),
// 															 img.getHeight(this));
// 		  System.err.println(img);
// 		  System.err.println(bi);
// 		  gbi = bi.createGraphics();
		  		  
// 		  gbi.drawImage(img, 0, 0, null);
// 		  int w = bi.getWidth();
// 		  int h = bi.getHeight();
//  		  Graphics2D g2 = (Graphics2D) g;
// 		  g2.drawImage(bi, null, 0, 0);

// 		  int[] rgbs = bi.getRGB(0, 0, w, h, (int[])null, 0, 0);
// 		  System.out.println(rgbs.length);

		  g.drawImage(img, 0, 0, this);

 	 }
}		  

class vriFFTDoubleView 
{

	 public static void main(String[] args) {
		  JFrame jf = new JFrame("FFT Double View");
		  jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		  jf.setSize(300, 300);	
 		  jf.setVisible(true);
		  JLabel j = new JLabel();
		  jf.add(j);
		  MediaTracker tracker = new MediaTracker(j);
		  try {
				String fn = "wide_gauss.gif";
				Image img = Toolkit.getDefaultToolkit().getImage(fn);
				tracker.addImage(img, 0);
				tracker.waitForID(0);
				int w = img.getHeight(null);
				int h = img.getWidth(null);
				System.out.println(String.format("Image size: %dx%d", w, h));
				BufferedImage bi = (BufferedImage) j.createImage(w, h);
				System.err.println(bi);
				System.out.println(bi.getColorModel());
				Graphics2D g = bi.createGraphics();
				g.drawImage(img, 0, 0, null);
				// int[] rgbs = bi.getRGB(0, 0, w, h, (int[])null, 0,w);
				// System.out.println(rgbs);

		  } catch (InterruptedException e) {
				System.err.println("Image wouldn't load");
		  }
	 }
}


