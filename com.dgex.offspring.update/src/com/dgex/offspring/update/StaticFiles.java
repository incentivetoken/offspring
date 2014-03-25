package com.dgex.offspring.update;

import java.util.ArrayList;
import java.util.List;

public class StaticFiles {

  /*
   * Lists of files that are not allowed to change between updates. All files
   * are relative to the install directory.
   */

  static List<String> paths;

  {
    paths = new ArrayList<String>();

    /* windows - no change allowed */

    paths.add("offspring.ini");
    paths.add("offspring.exe");
    paths.add("offspring.crt");     // <-- we should not be sending a new
    // certificate each time
    paths.add("offspring.config");
    paths.add("nxt.config");
    paths.add("log4j.properties");
    paths.add("keystore");
    paths.add("jetty.config");
    paths.add("eclipsec.exe");

    /* plugins path - all changes must be verified jar */

    /* mac - no change allowed */

    paths.add("offspring.crt");     // <-- we should not be sending a new
    // certificate each time
    paths.add("offspring.config");
    paths.add("offspring"); // link to program
    paths.add("nxt.config");
    paths.add("log4j.properties");
    paths.add("keystore");
    paths.add("jetty.config");
    paths.add("offspring.app/*");   // <-- nothing in app folder may change

    /* plugins path - all changes must be verified jar */

    paths.add("jetty.config");
    paths.add("keystore");
    paths.add("log4j.properties");
    paths.add("nxt.config");
    paths.add("offspring");
    paths.add("offspring.config");
    paths.add("offspring.crt");
    paths.add("offspring.ini");

    /* plugins path - all changes must be verified jar */
  }

}
