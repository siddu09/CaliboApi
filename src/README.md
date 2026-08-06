# Calibo API Automation Framework

A production-ready Java API Automation Framework for **Calibo Accelerate**, built on
**Rest Assured + TestNG**, with a one-time **Playwright** login used only to capture the
Bearer token (no browser usage during actual API test execution).

---

## Technology Stack

| Technology              | Version                  |
|-------------------------|---------------------------|
| Java                    | 21                        |
| Maven                   | 3.x                       |
| TestNG                  | 7.11.0                    |
| Rest Assured            | 5.5.5                     |
| Playwright (login only) | 1.44.0                    |
| JSON Simple             | 1.1.1                     |

---

## Folder Structure

> **Note:** The Maven project root is the `src/` folder itself (`pom.xml` lives inside
> `src/`, not at the repository root). Main sources and test sources are **not**
> separated using the standard `src/main/java` / `src/test/java` Maven layout — all
> production and test code live directly under `src/`, configured explicitly via the
> `<sourceDirectory>` / `<testSourceDirectory>` elements in `pom.xml`.

```
CaliboAPI/
│
├
│
└── src/                          ← Maven project root (contains pom.xml)
    │
    ├── pom.xml                   Maven build configuration
    ├── testng.xml                TestNG suite definition
    ├── README.md                 This file
    │
    ├── api/                      REST API client layer (raw HTTP calls only)
    │   └── UserApiClient.java
    │
    ├── base/                     Suite lifecycle (setup / teardown)
    │   └── BaseTest.java
    │
    ├── common/                   Framework-wide reusable classes
    │   ├── AuthCode.java             Playwright login + Bearer token capture
    │   └── RequestSpecProvider.java  Shared RestAssured RequestSpecification
    │
    ├── config/                   Centralized configuration & constants
    │   ├── Config.java               Loads config.properties, exposes static fields
    │   └── Constants.java            API endpoints, header names, file paths
    │
    ├── java/                     Test classes (package `tests`)
    │   └── PortfolioProductFeatureTests.java
    │
    ├── services/                 Business workflows (orchestration layer)
    │   ├── UserRequestBuilder.java   Builds/populates JSON request payloads + holds
    │   │                             runtime data shared across tests (userId, email...)
    │   └── UserService.java          Prepares request → calls API → validates → stores state
    │
    ├── utils/                    Reusable utility classes
    │   ├── DropdownUtils.java        Fetches/caches dropdown & lookup field values
    │   └── JsonUtils.java            Reads JSON files from disk
    │
    ├── validators/               Response assertions
    │   └── UserValidator.java
    │
    ├── resources/                Test data & configuration
    │   ├── config.properties         Environment, credentials, tenant, timeouts...
    │   └── users.json                 Request payload template(s)
    │
    └── target/                   Maven build output (generated, ignored by VCS)
```

---

## Framework Flow

```
@BeforeSuite (BaseTest)
        │
        ▼
Config.load()                     → reads resources/config.properties
        │
        ▼
AuthCode.login()                  → Playwright login + network request listener
        │
        ▼
Capture Bearer Access Token       → from network traffic, browser closed after
        │
        ▼
RequestSpecProvider.initialize()  → builds shared RequestSpecification
        │                            (base URL, Authorization header, Tenant-Id header)
        ▼
API Tests Execute                 → Tests → Services → API Client → REST API
```

Authentication happens **once per suite**. Every subsequent API call reuses the same
`RequestSpecification`/token — no duplicated headers, no duplicated base URL, and no
browser execution during the actual API test run.

---

## Design Principles

- Single Responsibility Principle (SRP)
- Separation of Concerns (API client vs. Service vs. Validator)
- Service Layer / Business Workflow pattern
- Shared `RequestSpecification` (built once, reused everywhere)
- Centralized configuration (`Config`) and constants (`Constants`)
- JSON-driven, dynamic test data
- Reusable utility classes

---

## Layer Responsibilities

