package com.dgex.offspring.application.ui.messages;

import java.text.DateFormat;
import java.util.Date;

import nxt.util.Convert;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.dgex.offspring.application.utils.ICellDataLabelProvider;
import com.dgex.offspring.config.Images;
import com.dgex.offspring.nxtCore.service.IMessage;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUser;

public class MessagesLabelProvider extends ColumnLabelProvider implements
    ICellDataLabelProvider {

  private static final String EMPTY_STRING = "";

  private static final Image MESSAGE_SENDER = Images.getImage("bullet_go.png");
  private static final Image MESSAGE_RECEIVER = Images
      .getImage("bullet_red.png");

  private final MessagesTableViewer viewer;
  private final INxtService nxt;
  private final IUser user;

  public MessagesLabelProvider(MessagesTableViewer viewer, IUser user,
      INxtService nxt) {
    this.viewer = viewer;
    this.user = user;
    this.nxt = nxt;

  }

  @Override
  public void getCellData(Object o, int columnId, Object[] data) {
    IMessage message = (IMessage) o;
    switch (columnId) {

    case MessagesTable.COLUMN_TYPE:
      if (user.getAccount().equals(message.getRecipient())) {
        data[IMAGE] = MESSAGE_RECEIVER;
      }
      else if (user.getAccount().equals(message.getSender())) {
        data[IMAGE] = MESSAGE_SENDER;
      }
      data[TEXT] = EMPTY_STRING;
      break;

    case MessagesTable.COLUMN_TIMESTAMP:
      Date date = new Date(nxt.convertTimestamp(message.getTimestamp()));
      DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
      DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT);
      data[TEXT] = dateFormatter.format(date) + " "
          + timeFormatter.format(date);

      break;

    case MessagesTable.COLUMN_ID:
      data[TEXT] = Convert.toUnsignedLong(message.getId());
      break;

    case MessagesTable.COLUMN_RECIPIENT:
      data[TEXT] = message.getRecipient().getStringId();
      break;

    case MessagesTable.COLUMN_SENDER:
      data[TEXT] = message.getSender().getStringId();
      break;

    case MessagesTable.COLUMN_MESSAGE:
      data[TEXT] = message.getMessage();
      break;

    default:
      data[TEXT] = "UNKNOWN " + columnId;
      break;
    }
  }

  @Override
  public void update(ViewerCell cell) {
    super.update(cell);
    IMessage t = (IMessage) cell.getElement();
    Object[] data = { null, null, null, null };
    getCellData(t, MessagesTable.getColumns()[cell.getColumnIndex()], data);
    if (data[TEXT] != null)
      cell.setText((String) data[TEXT]);
    if (data[IMAGE] != null)
      cell.setImage((Image) data[IMAGE]);
    if (data[FONT] != null)
      cell.setFont((Font) data[FONT]);
    if (data[FOREGROUND] != null)
      cell.setForeground((Color) data[FOREGROUND]);
  }
}
