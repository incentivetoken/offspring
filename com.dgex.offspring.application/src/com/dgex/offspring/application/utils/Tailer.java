package com.dgex.offspring.application.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Tailer {

  private static Logger logger = Logger.getLogger(Tailer.class);

  private final File file;

  public Tailer(File file) {
    this.file = file;
  }

  public String readLastLine() {
    try {
      FileReader reader = new FileReader(file);
      BufferedReader br = new BufferedReader(reader);
      List<String> list = new ArrayList<String>();
      String line;
      while ((line = br.readLine()) != null) {
        list.add(line);
      }
      br.close();
      return list.size() > 0 ? list.get(list.size() - 1) : "";
    }
    catch (FileNotFoundException e) {
      logger.error("FileNotFoundException", e);
    }
    catch (IOException e) {
      logger.error("IOException", e);
    }
    return "";
  }

}
