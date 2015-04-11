package za.co.discoverylife.appcore;

import java.util.*;

import za.co.discoverylife.appcore.task.TaskEntry;

/**
 * Provides static storage of data objects by name.
 * 
 * @author anton11
 */
public class DataHolder {
	private static Map<String, Object> map = new HashMap<String, Object>();
	private static Stack<Object> stack = new Stack<Object>();

	/** Returns true if the stack is empty */
	public static boolean isEmpty() {
		return stack.empty();
	}

	/**
	 * Looks at the object at the top of the stack without removing it from the
	 * stack.
	 */
	public static Object peek() {
		return stack.peek();
	}

	/**
	 * Removes the object at the top of the stack and returns that object as the
	 * value of this function.
	 */
	public static Object pop() {
		return stack.pop();
	}

	/** Pushes an item onto the top of the stack. */
	public static Object push(Object object) {
		return stack.push(object);
	}

	/** Removes all of the elements from the stack. */
	public static void clear() {
		stack.clear();
	}

	/** Stores the provided object under the given key */
	public static void store(String key, Object object) {
		map.put(key, object);
	}

	/** Removes the object under the given key */
	public static void remove(String key) {
		map.remove(key);
	}

	/** Returns the referenced object */
	public static Object recall(String key) {
		return map.get(key);
	}

	/** Returns the referenced object */
	public static Object recall(Object obj) {
		return recall(obj.getClass());
	}

	/** Store an object using it's base key as the store key */
	public static void store(Object object) {
		String key = TaskEntry.getBaseKey(object.getClass());
		store(key, object);
	}

	/** Returns the referenced object */
	public static Object recall(Class<?> k) {
		Object obj = map.get(TaskEntry.getBaseKey(k));
		if (obj == null) {
			try {
				obj = k.newInstance();
				store(obj);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

}
