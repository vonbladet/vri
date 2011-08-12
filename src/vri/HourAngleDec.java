package nl.jive.vri;

import java.lang.Math;
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;

class HourAngleDec extends JPanel {
	 JLabel ha1_label;
	 JLabel ha2_label;
	 JLabel dec_label;
	 JSlider ha1;
	 JSlider ha2;
	 JSlider dec;
	 double ha_limit1;     // The Hour angle limit imposed by the Ant.El.limt
	 double ha_limit2;     // and the choice of declination.	 
	 double ha1_val; // needed to cache old value for firePropertyChanges
	 double ha2_val;
	 int dec_val;
	 private PropertyChangeSupport propChanges;

	 vriObservatory obs;
	 
	 float boundValue(float val, float min, float max) {
		  float res;
		  if (val>max) 
				res = max;
		  else if (val<min)
				res = min;
		  else
				res = val;
		  return val;
	 }
	 int boundValue(int val, int min, int max) {
		  int res;
		  if (val>max) 
				res = max;
		  else if (val<min)
				res = min;
		  else
				res = val;
		  return val;
	 }

	 double boundValue(double val, double min, double max) {
		  double res;
		  if (val>max) 
				res = max;
		  else if (val<min)
				res = min;
		  else
				res = val;
		  return val;
	 }


	 double intToHours(int h) {
		  return Math.toRadians(1.5 * (double)h);
	 }

	 int hoursToInt(double ha) {
		  return (int) Math.round(Math.toDegrees(ha/1.5));    
	 }

