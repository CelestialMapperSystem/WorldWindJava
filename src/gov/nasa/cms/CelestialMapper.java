/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms;

import gov.nasa.cms.features.*;
import gov.nasa.cms.features.coordinates.*;
import gov.nasa.cms.features.layermanager.LayerManagerDialog;
import gov.nasa.cms.features.placemarks.SearchPlacenamesDialog;
import gov.nasa.cms.features.placemarks.PointPlacemarkDialog;
import gov.nasa.cms.layers.WorldMapLayer;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.MoonFlat;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.measure.MeasureTool;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

/**
 * CelestialMapper.java
 *
 */
public class CelestialMapper extends AppFrame
{
    private CMSPlaceNamesMenu cmsPlaceNamesMenu;
    private ApolloMenu apolloMenu;
    private CMSProfile profile;
    private MeasureDialog measureDialog;
    private MeasureTool measureTool;
    private LineOfSightController lineOfSight;
    private LayerManagerDialog layerManager;
    private WMSLayerManager wmsLayerManager;
    private MoonElevationModel elevationModel;
    private ImportKML kmlImporter;
    private ImportedDataDialog importedDataDialog;
    private CMSToolBar toolBar;
    private MouseListener mouseListener;
    private CoordinatesDialog coordinatesDialog;
    private ApolloDialog apolloDialog;
    private WorldMapLayer wml;
    private CoordinatesDisplay coordDisplay;
    private CMSWWOUnitsFormat unitsFormat;
    private PointPlacemarkDialog pointPlacemarkDialog;
    private SearchPlacenamesDialog searchPlacenamesDialog;
    
    private boolean stereo;
    private boolean flat;
    private boolean isMeasureDialogOpen;
    private boolean isWMSManagerOpen;
    private boolean resetWindow;
    private boolean sight;
    private boolean isLayerManagerOpen;
    private boolean isImportedDataDialogOpen;
    private boolean isCoordinatesDialogOpen;
    private boolean isLineOfSightOpen;
    private boolean isLandingSitesOpen;
    private boolean isPointPlacemarkDialogOpen;
    private boolean isSearchPlacenamesDialogOpen;

    private JCheckBoxMenuItem stereoCheckBox;
    private JCheckBoxMenuItem flatGlobe;
    private JCheckBoxMenuItem measurementCheckBox;
    private JCheckBoxMenuItem wmsCheckBox;
    private JCheckBoxMenuItem layerManagerCheckBox;
    private JCheckBoxMenuItem importedDataCheckBox;
    private JMenuItem reset;
    private JMenuItem exportMeasureTool;

    public void restart()
    {
        getWwd().shutdown();
        getContentPane().remove(wwjPanel); //removing component's parent must be JPanel
        this.toolBar.restart();
        this.toolBar = null;
        this.initialize();
    }


