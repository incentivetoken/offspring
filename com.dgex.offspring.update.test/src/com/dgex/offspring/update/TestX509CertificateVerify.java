package com.dgex.offspring.update;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestX509CertificateVerify {

  X509CertificateVerify verifier;

  File cert1 = new File("res/TestX509CertificateVerify/offspring-cert1.crt");
  File cert2 = new File("res/TestX509CertificateVerify/offspring-cert2.crt");
  File jar1 = new File("res/TestX509CertificateVerify/plugin-cert1.jar");
  File jar2 = new File("res/TestX509CertificateVerify/plugin-cert2.jar");

  @Before
  public void setUp() throws Exception {
    verifier = new X509CertificateVerify(Helper.createProgressMonitor(),
        Helper.createUpdateLog());
  }

  @Test
  public void testValidCertificate() {
    List<File> files = new ArrayList<File>(Arrays.asList(new File[] { jar1,
        jar2 }));
    X509Certificate certificate = X509CertificateFile.getCertificate(cert1);

    assertEquals(files.size(), 2);
    assertEquals(files.get(0), jar1);
    assertEquals(files.get(1), jar2);
    verifier.verify(files, certificate);

    assertEquals(files.size(), 1);
    assertEquals(files.get(0), jar2);
  }

}
