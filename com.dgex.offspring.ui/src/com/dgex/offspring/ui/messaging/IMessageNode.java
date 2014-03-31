package com.dgex.offspring.ui.messaging;

import java.util.List;

public interface IMessageNode {

  public IMessageNode getParent();

  public List<IMessageNode> getChildren();

  public boolean hasChildren();

  public MessageWrapper getMessage();

}
