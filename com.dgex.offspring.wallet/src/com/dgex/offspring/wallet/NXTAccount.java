package com.dgex.offspring.wallet;

import org.json.simple.JSONObject;

public class NXTAccount implements INXTWalletAccount {

  public final static String KEY_LABEL = "label";
  public final static String KEY_ACCOUNT = "account";
  public final static String KEY_KEY = "key";
  public final static String KEY_BALANCE = "balance";
  public final static String KEY_TYPE = "type";

  private final String label;
  private final String account;
  private final String privateKey;
  private final Object balance;

  public NXTAccount(String label, String account, String privateKey,
      Object balance) {
    if (label == null || label.isEmpty())
      throw new IllegalArgumentException("illegal label");
    if (account == null || account.isEmpty())
      throw new IllegalArgumentException("illegal account");
    // if (privateKey == null)
    // throw new IllegalArgumentException("illegal privateKey");

    this.label = label;
    this.account = account;
    this.privateKey = privateKey;
    this.balance = balance;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NXTAccount) {
      if (label.equals(((NXTAccount) obj).label)) {
        if (account.equals(((NXTAccount) obj).account)) {
          if (privateKey == null) {
            return ((NXTAccount) obj).privateKey == null;
          }
          else {
            return privateKey.equals(((NXTAccount) obj).privateKey);
          }
        }
      }
    }
    return false;
  }

  public static INXTWalletAccount create(JSONObject obj) {
    Object key = obj.get(KEY_KEY);
    return new NXTAccount((String) obj.get(KEY_LABEL),
        (String) obj.get(KEY_ACCOUNT), key == null ? null : (String) key,
        obj.get(KEY_BALANCE));
  }

  public static INXTWalletAccount create(String label, String account,
      String privateKey, Object balance) {
    return new NXTAccount(label, account, privateKey, balance);
  }

  @SuppressWarnings("unchecked")
  @Override
  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject();
    obj.put(KEY_LABEL, label);
    obj.put(KEY_ACCOUNT, account);
    if (privateKey != null) {
      obj.put(KEY_KEY, privateKey);
    }
    obj.put(KEY_BALANCE, balance);
    obj.put(KEY_TYPE, "nxt");
    return obj;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getPrivateKey() {
    return privateKey;
  }

  @Override
  public String getAccountNumber() {
    return account;
  }

  @Override
  public long getBalance() {
    if (balance instanceof Double)
      return ((Double) balance).longValue();
    else if (balance instanceof Long)
      return (Long) balance;
    return 0;
  }

}
