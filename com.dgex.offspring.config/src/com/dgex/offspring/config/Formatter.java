package com.dgex.offspring.config;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class Formatter {

  /*
   * This is how NXT creates transaction & block timestamps.
   * 
   *    Calendar calendar = Calendar.getInstance();
   *    calendar.set(Calendar.ZONE_OFFSET, 0);
   *    calendar.set(Calendar.YEAR, 2013);
   *    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
   *    calendar.set(Calendar.DAY_OF_MONTH, 24);
   *    calendar.set(Calendar.HOUR_OF_DAY, 12);
   *    calendar.set(Calendar.MINUTE, 0);
   *    calendar.set(Calendar.SECOND, 0);
   *    calendar.set(Calendar.MILLISECOND, 0);
   *    epochBeginning = calendar.getTimeInMillis();
   * 
   *    static int getEpochTime(long time) {
   *      return (int)((time - epochBeginning + 500) / 1000);
   *    }
   * 
   *    Transaction.timestamp = getEpochTime(System.currentTimeMillis());
   *    
   * NXT web interface translates timestamp like this.
   * 
   *    function formatTimestamp(timestamp) {
   *      return (new Date(Date.UTC(2013, 10, 24, 12, 0, 0, 0) + timestamp * 1000)).toLocaleString();
   *    }
   * 
   * The way NRS translates the timestamp is actually wrong! 
   * Since they dont include the 500 milliseconds that where added in getEpochTime function. 
   * 
   * What should be done is this ...
   * 
   *    int timestamp = Transaction.timestamp;
   *    Date date = new Date((epochBeginning - 500) * 1000)
   * 
   */

  private static long epochBeginning = 0l;

  private static long getEpochBeginning() {
    if (epochBeginning == 0l) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.ZONE_OFFSET, 0);
      calendar.set(Calendar.YEAR, 2013);
      calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
      calendar.set(Calendar.DAY_OF_MONTH, 24);
      calendar.set(Calendar.HOUR_OF_DAY, 12);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      epochBeginning = calendar.getTimeInMillis();
    }
    return epochBeginning;
  }

  public static Date formatTimestamp(Long timestamp) {
    return new Date((getEpochBeginning() - 500) + timestamp * 1000);
  }

  public static String formatTimestampLocale(Long timestamp) {
    Date date = formatTimestamp(timestamp);
    DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
    DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    return dateFormatter.format(date) + " " + timeFormatter.format(date);
  }

  /*

  function formatAmount(amount) {
    var digits=[], formattedAmount = "", i, cents = amount % 100;
    amount = Math.floor(amount / 100);
    do {
      digits[digits.length] = amount % 10;
      amount = Math.floor(amount / 10);
    } while (amount > 0);
    for (i = 0; i < digits.length; i++) {
      if (i > 0 && i % 3 == 0) {
        formattedAmount = "'" + formattedAmount;
      }
      formattedAmount = digits[i] + formattedAmount;
    }
    return formattedAmount + "<span class='cents'>." + Math.floor(cents / 10) + "" + cents % 10 + "</span>";
  }

  */

  public static Double formatAmount(Long amount) {

    return (double) 0;
  }

  public static String formatBaseTarget(Long target) {
    if (target >= 100000) {
      return new Long(Math.round(target / 1000)).toString();
    }
    else if (target >= 10000) {
      return new Long(Math.round(target / 100) / 10).toString();
    }
    else if (target >= 1000) {
      return new Long(Math.round(target / 10) / 100).toString();
    }
    return new Long(target / 1000).toString();
  }

  public static String readableFileSize(long size) {
    if (size <= 0)
      return "0";
    final String[] units = new String[] {
        "B", "KB", "MB", "GB", "TB"
    };
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

}
