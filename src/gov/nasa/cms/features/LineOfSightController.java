/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.*;
import gov.nasa.worldwind.WorldWindow;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author gknorman
 */
public class LineOfSightController {
    
    private JDialog dialog;
    private CMSLineOfSightPanel lineOfSightPanel;
    private CMSLineOfSight lineOfSightObject;
    private final WorldWindow wwd;
    private final CelestialMapper celestialMapper;

    public LineOfSightController(AppFrame celestialMapper, WorldWindow wwd) {
        
        this.wwd = wwd;
        this.celestialMapper = (CelestialMapper) celestialMapper;
        
        this.lineOfSightObject = new CMSLineOfSight(this.celestialMapper, this.wwd, this);
        
        createAndShowGui();
        
        SwingUtilities.invokeLater(() -> {
            //               createAndShowGui();
            lineOfSightObject.activate();
        });
    }
    
    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
    
    public final void createAndShowGui(){
        this.lineOfSightPanel = new CMSLineOfSightPanel(this.wwd, this.lineOfSightObject, this);
        
        dialog = new JDialog(this.celestialMapper);
        Rectangle bounds = this.celestialMapper.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Line of Sight Tool");
        dialog.getContentPane().add(lineOfSightPanel, BorderLayout.CENTER);
        
        // Set the location and resizable to true
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(true);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                celestialMapper.getLineOfSight().setVisible(false);
                celestialMapper.setLineOfSightOpen(false);

            }
        });

        dialog.pack();
    }

    public void updateProgressBar(int progress) {
        this.lineOfSightPanel.updateProgressBar(progress);
    }
    
    public void updateProgressBar(String progress) {
        this.lineOfSightPanel.updateProgressBar(progress);
    }

    boolean layersNotNull() {
        return this.lineOfSightObject.isLayersNotNull();
    }

    void toggleOrigin(boolean selected) {
        this.lineOfSightObject.toggleGridOrigin(selected);
    }

    void toggleGridLines(boolean selected) {
        this.lineOfSightObject.toggleGridLines(selected);
    }

    void toggleGridPoints(boolean selected) {
        this.lineOfSightObject.toggleGridPoints(selected);
    }

    void toggleIntersectionPoints(boolean selected) {
        this.lineOfSightObject.toggleIntersectionPoints(selected);
    }
    
    void toggleIntersectionSightLines(boolean selected) {
        this.lineOfSightObject.toggleIntersectionSightLines(selected);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 
    
}
