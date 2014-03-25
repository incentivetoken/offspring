package com.dgex.offspring.wallet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.dgex.offspring.config.Config;
import com.dgex.offspring.wallet.Cryptos.DecryptException;
import com.dgex.offspring.wallet.Cryptos.EncryptException;

@SuppressWarnings("serial")
public class Wallet implements IWallet {

  public static class WalletDecodeException extends Exception {}

  public static class WalletEncodeException extends Exception {}

  private final static Logger logger = Logger.getLogger(Wallet.class);
  private final static String KEY_ACCOUNTS = "walletAccounts";
  private final static String KEY_PADDING = "padding";
  private final List<IWalletAccount> walletAccounts = new ArrayList<IWalletAccount>();
  private File file = null;
  private String password = null;
  private boolean initialized = false;
  private boolean fileExists = false;

  public Wallet() {
    this.file = getDefaultWalletFile();
  }

  public Wallet(File file) {
    this.file = file;
  }

  @Override
  public File getDefaultWalletFile() {
    return Config.getAppPath("offspring.wallet");
  }

  @Override
  public void initialize(String password) throws WalletInvalidPassword {
    if (initialized)
      return;

    this.password = password;
    if (file.exists()) {
      String content;
      if (password == null)
        content = new String(readBytes(file));
      else {
        try {
          content = Cryptos.decrypt(password, readBytes(file));
        }
        catch (DecryptException e) {
          throw new WalletInvalidPassword(e);
        }
      }

      try {
        decode(content, walletAccounts);
      }
      catch (WalletDecodeException e) {
        clear();
        throw new WalletInvalidPassword(e);
      }
      fileExists = true;
    }
    initialized = true;
  }

  @Override
  public List<IWalletAccount> getAccounts()
      throws WalletNotInitializedException {

    if (!initialized)
      throw new WalletNotInitializedException();

    return walletAccounts;
  }

  @Override
  public IWalletStatus addAccount(IWalletAccount walletAccount)
      throws WalletNotInitializedException, DuplicateAccountException,
      WalletBackupException {

    if (!initialized)
      throw new WalletNotInitializedException();

    if (walletAccounts.contains(walletAccount))
      throw new DuplicateAccountException();

    WalletStatus status = new WalletStatus(this);
    if (fileExists) {
      try {
        File backup = createBackupFile();
        backup(backup);
        status.backupFile = backup;
      }
      catch (IOException ex) {
        throw new WalletBackupException();
      }
    }

    walletAccounts.add(walletAccount);

    try {
      save(file);
      if (fileExists)
        status.backupFile.deleteOnExit();
      fileExists = true;
    }
    catch (WalletSaveException e) {
      status.throwable = e;
      status.status = IWalletStatus.FAILURE;
    }

    return status;
  }

  @Override
  public IWalletStatus removeAccount(IWalletAccount walletAccount)
      throws WalletNotInitializedException, AccountNotFoundException,
      WalletBackupException {

    if (!initialized)
      throw new WalletNotInitializedException();

    if (!walletAccounts.contains(walletAccount))
      throw new AccountNotFoundException();

    WalletStatus status = new WalletStatus(this);
    if (fileExists) {
      try {
        File backup = createBackupFile();
        backup(backup);
        status.backupFile = backup;
      }
      catch (IOException ex) {
        throw new WalletBackupException();
      }
    }

    walletAccounts.remove(walletAccount);

    try {
      save(file);
      if (fileExists)
        status.backupFile.deleteOnExit();
      fileExists = true;
    }
    catch (WalletSaveException e) {
      status.throwable = e;
      status.status = IWalletStatus.FAILURE;
    }

    return status;
  }

  private void backup(File backupFile) throws WalletBackupException {
    try {
      FileUtils.copyFile(file, backupFile);

      if (!FileUtils.contentEquals(file, backupFile))
        throw new WalletBackupException();
    }
    catch (IOException e) {
      throw new WalletBackupException(e);
    }

    try {
      synchronized (walletAccounts) {
        verify(backupFile);
      }
    }
    catch (WalletVerifyException e) {
      throw new WalletBackupException(e);
    }
  }

  private void save(File file) throws WalletSaveException {
    String content;
    try {
      content = toJSONObject().toJSONString();
    }
    catch (WalletNotInitializedException e) {
      throw new WalletSaveException(e);
    }

    byte[] bytes = null;
    try {
      bytes = content.getBytes(Config.offspring_charset);
    }
    catch (UnsupportedEncodingException e) {
      logger.error("CHARACTER SET NOT SUPPORTED!!", e);
      throw new WalletSaveException(e);
    }

    if (password != null) {
      try {
        bytes = Cryptos.encrypt(password, bytes);
      }
      catch (EncryptException e) {
        throw new WalletSaveException(e);
      }
    }

    if (!writeBytes(file, bytes))
      throw new WalletSaveException();

    try {
      synchronized (walletAccounts) { // this doesnt make so much sense
        verify(file);
      }
    }
    catch (WalletVerifyException e) {
      throw new WalletSaveException(e);
    }
  }

