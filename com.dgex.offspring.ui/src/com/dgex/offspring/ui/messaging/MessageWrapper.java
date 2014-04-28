package com.dgex.offspring.ui.messaging;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import nxt.Account;
import nxt.Attachment;
import nxt.Transaction;

import org.bouncycastle.crypto.InvalidCipherTextException;

import com.dgex.offspring.config.Config;


public class MessageWrapper {

  private final Transaction transaction;
  private final String secretPhrase;
  private final Account account;

  /**
   * Wraps a Transaction message in the context of a single account.
   * 
   * @param transaction
   *          AM Transaction
   * @param account
   *          Account Id
   * @param secretPhrase
   *          Optional secret phrase (or null if we dont have it)
   */
  public MessageWrapper(Transaction transaction, Account account,
      String secretPhrase) {
    this.transaction = transaction;
    this.account = account;
    this.secretPhrase = secretPhrase;
  }

  public int getTimestamp() {
    return transaction.getTimestamp();
  }

  public Long getId() {
    return transaction.getId();
  }

  public Long getSenderId() {
    return transaction.getSenderId();
  }

  public Long getReceipientId() {
    return transaction.getRecipientId();
  }

  public String getMessage() {
    if (isEncrypted()) {

      /* Message is encrypted but we dont have the key */
      if (secretPhrase == null) {
        return "... Encrypted ...";
      }
      
      if (account == null) {
        return "... Initializing ...";
      }

      byte[] theirPublicKey = null;
      if (getReceipientId().equals(account.getId())) {
        Account acc = Account.getAccount(getSenderId());
        if (acc != null) {
          theirPublicKey = acc.getPublicKey();
        }
      }
      else if (getSenderId().equals(account.getId())) {
        Account acc = Account.getAccount(getReceipientId());
        if (acc != null) {
          theirPublicKey = acc.getPublicKey();
        }
      }
      else {
        return "Huh?";
      }

      try {
        if (theirPublicKey == null) {
          return "... Initializing ...";
        }
        return MessageCrypto.decrypt(getBytes(), secretPhrase, theirPublicKey);
      }
      catch (UnsupportedEncodingException e) {
        e.printStackTrace(System.err);
        return "... Error unsupported encoding ...";
      }
      catch (GeneralSecurityException e) {
        e.printStackTrace(System.err);
        return "... Security exception ...";
      }
      catch (RuntimeException e) {
        e.printStackTrace(System.err);
        return "... Unsupported encryption ...";
      }
      catch (InvalidCipherTextException e) {
        e.printStackTrace(System.err);
        return "... Invalid ciphertext ...";
      }
    }
    /* Unencrypted message */
    try {
      byte[] bytes = getBytes();
      if (MessageCrypto.startsWithMagicUnEncryptedByte(bytes)) {
        return new String(bytes, "UTF-8")
          .substring(Config.MAGIC_UNENCRYPTED_MESSAGE_NUMBER.length);
      }
      return new String(bytes, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "... Error unsupported encoding ...";
    }
  }

  public boolean isMessage() {
    byte[] bytes = getBytes();
    return MessageCrypto.startsWithMagicUnEncryptedByte(bytes)
        || MessageCrypto.startsWithMagicEncryptedByte(bytes);
  }

  public boolean isEncrypted() {
    return MessageCrypto.startsWithMagicEncryptedByte(getBytes());
  }

  private byte[] getBytes() {
    return ((Attachment.MessagingArbitraryMessage) transaction.getAttachment())
        .getMessage();
  }
}
