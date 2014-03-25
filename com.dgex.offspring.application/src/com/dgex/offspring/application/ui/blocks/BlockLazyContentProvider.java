package com.dgex.offspring.application.ui.blocks;

import nxt.Block;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.dgex.offspring.nxtCore.service.INxtService;

public class BlockLazyContentProvider implements ILazyContentProvider {

  private static Logger logger = Logger
      .getLogger(BlockLazyContentProvider.class);

  private final TableViewer viewer;
  private INxtService nxt;

  public BlockLazyContentProvider(TableViewer viewer) {
    this.viewer = viewer;
  }

  @Override
  public void dispose() {
    this.nxt = null;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.nxt = (INxtService) newInput;
  }

  @Override
  public void updateElement(int index) {

    /*
     * To *reverse* the index is to have the *last* block returned when block 0
     * is requested and the first block returned when the last row is requested.
     * The total number of blocks is always in nxt.getBlockCount().
     */
    int height = (nxt.getBlockCount() - index) - 1;
    Block block = nxt.getBlockAtHeight(height);

    // logger.info("index=" + index + " height=" + height + " block=" + block);

    if (block != null)
      viewer.replace(block, index);
  }
}
