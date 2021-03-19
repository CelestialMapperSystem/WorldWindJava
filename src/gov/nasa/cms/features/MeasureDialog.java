package gov.nasa.cms.features;

import static gov.nasa.cms.AppFrame.insertBeforePlacenames;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceEllipse;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.SurfaceSquare;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import gov.nasa.worldwindx.examples.kml.KMLDocumentBuilder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.xml.stream.XMLStreamException;

/**
 * Creates a MeasureDialog using CMSMeasurePanel.java. CMSMeasurePanel uses
 * MeasureTool and MeasureToolController to interact with shapes. The user can
 * create as many Measure Tools as they like by opening new tabs. Each
 * MeasureTool contains a TerrainProfileLayer which measures the terrain profile
 * along the MeasureTool that is created.
 *
 * @author kjdickin
 */
public class MeasureDialog
{

    private JDialog dialog;
    private final TerrainProfileLayer profile = new TerrainProfileLayer();
    private JTabbedPane tabbedPane = new JTabbedPane();
    private final PropertyChangeListener measureToolListener = new MeasureToolListener();
    private int lastTabIndex = -1;

    public MeasureDialog(WorldWindow wwdObject, MeasureTool measureToolObject, Component component)
    {
        // Add terrain profile layer
        profile.setEventSource(wwdObject);
        profile.setFollow(TerrainProfileLayer.FOLLOW_PATH);
        profile.setShowProfileLine(false);
        insertBeforePlacenames(wwdObject, profile);

        // Add + tab
        this.tabbedPane = new JTabbedPane();
        tabbedPane.add(new JPanel());
        tabbedPane.setTitleAt(0, "+");
        tabbedPane.addChangeListener((ChangeEvent changeEvent) ->
        {
            if (tabbedPane.getSelectedIndex() == 0)
            {
                // Add new measure tool in a tab when '+' selected
                MeasureTool measureTool = new MeasureTool(wwdObject);

                measureTool.setController(new MeasureToolController());

                tabbedPane.setOpaque(false);
                tabbedPane.add(new CMSMeasurePanel(wwdObject, measureTool));
                tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "" + (tabbedPane.getTabCount() - 1));
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                switchMeasureTool();
            } else
            {
                switchMeasureTool();
            }
        });

        // Add measure tool control panel to tabbed pane
        final MeasureTool measureTool = new MeasureTool(wwdObject);

        measureTool.setController(new MeasureToolController());
        CMSMeasurePanel measurePanel = new CMSMeasurePanel(wwdObject, measureTool);

        tabbedPane.add(measurePanel);
        tabbedPane.setOpaque(false);
        tabbedPane.setTitleAt(1, "1");
        tabbedPane.setSelectedIndex(1);
        tabbedPane.setToolTipTextAt(0, "Create measurement");
        switchMeasureTool();

