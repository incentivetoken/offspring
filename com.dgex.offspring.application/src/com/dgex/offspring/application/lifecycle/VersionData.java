package com.dgex.offspring.application.lifecycle;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class VersionData {
  
  private static String OS = System.getProperty("os.name").toLowerCase();
  
  public static final int WINDOWS_INSTALLER = 1;
  public static final int WIN_X86_ZIP = 2;
  public static final int WIN_X86_64_ZIP = 3;
  public static final int MAC_ZIP = 4;
  public static final int LIN_X86_ZIP = 5;
  public static final int LIN_X86_64_ZIP = 6;
  
  public static final String KEY_VERSION = "v";
  public static final String KEY_BASE = "b";
  public static final String KEY_SHA1 = "s";
  
  private final JSONObject versionObject;

  public VersionData(String data) {
    versionObject = (JSONObject) JSONValue.parse(data);
  }
  
  public boolean platformSupported(int platform) {
    switch (platform) {
    case WINDOWS_INSTALLER:
    case WIN_X86_64_ZIP:
    case WIN_X86_ZIP:
      return (OS.indexOf("win") >= 0);
    case MAC_ZIP:
      return (OS.indexOf("mac") >= 0);
    case LIN_X86_64_ZIP:
    case LIN_X86_ZIP:
      return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }
    return false;
  }
  
  public String getFilename(int platform) {
    return createFileName(platform, getVersion());
  }

  public String getVersion() {
    return (String) versionObject.get(KEY_VERSION);
  }

  public String getDownloadURL(int platform) {
    String version = (String) versionObject.get(KEY_VERSION);
    StringBuilder b = new StringBuilder();
    b.append(versionObject.get(KEY_BASE));
    b.append("v");
    b.append(version);
    b.append("/");
    b.append(createFileName(platform, version));
    return b.toString();
  }

  public String getSHA1Hash(int platform) {
    JSONObject urls = (JSONObject) versionObject.get(KEY_SHA1);
    return (String) urls.get(Integer.toString(platform));
  }

  /* This has to match prepare_release script */
  private String createFileName(int platform, String version) {
    String name = "OFFSPRING." + version;
    switch (platform) {
    case WINDOWS_INSTALLER:
      // return name + ".WIN.FULL.INSTALLER.exe";
      return name + ".INSTALLER.exe";
    case WIN_X86_ZIP:
      return name + ".WINDOWS.X86.zip";
    case WIN_X86_64_ZIP:
      return name + ".WINDOWS.X86_64.zip";
    case MAC_ZIP:
      return name + ".MACOSX.zip";
    case LIN_X86_ZIP:
      return name + ".LINUX.X86.zip";
    case LIN_X86_64_ZIP:
      return name + ".LINUX.X86_64.zip";
    }
    throw new RuntimeException("Unknown type");
  }
}
