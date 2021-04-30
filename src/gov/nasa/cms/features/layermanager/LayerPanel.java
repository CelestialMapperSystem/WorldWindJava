package gov.nasa.cms.features.layermanager;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.layers.WorldMapLayer;
import gov.nasa.cms.util.PanelTitle;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Vector;

public class LayerPanel extends JPanel
{

     private final CelestialMapper cms;
     private JPanel layersPanel = null;
     private JPanel treePanel_ = null;
     private JTree tree_ = null;

     private JScrollPane scrollPane = null;
     private Font defaultFont = null;
     private WorldWindow wwd = null;
     private Dimension size_ = null;
     private JTree tree;

     // Font problem : too thin, use the textfield one
     private Font font_ = UIManager.getFont("Textfield.font");

     public LayerPanel(WorldWindow wwd, CelestialMapper cms)
     {
          this.wwd = wwd;
          this.cms = cms;

          makePanel();
     }

     public LayerPanel(WorldWindow wwd, Dimension size, CelestialMapper cms)
     {
          this.wwd = wwd;
          this.size_ = size;
          this.cms = cms;

          makePanel();
     }

     protected void makePanel()
     {
          this.setLayout(new BorderLayout());

          tree_ = this.fill(this.wwd);

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
          this.validate();
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
                    // GNorman - 02/26/2021

                    // Originally was thinking of extending the CheckBoxNode to manually add an ActionListener
                    // to the JCheckBox that gets shown in the tree...but as it turns out the JCheckBox is added
                    // later by the Renderer using the text stored in CheckBoxNode.

                    // The easiest way to add an ActionListener then is in the CheckBoxNodeEditor, since the
                    // Renderer should only construct the checkboxes once and the tree needs to be set to "editable"
                    // in order for the user to even make a change to the checkboxes!

                    // SO instead of fighting how the Tree wants to operate in Java (with total and utter control)
                    // I updated the CheckBoxNodeEditor stub that Kaitlyn had made with an ActionListener and used
                    // the text stored here in the CheckBoxNode, stored in the Renderer, to look up and enable/disable
                    // the WorldWind layer.
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
               else if (name.startsWith("Lat") || name.startsWith("GARS") ||
                   name.startsWith("View") || name.startsWith("Scale") || name.startsWith("Mini") || name.startsWith("Coordinates"))
               {
                    toolsCategory.add(new CheckBoxNode("Tools", name, layer.isEnabled()));
               } 
               // Misc
               else
               {
                    miscOptions.add(new CheckBoxNode("Misc", name, layer.isEnabled()));
               }

          }

          Object rootNodes[] = new Object[6];

          rootNodes[0] = new NamedVector("LOLA", lolaCategory);
          rootNodes[1] = new NamedVector("Clementine", clementineCategory);
          rootNodes[2] = new NamedVector("Resource Maps", resourceMapsCategory);
          rootNodes[3] = new NamedVector("Global Maps", globalMapsCategory);
          rootNodes[4] = new NamedVector("Tools", toolsCategory);
          rootNodes[5] = new NamedVector("Misc", miscOptions);

          Vector rootVector = new NamedVector("Root", rootNodes);

          tree = new JTree(rootVector);
          tree.setCellRenderer(new CheckBoxNodeRenderer(wwd, font_, cms));
          tree.setCellEditor(new CheckBoxNodeEditor(wwd, tree, font_, cms));
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


}
