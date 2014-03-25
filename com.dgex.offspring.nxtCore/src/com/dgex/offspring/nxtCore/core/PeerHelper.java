package com.dgex.offspring.nxtCore.core;

import java.text.DecimalFormat;

import nxt.peer.Peer;

import com.dgex.offspring.nxtCore.service.IPeer;

public class PeerHelper implements IPeer {

  protected final Peer peer;

  public PeerHelper(Peer peer) {
    this.peer = peer;
  }

  @Override
  public Peer getNative() {
    return peer;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof PeerHelper
        && ((PeerHelper) other).peer.equals(this.peer);
  }

  @Override
  public long getUploadedVolume() {
    return peer.getUploadedVolume();
  }

  @Override
  public long getDownloadedVolume() {
    return peer.getDownloadedVolume();
  }

  @Override
  public String getFormattedUploadedVolume() {
    return readableFileSize(getUploadedVolume());
  }

  @Override
  public String getFormattedDownloadedVolume() {
    return readableFileSize(getDownloadedVolume());
  }

  @Override
  public String getPlatform() {
    return peer.getPlatform();
  }

  @Override
  public String getSoftware() {
    return peer.getSoftware();
  }

  @Override
  public String getPeerAddress() {
    return peer.getPeerAddress();
  }

  @Override
  public boolean isWellKnown() {
    return peer.isWellKnown();
  }

  @Override
  public boolean isBlacklisted() {
    return peer.isBlacklisted();
  }

  @Override
  public void blacklist() {
    peer.blacklist();
  }

  @Override
  public void removeBlacklistedStatus() {
    peer.unBlacklist();
  }

  @Override
  public void remove() {
    peer.remove();
  }

  public static String readableFileSize(long size) {
    if (size <= 0)
      return "0";
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size
        / Math.pow(1024, digitGroups))
        + " " + units[digitGroups];
  }

}
