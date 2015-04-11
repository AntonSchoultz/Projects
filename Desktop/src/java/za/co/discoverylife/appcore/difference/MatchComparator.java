package za.co.discoverylife.appcore.difference;

import java.util.Comparator;
/**
 * Comparator to compare objects by their Match Strings 
 * @author anton11
 *
 * @param <T>
 */
public class MatchComparator<T> implements Comparator<T>
{
	// adaptor/helper for visual difference of two lists of items.
	private ListComparatorData<T> differenceAdaptor;
	
	/** 
	 * CONSTRUCTOR which accepts a ListComparatorData helper
	 * which is then used to provide the comparison of objects.
	 */ 
	public MatchComparator(ListComparatorData<T> differenceAdaptor){
		this.differenceAdaptor  = differenceAdaptor;
	}

	/** returns comparison of match string values */
	public int compare(T o1, T o2) {
		return differenceAdaptor.getMatchString(o1)
		.compareTo(differenceAdaptor.getMatchString(o2));
	}

}
