package com.dgex.offspring.ui.messaging;

import java.util.ArrayList;
import java.util.List;


public class MessageNodeImpl implements IMessageNode {

  private final IMessageNode parent;
  private final MessageWrapper message;
  private List<IMessageNode> children = null;

  public MessageNodeImpl(IMessageNode parent, MessageWrapper message) {
    this.parent = parent;
    this.message = message;
  }

  @Override
  public IMessageNode getParent() {
    return parent;
  }

  @Override
  public List<IMessageNode> getChildren() {
    if (children == null)
      children = new ArrayList<IMessageNode>();
    return children;
  }

  @Override
  public MessageWrapper getMessage() {
    return message;
  }

  @Override
  public boolean hasChildren() {
    return children != null;
  }

}
