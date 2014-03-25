package com.dgex.offspring.application.addon;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.dgex.offspring.application.utils.Shutdown;
import com.dgex.offspring.dataprovider.service.IDataProviderPool;
import com.dgex.offspring.nxtCore.service.INxtService;

public class QuitHandlerAddon {

  static Logger logger = Logger.getLogger(QuitHandlerAddon.class);

  @Inject
  private IEventBroker broker;

  @Inject
  private UISynchronize sync;

  private INxtService nxt;
  private Display display;

  private final IWindowCloseHandler quitHandler = new IWindowCloseHandler() {

    @Override
    public boolean close(final MWindow inWindow) {
      logger.trace("IWindowCloseHandler.close");

      return Shutdown
          .execute(display.getActiveShell(), broker, sync, nxt, pool);
    }
  };

  private final EventHandler eventHandler = new EventHandler() {

    @Override
    public void handleEvent(final Event inEvent) {
      if (!UIEvents.isSET(inEvent)) {
        return;
      }
      final Object lElement = inEvent.getProperty(UIEvents.EventTags.ELEMENT);
      if (!(lElement instanceof MWindow)) {
        return;
      }
      final MWindow lWindow = (MWindow) lElement;
      if ("com.dgex.offspring.application.mainwindow".equals(lWindow
          .getElementId())) {
        logger.trace(UIEvents.Context.TOPIC_CONTEXT);
        if (lWindow.equals(inEvent.getProperty("ChangedElement"))
            && lWindow.getContext() != null) {
          lWindow.getContext().runAndTrack(new RunAndTrack() {

            @Override
            public boolean changed(final IEclipseContext inContext) {
              final Object lHandler = inContext.get(IWindowCloseHandler.class);
              if (!quitHandler.equals(lHandler)) {
                inContext.set(IWindowCloseHandler.class, quitHandler);
              }
              return true;
            }
          });
        }
      }
    }
  };

  private IDataProviderPool pool;

  @PostConstruct
  void hookListeners(INxtService nxt, Display display, IDataProviderPool pool) {
    this.nxt = nxt;
    this.display = display;
    this.pool = pool;
    broker.subscribe(UIEvents.Context.TOPIC_CONTEXT, eventHandler);
  }

  @PreDestroy
  void unhookListeners() {
    broker.unsubscribe(eventHandler);
  }
}