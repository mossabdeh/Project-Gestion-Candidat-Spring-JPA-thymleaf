import { expect } from '@playwright/test';
import { createBdd } from 'playwright-bdd';

const { When, Then } = createBdd();

When('I visit the {string} directory', async ({ page }, path: string) => {
  await page.goto(path);
});

Then('I should see the {string} directory in the Admin workspace', async ({ page }, title: string) => {
  await expect(page.getByRole('heading', { name: title, exact: true })).toBeVisible();
  await expect(page.getByRole('navigation', { name: 'Primary' })).toBeVisible();
  await expect(page.getByRole('search').getByLabel('Search by last name')).toBeVisible();
});

Then('the directory should not show an unsupported Delete action', async ({ page }) => {
  await expect(page.getByRole('button', { name: 'Delete' })).toHaveCount(0);
  await expect(page.getByRole('link', { name: 'Delete' })).toHaveCount(0);
});
