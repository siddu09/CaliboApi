# Calibo API Automation Framework

A production-ready Java API Automation Framework for Calibo Accelerate.

---

# Technology Stack

| Technology | Version |
|------------|----------|
| Java | 21 |
| Maven | Latest |
| TestNG | Latest |
| Rest Assured | Latest |
| Selenium | Latest (Login Only) |
| Jackson | Latest |
| JSON Simple | Latest |
| JsonPath | Latest |
| Apache Commons | Latest |
| Java Faker | Latest |

---

# Framework Architecture

```
calibo-api-automation
│
├── pom.xml
├── testng.xml
│
├── src
│
├── main
│   ├── java
│   │
│   ├── base
│   │      InitializeTestSuite.java
│   │
│   ├── common
│   │      Configuration.java
│   │      ConfigurationLoader.java
│   │      AuthCode.java
│   │      RequestSpecProvider.java
│   │
│   ├── constants
│   │      ApiEndpoints.java
│   │      FrameworkConstants.java
│   │
│   ├── helpers
│   │      UsersHelper.java
│   │      ProjectsHelper.java
│   │
│   ├── services
│   │      UserService.java
│   │      ProjectService.java
│   │      UserDataFactory.java
│   │      ProjectDataFactory.java
│   │      UserContext.java
│   │      ProjectContext.java
│   │
│   ├── validators
│   │      UserValidator.java
│   │      ProjectValidator.java
│   │
│   ├── utils
│   │      JsonUtils.java
│   │      RandomDataUtils.java
│   │      DropdownUtils.java
│   │
│   └── models
│
└── test
    ├── java
    │      UsersTests.java
    │      ProjectsTests.java
    │
    └── resources
           config.properties
           users.json
           project.json
```

---

# Framework Flow

```
@BeforeSuite

↓

ConfigurationLoader.load()

↓

AuthCode.login()

↓

Capture Access Token

↓

Capture Refresh Token

↓

RequestSpecProvider.initialize()

↓

API Automation Starts
```

---

# Design Principles

- Single Responsibility Principle (SRP)
- Open/Closed Principle (OCP)
- Reusable Components
- Separation of Concerns
- Service Layer Pattern
- Factory Pattern
- Context Pattern
- Utility Classes
- Centralized Configuration
- Shared RequestSpecification

---

# Folder Responsibilities

## base

Framework initialization.

Contains suite setup and teardown.

---

## common

Framework-wide reusable classes.

Examples:

- Configuration
- Authentication
- Request Specification

---

## constants

Framework constants.

Examples:

- API Endpoints
- Header Names
- Cookie Names

---

## helpers

Contains only REST API calls.

No business logic.

Example

```
createUser()

updateUser()

deleteUser()

searchUser()
```

---

## services

Contains business workflows.

Example

```
createUser()

↓

Prepare Request

↓

Call Helper

↓

Validate Response

↓

Store Runtime Data
```

---

## validators

Contains assertions and response validations.

Example

```
validateUserCreated()

validateUserUpdated()

validateProjectCreated()
```

---

## utils

Reusable utility classes.

Examples

- JsonUtils
- DropdownUtils
- RandomDataUtils

---

# Authentication Flow

Authentication is performed only once.

```
@BeforeSuite

↓

Selenium Login

↓

Capture Cookies

↓

Access Token

↓

Refresh Token

↓

Close Browser
```

Every API request reuses the same token.

No Selenium execution during API tests.

---

# Request Specification

RequestSpecification is created once.

```
RequestSpecProvider.initialize();
```

Every helper uses

```java
given()
    .spec(RequestSpecProvider.get())
```

No duplicated headers.

No duplicated base URL.

No duplicated Authorization header.

---

# Test Data

All request payloads are stored in JSON files.

Example

```
users.json

project.json
```

Dynamic values are injected during execution.

Examples

- Random Email
- Random Name
- Organization
- Business Group
- Skills
- Roles

---

# Runtime Context

Each module maintains runtime values.

Example

```
Created User ID

Created Project ID

Created Email

Created Role
```

These values are shared between dependent test scenarios.

---

# Service Layer

Tests never call Helpers directly.

```
UsersTests

↓

UserService

↓

UsersHelper

↓

REST API
```

---

# Example Test

```java
@Test
public void createUser() {

    userService.createUser();

}

@Test(dependsOnMethods = "createUser")
public void updateUser() {

    userService.updateUser();

}

@Test(dependsOnMethods = "updateUser")
public void deleteUser() {

    userService.deleteUser();

}
```

---

# Running Tests

Run complete suite

```
mvn clean test
```

Run specific TestNG suite

```
mvn test -DsuiteXmlFile=testng.xml
```

Run a single test class

```
mvn test -Dtest=UsersTests
```

---

# Configuration

Update

```
src/test/resources/config.properties
```

Configure

- Base URL
- Login URL
- Username
- Password
- Tenant
- Browser
- Headless Mode

---

# Coding Standards

- Java 21
- One class = One responsibility
- No duplicated code
- No hardcoded values
- API endpoints centralized
- Shared RequestSpecification
- Shared configuration
- JSON-driven test data
- Reusable utilities
- Business logic inside Services
- REST calls inside Helpers
- Assertions inside Validators

---

# Future Enhancements

- Parallel Execution
- Token Refresh
- Retry Analyzer
- API Request Logging
- Response Logging
- CI/CD Integration
- Docker Support
- Environment Profiles
- Extent Reports (Optional)
- Allure Reports (Optional)

---

# Maintainers

Automation Engineering Team

Calibo Accelerate API Automation Framework