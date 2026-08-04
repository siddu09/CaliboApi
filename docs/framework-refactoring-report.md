# Framework Refactoring Report

## Baseline

- Java files: 25 production and 4 test classes
- Source/config size: 3,771 lines
- Data-ingestion baseline: 1 passed, 0 failed, 52 API transactions
- Portfolio/product/feature baseline: 1 passed, 0 failed

## Findings and planned fixes

| Area | Issue | Fix |
|---|---|---|
| Suite | Valid test modules were omitted from `testng.xml` | Register all valid modules; keep only the proven duplicate `ProductTests` removed |
| Configuration | Credentials are stored in `config.properties` | Resolve secrets from system properties/environment variables and leave repository values blank |
| Configuration | Data-ingestion log uses a developer-specific absolute path | Resolve a portable relative path |
| HTTP | Rest Assured request construction is repeated | Introduce a small shared request helper without hiding endpoint intent |
| Parallelism | User runtime data is mutable static state | Move it into an instance-scoped context owned by each test flow |
| Tests | User update test is disabled | Fix its request/validation contract or report the external blocker; do not silently disable it |
| Services | User workflow contains excessive logging and duplicated response handling | Keep concise activity logs and centralize status validation |
| Validators | User validator contains unused methods and accepts malformed responses | Retain only exercised assertions and fail with response details |
| Waiting | Fixed sleeps occur in authentication and polling | Prefer explicit waits for UI state and a reusable interrupt-safe polling delay |
| Lifecycle | Logout is an empty method | Remove the misleading call/method or implement a real logout contract |
| Thread safety | Shared request/logging and dropdown caches are global | Isolate mutable execution state and synchronize/lazily scope shared caches |

## Completed changes

- Removed duplicate `ProductTests` and its unused service entry point.
- Added product cleanup to the complete portfolio/product/feature flow.
- Restored all valid test modules to the default TestNG suite.
- Replaced the machine-specific data-ingestion reference-log path with portable resolution.
- Replaced static user IDs/emails with instance-scoped `UserContext` state.
- Enabled and repaired the user-update test instead of hiding it.
- Corrected array-root dropdown parsing and JSON Simple conversion.
- Reduced user service logging and removed unused client/validator methods.
- Removed repository credentials; authentication now reads JVM properties or environment variables.
- Stopped printing access-token fragments.
- Made request-spec creation defensive and shared cache/lifecycle operations synchronized.
- Removed unused endpoints, constants, configuration fields, and Maven dependencies.
- Removed the misleading empty logout operation.

## Verification log

| Verification | Result |
|---|---|
| Portfolio/product/feature test | 1 passed, 0 failed; product DELETE returned 204 |
| Data-ingestion baseline | 1 passed, 0 failed; 52 transactions; workflow completed |
| Data-ingestion after refactor | 1 passed, 0 failed; 52 transactions; workflow completed |
| Users after refactor | 6 passed, 0 failed, 0 skipped; update and cleanup passed |
| Final production compile | Passed |
| Final test-source compile | Passed |
| Populated credential scan | Passed (none found) |
| Disabled-test scan | Passed (none found) |

The final live all-module suite requires `CALIBO_USERNAME` and `CALIBO_PASSWORD` in
the execution environment. Those variables were intentionally not persisted or
reconstructed after credential removal. The deployment-stage flow also depends on
configured external repositories, pipelines, and Kubernetes infrastructure, so its
runtime result must be reported separately from compilation.

## Size

- Baseline source/config size: 3,771 lines
- Final source/config size: 3,350 lines
- Net reduction: 421 lines (11.2%)

## Modified areas

- Suite: `testng.xml`
- Shared: `BaseTest`, `AuthCode`, `RequestSpecProvider`, `Config`, `Constants`, `ApiEndpoints`
- Users: test, API client, service, request builder, validator, context, JSON data
- Portfolio/product/feature: API client, service, request builder, test
- Data ingestion: service and setup JSON
- Build/docs: `pom.xml`, `README.md`, this report
