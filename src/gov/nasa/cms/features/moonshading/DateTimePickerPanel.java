/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

/**
 *
 * @author kjdickin
 */
public class DateTimePickerPanel extends JPanel
{
     private WorldWindow wwd;
     private JSpinner startDateTime;
     private JSpinner endDateTime;
     private JSlider animationSpeed;
     private JButton applyChangesButton;
     private Box controlPanel;
     
     public DateTimePickerPanel(WorldWindow wwdObject)
     {
          this.wwd = wwdObject;
          
     }
     
     private void makePanel()
     {
          setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
     }
}
