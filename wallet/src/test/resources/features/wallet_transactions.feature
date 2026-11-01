Feature: Wallet transactions

  As a wallet user
  I want to deposit and withdraw money
  So that I can manage my wallet balance safely

  Scenario: Deposit money into a wallet
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits 200.00 INR
    Then the wallet balance should be 700.00 INR

  Scenario: Withdraw money from a wallet
    Given a wallet exists with a balance of 500.00 INR
    When the user withdraws 200.00 INR
    Then the wallet balance should be 300.00 INR

  Scenario: Reject withdrawal when balance is insufficient
    Given a wallet exists with a balance of 500.00 INR
    When the user withdraws 800.00 INR
    Then the transaction should be rejected
    And the wallet balance should remain 500.00 INR

  Scenario: View transaction history after a deposit
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits 200.00 INR
    When the user requests transaction history
    Then the transaction history should include a CREDIT of 200.00 INR

  Scenario: Reject a transaction with a zero amount
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits 0.00 INR
    Then the transaction should be rejected
    And the wallet balance should remain 500.00 INR

  Scenario: Reject a transaction with a negative amount
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits -100.00 INR
    Then the transaction should be rejected
    And the wallet balance should remain 500.00 INR

  Scenario: View transaction history after a withdrawal
    Given a wallet exists with a balance of 500.00 INR
    When the user withdraws 200.00 INR
    And the user requests transaction history
    Then the transaction history should include a DEBIT of 200.00 INR

  Scenario: Update balance correctly after multiple transactions
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits 200.00 INR
    And the user withdraws 100.00 INR
    Then the wallet balance should be 600.00 INR

  Scenario: Do not record a withdrawal that fails due to insufficient funds
    Given a wallet exists with a balance of 500.00 INR
    When the user withdraws 800.00 INR
    Then the transaction should be rejected
    And the wallet balance should remain 500.00 INR
    When the user requests transaction history
    Then the transaction history should not include a DEBIT of 800.00 INR

  Scenario: Show the newest transaction first in transaction history
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits 200.00 INR
    And the user withdraws 100.00 INR
    And the user requests transaction history
    Then the latest transaction should be a DEBIT of 100.00 INR

  Scenario: View transaction history when no transactions exist
    Given a wallet exists with a balance of 500.00 INR
    When the user requests transaction history
    Then the transaction history should be empty

  Scenario: Reject a deposit when the wallet does not exist
    Given no wallet exists for the test user
    When the user deposits 200.00 INR
    Then the transaction should be rejected
    And no wallet should be created for the test user

  Scenario: Reject wallet creation with an unsupported currency
    When the user creates a wallet with currency "USD"
    Then wallet creation should be rejected

  Scenario: Reject creating a second wallet for the same user
    When the user creates a wallet with currency "INR"
    And the user creates another wallet with currency "INR" for the same user
    Then wallet creation should be rejected

  Scenario Outline: Deposit different amounts into a wallet
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits <amount> INR
    Then the wallet balance should be <expectedBalance> INR

    Examples:
      | amount | expectedBalance |
      | 50.00  | 550.00          |
      | 100.00 | 600.00          |
      | 250.00 | 750.00          |

  Scenario Outline: Withdraw different amounts from a wallet
    Given a wallet exists with a balance of 1000.00 INR
    When the user withdraws <amount> INR
    Then the wallet balance should be <expectedBalance> INR

    Examples:
      | amount | expectedBalance |
      | 100.00 | 900.00          |
      | 250.00 | 750.00          |
      | 500.00 | 500.00          |

  Scenario: Successfully transfer money between two wallets
    Given a sender wallet exists with a balance of 1000 INR
    And a receiver wallet exists with a balance of 300 INR
    When the sender transfers 200 INR to the receiver
    Then the transfer should be successful
    And the sender wallet balance should be 800 INR
    And the receiver wallet balance should be 500 INR
    And the transfer should create a debit and a credit transaction

  Scenario: Reject transfer when sender has insufficient funds
    Given a sender wallet exists with a balance of 100 INR
    And a receiver wallet exists with a balance of 300 INR
    When the sender transfers 200 INR to the receiver
    Then the transfer should be rejected
    And the sender wallet balance should be 100 INR
    And the receiver wallet balance should be 300 INR
    And the transfer should not create any transactions

  Scenario: Reject transfer to the same user
    Given a sender wallet exists with a balance of 1000 INR
    When the sender transfers 200 INR to themselves
    Then the transfer should be rejected
    And the sender wallet balance should be 1000 INR

  Scenario: Reject transfer when the receiver wallet does not exist
    Given a sender wallet exists with a balance of 1000 INR
    And the receiver wallet does not exist
    When the sender transfers 200 INR to the receiver
    Then the transfer should be rejected
    And the sender wallet balance should be 1000 INR
    And the transfer should not create any transactions

  # Day 14: Wallet soft deletion

  Scenario: Successfully soft-delete an existing wallet
    Given a wallet exists with a balance of 500.00 INR
    When the user deletes their wallet
    Then the wallet should be marked as deleted
    And the wallet record should still exist in the database

  Scenario: Reject access to a deleted wallet
    Given a wallet exists with a balance of 500.00 INR
    When the user deletes their wallet
    And the user requests their wallet details
    Then the wallet should not be found

  Scenario: Reject a transaction against a deleted wallet
    Given a wallet exists with a balance of 500.00 INR
    When the user deletes their wallet
    And the user deposits 200.00 INR
    Then the transaction should be rejected
    And the stored wallet balance should remain 500.00 INR

  Scenario: Preserve transaction history after wallet deletion
    Given a wallet exists with a balance of 500.00 INR
    When the user deposits 200.00 INR
    And the user deletes their wallet
    Then the existing transaction history should be preserved

      # Day 14: Transfers involving soft-deleted wallets

  Scenario: Reject transfer from a soft-deleted sender wallet
    Given a sender wallet exists with a balance of 1000.00 INR
    And a receiver wallet exists with a balance of 300.00 INR
    When the sender wallet is soft-deleted
    And the sender transfers 200.00 INR to the receiver
    Then the transfer should be rejected
    And the stored sender wallet balance should remain 1000.00 INR
    And the receiver wallet balance should be 300.00 INR
    And the transfer should not create any transactions

  Scenario: Reject transfer to a soft-deleted receiver wallet
    Given a sender wallet exists with a balance of 1000.00 INR
    And a receiver wallet exists with a balance of 300.00 INR
    When the receiver wallet is soft-deleted
    And the sender transfers 200.00 INR to the receiver
    Then the transfer should be rejected
    And the sender wallet balance should be 1000.00 INR
    And the stored receiver wallet balance should remain 300.00 INR
    And the transfer should not create any transactions