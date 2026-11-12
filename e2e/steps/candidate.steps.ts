import { randomUUID } from 'node:crypto';
import { expect, type Page } from '@playwright/test';
import { createBdd } from 'playwright-bdd';
import { AdminLoginPage } from '../pages/AdminLoginPage';

const { Given, When, Then } = createBdd();

type CandidateAccount = { username: string; password: string; nom: string; prenom: string; email: string };
const accounts = new WeakMap<Page, CandidateAccount>();

function adminCredentials() {
  const username = process.env.ADMIN_USERNAME;
  const password = process.env.ADMIN_PASSWORD;
  if (!username || !password) {
    throw new Error('ADMIN_USERNAME and ADMIN_PASSWORD are required for the Candidate BDD suite.');
  }
  return { username, password };
}

async function createCandidate(page: Page): Promise<CandidateAccount> {
  const suffix = randomUUID().replace(/-/g, '').slice(0, 12);
  const candidate: CandidateAccount = {
    username: `candidate-${suffix}`,
    password: `Candidate-E2E-${suffix}-2026`,
    nom: `Test${suffix}`,
    prenom: 'Browser',
    email: `candidate-${suffix}@example.test`,
  };

  const login = new AdminLoginPage(page);
  await login.open();
  const admin = adminCredentials();
  await login.signIn(admin.username, admin.password);
  await expect(page).toHaveURL(/\/adminPage$/);
  await page.goto('/candidat/CandidatAdd');
  await expect(page.getByRole('heading', { name: 'Add Candidat' })).toBeVisible();
  await page.locator('input[name="nom"]').fill(candidate.nom);
  await page.locator('input[name="prenom"]').fill(candidate.prenom);
  await page.locator('input[name="username"]').fill(candidate.username);
  await page.locator('input[name="email"]').fill(candidate.email);
  await page.locator('input[name="dateNaissance"]').fill('2000-01-01');
  await page.locator('input[name="password"]').fill(candidate.password);
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page).toHaveURL(/\/candidat\/getCandidats(?:\?.*)?$/);
  // A browser test must use the real login flow, not inject an auth session.
  await page.context().clearCookies();
  accounts.set(page, candidate);
  return candidate;
}

async function signInCandidate(page: Page) {
  const account = accounts.get(page) ?? await createCandidate(page);
  const login = new AdminLoginPage(page);
  await login.open();
  await login.signIn(account.username, account.password);
  await expect(page).toHaveURL(/\/candidatPage$/);
}

Given('a browser-test Candidate account is created by an Admin', async ({ page }) => {
  await createCandidate(page);
});

Given('I am signed in as a browser-test Candidate', async ({ page }) => {
  await signInCandidate(page);
});

When('the browser-test Candidate signs in', async ({ page }) => {
  await signInCandidate(page);
});

Then('the Candidate dashboard identifies the signed-in account', async ({ page }) => {
  const account = accounts.get(page);
  if (!account) throw new Error('Candidate fixture was not created');
  await expect(page.getByRole('heading', { name: `Welcome, ${account.prenom}.` })).toBeVisible();
  await expect(page.locator('.profile-details')).toContainText(account.username);
  await expect(page.locator('.profile-details')).toContainText(account.email);
});

Then('the Candidate navigation offers competition information and my results', async ({ page }) => {
  const nav = page.getByRole('navigation', { name: 'Main navigation' });
  await expect(nav.getByRole('link', { name: 'Competition details' })).toBeVisible();
  await expect(nav.getByRole('link', { name: 'My results' })).toBeVisible();
});

When('I open competition information from the Candidate navigation', async ({ page }) => {
  await page.getByRole('navigation', { name: 'Main navigation' })
    .getByRole('link', { name: 'Competition details' }).click();
});

Then('I see the Candidate competition information page', async ({ page }) => {
  await expect(page).toHaveURL(/\/candidat\/getConcourInfo$/);
  await expect(page.getByRole('heading', { name: 'Competition details', exact: true })).toBeVisible();
  // A fresh CI H2 database has no competition; on reused local test servers
  // it can instead display the latest competition. Both render within the portal.
  await expect(page.locator('#candidate-main')).toBeVisible();
});

When('I open my Candidate results', async ({ page }) => {
  await page.getByRole('navigation', { name: 'Main navigation' })
    .getByRole('link', { name: 'My results' }).click();
});

Then('the results page does not reveal grades or an admission decision', async ({ page }) => {
  await expect(page).toHaveURL(/\/candidat\/ResultatCandidat$/);
  await expect(page.getByRole('heading', { name: 'My results', exact: true })).toBeVisible();
  // A new Candidate has no anonymization code, so no published individual
  // scores may appear, even when an existing local server has a competition.
  await expect(page.locator('.results-panel')).toHaveCount(0);
  await expect(page.locator('.empty-panel[role="status"]')).toBeVisible();
});

When('I request the Admin account directory', async ({ page }) => {
  await page.goto('/admin/getAdmins');
});

Then('access to the Admin account directory is denied', async ({ page }) => {
  // Spring Security can forward to /notAuthorized while keeping the requested URL.
  // Assert the protected management UI is absent, regardless of redirect or forward.
  await expect(page.locator('table')).toHaveCount(0);
  await expect(page.getByRole('heading', { name: 'Admin Accounts', exact: true })).toHaveCount(0);
});

When('I sign out of the Candidate portal', async ({ page }) => {
  await page.getByRole('button', { name: 'Sign out' }).click();
});

Then('the Candidate session is closed', async ({ page }) => {
  await expect(page).toHaveURL(/\/login\?logout(?:[=&]|$)/);
});

Then('opening the Candidate dashboard redirects to login', async ({ page }) => {
  await page.goto('/candidatPage');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
});

When('I set the Candidate browser to a narrow mobile viewport', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 });
});

Then('the Candidate navigation and results link remain visible', async ({ page }) => {
  const nav = page.getByRole('navigation', { name: 'Main navigation' });
  await expect(nav.getByRole('link', { name: 'My results' })).toBeVisible();
  await expect(page.getByRole('heading', { name: /Welcome,/ })).toBeVisible();
});

Then('the Candidate workspace has no horizontal overflow', async ({ page }) => {
  const overflow = await page.evaluate(() =>
    document.documentElement.scrollWidth > document.documentElement.clientWidth + 1
  );
  expect(overflow).toBe(false);
});
