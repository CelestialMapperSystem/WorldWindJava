/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.cms.util.*;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {

    private Dimension canvasSize = new Dimension(1200, 800);

    protected AppPanel wwjPanel;
    protected JPanel controlPanel;

    public void initialize() {
        // Create the WorldWindow.
        this.wwjPanel = new AppPanel(this.canvasSize); //, includeStatusBar);
        this.wwjPanel.setPreferredSize(canvasSize);

        // Add the panel to the content pane
        this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
        
        // Create and install the view controls layer and register a controller for it with the WorldWindow.
        ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
        insertBeforeCompass(getWwd(), viewControlsLayer);
        this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));

        // Register a rendering exception listener that's notified when exceptions occur during rendering.
        this.wwjPanel.getWwd().addRenderingExceptionListener((Throwable t) -> {
            if (t instanceof WWAbsentRequirementException) {
                String message = "Computer does not meet minimum graphics requirements.\n";
                message += "Please install up-to-date graphics driver and try again.\n";
                message += "Reason: " + t.getMessage() + "\n";
                message += "This program will end when you press OK.";

                JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        });

        // Search the layer list for layers that are also select listeners and register them with the World
        // Window. This enables interactive layers to be included without specific knowledge of them here.
        for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers()) {
            if (layer instanceof SelectListener) {
                this.getWwd().addSelectListener((SelectListener) layer);
            }
        }

        this.pack();

        // Center the application on the screen.
        WWUtil.alignComponent(null, this, AVKey.CENTER);
        this.setResizable(true);
    }
    
    public Dimension getCanvasSize() {
        return canvasSize;
    }

    public AppPanel getWwjPanel() {
        return wwjPanel;
    }

    public WorldWindow getWwd() {
        return this.wwjPanel.getWwd();
    }

    public StatusBar getStatusBar() {
        return this.wwjPanel.getStatusBar();
    }

    /**
     * @deprecated Use getControlPanel instead.
     * @return This application's layer panel.
     */
    @Deprecated
    public JPanel getControlPanel() {
        return this.controlPanel;
    }

    public void setToolTipController(ToolTipController controller) {
        if (this.wwjPanel.toolTipController != null) {
            this.wwjPanel.toolTipController.dispose();
        }

        this.wwjPanel.toolTipController = controller;
    }

    public void setHighlightController(HighlightController controller) {
        if (this.wwjPanel.highlightController != null) {
            this.wwjPanel.highlightController.dispose();
        }

        this.wwjPanel.highlightController = controller;
    }

    public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof CompassLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    public static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    public static void insertAfterPlacenames(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just after the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition + 1, layer);
    }

    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName) {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l.getName().contains(targetName)) {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    static {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (Configuration.isWindowsOS()) {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }

    }
}
