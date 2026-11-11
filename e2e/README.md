# Admin E2E acceptance tests (Playwright + Gherkin)

This is the black-box functional and UI test suite for the Spring Boot application. Gherkin contains requirements, step definitions implement them, and Playwright drives a real browser.

## Project layout

- `features/admin/`: executable business scenarios
- `steps/`: Gherkin step definitions and assertions
- `pages/`: reusable browser interactions
- `playwright.config.ts`: browser, server lifecycle and reporting
- `docs/admin-coverage.md`: planned future coverage (not executed)

## Prerequisites

Java 17, Node.js 20+, and a working Maven wrapper. H2 is in-memory; the test runner starts a new development Spring Boot instance unless one is already running locally.

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

## Next

After the local BDD smoke suite is stable, add a dedicated GitHub Actions workflow with a committed lockfile (`npm ci`), Java 17 / Node setup, Chromium installation, isolated dev credentials and report artifacts.