| Layer          | Responsibility                                                                 |
|----------------|---------------------------------------------------------------------------------|
| `base`         | Suite-level setup (`@BeforeSuite`) and teardown (`@AfterSuite`)                 |
| `common`       | Login/token capture, shared `RequestSpecification`                             |
| `config`       | Loading `config.properties`, centralized endpoints/constants                    |
| `api`          | Raw REST calls only — **no** assertions, **no** business logic                  |
| `services`     | Business workflow: build request → call API client → validate → store context |
| `validators`   | Status code / payload / business-rule assertions                                |
| `utils`        | Generic helpers (JSON file reading, dropdown/lookup caching, random data)       |
| `java` (tests) | TestNG test classes, orchestrate calls to `services` only                       |
| `resources`    | `config.properties` and JSON request payload templates                         |

### Example call chain

```
PortfolioProductFeatureTests → PortfolioProductFeatureService
                                      │
                                      ├──→ PortfolioProductFeatureApiClient
                                      ├──→ PortfolioProductFeatureValidator
                                      └──→ PortfolioProductFeatureRequestBuilder
```

---

## Test Data & Runtime Context

- Request payloads are stored as JSON templates in `resources/` (e.g. `users.json`).
- `UserRequestBuilder` injects dynamic values at runtime (random email, dropdown field
  values fetched via `DropdownUtils`, etc.) and also holds values produced by one test
  that are consumed by dependent tests (e.g. `userId` created in `createUser()` is
  reused by `assignRole()` / `deleteUser()`).

---

## Configuration

Edit `src/resources/config.properties`:

```properties
environment=QA
base.url=https://accelerate-qa.calibo.com
login.url=https://accelerate-qa.calibo.com

username=
password=

tenant.name=Automation
tenant.id=Tenant296388

browser=chrome
headless=false

explicit.wait=60
page.load.timeout=120

retry.count=2
retry.interval=3000

console.logs=true
capture.request=true
capture.response=true

parallel=false
thread.count=1
```

Credentials must be supplied at runtime and are never stored in the repository:

```bash
export CALIBO_USERNAME='<api-user-email>'
export CALIBO_PASSWORD='<api-user-password>'
```

The equivalent JVM properties are `-Dcalibo.username=...` and
`-Dcalibo.password=...`.

Maven profiles (`qa`, `stage`, `local`) set the `environment` and `headless`
system properties consumed by the suite; the actual base URL / credentials still come
from `config.properties`.

---

## Maven Commands

All commands must be run **from the `src/` directory** (the Maven project root):

```bash
cd src
```

| Purpose                                   | Command                                        |
|--------------------------------------------|-------------------------------------------------|
| Clean build output                        | `mvn clean`                                     |
| Compile the project                       | `mvn clean compile`                             |
| Run the full test suite (`testng.xml`)    | `mvn clean test`                                |
| Run tests with a specific suite file      | `mvn test -DsuiteXmlFile=testng.xml`            |
| Run the portfolio/product/feature flow    | `mvn test`                                      |
| Run its single test method                | `mvn test -Dtest=PortfolioProductFeatureTests#createPortfolioProductAndFeature` |
| Run against the `qa` profile               | `mvn clean test -Pqa`                           |
| Run against the `stage` profile            | `mvn clean test -Pstage`                        |
| Run against the `local` profile (headed)   | `mvn clean test -Plocal`                        |
| Skip tests during build                    | `mvn clean install -DskipTests`                 |
| Package the project                        | `mvn clean package`                             |

---

## Adding a New Module (e.g. Projects)

1. Add endpoints to `config/Constants.java`.
2. Create `api/ProjectApiClient.java` (raw REST calls only).
3. Create `services/ProjectService.java`, `ProjectRequestBuilder.java`, `ProjectContext.java`.
4. Create `validators/ProjectValidator.java`.
5. Add a JSON template under `resources/` (e.g. `project.json`).
6. Create `java/ProjectsTests.java` extending `base.BaseTest`.
7. Register the test class in `testng.xml`.

---

## Coding Standards

- Java 21
- One class = one responsibility
- No duplicated code or hardcoded values
- API endpoints centralized in `Constants`
- Shared `RequestSpecification` — never build a new one per test
- Business logic lives in `services`, REST calls live in `api`, assertions live in `validators`
- Test data is JSON-driven, not hardcoded in test classes

---

## Future Enhancements

- Parallel execution
- Automatic token refresh
- TestNG Retry Analyzer
- Structured request/response logging
- CI/CD integration
- Environment-specific secrets management
- Extent / Allure reporting

---

## Maintainers

Automation Engineering Team — Calibo Accelerate API Automation Framework
