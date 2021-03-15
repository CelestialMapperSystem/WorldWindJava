/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.measure.*;
import gov.nasa.worldwindx.examples.util.LayerManagerLayer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.ArrayList;

/**
 * Example usage of MeasureTool to draw a shape on the globe and measure length,
 * area, etc. Click the "New" button, and then click and drag on the globe to
 * define a shape. The panel on the left shows the shape's measurement.
 *
 * @author Patrick Murris
 * @version $Id: MeasureToolUsage.java 2117 2014-07-01 20:36:49Z tgaskins $
 * @see gov.nasa.worldwind.util.measure.MeasureTool
 * @see gov.nasa.worldwind.util.measure.MeasureToolController
 * @see MeasureToolPanel
 */
public class MeasureToolUsage extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private int lastTabIndex = -1;
        private final JTabbedPane tabbedPane = new JTabbedPane();
        private final TerrainProfileLayer profile = new TerrainProfileLayer();
        private MeasureDialog measureDialog;

        public AppFrame() {
            super(true, false, false); // disable layer menu and statisics panel for AppFrame
            getWwd().getModel().getLayers().add(new LayerManagerLayer(getWwd())); // add layer box UI
            

            // Add measure tool control panel to tabbed pane
            final MeasureTool measureTool = new MeasureTool(this.getWwd());
            measureTool.setController(new MeasureToolController());

            // Attach to AppFrame's control panel
           // this.getControlPanel().add(tabbedPane, BorderLayout.EAST);
           // this.pack();
            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu toolsMenu = new JMenu("Panels");
            menuBar.add(toolsMenu);

            final WorldWindow wwd = this.getWwd();
            JMenuItem openMeasureDialogItem = new JMenuItem(new AbstractAction("Measurement") {
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        if (AppFrame.this.measureDialog == null) {
                            AppFrame.this.measureDialog = new MeasureDialog(wwd, measureTool, AppFrame.this);
                        }
                        AppFrame.this.measureDialog.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            toolsMenu.add(openMeasureDialogItem);
        }




    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Measure Tool", MeasureToolUsage.AppFrame.class);
    }

}
