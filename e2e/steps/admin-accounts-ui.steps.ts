import { expect } from '@playwright/test';
import { createBdd } from 'playwright-bdd';

const { When, Then } = createBdd();

When('I open the {string} account form', async ({ page }, path: string) => {
  await page.goto(path);
});

Then('I should see the {string} account form', async ({ page }, title: string) => {
  await expect(page.getByRole('heading', { name: title, exact: true })).toBeVisible();
  await expect(page.getByRole('navigation', { name: 'Primary' })).toBeVisible();
  for (const field of ['Nom', 'Prenom', 'Username', 'Email', 'Password']) {
    await expect(page.getByLabel(field, { exact: true })).toBeVisible();
  }
});

Then('the account form should have a Cancel action linking to {string}', async ({ page }, target: string) => {
  await page.getByRole('link', { name: 'Cancel' }).click();
  await expect(page).toHaveURL(new RegExp(`${target}$`));
});
