package com.dgex.offspring.update;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.dataprovider.service.IDataProviderPool;

public class UpdateDialog extends TitleAreaDialog {

  static Logger logger = Logger.getLogger(UpdateDialog.class);

  static final String REPOSITORY_LOC = System.getProperty("UpdateJob.Repo",
      "file://home/dirk/OFFSPRING-REPO");

  private Composite mainContainer = null;
  private Composite progressBarComposite;
  private Label messageLabel = null;
  private Button installButton;
  private ProgressIndicator progressBar = null;
  private final IProgressMonitor monitor;

  private Job updateJob = null;

  private final UISynchronize sync;
  private final IProvisioningAgent agent;
  private final Display display;
  private final IDataProviderPool pool;
  private final IWorkbench workbench;

  private boolean installUpdates = false;
  private boolean cancelUpdates = false;

  public UpdateDialog(Display display, Shell shell, UISynchronize sync,
      IProvisioningAgent agent, IDataProviderPool pool, IWorkbench workbench) {
    super(shell);
    this.monitor = new UpdateProgressMonitor(this, sync);
    this.sync = sync;
    this.agent = agent;
    this.display = display;
    this.pool = pool;
    this.workbench = workbench;
  }

  @Override
  public void create() {
    super.create();
    setTitle("Software Update");
    setMessage("Offspring software update service");
    scheduleJob();
  }

  public void setStatus(String status) {
    if (messageLabel != null && !messageLabel.isDisposed())
      messageLabel.setText(status);
  }

  public void setError(String error) {
    if (messageLabel != null && !messageLabel.isDisposed())
      messageLabel.setText("ERROR " + error);
  }

  public void showProgressIndicator() {
    if (progressBarComposite != null && !progressBarComposite.isDisposed()) {
      progressBarComposite.setVisible(true);
      GridDataFactory.fillDefaults().exclude(false)
          .applyTo(progressBarComposite);
    }
    if (mainContainer != null && !mainContainer.isDisposed())
      mainContainer.layout();
  }

  public void hideProgressIndicator() {
    if (progressBarComposite != null && !progressBarComposite.isDisposed()) {
      progressBarComposite.setVisible(false);
      progressBar.done();
      GridDataFactory.fillDefaults().exclude(true)
          .applyTo(progressBarComposite);
    }
    if (mainContainer != null && !mainContainer.isDisposed())
      mainContainer.layout();
  }

  public void showInstallButton() {
    if (installButton != null && !installButton.isDisposed()) {
      installButton.setVisible(true);
      GridDataFactory.fillDefaults().exclude(false).applyTo(installButton);
    }
    if (mainContainer != null && !mainContainer.isDisposed())
      mainContainer.layout();
  }

  public void hideInstallButton() {
    if (installButton != null && !installButton.isDisposed()) {
      installButton.setVisible(false);
      GridDataFactory.fillDefaults().exclude(true).applyTo(installButton);
    }
    if (mainContainer != null && !mainContainer.isDisposed())
      mainContainer.layout();
  }

  public ProgressIndicator getProgressIndicator() {
    return progressBar;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
        true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);

