/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.core;

import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.JDialog;

/**
 *
 * @author kjdickin
 */
public class LayerManagerDialog
{
    private JDialog dialog;
    private LayerManagerPanel layerPanel;
    
    public LayerManagerDialog(WorldWindow wwd, Component component)
    {
        layerPanel = new LayerManagerPanel(wwd);
        
        // Create the dialog
        dialog = new JDialog((Frame) component);
        this.dialog.setPreferredSize(new Dimension(370, 480));
        this.dialog.getContentPane().setLayout(new BorderLayout());
        this.dialog.setResizable(true);
        this.dialog.setModal(false);
        this.dialog.setTitle("Layer Manager");
        
        // Add the layer panel to the dialog and set the location
        dialog.getContentPane().add(layerPanel, BorderLayout.CENTER);
        Rectangle bounds = component.getBounds();
        dialog.setLocation(bounds.x + 840, bounds.y + 60); 
        
        dialog.setResizable(false); // Set false to resizable until we can expand panels with dialog
        
        // Set dialog to be visible always
        dialog.setVisible(true);
        
        dialog.pack();
    }
    
}
