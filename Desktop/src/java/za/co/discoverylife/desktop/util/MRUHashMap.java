package za.co.discoverylife.desktop.util;

import java.util.LinkedHashMap;

public class MRUHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 7744762809556592885L;
	private int maxEntries = 100;

	/**
	 * CONSTRUCTS a new MostRecntly used map of specified capacity
	 * @param maxSize maximum capacity
	 */
	public MRUHashMap(int maxSize) {
		super(maxSize/10, 0.8f /*loadFactor*/,true/* accessOrder*/);
		this.maxEntries=maxSize;
	}
	
	/** CONSTRUCTOR creates a Most recently used map
	 * using the default no of entries (100)
	 */
	public MRUHashMap(){
		super(100/10, 0.8f /*loadFactor*/,true/* accessOrder*/);
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		boolean zap = (size() > maxEntries);
		System.out.println("oldest has key "+eldest.getKey()+" zap="+zap);
		return size() > maxEntries;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

}