    getButton(IDialogConstants.OK_ID).setEnabled(false);
  }

  @Override
  protected void buttonPressed(int id) {
    if (id == IDialogConstants.CANCEL_ID) {
      cancelUpdates = true;
      if (updateJob != null) {
        updateJob.cancel();
      }
    }
    super.buttonPressed(id);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(1, false);
    layout.horizontalSpacing = 15;
    layout.marginTop = 10;
    layout.marginLeft = 10;

    GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
    gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);

    mainContainer = new Composite(container, SWT.NONE);
    mainContainer.setLayoutData(gd);
    mainContainer.setLayout(layout);

    messageLabel = new Label(mainContainer, SWT.WRAP);
    messageLabel.setText("...");
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
        .applyTo(messageLabel);

    installButton = new Button(mainContainer, SWT.PUSH);
    installButton.setText("Install Updates");
    installButton.setVisible(false);
    installButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        installUpdates = true;
        hideInstallButton();
      }
    });
    GridDataFactory.swtDefaults().exclude(true).applyTo(installButton);

    progressBarComposite = new Composite(mainContainer, SWT.NONE);
    progressBarComposite.setVisible(false);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true)
        .exclude(true).applyTo(progressBarComposite);
    GridLayoutFactory.fillDefaults().numColumns(1)
        .applyTo(progressBarComposite);

    progressBar = new ProgressIndicator(progressBarComposite, SWT.SMOOTH);
    GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL)
        .applyTo(progressBar);

    mainContainer.layout();
    return container;
  }

  private void scheduleJob() {
    updateJob = createUpdateJob();
    Job.getJobManager().setProgressProvider(new ProgressProvider() {

      @Override
      public IProgressMonitor createMonitor(Job job) {
        return monitor;
      }
    });
    updateJob.schedule();
  }

  private Job createUpdateJob() {
    return new Job("Update Job") {

      private final boolean doInstall = false;

      private boolean retry(String message) {
        return MessageDialog.openQuestion(getShell(), "Retry failed step?",
            message + "\n\nDo you want to retry?");
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {

        final UpdateManager updateManager = new UpdateManager(monitor);

        sync.syncExec(new Runnable() {

          @Override
          public void run() {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
          }
        });

        /* 1. Prepare update plumbing */

        final ProvisioningSession session = new ProvisioningSession(agent);
        final UpdateOperation operation = new UpdateOperation(session);

        boolean success = updateManager.initialize();
        if (!success) {
          while (!success && retry("Could not initialize update module")) {
            success = updateManager.initialize();
          }
          if (!success) {
            sync.syncExec(new Runnable() {

              @Override
              public void run() {
                setError("Could not initialize update module");
                getButton(IDialogConstants.OK_ID).setEnabled(true);
                getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
              }
            });
            return Status.CANCEL_STATUS;
          }
        }

        URI uri = null;
        try {
          uri = new URI(REPOSITORY_LOC);
        }
        catch (final URISyntaxException e) {
          sync.syncExec(new Runnable() {

            @Override
            public void run() {
              setError("URI invalid " + e.getMessage());
              getButton(IDialogConstants.OK_ID).setEnabled(true);
              getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
            }
          });
          return Status.CANCEL_STATUS;
        }

        // set location of artifact and metadata repo
        operation.getProvisioningContext().setArtifactRepositories(
            new URI[] { uri });
        operation.getProvisioningContext().setMetadataRepositories(
            new URI[] { uri });

        /* 2. check for updates */

        // run update checks causing I/O
        sync.syncExec(new Runnable() {

          @Override
          public void run() {
            setStatus("Checking for updates");
            showProgressIndicator();
            getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
          }
        });

        if (cancelUpdates)
          return Status.CANCEL_STATUS;

        final IStatus status = operation.resolveModal(monitor);

        if (cancelUpdates)
          return Status.CANCEL_STATUS;

        // failed to find updates (inform user and exit)
        if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
          sync.syncExec(new Runnable() {

            @Override
            public void run() {
              setStatus("No updates found for current installation");
              hideProgressIndicator();
              getButton(IDialogConstants.OK_ID).setEnabled(true);
              getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
            }
          });
          return Status.CANCEL_STATUS;
        }

        /* 3. Since there are updates we first scan the current installation */

        // found updates
        if (status.isOK() && status.getSeverity() != IStatus.ERROR) {
          sync.syncExec(new Runnable() {

            @Override
            public void run() {
              String updates = "";
              Update[] possibleUpdates = operation.getPossibleUpdates();
              for (Update update : possibleUpdates) {
                updates += update + "\n";
              }
              setStatus("These updates are available.\n" + updates
                  + "\n\nDo you want to install these updates?");
              showInstallButton();
              hideProgressIndicator();
              getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
            }
          });

          /* Wait for the user to press the install updates button */

          try {
            while (true) {
              Thread.sleep(100);
              if (installUpdates || cancelUpdates)
                break;
            }
          }
          catch (InterruptedException e) {
            return Status.CANCEL_STATUS;
          }

          if (cancelUpdates)
            return Status.CANCEL_STATUS;

          sync.syncExec(new Runnable() {

            @Override
            public void run() {
              hideInstallButton();
              setStatus("Creating backup of your install directory");
            }
          });

          success = updateManager.createFullBackup();

          if (cancelUpdates)
            return Status.CANCEL_STATUS;

          if (!success) {
            while (!success && retry("Could not backup install directory")) {
              success = updateManager.createFullBackup();
            }
            if (!success) {
              sync.syncExec(new Runnable() {

                @Override
                public void run() {
                  setError("Something went wrong [Backup Install Directory]");
                  hideProgressIndicator();
                  getButton(IDialogConstants.OK_ID).setEnabled(true);
                  getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
                }
              });
              return Status.CANCEL_STATUS;
            }
          }
        }
        else {
          sync.syncExec(new Runnable() {

            @Override
            public void run() {
              setError("Something went wrong '" + status.getMessage() + "'");
              hideProgressIndicator();
              getButton(IDialogConstants.OK_ID).setEnabled(true);
              getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
            }
          });
          return Status.CANCEL_STATUS;
        }

        final ProvisioningJob provisioningJob = operation
            .getProvisioningJob(monitor);
        // updates cannot run from within Eclipse IDE!!!
        if (provisioningJob == null) {
          System.err
              .println("Running update from within Eclipse IDE? This won't work!!!");
          throw new NullPointerException();
        }

        // register a job change listener to track
        // installation progress and notify user upon success

        provisioningJob.addJobChangeListener(new JobChangeAdapter() {

          @Override
          public void done(IJobChangeEvent event) {
            if (event.getResult().isOK()) {

              /* Have UpdateManager create a diff of installation files */

              sync.syncExec(new Runnable() {

                @Override
                public void run() {
                  hideProgressIndicator();
                  setStatus("Updates downloaded succesfully.\n"
                      + "Please wait while we analyze downloaded files.");
                }
              });

              boolean success = updateManager.analyzeChangedFiles();
              if (!success) {
                while (!success && retry("Could not analyze updated files")) {
                  success = updateManager.analyzeChangedFiles();
                }
                if (!success) {
                  sync.syncExec(new Runnable() {

                    @Override
                    public void run() {
                      hideProgressIndicator();
                      setError("Something went wrong [Analyzing Updates Files]");
                      getButton(IDialogConstants.OK_ID).setEnabled(true);
                      getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
                    }
                  });
                  return;
                }
              }

              /* Ensure that all jar files are in fact signed by us */

              sync.syncExec(new Runnable() {

                @Override
                public void run() {
                  setStatus("Verifying X.509 certificates of downloaded files.");
                  hideProgressIndicator();
                }
              });

              success = updateManager.verifyJarCertificates();
              if (!success) {
                while (!success && retry("Could not verify signed jar files")) {
                  success = updateManager.verifyJarCertificates();
                }
                if (!success) {
                  sync.syncExec(new Runnable() {

                    @Override
                    public void run() {
                      setError("Something went wrong [Verifying Jar Certificates]");
                      hideProgressIndicator();
                      getButton(IDialogConstants.OK_ID).setEnabled(true);
                      getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
                    }
                  });
                  return;
                }
              }

              /* Ensure that any unsigned updated file is not on our blacklist */

              sync.syncExec(new Runnable() {

                @Override
                public void run() {
                  setStatus("Verifying downloaded files.");
                  hideProgressIndicator();
                }
              });

              success = updateManager.verifyUnsignedUpdates();
              if (!success) {
                while (!success && retry("Could not verify other files")) {
                  success = updateManager.verifyUnsignedUpdates();
                }
                if (!success) {
                  sync.syncExec(new Runnable() {

                    @Override
                    public void run() {
                      setError("Something went wrong [Verifying Unsigned Updates]");
                      hideProgressIndicator();
                      getButton(IDialogConstants.OK_ID).setEnabled(true);
                      getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
                    }
                  });
                  return;
                }
              }

              /*
               * In case inconsistencies where found, we role back all updated
               * files and create an error log. The user is asked to send this
               * error log to DGEX for further support.
               */

              if (!updateManager.isVerified()) {
                updateManager.createErrorLog();
                sync.syncExec(new Runnable() {

                  @Override
                  public void run() {
                    setError("We could not verify downloaded instalation files.\n"
                        + "Your installation directory is backed up to [XXX] we advise you to delete your current "
                        + "installation directory and continue in your backup directory.");
                    hideProgressIndicator();
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                    getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
                  }
                });
                return;
              }

              sync.syncExec(new Runnable() {

                @Override
                public void run() {
                  boolean restart = MessageDialog
                      .openQuestion(
                          getShell(),
                          "Updates installed, restart?",
                          "Updates have been installed successfully, you must restart for changes to have effect.\n"
                              + "Do you want to restart Offspring?");
                  if (restart) {

                    /* Before we restart we should go over all files in */

                    // Shutdown.execute(display, nxt, pool);
                    // workbench.restart();

                  }
                }
              });
            }
            super.done(event);
          }
        });
        provisioningJob.schedule();
        return Status.OK_STATUS;
      }
    };
  }
}
