/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Measure Tool control panel for <code>{@link gov.nasa.cms.features.MeasureDialog}</code>.
 * Allows users to pick from 8 different shape drawing options. Also has the ability
 * to export Measure Tool statistics to a CSV file.
 *
 * @see gov.nasa.worldwind.util.measure.MeasureTool
 * @author kjdickin
 */
public class CMSPointPlacemarkPanel extends JPanel
{

    private WorldWindow wwd;

    private JComboBox colorCombo;
    private JTextField coordinatesTextField;
    private JTextField labelTextField;
    private JTextField scaleTextField;
    private JButton lineColorButton;
    private JButton pointColorButton;
    private JButton labelColorButton;
    private JCheckBox showControlsCheck;
    private JCheckBox showLabelCheck;
    private JButton addButton;
    private JButton viewButton;
    private JButton endButton;
    private JCheckBox followCheck;
    private JButton deleteButton;
    private JLabel[] pointLabels;
    private JLabel lengthLabel;
    private JLabel areaLabel;
    private JLabel widthLabel;
    private JLabel heightLabel;
    private JLabel headingLabel;
    private JLabel centerLabel;



    public CMSPointPlacemarkPanel(WorldWindow wwdObject)
    {
        super(new BorderLayout());
        this.wwd = wwdObject;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        this.makePanel(mainPanel);     
    }

    private void makePanel(JPanel panel)
    {

        //======== Measurement Panel ========  
        JPanel colorPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        colorPanel.add(new JLabel("Color:"));
        colorCombo = new JComboBox<>(new String[]
        {
            "Red","Orange", "Yellow", "Green", "Blue", "Purple", "White", "Black", "Gray"
        });
        colorCombo.addActionListener((ActionEvent event) ->
        {
            String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
            switch (item)
            {
                case "Red":
                    break;
                case "Orange":
                    break;
                case "Yellow":
                    break;
                case "Green":
                    break;
                case "Blue":
                    break;
                case "Purple":
                    break;
                case "White":
                    break;
                case "Black":
                    break;
                case "Gray":
                    break;
                default:
                    break;
            }
        });
        colorPanel.add(colorCombo);

        //======== Coordinates Panel ========  
        JPanel coordinatesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        coordinatesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        coordinatesPanel.add(new JLabel("Coordinates (lat, lon):"));
        coordinatesTextField = new JTextField();
        coordinatesPanel.add(coordinatesTextField);

        //======== Label Panel ========  
        JPanel labelPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        labelPanel.add(new JLabel("Label:"));
        labelTextField = new JTextField();
        labelPanel.add(labelTextField);

        //======== Scale Panel ========  
        JPanel scalePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        scalePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        scalePanel.add(new JLabel("Scale (1 is default):"));
        scaleTextField = new JTextField();
        scalePanel.add(scaleTextField);

        //======== Check Boxes Panel ========  
        JPanel checkPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        showLabelCheck = new JCheckBox("Label");
        showLabelCheck.addActionListener((ActionEvent event) ->
        {
            JCheckBox cb = (JCheckBox) event.getSource();
            wwd.redraw();
        });
        showLabelCheck.setSelected(true);
        checkPanel.add(showLabelCheck);

        //======== Label Color Button ========  
        labelColorButton = new JButton("Label");
        labelColorButton.addActionListener((ActionEvent event) ->
        {
            Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null)
            {
                ((JButton) event.getSource()).setBackground(c);
            }
        });
        colorPanel.add(labelColorButton);

        //======== Action Buttons ========  
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        addButton = new JButton("Add Placemark");
        addButton.addActionListener((ActionEvent actionEvent) ->
        {

        });
        buttonPanel.add(addButton);
        addButton.setEnabled(true);

        viewButton = new JButton("View");
        viewButton.addActionListener((ActionEvent actionEvent) ->
        {
        });
        buttonPanel.add(viewButton);
        viewButton.setEnabled(false);


        //======== Outer Panel ======== 
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        // Add the border padding in the dialog
        outerPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), new TitledBorder("Point Placemarks")));
        outerPanel.setToolTipText("Create point placemarks on the globe");
        outerPanel.add(colorPanel);
        outerPanel.add(colorPanel);
        outerPanel.add(coordinatesPanel);
        outerPanel.add(labelPanel);
        outerPanel.add(scalePanel);
        outerPanel.add(checkPanel);
        outerPanel.add(buttonPanel);

        this.add(outerPanel, BorderLayout.NORTH);
    }
    
    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public void setWwd(WorldWindow wwd)
    {
        this.wwd = wwd;
    }
}
