/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.AppFrame;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;

/**
 * Illustrates how to import ESRI Shapefiles into CelestialMapper. This uses a <code>{@link ShapefileLayerFactory}</code> to
 * parse a shapefile's contents and convert the moon nomenclature shapefile into a lunar place Names menu in CelestialMapper.
 * 
 * @author Tyler Choi
 * @version $Id: CMSPlaceNamesMenu.java 1171 2020-07-21 21:45:02Z twchoi $
 */
public class CMSPlaceNamesMenu extends JMenu implements ShapefileRenderable.AttributeDelegate
{
    private WorldWindow wwd;
    private boolean isItemEnabled;

    public CMSPlaceNamesMenu(AppFrame cms, WorldWindow Wwd)
    {   
        super("Place Names");      
        
        ShapefileLayerFactory factory = new ShapefileLayerFactory();

        try {
            InputStream is = WWIO.openFileOrResourceStream("images/yellow-dot.png", this.getClass());
            BufferedImage placemarkImage = ImageIO.read(is);
            PointPlacemarkAttributes placeMarkAttrs = new PointPlacemarkAttributes();
            placeMarkAttrs.setImage(placemarkImage);
            factory.setNormalPointAttributes(placeMarkAttrs);
            factory.setAttributeDelegate(this);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        String[] shapeFiles = {
            "cms-data/placenames/Oceanus/Oceanus.shp",
            "cms-data/placenames/Mare/Mare.shp", 
            "cms-data/placenames/Mons/Mons.shp", 
            "cms-data/placenames/Catena/Catena.shp",
            "cms-data/placenames/Vallis/Vallis.shp",
            "cms-data/placenames/Crater/Crater.shp",
            "cms-data/placenames/Dorsum/Dorsum.shp",
            "cms-data/placenames/Lacus/Lacus.shp",
            "cms-data/placenames/Sinus/Sinus.shp",
            "cms-data/placenames/Rima/Rima.shp",
            "cms-data/placenames/Promontorium/Promontorium.shp",
            "cms-data/placenames/Palus/Palus.shp",
            "cms-data/placenames/Rupes/Rupes.shp",
            "cms-data/placenames/Landing Site/LandingSiteName.shp",
            "cms-data/placenames/Planitia/Planitia.shp",
            "cms-data/placenames/Albedo/Albedo.shp",
            "cms-data/placenames/Satellite/Satellite.shp",
        };

        ShapefileLayerFactory.CompletionCallback callBack = new ShapefileLayerFactory.CompletionCallback() {
            @Override
            public void completion(Object result) {
                final Layer layer = (Layer) result; // the result is the layer the factory created
                String layerName = layer.getName();
                layerName = layerName.substring(0, layerName.length()-4);
                layer.setName(WWIO.getFilename(layerName));

                // Add the layer to the WorldWindow's layer list.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {                       
                        if (isItemEnabled) {
                            Wwd.getModel().getLayers().add(layer);       
                        } 
                    }
                });
            }

            @Override
            public void exception(Exception e) {
                Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            }
        };
        for (String shapeFile : shapeFiles) 
        {
            // Edit shapefile names to display feature name
            String placeName = shapeFile.substring(shapeFile.lastIndexOf('/') + 1, shapeFile.lastIndexOf('.'));
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(placeName);
            mi.addActionListener(new ActionListener()
            {               
                public void actionPerformed(ActionEvent event)
                {
                    
                    // Enable and disable when clicked 
                    isItemEnabled = ((JCheckBoxMenuItem) event.getSource()).getState();
               
                    if (isItemEnabled) {
                        // Load the shapefile. Define the completion callback.                       
                        factory.createFromShapefileSource(shapeFile, callBack);
                    }
                    else {
                        String chosenName = event.getActionCommand();
                        Layer selectedLayer = Wwd.getModel().getLayers().getLayerByName(chosenName);

                        // Remove chosen layers when disabled
                        switch(chosenName) {
                            case "Oceanus":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                Wwd.redraw();
                                break;
                            case "Mare":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Mons":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Catena":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Vallis":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Crater":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Dorsum":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Lacus":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Sinus":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Rima":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Promontorium":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Palus":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Rupes":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "LandingSiteName":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Planitia":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Albedo":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                            case "Satellite":
                                Wwd.getModel().getLayers().remove(selectedLayer);
                                break;
                        }
                    }        
                    doClick(0); // keep layer menu open
                }
            });
            
            this.add(mi);          
        }
    }
    @Override
    public void assignAttributes(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord) {
    }

    @Override
    public void assignRenderableAttributes(ShapefileRecord shapefileRecord, Renderable renderable) {
        if (isItemEnabled) {
            DBaseRecord attrs = shapefileRecord.getAttributes();
            String label = attrs.getValue("name").toString();
            if (renderable instanceof PointPlacemark) {
                PointPlacemark pp = (PointPlacemark) renderable;
                pp.setLabelText(label);
                pp.setEnableDecluttering(true);
            }
        }       
    }
    public WorldWindow getWwd()
    {
        return wwd;
    }

    public void setWwd(WorldWindow wwd)
    {

    }
    
}