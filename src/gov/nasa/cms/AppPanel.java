/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms;

import gov.nasa.cms.util.HighlightController;
import gov.nasa.cms.util.ToolTipController;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.util.StatusBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;

public class AppPanel extends JPanel {

    private WorldWindow wwd;
    protected StatusBar statusBar;
    protected ToolTipController toolTipController;
    protected HighlightController highlightController;

    public AppPanel(Dimension canvasSize) { //, boolean includeStatusBar) {
        super(new BorderLayout());

        this.wwd = this.createWorldWindow();
        ((Component) this.wwd).setPreferredSize(canvasSize);

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        this.add((Component) this.wwd, BorderLayout.CENTER);

        // TODO - Add option back in to tell constructor whether to include status bar instead of hard coding.
        //if (includeStatusBar) {
//        this.statusBar = new StatusBar();
//        this.add(statusBar, BorderLayout.PAGE_END);
//        this.statusBar.setEventSource(wwd);
        //}

        // Add controllers to manage highlighting and tool tips.
        this.toolTipController = new ToolTipController(this.getWwd(), AVKey.DISPLAY_NAME, null);
        this.highlightController = new HighlightController(this.getWwd(), SelectEvent.ROLLOVER);
    }

    protected WorldWindow createWorldWindow() {
        return new WorldWindowGLCanvas();
    }

    public WorldWindow getWwd() {
        return wwd;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

}
