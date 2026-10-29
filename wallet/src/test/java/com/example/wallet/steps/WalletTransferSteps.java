
package com.example.wallet.steps;

import com.example.wallet.dto.TransferRequestDto;
import com.example.wallet.dto.TransferResponseDto;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.service.TransferService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WalletTransferSteps {

  private static final Long SENDER_ID = 900011L;
  private static final Long RECEIVER_ID = 900012L;

  @Autowired
  private TransferService transferService;

  @Autowired
  private WalletTestHelper helper;

  private TransferResponseDto transferResponse;
  private RuntimeException transferException;

  @Given("a sender wallet exists with a balance of {double} INR")
  public void aSenderWalletExists(double balance) {
    helper.prepareTransferWallet(
        SENDER_ID, BigDecimal.valueOf(balance));
    transferResponse = null;
    transferException = null;
  }

  @Given("a receiver wallet exists with a balance of {double} INR")
  public void aReceiverWalletExists(double balance) {
    helper.prepareTransferWallet(
        RECEIVER_ID, BigDecimal.valueOf(balance));
  }

  @Given("the receiver wallet does not exist")
  public void theReceiverWalletDoesNotExist() {
    helper.removeTransferWallet(RECEIVER_ID);
  }

  @When("the sender transfers {double} INR to the receiver")
  public void theSenderTransfersMoney(double amount) {
    executeTransfer(SENDER_ID, RECEIVER_ID, amount);
  }

  @When("the sender transfers {double} INR to themselves")
  public void theSenderTransfersMoneyToThemselves(double amount) {
    executeTransfer(SENDER_ID, SENDER_ID, amount);
  }

  private void executeTransfer(
      Long senderId, Long receiverId, double amount) {

    TransferRequestDto request = new TransferRequestDto();
    request.setSenderUserId(senderId);
    request.setReceiverUserId(receiverId);
    request.setAmount(BigDecimal.valueOf(amount));
    request.setDescription("Cucumber transfer test");

    try {
      transferResponse = transferService.transferMoney(request);
    } catch (RuntimeException exception) {
      transferException = exception;
    }
  }

  @Then("the transfer should be successful")
  public void theTransferShouldBeSuccessful() {
    assertNull(transferException,
        "Transfer failed: " + transferException);
    assertNotNull(transferResponse);
    assertEquals("COMPLETED", transferResponse.getStatus());
  }

  @Then("the transfer should be rejected")
  public void theTransferShouldBeRejected() {
    assertNotNull(transferException,
        "Expected the transfer to be rejected");
    assertNull(transferResponse);
  }

  @And("the sender wallet balance should be {double} INR")
  public void theSenderBalanceShouldBe(double expectedBalance) {
    assertEquals(
        0,
        BigDecimal.valueOf(expectedBalance).compareTo(
            helper.getTransferWalletBalance(SENDER_ID)),
        "Unexpected sender balance");
  }

  @And("the receiver wallet balance should be {double} INR")
  public void theReceiverBalanceShouldBe(double expectedBalance) {
    assertEquals(
        0,
        BigDecimal.valueOf(expectedBalance).compareTo(
            helper.getTransferWalletBalance(RECEIVER_ID)),
        "Unexpected receiver balance");
  }

  @And("the transfer should create a debit and a credit transaction")
  public void transferShouldCreateDebitAndCredit() {
    List<WalletTransaction> senderTransactions = helper.getTransferTransactions(SENDER_ID);

    List<WalletTransaction> receiverTransactions = helper.getTransferTransactions(RECEIVER_ID);

    assertTrue(senderTransactions.stream().anyMatch(
        tx -> tx.getTransactionType() == TransactionType.DEBIT), "Expected a debit transaction for the sender");

    assertTrue(receiverTransactions.stream().anyMatch(
        tx -> tx.getTransactionType() == TransactionType.CREDIT), "Expected a credit transaction for the receiver");
  }

  @And("the transfer should not create any transactions")
  public void transferShouldNotCreateTransactions() {
    assertTrue(
        helper.getTransferTransactions(SENDER_ID).isEmpty(),
        "Sender should have no transactions from this scenario");

    assertTrue(
        helper.getTransferTransactions(RECEIVER_ID).isEmpty(),
        "Receiver should have no transactions from this scenario");
  }
}
