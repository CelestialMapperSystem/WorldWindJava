/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.coordinates;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.measure.MeasureTool;

import javax.swing.*;
import java.awt.*;

public class CoordinatesDialog
{
    private final JDialog dialog;
    private final Rectangle bounds;
    private final GoToCoordinatePanel coodinatePanel;

    public CoordinatesDialog(WorldWindow wwdObject, CelestialMapper celestialMapper)
    {
        this.dialog = new JDialog(celestialMapper);
        this.coodinatePanel = new GoToCoordinatePanel(wwdObject);
        this.bounds = celestialMapper.getBounds();

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Coordinates Input");

        // Set the location and resizable to true
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(true);

//        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Add the GoToCoordinatePanel to the dialog
        dialog.getContentPane().add(coodinatePanel, BorderLayout.CENTER);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                celestialMapper.getCoordinatesDialog().setVisible(false);
                celestialMapper.setCoordinatesDialogOpen(false);
            }
        });

        dialog.pack();

    }

    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
}
