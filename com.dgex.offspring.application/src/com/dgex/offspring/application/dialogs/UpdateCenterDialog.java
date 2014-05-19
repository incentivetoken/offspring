package com.dgex.offspring.application.dialogs;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.application.lifecycle.CountingOutputStream;
import com.dgex.offspring.application.lifecycle.UpgradeManager;
import com.dgex.offspring.application.lifecycle.VersionData;
import com.dgex.offspring.config.Config;
import com.dgex.offspring.config.Formatter;


public class UpdateCenterDialog extends TitleAreaDialog {
  
  static final String wikiURL = "https://github.com/incentivetoken/offspring/wiki/8.-Installation-of-ZIP-files#zip-file-installation-instructions";
  static Logger logger = Logger.getLogger(UpdateCenterDialog.class);
  static UpdateCenterDialog INSTANCE = null;
  static VersionData versionData = null;

  private Composite mainContainer;
  private ProgressIndicator progressIndicator;
  private final UISynchronize sync;
  private final ArrayList<Link> downloadLinks = new ArrayList<Link>();
  private Job currentJob = null;
  private Composite downloadComposite;
  private Label progressLabel;
  private Composite finalizeComposite;

  class ProgressMonitor implements IProgressMonitor {

    private int worked = 0;

    @Override
    public void beginTask(final String name, final int totalWork) {
      sync.syncExec(new Runnable() {

        @Override
        public void run() {
          worked = 0;
          progressIndicator.beginTask(totalWork);
          progressIndicator.setToolTipText(name);
        }
      });
      System.out.println("Starting");
    }

    @Override
    public void done() {
      System.out.println("Done");
    }

    @Override
    public void internalWorked(double work) {}

    @Override
    public boolean isCanceled() {
      return false;
    }

    @Override
    public void setCanceled(boolean value) {}

    @Override
    public void setTaskName(String name) {}

    @Override
    public void subTask(String name) {}

    @Override
    public void worked(final int work) {
      sync.syncExec(new Runnable() {

        @Override
        public void run() {
          worked += work;
          progressIndicator.worked(work);
          progressLabel.setText(Formatter.readableFileSize(worked));
          progressLabel.getParent().layout();
        }
      });
    }
  };

  public UpdateCenterDialog(Shell shell, UISynchronize sync) {
    super(shell);
    this.sync = sync;
  }
  
