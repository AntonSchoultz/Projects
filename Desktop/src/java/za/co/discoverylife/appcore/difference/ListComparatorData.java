package za.co.discoverylife.appcore.difference;

import java.util.List;

/**
 * Acts as an adaptor/helper for visual difference of two lists of items.
 * 
 * @author anton11
 *
 * @param <T>
 */
public abstract class ListComparatorData<T>
{
	protected MatchComparator<T> comparator;
	protected String titleLeft=" ";
	protected String titleRight=" ";
	protected int colWidth = 0;;
	
	/** CONSTRUCTOR which accepts left and right titles*/
	public ListComparatorData(String titleLeft, String titleRight) {
		super();
		this.titleLeft = titleLeft;
		this.titleRight = titleRight;
	}
	
	/** Returns the title for the left side */
	public String getTitleLeft() {
		return titleLeft;
	}

	/** Returns the title for the right side */
	public String getTitleRight() {
		return titleRight;
	}

	/** Returns the list of items for the Left side */
	public abstract List<T> getLhs();

	/** Returns the list of items for the Right side */
	public abstract List<T> getRhs();
	
	/** Return the string used to match rows with */
	public abstract String getMatchString(T item);
	
	/** Return the string to display in the list */
	public abstract String getDisplayString(T item);
	
	/** Return the tool tip text for the item */
	public abstract String getToolTipString(T item);
	
	/** Performs detailed comparison, returns true if items are the same */
	public abstract int compare(T left,T right);

	/** Performs match comparison, returns true if items are the same */
	public abstract int compareMatch(T left, T right);
	
	/** Drill down to more detailed comparison */
	public abstract void drillDown(T left,T right);
	
	/** Make changes permanent */
	public abstract void accept(List<T> lhs,List<T> rhs);

	public int getColWidth() {
		return colWidth;
	}
	/** Set no of characters for each column */
	public void setColWidth(int colWidth) {
		this.colWidth = colWidth;
	}
}
