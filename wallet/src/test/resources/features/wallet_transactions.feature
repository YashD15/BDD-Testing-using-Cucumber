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