  /**
   * Static method that opens a new dialog or switches to the existing dialog.
   * 
   * @param accountId
   * @return
   */
  public static void show(final UISynchronize sync) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        Shell shell = Display.getCurrent().getActiveShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }

        INSTANCE.versionData = UpgradeManager.getVersionData();
        if (INSTANCE.versionData != null) {
          if (INSTANCE == null) {
            INSTANCE = new UpdateCenterDialog(shell, sync);
            INSTANCE.open();
          }
          else {
            INSTANCE.getShell().forceActive();
          }
        }
      }
    });
  }

  public static boolean isOpened() {
    return INSTANCE != null;
  }

  @Override
  public void create() {
    super.create();
    setTitle("Update Center");
    setMessage("Wellcome to the Offspring Update Center");
  }
  
  @Override
  public int open() {
    int ret = super.open();
    if (UpgradeManager.getVersionData() == null) {
      MessageDialog
          .openInformation(getShell(), "Blockchain Incomplete",
              "Update Center is disabled because your blockchain is still downloading.");
      close();
    }
    return ret;
  }
  
  @Override
  public boolean close() {
    if (currentJob != null) {
      currentJob.cancel();
    }
    boolean closed = super.close();
    if (closed) {
      INSTANCE = null;
    }
    return closed;
  }
    
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    initializeDialogUnits(parent);
    Composite container = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(2, false);
    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 5;
    layout.marginTop = 5;
    layout.marginLeft = 5;

    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true);
    gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);

    mainContainer = new Composite(container, SWT.NONE);
    mainContainer.setLayoutData(gd);
    mainContainer.setLayout(layout);

    Label label;
    boolean up_to_date = !versionData.isOutdatedVersion(Config.VERSION);
    
    if (up_to_date) {
      label = new Label(mainContainer, SWT.NONE);
      label.setText("YOUR OFFSPRING VERSION " + Config.VERSION + " IS UP TO DATE");
      label.setFont(JFaceResources.getFontRegistry().getBold(""));
      GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.FILL)
          .grab(true, false).applyTo(label);

      final Button check = new Button(mainContainer, SWT.CHECK);
      GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).applyTo(check);
      check.setText("Let me download anyway.");
      check.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          check.setEnabled(false);
          downloadComposite.setVisible(true);
        }
      });
    }
    else {
      label = new Label(mainContainer, SWT.NONE);
      label.setText("UPDATE AVAILABLE! PLEASE UPDATE OFFSPRING TO VERSION " + versionData.getVersion());
      label.setFont(JFaceResources.getFontRegistry().getBold(""));
      GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(label);
      
    }
    
    downloadComposite = new Composite(mainContainer, SWT.NONE);
    downloadComposite.setVisible(up_to_date == false);
    GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(downloadComposite);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(downloadComposite);    
    
    Group group = new Group(downloadComposite, SWT.SHADOW_NONE);
    group.setLayout(new FillLayout());
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);

    label = new Label(group, SWT.NONE);
    label.setText("Download the latest release below.");
    
    group = new Group(downloadComposite, SWT.SHADOW_NONE);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);
    GridLayoutFactory.fillDefaults().numColumns(2).applyTo(group);
    
    createDownloadLink(group, VersionData.WINDOWS_INSTALLER);
    createDownloadLink(group, VersionData.MAC_ZIP);
    createDownloadLink(group, VersionData.LIN_X86_64_ZIP);
    createDownloadLink(group, VersionData.LIN_X86_ZIP);
    createDownloadLink(group, VersionData.WIN_X86_64_ZIP);
    createDownloadLink(group, VersionData.WIN_X86_ZIP);

    progressIndicator = new ProgressIndicator(mainContainer, SWT.SMOOTH);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
        .applyTo(progressIndicator);

    progressLabel = new Label(mainContainer, SWT.NONE);
    progressLabel.setText(" ");
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
        .applyTo(progressLabel);
    
    finalizeComposite = new Composite(mainContainer, SWT.NONE);
    GridDataFactory.fillDefaults().exclude(true).span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(finalizeComposite);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(finalizeComposite);
    
    new Label(finalizeComposite, SWT.NONE).setText("Installation Instructions..");
    new Label(finalizeComposite, SWT.NONE).setText("Windows users execute the installer and follow instructions.");
    new Label(finalizeComposite, SWT.NONE).setText("All users who downloaded the ZIP file check out our WIKI.");

    Link link = new Link(finalizeComposite, SWT.NONE);
    link.setText("<A>Click to visit WIKI</a>");
    link.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          openBrowser(wikiURL);
        }
        catch (IOException ex) {
          logger.error("Error opening browser", ex);
        }
      }
    });
    
    return container;
  }  
  
  private void createDownloadLink(Composite parent, final int platform) {
    if (versionData.platformSupported(platform)) {
      Link link = new Link(parent, SWT.NONE);
      downloadLinks.add(link);
      link.setText("<A>"+versionData.getFilename(platform)+"</A>");
      link.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          download(platform);
        }
      });
      Text text = new Text(parent, SWT.BORDER);
      text.setText(versionData.getSHA1Hash(platform));
      text.setEnabled(false);
      text.setEditable(false);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);
    }
  }
  
  protected void download(final int platform) {
    final File tempFile = getTempFile(platform);
    if (tempFile == null) {
      return;
    }

    currentJob = new Job("Download Update") {
      @Override
      public IStatus run(IProgressMonitor monitor) {
        try {
          setDownloadActive(true);
          try {
            setProgressBarAnimation(true);
            URL url = new URL(versionData.getDownloadURL(platform));
            int length = getContentLength(url);
            setProgressBarAnimation(false);

            monitor.beginTask("Downloading Update", length);
            OutputStream os = new CountingOutputStream(tempFile, monitor);
            InputStream is = url.openStream();
            IOUtils.copy(is, os);// begin transfer
            os.close();// close streams
            is.close();// ^
            monitor.done();
          }
          catch (IOException e) {
            logger.error("Could not download " + versionData.getDownloadURL(platform), e);
          }
        } finally {
          setDownloadActive(false);
        }
        sync.asyncExec(new Runnable() {

          @Override
          public void run() {
            downloadDone(platform, tempFile);
          }
        });
        return Status.OK_STATUS;
      }
      private int getContentLength(URL url) {
        try {
          return Integer.parseInt(url.openConnection().getHeaderField("Content-Length"));
        }
        catch (NumberFormatException e) {
          logger.error("Could not obtain contentlength for URL " + url, e);
        }
        catch (IOException e) {
          logger.error("Could not obtain contentlength for URL " + url, e);
        }
        return Integer.MAX_VALUE;
      }
    };

    IJobManager manager = Job.getJobManager();
    ProgressProvider provider = new ProgressProvider() {

      @Override
      public IProgressMonitor createMonitor(Job job) {
        return new ProgressMonitor();
      }
    };
    manager.setProgressProvider(provider);
    currentJob.schedule();
  }

  private void downloadDone(int platform, File tempFile) {
    String fileSha = getSHA256ForFile(tempFile);
    if (fileSha == null || !fileSha.equals(versionData.getSHA1Hash(platform))) {
      showError("Downloaded files did not verify against expected SHA1 hash.", null);
      return;
    }
    
    showInfo("Download and verification succesfull.\nPlease select a location to place the downloaded files?");

    String path = null;
    while (path == null) {
      FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
      dialog.setFileName(versionData.getFilename(platform));
      dialog.setFilterPath(System.getProperty("user.home"));
      path = dialog.open();
      if (path == null) {
        if (MessageDialog.openQuestion(getShell(), "Exit Update?", "Are you sure you want to exit the Update Center without saving the files?")) {
          return;
        }
      }
      
      File destination = new File(path);
      if (destination.exists()) {
        if (MessageDialog.openQuestion(getShell(), "File exists", "That file exists. Do you want to overwrite?") == false) {
          path = null;
          continue;
        }
        destination.delete();
      }

      try {
        FileUtils.moveFile(tempFile, destination);        
        try {
          FileUtils.deleteDirectory(tempFile.getParentFile());
        }
        catch (IOException e) {
          showError("Error deleting temporary download files.", e);
        }

        downloadComposite.setVisible(false);
        GridDataFactory.fillDefaults().exclude(true).applyTo(downloadComposite);
        GridDataFactory.fillDefaults().exclude(false).applyTo(finalizeComposite);
        mainContainer.layout(true);
      }
      catch (IOException e) {
        showError("Error saving file", e);
      }
    }
  }

  private void setDownloadActive(final boolean active) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        if (active)
          getButton(IDialogConstants.OK_ID).setText(IDialogConstants.CANCEL_LABEL);
        else
          getButton(IDialogConstants.OK_ID).setText(IDialogConstants.OK_LABEL);
        for (Link link : downloadLinks) {
          link.setEnabled(!active);
        }
      }
    });
  }

  private void setProgressBarAnimation(final boolean active) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        if (active)
          progressIndicator.beginAnimatedTask();
        else
          progressIndicator.done();
      }
    });
  }

  private void showError(final String message, final Throwable t) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        String msg = message;
        if (t != null) {
          logger.error(message, t);
          msg += "\n\nDetails: " + t.getLocalizedMessage();
        }
        else {
          logger.error(message);
        }
        MessageDialog.openError(getShell(), "An Error Occured", msg);
      }
    });
  }

  private void showInfo(final String message) {
    logger.info(message);
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        MessageDialog.openInformation(getShell(), "Information", message);
      }
    });
  }
  
  private File getTempFile(int platform) {
    File file = new File(System.getProperty("user.home") + File.separator + "Offspring-Temp" + File.separator + versionData.getFilename(platform));
    try {
      FileUtils.forceMkdir(file.getParentFile());
    }
    catch (IOException e) {
      showError("Could not create temp file", e);
      return null;
    }
    if (file.exists()) {
      file.delete();
    }
    return file;
  }
  
  private String getSHA256ForFile(File file) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      FileInputStream fis = new FileInputStream(file);
  
      byte[] dataBytes = new byte[1024];
      int nread = 0; 
      while ((nread = fis.read(dataBytes)) != -1) {
        md.update(dataBytes, 0, nread);
      };
      byte[] mdbytes = md.digest();
  
      //convert the byte to hex format method 1
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < mdbytes.length; i++) {
        sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
      }
      return sb.toString();
    } 
    catch (NoSuchAlgorithmException e) {
      showError("Error while calculating SHA1 hash", e);
    }
    catch (IOException e) {
      showError("Error while calculating SHA1 hash", e);
    }
    return null;
  }

  private static void openBrowser(String url) throws IOException {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(URI.create(url));
    }
    else {
      String os = System.getProperty("os.name").toLowerCase();
      Runtime rt = Runtime.getRuntime();
      if (os.indexOf("win") >= 0) {
        // this doesn't support showing urls in the form of
        // "page.html#nameLink"
        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
      }
      else if (os.indexOf("mac") >= 0) {
        rt.exec("open " + url);
      }
      else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
        // Do a best guess on unix until we get a platform independent way
        // Build a list of browsers to try, in this order.
        String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
            "netscape", "opera", "links", "lynx" };

        // Build a command string which looks like
        // "browser1 "url" || browser2 "url" ||..."
        StringBuffer cmd = new StringBuffer();
        for (int i = 0; i < browsers.length; i++)
          cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");
        rt.exec(new String[] { "sh", "-c", cmd.toString() });
      }
    }
  }
}
