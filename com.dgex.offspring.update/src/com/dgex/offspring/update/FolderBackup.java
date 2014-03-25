package com.dgex.offspring.update;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

public class FolderBackup {

  static Logger logger = Logger.getLogger(FolderBackup.class);
  private final UpdateLog updateLog;
  private final IProgressMonitor monitor;

  private static class ListenerFilter implements FileFilter {

    private final IProgressMonitor monitor;

    public ListenerFilter(IProgressMonitor monitor) {
      this.monitor = monitor;
    }

    @Override
    public boolean accept(File pathname) {
      monitor.worked(1);
      return true;
    }
  }

  public FolderBackup(IProgressMonitor monitor, UpdateLog updateLog) {
    this.monitor = monitor;
    this.updateLog = updateLog;
  }

  public boolean backup(File installDir, File backupDir) {
    /* Copy all files from the install dir to the backup dir */
    try {
      List<File> files = new ArrayList<File>(FileUtils.listFiles(installDir,
          null, true));
      monitor.beginTask("Backup Installation", files.size());
      FileUtils.copyDirectory(installDir, backupDir,
          new ListenerFilter(monitor));
    }
    catch (IOException e) {
      updateLog.logError(getClass().getName(),
          "Could not backup install directory");
      return false;
    }
    finally {
      monitor.done();
    }
    return true;
  }

  /* The install dir is the root directory for the Offspring installation. */
  public File getInstallDir() {
    try {
      return new File(Platform.getInstallLocation().getURL().toURI());
    }
    catch (URISyntaxException e) {
      logger.error("Could not find install dir", e);
    }
    return null;
  }

  /* The backup dir is a folder next to the install dir */
  public File getBackupDir() {
    return getUniqueDir(getInstallDir());
  }

  private static File getUniqueDir(File base) {
    int count = 1;
    File test = base;
    while (test.exists()) {
      test = new File(base.getParent() + File.separator + base.getName()
          + ".BACKUP." + String.format("%03d", count++));
    }
    return test;
  }

}