	 public HourAngleDec() {
		  propChanges = new PropertyChangeSupport(this);
		  setLayout(new GridLayout(3,0));
		  add(ha1_label = new JLabel("Hour Angle (-6.0h):"));
		  add(ha1 = new JSlider(JSlider.HORIZONTAL, -120, 120, -60));
		  add(ha2_label = new JLabel("Hour Angle (+6.0h):"));
		  add(ha2 = new JSlider(JSlider.HORIZONTAL, -120, 120, 60));
		  add(dec_label = new JLabel("Declination (90°):"));
		  add(dec = new JSlider(JSlider.HORIZONTAL, -90, 90, 90));

		  dec.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  int old_dec_val = dec_val;
						  dec_val = dec.getValue();
						  dec_label.setText(String.format("Declination (%d°):", dec_val));
						  double dec = Math.toRadians((double)dec_val);
						  propChanges.firePropertyChange("dec", old_dec_val, dec_val);
						  limitHArange();
					 }
				});
		  
		  ha1.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  double old_ha1_val = ha1_val;
						  int h1 = ha1.getValue();
						  ha1_val = boundValue(intToHours(h1), 
													  ha_limit1, ha_limit2);
						  setHAlabel(ha1_label, hoursToInt(ha1_val));
						  propChanges.firePropertyChange("ha1", old_ha1_val, ha1_val);
						  if (ha1_val > ha2_val) {
								double old_ha2_val = ha2_val;
								ha2_val = ha1_val;
								int h2 = hoursToInt(ha2_val);
								ha2.setValue(h2);
								setHAlabel(ha2_label, h2);
								propChanges.firePropertyChange("ha2", old_ha2_val, ha2_val);
						  }
					 }
				});
		  
		  ha2.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  double old_ha2_val = ha2_val;
						  int h2 = ha2.getValue();
						  ha2_val = boundValue(intToHours(h2), ha_limit1, ha_limit2);
						  setHAlabel(ha2_label, h2);
						  propChanges.firePropertyChange("ha2", old_ha2_val, ha2_val);
						  if (ha2_val < ha1_val) {
								double old_ha1_val = ha1_val;
								ha1_val = ha2_val;
								int h1 = hoursToInt(ha1_val);
								ha1.setValue(h1);
								setHAlabel(ha1_label, h1);
								propChanges.firePropertyChange("ha1", old_ha1_val, ha1_val);
						  }
					 }
				});

		  setHAlabel(ha1_label, -60);
		  setHAlabel(ha2_label,  60);
	 }

	 void limitHArange() {
		  double ha = (Math.sin(obs.ant_el_limit) - 
							Math.sin(obs.latitude) * Math.sin(Math.toRadians(dec_val))) /
				(Math.cos(obs.latitude) * Math.cos(Math.toRadians(dec_val)) );
		  System.err.println(String.format("Obs properties: ant_el_limit %.2f, latitude %.2f", 
															obs.ant_el_limit, obs.latitude));
		  System.err.println(String.format("Dec val %d", dec_val));
		  System.err.println(String.format("ha %.2f", ha));
				
		  if (ha >= 1.0) {
				ha_limit1 = ha_limit2 = 0.0;
		  } else if (ha <= -1.0) {
				ha_limit1 = -12.0;
				ha_limit2 = 12.0;
		  } else {
				ha = Math.abs(Math.acos(ha));
				ha_limit1 = -ha;
				ha_limit2 = ha;
		  }
		  if (ha1_val < ha_limit1) {
				double old_ha1_val = ha1_val;
				ha1_val = quantizeHA(ha_limit1);
				int h1 = hoursToInt(ha1_val);
				setHAlabel(ha1_label, h1);
				ha1.setValue(h1);
				propChanges.firePropertyChange("ha1", old_ha1_val, ha1_val);
		  }
		  if (ha2_val > ha_limit2) {
				double old_ha2_val = ha2_val;
				ha2_val = quantizeHA(ha_limit2);
				int h2 = hoursToInt(ha2_val);
				setHAlabel(ha2_label, h2);
				ha2.setValue(h2);
				propChanges.firePropertyChange("ha2", old_ha2_val, ha2_val);
		  }
	 }

	 public void setDecRange() {
		  int el = (int) Math.round(Math.toDegrees(obs.ant_el_limit));
		  int lat = (int) Math.round(Math.toDegrees(obs.latitude));

		  int dec_max = Math.min( 90 + lat - el,  90);    
		  int dec_min = Math.max(-90 + lat + el, -90);
		  int old_dec_val = dec_val;
		  int dec_val = boundValue(dec.getValue(), dec_min, dec_max);

		  dec.setValue(dec_val);
		  dec.setMinimum(dec_min);
		  dec.setMaximum(dec_max);
		  propChanges.firePropertyChange("dec", old_dec_val, dec_val);
	 };

	 double quantizeHA(double ha) {
		  ha *= 12.0 / Math.PI;             // Convert to hours
		  ha = Math.rint(ha*10.0) / 10.0;   // Round to 1 decimal place
		  ha *= Math.PI / 12.0;             // Convert back to radians
		  return ha;
	 }

	 void setHAlabel(JLabel label, int h) {
		  String s = String.format("Hour Angle (%+.1f)", h/10.0);
		  label.setText(s);
	 }

	 void setObservatory(vriObservatory o) {
		  obs = o;
		  limitHArange();
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


class simpleHourAngleDec extends JPanel {
	 JLabel ha_label;
	 JLabel dec_label;
	 JSlider ha, dec;
	 double ha_val; // needed to cache old value for firePropertyChanges
	 int dec_val;
	 String ha_format = "Hour Angle (%+.1f)";
	 String dec_format = "Declination (%d°):";

	 private PropertyChangeSupport propChanges;
	 vriObservatory obs;
	 
	 double intToHours(int h) {
		  return Math.toRadians(1.5 * (double)h);
	 }

	 int hoursToInt(double hour) {
		  return (int) Math.round(Math.toDegrees(hour/1.5));    
	 }

	 public simpleHourAngleDec() {
		  propChanges = new PropertyChangeSupport(this);
		  setLayout(new GridLayout(2,0));
		  add(ha_label = new JLabel("Hour Angle (6.0):"));
		  add(ha = new JSlider(JSlider.HORIZONTAL, 0, 240, 60));
		  ha_val = intToHours(60);
		  add(dec_label = new JLabel("Declination (70°):"));
		  add(dec = new JSlider(JSlider.HORIZONTAL, -90, 90, 70));
		  dec_val = 70;
		  

		  dec.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  int old_dec_val = dec_val;
						  dec_val = dec.getValue();
						  dec_label.setText(String.format(dec_format, dec_val));
						  double dec = Math.toRadians((double)dec_val);
						  propChanges.firePropertyChange("dec", old_dec_val, dec_val);
					 }
				});
		  
		  ha.addChangeListener(new ChangeListener() {
					 public void stateChanged(ChangeEvent e) {
						  double old_ha_val = ha_val;
						  int h = ha.getValue();
						  ha_val = intToHours(h); 
						  ha_label.setText(String.format(ha_format, h/10.0));
						  propChanges.firePropertyChange("ha1", null, 0.0);
						  propChanges.firePropertyChange("ha2", old_ha_val, ha_val);
					 }
				});
	 }

	 double getHa1() {
		  return 0.0;
	 }

	 double getHa2() {
		  return ha_val;
	 }

	 double getDec() {
		  return dec_val;
	 }

	 void setDecRange() {
	 }

	 void setObservatory(vriObservatory o) {
		  obs = o;
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


