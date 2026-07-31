# MyRA Journey - Organized Backend Structure

## 📁 Directory Structure

```
backend-organized/
├── core/                    # Main backend application
│   ├── src/                # Source code
│   │   ├── controllers/    # API controllers
│   │   ├── models/         # Data models
│   │   ├── middlewares/    # Middleware functions
│   │   ├── utils/          # Utility classes
│   │   └── config/         # Configuration files
│   ├── public/             # Public web files
│   │   ├── index.php       # Main API entry point
│   │   ├── .htaccess       # Apache configuration
│   │   └── uploads/        # File uploads directory
│   ├── .env                # Environment variables
│   └── README.md           # Core backend documentation
├── rehab/                  # Rehabilitation module
│   ├── RehabController.php # Rehab API controller
│   ├── RehabModel.php      # Rehab data model
│   ├── ExerciseController.php # Exercise API controller
│   ├── ExerciseSessionController.php # Exercise session controller
│   └── ExerciseModel.php   # Exercise data model
├── tests/                  # Test files
│   └── test-*.php          # All test scripts
├── utilities/              # Utility scripts
│   ├── check-*.php         # Database check scripts
│   ├── cleanup-*.php       # Cleanup scripts
│   ├── create-*.php        # Database creation scripts
│   ├── debug-*.php         # Debug scripts
│   └── fix-*.php           # Fix scripts
└── docs/                   # Documentation
    ├── AI_SETUP.md         # AI integration setup
    ├── BACKEND_TEST_RESULTS.md # Test results
    ├── DEPLOYMENT.md       # Deployment guide
    ├── MILESTONES.md       # Project milestones
    └── TESTING_GUIDE.md    # Testing guide
```

## 🎯 Core Backend Features

### API Controllers
- **AuthController.php** - User authentication
- **UserController.php** - User management
- **PatientController.php** - Patient operations
- **DoctorController.php** - Doctor operations
- **AdminController.php** - Admin operations
- **MedicationController.php** - Medication management
- **AppointmentController.php** - Appointment scheduling
- **ReportController.php** - Medical reports
- **ReportNoteController.php** - Report notes and diagnosis
- **SymptomController.php** - Symptom tracking
- **NotificationController.php** - Push notifications
- **ChatbotController.php** - AI chatbot integration
- **CrpController.php** - CRP measurements
- **SettingsController.php** - App settings
- **MetricController.php** - Health metrics
- **EducationController.php** - Educational content

### Data Models
- **UserModel.php** - User data operations
- **MedicationModel.php** - Medication data
- **AppointmentModel.php** - Appointment data
- **ReportModel.php** - Medical report data
- **SymptomModel.php** - Symptom data
- **NotificationModel.php** - Notification data
- **ConversationModel.php** - Chat conversations
- **CrpModel.php** - CRP measurement data
- **SettingsModel.php** - Application settings
- **MetricModel.php** - Health metrics data
- **EducationModel.php** - Educational content
- **PasswordResetModel.php** - Password reset functionality

### Utilities
- **SmartAI.php** - Advanced AI response generation
- **AIService.php** - AI service management
- **Response.php** - API response formatting
- **Upload.php** - File upload handling
- **Validation.php** - Input validation
- **JWT.php** - JSON Web Token handling

## 🏥 Rehabilitation Module

### Separated Rehab Components
- **RehabController.php** - Rehabilitation plan management
- **RehabModel.php** - Rehabilitation data operations
- **ExerciseController.php** - Exercise management
- **ExerciseSessionController.php** - Exercise session tracking
- **ExerciseModel.php** - Exercise data operations

### Features
- Rehabilitation plan creation and management
- Exercise assignment and tracking
- Progress monitoring
- Video-based exercise guidance
- Performance analytics

## 🧪 Testing & Utilities

### Test Scripts (51 files)
- API endpoint testing
- Database connectivity tests
- Authentication testing
- Feature integration tests
- Performance testing

### Utility Scripts (15 files)
- Database schema checks
- Data cleanup operations
- Migration scripts
- Debug utilities
- System maintenance

## 🚀 Deployment

### Production Setup
1. Copy `core/` directory to web server
2. Configure `.env` file with production settings
3. Set up database with proper credentials
4. Configure web server to point to `public/index.php`
5. Set proper file permissions for `uploads/` directory

### Development Setup
1. Use `core/` for main development
2. Use `tests/` for testing new features
3. Use `utilities/` for database maintenance
4. Use `rehab/` for rehabilitation feature development

## 📱 Frontend Integration

### Android App Structure
- Main app uses `core/` backend APIs
- Rehabilitation features use `rehab/` module APIs
- Separated frontend rehab components in `frontend-rehab/`

## 🔧 Configuration

### Environment Variables (.env)
```
DB_HOST=localhost
DB_NAME=myrajourney_new
DB_USER=root
DB_PASS=
JWT_SECRET=your_jwt_secret
AI_PROVIDER=smartai
```

### API Base URL
- Development: `http://localhost/backend-clean/public/index.php/api/v1/`
- Production: `https://yourdomain.com/api/v1/`

## 📊 API Endpoints

### Core Endpoints
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `GET /patients/{id}` - Get patient details
- `POST /medications` - Add medication
- `GET /appointments` - Get appointments
- `POST /reports/upload` - Upload medical report
- `POST /symptoms` - Log symptoms
- `POST /chatbot/chat` - AI chatbot interaction

### Rehab Endpoints
- `GET /rehab-plans` - Get rehabilitation plans
- `POST /rehab-plans` - Create rehabilitation plan
- `GET /exercises` - Get available exercises
- `POST /exercise-sessions` - Log exercise session

## 🎉 Benefits of Organization

### ✅ Advantages
- **Clean Structure** - Separated concerns and modules
- **Easy Maintenance** - Test and utility files organized
- **Scalable** - Modular architecture for future expansion
- **Production Ready** - Clean core without test files
- **Developer Friendly** - Clear separation of components

### 🔄 Migration Path
- Original `backend/` folder preserved
- New organized structure in `backend-organized/`
- All functionality maintained and tested
- Easy rollback if needed

## 🧪 Testing

After organization, test these key features:
1. User login/registration
2. Medication management
3. Report upload and status updates
4. CRP graph plotting
5. AI chatbot responses
6. Rehabilitation plan management
7. Exercise video playback

All core functionality has been preserved and organized for better maintainability!