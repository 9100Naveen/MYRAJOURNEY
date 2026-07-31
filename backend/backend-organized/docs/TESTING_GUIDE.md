# Backend Testing Guide

## 🧪 Complete Guide to Test Your Backend API

### Prerequisites
1. ✅ XAMPP running (Apache + MySQL)
2. ✅ Database `myrajourney` created
3. ✅ All migrations imported
4. ✅ `.env` file configured

---

## 📋 Quick Test Checklist

### 1. Verify Backend is Running

**Test in Browser:**
```
http://localhost/backend/public/api-info.php
```

Should show: JSON with all available endpoints

**Test Public Endpoint:**
```
http://localhost/backend/public/api/v1/education/articles
```

Should return: `{"success":true,"data":[],"meta":{"total":0,"page":1,"limit":20}}`

---

## 🔍 Testing Methods

### Method 1: Browser Testing (GET Requests Only)

#### Test Public Endpoints:
1. **Education Articles (List)**
   ```
   http://localhost/backend/public/api/v1/education/articles
   ```
   Expected: JSON with articles list

2. **Education Article by Slug**
   ```
   http://localhost/backend/public/api/v1/education/articles/what-is-ra
   ```
   Expected: JSON with article details

#### Test API Info:
```
http://localhost/backend/public/api-info.php
```
Shows all available endpoints

---

### Method 2: PowerShell Testing (All HTTP Methods)

Open PowerShell and run these commands:

#### 1. Test Public Endpoint (GET)
```powershell
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -Method GET | Select-Object -ExpandProperty Content
```

#### 2. Register a New User (POST)
```powershell
$body = '{"email":"newuser@test.com","password":"test123456","role":"PATIENT","name":"Test User"}'
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

Expected Response:
```json
{
  "success": true,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "1",
      "email": "newuser@test.com",
      "role": "PATIENT",
      "name": "Test User"
    }
  }
}
```

#### 3. Login (POST)
```powershell
$body = '{"email":"newuser@test.com","password":"test123456"}'
$response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
$response.Content
```

#### 4. Get Current User (GET with Token)
```powershell
# First login to get token
$body = '{"email":"newuser@test.com","password":"test123456"}'
$response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
$json = $response.Content | ConvertFrom-Json
$token = "Bearer " + $json.data.token

# Then use token
$headers = @{"Authorization" = $token}
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/me" -Headers $headers -Method GET | Select-Object -ExpandProperty Content
```

#### 5. Get Patient Overview (GET with Token)
```powershell
$headers = @{"Authorization" = $token}
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/patients/me/overview" -Headers $headers -Method GET | Select-Object -ExpandProperty Content
```

#### 6. Get Appointments (GET with Token)
```powershell
$headers = @{"Authorization" = $token}
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/appointments" -Headers $headers -Method GET | Select-Object -ExpandProperty Content
```

#### 7. Get Medications (GET with Token)
```powershell
$headers = @{"Authorization" = $token}
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/patient-medications" -Headers $headers -Method GET | Select-Object -ExpandProperty Content
```

#### 8. Create Symptom Log (POST with Token)
```powershell
$body = '{"patient_id":"1","date":"2025-11-04","pain_level":5,"stiffness_level":3,"fatigue_level":4,"notes":"Feeling better today"}'
$headers = @{"Authorization" = $token; "Content-Type" = "application/json"}
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/symptoms" -Method POST -Body $body -Headers $headers | Select-Object -ExpandProperty Content
```

#### 9. Get Symptoms (GET with Token)
```powershell
$headers = @{"Authorization" = $token}
Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/symptoms" -Headers $headers -Method GET | Select-Object -ExpandProperty Content
```

---

### Method 3: Using Postman (Recommended)

#### Setup Postman:
1. Download Postman: https://www.postman.com/downloads/
2. Create a new collection: "MyRA Journey API"
3. Set base URL variable: `http://localhost/backend/public/api/v1`

#### Test Endpoints:

