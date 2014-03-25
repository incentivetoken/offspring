package com.dgex.offspring.application.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileWatcher {

  /* Singleton class - use as FileWatcher.getInstance() */
  private static FileWatcher INSTANCE = null;

  private List<FileWatcherStruct> listeners;
  private ScheduledExecutorService scheduler;

  private class FileWatcherStruct {
    public String path;
    public FileWatcherListener listener;
    public long mostRecentByteCount;

    public FileWatcherStruct(String path, FileWatcherListener listener) {
      this.path = path;
      this.listener = listener;
      this.mostRecentByteCount = 0;
    }
  }

  public FileWatcher() {
    listeners = new ArrayList<FileWatcherStruct>();
    scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleWithFixedDelay(new Runnable() {
      public void run() {
        for (int i = 0; i < listeners.size(); i++) {
          FileWatcherStruct s = listeners.get(i);
          File file = new File(s.path);
          if (!file.exists()) {
            s.listener.fileChanged(readableFileSize(0));
          }
          else {
            long size = file.length();
            if (size != s.mostRecentByteCount) {
              s.mostRecentByteCount = size;
              s.listener.fileChanged(readableFileSize(size));
            }
          }
        }
      }
    }, 1, 500, TimeUnit.MILLISECONDS);
  };

  public static FileWatcher getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new FileWatcher();
    }
    return INSTANCE;
  }

  private String readableFileSize(long size) {
    if (size <= 0) {
      return "0";
    }
    final String[] units = new String[] {
        "B", "KB", "MB", "GB", "TB"
    };
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  public void addListener(String path, FileWatcherListener listener) {
    for (int i = 0; i < listeners.size(); i++) {
      FileWatcherStruct s = listeners.get(i);
      if (s.path.equals(path) && s.listener == listener) {
        return; // allready registered
      }
    }
    listeners.add(new FileWatcherStruct(path, listener));
  }

  public void removeListener(String path, FileWatcherListener listener) {
    for (int i = 0; i < listeners.size(); i++) {
      FileWatcherStruct s = listeners.get(i);
      if (s.path.equals(path) && s.listener == listener) {
        listeners.remove(i);
        return;
      }
    }
  }
}