  private void verify(File file) throws WalletVerifyException {
    String content = null;
    try {
      content = readString(file);
    }
    catch (DecryptException e) {
      throw new WalletVerifyException(e);
    }

    List<IWalletAccount> list = new ArrayList<IWalletAccount>();
    try {
      decode(content, list);
    }
    catch (WalletDecodeException e) {
      throw new WalletVerifyException(e);
    }

    if (list.size() != walletAccounts.size())
      throw new WalletVerifyException();

    for (int i = 0; i < list.size(); i++) {
      if (!walletAccounts.get(i).equals(list.get(i))) {

        logger.warn("These Dont match");
        logger.warn("(1) "
            + walletAccounts.get(i).toJSONObject().toJSONString());
        logger.warn("(2) " + list.get(i).toJSONObject().toJSONString());

        throw new WalletVerifyException();
      }
    }
  }

  private int generateRandomNumber(int low, int high) {
    Random random = new Random();
    return random.nextInt(high - low) + low;
  }

  private String generateRandomText() {
    String symbols = "!$%^&*()-_=+[{]};:@#~|,<.>"; //$NON-NLS-1$
    String alphaNum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890"; //$NON-NLS-1$
    return RandomStringUtils.random(generateRandomNumber(70, 90), symbols
        + alphaNum);
  }

  @SuppressWarnings("unchecked")
  public JSONObject toJSONObject() throws WalletNotInitializedException {
    JSONObject obj = new JSONObject();
    JSONArray array = new JSONArray();
    obj.put(KEY_ACCOUNTS, array);
    for (IWalletAccount walletAccount : getAccounts()) {
      array.add(walletAccount.toJSONObject());
    }

    // TODO all padding is in a single block of the text file. this should be
    // scattered all over the file.

    /* add the padding under the "padding" key */
    array = new JSONArray();
    obj.put(KEY_PADDING, array);
    int count = generateRandomNumber(30, 60);
    for (int i = 0; i < count; i++) {
      array.add(generateRandomText());
    }
    return obj;
  }

  @SuppressWarnings("rawtypes")
  private void decode(String content, List<IWalletAccount> walletAccounts)
      throws WalletDecodeException {
    if (content == null || content.isEmpty())
      throw new WalletDecodeException();

    JSONObject obj = (JSONObject) JSONValue.parse(content);
    if (obj == null)
      throw new WalletDecodeException();

    if (!(obj.get(KEY_ACCOUNTS) instanceof List))
      throw new WalletDecodeException();

    for (Object acc : (List) obj.get(KEY_ACCOUNTS)) {
      if (!(acc instanceof JSONObject))
        throw new WalletDecodeException();
      try {
        walletAccounts.add(NXTAccount.create((JSONObject) acc));
      }
      catch (IllegalArgumentException ex) {
        throw new WalletDecodeException();
      }
    }
  }

  private File createBackupFile() throws IOException {
    File backupdir = new File(file.getParentFile() + File.separator + "wallet");
    if (!backupdir.exists())
      backupdir.mkdir();
    return File.createTempFile(file.getName(), "bak", backupdir);
  }

  private String readString(File file) throws DecryptException {
    if (password == null)
      return new String(readBytes(file));

    return Cryptos.decrypt(password, readBytes(file));
  }

  private static byte[] readBytes(File file) {
    FileInputStream in = null;
    byte[] result = new byte[(int) file.length()];
    try {
      in = new FileInputStream(file);
      in.read(result);
    }
    catch (Exception e) {
      logger.error("Could not read FileInputStream", e);
    }
    finally {
      try {
        if (in != null)
          in.close();
      }
      catch (Exception e) {
        logger.error("Could not close FileInputStream", e);
      }
    }
    return result;
  }

  private static boolean writeBytes(File file, byte[] contents) {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      if (!file.exists())
        file.createNewFile();

      out.write(contents);
      out.flush();
    }
    catch (IOException e) {
      logger.error("Could not write contents", e);
      return false;
    }
    finally {
      try {
        if (out != null)
          out.close();
      }
      catch (IOException e) {
        logger.error("Could not close Outputstream", e);
        return false;
      }
    }
    return true;
  }

  @Override
  public File getWalletFile() {
    return file.getAbsoluteFile();
  }

  @Override
  public void setWalletFile(File file) throws WalletInitializedException {
    if (initialized)
      throw new WalletInitializedException();

    this.file = file;
  }

  @Override
  public void clear() {
    this.password = null;
    this.file = null;
    this.fileExists = false;
    this.initialized = false;
    this.walletAccounts.clear();
  }

  @Override
  public void createWalletFile() throws WalletSaveException {
    save(file);
  }
}
