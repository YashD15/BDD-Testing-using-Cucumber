# Day 4 — Cucumber BDD

## Goal

Integrate Cucumber into the Digital Wallet API project to implement Behaviour-Driven Development (BDD) tests using Gherkin scenarios and Spring Boot.

## Tasks Completed

- Added Cucumber dependencies to the Gradle build.
- Configured Cucumber to run with the JUnit Platform.
- Created a Spring Boot test configuration for Cucumber.
- Created a Cucumber test runner.
- Added Gherkin feature files for wallet and transaction behaviour.
- Implemented step definitions to execute scenarios against application services.
- Created a test helper for wallet setup, balance management, and cleanup.
- Configured test logging to make test results easier to read.
- Verified that the automated test suite passes.

## Technology Used

- Cucumber JVM
- Gherkin
- JUnit Platform
- Spring Boot Test
- Gradle

## How BDD Works in This Project

BDD describes application behaviour in a format that is easy to understand and test.

Cucumber feature files use three main keywords:

- `Given` — establishes the initial state.
- `When` — describes the action being performed.
- `Then` — verifies the expected result.

### Example Scenario

```gherkin
Scenario: Reject withdrawal when balance is insufficient
  Given a wallet exists with a balance of 100 INR
  When the user withdraws 200 INR
  Then the transaction should be rejected
  And the wallet balance should remain 100 INR
```

Cucumber connects these readable steps to Java step definitions, which exercise the application's services and verify the results.

## Scenarios Covered

The feature files cover behaviours including:

- Depositing and withdrawing money.
- Rejecting withdrawals when funds are insufficient.
- Validating zero and negative transaction amounts.
- Retrieving transaction history after deposits and withdrawals.
- Displaying the newest transaction first.
- Handling empty transaction history.
- Rejecting transactions for missing wallets.
- Rejecting unsupported wallet currencies.
- Preventing duplicate wallets.
- Verifying balances after multiple transactions.
- Testing different deposit and withdrawal amounts using Scenario Outlines.

## Test Configuration

The Cucumber test setup includes:

- `CucumberSpringConfiguration` — connects Cucumber with the Spring test context.
- `RunCucumberTest` — discovers and runs the feature files.
- `WalletTransactionSteps` — implements the Gherkin steps.
- `WalletTestHelper` — prepares and checks test data.
- `junit-platform.properties` — configures Cucumber execution and reporting.

A separate in-memory H2 database is used for automated tests so they do not modify the file-based development database.

## Running the Tests

Run all automated tests:

```powershell
.\gradlew.bat test
```

Run only the Cucumber suite:

```powershell
.\gradlew.bat test --tests "com.example.wallet.RunCucumberTest"
```

Run a clean test build:

```powershell
.\gradlew.bat clean test
```

## Why BDD Is Useful

- Business rules are expressed in readable scenarios.
- Expected behaviour is documented alongside automated tests.
- Scenarios help identify missing requirements.
- Regression tests help catch unintended changes.
- Scenario Outlines test multiple input values without duplicating scenarios.

## Key Learnings

- Writing Gherkin feature files.
- Connecting feature steps to Java methods.
- Integrating Cucumber with Spring Boot and JUnit Platform.
- Preparing and cleaning test data.
- Using Scenario Outlines for data-driven tests.
- Separating behaviour specifications from implementation details.
