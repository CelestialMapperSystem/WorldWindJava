/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.core;

import gov.nasa.cms.core.swinglayermanager.LayerNode;
import gov.nasa.cms.core.swinglayermanager.LayerTree;
import gov.nasa.cms.core.swinglayermanager.LayerTreeGroupNode;
import gov.nasa.cms.core.swinglayermanager.LayerTreeModel;
import gov.nasa.cms.core.swinglayermanager.LayerTreeNode;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;
import gov.nasa.worldwindx.applications.worldwindow.util.PanelTitle;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 *
 * @author kjdickin
 */
public class LayerManagerPanel extends JPanel implements TreeModelListener
{
    private WorldWindow wwd;
    private LayerTree layerTree;
    private JPanel panel;
    
    public LayerManagerPanel(WorldWindow wwdObject)
    {
        super(new BorderLayout());
        this.wwd = wwdObject;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        this.makePanel(mainPanel);
    }
    
    public void makePanel(JPanel panel)
    {
        LayerList layerList = getWwd().getModel().getLayers();
        layerList.setDisplayName("Base Layers");
        layerTree = new LayerTree(new LayerTreeModel(layerList));
       // layerTree.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.layerTree.getModel().addTreeModelListener(this);
        
        JScrollPane scrollPane = new JScrollPane(layerTree); // Add layerTree to the scroll pane
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JPanel layerPanel = new JPanel(new BorderLayout(5, 5));
        layerPanel.setOpaque(false);
        layerPanel.add(scrollPane, BorderLayout.CENTER);
        

        
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        // Add the border padding in the dialog
        outerPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), new TitledBorder("Layer Manager")));    
        outerPanel.add(layerPanel, BorderLayout.CENTER); // Add layer panel to the main panel
        
         layerList.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getSource() instanceof LayerList) // the layer list lost, gained or swapped layers
                {
                    ((LayerTreeModel) layerTree.getModel()).refresh((LayerList) event.getSource());
                    getWwd().redraw();
                }
                else if (event.getSource() instanceof Layer)
                {
                    // Just the state of the layer changed.
                    layerTree.repaint();
                }
            }
        });

    }

    private WorldWindow getWwd()
    {
        return wwd;
    }

    @Override
    public void treeNodesChanged(TreeModelEvent event)
    {
         Object[] changedNodes = event.getChildren();
        if (changedNodes != null && changedNodes.length > 0)
        {
            LayerList layerList = getWwd().getModel().getLayers();
            if (layerList == null)
                return;

            for (Object o : changedNodes)
            {
                if (o == null || !(o instanceof LayerNode))
                    continue;

                if (o instanceof LayerTreeGroupNode)
                    this.handleGroupSelection((LayerTreeGroupNode) o, layerList);
                else
                {
                    this.handleLayerSelection((LayerTreeNode) o, layerList);
                }
            }

            this.updateGroupSelections();
            this.layerTree.repaint();
            getWwd().redraw();
        }
    }

    @Override
    public void treeNodesInserted(TreeModelEvent arg0)
    {}

    @Override
    public void treeNodesRemoved(TreeModelEvent arg0)
    {}

    @Override
    public void treeStructureChanged(TreeModelEvent arg0)
    {}
    
    // METHODS REQUIRED TO MODIFY NODES IN THE TREE
    protected void handleGroupSelection(LayerTreeNode group, LayerList layerList)
    {
        Enumeration iter = group.breadthFirstEnumeration();
        while (iter.hasMoreElements())
        {
            Object o = iter.nextElement();
            if (!(o instanceof LayerNode) || (o instanceof LayerTreeGroupNode))
                continue;

            LayerTreeNode layerNode = (LayerTreeNode) o;
            layerNode.setSelected(group.isSelected());
            this.handleLayerSelection(layerNode, layerList);
        }
    }
    
    protected void handleLayerSelection(LayerTreeNode treeNode, LayerList layerList)
    {
        // Many layers do not exist until they're selected. This eliminates the overhead of layers never used.
        if (treeNode.getLayer() == null)
            this.createLayer(treeNode);

        if (treeNode.getLayer() == null)
        {
            // unable to create the layer
            Util.getLogger().warning("Unable to create the layer named " + treeNode.getTitle());
            return;
        }

        // Update the active layers list: Add a missing layer to the list if selected, remove a layer that is
        // not selected.
        if (treeNode.isSelected() && !layerList.contains(treeNode.getLayer()))
        {
            this.performSmartInsertion(treeNode, layerList);
            treeNode.getLayer().setEnabled(true);
        }
        else if (!treeNode.isSelected() && layerList.contains(treeNode.getLayer()))
        {
            layerList.remove(treeNode.getLayer());
        }
    }
    
    protected void performSmartInsertion(LayerTreeNode treeNode, LayerList layerList)
    {
        if (this.insertAfterPreviousSibling(treeNode, layerList))
            return;

        if (this.insertBeforeSubsequentSibling(treeNode, layerList))
            return;

        // No siblings found. Just append the layer to the layer list.
        layerList.add(treeNode.getLayer());
    }
    
        protected boolean insertAfterPreviousSibling(LayerTreeNode treeNode, LayerList layerList)
    {
        LayerTreeNode previousTreeNode = (LayerTreeNode) treeNode.getPreviousSibling();
        while (previousTreeNode != null)
        {
            int index = layerList.indexOf(previousTreeNode.getLayer());
            if (index >= 0)
            {
                layerList.add(index + 1, treeNode.getLayer());
                return true;
            }
            previousTreeNode = (LayerTreeNode) previousTreeNode.getPreviousSibling();
        }

        return false;
    }

    protected boolean insertBeforeSubsequentSibling(LayerTreeNode treeNode, LayerList layerList)
    {
        LayerTreeNode subsequentTreeNode = (LayerTreeNode) treeNode.getNextSibling();
        while (subsequentTreeNode != null)
        {
            int index = layerList.indexOf(subsequentTreeNode.getLayer());
            if (index >= 0)
            {
                layerList.add(index, treeNode.getLayer());
                return true;
            }
            subsequentTreeNode = (LayerTreeNode) subsequentTreeNode.getNextSibling();
        }

        return false;
    }
    
    protected void updateGroupSelections()
    {
        // Ensure that group nodes have their selection box checked if any sub-layer is selected, or does not have
        // its selection box checked if no sub-layer is active.
        Enumeration iter = ((LayerTreeModel) this.layerTree.getModel()).getRootNode().depthFirstEnumeration();
        while (iter.hasMoreElements())
        {
            LayerTreeNode node = (LayerTreeNode) iter.nextElement();
            if (!(node instanceof LayerTreeGroupNode))
                continue;

            this.updateGroupSelection(node);
        }
    }
    
    protected void updateGroupSelection(LayerTreeNode groupNode)
    {
        // Ensure that group nodes have their selection box checked if any child is selected, or does not have
        // its selection box checked if no child is active.
        if (groupNode == null || groupNode == ((LayerTreeModel) layerTree.getModel()).getDefaultGroupNode())
            return;

        for (int i = 0; i < groupNode.getChildCount(); i++)
        {
            if (((LayerNode) groupNode.getChildAt(i)).isSelected())
            {
                groupNode.setSelected(true);
                return;
            }
        }

        groupNode.setSelected(false);
    }
    
    protected void createLayer(LayerNode layerNode)
    {
        if (layerNode == null)
        {
            String msg = "LayerNode is null";
            Util.getLogger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (layerNode.getWmsLayerInfo() != null)
        {
            WMSLayerInfo wmsInfo = layerNode.getWmsLayerInfo();
            AVList configParams = wmsInfo.getParams().copy(); // Copy to insulate changes from the caller.

            // Some wms servers are slow, so increase the timeouts and limits used by WorldWind's retrievers.
            configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
            configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
            configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
            Layer layer = (Layer) factory.createFromConfigSource(wmsInfo.getCaps(), configParams);
            layerNode.setLayer(layer);
        }
    }
    
}
