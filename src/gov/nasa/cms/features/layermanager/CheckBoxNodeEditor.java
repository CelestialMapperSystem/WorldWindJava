package gov.nasa.cms.features.layermanager;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.tree.TreeCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor
{

    private final CelestialMapper cms;
    private WorldWindow wwd;
    protected CheckBoxNodeRenderer renderer = null;
    private ChangeEvent changeEvent = null;
    protected JTree tree = null;
    private ScreenImage lolaSteelImage;
    private ScreenImage lolaColorImage;
    private RenderableLayer lolaSteelLayer;
    private RenderableLayer lolaColorLayer;

    public CheckBoxNodeEditor(WorldWindow wwd, JTree tree, Font f, CelestialMapper cms)
    {
        this.tree = tree;
        this.renderer = new CheckBoxNodeRenderer(wwd, f, cms);
        this.cms = cms;
        this.wwd = wwd;
    }

    public Object getCellEditorValue()
    {
        JCheckBox checkbox = renderer.getLeafRenderer();

        return new CheckBoxNode(checkbox.getToolTipText(), checkbox.getText(), checkbox.isSelected());
    }

    public boolean isCellEditable(EventObject event)
    {
        boolean returnValue = false;

        if (event instanceof MouseEvent)
        {
            MouseEvent mouseEvent = (MouseEvent) event;

            TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

            if (path != null)
            {
                Object node = path.getLastPathComponent();

                if ((node != null) && (node instanceof DefaultMutableTreeNode))
                {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                    Object userObject = treeNode.getUserObject();
                    returnValue = ((treeNode.isLeaf()) && (userObject instanceof CheckBoxNode));
                }
            }
        }

        return returnValue;
    }

    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row)
    {
        final Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

        //editor always selected / focused
        ItemListener itemListener = new ItemListener()
        {
            public void itemStateChanged(ItemEvent itemEvent)
            {
                if (stopCellEditing())
                {
                    fireEditingStopped();
                }
            }
        };

        if (editor instanceof JCheckBox)
        {
            ((JCheckBox) editor).addActionListener(
                    e ->
            {

                // renderer refers to CheckBoxNodeRenderer which was originally set to ONLY returning
                // the JCheckbox object at it's "leaf" node.  We need the CheckBoxNode though that contains
                // this leaf, and barring an easy way to look up the parent of the JCheckbox (I tried0
                // the easiest way is to make the renderer return both the current CheckBoxNode as well as the
                // the "blind" checkbox - and assign the ActionListener to enable the layer
                // based on the shortened name text stored in the CheckBoxNode.
                CheckBoxNode node = renderer.getNode();
                JCheckBox checkBox = (JCheckBox) editor;
                node.setSelected(checkBox.isSelected());

                // Need to use shortened name of the layer for some reason to look it up properly
                Layer layer = this.renderer.getWwd().getModel().getLayers().getLayerByName(node.getText());

                // I expect that this code only executes when the "editable" cell's checkbox is selected
                // But it seems that both the Editor and Renderer need to have non-null results
                // for the layer look up in order for multiple checkboxes to be selected and
                // multiple layers to be shown at the same time.
                if (layer != null && layer.getName().equals("Mini Map"))
                {
                    cms.enableWML(node.isSelected());
                    cms.getWML().setOpacity(node.isSelected()
                            ? 1 * 0.6 : 0 * 0.6);
                } // Check is any of the layers have a legend that goes with it
                else if (layer != null && layer.getName().equals("LOLA Color Shaded Relief"))
                {
                    layer.setEnabled(node.isSelected());
                    if (lolaColorImage == null)
                    {
                        lolaColorImage = new ScreenImage();
                        lolaColorLayer = new RenderableLayer();
                        layer.setEnabled(node.isSelected());
                        try
                        {
                            lolaColorImage.setImageSource(ImageIO.read(new File("cms-data/images/lolacolor-legend.png")));
                            Rectangle view = cms.getWwd().getView().getViewport();
                            lolaColorImage.setScreenLocation(new Point(view.x + 1150, view.y + 600));
                            lolaColorLayer.addRenderable(lolaColorImage);
                            lolaColorLayer.setName("LOLA Color Legend");

                            wwd.getModel().getLayers().add(lolaColorLayer);
                        } catch (IOException ex)
                        {
                            Logger.getLogger(CheckBoxNodeEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else
                    {
                        wwd.getModel().getLayers().remove(lolaColorLayer);
                        lolaColorImage = null;
                        lolaColorLayer = null;
                    }
                } else if (layer != null && layer.getName().equals("LOLA Color Shaded Relief Steel Ramp"))
                {
                    layer.setEnabled(node.isSelected());
                    if (lolaSteelImage == null)
                    {
                        lolaSteelImage = new ScreenImage();
                        lolaSteelLayer = new RenderableLayer();
                        layer.setEnabled(node.isSelected());
                        try
                        {
                            lolaSteelImage.setImageSource(ImageIO.read(new File("cms-data/images/lolasteel-legend.png")));
                            Rectangle view = cms.getWwd().getView().getViewport();
                            lolaSteelImage.setScreenLocation(new Point(view.x + 960, view.y + 650));
                            lolaSteelLayer.addRenderable(lolaSteelImage);
                            lolaSteelLayer.setName("LOLA Steel Legend");

                            wwd.getModel().getLayers().add(lolaSteelLayer);
                        } catch (IOException ex)
                        {
                            Logger.getLogger(CheckBoxNodeEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else
                    {
                        wwd.getModel().getLayers().remove(lolaSteelLayer);
                        lolaSteelImage = null;
                        lolaSteelLayer = null;
                    }
                } else if (layer != null)
                {
                    layer.setEnabled(node.isSelected());
                }
            });
        }
        return editor;
    }

    public CheckBoxNodeRenderer getRenderer()
    {
        return renderer;
    }

}
