/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.examples.WMSLayersPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Allows users to enter a WMS server and returns layers from the endpoint that become cached in the local machine.
 * @author kjdickin
 */
public class WMSLayerManager
{

    Component comp;
    protected final Dimension wmsPanelSize = new Dimension(400, 600);
    protected JTabbedPane tabbedPane;
    protected int previousTabIndex;
    WorldWindow wwd;
    JFrame controlFrame;
    
    protected static final String[] servers = new String[]
    {
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/earth/moon_simp_cyl.map",
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/earth/moon_npole.map",
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/earth/moon_spole.map",
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/earth/moon_simp_cyl_quads.map",
        "https://wms.wr.usgs.gov/cgi-bin/mapserv?map=/maps/earth/moon_nomen_wms.map",
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/mars/mars_simp_cyl.map&",
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/saturn/titan_simp_cyl.map&",
        "http://webmap.lroc.asu.edu/",
        "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/venus/venus_simp_cyl.map&"
    };

    public WMSLayerManager(WorldWindow wwdObject, Component component)
    {
        this.comp = component;
        this.wwd = wwdObject;
        this.tabbedPane = new JTabbedPane();

        this.tabbedPane.add(new JPanel());
        this.tabbedPane.setTitleAt(0, "+");
        this.tabbedPane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                if (tabbedPane.getSelectedIndex() != 0)
                {
                    previousTabIndex = tabbedPane.getSelectedIndex();
                    return;
                }

                String server = JOptionPane.showInputDialog("Enter wms server URL");
                if (server == null || server.length() < 1)
                {
                    tabbedPane.setSelectedIndex(previousTabIndex);
                    return;
                }

                // Respond by adding a new WMSLayerPanel to the tabbed pane.
                if (addTab(tabbedPane.getTabCount(), server.trim()) != null)
                {
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                }
            }
        });

        // Create a tab for each server and add it to the tabbed panel.
        for (int i = 0; i < servers.length; i++)
        {
            this.addTab(i + 1, servers[i]); // i+1 to place all server tabs to the right of the Add Server tab
        }

        // Display the first server pane by default.
        this.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() > 0 ? 1 : 0);
        this.previousTabIndex = this.tabbedPane.getSelectedIndex();

        // Add the tabbed pane to a frame separate from the WorldWindow.
        controlFrame = new JFrame();
        controlFrame.getContentPane().add(tabbedPane);
        controlFrame.pack();
        controlFrame.setVisible(true);
    }

    protected WMSLayersPanel addTab(int position, String server)
    {
        // Add a server to the tabbed dialog.
        try
        {
            WMSLayersPanel layersPanel = new WMSLayersPanel(wwd, server, wmsPanelSize);
            this.tabbedPane.add(layersPanel, BorderLayout.CENTER);
            String title = layersPanel.getServerDisplayString();
            this.tabbedPane.setTitleAt(position, title != null && title.length() > 0 ? title : server);

            return layersPanel;
        } catch (URISyntaxException e)
        {
            JOptionPane.showMessageDialog(null, "Server URL is invalid", "Invalid Server URL",
                    JOptionPane.ERROR_MESSAGE);
            tabbedPane.setSelectedIndex(previousTabIndex);
            return null;
        }
    }
    
    public void setVisible(boolean visible)
    {
        controlFrame.setVisible(visible);
    }

}
