package com.dgex.offspring.update;


public class UpdateJob {

  // // repository location needs to be adjusted for your
  // // location
  // private static final String REPOSITORY_LOC = System.getProperty(
  // "UpdateJob.Repo", "file://home/dirk/OFFSPRING-REPO");
  //
  // @Execute
  // public void execute(final IProvisioningAgent agent, final Shell parent,
  // final UISynchronize sync, final IWorkbench workbench,
  // final Display display, final IEmbeddedJettyService nxt, final IDataProviderPool pool)
  // {
  //
  // Job j = new Job("Update Job") {
  //
  // private boolean doInstall = false;
  //
  // @Override
  // protected IStatus run(final IProgressMonitor monitor) {
  //
  // /* 1. Prepare update plumbing */
  //
  // final ProvisioningSession session = new ProvisioningSession(agent);
  // final UpdateOperation operation = new UpdateOperation(session);
  //
  // // create uri
  // URI uri = null;
  // try {
  // uri = new URI(REPOSITORY_LOC);
  // }
  // catch (final URISyntaxException e) {
  // sync.syncExec(new Runnable() {
  //
  // @Override
  // public void run() {
  // MessageDialog.openError(parent, "URI invalid", e.getMessage());
  // }
  // });
  // return Status.CANCEL_STATUS;
  // }
  //
  // // set location of artifact and metadata repo
  // operation.getProvisioningContext().setArtifactRepositories(
  // new URI[] { uri });
  // operation.getProvisioningContext().setMetadataRepositories(
  // new URI[] { uri });
  //
  // /* 2. check for updates */
  //
  // // run update checks causing I/O
  // final IStatus status = operation.resolveModal(monitor);
  //
  // // failed to find updates (inform user and exit)
  // if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
  // sync.syncExec(new Runnable() {
  //
  // @Override
  // public void run() {
  // MessageDialog.openWarning(parent, "No update",
  // "No updates for the current installation have been found");
  // }
  // });
  // return Status.CANCEL_STATUS;
  // }
  //
  // /* 3. Ask if updates should be installed and run installation */
  //
  // // found updates, ask user if to install?
  // if (status.isOK() && status.getSeverity() != IStatus.ERROR) {
  // sync.syncExec(new Runnable() {
  //
  // @Override
  // public void run() {
  // String updates = "";
  // Update[] possibleUpdates = operation.getPossibleUpdates();
  // for (Update update : possibleUpdates) {
  // updates += update + "\n";
  // }
  // doInstall = MessageDialog.openQuestion(parent,
  // "Really install updates?", updates);
  // }
  // });
  // }
  //
  // // start installation
  // if (doInstall) {
  //
  // final UpdateManager collector = new UpdateManager();
  //
  // final ProvisioningJob provisioningJob = operation
  // .getProvisioningJob(monitor);
  // // updates cannot run from within Eclipse IDE!!!
  // if (provisioningJob == null) {
  // System.err
  // .println("Running update from within Eclipse IDE? This won't work!!!");
  // throw new NullPointerException();
  // }
  //
  // // register a job change listener to track
  // // installation progress and notify user upon success
  // provisioningJob.addJobChangeListener(new JobChangeAdapter() {
  //
  // @Override
  // public void done(IJobChangeEvent event) {
  // if (event.getResult().isOK()) {
  // sync.syncExec(new Runnable() {
  //
  // @Override
  // public void run() {
  //
  // try {
  // collector.printDifference();
  // }
  // catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  //
  // boolean restart = MessageDialog
  // .openQuestion(
  // parent,
  // "Updates installed, restart?",
  // "Updates have been installed successfully, you must restart for changes to have effect.\nDo you want to restart Offspring?");
  // if (restart) {
  //
  // /* Before we restart we should go over all files in */
  //
  // Shutdown.execute(display, nxt, pool);
  // workbench.restart();
  //
  // }
  // }
  // });
  // }
  // super.done(event);
  // }
  // });
  //
  // provisioningJob.schedule();
  // }
  // return Status.OK_STATUS;
  // }
  // };
  // j.schedule();
  // }
}