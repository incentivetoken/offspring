package com.dgex.offspring.application.lifecycle;

import java.io.File;
import java.io.IOException;

import nxt.Nxt;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.config.Config;
import com.dgex.offspring.messages.Messages;

public class UpgradeManager {
  
  static String sJSONAlias = "OFFSPRING-LATEST-VERSION-DATA";
  static int MIN_SECONDS_OLD_FOR_ALIAS = 60 * 60;
  static boolean DEBUG = true;

  static Logger logger = Logger.getLogger(UpgradeManager.class);
  static UISynchronize sync = null;

  public static void init(UISynchronize sync) {
    UpgradeManager.sync = sync;
  }

  /*
   * Checks at startup if there has been a NXT update. Offers to delete the
   * blockchain if it was updated.
   */
  public static void checkForNXTUpdate(Shell shell) {

    String oldVersion = readVersion();
    oldVersion = oldVersion == null ? "-" : oldVersion;
    if (Nxt.VERSION.equalsIgnoreCase(oldVersion) == false) {
      if (MessageDialog.openQuestion(shell,
          Messages.UpgradeManager_delete_blockchain_title,
          Messages.UpgradeManager_delete_blockchain_msg)) {
        System.out.println("About to delete the blockchain");
        File[] dbDirs = { Config.getAppPath("nxt_db"),
            Config.getAppPath("nxt_test_db") };
        for (File dbDir : dbDirs) {
          if (dbDir != null && dbDir.isDirectory()) {
            try {
              for (File file : dbDir.listFiles()) {
                FileDeleteStrategy.FORCE.delete(file);
              }
            }
            catch (IOException e) {
              if (MessageDialog.openQuestion(shell,
                  Messages.UpgradeManager_fail_delete_blockchain_title,
                  Messages.UpgradeManager_fail_delete_blockchain_msg)) {
                System.exit(0);
              }
            }
          }
        }
      }
    }
  }

  private static File getVersionFile() {
    return new File(Config.appPath.getAbsolutePath() + File.separator + "VERSION");
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

  public static void notifySuccessfullShutdown() {
    writeVersion(Nxt.VERSION);
  }
}
