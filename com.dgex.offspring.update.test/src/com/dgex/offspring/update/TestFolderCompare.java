package com.dgex.offspring.update;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class TestFolderCompare {

  FolderCompare compare;

  File dir1 = new File("res/TestFolderCompare/dir1");
  File dir2 = new File("res/TestFolderCompare/dir2");
  File dir3 = new File("res/TestFolderCompare/dir3");
  File dir4 = new File("res/TestFolderCompare/dir4");

  @Before
  public void before() {
    compare = new FolderCompare(Helper.createProgressMonitor(),
        Helper.createUpdateLog());
  }

  @Test
  public void testIdenticalDirectories() throws IOException {
    assertEquals(compare.compare(dir1, dir2).size(), 0);
  }

  @Test
  public void testChanged1File() throws IOException {
    assertEquals(compare.compare(dir1, dir3).size(), 1);
    assertEquals(compare.compare(dir1, dir3).get(0), new File(
        "res/TestFolderCompare/dir1/file2.txt"));
  }

  @Test
  public void testAdded1File() throws IOException {
    assertEquals(compare.compare(dir4, dir1).size(), 1);
    assertEquals(compare.compare(dir4, dir1).get(0), new File(
        "res/TestFolderCompare/dir4/dir1/file3.txt"));
  }

}
