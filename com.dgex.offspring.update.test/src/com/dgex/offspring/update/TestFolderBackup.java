package com.dgex.offspring.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFolderBackup {

  FolderBackup backup;
  FolderCompare compare;

  File install_dir = new File("res/TestFolderBackup/install_dir");
  File backup_dir = new File("res/TestFolderBackup/backup_dir");

  @Before
  public void setUp() throws Exception {
    compare = new FolderCompare(Helper.createProgressMonitor(),
        Helper.createUpdateLog());
    backup = new FolderBackup(Helper.createProgressMonitor(),
        Helper.createUpdateLog());
    FileUtils.deleteDirectory(backup_dir);
  }

  @After
  public void teardown() throws IOException {
    FileUtils.deleteDirectory(backup_dir);
  }

  @Test
  public void testBackup() throws IOException {
    assertEquals(compare.compare(install_dir, backup_dir).size(), 4);
    assertTrue(backup.backup(install_dir, backup_dir));
    assertEquals(compare.compare(install_dir, backup_dir).size(), 0);
  }

}
