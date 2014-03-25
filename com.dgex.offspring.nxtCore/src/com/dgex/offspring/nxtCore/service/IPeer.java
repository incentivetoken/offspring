package com.dgex.offspring.nxtCore.service;

import nxt.peer.Peer;

public interface IPeer {

  public Peer getNative();

  public long getUploadedVolume();

  public long getDownloadedVolume();

  public String getFormattedUploadedVolume();

  public String getFormattedDownloadedVolume();

  public String getPlatform();

  public String getSoftware();

  public String getPeerAddress();

  public boolean isWellKnown();

  public boolean isBlacklisted();

  public void blacklist();

  public void removeBlacklistedStatus();

  public void remove();

}
