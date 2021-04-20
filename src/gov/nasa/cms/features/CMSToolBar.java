/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.features.coordinates.CoordinatesDialog;
import gov.nasa.cms.features.layermanager.LayerManagerDialog;
import gov.nasa.cms.features.placemarks.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/*
@author: Geoff Norman - Ames Intern - 03/2021
*/
public class CMSToolBar
{
    private CelestialMapper frame;
    private JToolBar toolBar;

    private boolean isLayerManagerOpen = false;
    private boolean isMeasureDialogOpen = false;
    private boolean isCoordinatesDialogOpen = false;
    private boolean isProfilerOpen = false;
    private boolean isLineOfSightOpen = false;
    private boolean isLandingSitesOpen = false;
    private boolean isPlacemarksOpen = false;
    private boolean isPlaceNamesSearchOpen = false;

    public CMSToolBar(CelestialMapper frame)
    {
        this.frame = frame;
        this.toolBar = null;
    }

    public void createToolbar()
    {
        // The original constructor in worldwindow.ToolBarImpl relies completely
        // on an XML based configuration and initialization.
        // Will attempt to create a new GradientToolBar() object without requiring the
        // same process
        this.toolBar = new JToolBar();
        toolBar.setLayout(new GridLayout(1, 5));
        toolBar.setRollover(false);
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);

        // To wire up a new button, start by adding a new JButton object here:
        ArrayList<JButton> buttons = new ArrayList<>(5);
        buttons.add(new JButton("Layer Manager"));
        buttons.add(new JButton("Measurements"));
        buttons.add(new JButton("Coordinates"));
        buttons.add(new JButton("Profiler"));
        buttons.add(new JButton("Line of Sight"));
        buttons.add(new JButton("Landing Sites"));
        buttons.add(new JButton("Search Lunar Features"));
        buttons.add(new JButton("Placemarks"));

        try
        {
            // Use this method to add the correction actionlistener to each button
            initializeButtons(buttons);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        buttons.forEach(toolBar::add);

        // Have to add this as a child of AppPanel, the parent of CelestialMapper
        // so it gets removed at the same time as wwjPanel when reset is called
        this.frame.getWwjPanel().add(toolBar, BorderLayout.NORTH);
    }

    public JToolBar getToolBar()
    {
        return toolBar;
    }

    public void setToolBar(JToolBar toolBar)
    {
        this.toolBar = toolBar;
    }

    private void initializeButtons(ArrayList<JButton> buttons) throws IOException
    {
        for (JButton button : buttons)
        {
            button.setPreferredSize(new Dimension(50, 80));
            button.setFocusPainted(false);

            button.setHorizontalTextPosition(AbstractButton.CENTER);
            button.setVerticalTextPosition(AbstractButton.BOTTOM);

            String buttonText = button.getText();

            // Due to weird issues with the original Switch/Case code block here
            // Where the button was set according to the string value of it's name
            // I had to encapsulate everything in an Enum and resort to using this
            // convoluted If / Else tree to make sure that this first button
            // wasn't being given multiple ActionListeners AND that each button was
            //
            switch (buttonText)
            {
                case "Layer Manager":
//                System.out.println(buttonText + " = LAYER_MANAGER: " + buttonText.equals(BUTTON.LAYER_MANAGER.name));
                    setButtonIcon("cms-data/icons/icons8-layers-48.png", button);
                    button.addActionListener(e -> showLayerManager());
                    break;
                case "Measurements":
//                System.out.println(buttonText + " = MEASUREMENTS: " + buttonText.equals(BUTTON.MEASUREMENTS.name));
                    setButtonIcon("cms-data/icons/icons8-measurement-tool-48.png", button);
                    button.addActionListener(e -> showMeasureTool());
                    break;
                case "Coordinates":
                    setButtonIcon("cms-data/icons/icons8-grid-48.png", button);
                    button.addActionListener(e -> showCoordinatesDialog());
                    break;
                case "Profiler":
                    setButtonIcon("cms-data/icons/icons8-bell-curve-48.png", button);
                    button.addActionListener(e -> showProfiler());
                    break;
                case "Line of Sight":
                    setButtonIcon("cms-data/icons/icons8-head-profile-48.png", button);
                    button.addActionListener(e -> showLineOfSight());
                    break;
                case "Search Lunar Features":
                    setButtonIcon("cms-data/icons/icons8-map-marker-48.png", button);
                    button.addActionListener(e -> showPlacenamesSearch());
                    break;
                case "Landing Sites":
                    setButtonIcon("cms-data/icons/icons8-launchpad-48.png", button);
                    button.addActionListener(e -> showLandingSites());
                    break;
                case "Placemarks":
                    setButtonIcon("cms-data/icons/icons8-place-marker-48.png", button);
                    button.addActionListener(e -> showPlacemarks());
                    break;
            }
        }
    }

    private void setButtonIcon(String path, AbstractButton button) throws IOException
    {
        button.setIcon(new ImageIcon(ImageIO.read(new File(path))));
    }

