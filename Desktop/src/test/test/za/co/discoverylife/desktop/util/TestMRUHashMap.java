package test.za.co.discoverylife.desktop.util;

import test.BaseTestCase;
import za.co.discoverylife.desktop.util.Encryption;
import za.co.discoverylife.desktop.util.MRUHashMap;


public class TestMRUHashMap extends BaseTestCase {

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
  }
	
  public void testMRUHashMap(){
	  MRUHashMap<String, String> mru = new MRUHashMap<String, String>(3);
	  mru.put("One", "One1");
	  mru.put("Two", "Two1");
	  mru.put("Three", "Three");
	  mru.get("One");
	  mru.put("Four", "4");
	  String test = mru.get("Two");// should have been zapped
	  assertEquals("Did not remove oldest item", null, test);
	  assertEquals("Did not retrieve item", "One1", mru.get("One"));
	  assertEquals("Did not retrieve item", "Three", mru.get("Three"));
	  assertEquals("Did not retrieve item", "4", mru.get("Four"));
  }
}
