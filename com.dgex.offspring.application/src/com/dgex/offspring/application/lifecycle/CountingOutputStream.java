package com.dgex.offspring.application.lifecycle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

public class CountingOutputStream extends FileOutputStream {

  private final IProgressMonitor monitor;

  public CountingOutputStream(File destination, IProgressMonitor monitor)
      throws FileNotFoundException {
    super(destination);
    this.monitor = monitor;
  }

  @Override
  public void write(byte[] b) throws IOException {
    super.write(b);
    this.monitor.worked(b.length);
    if (this.monitor.isCanceled()) {
      close();
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    super.write(b, off, len);
    this.monitor.worked(len);
    if (this.monitor.isCanceled()) {
      close();
    }
  }
}