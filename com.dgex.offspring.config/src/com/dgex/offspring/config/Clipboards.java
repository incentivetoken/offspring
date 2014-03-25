package com.dgex.offspring.config;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class Clipboards {

  public static void copy(Display display, String contents) {
    Clipboard clipboard = new Clipboard(display);
    TextTransfer textTransfer = TextTransfer.getInstance();
    clipboard.setContents(new String[] {
        contents
    }, new Transfer[] {
      textTransfer
    });
    clipboard.dispose();
  }
}
