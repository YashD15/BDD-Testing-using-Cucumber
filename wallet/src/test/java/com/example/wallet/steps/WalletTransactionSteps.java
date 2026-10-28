
package com.example.wallet.steps;

import com.example.wallet.dto.CreateTransactionRequestDto;
import com.example.wallet.dto.CreateWalletRequestDto;
import com.example.wallet.dto.WalletTransactionResponseDto;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.service.WalletService;
import com.example.wallet.service.WalletTransactionService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WalletTransactionSteps {

  @Autowired
  private WalletTransactionService transactionService;

  @Autowired
  private WalletTestHelper walletTestHelper;

  @Autowired
  private WalletService walletService;

  private Exception walletCreationException;

  private Long userId;
  private WalletTransactionResponseDto transactionResponse;
  private Exception transactionException;
  private List<WalletTransactionResponseDto> transactionHistory;

  @Given("a wallet exists with a balance of {double} INR")
  public void aWalletExistsWithBalance(double initialBalance) {
    userId = 900001L;

    if (!walletTestHelper.walletExists(userId)) {
      walletTestHelper.createWalletWithBalance(
          BigDecimal.valueOf(initialBalance));
    }

    walletTestHelper.clearTransactions(userId);
    walletTestHelper.resetWallet(
        userId,
        BigDecimal.valueOf(initialBalance));

    transactionResponse = null;
    transactionException = null;
    transactionHistory = null;
  }

  @When("the user deposits {double} INR")
  public void theUserDeposits(double amount) {
    executeTransaction(TransactionType.CREDIT, amount);
  }

  @When("the user withdraws {double} INR")
  public void theUserWithdraws(double amount) {
    executeTransaction(TransactionType.DEBIT, amount);
  }

  private void executeTransaction(TransactionType type, double amount) {
    CreateTransactionRequestDto request = new CreateTransactionRequestDto();

    request.setUserId(userId);
    request.setTransactionType(type);
    request.setAmount(BigDecimal.valueOf(amount));
    request.setDescription("Cucumber integration test");

    try {
      transactionResponse = transactionService.createTransaction(request);
    } catch (Exception exception) {
      transactionException = exception;
    }
  }

  @Then("the wallet balance should be {double} INR")
  public void theWalletBalanceShouldBe(double expectedBalance) {
    assertEquals(
        0,
        walletTestHelper.getWalletBalance(userId)
            .compareTo(BigDecimal.valueOf(expectedBalance)),
        "Wallet balance does not match expected balance");

    assertNotNull(transactionResponse,
        "A successful transaction response was expected");
  }

  @Then("the transaction should be rejected")
  public void theTransactionShouldBeRejected() {
    assertNotNull(
        transactionException,
        "Expected the transaction to be rejected");
  }

  @Then("the wallet balance should remain {double} INR")
  public void theWalletBalanceShouldRemain(double expectedBalance) {
    assertEquals(
        0,
        walletTestHelper.getWalletBalance(userId)
            .compareTo(BigDecimal.valueOf(expectedBalance)),
        "Balance should remain unchanged after a rejected transaction");
  }

  @When("the user requests transaction history")
  public void theUserRequestsTransactionHistory() {
    transactionHistory = transactionService.getTransactionsByUserId(userId);
  }

  @Then("the transaction history should include a CREDIT of {double} INR")
  public void theTransactionHistoryShouldIncludeCredit(double amount) {
    assertNotNull(transactionHistory, "Transaction history should be returned");

    boolean transactionFound = transactionHistory.stream()
        .anyMatch(transaction -> transaction.getTransactionType() == TransactionType.CREDIT
            && transaction.getAmount().compareTo(
                BigDecimal.valueOf(amount)) == 0);

    assertTrue(transactionFound,
        "Expected CREDIT transaction of " + amount + " INR");
  }

  @Then("the transaction history should include a DEBIT of {double} INR")
  public void theTransactionHistoryShouldIncludeDebit(double amount) {
    assertNotNull(transactionHistory, "Transaction history should be returned");

    boolean transactionFound = transactionHistory.stream()
        .anyMatch(transaction -> transaction.getTransactionType() == TransactionType.DEBIT
            && transaction.getAmount()
                .compareTo(BigDecimal.valueOf(amount)) == 0);

    assertTrue(transactionFound,
        "Expected DEBIT transaction of " + amount + " INR");
  }

  @Then("the transaction history should not include a DEBIT of {double} INR")
  public void theTransactionHistoryShouldNotIncludeDebit(double amount) {
    assertNotNull(transactionHistory, "Transaction history should be returned");

    boolean transactionFound = transactionHistory.stream()
        .anyMatch(transaction -> transaction.getTransactionType() == TransactionType.DEBIT
            && transaction.getAmount()
                .compareTo(BigDecimal.valueOf(amount)) == 0);

    assertTrue(!transactionFound,
        "Unexpected DEBIT transaction of " + amount + " INR was recorded");
  }

  @Then("the latest transaction should be a DEBIT of {double} INR")
  public void theLatestTransactionShouldBeDebit(double amount) {
    assertNotNull(transactionHistory, "Transaction history should be returned");
    assertTrue(!transactionHistory.isEmpty(),
        "Transaction history should not be empty");

    WalletTransactionResponseDto latestTransaction = transactionHistory.get(0);

    assertEquals(TransactionType.DEBIT, latestTransaction.getTransactionType(),
        "The latest transaction should be a DEBIT");

    assertEquals(0, latestTransaction.getAmount()
        .compareTo(BigDecimal.valueOf(amount)),
        "The latest transaction amount does not match");
  }

  @Then("the transaction history should be empty")
  public void theTransactionHistoryShouldBeEmpty() {
    assertNotNull(transactionHistory, "Transaction history should be returned");
    assertTrue(transactionHistory.isEmpty(),
        "Expected no transactions in wallet history");
  }

  @Given("no wallet exists for the test user")
  public void noWalletExistsForTheTestUser() {
    userId = 900002L;
    transactionResponse = null;
    transactionException = null;
    transactionHistory = null;

    walletTestHelper.clearTransactions(userId);
    walletTestHelper.deleteWallet(userId);
  }

  @Then("no wallet should be created for the test user")
  public void noWalletShouldBeCreatedForTheTestUser() {
    assertTrue(!walletTestHelper.walletExists(userId),
        "A wallet should not be created automatically");
  }

  @When("the user creates a wallet with currency {string}")
  public void theUserCreatesAWalletWithCurrency(String currency) {
    CreateWalletRequestDto request = new CreateWalletRequestDto();
    request.setUserId(900003L);
    request.setCurrency(currency);

    walletCreationException = null;

    try {
      walletService.createWallet(request);
    } catch (RuntimeException exception) {
      walletCreationException = exception;
    }
  }

  @Then("wallet creation should be rejected")
  public void walletCreationShouldBeRejected() {
    assertNotNull(walletCreationException,
        "Expected wallet creation to be rejected");
  }

  @When("the user creates another wallet with currency {string} for the same user")
  public void theUserCreatesAnotherWalletWithCurrency(String currency) {
    CreateWalletRequestDto request = new CreateWalletRequestDto();
    request.setUserId(900003L);
    request.setCurrency(currency);

    walletCreationException = null;

    try {
      walletService.createWallet(request);
    } catch (Exception exception) {
      walletCreationException = exception;
    }
  }

}
