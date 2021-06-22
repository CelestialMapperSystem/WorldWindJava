/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.features.coordinates.CoordinatesDialog;
import gov.nasa.cms.features.layermanager.LayerManagerDialog;
import gov.nasa.cms.features.placemarks.SearchPlacenamesDialog;
import gov.nasa.cms.features.placemarks.PointPlacemarkDialog;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import gov.nasa.cms.features.moonshading.MoonShadingDialog;
import java.util.concurrent.ExecutionException;

/*
@author: Geoff Norman - Ames Intern - 03/2021
*/
public class CMSToolBar
{
    private CelestialMapper celestialMapper;
    private JToolBar toolBar;

    private boolean isLayerManagerOpen = false;
    private boolean isMeasureDialogOpen = false;
    private boolean isCoordinatesDialogOpen = false;
    private boolean isProfilerOpen = false;
    private boolean isLineOfSightOpen = false;
    private boolean isLandingSitesOpen = false;
    private boolean isPlacemarksOpen = false;
    private boolean isPlaceNamesSearchOpen = false;
    private boolean isMoonShadingDialogOpen = false;

    public CMSToolBar(CelestialMapper celestialMapper)
    {
        this.celestialMapper = celestialMapper;
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
        buttons.add(new JButton("Sight Lines"));
        buttons.add(new JButton("Landing Sites"));
        buttons.add(new JButton("Search Lunar Features"));
        buttons.add(new JButton("Placemarks"));
        buttons.add(new JButton("Moon Shading"));

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
        this.celestialMapper.getWwjPanel().add(toolBar, BorderLayout.NORTH);
    }

    public JToolBar getToolBar()
    {
        return toolBar;
    }

    public void setToolBar(JToolBar toolBar)
    {
        this.toolBar = toolBar;
    }

