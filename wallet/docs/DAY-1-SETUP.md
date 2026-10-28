# Day 1 — Project Setup

## Goal
Set up the foundation for the Digital Wallet API using Spring Boot and Gradle, configure a persistent database, and verify that the application starts successfully.

## Tasks Completed

- Created a Spring Boot project using Gradle.
- Configured the required dependencies.
- Created a health-check endpoint.
- Configured file-based H2 database storage.
- Added a database health-check endpoint.
- Updated `.gitignore` to exclude generated files and local database files.
- Verified the project using Gradle tests and build commands.

## Technology Used

- Java
- Spring Boot
- Gradle
- H2 Database
- Spring Data JPA

## Database Configuration

The application uses a file-based H2 database stored inside the project directory.

Database URL:

`jdbc:h2:file:./folderName/dbName`

This allows database data to persist between application restarts, unlike an in-memory database.

The H2 console is enabled at:

`/h2-console`

## Health Checks

### Application Health

Endpoint: `GET /api/health`

Expected response:

```json
{
  "status": "UP"
}
```

## Verification

The following commands completed successfully:

```bash
./gradlew test
./gradlew build
```

On Windows, use:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

## Key Learnings

- Understanding the basic structure of a Spring Boot application.
- Using Gradle to build and test a Java project.
- Configuring a persistent H2 database.
- Creating health-check endpoints.
- Excluding generated files and local database data through `.gitignore`.
