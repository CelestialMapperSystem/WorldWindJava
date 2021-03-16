/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.util.measure.MeasureTool;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Measure Tool control panel for <code>{@link gov.nasa.cms.features.MeasureDialog}</code>.
 * Allows users to pick from 8 different shape drawing options. Also has the ability
 * to export Measure Tool statistics to a CSV file.
 *
 * @see gov.nasa.worldwind.util.measure.MeasureTool
 * @author kjdickin
 */
public class CMSMeasurePanel extends JPanel
{

    private WorldWindow wwd;
    private MeasureTool measureTool;
    private PropertyChangeListener measureToolListener;

    private JComboBox shapeCombo;
    private JComboBox pathTypeCombo;
    private JComboBox unitsCombo;
    private JComboBox anglesCombo;
    private JButton lineColorButton;
    private JButton pointColorButton;
    private JButton annotationColorButton;
    private JCheckBox showControlsCheck;
    private JCheckBox showAnnotationCheck;
    private JButton newButton;
    private JButton pauseButton;
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

    private static final ArrayList<Position> LINE = new ArrayList<>();
    private static final ArrayList<Position> PATH = new ArrayList<>();
    private static final ArrayList<Position> POLYGON = new ArrayList<>();

    static
    {
        LINE.add(Position.fromDegrees(44, 7, 0));
        LINE.add(Position.fromDegrees(45, 8, 0));

        PATH.addAll(LINE);
        PATH.add(Position.fromDegrees(46, 6, 0));
        PATH.add(Position.fromDegrees(47, 5, 0));
        PATH.add(Position.fromDegrees(45, 6, 0));

        POLYGON.addAll(PATH);
        POLYGON.add(Position.fromDegrees(44, 7, 0));
    }

