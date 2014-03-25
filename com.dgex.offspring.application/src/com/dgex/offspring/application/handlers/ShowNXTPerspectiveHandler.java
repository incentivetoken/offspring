package com.dgex.offspring.application.handlers;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class ShowNXTPerspectiveHandler {

  static Logger logger = Logger.getLogger(ShowNXTPerspectiveHandler.class);

  private final static String PART_ID = "com.dgex.offspring.application.perspective.nxt";

  @CanExecute
  public boolean canExecute(MPerspective myPerspective) {
    if (!myPerspective.getElementId().equals(PART_ID))
      return true;
    return false;
  }

  @Execute
  public void execute(MApplication app, EPartService partService,
      EModelService modelService) {
    MPerspective element = (MPerspective) modelService.find(PART_ID, app);
    if (element != null) {
      partService.switchPerspective(element);
      logger.trace("Switch to " + PART_ID);
    }
  }
}