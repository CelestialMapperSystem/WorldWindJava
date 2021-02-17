
package gov.nasa.cms.features.layermanager;

import gov.nasa.worldwind.WorldWindow;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.tree.TreeCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import java.util.EventObject;

public class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor
{
	private CheckBoxNodeRenderer renderer = null ;
	private ChangeEvent changeEvent = null;
	private JTree tree = null ;

	public CheckBoxNodeEditor( WorldWindow wwd, JTree tree, Font f )
	{
		this.tree = tree;
		this.renderer = new CheckBoxNodeRenderer( wwd, f );
	}

	public Object getCellEditorValue()
	{
		JCheckBox checkbox = renderer.getLeafRenderer();

		return new CheckBoxNode( checkbox.getToolTipText(), checkbox.getText(), checkbox.isSelected());
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
		Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

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
			((JCheckBox) editor).addItemListener(itemListener);
		}
	
		return editor;
	}
}