        // Create the dialog from a Frame and set the bounds
        dialog = new JDialog((Frame) component);
        Rectangle bounds = component.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Measure Tool");
        // Set the location and resizable to false
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(false);
        // Add the tabbedPane to the dialog
        dialog.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        dialog.pack();
    }

    private void deleteCurrentPanel()
    {
        CMSMeasurePanel mp = getCurrentPanel();
        if (tabbedPane.getTabCount() > 2)
        {
            mp.deletePanel();
            tabbedPane.remove(tabbedPane.getSelectedComponent());
        } else
        {
            mp.clearPanel();
        }
    }

    // Get the current tabbedPane 
    private CMSMeasurePanel getCurrentPanel()
    {
        JComponent p = (JComponent) tabbedPane.getSelectedComponent();
        return (CMSMeasurePanel) p;
    }

    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }

    private void switchMeasureTool()
    {
        // Disarm last measure tool when changing tab and switching tool
        if (lastTabIndex != -1)
        {
            MeasureTool mt = ((CMSMeasurePanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
            mt.setArmed(false);
            mt.removePropertyChangeListener(measureToolListener);
        }
        // Update terrain profile from current measure tool
        lastTabIndex = tabbedPane.getSelectedIndex();
        MeasureTool mt = ((CMSMeasurePanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
        mt.addPropertyChangeListener(measureToolListener);
        updateProfile(mt);
    }

    private class MeasureToolListener implements PropertyChangeListener
    {

        @Override
        public void propertyChange(PropertyChangeEvent event)
        {
            // Measure shape position list changed - update terrain profile
            if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE))
            {
                updateProfile(((MeasureTool) event.getSource()));
            }
        }
    }

    // Updates the Terrain Profile Layer from the measure tool passed into it
    private void updateProfile(MeasureTool mt)
    {
        ArrayList<? extends LatLon> positions = mt.getPositions();
        if (positions != null && positions.size() > 1)
        {
            profile.setPathPositions(positions);
            profile.setEnabled(true);
        } else
        {
            profile.setEnabled(false);
        }

        mt.getWwd().redraw();
    }

    // TO-DO: How to export other shapes besides lines? 
    public void exportMeasureTool() throws FileNotFoundException, XMLStreamException, IOException
    {
        JFrame parentFrame = new JFrame(); // Create the frame for the save dialog

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        int userSelection = fileChooser.showSaveDialog(parentFrame); // Show the JFileChoose in the popup frame

        // If user approves, set the file save location and export the Measure Tool information in KML
        if (userSelection == JFileChooser.APPROVE_OPTION)
        {
            File fileToSave = fileChooser.getSelectedFile();

            MeasureTool mt = ((CMSMeasurePanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();

            if (mt.getMeasureShapeType().equals(MeasureTool.SHAPE_LINE) 
                    || mt.getMeasureShapeType().equals(MeasureTool.SHAPE_PATH))
            {

                // Create a new FileOutputStream to where the user chose to save the file
                OutputStream os = new FileOutputStream(fileToSave);

                // Build the KML document from the file stream
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
                // Write to KML document
                kmlBuilder.writeObjects(
                        mt.getLine());
                
                kmlBuilder.close();
            }
            
            // Get the SurfaceShape from the current MeasureTool
            SurfaceShape shape = mt.getSurfaceShape();
            
            // Condition statements to check what shape the Measure Tool is
            if(mt.getSurfaceShape() instanceof SurfaceQuad)
            {
                // Cast an instance of SurfaceQuad to the MeasureTool SurfaceShape
                SurfaceQuad quad = ((SurfaceQuad) shape);
                
                OutputStream os = new FileOutputStream(fileToSave);
                
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
                kmlBuilder.writeObjects(
                        quad);
                
                kmlBuilder.close();
            }
            else if(mt.getSurfaceShape() instanceof SurfaceSquare)
            {
                SurfaceSquare square = ((SurfaceSquare) shape);
                
                OutputStream os = new FileOutputStream(fileToSave);
                
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
                kmlBuilder.writeObjects(
                        square);
                
                kmlBuilder.close();
            }
            // TO-DO: Fix Circle and Ellipse not filling inside the shape
            else if(mt.getSurfaceShape() instanceof SurfaceEllipse)
            {
                SurfaceEllipse ellipse = ((SurfaceEllipse) shape);
                
                OutputStream os = new FileOutputStream(fileToSave);
                
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
                kmlBuilder.writeObjects(
                        ellipse);
                
                kmlBuilder.close();
            }
            else if(mt.getSurfaceShape() instanceof SurfaceCircle)
            {
                SurfaceCircle circle = ((SurfaceCircle) shape);
                
                OutputStream os = new FileOutputStream(fileToSave);
                
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
                kmlBuilder.writeObjects(
                        circle);
                
                kmlBuilder.close();
            }
            else // Polygon
            {
                SurfacePolygon poly = ((SurfacePolygon) shape);
                
                OutputStream os = new FileOutputStream(fileToSave);
                
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
                kmlBuilder.writeObjects(
                        poly);
                
                kmlBuilder.close();
            }
        }
    }

}
