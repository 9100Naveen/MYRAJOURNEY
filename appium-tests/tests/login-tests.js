// login-tests.js
// Appium E2E tests for MyRA Journey mobile app login page
// Uses Mocha + Chai as test runner and Selenium WebDriver as Appium client

const { Builder, By, until } = require('selenium-webdriver');
const { expect } = require('chai');
const fs = require('fs');

// Configuration – adjust as needed for your environment
const APPIUM_SERVER = 'http://localhost:4723/wd/hub'; // Appium server URL
const PLATFORM = 'Android'; // or 'iOS'
const DEVICE_NAME = 'Android Emulator'; // name of the emulator/simulator
const APP_PACKAGE = 'com.myrajourney.app'; // replace with actual Android app package
const APP_ACTIVITY = '.MainActivity'; // replace with actual main activity
const AUTOMATION_NAME = 'UiAutomator2'; // for Android; use 'XCUITest' for iOS

// Helper to generate the 300 test scenarios (same logic as Selenium suite)
function generateTestCases() {
  const cases = [];
  for (let i = 1; i <= 300; i++) {
    const id = `TC_APPIUM_LOGIN_${String(i).padStart(3, '0')}`;
    let username, password, description, expectSuccess;
    if (i % 5 === 0) {
      username = `invalid-email-${i}`;
      password = 'Password123!';
      description = 'Invalid email format should be rejected';
      expectSuccess = false;
    } else if (i % 7 === 0) {
      username = `user${i}@example.com`;
      password = 'WrongPass!';
      description = 'Correct email with wrong password should be rejected';
      expectSuccess = false;
    } else if (i % 11 === 0) {
      username = `user${i}@example.com`;
      password = '';
      description = 'Empty password should be rejected';
      expectSuccess = false;
    } else if (i % 13 === 0) {
      username = '';
      password = 'Password123!';
      description = 'Empty email should be rejected';
      expectSuccess = false;
    } else {
      username = `validuser${i}@example.com`;
      password = 'Password123!';
      description = 'Valid credentials should login successfully';
      expectSuccess = true;
    }
    cases.push({ id, username, password, description, expectSuccess });
  }
  return cases;
}

const testCases = generateTestCases();

describe('Login E2E Appium Tests', function () {
  this.timeout(30000); // increase timeout for mobile actions
  let driver;

  before(async () => {
    const caps = {
      platformName: PLATFORM,
      deviceName: DEVICE_NAME,
      automationName: AUTOMATION_NAME,
      appPackage: APP_PACKAGE,
      appActivity: APP_ACTIVITY,
      // If you have a .apk/.ipa file, you can use 'app' capability instead
    };
    driver = await new Builder()
      .usingServer(APPIUM_SERVER)
      .withCapabilities(caps)
      .build();
  });

  after(async () => {
    if (driver) await driver.quit();
  });

  afterEach(async function () {
    if (this.currentTest.state === 'failed') {
      const img = await driver.takeScreenshot();
      fs.writeFileSync(`screenshots/${this.currentTest.title}.png`, img, 'base64');
    }
  });

  // Iterate over the generated cases
  testCases.forEach(({ id, username, password, description, expectSuccess }) => {
    it(`${id}: ${description}`, async () => {
      // Assume the app opens on a welcome screen with a button that navigates to login.
      // Adjust selectors according to your actual UI.

      // Navigate to login screen (example using accessibility id "loginButton")
      const loginBtn = await driver.wait(until.elementLocated(By.accessibilityId('loginButton')), 8000);
      await loginBtn.click();

      // Wait for email and password fields
      const emailField = await driver.wait(until.elementLocated(By.accessibilityId('emailInput')), 8000);
      const passwordField = await driver.wait(until.elementLocated(By.accessibilityId('passwordInput')), 8000);

      await emailField.clear();
      await emailField.sendKeys(username);
      await passwordField.clear();
      await passwordField.sendKeys(password);

      // Submit – assume a button with accessibility id "submitLogin"
      const submitBtn = await driver.findElement(By.accessibilityId('submitLogin'));
      await submitBtn.click();

      if (expectSuccess) {
        // Successful login – expect some element on home/dashboard, e.g., "dashboardHeader"
        const dashboard = await driver.wait(until.elementLocated(By.accessibilityId('dashboardHeader')), 10000);
        const isDisplayed = await dashboard.isDisplayed();
        expect(isDisplayed).to.be.true;
      } else {
        // Failure – expect error toast/message with id "loginError"
        const errorMsg = await driver.wait(until.elementLocated(By.accessibilityId('loginError')), 8000);
        const isDisplayed = await errorMsg.isDisplayed();
        expect(isDisplayed).to.be.true;
      }
    });
  });
});
