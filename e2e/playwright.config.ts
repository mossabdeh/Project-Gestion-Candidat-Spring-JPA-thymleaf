import { defineConfig, devices } from '@playwright/test';
import { defineBddConfig, cucumberReporter } from 'playwright-bdd';

const baseURL = process.env.BASE_URL ?? 'http://127.0.0.1:8082';
const testDir = defineBddConfig({
  features: 'features/**/*.feature',
  steps: 'steps/**/*.ts',
});

export default defineConfig({
  testDir,
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  timeout: 30_000,
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
    cucumberReporter('json', { outputFile: 'test-results/cucumber.json' }),
  ],
  use: {
    baseURL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    // Run the existing H2-backed Spring Boot app locally for E2E testing.
    command: process.platform === 'win32'
      ? '..\\mvnw.cmd -B -DskipTests spring-boot:run -Dspring-boot.run.profiles=dev > spring-boot.log 2>&1'
      : '../mvnw -B -DskipTests spring-boot:run -Dspring-boot.run.profiles=dev > spring-boot.log 2>&1',
    url: `${baseURL}/login`,
    reuseExistingServer: !process.env.CI,
    timeout: 180_000,
    stdout: 'pipe',
    stderr: 'pipe',
    env: { SPRING_PROFILES_ACTIVE: 'dev' },
  },
});
