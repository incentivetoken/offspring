package com.dgex.offspring.application.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.dgex.offspring.config.Config;

public class Activator implements BundleActivator {

  private static Logger logger;

  @Override
  public void start(BundleContext context) throws Exception {

    String log4jconfig = new File(System.getProperty("eclipse.launcher"))
        .getParentFile().getAbsolutePath()
        + File.separator
        + "log4j.properties";
    PropertyConfigurator.configure(log4jconfig);

    logger = Logger.getLogger(Activator.class);

    // Config.initialize();

    String os = System.getProperty("os.name") + " "
        + System.getProperty("os.version") + " "
        + System.getProperty("os.arch");
    String java = "Java " + System.getProperty("java.version") + " ("
        + System.getProperty("java.vendor") + ")";

    logger.info("Starting Offspring NXT Client");
    logger.info(java + " " + os);
    logger.info("offspring.config = "
        + Config.getInstallPath("offspring.config"));
    logger.info("log4j config = " + log4jconfig);
  }

  @Override
  public void stop(BundleContext context) throws Exception {}

  private static File findFileInParent(File parent, String filename) {
    File file = new File(parent.getAbsoluteFile() + File.separator + filename);
    if (file.exists())
      return file;

    if (parent.getParentFile() != null)
      return findFileInParent(parent.getParentFile(), filename);

    return null;
  }

  private void writeConfigFile(Properties properties, File file) {
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
