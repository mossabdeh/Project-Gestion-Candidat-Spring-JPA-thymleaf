# Admin E2E acceptance tests (Playwright + Gherkin)

This is the black-box functional and UI test suite for the Spring Boot application. Gherkin contains requirements, step definitions implement them, and Playwright drives a real browser.

## Project layout

- `features/admin/`: executable business scenarios
- `steps/`: Gherkin step definitions and assertions
- `pages/`: reusable browser interactions
- `playwright.config.ts`: browser, server lifecycle and reporting
- `docs/admin-coverage.md`: planned future coverage (not executed)

## Prerequisites

Java 17, Node.js 22+, and a working Maven wrapper. H2 is in-memory; the test runner starts a new development Spring Boot instance unless one is already running locally.

Choose a **unique, private** dev admin password with at least 12 characters, different from the username.

On Linux/macOS:

```bash
export ADMIN_USERNAME=e2e-admin
export ADMIN_PASSWORD='choose-your-own-private-strong-password'
cd e2e
npm install
npx playwright install chromium
npm test
```

On Windows PowerShell:

```powershell
$env:ADMIN_USERNAME="e2e-admin"
$env:ADMIN_PASSWORD="choose-your-own-private-strong-password"
cd e2e
npm install
npx playwright install chromium
npm test
```

The runner starts Spring Boot with the `dev` profile on port 8082, using the application's existing H2 settings. If the application is already running, the runner reuses it locally; **ensure the running instance has the same admin credentials**.

To target another local test server: set `BASE_URL`. Never run these destructive-account tests against production.

## GitHub Actions

The workflow in `../.github/workflows/admin-bdd.yml` runs Java tests and the BDD browser suite on pull requests targeting `master` and pushes to `master`.

GitHub Actions uses Ubuntu, Java 17, Node.js 22, Chromium, and temporary generated admin credentials. It starts Spring Boot automatically and uploads the Playwright HTML report, traces/screenshots and `spring-boot.log` as the `admin-bdd-evidence` artifact.

There is no committed npm lockfile yet. The initial CI workflow uses `npm install` and uploads the generated `package-lock.json`; **commit that file in a follow-up to switch CI to reproducible `npm ci`**.

## Troubleshooting server startup failures

If Playwright says `Process from config.webServer was not able to start. Exit code: 1`, the Spring Boot process exited before tests started. This does **not** necessarily mean Chromium or WSL is broken. Inspect `e2e/spring-boot.log` for Maven, Java, or application errors. On GitHub Actions, open the failed job's `Show Spring Boot logs` step or download the `admin-bdd-evidence` artifact.

Check `java -version`, and run `./mvnw spring-boot:run` (Linux/macOS) or `..\\mvnw.cmd -B spring-boot:run -Dspring-boot.run.profiles=dev` from `e2e` (PowerShell) to isolate backend startup issues.

## Commands

- `npm test` — generate Playwright tests from feature files and run Chromium
- `npm run test:headed` — inspect a visible browser
- `npm run test:ui` — Playwright interactive UI
- `npm run report` — open the HTML report
- `npm run bdd:generate` — generate tests without executing

Failed tests retain screenshots and traces in `test-results/`. Reports and generated specs are gitignored. No credentials are committed.

## Conventions

- `@admin`, `@auth`, `@navigation` identify areas; `@smoke` identifies quick checks; `@P0` / `@P1` indicate priority
- Each scenario is independent and asserts an observable outcome
- Keep business behavior in `.feature`; browser interaction and assertions belong in `steps/` or `pages/`
- Prefer accessible selectors over brittle CSS selectors, and use uniquely named test records for CRUD cases
- Do not check in pending/undefined steps as if they were executable tests
