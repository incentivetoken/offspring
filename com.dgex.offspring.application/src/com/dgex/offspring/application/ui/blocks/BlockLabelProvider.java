package com.dgex.offspring.application.ui.blocks;

import java.text.DateFormat;
import java.util.Date;

import nxt.Block;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import com.dgex.offspring.config.Formatter;
import com.dgex.offspring.config.Images;
import com.dgex.offspring.nxtCore.service.Utils;

public class BlockLabelProvider extends ColumnLabelProvider {

  private static final Image BLOCK = Images.getImage("bricks.png");

  private static final Image GENERATOR = Images.getImage("user_green.png");

  private String formatTimestamp(int timestamp) {
    Date date = Formatter.formatTimestamp((long) timestamp);
    DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
    DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
    return dateFormatter.format(date) + " " + timeFormatter.format(date);
  }

  @Override
  public void update(ViewerCell cell) {
    super.update(cell);
    Block b = (Block) cell.getElement();
    switch (BlockTable.getColumns()[cell.getColumnIndex()]) {

    case BlockTable.COLUMN_HEIGHT:
      cell.setText(Integer.toString(b.getHeight()));
      break;

    case BlockTable.COLUMN_NUMBER_OF_TRANSACTIONS:
      cell.setText(Integer.toString(b.getTransactions().size()));
      break;

    case BlockTable.COLUMN_TOTAL_AMOUNT:
      cell.setText(Utils.quantToString(b.getTotalAmountNQT(), 8));
      break;

    case BlockTable.COLUMN_TOTAL_FEE:
      cell.setText(Utils.quantToString(b.getTotalFeeNQT(), 8));
      break;

    case BlockTable.COLUMN_PAYLOAD_LENGTH:
      cell.setText(Formatter.readableFileSize(b.getPayloadLength()));
      break;

    case BlockTable.COLUMN_VERSION:
      cell.setText(Integer.toString(b.getVersion()));
      break;

    case BlockTable.COLUMN_BASETARGET:
      cell.setText(Formatter.formatBaseTarget(b.getBaseTarget()) + " %");
      break;

    case BlockTable.COLUMN_BLOCK:
      cell.setImage(BLOCK);
      cell.setText(b.getStringId());
      break;

    case BlockTable.COLUMN_GENERATOR:
      // cell.setImage(GENERATOR);
      cell.setText(Long.toString(b.getGeneratorId()));
      break;

    case BlockTable.COLUMN_TIMESTAMP:
      cell.setText(formatTimestamp(b.getTimestamp()));
      break;

    default:
      cell.setText("UNKNOWN " + BlockTable.getColumns()[cell.getColumnIndex()]);
    }
  }

}