    @Override
    public void initialize()
    {
        super.initialize();

        // Make the menu bar
        makeMenuBar(this);

        // create minimap
        createNewWML();

        // create toolbar with buttons
        this.toolBar = new CMSToolBar(this);
        this.toolBar.createToolbar();

        // create coordinates display layer
        this.unitsFormat = new CMSWWOUnitsFormat();
        this.unitsFormat.setShowUTM(true);
        this.unitsFormat.setShowWGS84(false);
        this.coordDisplay = new CoordinatesDisplay(this);

        // Import the lunar elevation data
        elevationModel = new MoonElevationModel(this.getWwd());
        
        // Display the ScreenImage CMS logo as a RenderableLayer
        this.renderLogo();

        // TODO - Decide whether to use pack or not, to accommodate the space that the cmsToolBar overlaps WorldWindow.
         this.pack();

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
    public void makeMenuBar(JFrame frame)
    {
        JMenuBar menuBar = new JMenuBar();

        //========"File"=========
        JMenu file = new JMenu("File");
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
            file.add(wmsCheckBox);
            
            // Imported Data Dialog
            importedDataCheckBox = new JCheckBoxMenuItem("Import Imagery & Elevations");
            importedDataCheckBox.setSelected(isImportedDataDialogOpen);
            importedDataCheckBox.addActionListener((ActionEvent event) ->
            {
                isImportedDataDialogOpen = !isImportedDataDialogOpen;
                if (isImportedDataDialogOpen)
                {
                    if (importedDataDialog == null)
                    {
                        importedDataDialog = new ImportedDataDialog(this.getWwd(), this);
                    }
                    importedDataDialog.setVisible(true);
                }
                else
                {
                    importedDataDialog.setVisible(false);
                }
            });
            file.add(importedDataCheckBox);
            
            // KML Measurement Export            
            exportMeasureTool = new JMenuItem("Export Measure Tool");
            exportMeasureTool.addActionListener((ActionEvent event) ->
            {
                try
                {
                    measureDialog.exportMeasureTool();
                } catch (XMLStreamException ex)
                {
                    Logger.getLogger(CelestialMapper.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex)
                {
                    Logger.getLogger(CelestialMapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            file.add(exportMeasureTool);
            
            // KML Importer
            kmlImporter = new ImportKML(this, this.getWwd(), file);
        }
        menuBar.add(file);
                    
        //======== "CMS Place Names" ========          
        cmsPlaceNamesMenu = new CMSPlaceNamesMenu(this, this.getWwd());
        menuBar.add(cmsPlaceNamesMenu);

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
                } else {
                    System.setProperty("gov.nasa.worldwind.stereo.mode", "");
                    Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);
                    Configuration.setValue(AVKey.INITIAL_LONGITUDE, 0);
                    Configuration.setValue(AVKey.INITIAL_ALTITUDE, 8e6);
                    Configuration.setValue(AVKey.INITIAL_HEADING, 0);
                    Configuration.setValue(AVKey.INITIAL_PITCH, 0);
                    Configuration.setValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Moon");
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
                    Configuration.setValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Moon");
                }
                restart();
            });
            view.add(flatGlobe);    
            
            //======== "Reset" =========
            reset = new JMenuItem("Reset");
            reset.setSelected(resetWindow);
            reset.addActionListener((ActionEvent event) ->
            {
                resetWindow = !resetWindow;
                if (resetWindow)
                {
                    Configuration.setValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Moon");
                    stereo = false;
                    flat = false;
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
            cmsLogo.setScreenLocation(new Point(view.x + 70, view.y + 70));
//            cmsLogo.setScreenLocation(new Point(view.x + 1000, view.y + 800));
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

    public LineOfSightController getLineOfSight()
    {
        return lineOfSight;
    }

    public void setLineOfSight(LineOfSightController lineOfSight)
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

    public MouseListener getMouseListener()
    {
        return mouseListener;
    }

    public void setMouseListener(MouseListener mouseListener)
    {
        this.mouseListener = mouseListener;
    }

    public CoordinatesDialog getCoordinatesDialog()
    {
        return this.coordinatesDialog;
    }

    public void setCoordinatesDialog(CoordinatesDialog coordinatesDialog)
    {
        this.coordinatesDialog = coordinatesDialog;
    }

    public ApolloDialog getLandingSites()
    {
        return apolloDialog;
    }

    public void setLandingSites(ApolloDialog dialog)
    {
        this.apolloDialog = dialog;
    }

    public void createNewWML(){
        this.wml = new WorldMapLayer();
        /*
         IMPORTANT - the constructor doesn't provide a name
         Which the layertree needs later to label the checkbox
        */
        this.wml.setName("Mini Map");
        this.wml.setIconFilePath("cms-data/icons/lunar_minimap_ldem_3_8bit.jpg");
        this.wml.setResizeBehavior(AVKey.RESIZE_STRETCH);

        // set location of minimap
        this.wml.setPosition(AVKey.NORTHEAST);

        // enable globe navigation by clicking the minimap
        this.getWwd().addSelectListener(new ClickAndGoSelectListener(this.getWwd(), WorldMapLayer.class));

        // add minimap to screen
        this.getWwd().getModel().getLayers().add(wml);
    }

    public void setWML(WorldMapLayer worldMapLayer)
    {
        this.wml = worldMapLayer;
    }

    public WorldMapLayer getWML()
    {
        return this.wml;
    }

    public void enableWML(boolean enable){
        this.wml.setEnabled(enable);
    }

    public CMSWWOUnitsFormat getUnits()
    {
        return this.unitsFormat;
    }

    public CoordinatesDisplay getCoordinatesDisplay()
    {
        return this.coordDisplay;
    }

    public void setCoordinatesDisplay(CoordinatesDisplay coordinatesDisplay)
    {
        this.coordDisplay = coordinatesDisplay;
    }


    public void setPointPlacemarkDialog(PointPlacemarkDialog pointPlacemarkDialog)
    {
        this.pointPlacemarkDialog = pointPlacemarkDialog;
    }

    public PointPlacemarkDialog getPointPlacemarkDialog()
    {
        return pointPlacemarkDialog;
    }

    public SearchPlacenamesDialog getSearchPlacenamesDialog()
    {
        return searchPlacenamesDialog;
    }

    public void setSearchPlacenamesDialog(SearchPlacenamesDialog searchPlacenamesDialog)
    {
        this.searchPlacenamesDialog = searchPlacenamesDialog;
    }

    public boolean isCoordinatesDialogOpen()
    {

        return isCoordinatesDialogOpen;
    }

    public void setCoordinatesDialogOpen(boolean coordinatesDialogOpen)
    {
        isCoordinatesDialogOpen = coordinatesDialogOpen;
    }

    public boolean isLineOfSightOpen()
    {
        return isLineOfSightOpen;
    }

    public void setLineOfSightOpen(boolean lineOfSightOpen)
    {
        isLineOfSightOpen = lineOfSightOpen;
    }

    public boolean isLandingSitesOpen()
    {
        return isLandingSitesOpen;
    }

    public void setLandingSitesOpen(boolean landingSitesOpen)
    {
        isLandingSitesOpen = landingSitesOpen;
    }

    public boolean isPointPlacemarkDialogOpen()
    {
        return isPointPlacemarkDialogOpen;
    }

    public void setPointPlacemarkDialogOpen(boolean pointPlacemarkDialogOpen)
    {
        isPointPlacemarkDialogOpen = pointPlacemarkDialogOpen;
    }

    public boolean isSearchPlacenamesDialogOpen()
    {
        return isSearchPlacenamesDialogOpen;
    }

    public void setSearchPlacenamesDialogOpen(
        boolean searchPlacenamesDialogOpen)
    {
        isSearchPlacenamesDialogOpen = searchPlacenamesDialogOpen;
    }
}
