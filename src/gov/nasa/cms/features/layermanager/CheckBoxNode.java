
package gov.nasa.cms.features.layermanager;

public class CheckBoxNode
{
	private String parent;
	private String text;
	private boolean selected;
	
	public CheckBoxNode( String parent, String text, boolean selected)
	{
		this.parent = parent;
		this.text = text;
		this.selected = selected;
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void setSelected(boolean newValue)
	{
		selected = newValue;
	}
	
	public String getText()
	{
		return text;
	}

	public String getParentText()
	{
		return parent;
	}
	
	public void setText(String newValue)
	{
		text = newValue;
	}

	public String getFullText()
	{
		if ( parent.equals("Misc"))
			return text ;

		return parent + " " + text ;
	}
	
	public String toString()
	{
		return text ;
	}
}
