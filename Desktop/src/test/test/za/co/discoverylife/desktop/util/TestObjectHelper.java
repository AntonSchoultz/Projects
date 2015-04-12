package test.za.co.discoverylife.desktop.util;

import test.BaseTestCase;
import za.co.discoverylife.desktop.field.ObjectHelper;
import za.co.discoverylife.desktop.host.ServerConnection;

public class TestObjectHelper extends BaseTestCase
{
	
	private static final String JOHN = "ServerConnection [url=host, port=8080, user=john, password=pass]";
	private static final String ANTON =	"ServerConnection [url=localhost, port=80, user=anton, password=password]";
	
	public void testCopy(){
		ServerConnection sc = new ServerConnection("localhost", 80, "anton", "password");
		ServerConnection st = new ServerConnection("host", 8080, "john", "pass");
		assertEquals("Setup failed", JOHN, st.toString());
		System.out.println(sc.toString());
		try {
			ObjectHelper.copy(sc, st);
			assertEquals("Copy failed", ANTON, st.toString());
		} catch (Exception e) {
			fail("Copy threw an Exception "+e.getMessage());;
		}
	}
}
