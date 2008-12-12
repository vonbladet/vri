/*
 * vriDisplay.java
 *
 * Used in the Virtual Radio Interferometer
 *
 * 05/Jan/1998 Nuria McKay - Extracted this class from vri.java and placed here
 *
 */

package nl.jive.vri;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*; // AffineTransform
import javax.swing.*;
import java.beans.*;

class vriDisplay extends JComponent
	 implements FocusListener
					
{
	 AffineTransform defaultTransform, aff;
	 boolean hasFocus;
	 
	 protected PropertyChangeSupport propChanges;

	 public vriDisplay() {
		  aff = new AffineTransform();
		  defaultTransform = (AffineTransform) aff.clone();
		  hasFocus = false;
		  propChanges = new PropertyChangeSupport(this);
		  addMouseListener(new Mousey());
		  addKeyListener(new Keyboardy());
		  setFocusable(true);
		  addFocusListener(this);
	 }

	 public Dimension getPreferredSize() {
		  int width, height;
		  width = 200;
		  height = 200;
		  return new Dimension(width, height);
	 }

	 double getDisplayScale() {
		  return aff.getScaleX();
	 }

	 public void displayShift(double x, double y) {
		  aff.translate(x, y);
		  repaint();
	 }

	 public void zoomIn() {	
		  // scale(1.41, 1.41);
		  double s = 1.41;
		  aff.scale(s, s);
		  repaint();
	 }

	 public void zoomOut() {
		  aff.scale(1/1.41, 1/1.41);
		  repaint();
	 }

	 public void zoomReset() {
		  aff = defaultTransform;
		  repaint();
	 }

	 public void plotFocus(Graphics g) {
		  Rectangle r = getBounds();
		  g.setColor(hasFocus ? Color.cyan : Color.black);
		  g.drawRect(0, 0, r.width-1, r.height-1);
	 }

	 public void focusGained(FocusEvent e) {
		  Graphics g = getGraphics();
		  hasFocus = true;
		  plotFocus(g);
	 }	 

	 double roundPower(double l) {
		  double power = Math.log10(l);
		  double res = Math.pow(10.0, Math.floor(power));
		  return res;
	 }

	 double roundUpPower(double l) {
		  double power = Math.log10(l);
		  double res = Math.pow(10.0, Math.ceil(power));
		  return res;
	 }

	 static String roundUnit(double l, String unit) {
		  // System.err.println("roundUnit: "+l);
		  int exp = (int)Math.floor(Math.log10(l));
		  String prefix = "";
		  double value = l;
		  if (exp < 0) {
				prefix = "m";
				value = l*1000.0;
		  } else if (0 <= exp && exp < 3) {
				prefix = "";
				value = l;
		  } else if (3 <= exp) {
				prefix = "k";
				value = l/1000.0;
		  }
		  return Double.toString(value)+String.format(" %s%s", prefix, unit);
	 }

	 public void focusLost(FocusEvent e) {
		  Graphics g = getGraphics();
		  hasFocus = false;
		  plotFocus(g);
	 }

	 class Mousey extends MouseAdapter {
		  public void mouseEntered(MouseEvent e) {
				boolean b = requestFocusInWindow();
		  }
	 }

	 class Keyboardy extends KeyAdapter {
		  public void keyPressed(KeyEvent e) {

				int mods = e.getModifiersEx();

				double shift;
				int width = getWidth();
				double w  = (double)width;
				if ((mods & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
					 shift = (w/5.0);
				} else {
					 shift = (w/20.0);
				}

				int key = e.getKeyCode();

				switch(key) {
				case KeyEvent.VK_UP: 
				case KeyEvent.VK_KP_UP:
					 displayShift(0, shift); 
					 break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
					 displayShift(0, -shift); 
					 break;
				case KeyEvent.VK_LEFT: 
				case KeyEvent.VK_KP_LEFT:
					 displayShift(shift, 0); 
					 break;
				case KeyEvent.VK_RIGHT: 
				case KeyEvent.VK_KP_RIGHT: 
					 displayShift(-shift, 0); 
					 break;
				case KeyEvent.VK_PAGE_DOWN: 
					 zoomIn();
					 break;
				case KeyEvent.VK_PAGE_UP: 
					 zoomOut();
					 break;
				case KeyEvent.VK_HOME: 
					 zoomReset();
					 break;
				default: break;
				}
		  }
	 }
	 

	 public void addPropertyChangeListener(PropertyChangeListener l)
	 {
		  propChanges.addPropertyChangeListener(l);
	 }
	 public void removePropertyChangeListener(PropertyChangeListener l)
	 {
		  propChanges.removePropertyChangeListener(l);
	 }
}
