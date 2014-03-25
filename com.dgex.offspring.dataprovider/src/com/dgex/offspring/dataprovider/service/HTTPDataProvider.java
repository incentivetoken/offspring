package com.dgex.offspring.dataprovider.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.dgex.offspring.dataprovider.internal.DataProvider;

public abstract class HTTPDataProvider extends DataProvider {

  private static Logger logger = Logger.getLogger(HTTPDataProvider.class);

  private static int sConnectTimeout = 6666;
  private static int sReadTimeout = 6666;

  protected String get(URL url) throws HTTPDataProviderException, IOException {
    StringWriter writer = new StringWriter();
    InputStream is = null;
    try {
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);
      connection.setConnectTimeout(sConnectTimeout);
      connection.setReadTimeout(sReadTimeout);
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");

      is = connection.getInputStream();
      IOUtils.copy(is, writer, "UTF-8");

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) { throw new HTTPDataProviderException(
          url, connection.getResponseCode()); }

      return writer.toString();
    }
    finally {
      writer.close();
      if (is != null)
        is.close();
    }
  }

}