    public CMSMeasurePanel(WorldWindow wwdObject, MeasureTool measureToolObject)
    {
        super(new BorderLayout());
        this.wwd = wwdObject;
        this.measureTool = measureToolObject;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        this.makePanel(mainPanel);

        // Always follow the terrain
        measureTool.setFollowTerrain(true);

        // Handle measure tool events
        measureTool.addPropertyChangeListener((PropertyChangeEvent event) -> {
            // Add, remove or change positions
            if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE)) {
                fillPointsPanel();    // Update position list when changed
            } // The tool was armed / disarmed
            else if (event.getPropertyName().equals(MeasureTool.EVENT_ARMED)) {
                if (measureTool.isArmed()) {
                    newButton.setEnabled(false);
                    pauseButton.setText("Pause");
                    pauseButton.setEnabled(true);
                    endButton.setEnabled(true);
                    ((Component) wwd).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    newButton.setEnabled(true);
                    pauseButton.setText("Pause");
                    pauseButton.setEnabled(false);
                    endButton.setEnabled(false);
                    ((Component) wwd).setCursor(Cursor.getDefaultCursor());
                }

            } // Metric changed - sent after each render frame
            else if (event.getPropertyName().equals(MeasureTool.EVENT_METRIC_CHANGED)) {
                updateMetric();
            }
        });
    }

    private void makePanel(JPanel panel)
    {

        //======== Measurement Panel ========  
        JPanel shapePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        shapePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        shapePanel.add(new JLabel("Measurement Type:"));
        shapeCombo = new JComboBox<>(new String[]
        {
            "Line", "Path", "Polygon", "Circle", "Ellipse", "Square", "Rectangle", "Freehand"
        });
        shapeCombo.addActionListener((ActionEvent event) ->
        {
            String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
            // Make sure Freehand isn't enabled for Path unless selected
            measureTool.getController().setFreeHand(false);
            wwd.redraw();
            switch (item)
            {
                case "Line":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_LINE);
                    break;
                case "Path":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_PATH);
                    break;
                case "Polygon":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
                    break;
                case "Circle":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_CIRCLE);
                    break;
                case "Ellipse":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_ELLIPSE);
                    break;
                case "Square":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_SQUARE);
                    break;
                case "Rectangle":
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_QUAD);
                    break;
                case "Freehand":
                    // Enable Freehand for Path
                    measureTool.setMeasureShapeType(MeasureTool.SHAPE_PATH);
                    measureTool.getController().setFreeHand(true);
                    wwd.redraw();
                default:
                    break;
            }
        });
        shapePanel.add(shapeCombo);

        //======== Path Type Panel ========  
        JPanel pathTypePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        pathTypePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pathTypePanel.add(new JLabel("Path type:"));
        pathTypeCombo = new JComboBox<>(new String[]
        {
            "Linear", "Rhumb", "Great circle"
        });
        pathTypeCombo.setSelectedIndex(2);
        pathTypeCombo.addActionListener((ActionEvent event) ->
        {
            String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
            switch (item)
            {
                case "Linear":
                    measureTool.setPathType(AVKey.LINEAR);
                    break;
                case "Rhumb":
                    measureTool.setPathType(AVKey.RHUMB_LINE);
                    break;
                case "Great circle":
                    measureTool.setPathType(AVKey.GREAT_CIRCLE);
                    break;
                default:
                    break;
            }
        });
        pathTypePanel.add(pathTypeCombo);

        //======== Units Panel ========  
        JPanel unitsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        unitsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        unitsPanel.add(new JLabel("Units:"));
        unitsCombo = new JComboBox<>(new String[]
        {
            "M/M\u00b2", "KM/KM\u00b2", "KM/Hectare", "Feet/Feet\u00b2",
            "Miles/Miles\u00b2", "Nm/Miles\u00b2", "Yards/Acres"
        });
        unitsCombo.setSelectedItem("KM/KM\u00b2");
        unitsCombo.addActionListener((ActionEvent event) ->
        {
            String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
            switch (item)
            {
                case "M/M\u00b2":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.METERS);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_METERS);
                    break;
                case "KM/KM\u00b2":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.KILOMETERS);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_KILOMETERS);
                    break;
                case "KM/Hectare":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.KILOMETERS);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.HECTARE);
                    break;
                case "Feet/Feet\u00b2":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.FEET);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_FEET);
                    break;
                case "Miles/Miles\u00b2":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.MILES);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_MILES);
                    break;
                case "Nm/Miles\u00b2":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.NAUTICAL_MILES);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_MILES);
                    break;
                case "Yards/Acres":
                    measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.YARDS);
                    measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.ACRE);
                    break;
                default:
                    break;
            }
        });
        unitsPanel.add(unitsCombo);

        //======== Angles Panel ========  
        JPanel anglesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        anglesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        anglesPanel.add(new JLabel("Angle Format:"));
        anglesCombo = new JComboBox<>(new String[]
        {
            "DD", "DMS"
        });
        anglesCombo.setSelectedItem("DD");
        anglesCombo.addActionListener((ActionEvent event) ->
        {
            String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
            measureTool.getUnitsFormat().setShowDMS(item.equals("DMS"));
        });
        anglesPanel.add(anglesCombo);

        //======== Check Boxes Panel ========  
        JPanel checkPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        showControlsCheck = new JCheckBox("Control points");
        showControlsCheck.setSelected(measureTool.isShowControlPoints());
        showControlsCheck.addActionListener((ActionEvent event) ->
        {
            JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.setShowControlPoints(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(showControlsCheck);

        showAnnotationCheck = new JCheckBox("Statistics");
        showAnnotationCheck.setSelected(measureTool.isShowAnnotation());
        showAnnotationCheck.addActionListener((ActionEvent event) ->
        {
            JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.setShowAnnotation(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(showAnnotationCheck);
        
        followCheck = new JCheckBox("Follow terrain");
        followCheck.setSelected(true); // start off selected
        followCheck.addActionListener((ActionEvent event) -> {
            JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.setFollowTerrain(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(followCheck);

        //======== Color Buttons ========  
        final JPanel colorPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        lineColorButton = new JButton("Line");
        lineColorButton.addActionListener((ActionEvent event) ->
        {
            Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null)
            {
                ((JButton) event.getSource()).setBackground(c);
                measureTool.setLineColor(c);
                Color fill = new Color(c.getRed() / 255f * .5f,
                        c.getGreen() / 255f * .5f, c.getBlue() / 255f * .5f, .5f);
                measureTool.setFillColor(fill);
            }
        });
        colorPanel.add(lineColorButton);
        lineColorButton.setBackground(measureTool.getLineColor());

        pointColorButton = new JButton("Points");
        pointColorButton.addActionListener((ActionEvent event) ->
        {
            Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null)
            {
                ((JButton) event.getSource()).setBackground(c);
                measureTool.getControlPointsAttributes().setBackgroundColor(c);
            }
        });
        colorPanel.add(pointColorButton);
        pointColorButton.setBackground(measureTool.getControlPointsAttributes().getBackgroundColor());

        annotationColorButton = new JButton("Tooltip");
        annotationColorButton.addActionListener((ActionEvent event) ->
        {
            Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null)
            {
                ((JButton) event.getSource()).setBackground(c);
                measureTool.getAnnotationAttributes().setTextColor(c);
            }
        });
        annotationColorButton.setBackground(measureTool.getAnnotationAttributes().getTextColor());
        colorPanel.add(annotationColorButton);

        //======== Action Buttons ========  
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        newButton = new JButton("New");
        newButton.addActionListener((ActionEvent actionEvent) ->
        {
            measureTool.clear();
            measureTool.setArmed(true);

        });
        buttonPanel.add(newButton);
        newButton.setEnabled(true);

        pauseButton = new JButton("Pause");
        pauseButton.addActionListener((ActionEvent actionEvent) ->
        {
            measureTool.setArmed(!measureTool.isArmed());
            pauseButton.setText(!measureTool.isArmed() ? "Resume" : "Pause");
            pauseButton.setEnabled(true);
            ((Component) wwd).setCursor(!measureTool.isArmed() ? Cursor.getDefaultCursor()
                    : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        });
        buttonPanel.add(pauseButton);
        pauseButton.setEnabled(false);

        endButton = new JButton("End");
        endButton.addActionListener((ActionEvent actionEvent) ->
        {
            measureTool.setArmed(false);
        });
        buttonPanel.add(endButton);
        endButton.setEnabled(false);

        // In process feature
//        deleteButton = new JButton("Clear");
//        deleteButton.addActionListener((ActionEvent actionEvent) ->
//        {
//            // Remove all shapes and control points from the AppFrame
//            measureTool.clear();
//            // Remove Measure Tool layers from the WorldWindow
//           // deletePanel();
//        });
//        buttonPanel.add(deleteButton);
//        deleteButton.setEnabled(true);

        //======== Point Labels ========   
        JPanel pointPanel = new JPanel();
        this.pointLabels = new JLabel[100];
        for (int i = 0; i < this.pointLabels.length; i++)
        {
            this.pointLabels[i] = new JLabel("");
            pointPanel.add(this.pointLabels[i]);
        }

        //======== Metric Panel ========     
        JPanel metricPanel = new JPanel(new GridLayout(0, 2, 0, 4));
        metricPanel.setBorder(new CompoundBorder(
                new TitledBorder("Metric"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        metricPanel.add(new JLabel("Length:"));
        lengthLabel = new JLabel();
        metricPanel.add(lengthLabel);
        metricPanel.add(new JLabel("Area:"));
        areaLabel = new JLabel();
        metricPanel.add(areaLabel);
        metricPanel.add(new JLabel("Width:"));
        widthLabel = new JLabel();
        metricPanel.add(widthLabel);
        metricPanel.add(new JLabel("Height:"));
        heightLabel = new JLabel();
        metricPanel.add(heightLabel);
        metricPanel.add(new JLabel("Heading:"));
        headingLabel = new JLabel();
        metricPanel.add(headingLabel);
        metricPanel.add(new JLabel("Center:"));
        centerLabel = new JLabel();
        metricPanel.add(centerLabel);
        
        //======== Export as CSV ========    
        JButton exportButton = new JButton("Export as CSV");
        exportButton.addActionListener((ActionEvent actionEvent) ->
        {             
            JFrame parentFrame = new JFrame(); // Create the frame for the save dialog

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");

            int userSelection = fileChooser.showSaveDialog(parentFrame); // Show the JFileChoose in the popup frame

            // If user approves, set the file save location and start writing to CSV file
            if (userSelection == JFileChooser.APPROVE_OPTION)
            {
                File fileToSave = fileChooser.getSelectedFile();
                try ( PrintWriter writer = new PrintWriter(fileToSave))
                {
                    StringBuilder sb = new StringBuilder();

                    double length = measureTool.getLength();
                    double area = measureTool.getArea();
                    double width = measureTool.getWidth();
                    double height = measureTool.getHeight();
                    Angle heading = measureTool.getOrientation();
                    Position center = measureTool.getCenterPosition();

                    // Header
                    sb.append("Length,");
                    sb.append("Area,");
                    sb.append("Width,");
                    sb.append("Height,");
                    sb.append("Heading,");
                    sb.append("Center");
                    sb.append('\n');

                    // Length
                    String s = String.format("%7.1f m", length);
                    sb.append(s);
                    sb.append(',');

                    // Area
                    if (area < 0)
                    {
                        s = "na";
                    } else
                    {
                        s = String.format("%7.1f km2", area);
                    }
                    sb.append(s);
                    sb.append(',');

                    // Width
                    if (width < 0)
                    {
                        s = "na";
                    } else
                    {
                        s = String.format("%7.1f km", width);
                    }
                    sb.append(s);
                    sb.append(',');

                    // Height
                    if (height < 0)
                    {
                        s = "na";
                    } else
                    {
                        s = String.format("%7.1f km", height);
                    }
                    sb.append(s);
                    sb.append(',');

                    // Heading
                    if (heading == null)
                    {
                        s = "na";
                    } else
                    {
                        s = String.format("%6.2f", heading.degrees);
                    }
                    sb.append(s);
                    sb.append(',');

                    // Center
                    if (center == null)
                    {
                        s = "na";
                    } else
                    {
                        s = String.format("%,7.4f %,7.4f", center.getLatitude().degrees, center.getLongitude().degrees);
                    }

                    sb.append(s);
                    sb.append('\n');

                    writer.write(sb.toString());

                    System.out.println("File has been exported");
                } catch (FileNotFoundException ex)
                {
                    Logger.getLogger(CMSMeasurePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        metricPanel.add(exportButton);

        //======== Outer Panel ======== 
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        // Add the border padding in the dialog
        outerPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), new TitledBorder("Measure")));
        outerPanel.setToolTipText("Measure tool control and info");
        outerPanel.add(colorPanel);
        outerPanel.add(shapePanel);
        outerPanel.add(pathTypePanel);
        outerPanel.add(unitsPanel);
        outerPanel.add(anglesPanel);
        outerPanel.add(checkPanel);
        outerPanel.add(buttonPanel);
        outerPanel.add(metricPanel);

        this.add(outerPanel, BorderLayout.NORTH);
    }

    // Updates the position list when the measute tool changes
    private void fillPointsPanel()
    {
        int i = 0;
        if (measureTool.getPositions() != null)
        {
            for (LatLon pos : measureTool.getPositions())
            {
                if (i == this.pointLabels.length)
                {
                    break;
                }

                String las = String.format("Lat %7.4f\u00B0", pos.getLatitude().getDegrees());
                String los = String.format("Lon %7.4f\u00B0", pos.getLongitude().getDegrees());
                pointLabels[i++].setText(las + "  " + los);
            }
        }
        // Clear remaining labels
        for (; i < this.pointLabels.length; i++)
        {
            pointLabels[i].setText("");
        }

    }
    // Updates the labels when the metric has changed
    private void updateMetric()
    {
        // Update length label
        double value = measureTool.getLength();
        String s;
        if (value <= 0)
        {
            s = "na";
        } else if (value < 1000)
        {
            s = String.format("%,7.1f m", value);
        } else
        {
            s = String.format("%,7.3f km", value / 1000);
        }
        lengthLabel.setText(s);

        // Update area label
        value = measureTool.getArea();
        if (value < 0)
        {
            s = "na";
        } else if (value < 1e6)
        {
            s = String.format("%,7.1f m2", value);
        } else
        {
            s = String.format("%,7.3f km2", value / 1e6);
        }
        areaLabel.setText(s);

        // Update width label
        value = measureTool.getWidth();
        if (value < 0)
        {
            s = "na";
        } else if (value < 1000)
        {
            s = String.format("%,7.1f m", value);
        } else
        {
            s = String.format("%,7.3f km", value / 1000);
        }
        widthLabel.setText(s);

        // Update height label
        value = measureTool.getHeight();
        if (value < 0)
        {
            s = "na";
        } else if (value < 1000)
        {
            s = String.format("%,7.1f m", value);
        } else
        {
            s = String.format("%,7.3f km", value / 1000);
        }
        heightLabel.setText(s);

        // Update heading label
        Angle angle = measureTool.getOrientation();
        if (angle != null)
        {
            s = String.format("%,6.2f\u00B0", angle.degrees);
        } else
        {
            s = "na";
        }
        headingLabel.setText(s);

        // Update center label
        Position center = measureTool.getCenterPosition();
        if (center != null)
        {
            s = String.format("%,7.4f\u00B0 %,7.4f\u00B0", center.getLatitude().degrees, center.getLongitude().degrees);
        } else
        {
            s = "na";
        }
        centerLabel.setText(s);
    }

    public void deletePanel()
    {
        this.disposeCurrentMeasureTool();
    }

     public void clearPanel()
    {
        this.disposeCurrentMeasureTool();
        this.shapeCombo.setSelectedItem(LINE);
    }
    protected void disposeCurrentMeasureTool()
    {
        if (this.measureTool == null)
        {
            return;
        }
        this.measureTool.dispose();
      //  this.measureTool = null;
    }
   
    public MeasureTool getMeasureTool()
    {
        return this.measureTool;
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
