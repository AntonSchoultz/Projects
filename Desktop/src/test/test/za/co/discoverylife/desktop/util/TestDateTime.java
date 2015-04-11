package test.za.co.discoverylife.desktop.util;
import test.BaseTestCase;
import za.co.discoverylife.desktop.util.DateTime;

/**
 * 
 */
/**
 * @author Anton Schoultz - 2015
 *
 */
public class TestDateTime extends BaseTestCase
{
  DateTime dtm;
  
  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    dtm = new DateTime("2015/04/02 13:14:50");
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#getFromMillis()}.
   */
  public void testFromMillis(){
	  long ms = dtm.getTimeInMillis();
	  assertEquals("getFromMillis failed","2015/04/02 13:14:50",DateTime.getFromMillis(ms).toYmdHms());
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#elapsedAsHMS()}.
   */
 public void testElapsedHMS(){
	  long ms = 31415926;
	  String test = DateTime.elapsedAsHMS(ms);
	  assertEquals("Elapsed to HMS failed","08:43:36",test);
  }
  
  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#toYmd()}.
   */
  public void testToYmd()
  {
    String test = dtm.toYmd();
    wr(test);
    assertEquals("toYmd() failed", "2015/04/02" , test );
  }
  
  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#toHms()}.
   */
  public void testToHms()
  {
    String test = dtm.toHms();
    wr(test);
    assertEquals("toHms() failed", "13:14:50" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#toYmdHms()}.
   */
  public void testToYmdHms()
  {
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("toYmd() failed", "2015/04/02 13:14:50" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#addDays(int)}.
   */
  public void testAddDays()
  {
    dtm.addDays(40);
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("addDays() failed", "2015/05/12 13:14:50" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#addMonths(int)}.
   */
  public void testAddMonths()
  {
    dtm.addMonths(10);
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("addMonths() failed", "2016/02/02 13:14:50" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#goStartOfDay()}.
   */
  public void testGoStartOfDay()
  {
    dtm.goStartOfDay();
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("goStartOfDay() failed", "2015/04/02 00:00:00" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#goEndOfDay()}.
   */
  public void testGoEndOfDay()
  {
    dtm.goEndOfDay();
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("goEndOfDay() failed", "2015/04/02 23:59:59" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#goStartOfMonth()}.
   */
  public void testGoStartOfMonth()
  {
    dtm.goStartOfMonth();
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("goStartOfMonth() failed", "2015/04/01 00:00:00" , test );
  }

  /**
   * Test method for {@link za.co.discoverylife.desktop.util.DateTime#goEndOfMonth()}.
   */
  public void testGoEndOfMonth()
  {
    dtm.goEndOfMonth();
    String test = dtm.toYmdHms();
    wr(test);
    assertEquals("goEndOfMonth() failed", "2015/04/30 23:59:59" , test );
  }
}
