package za.co.discoverylife.appcore.difference;

import java.security.InvalidParameterException;
import java.util.List;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.ValueObject;

/**
 * Provides field comparison for an item in the lists being compared. 
 */
public class FieldComparatorData extends ListComparatorData<ValueObject>
{
	// name of the field
	private String fieldName;

	/** CONSTRUCTOR which accepts a field name */
	public FieldComparatorData(String fieldName) {
		super(fieldName, fieldName);
		this.fieldName = fieldName;
	}
	
	@Override
	public List<ValueObject> getLhs() {
		return null;
	}

	@Override
	public List<ValueObject> getRhs() {
		return null;
	}

	@Override
	public String getMatchString(ValueObject item) {
		return field(item).getStringFromField();
	}

	@Override
	public String getDisplayString(ValueObject item) {
		return field(item).getStringFromField();
	}

	@Override
	public String getToolTipString(ValueObject item) {
		return field(item).getStringFromField();
	}

	@Override
	public int compare(ValueObject left, ValueObject right) {
		String ls = field(left).getStringFromField();
		String rs = field(right).getStringFromField();
		return ls.compareTo(rs);
	}

	@Override
	public int compareMatch(ValueObject left, ValueObject right) {
		return compare(left,right);
	}

	@Override
	public void drillDown(ValueObject left, ValueObject right) {
	}

	@Override
	public void accept(List<ValueObject> lhs, List<ValueObject> rhs) {
	}
	
	/** Returns a FieldAccessor for the specified field in the provided object. 
	 * @param object Object which contains the field to be accessed.
	 * @return FieldAccessor to the field in the object
	 */
	private FieldAccessor field(ValueObject object){
		try {
			return new FieldAccessor(object, fieldName);
		} catch (FieldAccessException e) {
			e.printStackTrace();
			throw new InvalidParameterException(fieldName);
		}
	}
}
