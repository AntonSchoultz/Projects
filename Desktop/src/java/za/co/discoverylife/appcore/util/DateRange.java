package za.co.discoverylife.appcore.util;

import java.util.Date;

import za.co.discoverylife.appcore.field.MetaFieldInfo;
import za.co.discoverylife.appcore.field.ValueObject;

/**
 * Value object to hold date range
 * 
 * @author Anton Schoultz
 *
 */
public class DateRange extends ValueObject
{

  @MetaFieldInfo(label = "From", hint = "Starting date")
  public Date fromDate = new Date();

  @MetaFieldInfo(label = "To", hint = "Ending date")
  public Date toDate = new Date();

  /** CONSTRUCTOR - defaults from and to to today's date */
  public DateRange()
  {
  }

  /**
   * CONSTRUCTOR that accepts to and from dates
   * 
   * @param fromDate Date object with from date/time
   * @param toDate Date object with to date/time
   */
  public DateRange(Date fromDate, Date toDate)
  {
    super();
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  /** Returns from-dateTime as a DateTime object */
  public DateTime getFromDateTime()
  {
    return new DateTime(fromDate);
  }

  /** Returns to-dateTime as a DateTime object */
  public DateTime getToDateTime()
  {
    return new DateTime(toDate);
  }

  /** Returns from-dateTime as a Date object */
  public Date getFromDate()
  {
    return fromDate;
  }

  /** Sets the from-date and time to match the supplied Date object */
  public void setFromDate(Date fromDate)
  {
    this.fromDate = fromDate;
  }

  /** Sets the from-date and time to match the supplied DateTime object */
  public void setFromDate(DateTime fromDate)
  {
    this.fromDate = fromDate.getTime();
  }

  public Date getToDate()
  {
    return toDate;
  }

  /** Sets the to-date and time to match the supplied Date object */
  public void setToDate(Date toDate)
  {
    this.toDate = toDate;
  }

  /** Sets the to-date and time to match the supplied DateTime object */
  public void setToDate(DateTime toDate)
  {
    this.toDate = toDate.getTime();
  }

  /** Returns true if the supplied date is within range */
  public boolean isInRange(Date date)
  {
    return isInRange(date.getTime());
  }

  /** Returns true if the supplied date is within range */
  public boolean isInRange(long dateMillis)
  {
    return (dateMillis >= fromDate.getTime() && dateMillis <= toDate.getTime());
  }

  /** Sets starting date's time to 00:00:00.000 and ending date's time to 23:59:59.999 */
  public DateRange setTimes()
  {
    fromDate = (new DateTime(fromDate)).goStartOfDay().getTime();
    toDate = (new DateTime(toDate)).goEndOfDay().getTime();
    return this;
  }

  /** Returns range as 'YYYY/MM/DD hh:mm:ss - YYYY/MM/DD hh:mm:ss' */
  public String toStringYmdHms()
  {
    return (new DateTime(fromDate)).toStringYmdHms() + " - "
        + (new DateTime(toDate)).toStringYmdHms();
  }

  /** Returns range as 'YYYY/MM/DD - YYYY/MM/DD' */
  public String toStringYmd()
  {
    return (new DateTime(fromDate)).toStringYmd() + " - "
        + (new DateTime(toDate)).toStringYmd();
  }

  /** Returns range as 'YYYY/MM/DD hh:mm:ss - YYYY/MM/DD hh:mm:ss' */
  public String toString()
  {
    return toStringYmdHms();
  }
}
