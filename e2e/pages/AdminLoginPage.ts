import { expect, type Page } from '@playwright/test';

export class AdminLoginPage {
  constructor(private readonly page: Page) {}

  async open() {
    await this.page.goto('/login');
    await expect(this.page.locator('input[name="username"]')).toBeVisible();
  }

  async signIn(username: string, password: string) {
    await this.page.locator('input[name="username"]').fill(username);
    await this.page.locator('input[name="password"]').fill(password);
    await this.page.locator('button[type="submit"], input[type="submit"]').click();
  }
}
