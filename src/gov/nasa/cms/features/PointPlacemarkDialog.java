package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import java.awt.*;
import javax.swing.JDialog;

/**
 *
 * @author kjdickin
 */
public class PointPlacemarkDialog
{

    private JDialog dialog;

    public PointPlacemarkDialog(WorldWindow wwdObject, Component component)
    {
        CMSPointPlacemarkPanel measurePanel = new CMSPointPlacemarkPanel(wwdObject);

        //dialog.setSize(new Dimension(200, 400));
        // Create the dialog from a Frame and set the bounds
        dialog = new JDialog((Frame) component);
        Rectangle bounds = component.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Point Placemarks");
        // Set the location and resizable to false
        dialog.setLocation(bounds.x + 900, bounds.y + 300);
        dialog.setResizable(false);
        // Add the tabbedPane to the dialog
        dialog.getContentPane().add(measurePanel, BorderLayout.CENTER);

        dialog.pack();
    }
    
    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }

}
