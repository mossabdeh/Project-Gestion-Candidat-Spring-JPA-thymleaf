import { expect } from '@playwright/test';
import { createBdd } from 'playwright-bdd';
import { AdminLoginPage } from '../pages/AdminLoginPage';

const { Given, When, Then } = createBdd();

function adminCredentials() {
  const username = process.env.ADMIN_USERNAME;
  const password = process.env.ADMIN_PASSWORD;
  if (!username || !password) {
    throw new Error('Set ADMIN_USERNAME and ADMIN_PASSWORD before running E2E tests.');
  }
  return { username, password };
}

Given('I am on the login page', async ({ page }) => {
  await new AdminLoginPage(page).open();
});

Given('I am signed in as an admin', async ({ page }) => {
  const { username, password } = adminCredentials();
  const loginPage = new AdminLoginPage(page);
  await loginPage.open();
  await loginPage.signIn(username, password);
  await expect(page).toHaveURL(/\/adminPage$/);
});

When('I sign in with valid admin credentials', async ({ page }) => {
  const { username, password } = adminCredentials();
  await new AdminLoginPage(page).signIn(username, password);
});

When('I sign in with invalid credentials', async ({ page }) => {
  const { username } = adminCredentials();
  await new AdminLoginPage(page).signIn(username, 'wrong-password-for-e2e');
});

Then('I should see the admin dashboard', async ({ page }) => {
  await expect(page).toHaveURL(/\/adminPage$/);
  await expect(page.getByRole('link', { name: 'Admin', exact: true })).toBeVisible();
});

Then('I should see the login error', async ({ page }) => {
  await expect(page).toHaveURL(/\/login\?error(?:[=&]|$)/);
});

When('I sign out', async ({ page }) => {
  await page.getByRole('button', { name: 'Logout' }).click();
});

Then('I should be signed out', async ({ page }) => {
  await expect(page).toHaveURL(/\/login\?logout(?:[=&]|$)/);
});

Then('I cannot reopen the admin dashboard without logging in', async ({ page }) => {
  await page.goto('/adminPage');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
});

When('I open the {string} section', async ({ page }, section: string) => {
  await page.getByRole('link', { name: section, exact: true }).click();
});

Then('I should be on the {string} management page', async ({ page }, path: string) => {
  await expect(page).toHaveURL(new RegExp(`${path.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(?:\\?.*)?$`));
  await expect(page.locator('table, .card-body').first()).toBeVisible();
});
