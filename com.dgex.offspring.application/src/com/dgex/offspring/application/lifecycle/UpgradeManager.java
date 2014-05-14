package com.dgex.offspring.application.lifecycle;

import java.io.File;
import java.io.IOException;

import nxt.Constants;
import nxt.Nxt;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.config.Config;

public class UpgradeManager {
  
  static String title1 = "Offspring Upgrade";
  static String message1 = "It seems NXT was updated %MESSAGE%\n\n"
      + "We recommend to delete the blockchain.\n\n"
      + "Do you want Offspring to delete and then re-download the blockchain?";

  static String title2 = "Unable to delete blockchain";
  static String message2 = "Offspring was unable to delete the blockchain.\n\n"
      + "You should delete the blockchain by hand and then start Offspring.\n\n"
      + "Do you want to shutdown Offspring so you can manualy delete the blockchain?";

  private static File getVersionFile() {
    return new File(Config.appPath.getAbsolutePath() + File.separator
        + "VERSION");
  }

  private static String readVersion() {
    try {
      return FileUtils.readFileToString(getVersionFile()).trim();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void writeVersion(String version) {
    try {
      FileUtils.writeStringToFile(getVersionFile(), version);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void checkForUpdate(Shell shell) {
    String oldVersion = readVersion();
    oldVersion = oldVersion == null ? "-" : oldVersion;
        
    if (Nxt.VERSION.equalsIgnoreCase(oldVersion)) {
      String msg = message1.replaceFirst("%MESSAGE%", "from '" + oldVersion + "' to '" + Nxt.VERSION + "'");
      if (MessageDialog.openQuestion(shell, title1, msg)) {
        System.out.println("About to delete the blockchain");
        File dbDir;
        if (Constants.isTestnet) {
          dbDir = Config.getAppPath("nxt_db");
        }
        else {
          dbDir = Config.getAppPath("nxt_test_db");
        }
        if (dbDir != null && dbDir.isDirectory()) {
          try {
            for (File file : dbDir.listFiles()) {
              FileDeleteStrategy.FORCE.delete(file);
            }
          }
          catch (IOException e) {
            if (MessageDialog.openQuestion(shell, title2, message2)) {
              System.exit(0);
            }
          }
        }
      }
    }
  }

  public static void notifySuccessfullShutdown() {
    writeVersion(Nxt.VERSION);
  }

}
