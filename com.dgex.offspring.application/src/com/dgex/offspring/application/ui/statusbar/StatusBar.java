package com.dgex.offspring.application.ui.statusbar;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.Block;
import nxt.Nxt;
import nxt.peer.Peer;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.dgex.offspring.config.Formatter;
import com.dgex.offspring.nxtCore.service.INxtService;

public class StatusBar {

  static Logger logger = Logger.getLogger(StatusBar.class);

  private Composite mainComposite;
  private Composite messageGroup;
  private Composite progressGroup;
  private Composite peersGroup;
  private Composite blocksGroup;
  private Composite downloadsGroup;
  private Label messageText;
  private Label peersText;
  private Label blocksText;
  private Label downloadsText;

  private INxtService nxt;
  private ProgressIndicator progressIndicator;
  private PixelConverter pixelConverter;
  private final BlockchainDownloadMonitor downloadMonitor = new BlockchainDownloadMonitor();

  static final String INITIALIZING_TEXT = "Initializing NXT " + Nxt.VERSION
      + " (might take several minutes)";

  private String messageTextValue = INITIALIZING_TEXT;
  private Block lastBlock = null;
  private long messageTime = 0l;

  @PostConstruct
  public void postConstruct(Composite parent, INxtService _nxt,
      final Display display) {
    this.nxt = _nxt;
    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(5).spacing(5, 0).margins(5, 5)
        .applyTo(mainComposite);
    pixelConverter = new PixelConverter(mainComposite);
    createContents(mainComposite);

    /* Display the block age each 1/5 th of second */
    display.timerExec(200, new Runnable() {

      @Override
      public void run() {
        if (display != null && !display.isDisposed() && messageText != null
            && !messageText.isDisposed() && blocksText != null
            && !blocksText.isDisposed()) {
          if ((System.currentTimeMillis() - messageTime) < 2000) {
            messageText.setText(messageTextValue);
          }
          else if (lastBlock != null) {
            messageText.setText(createBlockAgeText(lastBlock));
          }
          else {
            messageText.setText("");
          }
          messageText.pack();

          if (!downloadMonitor.isActive() && lastBlock != null) {
            downloadMonitor.blockPushed(lastBlock);
          }
          if (lastBlock != null) {
            blocksText.setText(createBlockText(lastBlock.getHeight(), 0));
            blocksText.pack();
          }
          mainComposite.layout();
          display.timerExec(200, this);
        }
      }
    });
  }

  private void createContents(Composite parent) {
    messageGroup = new Composite(parent, SWT.NONE);
    progressGroup = new Composite(parent, SWT.NONE);
    peersGroup = new Composite(parent, SWT.NONE);
    blocksGroup = new Composite(parent, SWT.NONE);
    downloadsGroup = new Composite(parent, SWT.NONE);

    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .grab(true, false).applyTo(messageGroup);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .hint(pixelConverter.convertHorizontalDLUsToPixels(130), SWT.DEFAULT)
        .applyTo(progressGroup);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(peersGroup);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(blocksGroup);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(downloadsGroup);

    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(messageGroup);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(progressGroup);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(peersGroup);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(blocksGroup);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(downloadsGroup);

    messageText = new Label(messageGroup, SWT.NONE);
    messageText.setText(messageTextValue);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(messageText);

    progressIndicator = new ProgressIndicator(progressGroup);
    GridDataFactory.fillDefaults().grab(true, false)
        .align(SWT.FILL, SWT.BEGINNING).applyTo(progressIndicator);

    peersText = new Label(peersGroup, SWT.NONE);
    peersText.setText(createPeerText(0, 0, 0));
    peersText.setToolTipText("Active peers / Known peers / Blacklisted peers");
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(peersText);

    downloadsText = new Label(downloadsGroup, SWT.NONE);
    downloadsText.setText(createDownloadsText(0, 0));
    downloadsText.setToolTipText("Download volume / Upload volume");
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(downloadsText);

    blocksText = new Label(blocksGroup, SWT.NONE);
    blocksText.setText(createBlockText(0, 0));
    blocksText.setToolTipText("Most recent block height");
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
        .applyTo(blocksText);

    downloadMonitor.setProgress(progressIndicator);
  }

  private void setStatusText(String message, boolean force) {
    messageTime = System.currentTimeMillis();
    messageTextValue = message;
    if (force && messageText != null && !messageText.isDisposed()) {
      messageText.setText(messageTextValue);
    }
  }

