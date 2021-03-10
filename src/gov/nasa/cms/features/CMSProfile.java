/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;


/**
 * Creates a new terrain profile layer from <code>{@link JCheckBoxMenuItem}</code> created
 * from the passed in WorldWindow.
 * @author kjdickin
 */
public class CMSProfile extends JCheckBoxMenuItem
{

    private final String layer =  "Terrain Profile";
    private WorldWindow wwd;
    private TerrainProfileLayer tpl;
    private boolean isItemEnabled;
    private CelestialMapper cms;
    private Layer selectedLayer;

    // Create the terrain profile layer
    public void setupProfile()
    {
        this.tpl = new TerrainProfileLayer(); 
        this.tpl.setEventSource(this.getWwd()); 
        String layerName = "Terrain Profile"; 
        this.tpl.setName(layerName);
        AppFrame.insertBeforeCompass(this.getWwd(), tpl); // display on screen
    }

    public CMSProfile(WorldWindow Wwd)
    {
        super("Terrain Profiler");

        // Enable and disable terrain profile 
        this.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                isItemEnabled = ((JCheckBoxMenuItem) event.getSource()).getState();

                // Setup the TerrainProfileLayer
                if (isItemEnabled)
                {
                    setWwd(Wwd);
                    setupProfile();
                } 
                // Remove TerrainProfileLayer from the WorldWindow
                else
                {
                    String layer = "Terrain Profile";                 
                    Layer selectedLayer = Wwd.getModel().getLayers().getLayerByName(layer);
                    Wwd.getModel().getLayers().remove(selectedLayer); 
                }
            }
        });
    }

    public CMSProfile(WorldWindow Wwd, CelestialMapper cms){
        super("Terrain Profiler");
        setWwd(Wwd);
        setCMS(cms);
        setupProfile();
        this.selectedLayer = Wwd.getModel().getLayers().getLayerByName(layer);
    }

    @Override
    public void setVisible(boolean visible)
    {
//        super.setVisible(aFlag);
        if(visible){
            setupProfile();
            this.selectedLayer.setEnabled(visible);
        } else {
            this.wwd.getModel().getLayers().remove(selectedLayer);
        }
//        tpl.setEnabled(visible);
//        this.selectedLayer.setEnabled(visible);
    }

    private void setCMS(CelestialMapper cms)
    {
        this.cms = cms;
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
