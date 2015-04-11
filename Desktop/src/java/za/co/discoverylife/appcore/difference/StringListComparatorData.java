package za.co.discoverylife.appcore.difference;

import java.util.List;

/**
 * Implementation of ListComparataorData which supports providing 
 * visual difference of two lists of string values.
 * 
 * @author anton11
 */
public class StringListComparatorData extends ListComparatorData<String>
{
	// left hand side list
	List<String> lhs;
	// right hand side list
	List<String> rhs;

	/** CONSTRUCTOR accepts column headings and List<String> for each side to compare */
	public StringListComparatorData(String titleLeft, String titleRight,List<String> leftSide,List<String> rightSide) {
		super(titleLeft, titleRight);
		lhs = leftSide;
		rhs = rightSide;
	}

	@Override
	public List<String> getLhs() {
		return lhs;
	}

	@Override
	public List<String> getRhs() {
		return rhs;
	}

	@Override
	public String getMatchString(String item) {
		return item==null? " ":item;
	}

	@Override
	public String getDisplayString(String item) {
		return item==null? " ":item;
	}

	@Override
	public String getToolTipString(String item) {
		return item==null? " ":item;
	}

	@Override
	public int compare(String left, String right) {
		if (left == null && right != null) {
			return 1;
		}
		if (left != null && right == null) {
			return -1;
		}
		if(left==null && right==null){
			return 0;
		}
		return getMatchString(left).compareTo(getMatchString(right));
	}

	@Override
	public int compareMatch(String left, String right) {
		return compare(left,right);
	}

	@Override
	public void drillDown(String left, String right) {
		// no drill down for strings !
	}

	@Override
	public void accept(List<String> lhs, List<String> rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

}
