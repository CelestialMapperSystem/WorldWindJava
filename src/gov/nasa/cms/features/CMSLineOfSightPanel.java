/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.CMSLineOfSight;
import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * This class currently only provides one panel to interact with the
 * CMSLineOfSight class.
 *
 * @author gknorman
 */
public class CMSLineOfSightPanel extends JPanel {

    private WorldWindow wwd;
    private JDialog dialog;
    private JPanel mainPanel;
    private JComboBox<String> shapeCombo;
    private JCheckBox gridLines, gridPoints, intersectionPoints, intersectionLines,
            originPoint;
    protected JProgressBar progressBar;
    private final CMSLineOfSight lineOfSight;
    private LineOfSightController lineOfSightController;

//    public CMSLineOfSightPanel(WorldWindow wwd, CMSLineOfSight lineOfSightObject) {
//        super(new BorderLayout());
//        this.wwd = wwd;
//        this.lineOfSight = lineOfSightObject;
//        
//        mainPanel = new JPanel();
//        mainPanel.setOpaque(false);
//        this.makePanel(mainPanel);
//    }
    CMSLineOfSightPanel(WorldWindow wwd, CMSLineOfSight lineOfSightObject, LineOfSightController lineOfSightController) {
        super(new BorderLayout());
        this.wwd = wwd;
        this.lineOfSight = lineOfSightObject;
        this.lineOfSightController = lineOfSightController;

        mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        this.makePanel(mainPanel);
    }

    private void makePanel(JPanel mainPanel) {
        //======== Inner Panel ======== 
        JPanel shapePanel = new JPanel(new GridLayout(6, 1, 5, 5));
        shapePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        shapePanel.add(new JLabel("Show / Hide LOS Components"));
        this.originPoint = new JCheckBox("Origin Point");
        this.gridLines = new JCheckBox("Grid Lines");
        this.gridPoints = new JCheckBox("Grid Points");
        this.intersectionPoints = new JCheckBox("Intersection Points");
        this.intersectionLines = new JCheckBox("Lines to Intersections");

        // Display a progress bar.
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setBorder(new EmptyBorder(0, 10, 0, 10));
        this.progressBar.setBorderPainted(false);
        this.progressBar.setStringPainted(true);
        // this.layerPanel.add(this.progressBar, BorderLayout.SOUTH);  //this line causes an error due to different CMS layer panel - twchoi

        SwingUtilities.invokeLater(() -> {
            shapePanel.add(originPoint);
            shapePanel.add(gridLines);
            shapePanel.add(gridPoints);
            shapePanel.add(intersectionPoints);
            shapePanel.add(intersectionLines);
            this.add(progressBar, BorderLayout.SOUTH); // 
        });

        SwingUtilities.invokeLater(() -> {
            addLosListener(originPoint, "toggleGrid");
            addLosListener(gridLines, "togglesightLines");
            addLosListener(gridPoints, "toggleGrid");
            addLosListener(intersectionPoints, "togglesIntersections");
            addLosListener(intersectionLines, "togglesIntersections");
        });

        // Iterate through all components in shapePanel to set the checkboxes
        // to selected by default
        for (Component checkbox : shapePanel.getComponents()) {
            if (checkbox instanceof JCheckBox) {
                ((JCheckBox) checkbox).setSelected(true);
            }
        } 

        //======== Outer Panel ======== 
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        // Add the border padding in the dialog
        outerPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), new TitledBorder("Measure")));
        outerPanel.setToolTipText("Measure tool control and info");
        outerPanel.add(shapePanel);
        this.add(outerPanel, BorderLayout.NORTH);

    } // end makePanel();

    //     private void createAndShowGui() {
//      this.controlPanel = new CMSLineOfSightPanel(this.getWwd());
//      this.controlPanel.getGridLines().addItemListener(((e) -> {
//          if (e.getStateChange() == ItemEvent.SELECTED){
//                    this.showGrid(grid, referencePosition);
//                }
//                else{
//                    this.gridLayer.removeAllRenderables();
//                    // Apparently have to call .redraw() to "remove" gridlayer
//                    // from the view
//                    
//                    this.getWwd().redraw();
//                }
//      }));
//        
    // Attempting use of Reflection to avoid naming each and every single
    // actionlistener that needs to be added to the checkboxes
    private void addLosListener(JCheckBox box, String feature) {
        // This stores the method reference if "feature" matches the name
        // of a function inside of lineOfSightObject
        final Method method;
        try {
            // This calls the name of the funciton inside of CMSLIneOfSight.java
            // and passes true or false, which toggles showing the layer or hiding.
            method = this.lineOfSight.getClass().getDeclaredMethod(feature, boolean.class);
            box.addItemListener(((e) -> {
                if (e.getStateChange() == ItemEvent.SELECTED && this.lineOfSight.isLayersNotNull()) {

                    try {
                        method.invoke(lineOfSight, true);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(CMSLineOfSightPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        method.invoke(lineOfSight, false);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(CMSLineOfSightPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }));
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(CMSLineOfSightPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the wwd
     */
    public WorldWindow getWwd() {
        return wwd;
    }

    /**
     * @param wwd the wwd to set
     */
    public void setWwd(WorldWindow wwd) {
        this.wwd = wwd;
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void updateProgressBar(int progress) {
        this.progressBar.setValue(progress);
    }

    public void updateProgressBar(String update) {
        this.progressBar.setString(update);
    }

    public JDialog getDialog() {
        return dialog;
    }

    public void setDialog(JDialog dialog) {
        this.dialog = dialog;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public JComboBox<String> getShapeCombo() {
        return shapeCombo;
    }

    public void setShapeCombo(JComboBox<String> shapeCombo) {
        this.shapeCombo = shapeCombo;
    }

    public JCheckBox getGridLines() {
        return gridLines;
    }

    public void setGridLines(JCheckBox gridLines) {
        this.gridLines = gridLines;
    }

    public JCheckBox getGridPoints() {
        return gridPoints;
    }

    public void setGridPoints(JCheckBox gridPoints) {
        this.gridPoints = gridPoints;
    }

    public JCheckBox getIntersectionPoints() {
        return intersectionPoints;
    }

    public void setIntersectionPoints(JCheckBox intersectionPoints) {
        this.intersectionPoints = intersectionPoints;
    }

    public JCheckBox getIntersectionLines() {
        return intersectionLines;
    }

    public void setIntersectionLines(JCheckBox intersectionLines) {
        this.intersectionLines = intersectionLines;
    }

    public JCheckBox getOriginPoint() {
        return originPoint;
    }

    public void setOriginPoint(JCheckBox originPoint) {
        this.originPoint = originPoint;
    }

}
