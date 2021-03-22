/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.util.PanelTitle;
import gov.nasa.cms.util.ShadedPanel;
import gov.nasa.worldwind.WorldWindow;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import gov.nasa.worldwind.util.*;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 *
 * @author kjdickin
 */
public class ImportedDataPanel extends ShadedPanel
{
    private WorldWindow wwd;
    private CelestialMapper cms;
    protected JPanel dataConfigPanel;
    
    public ImportedDataPanel(WorldWindow wwd)
    {
        super(new BorderLayout());

        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.makePanel();
    }
    
    protected void makePanel()
    {
        this.add(new PanelTitle("Imported Data", SwingConstants.CENTER), BorderLayout.NORTH);

        this.dataConfigPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        this.dataConfigPanel.setOpaque(false);
        this.dataConfigPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right

        // Put the grid in a container to prevent scroll panel from stretching its vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.setOpaque(false);
        dummyPanel.add(this.dataConfigPanel, BorderLayout.NORTH);

        // Add the dummy panel to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // top, left, bottom, right

        // Add the scroll pane to a titled panel that will resize with the main window.
        JPanel bodyPanel = new JPanel(new GridLayout(0, 1, 0, 10)); // rows, cols, hgap, vgap
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(bodyPanel, BorderLayout.CENTER);
    }
    
}
