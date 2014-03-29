package com.dgex.offspring.config;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class Colors {

  private static final String KEY_PREFIX = Colors.class.getCanonicalName()
      + "."; //$NON-NLS-1$

  public static final RGB DARK_GREEN = new RGB(4, 15, 12);
  public static final RGB DARK_RED = new RGB(139, 0, 19);
  public static final RGB WHITE = new RGB(255, 255, 255);
  public static final RGB BLACK = new RGB(0, 0, 0);
  public static final RGB BLUE = new RGB(17, 0, 255);
  public static final RGB YELLOW = new RGB(255, 255, 0);

  public static Color getColor(RGB rgb) {
    return getColor(rgb.red, rgb.green, rgb.blue);
  }

  public static Color getColor(int red, int green, int blue) {
    String key = getColorKey(red, green, blue);
    if (JFaceResources.getColorRegistry().hasValueFor(key)) {
      return JFaceResources.getColorRegistry().get(key);
    }
    else {
      JFaceResources.getColorRegistry().put(key, new RGB(red, green, blue));
      return getColor(key);
    }
  }

  public static Color getColor(String key) {
    return JFaceResources.getColorRegistry().get(key);
  }

  private static String getColorKey(int red, int green, int blue) {
    return KEY_PREFIX + "_COLOR_" + red + "_" + green + "_" + blue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
