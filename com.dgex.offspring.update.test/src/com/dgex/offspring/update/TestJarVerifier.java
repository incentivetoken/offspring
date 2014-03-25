package com.dgex.offspring.update;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.jar.JarFile;

import org.junit.Test;

public class TestJarVerifier {

  public File cert1 = new File("res/TestJarVerifier/offspring-cert1.crt");
  public File jar1 = new File("res/TestJarVerifier/plugin-cert1.jar");

  public File cert2 = new File("res/TestJarVerifier/offspring-cert2.crt");
  public File jar2 = new File("res/TestJarVerifier/plugin-cert2.jar");

  public File certEclipse = new File("res/TestJarVerifier/eclipse.crt");
  public File jarEclipse = new File("res/TestJarVerifier/eclipse.jar");

  File cert3 = new File(
      "res/TestJarVerifier/DigiCertHighAssuranceCodeSigningCA-1.crt");
  File jar3 = new File("res/TestJarVerifier/javax.inject_1.0.0.v20091030.jar");

  File cert4 = new File("res/TestJarVerifier/offspring.crt");
  File jar4 = new File(
      "res/TestJarVerifier/com.dgex.offspring.wallet_1.0.0.201402161933.jar");

  public void verify(File cert, File jarFile) throws Exception {
    X509Certificate certificate = X509CertificateFile.getCertificate(cert);

    try {
      JarVerifier.verify(new JarFile(jarFile),
          new X509Certificate[] { certificate });
    }
    catch (CertificateException e) {
      throw e;
    }
    catch (IOException e) {
      throw new Exception(e);
    }
  }

  @Test
  public void testCorrect() throws Exception {
    verify(cert1, jar1);
    verify(cert2, jar2);
  }

  @Test
  public void testEclipse() throws Exception {
    verify(cert3, jar3);
  }

  @Test
  public void testOffspring() throws Exception {
    verify(cert4, jar4);
  }

  @Test(expected = SecurityException.class)
  public void testWrongCert() throws Exception {
    verify(cert2, jar1);
  }

  @Test(expected = SecurityException.class)
  public void testWrongPlugin() throws Exception {
    verify(cert1, jar2);
  }

  @Test(expected = SecurityException.class)
  public void testWrongCert2() throws Exception {
    verify(cert1, jar2);
  }

  @Test(expected = SecurityException.class)
  public void testWrongPlugin2() throws Exception {
    verify(cert2, jar1);
  }

}