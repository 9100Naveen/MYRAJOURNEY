// generate-summary.js
// Generates an Excel summary of the 300 login test cases

const Excel = require('exceljs');
const fs = require('fs');
const path = require('path');

// Reuse the same test case generator as in login-tests.js
function generateTestCases() {
  const cases = [];
  for (let i = 1; i <= 300; i++) {
    const id = `TC_LOGIN_${String(i).padStart(3, '0')}`;
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

async function createExcelReport() {
  const workbook = new Excel.Workbook();
  const sheet = workbook.addWorksheet('Login Test Summary');

  sheet.columns = [
    { header: 'Test ID', key: 'id', width: 15 },
    { header: 'Description', key: 'description', width: 50 },
    { header: 'Username', key: 'username', width: 30 },
    { header: 'Password', key: 'password', width: 20 },
    { header: 'Expected Result', key: 'expected', width: 20 },
    { header: 'Status', key: 'status', width: 15 },
  ];

  const testCases = generateTestCases();
  testCases.forEach(tc => {
    sheet.addRow({
      id: tc.id,
      description: tc.description,
      username: tc.username,
      password: tc.password,
      expected: tc.expectSuccess ? 'Success' : 'Failure',
      status: '' // placeholder for manual fill after execution
    });
  });

  const outputPath = path.resolve(__dirname, 'test-summary.xlsx');
  await workbook.xlsx.writeFile(outputPath);
  console.log('Test summary Excel file written to', outputPath);
}

createExcelReport().catch(err => {
  console.error('Error generating Excel report:', err);
});
