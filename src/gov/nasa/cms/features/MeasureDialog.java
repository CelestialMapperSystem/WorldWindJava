package gov.nasa.cms.features;

import static gov.nasa.cms.AppFrame.insertBeforePlacenames;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import gov.nasa.worldwindx.examples.kml.KMLDocumentBuilder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.xml.stream.XMLStreamException;

/**
 * Creates a MeasureDialog using CMSMeasurePanel.java. CMSMeasurePanel uses
 * MeasureTool and MeasureToolController to interact with shapes. The user
 * can create as many Measure Tools as they like by opening new tabs. Each
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
        }
        else
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
    
    public void exportMeasureTool() throws FileNotFoundException, XMLStreamException, IOException
    {
        MeasureTool mt = ((CMSMeasurePanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
        
        // Create a new FileOutputStream to the user's home directory
        OutputStream os = new FileOutputStream(Configuration.getUserHomeDirectory() + "/MeasureTool.kml");
            
        // Build the KML document from the file stream
        KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);
        
        kmlBuilder.writeObjects(
                mt.getLine());
    }

}
