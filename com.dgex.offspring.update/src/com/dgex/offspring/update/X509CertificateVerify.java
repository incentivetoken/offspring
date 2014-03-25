package com.dgex.offspring.update;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IProgressMonitor;

public class X509CertificateVerify {

  private final IProgressMonitor monitor;
  private final UpdateLog updateLog;

  public X509CertificateVerify(IProgressMonitor monitor, UpdateLog updateLog) {
    this.monitor = monitor;
    this.updateLog = updateLog;
  }

  public void verify(List<File> fileList, X509Certificate certificate) {
    monitor.beginTask("Verify Jar Files", fileList.size());
    Iterator<File> iterator = fileList.iterator();
    while (iterator.hasNext()) {
      monitor.worked(1);

      File file = iterator.next();
      if (verify(file, certificate))
        iterator.remove();
    }
    monitor.done();
  }

  private boolean verify(File file, X509Certificate certificate) {
    try {
      JarVerifier.verify(new JarFile(file),
          new X509Certificate[] { certificate });
      return true;
    }
    catch (CertificateException e) {
      updateLog
          .logMessage(getClass().getName(), "Not signed " + file.getPath());
    }
    catch (SecurityException e) {
      updateLog
          .logMessage(getClass().getName(), "Not signed " + file.getPath());
    }
    catch (IOException e) {
      updateLog.logError(getClass().getName(), "IOException " + file.getPath());
    }
    return false;
  }

}
