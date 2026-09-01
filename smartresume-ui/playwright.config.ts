import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 45000,
  fullyParallel: false,
  reporter: [['html', { open: 'always' }], ['list']],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on',
    headless: false, // Launches a visible browser window on your desktop
    video: 'on',
    screenshot: 'on',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 800 } },
    },
  ],
});
