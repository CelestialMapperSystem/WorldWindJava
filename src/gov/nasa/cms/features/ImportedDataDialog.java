/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwindx.applications.worldwindow.features.NetworkActivitySignal;
import java.awt.Component;
import java.nio.file.FileStore;
import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.JDialog;

/**
 *
 * @author kjdickin
 */
public class ImportedDataDialog implements NetworkActivitySignal.NetworkUser
{
    protected FileStore fileStore;
    protected ImportedDataPanel dataConfigPanel;
    protected Thread importThread;
    private JDialog dialog;

    public ImportedDataDialog(WorldWindow wwd, Component component)
    {
        dialog = new JDialog((Frame) component);
        Rectangle bounds = component.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Import Imagery & Elevations");
        // Set the location and resizable to false
        dialog.setPreferredSize(new Dimension(400, 400));
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(false);
        // Add the tabbedPane to the dialog
        
        ImportedDataPanel importedDataPanel = new ImportedDataPanel(wwd);
        dialog.getContentPane().add(importedDataPanel, BorderLayout.CENTER);
        dialog.pack();
    }
    
    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
    
    @Override
    public boolean hasNetworkActivity()
    {
        return this.importThread != null && this.importThread.isAlive();
    }
}
