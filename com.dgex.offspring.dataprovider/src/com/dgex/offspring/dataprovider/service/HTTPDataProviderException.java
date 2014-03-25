package com.dgex.offspring.dataprovider.service;

import java.net.URL;

@SuppressWarnings("serial")
public class HTTPDataProviderException extends Exception {

  private final int responseCode;
  private final URL url;

  public HTTPDataProviderException(URL url, int responseCode) {
    this.url = url;
    this.responseCode = responseCode;
  }

  public URL getUrl() {
    return url;
  }

  public int getResponseCode() {
    return responseCode;
  }

}
