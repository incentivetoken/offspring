package com.dgex.offspring.nxtCore.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import nxt.Nxt;

import org.apache.commons.io.FileUtils;

public class NxtLoader {

  static Properties properties = new Properties();
  static File nxtPropertiesFile = new File(
      "../com.dgex.offspring.nxtCore/nxt/conf/nxt-default.properties");
  static File dbDir = new File("target/nxt_db");
  static String dbUrl = "jdbc:h2:" + dbDir.getAbsolutePath()
      + ";DB_CLOSE_ON_EXIT=FALSE";
  static File myPropertiesFile = new File("my.properties");

  static void init() {
    if (dbDir.exists()) {
      try {
        FileUtils.deleteDirectory(dbDir);
      }
      catch (IOException e1) {
        e1.printStackTrace();
        return;
      }
    }

    try {
      properties.load(new FileReader(nxtPropertiesFile));
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      return;
    }
    catch (IOException e) {
      e.printStackTrace();
      return;
    }

    properties.put("nxt.testnetPeers", "");
    properties.put("nxt.isTestnet", "true");
    properties.put("nxt.enableAPIServer", "false");
    properties.put("nxt.enableUIServer", "false");
    properties.put("nxt.testDbUrl", dbUrl);

    writeConfigFile(properties, myPropertiesFile);

    System.setProperty("nxt-default.properties",
        myPropertiesFile.getAbsolutePath());

    System.out.println("Starting NXT");

    Nxt.init(properties);

    System.out.println("Successfully started NXT");
  }

  static void shutdown() {
    try {
      Nxt.shutdown();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    if (dbDir.exists()) {
      try {
        FileUtils.deleteDirectory(dbDir);
      }
      catch (IOException e1) {
        e1.printStackTrace();
        return;
      }
    }
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