**1. Register User**
- Method: `POST`
- URL: `{{baseUrl}}/auth/register`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "test123456",
  "role": "PATIENT",
  "name": "Test User"
}
```

**2. Login**
- Method: `POST`
- URL: `{{baseUrl}}/auth/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "test123456"
}
```

**3. Get Current User (After Login)**
- Method: `GET`
- URL: `{{baseUrl}}/auth/me`
- Headers: 
  - `Authorization: Bearer YOUR_TOKEN_HERE`
  - `Content-Type: application/json`

**4. Get Patient Overview**
- Method: `GET`
- URL: `{{baseUrl}}/patients/me/overview`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

**5. Get Education Articles (Public)**
- Method: `GET`
- URL: `{{baseUrl}}/education/articles`
- No auth required

**6. Get Appointments**
- Method: `GET`
- URL: `{{baseUrl}}/appointments`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

**7. Create Appointment**
- Method: `POST`
- URL: `{{baseUrl}}/appointments`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`
- Body:
```json
{
  "patient_id": "1",
  "doctor_id": "1",
  "title": "Follow-up Consultation",
  "description": "Regular checkup",
  "start_time": "2025-11-15 10:00:00",
  "end_time": "2025-11-15 10:30:00"
}
```

**8. Get Medications**
- Method: `GET`
- URL: `{{baseUrl}}/patient-medications`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

**9. Get Reports**
- Method: `GET`
- URL: `{{baseUrl}}/reports`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

**10. Get Symptoms**
- Method: `GET`
- URL: `{{baseUrl}}/symptoms`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

**11. Create Symptom Log**
- Method: `POST`
- URL: `{{baseUrl}}/symptoms`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`
- Body:
```json
{
  "patient_id": "1",
  "date": "2025-11-04",
  "pain_level": 5,
  "stiffness_level": 3,
  "fatigue_level": 4,
  "notes": "Feeling better today"
}
```

**12. Get Notifications**
- Method: `GET`
- URL: `{{baseUrl}}/notifications`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

**13. Get Settings**
- Method: `GET`
- URL: `{{baseUrl}}/settings`
- Headers: `Authorization: Bearer YOUR_TOKEN_HERE`

---

### Method 4: Using cURL (Command Line)

#### Test Public Endpoint:
```bash
curl -X GET http://localhost/backend/public/api/v1/education/articles
```

#### Register User:
```bash
curl -X POST http://localhost/backend/public/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"test123456\",\"role\":\"PATIENT\",\"name\":\"Test User\"}"
```

#### Login:
```bash
curl -X POST http://localhost/backend/public/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"test123456\"}"
```

#### Get Current User (with token):
```bash
curl -X GET http://localhost/backend/public/api/v1/auth/me ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🔐 Testing Authentication Flow

### Step-by-Step Authentication Test:

1. **Register a new user**
   ```powershell
   $body = '{"email":"testuser@example.com","password":"test123456","role":"PATIENT","name":"Test User"}'
   $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json"
   $response.Content
   ```
   **Save the token from response!**

2. **Login with same credentials**
   ```powershell
   $body = '{"email":"testuser@example.com","password":"test123456"}'
   $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
   $json = $response.Content | ConvertFrom-Json
   $token = "Bearer " + $json.data.token
   Write-Host "Token: $token"
   ```

3. **Test protected endpoint with token**
   ```powershell
   $headers = @{"Authorization" = $token}
   Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/me" -Headers $headers -Method GET | Select-Object -ExpandProperty Content
   ```

4. **Test without token (should fail)**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/me" -Method GET | Select-Object -ExpandProperty Content
   ```
   Expected: `{"success":false,"error":{"code":"UNAUTHORIZED","message":"Unauthorized"}}`

---

## 🐛 Troubleshooting

### Issue: "DB connection failed"
**Solution:**
- Check MySQL is running in XAMPP
- Verify `.env` file has correct database credentials
- Test database connection:
  ```powershell
  mysql -u root -e "USE myrajourney; SHOW TABLES;"
  ```

### Issue: "404 Not Found"
**Solution:**
- Verify URL is correct: `http://localhost/backend/public/api/v1/...`
- Check Apache is running
- Verify file exists: `C:\xampp\htdocs\backend\public\index.php`

