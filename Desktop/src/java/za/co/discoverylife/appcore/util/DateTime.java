package za.co.discoverylife.appcore.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Extends GregorianCalendar to add some utility/helper methods.
 * 
 * @author Anton Schoultz - 2013
 */
public class DateTime extends GregorianCalendar
{
  private static final long serialVersionUID = 6560752850362354556L;

  private static final SimpleDateFormat sdfYmdHms = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  private static final SimpleDateFormat sdfHms = new SimpleDateFormat("HH:mm:ss");
  private static final SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy/MM/dd");

  /** 
   * CONSTRUCTOR - Default
   */
  public DateTime()
  {
    goNow();
  }

  /**
   * CONSTRUCTOR that accepts a string of the form 'yyyy/mm/dd[ hh:mm:ss]'
   * @param value String value of the date as YYYY/MM/DD[ hh:mm:ss]
   */
  public DateTime(String value)
  {
    parse(value);
  }

  /** Returns DateTime object for current date/time */
  public static DateTime now()
  {
    return new DateTime();
  }

  /** Returns a date corresponding to the start of the month of the supplied date */
  public static Date goStartOfMonth(Date date)
  {
    return (new DateTime(date).goStartOfMonth()).getTime();
  }

  /** Returns a date corresponding to the end of the month of the supplied date */
  public static Date goEndOfMonth(Date date)
  {
    return (new DateTime(date).goEndOfMonth()).getTime();
  }

  /** Returns a date corresponding to the start of the day of the supplied date */
  public static Date goStartOfDay(Date date)
  {
    return (new DateTime(date).goStartOfDay()).getTime();
  }

  /** Returns a date corresponding to the end of the day of the supplied date */
  public static Date goEndOfDay(Date date)
  {
    return (new DateTime(date).goEndOfDay()).getTime();
  }

  /** CONSTRUCTOR which accepts a Date */
  public DateTime(Date date)
  {
    setToDate(date);
  }

  /** Returns the value as a Date object */
  public Date getAsDate()
  {
    return getTime();
  }

  /** Set from supplied Date object */
  public void setToDate(Date date)
  {
    setTime(date);
  }

  /** Parse provided string of 'YYYY/MM/DD HH:mm:ss' or 'YYYY/MM/DD' */
  public DateTime parse(String YMDhms)
  {
    if ( YMDhms == null )
    {
      goNow();
      return this;
    }
    try
    {
      switch (YMDhms.length())
      {
        case 10 :
          setTime(sdfYmd.parse(YMDhms));
          break;
        case 19 :
          setTime(sdfYmdHms.parse(YMDhms));
          break;
        default :
          goNow();
      }
    }
    catch (ParseException e)
    {
      goNow();
    }
    return this;
  }

  /** Over-ride to string to return YMD hms format */
  public String toString()
  {
    return toStringYmdHms();
  }

  /** Return the date as 'YYYY/MM/DD' */
  public String toStringYmd()
  {
    return sdfYmd.format(getTime());
  }

  /** Return the time as 'HH:MM:SS' */
  public String toStringHms()
  {
    return sdfHms.format(getTime());
  }

  /** Return the date and time as 'YYYY/MM/DD HH:MM:SS' */
  public String toStringYmdHms()
  {
    return sdfYmdHms.format(getTime());
  }

  /** Add a number of days (may be negative) */
  public DateTime addDays(int adjust)
  {
    add(Calendar.DAY_OF_MONTH, adjust);
    return this;
  }

  /** Add a number of months (may be negative) */
  public DateTime addMonths(int adjust)
  {
    add(Calendar.MONTH, adjust);
    return this;
  }

  /** Set the date to current date and time */
  public DateTime goNow()
  {
    setTime(new Date());
    return this;
  }

  /** Zero all time fields '00:00:00.000' */
  public DateTime goStartOfDay()
  {
    set(Calendar.MILLISECOND, 0);
    set(Calendar.SECOND, 0);
    set(Calendar.MINUTE, 0);
    set(Calendar.HOUR_OF_DAY, 0);
    return this;
  }

  /** Set all time fields to maximum value '23:59:59.999' */
  public DateTime goEndOfDay()
  {
    set(Calendar.MILLISECOND, 999);
    set(Calendar.SECOND, 59);
    set(Calendar.MINUTE, 59);
    set(Calendar.HOUR_OF_DAY, 23);
    return this;
  }

  /** Sets to start of the month */
  public DateTime goStartOfMonth()
  {
    goStartOfDay();
    set(Calendar.DAY_OF_MONTH, 1);
    return this;
  }

  /** Sets to the last millisecond of the last day of the month */
  public DateTime goEndOfMonth()
  {
    goEndOfDay();
    int maxDay = getActualMaximum(Calendar.DAY_OF_MONTH);
    set(Calendar.DAY_OF_MONTH, maxDay);
    return this;
  }

  /** Add a number of years (may be negative) */
  public DateTime addYears(int adjust)
  {
    add(Calendar.YEAR, adjust);
    return this;
  }

  public int getYear()
  {
    return get(Calendar.YEAR);
  }

  public int getMonth()
  {
    return get(Calendar.MONTH);
  }

  public int getDay()
  {
    return get(Calendar.DAY_OF_MONTH);
  }

}
