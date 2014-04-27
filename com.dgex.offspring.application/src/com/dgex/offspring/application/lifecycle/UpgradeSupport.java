package com.dgex.offspring.application.lifecycle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.dgex.offspring.config.Config;

public class UpgradeSupport {

  private final String previousVersion;
  private final String versionFilePath = Config.appPath.getAbsolutePath()
      + File.separator + "OFFSPRING_VERSION";

  public UpgradeSupport() {
    previousVersion = getPreviousVersion();
  }

  // private int parseVersion(String version) {
  //
  // }

  private String getPreviousVersion() {
    try {
      String content = readFile(versionFilePath, StandardCharsets.UTF_8);
      return content.trim();
    }
    catch (IOException e) {
    }
    return null;
  }

  private void safeCurrentVersion(String version) {
    writeFile(versionFilePath, StandardCharsets.UTF_8, version);
  }

  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  static void writeFile(String path, Charset encoding, String contents) {
    File file = new File(path);
    try (FileOutputStream fop = new FileOutputStream(file)) {
      if (!file.exists()) {
        file.createNewFile();
      }
      byte[] contentInBytes = contents.getBytes(encoding);
      fop.write(contentInBytes);
      fop.flush();
      fop.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
