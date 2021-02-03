/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.core;

import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
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
        dialog = new JDialog((Frame) component);   
        this.dialog.setPreferredSize(new Dimension(350, 700));
        this.dialog.getContentPane().setLayout(new BorderLayout());
        this.dialog.setResizable(true);
        this.dialog.setModal(false);
        this.dialog.setTitle("Layer Manager");
        dialog.getContentPane().add(layerPanel, BorderLayout.CENTER);
        dialog.pack();
    }
    
}
