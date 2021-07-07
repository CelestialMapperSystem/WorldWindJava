/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import javax.swing.JDialog;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Displays the dialog for Moon Shading feature, which allows users
 * to change the elevation and azimuth manually, or calculate the
 * elevation & azimuth based on the date and time they enter.
 * @author kjdickin
 */
public class MoonShadingDialog 
{
     private JDialog dialog;
     private MoonShadingPanel moonShadingPanel;
     private WorldWindow wwd;
     private TimeFrame timeFrame;
     

     public MoonShadingDialog(WorldWindow wwdObject, Component component)
     {
        moonShadingPanel = new MoonShadingPanel(wwdObject);
        
        timeFrame = new TimeFrame(wwdObject, component, moonShadingPanel);
        timeFrame.setVisible(true);
        
        moonShadingPanel.getTimeFrameCheckBox().addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent event)
            {
                if (moonShadingPanel.getTimeFrameCheckBox().isSelected())
                {
                    timeFrame.setVisible(true);
                }
                else
                {
                    timeFrame.setVisible(false);
                }
            }
        });
        
        this.wwd = wwdObject; 
          
        dialog = new JDialog((Frame) component);     
        Rectangle bounds = component.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Moon Shading");
        
        // Set the location and resizable to false
        dialog.setLocation(bounds.x + 1600, bounds.y + 550);
        dialog.setResizable(false);
        dialog.getContentPane().add(moonShadingPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setVisible(true);  
          
    }
     
     public void setVisible(boolean visible)
     {
         dialog.setVisible(visible);
         timeFrame.setVisible(visible);
     }
     
    public void resetDialog()
    {
        moonShadingPanel.resetMoonShadingProperties();
        dialog.setVisible(false);   
        timeFrame.setVisible(false);
    }
    
    public JDialog getDialog()
    {
        return this.dialog;
    }
    
}