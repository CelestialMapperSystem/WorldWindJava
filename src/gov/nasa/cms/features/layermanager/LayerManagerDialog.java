/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.layermanager;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
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

    private final WorldWindow wwd;
    private JDialog dialog;
    private LayerPanel layerPanel;

    public LayerManagerDialog(WorldWindow wwd, CelestialMapper celestialMapper)
    {
        layerPanel = new LayerPanel(wwd, celestialMapper);
        this.wwd = wwd;
        // Create the dialog
        dialog = new JDialog(celestialMapper);
        this.dialog.setPreferredSize(new Dimension(340, 460));
        this.dialog.getContentPane().setLayout(new BorderLayout());
        this.dialog.setResizable(true);
        this.dialog.setModal(false);
        this.dialog.setTitle("Layer Manager");

        // Add the layer panel to the dialog and set the location
        dialog.getContentPane().add(layerPanel, BorderLayout.CENTER);
        Rectangle bounds = celestialMapper.getBounds();
        dialog.setLocation(bounds.x + 60, bounds.y + 300);

//        dialog.setResizable(false); // Set false to resizable until we can expand panels with dialog
        // Set dialog to be visible always
        dialog.setVisible(true);
        dialog.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent)
            {
                celestialMapper.getLayerManager().setVisible(false);
                celestialMapper.setLayerManagerOpen(false);
            }
        });

        dialog.pack();
    }

    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }

    public LayerPanel getLayerPanel()
    {
        return layerPanel;
    }

    public void update()
    {
        layerPanel.update(wwd);
    }

    public boolean isVisible()
    {
        return this.isVisible();
    }
}
