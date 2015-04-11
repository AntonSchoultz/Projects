package za.co.discoverylife.appcore.task;

import java.lang.reflect.Method;

/**
 * Records details about a model and method available
 * 
 * @author Anton Schoultz
 */
public class TaskEntry
		implements Comparable<Object>
{

	private Class<?> modelClass;
	private Method method;
	private MetaTask meta;
	private String key;

	private boolean enabled = true;

	/**
	 * CONSTRUCTS an action Entry
	 * 
	 * @param modelClass
	 *          Class of the model
	 * @param method
	 *          Method that is available (public void method(){...} )
	 * @param meta
	 *          MetaAction meta details about the method see {@link MetaTask}
	 */
	public TaskEntry(Class<?> modelClass, Method method, MetaTask meta) {
		super();
		this.modelClass = modelClass;
		this.method = method;
		this.meta = meta;
		key = getBaseKey(modelClass) + method.getName();
	}

	/** Implements Comparable to compares ActionEntry items by key */
	public int compareTo(Object obj) {
		if (this == obj) return 0;
		if (obj == null) return 1;
		if (getClass() == obj.getClass()) {
			TaskEntry other = (TaskEntry) obj;
			// return key.compareTo(other.key);
			return getSequenceKey().compareTo(other.getSequenceKey());
		}
		return getClass().getCanonicalName().compareTo(obj.getClass().getCanonicalName());
	}

	public String getSequenceKey() {
		return String.valueOf(100000 + getSequenceNo()) + key;
	}

	/** Returns sequence number (or zero if none) */
	public int getSequenceNo(){
		int s = 0;
		if (meta != null) {
			s = meta.seqId();
		}
		return s;
	}

	/**
	 * Returns the basic part of the key for the given class,
	 * at this stage this is the class's simple name.
	 * (May need to change this to full class name, or have a MetaInformation on the model
	 * to specify the base name)
	 * 
	 * @param modelClass
	 * @return
	 */
	public static String getBaseKey(Class<?> modelClass) {
		return modelClass.getSimpleName() + "@";
	}

	public String getModelKey() {
		return getBaseKey(modelClass);
	}

	public String toString() {
		return getSequenceKey() + " " + meta.toString();
	}

	public Class<?> getModel() {
		return modelClass;
	}

	public void setModel(Class<?> modelClass) {
		this.modelClass = modelClass;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public MetaTask getMeta() {
		return meta;
	}

	public void setMeta(MetaTask meta) {
		this.meta = meta;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TaskEntry other = (TaskEntry) obj;
		if (key == null) {
			if (other.key != null) return false;
		} else if (!key.equals(other.key)) return false;
		return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