  @Inject
  @Optional
  private void onInitializationStart(
      @UIEventTopic(INxtService.TOPIC_INITIALIZATION_START) int dummy) {
    setStatusText(INITIALIZING_TEXT, true);
  }

  @Inject
  @Optional
  private void onInitializationFinished(
      @UIEventTopic(INxtService.TOPIC_INITIALIZATION_FINISHED) int dummy) {
    setStatusText("Done", true);
    downloadMonitor.done();
  }

  @Inject
  @Optional
  private void onLoggerMessage(
      @UIEventTopic(INxtService.TOPIC_LOGGER_MESSAGE) String message) {
    if (!nxt.isInitializing() && !nxt.isScanning()) {
      setStatusText(message, true);
    }
  }

  @Inject
  @Optional
  private void onLoggerException(
      @UIEventTopic(INxtService.TOPIC_LOGGER_EXCEPTION) Exception exception) {
    setStatusText("EXCEPTION >> " + exception, true);
  }

  @Inject
  @Optional
  private void onPeerEvent(@UIEventTopic(INxtService.TOPIC_PEER) Peer peer) {
    if (peersText != null && !peersText.isDisposed()) {
      int active = nxt.getAllConnectedPeers().size();
      int known = nxt.getAllWellknownPeers().size();
      int blacklisted = nxt.getAllBlacklistedPeers().size();

      peersText.setText(createPeerText(active, known, blacklisted));
      peersText.pack();

      long downloadVolume = 0;
      long uploadVolume = 0;
      for (Peer p : nxt.getAllConnectedPeers()) {
        downloadVolume += p.getDownloadedVolume();
        uploadVolume += p.getUploadedVolume();
      }
      downloadsText.setText(createDownloadsText(downloadVolume, uploadVolume));
      downloadsText.pack();

      mainComposite.layout();
    }
  }

  @Inject
  @Optional
  private void onBlockScanStart(
      @UIEventTopic(INxtService.TOPIC_BLOCK_SCANNER_START) int dummy) {
    setStatusText("Scanning blockchain", true);
    downloadMonitor.done();
  }

  @Inject
  @Optional
  private void onBlockScanFinished(
      @UIEventTopic(INxtService.TOPIC_BLOCK_SCANNER_FINISHED) int dummy) {
    setStatusText("Scan complete", true);
    downloadMonitor.done();
  }

  @Inject
  @Optional
  private void onBlockScanned(
      @UIEventTopic(INxtService.TOPIC_BLOCK_SCANNED) Block block) {
    lastBlock = block;
    downloadMonitor.blockPushed(block);
  }

  /* For each block pushed/poped we update the text in the blocks section */
  @Inject
  @Optional
  private void onBlockPushed(
      @UIEventTopic(INxtService.TOPIC_BLOCK_PUSHED) Block block) {
    lastBlock = block;
    downloadMonitor.blockPushed(block);
  }

  @Inject
  @Optional
  private void onBlockPopped(
      @UIEventTopic(INxtService.TOPIC_BLOCK_POPPED) Block block) {
    lastBlock = block;
  }

  private String createPeerText(int active, int known, int blacklisted) {
    StringBuilder b = new StringBuilder();
    //    b.append(Messages.StatusBarToolControl_label_peers + " "); //$NON-NLS-2$
    b.append(Integer.toString(active));
    b.append("/"); //$NON-NLS-1$
    b.append(Integer.toString(known));
    b.append("/"); //$NON-NLS-1$
    b.append(Integer.toString(blacklisted));
    return b.toString();
  }

  private String createBlockText(int height, int totalBlocks) {
    return Integer.toString(height);
  }

  private String createDownloadsText(long downloadVolume, long uploadVolume) {
    StringBuilder sb = new StringBuilder();
    sb.append(Formatter.readableFileSize(downloadVolume));
    sb.append("/");
    sb.append(Formatter.readableFileSize(uploadVolume));
    return sb.toString();
  }

  private final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "dd MMM yy H:mm:ss");

  private String createBlockAgeText(Block block) {
    Date date = new Date(nxt.convertTimestamp(block.getTimestamp()));

    StringBuilder sb = new StringBuilder();
    if (nxt.isScanning()) {
      sb.append("Scanning ");
    }
    else if ((System.currentTimeMillis() - date.getTime()) > 240 * 1000) {
      sb.append("Downloading ");
    }
    else {
      sb.append("Newest block ");
    }
    sb.append(dateFormat.format(date));
    sb.append(" (" + ((new Date().getTime() - date.getTime()) / 1000)
        + " seconds)");

    return sb.toString();
  }
}
