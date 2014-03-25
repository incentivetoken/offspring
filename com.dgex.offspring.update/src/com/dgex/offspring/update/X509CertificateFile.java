package com.dgex.offspring.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

public class X509CertificateFile {

  static Logger logger = Logger.getLogger(X509CertificateFile.class);

  public static X509Certificate getCertificate(File certificate) {
    try {
      InputStream inStream = null;
      inStream = new FileInputStream(certificate);
      return getCertificate(inStream);
    }
    catch (IOException e) {
      e.printStackTrace(System.err);
    }
    return null;
  }

  public static X509Certificate getCertificate(InputStream inStream) {
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
      return cert;
    }
    catch (CertificateException e) {
      e.printStackTrace(System.err);
    }
    return null;
  }
}
