import { expect, type Locator, type Page } from '@playwright/test';

export type NewAdmin = {
  username: string;
  password: string;
  nom: string;
  prenom: string;
  email: string;
};

export class AdminAccountsPage {
  constructor(private readonly page: Page) {}

  async openList() {
    await this.page.goto('/admin/getAdmins');
    await expect(this.page.getByRole('heading', { name: 'Admin CRUD' })).toBeVisible();
  }

  async openCreate() {
    await this.page.goto('/admin/AdminAdd');
    await expect(this.page.getByRole('heading', { name: 'Add Admin' })).toBeVisible();
  }

  async fillCreate(admin: NewAdmin) {
    await this.page.locator('input[name="nom"]').fill(admin.nom);
    await this.page.locator('input[name="prenom"]').fill(admin.prenom);
    await this.page.locator('input[name="username"]').fill(admin.username);
    await this.page.locator('input[name="password"]').fill(admin.password);
    await this.page.locator('input[name="email"]').fill(admin.email);
  }

  async submitCreate() {
    await this.page.getByRole('button', { name: 'Save' }).click();
  }

  async create(admin: NewAdmin) {
    await this.openCreate();
    await this.fillCreate(admin);
    await this.submitCreate();
    await expect(this.page).toHaveURL(/\/admin\/getAdmins$/);
  }

  async filterByLastName(lastName: string) {
    await this.openList();
    await this.page.locator('form[method="get"] input[name="keyword"][type="text"]').fill(lastName);
    await this.page.getByRole('button', { name: 'Chercher' }).click();
    await expect(this.page).toHaveURL(/\/admin\/getAdmins\?keyword=/);
  }

  rowForUsername(username: string): Locator {
    return this.page.locator('tbody tr').filter({
      has: this.page.locator('td').filter({ hasText: username }),
    });
  }

  async openEdit(username: string) {
    await this.rowForUsername(username).getByRole('link', { name: 'Edit' }).click();
    await expect(this.page).toHaveURL(/\/admin\/editAdmin\?/);
  }

  async editFields(nom: string, prenom: string, email: string) {
    await this.page.locator('input[name="nom"]').fill(nom);
    await this.page.locator('input[name="prenom"]').fill(prenom);
    await this.page.locator('input[name="email"]').fill(email);
    await this.page.getByRole('button', { name: 'Save' }).click();
    await expect(this.page).toHaveURL(/\/admin\/getAdmins$/);
  }

  async delete(username: string) {
    const row = this.rowForUsername(username);
    await expect(row).toHaveCount(1);
    this.page.once('dialog', dialog => dialog.accept());
    await row.getByRole('button', { name: 'Delete' }).click();
    await expect(this.page).toHaveURL(/\/admin\/getAdmins\?/);
  }
}
