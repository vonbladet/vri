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
import javax.swing.*;
import java.beans.*;

class vriDisplay extends JComponent
	 implements FocusListener
					
{
	 public double displaySize;    // Width of display
	 public double displayScale;   // Scale of display
	 public double defaultScale;   // Scale of display
	 public Point displayCentre;   // Centre point of display
	 public Point defaultCentre;   // Centre point of display

	 boolean hasFocus;
	 
	 protected PropertyChangeSupport propChanges;

	 public vriDisplay() {
		  int w = 200;
		  int h = 200;
		  displaySize = (double) w - 1;
		  displayScale = 1.0;
		  defaultScale = 1.0;
		  displayCentre = new Point(w/2, h/2);
		  defaultCentre = new Point(w/2, h/2);
		  setSize(w, h);
		  hasFocus = false;
		  //    System.out.println(this);
		  propChanges = new PropertyChangeSupport(this);
		  addMouseListener(new Mousey());
		  addKeyListener(new Keyboardy());
		  setFocusable(true);
		  addFocusListener(this);
	 }

	 public Dimension getPreferredSize() {
		  return new Dimension(200, 200);
	 }

	 public void displayShift(int x, int y) {
		  displayCentre.translate(x,y);
		  repaint();
	 }

	 public void zoomIn() {
		  displayCentre.x += (displayCentre.x - defaultCentre.x) * .41;
		  displayCentre.y += (displayCentre.y - defaultCentre.y) * .41;
		  displayScale /= 1.41;
		  repaint();
	 }

	 public void zoomOut() {
		  displayCentre.x -= (displayCentre.x - defaultCentre.x) * (1.0 - 1.0/1.41);
		  displayCentre.y -= (displayCentre.y - defaultCentre.y) * (1.0 - 1.0/1.41);
		  displayScale *= 1.41;
		  repaint();
	 }

	 public void zoomReset() {
		  displayScale = defaultScale;
		  displayCentre.x = defaultCentre.x;
		  displayCentre.y = defaultCentre.y;
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

				int shift;
				if ((mods & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
					 shift = (int)(displaySize/5.0);
				} else {
					 shift = (int)(displaySize/20.0);
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
