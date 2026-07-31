import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import { Pill, Calendar, HeartPulse, Sparkles } from 'lucide-react';

// Layouts
import DashboardLayout from './layouts/DashboardLayout';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import Dashboard from './pages/Dashboard';
import PatientsView from './pages/PatientsView';
import RehabLibrary from './pages/RehabLibrary';
import ClinicalReports from './pages/ClinicalReports';
import Medications from './pages/Medications';
import Schedule from './pages/Schedule';
import CareManagement from './pages/CareManagement';
import AIAssistant from './pages/AIAssistant';
import CreatePatient from './pages/CreatePatient';
import Notifications from './pages/Notifications';
import PatientDetails from './pages/PatientDetails';
import AssignMedications from './pages/AssignMedications';
import AssignRehabilitation from './pages/AssignRehabilitation';

// A simple protected route component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

// Premium Placeholder View for dynamic navigation
const PlaceholderView = ({ title, icon: Icon }) => (
  <div className="animate-slide-up" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '80px 40px', background: '#ffffff', borderRadius: '24px', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 8px 30px rgba(0,0,0,0.02)', marginTop: '20px' }}>
    <div style={{ width: '80px', height: '80px', borderRadius: '24px', background: '#f0f9ff', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '24px' }}>
      <Icon size={40} color="#0ea5e9" />
    </div>
    <h2 style={{ fontSize: '28px', fontWeight: 800, color: '#0f172a', margin: '0 0 12px 0' }}>{title}</h2>
    <p style={{ color: 'var(--text-secondary)', textAlign: 'center', fontSize: '16px', maxWidth: '400px', lineHeight: 1.5 }}>
      This screen is seamlessly integrated into the navigation flow and is currently being prepared for production data syncing.
    </p>
  </div>
);

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      
      <Route 
        path="/dashboard" 
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="patients" element={<PatientsView />} />
        <Route path="patients/:id" element={<PatientDetails />} />
        <Route path="patients/:id/assign-medications" element={<AssignMedications />} />
        <Route path="patients/:id/assign-rehabilitation" element={<AssignRehabilitation />} />
        <Route path="create-patient" element={<CreatePatient />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="exercises" element={<RehabLibrary />} />
        <Route path="reports" element={<ClinicalReports />} />
        
// Dynamic Placeholder Routes
        <Route path="medications" element={<Medications />} />
        <Route path="schedule" element={<Schedule />} />
        <Route path="care" element={<CareManagement />} />
        <Route path="ai-assistant" element={<AIAssistant />} />
      </Route>
      
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
