package gov.nasa.cms.features.layermanager;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.features.CMSPlaceNamesMenu;
import gov.nasa.cms.util.PanelTitle;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.util.Vector;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.tree.TreePath;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LayerPanel extends JPanel
{

     private JPanel layersPanel = null;
     private JPanel treePanel_ = null;
     private JTree tree_ = null;
     private JScrollPane scrollPane = null;
     private Font defaultFont = null;
     private WorldWindow wwd = null;
     private Dimension size_ = null;
     private CMSPlaceNamesMenu cmsPlaceNamesMenu;
     private CelestialMapper cms;

     // Font problem : too thin, use the textfield one
     private Font font_ = UIManager.getFont("Textfield.font");

     public LayerPanel(WorldWindow wwd)
     {
          this.wwd = wwd;

          makePanel();
     }

     public LayerPanel(WorldWindow wwd, Dimension size)
     {
          this.wwd = wwd;
          this.size_ = size;

          makePanel();
     }

     protected void makePanel()
     {
          this.setLayout(new BorderLayout());

          tree_ = this.fill(wwd);

          // Put the tree in a scroll bar.
          this.scrollPane = new JScrollPane(tree_);
          this.scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

          if (size_ == null)
          {
               size_ = new Dimension(200, 400);
          }

          this.scrollPane.setPreferredSize(size_);

          // Add the scroll bar and name panel to a titled panel that will resize with the main window.
          treePanel_ = new JPanel(new GridLayout(0, 1, 0, 10));
          treePanel_.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("")));
          treePanel_.setToolTipText("Layers to Show");
          treePanel_.add(scrollPane);
          PanelTitle panelTitle = new PanelTitle("Available Layers", SwingConstants.CENTER);
          this.add(treePanel_, BorderLayout.CENTER);
          this.add(panelTitle, BorderLayout.NORTH);
     }

     /**
      * Update the panel to match the layer list active in a WorldWindow.
      *
      * @param wwd WorldWindow that will supply the new layer list.
      */
     public void update(WorldWindow wwd)
     {
          // Replace all the layer names in the layers panel with the names of the current layers.
          this.removeAll();
          makePanel();
     }

     protected JTree fill(WorldWindow wwd)
     {
          Vector lolaCategory = new Vector();
          Vector clementineCategory = new Vector();
          Vector resourceMapsCategory = new Vector();
          Vector globalMapsCategory = new Vector();
          Vector toolsCategory = new Vector();
          Vector miscOptions = new Vector();

          // Put the layers into categories for WorldWindow's LayerList
          for (Layer layer : wwd.getModel().getLayers())
          {
               String name = layer.getName();

               // LOLA Maps
               if (name.startsWith("LOLA") || name.startsWith("LRO"))
               {
                    lolaCategory.add(new CheckBoxNode("LOLA", name, layer.isEnabled()));
                    wwd.redraw();
               } 
               // Clementine Maps
               else if (name.startsWith("Clementine"))
               {
                    clementineCategory.add(new CheckBoxNode("Clementine", name, layer.isEnabled()));
               } 
               // Kaguya Maps
               else if (name.startsWith("Kaguya") || name.startsWith("Lunar"))
               {
                    globalMapsCategory.add(new CheckBoxNode("Global Maps", name, layer.isEnabled()));
               } 
               // Resource Maps
               else if (name.startsWith("Unified"))
               {
                    resourceMapsCategory.add(new CheckBoxNode("Resource Maps", name, layer.isEnabled()));
               } 
               // Tools
               else if (name.startsWith("Lat") || name.startsWith("GARS") || name.startsWith("View") || name.startsWith("Scale"))
               {
                    toolsCategory.add(new CheckBoxNode("Tools", name, layer.isEnabled()));
               } 
               // Misc
               else
               {
                    miscOptions.add(new CheckBoxNode("Misc", name, layer.isEnabled()));
               }
//               LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
//               JCheckBox jcb = new JCheckBox(action);
//               jcb.setSelected(action.selected);
//               this.layersPanel.add(jcb);
			
          }

          Object rootNodes[] = new Object[6];

          rootNodes[0] = new NamedVector("LOLA", lolaCategory);
          rootNodes[1] = new NamedVector("Clementine", clementineCategory);
          rootNodes[2] = new NamedVector("Resource Maps", resourceMapsCategory);
          rootNodes[3] = new NamedVector("Global Maps", globalMapsCategory);
          rootNodes[4] = new NamedVector("Tools", toolsCategory);
          rootNodes[5] = new NamedVector("Misc", miscOptions);

          Vector rootVector = new NamedVector("Root", rootNodes);

          JTree tree = new JTree(rootVector);

          tree.setCellRenderer(new CheckBoxNodeRenderer(wwd, font_));
          tree.setCellEditor(new CheckBoxNodeEditor(wwd, tree, font_));

          tree.setEditable(true);

          tree.expandRow(6); 
          tree.expandRow(0); 

          return tree;
     }

     private String shorten(String parent, String name)
     {
          return name.substring(parent.length() + 1);
     }

     private String rebuildLayerName(TreePath parentPath, TreePath path)
     {
          String parentName = String.valueOf(parentPath.getLastPathComponent());
          String name = String.valueOf(path.getLastPathComponent());

          if (parentName.equals("Misc"))
          {
               return name;
          }

          return parentName + " " + name;
     }

     private boolean hasProperties(String layerName)
     {
          Layer layer = wwd.getModel().getLayers().getLayerByName(layerName);

          if (layer == null
                  || layer instanceof StarsLayer
                  || layer instanceof SkyGradientLayer
                  || layer instanceof SkyColorLayer
                  || layer instanceof PlaceNameLayer
                  || layer instanceof CompassLayer)
          {
               return false;
          }

          return true;
     }

     protected static class LayerAction extends AbstractAction
     {
          WorldWindow wwd;
          protected Layer layer;
          protected boolean selected;

          public LayerAction(Layer layer, WorldWindow wwd, boolean selected)
          {
               super(layer.getName());
               this.wwd = wwd;
               this.layer = layer;
               this.selected = selected;
               this.layer.setEnabled(this.selected);
          }

          public void actionPerformed(ActionEvent actionEvent)
          {
               // Simply enable or disable the layer based on its toggle button.
               if (((JCheckBoxMenuItem) actionEvent.getSource()).isSelected())
               {
                    this.layer.setEnabled(true);
               } else
               {
                    this.layer.setEnabled(false);
               }

               wwd.redraw();
          }
     }
}