    private void initializeButtons(ArrayList<JButton> buttons)
        throws IOException
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
                    setButtonIcon("cms-data/icons/icons8-layers-48.png",
                        button);
                    button.addActionListener(e -> showLayerManager());
                    break;
                case "Measurements":
//                System.out.println(buttonText + " = MEASUREMENTS: " + buttonText.equals(BUTTON.MEASUREMENTS.name));
                    setButtonIcon(
                        "cms-data/icons/icons8-measurement-tool-48.png",
                        button);
                    button.addActionListener(e -> showMeasureTool());
                    break;
                case "Coordinates":
                    setButtonIcon("cms-data/icons/icons8-grid-48.png", button);
                    button.addActionListener(e -> showCoordinatesDialog());
                    break;
                case "Profiler":
                    setButtonIcon("cms-data/icons/icons8-bell-curve-48.png",
                        button);
                    button.addActionListener(e -> showProfiler());
                    break;
                case "Sight Lines":
                    setButtonIcon("cms-data/icons/icons8-head-profile-48.png",
                        button);
                    button.addActionListener(e -> showLineOfSight());
                    break;
                case "Search Lunar Features":
                    setButtonIcon("cms-data/icons/icons8-map-marker-48.png",
                        button);
                    button.addActionListener(e -> showPlacenamesSearch());
                    break;
                case "Landing Sites":
                    setButtonIcon("cms-data/icons/icons8-launchpad-48.png",
                        button);
                    button.addActionListener(e -> showLandingSites());
                    break;
                case "Placemarks":
                    setButtonIcon("cms-data/icons/icons8-place-marker-48.png",
                        button);
                    button.addActionListener(e -> showPlacemarks());
                    break;
                case "Moon Shading":
                    setButtonIcon("cms-data/icons/icons8-moon-48.png", button);
                    button.addActionListener(e -> showMoonShading());
                    break;
            }
        }
    }

    private void setButtonIcon(String path, AbstractButton button)
        throws IOException
    {
        button.setIcon(new ImageIcon(ImageIO.read(new File(path))));
    }

    private void showPlacenamesSearch()
    {

//        this.isPlaceNamesSearchOpen = !isPlaceNamesSearchOpen;
//        if (isPlaceNamesSearchOpen)
//        {

        /*
        * There's now a substantial GUI blocking delay when creating
        * the table so we need to execute this on the EDT... or at least a new
        * Thread.
        * Problem with the Runnable interface is that I can't
        * guarantee without using a ThreadPoolExecutor / Futures
        * implementation that the thread is finished BEFORE attempting to
        * open up the dialog. SwingWorker at least has the get() method which
        *  blocks the next code blocks until it's actually finished.
        * https://docs.oracle.com/javase/tutorial/uiswing/concurrency/simple.html
        */
            SwingWorker createPlacenamesDialog = new SwingWorker<SearchPlacenamesDialog, Void>()
            {
                @Override
                protected SearchPlacenamesDialog doInBackground() throws Exception
                {
                    return new SearchPlacenamesDialog(celestialMapper.getWwd(), celestialMapper);
                }

                @Override
                public void done() {
                    try
                    {
                        celestialMapper.setSearchPlacenamesDialog(get());
                        celestialMapper.getSearchPlacenamesDialog().setVisible(true);
                        celestialMapper.setSearchPlacenamesDialogOpen(true);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            if (celestialMapper.getSearchPlacenamesDialog() == null)
            {
                createPlacenamesDialog.execute();
//                Runnable createPlacenamesDialog = () -> {
//                    celestialMapper.setSearchPlacenamesDialog(
//                        new SearchPlacenamesDialog(celestialMapper.getWwd(),
//                            celestialMapper));
//                };
//                new Thread(createPlacenamesDialog).start();

            } else {
                if(!celestialMapper.isSearchPlacenamesDialogOpen())    {

                    celestialMapper.getSearchPlacenamesDialog().setVisible(true);
                    celestialMapper.setSearchPlacenamesDialogOpen(true);
                }
                else
                {
                    celestialMapper.getSearchPlacenamesDialog().setVisible(false);
                    celestialMapper.setSearchPlacenamesDialogOpen(false);
                }
            }
    }

    private void showPlacemarks()
    {
//        this.isPlacemarksOpen = !isPlacemarksOpen;
//        if (isPlacemarksOpen)
//        {
        if (celestialMapper.getPointPlacemarkDialog() == null)
        {
            celestialMapper.setPointPlacemarkDialog(
                new PointPlacemarkDialog(celestialMapper.getWwd(),
                    celestialMapper));
        }

        if (!celestialMapper.isPointPlacemarkDialogOpen())
        {
            celestialMapper.getPointPlacemarkDialog().setVisible(true);
            celestialMapper.setPointPlacemarkDialogOpen(true);
        }
        else
        {
            celestialMapper.getPointPlacemarkDialog().setVisible(false);
            celestialMapper.setPointPlacemarkDialogOpen(false);
        }
    }
    
    private void showMoonShading()
    {
        this.isMoonShadingDialogOpen = !isMoonShadingDialogOpen ;
        if (isMoonShadingDialogOpen )
        {
            if (celestialMapper.getMoonShadingDialog() == null)
            {
                celestialMapper.setMoonShadingDialog(new MoonShadingDialog(celestialMapper.getWwd(), celestialMapper));
            }
            celestialMapper.getMoonShadingDialog().setVisible(true);
        }
        else
        {
            celestialMapper.getMoonShadingDialog().setVisible(false);
        }
    }

    private void showLandingSites()
    {
//        this.isLandingSitesOpen = !isLandingSitesOpen;
//        if (isLandingSitesOpen)
//        {
        if (celestialMapper.getLandingSites() == null)
        {
            celestialMapper.setLandingSites(new ApolloDialog(
                celestialMapper.getWwd(), celestialMapper));
        }

        if (!celestialMapper.isLandingSitesOpen())
        {
            celestialMapper.getLandingSites().setVisible(true);
            celestialMapper.setLandingSitesOpen(true);
        }
        else
        {
            celestialMapper.getLandingSites().setVisible(false);
            celestialMapper.setLandingSitesOpen(false);
        }
    }

    private void showLineOfSight()
    {
//        this.isLineOfSightOpen = !isLineOfSightOpen;
//        if (isLineOfSightOpen) {
        if (celestialMapper.getLineOfSight() == null)
        {
            celestialMapper.setLineOfSight(
                new LineOfSightController(celestialMapper,
                    celestialMapper.getWwd()));
        }

        if (!celestialMapper.isLineOfSightOpen())
        {
            celestialMapper.getLineOfSight().setVisible(true);
            celestialMapper.setLineOfSightOpen(true);
        }
        else
        {
            celestialMapper.getLineOfSight().setVisible(false);
            celestialMapper.setLineOfSightOpen(false);
        }
    }

    private void showProfiler()
    {
        this.isProfilerOpen = !isProfilerOpen;
        if (isProfilerOpen)
        {
            if (celestialMapper.getProfile() == null)
            {
                celestialMapper.setProfile(
                    new CMSProfile(celestialMapper.getWwd(),
                        celestialMapper));
            }
            celestialMapper.getProfile().setVisible(true);
        }
        else
        {
            celestialMapper.getProfile().setVisible(false);
        }
    }

    private void showCoordinatesDialog()
    {
//        this.isCoordinatesDialogOpen = !isCoordinatesDialogOpen;
//        if (isCoordinatesDialogOpen)
//        {
        if (celestialMapper.getCoordinatesDialog() == null)
        {
            celestialMapper.setCoordinatesDialog(
                new CoordinatesDialog(celestialMapper.getWwd(),
                    celestialMapper));
        }

        if (!celestialMapper.isCoordinatesDialogOpen())
        {
            celestialMapper.getCoordinatesDialog().setVisible(true);
            celestialMapper.setCoordinatesDialogOpen(true);
        }
        else
        {
            celestialMapper.getCoordinatesDialog().setVisible(false);
            celestialMapper.setCoordinatesDialogOpen(false);
        }
    }

    public void showLayerManager()
    {
        {
//            this.isLayerManagerOpen = !isLayerManagerOpen;
//            if (isLayerManagerOpen)
//            {
            if (celestialMapper.getLayerManager() == null)
            {
                celestialMapper.setLayerManager(
                    new LayerManagerDialog(celestialMapper.getWwd(),
                        celestialMapper));
            }
            if (!celestialMapper.isLayerManagerOpen())
            {
                celestialMapper.getLayerManager().setVisible(true);
                celestialMapper.setLayerManagerOpen(true);
            }
//                frame.setLayerManagerisOpen(true);
//            }
            else
            {
                celestialMapper.getLayerManager().setVisible(false);
                celestialMapper.setLayerManagerOpen(false);
//                frame.setLayerManagerisOpen(false);
            }
        }
    }

    public void showMeasureTool()
    {
        {
//            this.isMeasureDialogOpen = !isMeasureDialogOpen;
//            if (isMeasureDialogOpen)
//            {
            // Only open if the MeasureDialog has never been opened
            if (celestialMapper.getMeasureDialog() == null)
            {
                // Create the dialog from the WorldWindow, MeasureTool and AppFrame
                celestialMapper.setMeasureDialog(
                    new MeasureDialog(celestialMapper.getWwd(),
                        celestialMapper.getMeasureTool(),
                        celestialMapper));
            }
            if (!celestialMapper.isMeasureDialogOpen())
            {

                // Display on screen
                celestialMapper.getMeasureDialog().setVisible(true);
                celestialMapper.setMeasureDialogOpen(true);
            }
            else // Hide the dialog
            {
                celestialMapper.getMeasureDialog().setVisible(false);
                celestialMapper.setMeasureDialogOpen(false);
            }
        }
    }

    public void update()
    {

    }

    public void restart()
    {
        if (celestialMapper.getMeasureDialog() != null)
            celestialMapper.getMeasureDialog().setVisible(false);
        celestialMapper.setMeasureDialog(null);

        if (celestialMapper.getLayerManager() != null)
            celestialMapper.getLayerManager().setVisible(false);
        celestialMapper.setLayerManager(null);

        if (celestialMapper.getCoordinatesDialog() != null)
            celestialMapper.getCoordinatesDialog().setVisible(false);
        celestialMapper.setCoordinatesDialog(null);

        if (celestialMapper.getProfile() != null)
            celestialMapper.getProfile().setVisible(false);
        celestialMapper.setProfile(null);

        if (celestialMapper.getLineOfSight() != null)
            celestialMapper.getLineOfSight().setVisible(false);
        celestialMapper.setLineOfSight(null);

        if (celestialMapper.getSearchPlacenamesDialog() != null)
            celestialMapper.getSearchPlacenamesDialog().setVisible(false);
        celestialMapper.setSearchPlacenamesDialog(null);

        if (celestialMapper.getPointPlacemarkDialog() != null)
            celestialMapper.getPointPlacemarkDialog().setVisible(false);
        celestialMapper.setPointPlacemarkDialog(null);

        this.celestialMapper = null;
        this.toolBar.removeAll();
        this.toolBar = null;
    }
}
