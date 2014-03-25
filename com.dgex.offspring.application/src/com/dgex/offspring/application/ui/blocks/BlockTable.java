package com.dgex.offspring.application.ui.blocks;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.SWT;

import com.dgex.offspring.messages.Messages;

public class BlockTable {

  public final static int COLUMN_HEIGHT = 34;
  public final static int COLUMN_GENERATOR = 35;
  public final static int COLUMN_TIMESTAMP = 36;
  public final static int COLUMN_NUMBER_OF_TRANSACTIONS = 37;
  public final static int COLUMN_TOTAL_AMOUNT = 38;
  public final static int COLUMN_TOTAL_FEE = 39;
  public final static int COLUMN_PAYLOAD_LENGTH = 40;
  public final static int COLUMN_VERSION = 41;
  public final static int COLUMN_BLOCK = 42;
  public final static int COLUMN_BASETARGET = 43;

  private final static int[] columns = new int[] {

  COLUMN_HEIGHT,

  COLUMN_TIMESTAMP,

  COLUMN_BLOCK,

  COLUMN_TOTAL_FEE,

  COLUMN_TOTAL_AMOUNT,

  COLUMN_NUMBER_OF_TRANSACTIONS,

  COLUMN_BASETARGET,

  COLUMN_PAYLOAD_LENGTH,

  COLUMN_VERSION,

  COLUMN_GENERATOR };

  public static int[] getColumns() {
    return columns;
  }

  public static CellLabelProvider createLabelProvider(int id) {
    return new BlockLabelProvider();
  }

  public static String getColumnLabel(int id) {
    switch (id) {
    case COLUMN_HEIGHT:
      return Messages.BlockTable_column_height_label;
    case COLUMN_GENERATOR:
      return Messages.BlockTable_column_generator_label;
    case COLUMN_TIMESTAMP:
      return Messages.BlockTable_column_timestamp_label;
    case COLUMN_NUMBER_OF_TRANSACTIONS:
      return Messages.BlockTable_column_count_label;
    case COLUMN_TOTAL_AMOUNT:
      return Messages.BlockTable_column_amount_label;
    case COLUMN_TOTAL_FEE:
      return Messages.BlockTable_column_fee_label;
    case COLUMN_PAYLOAD_LENGTH:
      return Messages.BlockTable_column_payload_label;
    case COLUMN_VERSION:
      return Messages.BlockTable_column_version_label;
    case COLUMN_BLOCK:
      return Messages.BlockTable_column_block_label;
    case COLUMN_BASETARGET:
      return Messages.BlockTable_column_target_label;
    }
    return "FAILURE"; //$NON-NLS-1$
  }

  public static int getColumnWidth(int id) {
    switch (id) {
    case COLUMN_TOTAL_FEE:
    case COLUMN_NUMBER_OF_TRANSACTIONS:
    case COLUMN_VERSION:
      return 50;

    case COLUMN_TOTAL_AMOUNT:
    case COLUMN_HEIGHT:
    case COLUMN_BASETARGET:
    case COLUMN_PAYLOAD_LENGTH:
      return 75;

    case COLUMN_BLOCK:
    case COLUMN_GENERATOR:
    case COLUMN_TIMESTAMP:
      return 200;
    }
    return 10;
  }

  public static int getColumnAlignment(int id) {
    switch (id) {
    case COLUMN_HEIGHT:
    case COLUMN_NUMBER_OF_TRANSACTIONS:
    case COLUMN_TOTAL_AMOUNT:
    case COLUMN_TOTAL_FEE:
    case COLUMN_PAYLOAD_LENGTH:
    case COLUMN_VERSION:
    case COLUMN_BASETARGET:
      return SWT.RIGHT;
    }
    return SWT.LEFT;
  }

  public static boolean getColumnResizable(int id) {
    switch (id) {
    case COLUMN_HEIGHT:
    case COLUMN_NUMBER_OF_TRANSACTIONS:
    case COLUMN_TOTAL_FEE:
    case COLUMN_PAYLOAD_LENGTH:
    case COLUMN_VERSION:
    case COLUMN_BASETARGET:
      return false;
    }
    return true;
  }
}
