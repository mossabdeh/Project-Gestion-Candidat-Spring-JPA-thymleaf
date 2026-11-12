# Candidate E2E coverage

The Candidate suite lives under `features/candidate/` and uses the same Playwright + playwright-bdd runner as the Admin suite.

Each scenario creates a unique Candidate through the real Admin creation form, clears the browser session, and logs in with the Candidate credentials. This isolates scenarios without permanent test credentials or test-only production endpoints.

| Area | Browser checks | Additional Java coverage |
| --- | --- | --- |
| Authentication | Candidate login, profile identity, logout, protected dashboard | Existing role-security tests |
| Navigation | Dashboard → competition information/results | Candidate dashboard UI tests |
| Results privacy | Newly created, unassigned Candidate sees no personal scores or decision | Published/pending and subject-order tests |
| Authorization | Candidate cannot access Admin directory | Role-access tests |
| Responsive layout | Navigation visible and no horizontal overflow at 375px | CSS/static asset smoke tests |

**Limitations:** CI uses a fresh H2 in-memory database. Browser tests do not create completed exam marking cycles. Published/admitted/not-admitted scenarios and multiple subject/correction combinations remain tested at the Java integration layer. Full browser publication lifecycle requires a controlled multi-role fixture/workflow and is tracked in issue #32.

Run from `e2e/` after setting `ADMIN_USERNAME` and `ADMIN_PASSWORD`:

```bash
npm test
```

Do not point these account-creating tests at production.
