package com.dgex.offspring.application.ui.messages;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.dgex.offspring.nxtCore.service.IMessage;
import com.dgex.offspring.user.service.IUser;

public class MessagesContentProvider implements IStructuredContentProvider {

  private IUser user = null;
  private String filterAccount = null;

  public void setAccountFilter(String account) {
    filterAccount = account;
  }

  public String getAccountFilter() {
    return filterAccount;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.user = (IUser) newInput;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (user == null) {
      return new Object[0];
    }

    List<IMessage> aliases = user.getAccount().getMessages();
    if (aliases == null) {
      return new Object[0];
    }
    return aliases.toArray(new Object[aliases.size()]);
  }

  @Override
  public void dispose() {
    user = null;
  }
}
