/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.AppFrame;
import gov.nasa.cms.CMSLineOfSight;
import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.measure.MeasureTool;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author gknorman
 */
public class LineOfSightController {
    
    private JDialog dialog;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private int lastTabIndex = -1;
    private CMSLineOfSightPanel lineOfSightPanel;
    private CMSLineOfSight lineOfSightObject;
    private WorldWindow wwd;
    private CelestialMapper cms;

    public LineOfSightController(AppFrame cms, WorldWindow wwd) {
        
        this.wwd = wwd;
        this.cms = (CelestialMapper) cms;
        
        this.lineOfSightObject = new CMSLineOfSight(this.cms, this.wwd, this);
        
        createAndShowGui();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//               createAndShowGui();
                lineOfSightObject.activate();
            }
        });
    }
    
    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
    
    public final void createAndShowGui(){
        this.lineOfSightPanel = new CMSLineOfSightPanel(this.wwd, this.lineOfSightObject, this);
        
        dialog = new JDialog((Frame) this.cms);     
        Rectangle bounds = this.cms.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Line of Sight Tool");
        // Set the location and resizable to true
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(true);
        dialog.getContentPane().add(lineOfSightPanel, BorderLayout.CENTER);
        dialog.pack();
    }

    public void updateProgressBar(int progress) {
        this.lineOfSightPanel.updateProgressBar(progress);
    }
    
    public void updateProgressBar(String progress) {
        this.lineOfSightPanel.updateProgressBar(progress);
    }
    
    
    
    
 
    
}
