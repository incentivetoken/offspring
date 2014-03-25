package com.dgex.offspring.wallet;

import org.json.simple.JSONObject;

public interface IWalletAccount {

  public String getLabel();

  public JSONObject toJSONObject();

}
