/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;

import javax.swing.*;
import java.awt.*;

/**
 * @author : gknorman
 * @created : 3/31/2021, Wednesday
 **/
public class SearchPlacenamesDialog{

    private JDialog dialog;
    public SearchPlacenamesDialog(WorldWindow wwd, CelestialMapper celestialMapper)
    {
        JPanel searchPanel = new PlacenamesSearchPanel(wwd, celestialMapper);



        // Create the dialog from a Frame and set the bounds
        dialog = new JDialog(celestialMapper);
        Rectangle bounds = celestialMapper.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Placename Search");
//        dialog.setSize(new Dimension(200, 400));

        // Set the location and resizable
        dialog.setLocation(bounds.x + 50, bounds.y + 200);
        dialog.setResizable(true);

        // Add JPanel to JDialog
        dialog.getContentPane().add(searchPanel, BorderLayout.CENTER);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                celestialMapper.getSearchPlacenamesDialog().setVisible(false);
                celestialMapper.setSearchPlacenamesDialogOpen(false);
            }
        });

        // Size all of the elements within the dialog
        dialog.pack();
    }

    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
}