    private void showPlacenamesSearch()
    {
        this.isPlaceNamesSearchOpen = !isPlaceNamesSearchOpen;
        if (isPlaceNamesSearchOpen)
        {
            if (frame.getSearchPlacenamesDialog() == null)
            {
                frame.setSearchPlacenamesDialog(new SearchPlacenamesDialog(frame.getWwd(), frame));
            }
            frame.getSearchPlacenamesDialog().setVisible(true);
        }
        else
        {
            frame.getSearchPlacenamesDialog().setVisible(false);
        }
    }

    private void showPlacemarks()
    {
        this.isPlacemarksOpen = !isPlacemarksOpen;
        if (isPlacemarksOpen)
        {
            if (frame.getPointPlacemarkDialog() == null)
            {
                frame.setPointPlacemarkDialog(new PointPlacemarkDialog(frame.getWwd(), frame));
            }
            frame.getPointPlacemarkDialog().setVisible(true);
        }
        else
        {
            frame.getPointPlacemarkDialog().setVisible(false);
        }
    }

    private void showLandingSites()
    {
        this.isLandingSitesOpen = !isLandingSitesOpen;
        if (isLandingSitesOpen)
        {
            if (frame.getLandingSites() == null)
            {
                frame.setLandingSites(new ApolloDialog(frame.getWwd(), frame));
            }
            frame.getLandingSites().setVisible(true);
        }
        else
        {
            frame.getLandingSites().setVisible(false);
        }
    }

    private void showLineOfSight()
    {
        this.isLineOfSightOpen = !isLineOfSightOpen;
        if (isLineOfSightOpen)
        {
            if (frame.getLineOfSight() == null)
            {
                frame.setLineOfSight(new LineOfSightController(frame, frame.getWwd()));
            }
            frame.getLineOfSight().setVisible(true);
        }
        else
        {
            frame.getLineOfSight().setVisible(false);
        }
    }

    private void showProfiler()
    {
        this.isProfilerOpen = !isProfilerOpen;
        if (isProfilerOpen)
        {
            if (frame.getProfile() == null)
            {
                frame.setProfile(new CMSProfile(frame.getWwd(), frame));
            }
            frame.getProfile().setVisible(true);
        }
        else
        {
            frame.getProfile().setVisible(false);
        }
    }



    private void showCoordinatesDialog()
    {
        this.isCoordinatesDialogOpen = !isCoordinatesDialogOpen;
        if (isCoordinatesDialogOpen)
        {
            if (frame.getCoordinatesDialog() == null)
            {
                frame.setCoordinatesDialog(new CoordinatesDialog(frame.getWwd(), frame));
            }
            frame.getCoordinatesDialog().setVisible(true);
        }
        else
        {
            frame.getCoordinatesDialog().setVisible(false);
        }
    }

    public void showLayerManager()
    {
        {
            this.isLayerManagerOpen = !isLayerManagerOpen;
            if (isLayerManagerOpen)
            {
                if (frame.getLayerManager() == null)
                {
                    frame.setLayerManager(new LayerManagerDialog(frame.getWwd(), frame));
                }
                frame.getLayerManager().setVisible(true);
//                frame.setLayerManagerisOpen(true);
            }
            else
            {
                frame.getLayerManager().setVisible(false);
//                frame.setLayerManagerisOpen(false);
            }
        }
    }

    public void showMeasureTool()
    {
        {
            this.isMeasureDialogOpen = !isMeasureDialogOpen;
            if (isMeasureDialogOpen)
            {
                // Only open if the MeasureDialog has never been opened
                if (frame.getMeasureDialog() == null)
                {
                    // Create the dialog from the WorldWindow, MeasureTool and AppFrame
                    frame.setMeasureDialog(new MeasureDialog(frame.getWwd(), frame.getMeasureTool(), frame));
                }
                // Display on screen
                frame.getMeasureDialog().setVisible(true);
//                frame.setMeasureDialogOpen(true);
            }
            else // Hide the dialog
            {
                frame.getMeasureDialog().setVisible(false);
//                frame.setMeasureDialogOpen(false);
            }
        }
    }

    public void update()
    {

    }

    public void restart()
    {
        if(frame.getMeasureDialog() != null)
        frame.getMeasureDialog().setVisible(false);
        frame.setMeasureDialog(null);

        if(frame.getLayerManager() != null)
        frame.getLayerManager().setVisible(false);
        frame.setLayerManager(null);

        if(frame.getCoordinatesDialog() != null)
        frame.getCoordinatesDialog().setVisible(false);
        frame.setCoordinatesDialog(null);

        if(frame.getProfile() != null)
        frame.getProfile().setVisible(false);
        frame.setProfile(null);

        if(frame.getLineOfSight() != null)
        frame.getLineOfSight().setVisible(false);
        frame.setLineOfSight(null);

        if(frame.getSearchPlacenamesDialog() != null)
        frame.getSearchPlacenamesDialog().setVisible(false);
        frame.setSearchPlacenamesDialog(null);

        if(frame.getPointPlacemarkDialog() != null)
        frame.getPointPlacemarkDialog().setVisible(false);
        frame.setPointPlacemarkDialog(null);

        this.frame = null;
        this.toolBar.removeAll();
        this.toolBar = null;
    }
}
