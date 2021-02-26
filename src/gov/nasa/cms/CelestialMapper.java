/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms;

import gov.nasa.cms.features.*;
import gov.nasa.cms.features.layermanager.LayerManagerDialog;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.MoonFlat;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.features.Measurement;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.imageio.ImageIO;

/**
 * CelestialMapper.java
 *
 */
public class CelestialMapper extends AppFrame
{
    protected ActionListener controller;
    private CMSPlaceNamesMenu cmsPlaceNamesMenu;
    private ApolloMenu apolloMenu;
    private MoonElevationModel elevationModel;
    private CMSProfile profile;
    private MeasureDialog measureDialog;
    private MeasureTool measureTool;
    private CMSLineOfSight lineOfSight;
    private LayerManagerDialog layerManager;
    private WMSLayerManager wmsLayerManager;
    
    private boolean stereo;
    private boolean flat;
    private boolean isMeasureDialogOpen;
    private boolean isWMSManagerOpen;
    private boolean resetWindow;
    private boolean sight;
    private boolean isLayerManagerOpen;

    private JCheckBoxMenuItem stereoCheckBox;
    private JCheckBoxMenuItem flatGlobe;
    private JCheckBoxMenuItem measurementCheckBox;
    private JCheckBoxMenuItem wmsCheckBox;
    private JCheckBoxMenuItem layerManagerCheckBox;
    private JMenuItem reset;
    private CMSToolBar toolBar;
    private Registry regController;
    private Controller generalController;
    private MouseListener mouseListener;

    public void restart()
    {
        getWwd().shutdown();
        getContentPane().remove(wwjPanel); //removing component's parent must be JPanel
        this.initialize();
    }

    @Override
    public void initialize()
    {
        super.initialize();

        // Make the menu bar
        makeMenuBar(this, this.controller);

//        createToolbar(this);
        this.toolBar = new CMSToolBar(this);
        toolBar.createToolbar();



        // Import the lunar elevation data
        elevationModel = new MoonElevationModel(this.getWwd());
        //layerManager = new LayerManagerDialog(this.getWwd(), this);
        
        // Display the ScreenImage CMS logo as a RenderableLayer
        this.renderLogo();
    }



    /**
     * Causes the View attached to the specified WorldWindow to animate to the
     * specified sector. The View starts animating at its current location and
     * stops when the sector fills the window.
     *
     * @param wwd the WorldWindow who's View animates.
     * @param sector the sector to go to.
     *
     * @throws IllegalArgumentException if either the <code>wwd</code> or the
     * <code>sector</code> are <code>null</code>.
     */
    public static void goTo(WorldWindow wwd, Sector sector)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Create a bounding box for the specified sector in order to estimate its size in model coordinates.
        gov.nasa.worldwind.geom.Box extent = Sector.computeBoundingBox(wwd.getModel().getGlobe(),
                wwd.getSceneController().getVerticalExaggeration(), sector);

        // Estimate the distance between the center position and the eye position that is necessary to cause the sector to
        // fill a viewport with the specified field of view. 
        Angle fov = wwd.getView().getFieldOfView();
        double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

