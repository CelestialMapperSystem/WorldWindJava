/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwindx.examples.sunlight;

import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Frame;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;


/**
 *
 * @author kjdickin
 */
public class MoonShadingDialog 
{
     private JDialog dialog;
     private JPanel moonShadingPanel;
     private WorldWindow wwd;
     
     
     // Pass in the WorldWindow and Component (AppFrame)
     // To initialize in CMS look at MeasureDialog
     // In CelestialMapper->  
        //MoonShadingDialog moonShadingDialog = new MeasureDialog(getWwd(), this); -> passes in CMS WorldWindow and AppFrame (CMS extends AppFrame, so use this)
     public MoonShadingDialog(WorldWindow wwdObject, Component component)
     {
         
        moonShadingPanel = new MoonShadingPanel(wwdObject);
        this.wwd = wwdObject; // Make sure WorldWindow = passed in WorldWindow
          
        dialog = new JDialog((Frame) component);     
        Rectangle bounds = component.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Moon Shading");
        // Set the location and resizable to false
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(false);
        dialog.getContentPane().add(moonShadingPanel, BorderLayout.CENTER);
        dialog.pack();
        ShowTimeJSpinner calendar = new ShowTimeJSpinner();
          
     }
      public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
    
}
