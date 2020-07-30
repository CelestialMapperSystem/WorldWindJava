/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms;

import gov.nasa.cms.features.CMSLayerManager;
import gov.nasa.cms.features.MeasureDialog;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.terrain.LocalElevationModel;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import gov.nasa.worldwindx.applications.worldwindow.core.Constants;
import gov.nasa.worldwindx.examples.MeasureToolUsage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * CelestialMapper.java
 *
 */
public class CelestialMapper
{

    protected static final String CMS_LAYER_NAME = "Celestial Shapes";
    protected static final String CLEAR_SELECTION = "CelestialMapper.ClearSelection";
    protected static final String ENABLE_EDIT = "CelestialMapper.EnableEdit";
    protected static final String OPEN = "CelestialMapper.Open";
    protected static final String OPEN_URL = "CelestialMapper.OpenUrl";
    protected static final String REMOVE_SELECTED = "CelestialMapper.RemoveSelected";
    protected static final String SAVE = "CelestialMapper.Save";
    protected static final String SELECTION_CHANGED = "CelestialMapper.SelectionChanged";
    protected static final String ELEVATIONS_PATH = "testData/lunar-dem.tif";

    //**************************************************************//
    //********************  Main  **********************************//
    //**************************************************************//
    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {

        ActionListener controller;
        protected RenderableLayer airspaceLayer;
        private CMSPlaceNamesMenu cmsPlaceNamesMenu;
        private WorldWindow wwd;
        private MeasureDialog measureDialog;

        public AppFrame()
        {
            super(true, false, false); // disable layer menu and statisics panel for AppFrame
            getWwd().getModel().getLayers().add(new CMSLayerManager(getWwd())); // add layer box UI

            // Wait for the elevation to import            
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Import the elevation model on a new thread to avoid freezing the UI
            Thread em = new Thread(new Runnable()
            {
                public void run()
                {
                    importElevations();
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            em.start(); // Load the elevation model   
            makeMenuBar(this, this.controller); // Make the menu bar

        }

        // Creates a local elevation model from ELEVATIONS_PATH and sets the view
        protected void importElevations()
        {
            try
            {
                // Download the data and save it in a temp file.
                File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH, ".tif");

                // Create a local elevation model from the data.
                final LocalElevationModel elevationModel = new LocalElevationModel();
                elevationModel.addElevations(sourceFile);

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        // Get current model
                        Globe globe = AppFrame.this.getWwd().getModel().getGlobe();
                        globe.setElevationModel(elevationModel);

                        // Set the view to look at the imported elevations
                        Sector modelSector = elevationModel.getSector();
                        ExampleUtil.goTo(getWwd(), modelSector);
                    }
                });
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Menu bar creation
        public void makeMenuBar(JFrame frame, final ActionListener controller)
        {
            JMenuBar menuBar = new JMenuBar();

            //======== "File" ========   
            JMenu menu = new JMenu("File");
            {
                JMenuItem item = new JMenuItem("Import Imagery");
                item.setActionCommand(OPEN_URL);
                item.addActionListener(controller);
                menu.add(item);
            }
            menuBar.add(menu);

            //======== "CMS Place Names" ========          
            cmsPlaceNamesMenu = new CMSPlaceNamesMenu(this, this.getWwd());
            menuBar.add(cmsPlaceNamesMenu);

            //======== "Tools" ========        
            JMenu tools = new JMenu("Tools");
            {
                JMenuItem tp = new JMenuItem("Terrain Profile");
                tp.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        // Add TerrainProfileLayer
                        TerrainProfileLayer tpl = new TerrainProfileLayer();
                        tpl.setEventSource(getWwd());
                        ApplicationTemplate.insertBeforeCompass(getWwd(), tpl); // display on screen
                    }
                });
                tools.add(tp);

                JMenuItem openMeasureDialogItem = new JMenuItem(new AbstractAction("Measurement")
                {
                    public void actionPerformed(ActionEvent actionEvent)
                    {
                        final MeasureTool measureTool = new MeasureTool(getWwd());
                        measureTool.setController(new MeasureToolController());
                        final WorldWindow wwd = getWwd();
                        try
                        {
                            if (AppFrame.this.measureDialog == null)
                            {
                                // Create the dialog from a final WorldWindow object
                                AppFrame.this.measureDialog = new MeasureDialog(wwd, measureTool, AppFrame.this);
                            }
                            AppFrame.this.measureDialog.setVisible(true); // display on screen
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                tools.add(openMeasureDialogItem);
            }
            menuBar.add(tools);

            //======== "Selection" ========            
            menu = new JMenu("Selection");
            {
                JMenuItem item = new JMenuItem("Deselect");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                item.setActionCommand(CLEAR_SELECTION);
                item.addActionListener(controller);
                menu.add(item);

                item = new JMenuItem("Delete");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
                item.setActionCommand(REMOVE_SELECTED);
                item.addActionListener(controller);
                menu.add(item);
            }
            menuBar.add(menu);
            frame.setJMenuBar(menuBar);

            //======== "View" ========           
            menu = new JMenu("View");
            {

            }
            menuBar.add(menu);

            //======== "Apollo" ========          
            /* This menu likely will have to take a similar 
            approach to how the place names are done when revisited */
            JMenu apolloMenu = new JMenu();
            {
                apolloMenu.setText("Apollo");

                //---- "Apollo Annotation..." ----
                JMenuItem newAnnotation = new JMenuItem();
                newAnnotation.setText("Annotation");
                newAnnotation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                newAnnotation.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                    }
                });
                apolloMenu.add(newAnnotation);

            }
            menuBar.add(apolloMenu);

            this.cmsPlaceNamesMenu.setWwd(this.wwd); //sets window for place names        
        }
    }
}
