import { Builder } from 'selenium-webdriver';
import chrome from 'selenium-webdriver/chrome.js';
import { runValidationTests } from './tests/validation.js';
import { runE2ETests } from './tests/e2e.js';
import { runUnitTests } from './tests/unit.js';
import { runLoadTests } from './tests/load.js';
import XLSX from 'xlsx';

const baseUrl = 'http://localhost:5174';

async function main() {
  console.log("=== STARTING AUTONOMOUS MYRA JOURNEY TEST SUITE ===");
  console.log("Running against Vite dev server at http://localhost:5174\n");

  let driver;
  let allResults = [];

  try {
    console.log("Initializing headless Chrome browser via Selenium...");
    let options = new chrome.Options();
    options.addArguments('--headless=new');
    options.addArguments('--disable-gpu');
    options.addArguments('--no-sandbox');
    options.addArguments('--disable-dev-shm-usage');
    options.addArguments('--window-size=1536,730');

    driver = await new Builder()
      .forBrowser('chrome')
      .setChromeOptions(options)
      .build();

    console.log("1. Running Login & Input Validation UI Tests (50 cases)...");
    const valResults = await runValidationTests(driver, baseUrl);
    console.log(`   Completed: ${valResults.length} cases.`);
    allResults = allResults.concat(valResults);

    console.log("2. Running End-to-End User Portal Flows (210 cases)...");
    const e2eResults = await runE2ETests(driver, baseUrl);
    console.log(`   Completed: ${e2eResults.length} cases.`);
    allResults = allResults.concat(e2eResults);

    console.log("3. Running Application Core Unit Tests (80 cases)...");
    const unitResults = runUnitTests();
    console.log(`   Completed: ${unitResults.length} cases.`);
    allResults = allResults.concat(unitResults);

    console.log("4. Running Asset & Route Performance Load Tests (40 cases)...");
    const loadResults = await runLoadTests(baseUrl);
    console.log(`   Completed: ${loadResults.length} cases.`);
    allResults = allResults.concat(loadResults);

  } catch (err) {
    console.error("Critical error in test runner execution:", err);
  } finally {
    if (driver) {
      console.log("Closing Chrome browser...");
      await driver.quit();
    }
  }

  // Generate Excel sheet
  console.log("\nGenerating test_report.xlsx...");
  try {
    const formattedData = allResults.map(r => ({
      "Test ID": r.id,
      "Test Module": r.module,
      "Test Type": r.type,
      "Test Name": r.name,
      "Test Description": r.description,
      "Expected Result": r.expected,
      "Actual Result": r.actual,
      "Status": r.status,
      "Execution Time (ms)": r.durationMs
    }));

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(formattedData);

    // Adjust column widths to be readable
    ws['!cols'] = [
      { wch: 15 }, // Test ID
      { wch: 20 }, // Test Module
      { wch: 15 }, // Test Type
      { wch: 30 }, // Test Name
      { wch: 60 }, // Test Description
      { wch: 60 }, // Expected Result
      { wch: 60 }, // Actual Result
      { wch: 12 }, // Status
      { wch: 20 }  // Execution Time
    ];

    XLSX.utils.book_append_sheet(wb, ws, "Test Executions");
    XLSX.writeFile(wb, "test_report.xlsx");
    console.log("SUCCESS: test_report.xlsx has been successfully written to the test-suite directory!");
    
    // Print summary stats
    const total = allResults.length;
    const passed = allResults.filter(r => r.status === "PASS").length;
    const failed = total - passed;
    console.log(`\nTest Run Summary:`);
    console.log(`  Total Run: ${total}`);
    console.log(`  Passed:    ${passed}`);
    console.log(`  Failed:    ${failed}`);
    console.log(`  Pass Rate: ${((passed/total)*100).toFixed(2)}%\n`);

  } catch (ex) {
    console.error("Failed to generate Excel report:", ex);
  }
}

main();
