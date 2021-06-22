/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import java.awt.*;
import javax.swing.JDialog;

/**
 *
 * @author kjdickin
 */
public class PointPlacemarkDialog
{

    private JDialog dialog;

    public PointPlacemarkDialog(WorldWindow wwdObject, CelestialMapper celestialMapper)
    {
        CMSPointPlacemarkPanel measurePanel = new CMSPointPlacemarkPanel(wwdObject, celestialMapper);

        //dialog.setSize(new Dimension(200, 400));
        // Create the dialog from a Frame and set the bounds
        dialog = new JDialog(celestialMapper);
        Rectangle bounds = celestialMapper.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Point Placemarks");
        // Set the location and resizable to false
        dialog.setLocation(bounds.x + 50, bounds.y + 200);
        dialog.setResizable(true);
        // Add the tabbedPane to the dialog
        dialog.getContentPane().add(measurePanel, BorderLayout.CENTER);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                celestialMapper.getPointPlacemarkDialog().setVisible(false);
                celestialMapper.setPointPlacemarkDialogOpen(false);

            }
        });

        dialog.pack();
    }
    
    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }

}
