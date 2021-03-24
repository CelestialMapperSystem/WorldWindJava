/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
    private JTextField latTextField;
    private JTextField lonTextField;
    private JTextField elevTextField;
    private JTextField labelTextField;
    private JTextField scaleTextField;
    private JButton labelColorButton;
    private JCheckBox showLabelCheck;
    private JButton addButton;
    private JButton viewButton;
    
    private PointPlacemark placemark;
    private RenderableLayer layer;
    private PointPlacemarkAttributes attrs;
    
    private double latLocation;
    private double lonLocation;
    private double elevLocation;
    

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
        placemark = new PointPlacemark(Position.fromDegrees(0, 0, 1e4));
        layer = new RenderableLayer();
        attrs = new PointPlacemarkAttributes();
        
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
                    attrs.setImageAddress("images/pushpins/plain-red.png");
                    break;
                case "Orange":
                    attrs.setImageAddress("images/pushpins/plain-orange.png");
                    break;
                case "Yellow":
                    attrs.setImageAddress("images/pushpins/plain-yellow.png");
                    break;
                case "Green":
                    attrs.setImageAddress("images/pushpins/plain-green.png");
                    break;
                case "Blue":
                    attrs.setImageAddress("images/pushpins/plain-blue.png");
                    break;
                case "Purple":
                    attrs.setImageAddress("images/pushpins/plain-purple.png");
                    break;
                case "White":
                    attrs.setImageAddress("images/pushpins/plain-white.png");
                    break;
                case "Black":
                    attrs.setImageAddress("images/pushpins/plain-black.png");
                    break;
                case "Gray":
                    attrs.setImageAddress("images/pushpins/plain-gray.png");
                    break;
                default:
                    break;
            }
        });
        colorPanel.add(colorCombo);

        //======== Coordinates Panel ========  
        JPanel coordinatesPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        coordinatesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        coordinatesPanel.add(new JLabel("Coordinates (lat, lon, elev):"));
        
        latTextField = new JTextField(5);
        latTextField.addActionListener(new java.awt.event.ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent event) 
            {
                String latInput = latTextField.getText();
                latLocation = Double.parseDouble(latInput);
            }
        });
        lonTextField = new JTextField(5);
        lonTextField.addActionListener(new java.awt.event.ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent event) 
            {
                String latInput = lonTextField.getText();
                lonLocation = Double.parseDouble(latInput);
            }
        });
        elevTextField = new JTextField(5);
        elevTextField.addActionListener(new java.awt.event.ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent event) 
            {
                String latInput = elevTextField.getText();
                elevLocation = Double.parseDouble(latInput);
            }
        });
        coordinatesPanel.add(latTextField);
        coordinatesPanel.add(lonTextField);
        coordinatesPanel.add(elevTextField);      

        //======== Label Panel ========  
        JPanel labelPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        labelPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        labelPanel.add(new JLabel("Label:"));
        labelTextField = new JTextField();
        labelTextField.addActionListener(new java.awt.event.ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent event) 
            {
                String labelInput = labelTextField.getText();
                placemark.setLabelText(labelInput);
            }
        });
        labelPanel.add(labelTextField);
              
        //======== Scale Panel ========  
        JPanel scalePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        scalePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        scalePanel.add(new JLabel("Scale (1 is default):"));
        scaleTextField = new JTextField();
        scaleTextField.addActionListener(new java.awt.event.ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent event) 
            {
               String scaleInput = scaleTextField.getText();
               double scale = Double.parseDouble(scaleInput);
               attrs.setScale(scale);
            }
        });
        scalePanel.add(scaleTextField);           

        //======== Check Boxes Panel ========  
        JPanel checkPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        showLabelCheck = new JCheckBox("Label");
        showLabelCheck.setSelected(true);
        showLabelCheck.addActionListener((ActionEvent event) ->
        {
            JCheckBox cb = (JCheckBox) event.getSource();
            wwd.redraw();
        });
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
            placemark.setAttributes(attrs);
            placemark.setPosition(Position.fromDegrees(latLocation, lonLocation, elevLocation));
            layer.addRenderable(placemark);
            getWwd().getModel().getLayers().add(layer);
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
