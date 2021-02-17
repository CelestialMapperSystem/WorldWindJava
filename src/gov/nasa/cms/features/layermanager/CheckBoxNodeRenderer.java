
package gov.nasa.cms.features.layermanager;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;

import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import javax.swing.UIManager;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

public class CheckBoxNodeRenderer implements TreeCellRenderer
{
	private JCheckBox leafRenderer = new JCheckBox();
	private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();
	private Color selectionBorderColor;
	private Color selectionForeground;
	private Color selectionBackground;
	private Color textForeground;
	private Color textBackground;
	private Font fontValue;
	private WorldWindow wwd;

	public CheckBoxNodeRenderer( WorldWindow wwd, Font fontValue )
	{
		this.wwd = wwd ;
		this.fontValue = fontValue ;

		Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");

		leafRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));

		selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");

		if ( fontValue != null )
		{
			leafRenderer.setFont( fontValue );
			nonLeafRenderer.setFont( fontValue );
		}
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
		boolean hasFocus)
	{

		Component c;

		if (leaf)
		{
			String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);

			leafRenderer.setText(stringValue);
			leafRenderer.setSelected(false);

			leafRenderer.setEnabled(tree.isEnabled());

			if (selected)
			{
				leafRenderer.setForeground(selectionForeground);
				leafRenderer.setBackground(selectionBackground);
			}
			else
			{
				leafRenderer.setForeground(textForeground);
				leafRenderer.setBackground(textBackground);
			}

			if ((value != null) && (value instanceof DefaultMutableTreeNode))
			{
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

				if (userObject instanceof CheckBoxNode)
				{
					CheckBoxNode node = (CheckBoxNode) userObject;

					leafRenderer.setToolTipText(node.getParentText());
					leafRenderer.setText(node.getText());
					leafRenderer.setSelected(node.isSelected());

					Layer layer = wwd.getModel().getLayers().getLayerByName( node.getFullText() );

					if ( layer != null )	
					{
						layer.setEnabled( leafRenderer.isSelected() );
					}
				}
			}

			c = leafRenderer;
		}
		else
		{
			c = nonLeafRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}

		return c;
	}

	protected JCheckBox getLeafRenderer()
	{
		return leafRenderer;
	}
}
