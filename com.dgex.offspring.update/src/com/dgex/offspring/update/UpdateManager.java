package com.dgex.offspring.update;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

public class UpdateManager {

  static Logger logger = Logger.getLogger(UpdateManager.class);
  private final Map<File, Long> map = null;
  private final File dir = null;
  private final IProgressMonitor monitor;
  private final FolderBackup backup;
  private final UpdateLog updateLog;
  private final FolderCompare compare;
  private X509Certificate certificate;
  private List<File> changedFiles = null;
  private final X509CertificateVerify jarVerifier;

  public UpdateManager(IProgressMonitor monitor) {
    this.monitor = monitor;
    this.updateLog = new UpdateLog();
    this.backup = new FolderBackup(monitor, updateLog);
    this.compare = new FolderCompare(monitor, updateLog);
    this.jarVerifier = new X509CertificateVerify(monitor, updateLog);
  }

  /* Step 0 */
  public boolean initialize() {
    certificate = getCertificate();
    if (certificate == null) {
      updateLog.logError(getClass().getName(),
          "Could not get X509Certificate certificate");
      return false;
    }
    return true;
  }

  /*
   * (1)
   * 
   * Create a full backup of all installation files and place these files in a
   * temporary backup folder outside the installation directory.
   */
  public boolean createFullBackup() {
    return backup.backup(backup.getInstallDir(), backup.getBackupDir());
  }

  /*
   * (2)
   * 
   * Compare all files in the installation directory to those in the backup
   * folder. We are interested in all added and all updated files. Removed files
   * are not of interest.
   * 
   * After this operation in changedFiles we have a list of all files that
   * either changed or where added during the update.
   */
  public boolean analyzeChangedFiles() {
    try {
      logger.info("==================================================");
      logger.info("BEFORE analyzeChangedFiles");
      for (File file : changedFiles) {
        logger.info(file.getAbsoluteFile());
      }

      changedFiles = compare.compare(backup.getInstallDir(),
          backup.getBackupDir());

      logger.info("==================================================");
      logger.info("AFTER analyzeChangedFiles");
      for (File file : changedFiles) {
        logger.info(file.getAbsoluteFile());
      }
    }
    catch (IOException e) {
      updateLog.logError(getClass().getName(),
          "IOError while doing file compare");
      return false;
    }
    return true;
  }

  /*
   * (3)
   * 
   * All jar files in Offspring are signed with the offspring certificate. We
   * visit all files in the changedFiles list and verify if that file could be
   * verified as signed by us.
   * 
   * All verified files are removed from the list of changed files, what remains
   * in the changedFiles are files that still need verification.
   */
  public boolean verifyJarCertificates() {

    logger.info("==================================================");
    logger.info("BEFORE verifyJarCertificates");
    for (File file : changedFiles) {
      logger.info(file.getAbsoluteFile());
    }

    jarVerifier.verify(changedFiles, certificate);

    logger.info("==================================================");
    logger.info("AFTER verifyJarCertificates");
    for (File file : changedFiles) {
      logger.info(file.getAbsoluteFile());
    }

    return true;
  }

  /*
   * (4)
   * 
   * Not only jar files are distributed through updates also log files and
   * configuration files these files are a lot less sensitive than the
   * executable jar files.
   * 
   * Executables and dynamic libraries on the other hand are not allowed to
   * change between updates. Also ini files that control
   */
  public boolean verifyUnsignedUpdates() {

    logger.info("These files are left over and are not verified");
    for (File file : changedFiles) {
      logger.info(file.getAbsoluteFile());
    }

    return true;
  }

  public boolean isVerified() {
    return true;
  }

  public void createErrorLog() {}

  public void rolebackUpdate() {}

  public void printDifference() throws IOException {
    Map<File, Long> map2 = generateChecksumMap(dir);
    List<File> all = combinedKeys(map, map2);
    Collections.sort(all);

    List<File> removed = new ArrayList<File>();
    List<File> added = new ArrayList<File>();
    List<File> changed = new ArrayList<File>();

    for (File file : all) {
      if (!map2.containsKey(file)) {
        removed.add(file);
      }
      else if (!map.containsKey(file)) {
        added.add(file);
      }
      else {
        long checksum = FileUtils.checksumCRC32(file);
        if (!map.get(file).equals(checksum)) {
          changed.add(file);
        }
      }
    }

    for (File file : changed)
      logger.info("CHANGED  - " + file.getAbsolutePath());

    for (File file : removed)
      logger.info("REMOVED  - " + file.getAbsolutePath());

    for (File file : added)
      logger.info("ADDED    - " + file.getAbsolutePath());
  }

  /* Returns the combined list of keys in two hasmaps */
  private List<File> combinedKeys(Map<File, Long> map1, Map<File, Long> map2) {
    Map<File, Long> combined = new HashMap<File, Long>();
    for (File key : map1.keySet())
      combined.put(key, 1l);
    for (File key : map2.keySet())
      combined.put(key, 1l);
    return new ArrayList<File>(combined.keySet());
  }

  private Map<File, Long> generateChecksumMap(File directory)
      throws IOException {
    Map<File, Long> map = new HashMap<File, Long>();
    Iterator<File> files = FileUtils.iterateFiles(directory, null, true);
    while (files != null && files.hasNext()) {
      File file = files.next();
      long checksum = FileUtils.checksumCRC32(file);
      map.put(file, checksum);
    }
    return map;
  }

  private X509Certificate getCertificate() {
    try {
      File dir = new File(Platform.getInstallLocation().getURL().toURI());
      File file = new File(dir.getAbsolutePath() + File.separator
          + "offspring.crt");
      return X509CertificateFile.getCertificate(file);
    }
    catch (URISyntaxException e) {
      logger.error("Could not read certificate", e);
    }
    return null;
  }

}
