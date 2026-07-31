// login-tests.js
// Selenium WebDriver E2E tests for MyRA Journey login page
// Uses Mocha as test runner and Chai for assertions
// Generates 300 test cases programmatically

const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const { expect } = require('chai');
const fs = require('fs');

// Configuration
const BASE_URL = 'http://localhost:5174'; // Adjust if different
const LOGIN_PATH = '/login'; // Assuming route; update if needed

// Helper to generate mock test data
function generateTestCases() {
  const cases = [];
  for (let i = 1; i <= 300; i++) {
    const id = `TC_LOGIN_${String(i).padStart(3, '0')}`;
    // Create varied scenarios: valid, invalid email, wrong password, empty fields, etc.
    let username, password, description, expectSuccess;
    if (i % 5 === 0) {
      // Invalid email format
      username = `invalid-email-${i}`;
      password = 'Password123!';
      description = 'Invalid email format should be rejected';
      expectSuccess = false;
    } else if (i % 7 === 0) {
      // Wrong password
      username = `user${i}@example.com`;
      password = 'WrongPass!';
      description = 'Correct email with wrong password should be rejected';
      expectSuccess = false;
    } else if (i % 11 === 0) {
      // Empty password
      username = `user${i}@example.com`;
      password = '';
      description = 'Empty password should be rejected';
      expectSuccess = false;
    } else if (i % 13 === 0) {
      // Empty email
      username = '';
      password = 'Password123!';
      description = 'Empty email should be rejected';
      expectSuccess = false;
    } else {
      // Valid credentials (placeholder – replace with real test accounts as needed)
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

describe('Login E2E Selenium Tests', function () {
  // Increase timeout for Selenium actions
  this.timeout(20000);

  let driver;

  before(async () => {
    const options = new chrome.Options();
    options.addArguments('--headless', '--disable-gpu', '--no-sandbox');
    driver = await new Builder()
      .forBrowser('chrome')
      .setChromeOptions(options)
      .build();
  });

  after(async () => {
    await driver.quit();
  });

  afterEach(async function () {
    // Capture screenshot on failure for debugging (optional)
    if (this.currentTest.state === 'failed') {
      const img = await driver.takeScreenshot();
      fs.writeFileSync(`screenshots/${this.currentTest.title}.png`, img, 'base64');
    }
  });

  testCases.forEach(({ id, username, password, description, expectSuccess }) => {
    it(`${id}: ${description}`, async () => {
      await driver.get(`${BASE_URL}${LOGIN_PATH}`);
      // Wait for the login form to be present
      await driver.wait(until.elementLocated(By.name('email')), 5000);
      // Fill email and password fields (adjust selectors if different)
      const emailInput = await driver.findElement(By.name('email'));
      const passwordInput = await driver.findElement(By.name('password'));
      await emailInput.clear();
      await emailInput.sendKeys(username);
      await passwordInput.clear();
      await passwordInput.sendKeys(password);

      // Click submit button (assumes button type="submit")
      const submitBtn = await driver.findElement(By.css('button[type="submit"]'));
      await submitBtn.click();

      if (expectSuccess) {
        // Successful login – expect navigation to dashboard or a success indicator
        await driver.wait(until.urlContains('/dashboard'), 8000);
        const url = await driver.getCurrentUrl();
        expect(url).to.include('/dashboard');
      } else {
        // Failure – expect an error message element to be visible
        const errorMsg = await driver.wait(until.elementLocated(By.css('.error-message')), 5000);
        const isDisplayed = await errorMsg.isDisplayed();
        expect(isDisplayed).to.be.true;
      }
    });
  });
});
