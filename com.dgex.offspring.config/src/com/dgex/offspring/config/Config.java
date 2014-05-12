package com.dgex.offspring.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class Config {

  public static final String VERSION = "0.4.5";
  public static final String TITLE = "Offspring v" + VERSION;

  static Logger logger = Logger.getLogger(Config.class);

  /* Encrypted messages start with this number */
  public static byte[] MAGIC_ENCRYPTED_MESSAGE_NUMBER_XOR = new byte[] { 0x43,
      0x52, 0x59, 0x50, 0x54, 0x45, 0x44, 0x21 }; // CRYPT!

  /* Non encrypted messages start with this number (Invisible Characters) */
  public static byte[] MAGIC_UNENCRYPTED_MESSAGE_NUMBER = new byte[] { 0x4d,
      0x45, 0x53, 0x53, 0x41, 0x47, 0x45, 0x21 };

  /* AES Encrypted messages start with this number */
  public static byte[] MAGIC_ENCRYPTED_MESSAGE_NUMBER_AES = new byte[] { 0x41,
      0x45, 0x53, 0x43, 0x52, 0x59, 0x50, 0x21 };   // AESCRYP!

  /* NXT config settings */
  public static boolean nxtIsTestNet = false;

  /* Last updated from 0.9.9 */
  public static String testNetPeers = "bug.airdns.org; node10.mynxtcoin.org; node9.mynxtcoin.org; testnxt-jp.cloudapp.net; testnxt-ne.cloudapp.net; testnxt-we.cloudapp.net";

  public static File certificate;
  public static String offspring_charset = "UTF-8";
  public static Properties properties = new Properties();
  public static long uptime = System.currentTimeMillis();

  public static File installPath;
  public static File appPath;
  public static File defaultConfig;
  private static File dbPath;
  private static File dbTestPath;
  private static File logPath;

  static {
    initialize();
  }

  private Config() {}

  /* The window title for the main window */
  public static String getWindowTitle(String nxtVersion) {
    return TITLE + " (nxt " + nxtVersion + ")"
        + (nxtIsTestNet ? " >>> TEST NET <<< (DO NOT USE REAL ACCOUNTS)" : "");
  }

  public static void initialize() {

    /* Junit test run */
    if (System.getProperty("eclipse.launcher") == null)
      return;

    installPath = new File(System.getProperty("eclipse.launcher"))
        .getParentFile();
    appPath = new File(System.getProperty("user.home") + File.separator
        + "Offspring");

    appPath.mkdirs();
    logPath = getAppPath("nxt.log");
    dbPath = getAppPath("nxt_db/nxt");
    dbTestPath = getAppPath("nxt_test_db/nxt");
    dbPath.getParentFile().mkdirs();

    defaultConfig = findFileInParent(installPath, "offspring.config");
    if (!defaultConfig.exists()) {
      MessageDialog.openError(Display.getDefault().getActiveShell(), "Error",
          "Missing " + defaultConfig.getPath());
      System.exit(-1);
    }

    try {
      properties.load(new FileReader(defaultConfig));
    }
    catch (FileNotFoundException e) {
      logger.error(e);
      System.exit(-1);
    }
    catch (IOException e) {
      logger.error(e);
      System.exit(-1);
    }

    /* If there is no local config file in the HOME folder we write our own */

    if (!getAppPath("offspring.config").exists()) {
      logger.info(getAppPath("offspring.config") + " DOES NOT EXIST");
      // String value =
      // "jdbc:h2:$PATH$;DB_CLOSE_ON_EXIT=FALSE;DATABASE_EVENT_LISTENER='com.dgex.offspring.nxtCore.h2.H2Listener'";
      String value = "jdbc:h2:$PATH$;DB_CLOSE_ON_EXIT=FALSE";
      value = value.replace("$PATH$", dbPath.getAbsolutePath());
      logger.info("JDBC = " + value);
      properties.put("nxt.dbUrl", value);

      // value =
      // "jdbc:h2:$PATH$;DB_CLOSE_ON_EXIT=FALSE;DATABASE_EVENT_LISTENER='com.dgex.offspring.nxtCore.h2.H2Listener'";
      value = "jdbc:h2:$PATH$;DB_CLOSE_ON_EXIT=FALSE";
      value = value.replace("$PATH$", dbTestPath.getAbsolutePath());
      properties.put("nxt.testDbUrl", value);

      properties.put("nxt.log", logPath.getAbsolutePath());
      writeConfigFile(properties, getAppPath("offspring.config"));
    }
    else {
      logger.info(getAppPath("offspring.config") + " EXISTS");
      Properties home = new Properties();
      try {
        home.load(new FileReader(getAppPath("offspring.config")));
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
        System.exit(-1);
      }
      catch (IOException e) {
        e.printStackTrace();
        System.exit(-1);
      }
      for (String key : home.stringPropertyNames()) {
        properties.put(key, home.getProperty(key));
      }

      /* ALLWAYS OVERWRITE DB PATH - REMOVE H2 LISTENER - IT'S SLOW */
      /* THIS IS REQUIRED TO FIX OLD INSTALLATIONS */
      String value = "jdbc:h2:$PATH$;DB_CLOSE_ON_EXIT=FALSE";
      value = value.replace("$PATH$", dbPath.getAbsolutePath());
      properties.put("nxt.dbUrl", value);

      value = "jdbc:h2:$PATH$;DB_CLOSE_ON_EXIT=FALSE";
      value = value.replace("$PATH$", dbTestPath.getAbsolutePath());
      properties.put("nxt.testDbUrl", value);
    }

    if (Config.nxtIsTestNet == true) {

      logger.info("Adding TEST NET is TRUE");

      properties.put("nxt.testnetPeers", testNetPeers);
      properties.put("nxt.isTestnet", "true");
    }

    writeConfigFile(properties, getAppPath("AUTO_GENERATED.offspring.config"));

    System.setProperty("nxt-default.properties",
        getAppPath("AUTO_GENERATED.offspring.config").getAbsolutePath());

    certificate = findFileInParent(installPath, "offspring.crt");
    if (Config.certificate == null || !Config.certificate.exists()) {
      MessageDialog.openError(Display.getDefault().getActiveShell(), "Error",
          "Missing offspring.crt");
      System.exit(-1);
    }
    debugDump();
  }

  private static void debugDump() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n========================================================");
    sb.append("\nOffspring Configuration Settings");
    sb.append("\n========================================================");

    Enumeration<Object> keys = Config.properties.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = (String) Config.properties.get(key);
      if (value.length() > 100) {
        value = value.substring(0, 100) + " ...";
      }
      sb.append("\n" + key + "=" + value);
    }
    logger.info(sb.toString());
  }

  private static File findFileInParent(File parent, String filename) {
    File file = new File(parent.getAbsolutePath() + File.separator + filename);
    if (file.exists())
      return file;

    if (parent.getParentFile() != null)
      return findFileInParent(parent.getParentFile(), filename);

    return null;
  }

  public static File getInstallPath(String path) {
    return getPath(installPath, path);
  }

  public static File getAppPath(String path) {
    return getPath(appPath, path);
  }

  private static File getPath(File file, String path) {
    String[] paths = path.split("/");
    StringBuilder sb = new StringBuilder();
    sb.append(file.getAbsolutePath());
    for (int i = 0; i < paths.length; i++) {
      sb.append(File.separator).append(paths[i]);
    }
    return new File(sb.toString());
  }

  private static void writeConfigFile(Properties properties, File file) {
    OutputStream output = null;
    try {
      output = new FileOutputStream(file);
      properties.store(output, null);
    }
    catch (IOException io) {
      io.printStackTrace();
    }
    finally {
      if (output != null) {
        try {
          output.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
