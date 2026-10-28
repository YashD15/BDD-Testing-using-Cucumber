# 💳 Digital Wallet API

A RESTful Digital Wallet API built using **Java, Spring Boot, Gradle, H2 Database, and Cucumber BDD**.

This project is a hands-on learning journey into backend development and Behaviour-Driven Development (BDD). It implements wallet management, deposits, withdrawals, transaction history, validation, and automated testing.

The primary focus is on using **Cucumber BDD** to describe expected application behaviour in readable language and verify that the implementation behaves correctly.

---

## 🎯 Project Objectives

- Learn REST API development using Spring Boot.
- Understand layered architecture, DTOs, repositories, services, and controllers.
- Implement wallet operations with validation and error handling.
- Work with a file-based H2 database.
- Learn Behaviour-Driven Development using Cucumber and Gherkin.
- Write readable, maintainable automated tests for successful and unsuccessful transactions.
- Understand how BDD complements unit and controller tests.

## ✨ Features

### Wallet Management
- Create a wallet for a user.
- Prevent duplicate active wallets for the same user.
- Retrieve a wallet using its user ID.
- Initialize wallets with a zero balance.
- Support INR currency in the current implementation.
- Validate wallet creation requests.

### Wallet Transactions
- Deposit money into a wallet.
- Withdraw money from a wallet.
- Reject withdrawals when funds are insufficient.
- Update wallet balances after successful transactions.
- Record transaction details, including transaction type and resulting balance.
- Retrieve transaction history.
- Reject zero and negative transaction amounts.
- Ensure failed withdrawals do not change the balance or create successful debit records.

### Automated Testing
- Unit tests for wallet and transaction services.
- Controller tests for HTTP responses and request validation.
- Cucumber BDD scenarios for wallet transaction behaviour.
- Scenario Outlines for testing multiple input values.
- Coverage for invalid amounts, insufficient funds, missing wallets, duplicate wallets, and unsupported currencies.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Programming language |
| Spring Boot | Backend application framework |
| Spring Web MVC | REST API development |
| Spring Data JPA | Database persistence |
| H2 Database | Relational database |
| Gradle | Build and dependency management |
| Lombok | Reducing Java boilerplate |
| Jakarta Bean Validation | Input validation |
| JUnit | Automated testing |
| Mockito | Mocking dependencies |
| Cucumber | Behaviour-Driven Development |
| Gherkin | Readable behaviour specifications |

---

## 🥒 Understanding Cucumber BDD

### What is BDD?

**Behaviour-Driven Development (BDD)** is an approach to software development where expected behaviour is described through concrete examples from a user's or stakeholder's perspective.

Instead of focusing only on individual methods, BDD focuses on what the application should do in a particular situation.

For example:

> Given a wallet has ₹500, when the user deposits ₹200, then the wallet balance should become ₹700.

This example describes the expected behaviour without requiring knowledge of the Java implementation.

### What is Cucumber?

Cucumber is a testing framework that executes behaviour specifications written in **Gherkin**, a readable language using keywords such as:

- `Feature` — describes a capability.
- `Scenario` — describes an example of expected behaviour.
- `Given` — establishes the initial state.
- `When` — describes an action.
- `Then` — checks the expected outcome.
- `And` — adds another step.
- `Scenario Outline` — runs a scenario template with multiple input sets.
- `Examples` — provides data for a Scenario Outline.

### How Cucumber works in this project

Our Cucumber tests follow this flow:

```text
Gherkin Feature File
        |
        v
Cucumber Test Runner
        |
        v
Java Step Definitions
        |
        v
Wallet / Transaction Service
        |
        v
Repositories and H2 Test Database
        |
        v
Assertions verify expected behaviour
```

For example, the Gherkin step:

```gherkin
When the user deposits 200.00 INR
```

is connected to a Java step definition:

```java
@When("the user deposits {double} INR")
public void theUserDeposits(double amount) {
    executeTransaction(TransactionType.CREDIT, amount);
}
```

The step definition calls the transaction service, and subsequent assertions verify the resulting balance or transaction history.

The current Cucumber scenarios exercise the service layer against the test database rather than sending HTTP requests through the controllers.

---

## 💡 Why Use BDD for a Wallet API?

Financial operations must behave correctly in both success and failure cases. BDD makes these expectations explicit and easy to review.

| Scenario | Expected behaviour |
|---|---|
| Deposit money | Balance increases by the deposited amount. |
| Withdraw money | Balance decreases by the withdrawn amount. |
| Insufficient funds | Withdrawal is rejected and the balance remains unchanged. |
| Invalid amount | Zero and negative transaction amounts are rejected. |
| Transaction history | Successful transactions can be retrieved. |
| Failed withdrawal | No successful debit transaction is recorded. |
| Missing wallet | A transaction is rejected when the wallet does not exist. |
| Duplicate wallet | A second active wallet for the same user is rejected. |
| Unsupported currency | Wallet creation with a currency other than INR is rejected. |
| Multiple transactions | Consecutive operations produce the expected final balance. |
| History ordering | The newest transaction appears first. |
| Scenario Outline | Multiple amounts are tested using one reusable scenario template. |

### BDD versus unit testing

BDD complements other types of automated tests.

- **Unit tests** verify individual pieces of logic.
- **Controller tests** verify HTTP request handling and responses.
- **Cucumber BDD tests** describe and verify application behaviour using readable scenarios.

Using all three approaches provides broader coverage than relying on any single testing technique.

---

## 📝 Example Gherkin Scenarios

### Successful deposit

```gherkin
Scenario: Deposit money into a wallet
  Given a wallet exists with a balance of 500.00 INR
  When the user deposits 200.00 INR
  Then the wallet balance should be 700.00 INR
```

### Reject insufficient funds

```gherkin
Scenario: Reject withdrawal when balance is insufficient
  Given a wallet exists with a balance of 500.00 INR
  When the user withdraws 800.00 INR
  Then the transaction should be rejected
  And the wallet balance should remain 500.00 INR
```

These scenarios document both the expected result and the conditions under which a transaction must fail.

### Scenario Outline

```gherkin
Scenario Outline: Deposit different amounts into a wallet
  Given a wallet exists with a balance of 500.00 INR
  When the user deposits <amount> INR
  Then the wallet balance should be <expectedBalance> INR

  Examples:
    | amount | expectedBalance |
    | 50.00  | 550.00          |
    | 100.00 | 600.00          |
    | 250.00 | 750.00          |
```

Cucumber executes this scenario once for each row in the `Examples` table. This avoids duplicating nearly identical scenarios and makes it easy to extend test coverage with new values.

---

## ⚙️ Running Automated Tests

Run these commands from the project root.

### Run all tests

**Windows:**

```powershell
.\gradlew.bat test
```

**macOS / Linux:**

```bash
./gradlew test
```

Runs the configured unit tests, controller tests, and Cucumber test suite.

### Run only Cucumber BDD tests

**Windows:**

```powershell
.\gradlew.bat test --tests "com.example.wallet.RunCucumberTest"
```

**macOS / Linux:**

```bash
./gradlew test --tests "com.example.wallet.RunCucumberTest"
```

Runs the Cucumber suite through `RunCucumberTest`.

### Clean and run the full test suite

**Windows:**

```powershell
.\gradlew.bat clean test
```

**macOS / Linux:**

```bash
./gradlew clean test
```

Cleans previous build outputs before running all tests.

---