        // Configure OrbitView to look at the center of the sector from our estimated distance. 
        wwd.getView().goTo(new Position(sector.getCentroid(), 0d), zoom);
    }

    // Menu bar creation
    public void makeMenuBar(JFrame frame, final ActionListener controller)
    {
        JMenuBar menuBar = new JMenuBar();

        //========"File"=========
        JMenu layers = new JMenu("File");
        {
           // WMS Layer Manager
            wmsCheckBox = new JCheckBoxMenuItem("WMS Layer Panel");
            wmsCheckBox.setSelected(isWMSManagerOpen);
            wmsCheckBox.addActionListener((ActionEvent event) ->
            {
                isWMSManagerOpen = !isWMSManagerOpen;
                if (isWMSManagerOpen)
                {
                    if (wmsLayerManager == null)
                    {
                        wmsLayerManager = new WMSLayerManager(this.getWwd(), this);
                    }
                    wmsLayerManager.setVisible(true);
                }
                else
                {
                    wmsLayerManager.setVisible(false);
                }
            });
            layers.add(wmsCheckBox);
        }
        menuBar.add(layers);
        
        //======== "CMS Place Names" ========          
        cmsPlaceNamesMenu = new CMSPlaceNamesMenu(this, this.getWwd());
        menuBar.add(cmsPlaceNamesMenu);

        //======== "Tools" ========        
        JMenu tools = new JMenu("Tools");
        {
            // Terrain Profiler
            profile = new CMSProfile(this.getWwd());
            tools.add(profile);
            menuBar.add(tools);

            // Measure Tool
//            measurementCheckBox = new JCheckBoxMenuItem("Measurement");
//            measurementCheckBox.setSelected(isMeasureDialogOpen);
//            measurementCheckBox.addActionListener((ActionEvent event) ->
//            {
//                isMeasureDialogOpen = !isMeasureDialogOpen;
//                if (isMeasureDialogOpen)
//                {
//                    // Only open if the MeasureDialog has never been opened
//                    if (measureDialog == null)
//                    {
//                        // Create the dialog from the WorldWindow, MeasureTool and AppFrame
//                        measureDialog = new MeasureDialog(getWwd(), measureTool, this);
//                    }
//                    // Display on screen
//                    measureDialog.setVisible(true);
//                } else // Hide the dialog
//                {
//                    measureDialog.setVisible(false);
//                }
//            });
//            tools.add(measurementCheckBox);
        }
        menuBar.add(tools);

        //======== "Apollo" ========      
        apolloMenu = new ApolloMenu(this.getWwd());
        menuBar.add(apolloMenu);

        //======== "View" ========           
        JMenu view = new JMenu("View");
        {
            //======== "Stereo" ==========
            stereoCheckBox = new JCheckBoxMenuItem("Stereo");
            stereoCheckBox.setSelected(stereo);
            stereoCheckBox.addActionListener((ActionEvent event) ->
            {
                stereo = !stereo;
                if (stereo && !flat)
                {
                    // Set the stereo.mode property to request stereo. Request red-blue anaglyph in this case. Can also request
                    // "device" if the display device supports stereo directly. To prevent stereo, leave the property unset or set
                    // it to an empty string.
                    System.setProperty("gov.nasa.worldwind.stereo.mode", "redblue");
                    //  Configure the initial view parameters so that the balloons are immediately visible.
                    Configuration.setValue(AVKey.INITIAL_LATITUDE, 20);
                    Configuration.setValue(AVKey.INITIAL_LONGITUDE, 30);
                    Configuration.setValue(AVKey.INITIAL_ALTITUDE, 10e4);
                    Configuration.setValue(AVKey.INITIAL_HEADING, 500);
                    Configuration.setValue(AVKey.INITIAL_PITCH, 80);
                } else if (stereo && flat)
                {
                    //without this else if loop, the canvas glitches               
                } else {
                    System.setProperty("gov.nasa.worldwind.stereo.mode", "");
                    Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);
                    Configuration.setValue(AVKey.INITIAL_LONGITUDE, 0);
                    Configuration.setValue(AVKey.INITIAL_ALTITUDE, 8e6);
                    Configuration.setValue(AVKey.INITIAL_HEADING, 0);
                    Configuration.setValue(AVKey.INITIAL_PITCH, 0);
                }
                restart();
            });
            view.add(stereoCheckBox);  
            
            //======== "2D Flat Globe" ==========
            flatGlobe = new JCheckBoxMenuItem("2D Flat");
            flatGlobe.setSelected(flat);
            flatGlobe.addActionListener((ActionEvent event) ->
            {
                flat = !flat;
                if (flat)
                {
                    Configuration.setValue(AVKey.GLOBE_CLASS_NAME, MoonFlat.class.getName());
                } else 
                {
                    Configuration.setValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Earth");
                }
                restart();
            });
            view.add(flatGlobe);    
            
            //======== "Line of Sight" =========
            lineOfSight = new CMSLineOfSight(this, this.getWwd());
            view.add(lineOfSight);
            
            
            //======== "Reset" =========
            reset = new JMenuItem("Reset");
            reset.setSelected(resetWindow);
            reset.addActionListener((ActionEvent event) ->
            {
                resetWindow = !resetWindow;
                if (resetWindow)
                {
                    restart(); //resets window to launch status
                } 
            });
            view.add(reset);
        }
        menuBar.add(view);
        
        frame.setJMenuBar(menuBar);
    }

    // Renders the logo for CMS in the northwest corner of the screen 
    private void renderLogo()
    {
        final ScreenImage cmsLogo = new ScreenImage();

        try
        {
            cmsLogo.setImageSource(ImageIO.read(new File("cms-data/cms-logo.png")));
            Rectangle view = getWwd().getView().getViewport();
            // Set the screen location to different points to offset the image size
            cmsLogo.setScreenLocation(new Point(view.x + 55, view.y + 70));
        } catch (IOException ex) 
        {
            Logger.getLogger(CelestialMapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderable(cmsLogo);
        layer.setName("Logo");

        getWwd().getModel().getLayers().add(layer);
    }

    public LayerManagerDialog getLayerManager()
    {
        return layerManager;
    }

    public WMSLayerManager getWmsLayerManager()
    {
        return wmsLayerManager;
    }

    public boolean isWMSManagerOpen()
    {
        return isWMSManagerOpen;
    }

    public boolean isLayerManagerOpen()
    {
        return isLayerManagerOpen;
    }

    public void setLayerManager(LayerManagerDialog layerManager)
    {
        this.layerManager = layerManager;
    }

    public void setWmsLayerManager(WMSLayerManager wmsLayerManager)
    {
        this.wmsLayerManager = wmsLayerManager;
    }

    public void setWMSManagerOpen(boolean WMSManagerOpen)
    {
        isWMSManagerOpen = WMSManagerOpen;
    }

    public void setLayerManagerOpen(boolean layerManagerOpen)
    {
        isLayerManagerOpen = layerManagerOpen;
    }

    public boolean isMeasureDialogOpen()
    {
        return isMeasureDialogOpen;
    }

    public MeasureDialog getMeasureDialog()
    {
        return this.measureDialog;
    }

    public CMSPlaceNamesMenu getCmsPlaceNamesMenu()
    {
        return cmsPlaceNamesMenu;
    }

    public void setCmsPlaceNamesMenu(CMSPlaceNamesMenu cmsPlaceNamesMenu)
    {
        this.cmsPlaceNamesMenu = cmsPlaceNamesMenu;
    }

    public ApolloMenu getApolloMenu()
    {
        return apolloMenu;
    }

    public void setApolloMenu(ApolloMenu apolloMenu)
    {
        this.apolloMenu = apolloMenu;
    }

    public MoonElevationModel getElevationModel()
    {
        return elevationModel;
    }

    public void setElevationModel(MoonElevationModel elevationModel)
    {
        this.elevationModel = elevationModel;
    }

    public CMSProfile getProfile()
    {
        return profile;
    }

    public void setProfile(CMSProfile profile)
    {
        this.profile = profile;
    }

    public void setMeasureDialog(MeasureDialog measureDialog)
    {
        this.measureDialog = measureDialog;
    }

    public MeasureTool getMeasureTool()
    {
        return measureTool;
    }

    public void setMeasureTool(MeasureTool measureTool)
    {
        this.measureTool = measureTool;
    }

    public CMSLineOfSight getLineOfSight()
    {
        return lineOfSight;
    }

    public void setLineOfSight(CMSLineOfSight lineOfSight)
    {
        this.lineOfSight = lineOfSight;
    }

    public boolean isStereo()
    {
        return stereo;
    }

    public void setStereo(boolean stereo)
    {
        this.stereo = stereo;
    }

    public boolean isFlat()
    {
        return flat;
    }

    public void setFlat(boolean flat)
    {
        this.flat = flat;
    }

    public void setMeasureDialogOpen(boolean measureDialogOpen)
    {
        isMeasureDialogOpen = measureDialogOpen;
    }

    public boolean isResetWindow()
    {
        return resetWindow;
    }

    public void setResetWindow(boolean resetWindow)
    {
        this.resetWindow = resetWindow;
    }

    public boolean isSight()
    {
        return sight;
    }

    public void setSight(boolean sight)
    {
        this.sight = sight;
    }

    public JCheckBoxMenuItem getStereoCheckBox()
    {
        return stereoCheckBox;
    }

    public void setStereoCheckBox(JCheckBoxMenuItem stereoCheckBox)
    {
        this.stereoCheckBox = stereoCheckBox;
    }

    public JCheckBoxMenuItem getFlatGlobe()
    {
        return flatGlobe;
    }

    public void setFlatGlobe(JCheckBoxMenuItem flatGlobe)
    {
        this.flatGlobe = flatGlobe;
    }

    public JCheckBoxMenuItem getMeasurementCheckBox()
    {
        return measurementCheckBox;
    }

    public void setMeasurementCheckBox(JCheckBoxMenuItem measurementCheckBox)
    {
        this.measurementCheckBox = measurementCheckBox;
    }

    public JCheckBoxMenuItem getWmsCheckBox()
    {
        return wmsCheckBox;
    }

    public void setWmsCheckBox(JCheckBoxMenuItem wmsCheckBox)
    {
        this.wmsCheckBox = wmsCheckBox;
    }

    public JCheckBoxMenuItem getLayerManagerCheckBox()
    {
        return layerManagerCheckBox;
    }

    public void setLayerManagerCheckBox(JCheckBoxMenuItem layerManagerCheckBox)
    {
        this.layerManagerCheckBox = layerManagerCheckBox;
    }

    public JMenuItem getReset()
    {
        return reset;
    }

    public void setReset(JMenuItem reset)
    {
        this.reset = reset;
    }

    public CMSToolBar getToolBar()
    {
        return toolBar;
    }

    public void setToolBar(CMSToolBar toolBar)
    {
        this.toolBar = toolBar;
    }

    public Registry getRegController()
    {
        return regController;
    }

    public void setRegController(Registry regController)
    {
        this.regController = regController;
    }

    public Controller getGeneralController()
    {
        return generalController;
    }

    public void setGeneralController(Controller generalController)
    {
        this.generalController = generalController;
    }

    public MouseListener getMouseListener()
    {
        return mouseListener;
    }

    public void setMouseListener(MouseListener mouseListener)
    {
        this.mouseListener = mouseListener;
    }

    public void setLayerManagerisOpen(boolean b)
    {
        this.isLayerManagerOpen = b;
    }
}
