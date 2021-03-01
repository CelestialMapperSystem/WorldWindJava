/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.coordinates;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;

import javax.swing.*;
import java.awt.*;

public class CoordinatesDialog
{
    private final JDialog dialog;
    private final Rectangle bounds;
    private final GoToCoordinatePanel coodinatePanel;

    public CoordinatesDialog(WorldWindow wwdObject, CelestialMapper component)
    {
        this.dialog = new JDialog(component);
        this.coodinatePanel = new GoToCoordinatePanel(wwdObject);
        this.bounds = component.getBounds();

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Coordinates Input");

        // Set the location and resizable to true
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(true);

        // Can't seem to set a minimum size for the tool using BorderLayout

//        dialog.setPreferredSize(new Dimension(120,20));
//        dialog.setMinimumSize(new Dimension(120,20));
//        coodinatePanel.setMinimumSize(new Dimension(120,20));

        // Add the GoToCoordinatePanel to the dialog
        dialog.getContentPane().add(coodinatePanel, BorderLayout.CENTER);

        dialog.pack();

    }

    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
}
