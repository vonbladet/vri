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
import java.beans.*;

class vriDisplay extends Canvas {
	 public double displaySize;    // Width of display
	 public double displayScale;   // Scale of display
	 public double defaultScale;   // Scale of display
	 public Point displayCentre;   // Centre point of display
	 public Point defaultCentre;   // Centre point of display
	 boolean focus = false;        // True if "display" is in focus
	 
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
		  //    System.out.println(this);
		  propChanges = new PropertyChangeSupport(this);

	 }

	 public void plotFocus(Graphics g) {
		  Rectangle r = bounds();

		  if (focus) {
				g.setColor(Color.cyan);
				g.drawRect(0, 0, r.width-1, r.height-1);
		  } else {
				g.setColor(Color.black);
				g.drawRect(0, 0, r.width-1, r.height-1);
		  }
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

	 public boolean gotFocus(Event e, Object arg) {
		  focus = true;
		  Graphics g = getGraphics();
		  plotFocus(g);
		  return true;
	 }

	 public boolean lostFocus(Event e, Object arg) {
		  focus = false;
		  Graphics g = getGraphics();
		  plotFocus(g);
		  return true;
	 }

	 public boolean mouseEnter(Event e, int x, int y) {
		  requestFocus();
		  return true;
	 }

	 public boolean mouseExit(Event e, int x, int y) {
		  requestFocus();
		  return true;
	 }

	 public boolean keyDown(Event e, int key) {

		  int shift = (int)( displaySize/20.0 );
		  if(e.shiftDown()) shift = (int)( displaySize/5.0 );

		  switch(key) {
		  case Event.UP: displayShift(0, shift); break;
		  case Event.DOWN: displayShift(0, -shift); break;
		  case Event.LEFT: displayShift(shift, 0); break;
		  case Event.RIGHT: displayShift(-shift, 0); break;
		  case Event.PGDN: zoomIn();
				break;
		  case Event.PGUP: zoomOut();
				break;
		  case Event.HOME: zoomReset();
				break;
		  default: break;
		  }
		  return true;
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