### Issue: "UNAUTHORIZED"
**Solution:**
- Check token is included in Authorization header
- Format: `Authorization: Bearer YOUR_TOKEN`
- Verify token hasn't expired (default: 7 days)

### Issue: "Endpoint not found"
**Solution:**
- Check HTTP method (GET, POST, PUT, PATCH)
- Verify endpoint path matches exactly
- Check `backend/public/index.php` routing

### Issue: "Validation error"
**Solution:**
- Check request body format (JSON)
- Verify required fields are present
- Check data types match expected format

---

## ✅ Expected Responses

### Success Response Format:
```json
{
  "success": true,
  "data": { ... },
  "meta": { ... }  // Optional
}
```

### Error Response Format:
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message"
  }
}
```

### Common HTTP Status Codes:
- `200` - Success
- `201` - Created
- `401` - Unauthorized (missing/invalid token)
- `404` - Not Found
- `422` - Validation Error
- `500` - Server Error

---

## 📊 Quick Test Script

Create a PowerShell script `test-backend.ps1`:

```powershell
# Test Backend API
Write-Host "Testing Backend API..." -ForegroundColor Green

# 1. Test Public Endpoint
Write-Host "`n1. Testing Public Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/education/articles" -Method GET
    Write-Host "✅ Public endpoint works!" -ForegroundColor Green
    Write-Host $response.Content
} catch {
    Write-Host "❌ Public endpoint failed: $_" -ForegroundColor Red
}

# 2. Register User
Write-Host "`n2. Testing Registration..." -ForegroundColor Yellow
try {
    $body = '{"email":"test' + (Get-Random) + '@example.com","password":"test123456","role":"PATIENT","name":"Test User"}'
    $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json"
    Write-Host "✅ Registration works!" -ForegroundColor Green
    $json = $response.Content | ConvertFrom-Json
    $token = "Bearer " + $json.data.token
    Write-Host "Token: $($token.Substring(0,30))..." -ForegroundColor Cyan
} catch {
    Write-Host "❌ Registration failed: $_" -ForegroundColor Red
}

# 3. Test Protected Endpoint
Write-Host "`n3. Testing Protected Endpoint..." -ForegroundColor Yellow
if ($token) {
    try {
        $headers = @{"Authorization" = $token}
        $response = Invoke-WebRequest -Uri "http://localhost/backend/public/api/v1/auth/me" -Headers $headers -Method GET
        Write-Host "✅ Protected endpoint works!" -ForegroundColor Green
        Write-Host $response.Content
    } catch {
        Write-Host "❌ Protected endpoint failed: $_" -ForegroundColor Red
    }
}

Write-Host "`n✅ Backend testing complete!" -ForegroundColor Green
```

Run it:
```powershell
.\test-backend.ps1
```

---

## 🎯 Testing Checklist

- [ ] Public endpoints work (education/articles)
- [ ] User registration works
- [ ] User login works
- [ ] Token is received and valid
- [ ] Protected endpoints work with token
- [ ] Protected endpoints fail without token
- [ ] Patient overview works
- [ ] Appointments list works
- [ ] Medications list works
- [ ] Reports list works
- [ ] Symptoms list works
- [ ] Notifications list works
- [ ] Settings work

---

## 📝 Notes

1. **Token Storage**: Save tokens from login/register responses
2. **Token Format**: Always use `Bearer YOUR_TOKEN` in Authorization header
3. **Base URL**: Always use full path: `http://localhost/backend/public/api/v1/...`
4. **Content-Type**: Always use `application/json` for POST/PUT requests
5. **Error Handling**: Check response status codes and error messages

---

**Last Updated**: November 2024
**Status**: ✅ Ready for Testing

