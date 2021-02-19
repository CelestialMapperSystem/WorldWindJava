/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.*;
import java.io.File;

/**
 * Imports the lunar elevation model for CelestialMapper.java This imports a
 * down-sampled GeoTIFF file containing elevation data and creates an
 * <code>{@link gov.nasa.worldwind.globes.LocalElevationModel}</code> for it.
 *
 * @author kjdickin
 */
public class MoonElevationModel extends CelestialMapper {

    // The data to import.
    protected static final String ELEVATIONS_PATH = "cms-data/lunar-dem.tif";
    private WorldWindow wwd;

    public MoonElevationModel(WorldWindow wwd) {
        // Import the elevation on a new thread to avoid freezing the UI
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    // Download the data and save it in a File
                    File sourceFile = new File(ELEVATIONS_PATH);
                    // Create a local elevation model from the data.
                    LocalElevationModel elevationModel = new LocalElevationModel();
                    elevationModel.addElevations(sourceFile);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {

                            // Get the WorldWindow's current elevation model.
                            wwd.getModel().getGlobe().setElevationModel(elevationModel);
                            
                            // Set the view to look at the imported elevations, although they might be hard to detect. To
                            // make them easier to detect, replace the globe's CompoundElevationModel with the new elevation
                            // model rather than adding it.
//                            Sector modelSector = elevationModel.getSector();
//                            ExampleUtil.goTo(wwd, modelSector);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }

    public WorldWindow getWwd() {
        return this.wwd;
    }

    public void setWwd(WorldWindow wwd) {
        this.wwd = wwd;
    }
}
