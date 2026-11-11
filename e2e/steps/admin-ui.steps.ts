import { expect } from '@playwright/test';
import { createBdd } from 'playwright-bdd';
import { AdminAccountsPage } from '../pages/AdminAccountsPage';

const { When, Then } = createBdd();

When('I open the administration dashboard on a mobile viewport', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/adminPage');
});

Then('the mobile navigation menu should be available', async ({ page }) => {
  const menu = page.locator('details.mobile-navigation');
  await expect(menu.locator('summary')).toBeVisible();
  await menu.locator('summary').click();
  await expect(menu).toHaveAttribute('open', '');
});

Then('the mobile Administrators link should open the directory', async ({ page }) => {
  const menu = page.locator('details.mobile-navigation');
  await menu.getByRole('link', { name: 'Administrators' }).click();
  await expect(page).toHaveURL(/\/admin\/getAdmins$/);
  await expect(page.getByRole('heading', { name: 'Admin CRUD' })).toBeVisible();
});

When('I open the new administrator form', async ({ page }) => {
  await new AdminAccountsPage(page).openCreate();
});

Then('the profile inputs should have accessible labels', async ({ page }) => {
  for (const label of ['Nom', 'Prenom', 'Username', 'Email', 'Password']) {
    await expect(page.getByRole('textbox', { name: label, exact: true })
      .or(page.getByLabel(label, { exact: true }))).toBeVisible();
  }
});

Then('cancelling should return to the administrator directory', async ({ page }) => {
  await page.getByRole('link', { name: 'Cancel' }).click();
  await expect(page).toHaveURL(/\/admin\/getAdmins$/);
});
