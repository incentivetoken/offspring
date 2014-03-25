package com.dgex.offspring.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

public class FolderCompare {

  static String[] ignored = new String[] {

  "nxt_db" + File.separator + "nxt.h2.db",

  "nxt_db" + File.separator + "nxt.lock.db",

  "nxt_db" + File.separator + "nxt.trace.db"

  };

  private final IProgressMonitor monitor;

  private final UpdateLog updateLog;

  public FolderCompare(IProgressMonitor monitor, UpdateLog log) {
    this.monitor = monitor;
    this.updateLog = log;
  }

  public List<File> compare(File installDir, File backupDir) throws IOException {
    List<File> changed = new ArrayList<File>();
    List<File> files = new ArrayList<File>(FileUtils.listFiles(installDir, null,
        true));
    monitor.beginTask("Compare files", files.size());
    for (int i = 0; i < files.size(); i++) {

      /* update worked amount every 5 files */
      if (i % 5 == 0)
        monitor.worked(5);

      File file = files.get(i);
      if (file.isDirectory())
        continue;

      /* skip files in ignore list */
      if (ignoredFile(installDir, file))
        continue;

      if (changed(installDir, backupDir, file))
        changed.add(file);
    }
    monitor.done();
    return changed;
  }

  /*
   * Checks that a file (child or descendant) in the install directory either
   * changed (compared to the file in the backup folder) or was added since the
   * moment we made the backup.
   */
  private boolean changed(File installDir, File backupDir, File installFile)
      throws IOException {

    // To get a file in srcdir but then in destdir, replace the srcdir path with
    // destdir path in srcfile
    String srcdirPath = installDir.getAbsolutePath();
    String destdirPath = backupDir.getAbsolutePath();
    File backupFile = new File(installFile.getAbsolutePath().replace(
        srcdirPath, destdirPath));

    // if the file is not in backup dir it has been added
    if (!backupFile.exists()) {
      updateLog.logMessage(getClass().getName(),
          "File added " + installFile.getPath());
      return true;
    }

    // do a file compare
    boolean changed = !FileUtils.contentEquals(installFile, backupFile);
    if (changed) {
      updateLog.logMessage(getClass().getName(),
          "File changed " + installFile.getPath());
    }
    return changed;
  }

  private boolean ignoredFile(File srcdir, File file) {
    for (int i = 0; i < ignored.length; i++) {
      File f = new File(srcdir.getAbsoluteFile() + File.separator + ignored[i]);
      if (file.equals(f))
        return true;
    }
    return false;
  }

}
