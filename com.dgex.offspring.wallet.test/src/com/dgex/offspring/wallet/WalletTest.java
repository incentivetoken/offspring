package com.dgex.offspring.wallet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dgex.offspring.wallet.IWalletAccount;
import com.dgex.offspring.wallet.IWallet;
import com.dgex.offspring.wallet.IWalletStatus;
import com.dgex.offspring.wallet.NXTAccount;
import com.dgex.offspring.wallet.Wallet;
import com.dgex.offspring.wallet.IWallet.AccountNotFoundException;
import com.dgex.offspring.wallet.IWallet.DuplicateAccountException;
import com.dgex.offspring.wallet.IWallet.WalletBackupException;
import com.dgex.offspring.wallet.IWallet.WalletInvalidPassword;
import com.dgex.offspring.wallet.IWallet.WalletNotInitializedException;

public class WalletTest {

  private final String password = "foo";
  private File file;

  private long balance() {
    return new Double(Math.random()).longValue();
  }

  // With ! space, backslash, newline, tab
  private String generate() {
    String symbols = "!\"$%^&*()-_=+[{]};:'@#~|,<.>/?\n\t\\\r";
    String alphaNum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
    int low = 96;
    int high = 145;
    Random random = new Random();
    int count = random.nextInt(high - low) + low;
    return RandomStringUtils.random(count, symbols + alphaNum);
  }

  private List<IWalletAccount> createAccounts() {
    List<IWalletAccount> walletAccounts = new ArrayList<IWalletAccount>();
    walletAccounts.add(NXTAccount.create("name a", "1234567890123456789",
        "#@%#&$$*(*&$)*$)__)$()*()(*(", 100l));
    walletAccounts.add(NXTAccount.create("name b", "2345678901234567890",
        "&**&&&*$^#*(&$#***", 1000l));
    walletAccounts.add(NXTAccount.create("name c", "3456789012345678901",
        "kfiubcuireybcui", 2983983l));
    return walletAccounts;
  }

  @Before
  public void initialize() throws IOException {
    file = File.createTempFile("wallettest", "wallet");
    if (file.exists())
      file.delete();
  }

  @After
  public void teardown() {
    if (file.exists())
      file.delete();
  }

  @Test
  public void testWalletAddRemove() throws WalletInvalidPassword,
      WalletNotInitializedException, DuplicateAccountException,
      WalletBackupException, AccountNotFoundException {
    Wallet wallet = new Wallet(file);
    wallet.initialize("foo");
    wallet.addAccount(createAccounts().get(0));
    wallet.removeAccount(createAccounts().get(0));
  }

  @Test
  public void testWalletVerified() throws WalletInvalidPassword,
      WalletNotInitializedException, DuplicateAccountException,
      WalletBackupException, IOException {

    List<IWalletAccount> walletAccounts = createAccounts();

    IWallet wallet = new Wallet(file);
    wallet.initialize("foo");
    for (IWalletAccount acc : walletAccounts) {
      IWalletStatus status = wallet.addAccount(acc);
      assertTrue(status.getThrowable() == null);
      assertTrue(status.getStatus() == IWalletStatus.SUCCESS);
    }

    wallet = new Wallet(file);
    wallet.initialize("foo");
    assertEquals(walletAccounts.size(), wallet.getAccounts().size());
    assertArrayEquals(walletAccounts.toArray(), wallet.getAccounts().toArray());
  }

  @Test
  public void testFusser() throws WalletInvalidPassword,
      WalletNotInitializedException, DuplicateAccountException,
      WalletBackupException, AccountNotFoundException {
    IWallet wallet = new Wallet(file);
    wallet.initialize("foo");

    List<IWalletAccount> walletAccounts = new ArrayList<IWalletAccount>();
    for (int i = 0; i < 10; i++)
      walletAccounts.add(NXTAccount.create(generate(), generate(), generate(),
          balance()));

    /* add all accounts */
    for (IWalletAccount acc : walletAccounts) {
      IWalletStatus status = wallet.addAccount(acc);
      assertTrue(status.getThrowable() == null);
      assertTrue(status.getStatus() == IWalletStatus.SUCCESS);
    }

    /* remove all accounts */
    for (IWalletAccount acc : walletAccounts) {
      IWalletStatus status = wallet.removeAccount(acc);
      assertTrue(status.getThrowable() == null);
      assertTrue(status.getStatus() == IWalletStatus.SUCCESS);
    }
  }

  @Test
  public void testBackup() throws WalletInvalidPassword,
      WalletNotInitializedException, DuplicateAccountException,
      WalletBackupException, AccountNotFoundException {

    IWalletAccount a0 = createAccounts().get(0);
    IWalletAccount a1 = createAccounts().get(1);
    IWalletAccount a2 = createAccounts().get(2);

    IWallet wallet = new Wallet(file);
    wallet.initialize("foo");
    wallet.addAccount(a0);
    wallet.addAccount(a1);
    wallet.addAccount(a2);
    wallet.removeAccount(a1);
    wallet.removeAccount(a2);
    wallet.addAccount(a1);

    IWalletStatus status = wallet.addAccount(a2);

    /* backup must contain account 0 and 1 */
    File backup = status.getBackupFile();

    wallet = new Wallet(backup);
    wallet.initialize("foo");
    assertArrayEquals(new IWalletAccount[] { a0, a1 }, wallet.getAccounts().toArray());
  }

}
