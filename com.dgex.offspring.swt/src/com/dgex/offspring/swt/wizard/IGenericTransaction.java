package com.dgex.offspring.swt.wizard;

public interface IGenericTransaction {

  public IGenericTransactionField[] getFields();

  public boolean verifySender(String[] message);

  public String sendTransaction(String[] message);
}
