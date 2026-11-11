import { randomUUID } from 'node:crypto';
import { expect, type Page, type Browser } from '@playwright/test';
import { createBdd } from 'playwright-bdd';
import { AdminLoginPage } from '../pages/AdminLoginPage';
import { AdminAccountsPage, type NewAdmin } from '../pages/AdminAccountsPage';

const { Given, When, Then } = createBdd();

type ScenarioState = {
  account?: NewAdmin;
  enteredUsername?: string;
  edited?: { nom: string; prenom: string; email: string };
};
const scenarios = new WeakMap<Page, ScenarioState>();
function state(page: Page) {
  let value = scenarios.get(page);
  if (!value) {
    value = {};
    scenarios.set(page, value);
  }
  return value;
}

function uniqueAdmin(): NewAdmin {
  const suffix = randomUUID().replace(/-/g, '').slice(0, 12);
  return {
    username: `qa-${suffix}`,
    password: `Private-QA-${suffix}-2026`,
    nom: `Qa${suffix}`,
    prenom: 'Browser',
    email: `qa-${suffix}@example.test`,
  };
}

async function createAccount(page: Page) {
  const account = uniqueAdmin();
  await new AdminAccountsPage(page).create(account);
  state(page).account = account;
}

function getAccount(page: Page): NewAdmin {
  const account = state(page).account;
  if (!account) throw new Error('Scenario must create an Admin account first');
  return account;
}

async function checkLogin(browser: Browser, account: NewAdmin, valid: boolean) {
  const context = await browser.newContext();
  try {
    const loginPage = await context.newPage();
    const login = new AdminLoginPage(loginPage);
    await login.open();
    await login.signIn(account.username, account.password);
    if (valid) {
      await expect(loginPage).toHaveURL(/\/adminPage$/);
    } else {
      await expect(loginPage).toHaveURL(/\/login\?error(?:[=&]|$)/);
    }
  } finally {
    await context.close();
  }
}

Given('I have created a separate Admin account', async ({ page }) => {
  await createAccount(page);
});

When('I create a new Admin with valid details', async ({ page }) => {
  await createAccount(page);
});

Then('the new Admin should appear in the Admin list', async ({ page }) => {
  const account = getAccount(page);
  const accounts = new AdminAccountsPage(page);
  await accounts.filterByLastName(account.nom);
  const row = accounts.rowForUsername(account.username);
  await expect(row).toHaveCount(1);
  await expect(row).toContainText(account.email);
  await expect(row).toContainText(account.nom);
});

When("I edit that Admin's name and email", async ({ page }) => {
  const account = getAccount(page);
  const accounts = new AdminAccountsPage(page);
  await accounts.filterByLastName(account.nom);
  await accounts.openEdit(account.username);
  const edited = {
    nom: `${account.nom}Updated`,
    prenom: 'Edited',
    email: `edited-${account.username}@example.test`,
  };
  await accounts.editFields(edited.nom, edited.prenom, edited.email);
  state(page).edited = edited;
});

Then('the edited details should appear in the Admin list', async ({ page }) => {
  const account = getAccount(page);
  const edited = state(page).edited;
  if (!edited) throw new Error('Scenario must edit an Admin first');
  const accounts = new AdminAccountsPage(page);
  await accounts.filterByLastName(edited.nom);
  const row = accounts.rowForUsername(account.username);
  await expect(row).toHaveCount(1);
  await expect(row).toContainText(edited.nom);
  await expect(row).toContainText(edited.prenom);
  await expect(row).toContainText(edited.email);
});

Then('the edited Admin should still be able to sign in', async ({ page, browser }) => {
  await checkLogin(browser, getAccount(page), true);
});

When('I confirm deletion of that Admin', async ({ page }) => {
  const account = getAccount(page);
  const accounts = new AdminAccountsPage(page);
  await accounts.filterByLastName(account.nom);
  await accounts.delete(account.username);
});

Then('that Admin should no longer appear in the list', async ({ page }) => {
  const account = getAccount(page);
  const accounts = new AdminAccountsPage(page);
  await accounts.filterByLastName(account.nom);
  await expect(accounts.rowForUsername(account.username)).toHaveCount(0);
});

Then('the deleted Admin should not be able to sign in', async ({ page, browser }) => {
  await checkLogin(browser, getAccount(page), false);
});

When('I submit an Admin creation form with a blank last name', async ({ page }) => {
  const admin = uniqueAdmin();
  const accounts = new AdminAccountsPage(page);
  await accounts.openCreate();
  await accounts.fillCreate({ ...admin, nom: ' ' });
  await accounts.submitCreate();
  state(page).enteredUsername = admin.username;
});

Then('the last name validation message should be shown', async ({ page }) => {
  await expect(page).toHaveURL(/\/admin\/saveAdmin$/);
  await expect(page.locator('.text-danger')).toContainText(['Last name is required']);
});

Then('the entered username should be preserved', async ({ page }) => {
  await expect(page.locator('input[name="username"]'))
    .toHaveValue(state(page).enteredUsername!);
});

When('I attempt to create another Admin with the same username', async ({ page }) => {
  const account = getAccount(page);
  const accounts = new AdminAccountsPage(page);
  const another = uniqueAdmin();
  await accounts.openCreate();
  await accounts.fillCreate({ ...another, username: account.username });
  await accounts.submitCreate();
});

Then('the duplicate username validation message should be shown', async ({ page }) => {
  await expect(page).toHaveURL(/\/admin\/saveAdmin$/);
  await expect(page.locator('.text-danger')).toContainText(['Username already exists']);
});

When('I open my own Admin record in the list', async ({ page }) => {
  const username = process.env.ADMIN_USERNAME;
  if (!username) throw new Error('Missing ADMIN_USERNAME');
  const accounts = new AdminAccountsPage(page);
  await accounts.filterByLastName('Admin');
  state(page).enteredUsername = username;
});

Then('the Delete action should not be available for my account', async ({ page }) => {
  const accounts = new AdminAccountsPage(page);
  const row = accounts.rowForUsername(state(page).enteredUsername!);
  await expect(row).toHaveCount(1);
  await expect(row.getByRole('button', { name: 'Delete' })).toHaveCount(0);
});
