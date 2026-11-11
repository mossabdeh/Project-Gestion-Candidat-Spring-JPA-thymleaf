import { randomUUID } from 'node:crypto';
import { expect, type Page } from '@playwright/test';
import { createBdd } from 'playwright-bdd';

const { Given, When, Then } = createBdd();

type TestUser = { username: string; nom: string; role: string };
const created = new WeakMap<Page, TestUser>();
const route = (role: string) => role === 'candidate'
  ? { list: '/candidat/getCandidats', add: '/candidat/CandidatAdd' }
  : { list: '/enseignant/getEnseignants', add: '/enseignant/EnseignantAdd' };

async function findRecord(page: Page, role: string) {
  const user = created.get(page);
  if (!user || user.role !== role) throw new Error('Expected role-specific account fixture');
  await page.goto(route(role).list + '?keyword=' + encodeURIComponent(user.nom));
  return page.locator('tbody tr').filter({ hasText: user.username });
}

When('I request {string} with GET', async ({ page }, path: string) => {
  const response = await page.request.get(path, { failOnStatusCode: false, maxRedirects: 0 });
  expect(response.status()).toBe(405);
});

Then('the deletion endpoint should reject GET with status 405', async () => {
  // Asserted in the request step to avoid storing an API response in browser state.
});

Given('I created a {string} account for safe deletion testing', async ({ page }, role: string) => {
  if (role !== 'candidate' && role !== 'teacher') throw new Error('Unexpected role: ' + role);
  const id = randomUUID().replace(/-/g, '').slice(0, 12);
  const username = 'delete-qa-' + id;
  const nom = 'DeleteQa' + id;
  await page.goto(route(role).add);
  const form = page.locator('.form-panel form');
  await form.locator('[name="nom"]').fill(nom);
  await form.locator('[name="prenom"]').fill('Regression');
  await form.locator('[name="username"]').fill(username);
  await form.locator('[name="email"]').fill(username + '@example.test');
  await form.locator('[name="password"]').fill('Test-Pass-' + id + '-2026');
  if (role === 'candidate') {
    await form.locator('[name="dateNaissance"]').fill('1999-04-02');
  }
  await form.getByRole('button', { name: 'Save' }).click();
  await expect(page).toHaveURL(new RegExp(route(role).list + '$'));
  created.set(page, { username, nom, role });
  const row = await findRecord(page, role);
  await expect(row).toHaveCount(1);
});

When('I confirm deleting that {string} account', async ({ page }, role: string) => {
  const row = await findRecord(page, role);
  const form = row.locator('form[method="post"]');
  await expect(form.locator('input[name="_csrf"]')).toHaveCount(1);
  page.once('dialog', dialog => dialog.accept());
  await form.getByRole('button', { name: 'Delete' }).click();
  await expect(page).toHaveURL(new RegExp(route(role).list));
});

When('I cancel deleting that {string} account', async ({ page }, role: string) => {
  const row = await findRecord(page, role);
  const form = row.locator('form[method="post"]');
  await expect(form.locator('input[name="_csrf"]')).toHaveCount(1);
  page.once('dialog', dialog => dialog.dismiss());
  await form.getByRole('button', { name: 'Delete' }).click();
});

Then('that {string} account should no longer appear in the filtered directory', async ({ page }, role: string) => {
  const row = await findRecord(page, role);
  await expect(row).toHaveCount(0);
});

Then('that {string} account should remain in the filtered directory', async ({ page }, role: string) => {
  const row = await findRecord(page, role);
  await expect(row).toHaveCount(1);
});
