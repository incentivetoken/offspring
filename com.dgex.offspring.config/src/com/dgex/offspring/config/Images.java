package com.dgex.offspring.config;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class Images {

  /* helper method to load the images
   * ensure to dispose the images in your @PreDestroy method */
  public static Image getImage(String file) {
    Bundle bundle = FrameworkUtil.getBundle(Images.class);
    URL url = FileLocator.find(bundle, new Path("icons/" + file), null);
    ImageDescriptor image = ImageDescriptor.createFromURL(url);
    return image.createImage();
  }

}
