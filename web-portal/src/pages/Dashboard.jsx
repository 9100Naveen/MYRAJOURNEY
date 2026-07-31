import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import apiClient from '../api/client';
import { useAuth } from '../contexts/AuthContext';
import { 
  Users, Activity, FileText, Calendar, HeartPulse, Sparkles, 
  ChevronRight, CheckCircle2, XCircle, Pill, User, UserPlus, 
  RefreshCw, Clock, MapPin, Stethoscope, ArrowLeftRight, UserX,
  Smartphone, Monitor, BellRing
} from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const isPatient = user?.role === 'PATIENT';
  const isDoctor = user?.role === 'DOCTOR';
  const isAdmin = user?.role === 'ADMIN';

  // Removed mobile view simulation toggle as per web overhaul

  // Patient Check-in State
  const [medsCheckIn, setMedsCheckIn] = useState(null); // 'YES' or 'NO'
  const [patientMeds, setPatientMeds] = useState([]);

  // Interaction/Modal States
  const [toast, setToast] = useState('');
  const [activeModal, setActiveModal] = useState(null); // 'Create Patient', 'Create Doctor', 'Assign Patients', 'Manage Users'
  const [modalInput, setModalInput] = useState({});
  const [loadingBtn, setLoadingBtn] = useState(null); // tracks which button is loading

  // Dynamic Lists for Admin Modals
  const [adminDoctors, setAdminDoctors] = useState([]);
  const [adminPatients, setAdminPatients] = useState([]);
  const [adminUsers, setAdminUsers] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  // Appointment Rescheduling State
  const [reschedulingAppt, setReschedulingAppt] = useState(null);
  const [rescheduleTime, setRescheduleTime] = useState('');

  const triggerToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  // Ripple effect helper
  const addRipple = (e) => {
    const btn = e.currentTarget;
    const span = document.createElement('span');
    span.className = 'ripple-span';
    const rect = btn.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    span.style.width = span.style.height = size + 'px';
    span.style.left = (e.clientX - rect.left - size / 2) + 'px';
    span.style.top  = (e.clientY - rect.top  - size / 2) + 'px';
    btn.appendChild(span);
    span.addEventListener('animationend', () => span.remove());
  };

  // State for Doctor Dashboard
  const [doctorStats, setDoctorStats] = useState({
    patientsCount: 3,
    recentReportsCount: 2,
    todayScheduleCount: 1
  });
  
  const [patients, setPatients] = useState([]);

  const [recentActivities, setRecentActivities] = useState([
    {
      id: 1,
      title: "New Symptom Log from lingaiah",
      description: "lingaiah has updated their symptom log with pain level 0/10, stiffness 0/10, and fatigue 0/10...",
      time: "2026-05-29 14:58:55",
      unread: true
    },
    {
      id: 2,
      title: "New Symptom Log from lingaiah",
      description: "lingaiah has updated their symptom log with pain level 0/10, stiffness 0/10, and fatigue 0/10...",
      time: "2026-05-29 13:45:36",
      unread: true
    },
    {
      id: 3,
      title: "New Symptom Log from lingaiah",
      description: "lingaiah has updated their symptom log with pain level 0/10, stiffness 0/10, and fatigue 0/10...",
      time: "2026-05-28 09:24:50",
      unread: false
    }
  ]);

  const [appointments, setAppointments] = useState([
    {
      id: 1,
      patientName: "lingaiah",
      doctorName: "Dr. sai",
      title: "Medical Consultation",
      date: "May 31, 2026",
      time: "10:30 AM",
      type: "Standard Clinical Consultation",
      status: "SCHEDULED"
    }
  ]);

  // Load backend data if available, with failover
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Fetch appointments for both doctors and patients
        const apptRes = await apiClient.get('/appointments?limit=3');
        if (apptRes.data.success && apptRes.data.data) {
          const fetchedAppts = apptRes.data.data.map(apiAppt => ({
            id: apiAppt.id,
            patientName: apiAppt.patient_name || user?.name || "Patient",
            doctorName: apiAppt.doctor_name || "Doctor",
            title: apiAppt.title || "Medical Consultation",
            date: apiAppt.formatted_date || (apiAppt.start_time ? new Date(apiAppt.start_time).toLocaleDateString() : "TBD"),
            time: apiAppt.formatted_time_slot || (apiAppt.start_time ? new Date(apiAppt.start_time).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "TBD"),
            type: apiAppt.description || "Consultation",
            status: apiAppt.status || "SCHEDULED"
          }));
          if (fetchedAppts.length > 0) {
            setAppointments(fetchedAppts);
          }
        }

        if (isPatient) {
          const res = await apiClient.get('/patients/me/overview');
          if (res.data.success && res.data.data) {
            // Stats or other overviews can be set here if needed
          }
          // Fetch medications & logs for check-in
          try {
            const medsRes = await apiClient.get('/patient-medications');
            if (medsRes.data.success && medsRes.data.data) {
              setPatientMeds(medsRes.data.data);
            }
            
            const logsRes = await apiClient.get('/medication-logs');
            if (logsRes.data.success && logsRes.data.data) {
              const logs = logsRes.data.data;
              const todayStr = new Date().toISOString().split('T')[0];
              const todayLogs = logs.filter(log => log.taken_at && log.taken_at.startsWith(todayStr));
              if (todayLogs.length > 0) {
                const hasTaken = todayLogs.some(log => log.status === 'TAKEN');
                const hasSkipped = todayLogs.some(log => log.status === 'SKIPPED' || log.status === 'MISSED');
                if (hasTaken) {
                  setMedsCheckIn('YES');
                } else if (hasSkipped) {
                  setMedsCheckIn('NO');
                }
              }
            }
          } catch (medErr) {
            console.warn("Failed to fetch meds or logs", medErr);
          }
        } else if (isDoctor) {
          const overviewRes = await apiClient.get('/doctor/overview');
          if (overviewRes.data.success && overviewRes.data.data) {
            const stats = overviewRes.data.data;
            setDoctorStats({
              patientsCount: stats.patientsCount || 0,
              recentReportsCount: stats.recentReportsCount || 0,
              todayScheduleCount: stats.todaySchedule?.length || 0
            });
          }
          const patientsRes = await apiClient.get('/patients');
          if (patientsRes.data.success && patientsRes.data.data) {
            setPatients(patientsRes.data.data);
          }
          
          try {
            const notifRes = await apiClient.get('/notifications');
            if (notifRes.data.success && notifRes.data.data) {
              const mappedNotifs = notifRes.data.data.slice(0, 3).map(n => ({
                id: n.id,
                title: n.title,
                description: n.body,
                time: n.created_at ? n.created_at.substring(0, 16) : 'Recently',
                unread: !n.read_at
              }));
              setRecentActivities(mappedNotifs);
            }
          } catch (e) {
            console.warn("Failed to fetch recent activities");
          }
        }
      } catch (err) {
        console.warn("Backend API not reachable. Using exact mockup data for presentation.", err);
      }
    };

    fetchDashboardData();
  }, [isPatient, isDoctor, user]);

  // Load modal-specific lists
  useEffect(() => {
    if (!activeModal) return;

    const fetchModalData = async () => {
      try {
        if (activeModal === 'Assign Patients' || activeModal === 'Create Patient') {
          const [docRes, patRes] = await Promise.all([
            apiClient.get('/admin/doctors'),
            apiClient.get('/patients')
          ]);
          if (docRes.data.success && docRes.data.data) {
            setAdminDoctors(docRes.data.data);
          }
          if (patRes.data.success && patRes.data.data) {
            setAdminPatients(patRes.data.data);
          }
        } else if (activeModal === 'Manage Users') {
          setLoadingUsers(true);
          const res = await apiClient.get('/admin/users');
          if (res.data.success && res.data.data) {
            setAdminUsers(res.data.data);
          }
          setLoadingUsers(false);
        }
      } catch (err) {
        console.error("Failed to load modal details:", err);
      }
    };

    fetchModalData();
  }, [activeModal]);

  const handleMedsCheckIn = async (choice, e) => {
    if (e) addRipple(e);
    setMedsCheckIn(choice);
    setLoadingBtn(`checkin-${choice}`);
    try {
      let medsToLog = patientMeds;
      if (medsToLog.length === 0) {
        const medsRes = await apiClient.get('/patient-medications');
        if (medsRes.data.success && medsRes.data.data) {
          medsToLog = medsRes.data.data;
          setPatientMeds(medsToLog);
        }
      }

      if (medsToLog.length === 0) {
        triggerToast("No active medications found to check in.");
        return;
      }

      const status = choice === 'YES' ? 'TAKEN' : 'SKIPPED';
      const promises = medsToLog.map(med =>
        apiClient.post('/medications/log', {
          patient_medication_id: med.id,
          status: status,
          taken_at: new Date().toISOString().replace('T', ' ').slice(0, 19)
        })
      );
      await Promise.all(promises);
      triggerToast(`Check-in recorded: All medications marked as ${choice === 'YES' ? 'Taken' : 'Skipped'}`);
    } catch (err) {
      console.error("Failed to save check-in:", err);
      triggerToast("Failed to save check-in to backend.");
    } finally {
      setLoadingBtn(null);
    }
  };

  const handleActionClick = (actionName) => {
    setActiveModal(actionName);
    setModalInput({});
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;
    try {
      const res = await apiClient.delete(`/admin/users/${userId}`);
      if (res.data.success) {
        triggerToast("User deleted successfully.");
        const usersRes = await apiClient.get('/admin/users');
        if (usersRes.data.success && usersRes.data.data) {
          setAdminUsers(usersRes.data.data);
        }
        const patientsRes = await apiClient.get('/patients');
        if (patientsRes.data.success && patientsRes.data.data) {
          setPatients(patientsRes.data.data);
        }
      }
    } catch (err) {
      console.error("Failed to delete user", err);
      triggerToast(err.response?.data?.error?.message || "Failed to delete user.");
    }
  };

  const handleCancelAppointment = async (apptId) => {
    if (!window.confirm("Are you sure you want to cancel this appointment?")) return;
    try {
      const res = await apiClient.delete(`/appointments/${apptId}`);
      if (res.data.success) {
        triggerToast("Appointment cancelled successfully.");
        const apptRes = await apiClient.get('/appointments?limit=3');
        if (apptRes.data.success && apptRes.data.data) {
          const fetchedAppts = apptRes.data.data.map(apiAppt => ({
            id: apiAppt.id,
            patientName: apiAppt.patient_name || user?.name || "Patient",
            doctorName: apiAppt.doctor_name || "Doctor",
            title: apiAppt.title || "Medical Consultation",
            date: apiAppt.formatted_date || (apiAppt.start_time ? new Date(apiAppt.start_time).toLocaleDateString() : "TBD"),
            time: apiAppt.formatted_time_slot || (apiAppt.start_time ? new Date(apiAppt.start_time).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "TBD"),
            type: apiAppt.description || "Consultation",
            status: apiAppt.status || "SCHEDULED"
          }));
          setAppointments(fetchedAppts);
        }
      }
    } catch (err) {
      console.error("Failed to cancel appointment", err);
      triggerToast(err.response?.data?.error?.message || "Failed to cancel appointment.");
    }
  };

  const handleModalSubmit = async (e) => {
    e.preventDefault();
    try {
      if (activeModal === 'Create Patient') {
        const payload = {
          name: modalInput.name,
          email: modalInput.email,
          phone: modalInput.phone || '',
          age: parseInt(modalInput.age) || 0,
          gender: modalInput.gender || 'OTHER',
          password: modalInput.password || 'password123',
          role: 'PATIENT'
        };
        if (isAdmin && modalInput.assigned_doctor_id) {
          payload.assigned_doctor_id = parseInt(modalInput.assigned_doctor_id);
        }
        const res = await apiClient.post('/admin/users', payload);
        if (res.data.success) {
          triggerToast("Patient created successfully!");
          setActiveModal(null);
          const patientsRes = await apiClient.get('/patients');
          if (patientsRes.data.success && patientsRes.data.data) {
            setPatients(patientsRes.data.data);
          }
        } else {
          triggerToast(res.data?.error?.message || "Failed to create patient.");
        }
      }
      else if (activeModal === 'Create Doctor') {
        const payload = {
          name: modalInput.name,
          email: modalInput.email,
          phone: modalInput.phone || '',
          specialization: modalInput.specialization || '',
          password: modalInput.password || 'password123',
          role: 'DOCTOR'
        };
        const res = await apiClient.post('/admin/users', payload);
        if (res.data.success) {
          triggerToast("Doctor created successfully!");
          setActiveModal(null);
        } else {
          triggerToast(res.data?.error?.message || "Failed to create doctor.");
        }
      }
      else if (activeModal === 'Assign Patients') {
        const payload = {
          patient_id: parseInt(modalInput.patient_id),
          doctor_id: parseInt(modalInput.doctor_id)
        };
        const res = await apiClient.post('/admin/assign-patient', payload);
        if (res.data.success) {
          triggerToast("Patient assigned to Doctor successfully!");
          setActiveModal(null);
        } else {
          triggerToast(res.data?.error?.message || "Assignment failed.");
        }
      }
    } catch (err) {
      console.error("Modal action failed:", err);
      triggerToast(err.response?.data?.error?.message || "An error occurred.");
    }
  };

  // PATIENT PORTAL RENDERING
  const renderPatientPortal = () => (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px' }}>
      {/* Left Column */}
      <div style={{ flex: '1 1 500px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Greeting */}
        <div style={{ marginTop: '8px' }}>
          <p style={{ color: 'var(--text-secondary)', fontSize: '16px', margin: 0 }}>Good Morning,</p>
          <h1 style={{ fontSize: '36px', fontWeight: 800, margin: '4px 0 0 0', color: '#000000' }}>
            {user?.name || 'lingaiah'}!
          </h1>
        </div>

        {/* AI Health Assistant Card (Ultra Premium) */}
        <motion.div 
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          style={{
            background: 'linear-gradient(135deg, rgba(255,255,255,1) 0%, rgba(240,249,255,1) 100%)',
            borderRadius: 'var(--radius-xl)',
            padding: '24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: 'var(--shadow-lg), 0 0 24px rgba(14, 165, 233, 0.15)',
            border: '1px solid rgba(14, 165, 233, 0.3)',
            cursor: 'pointer',
            position: 'relative',
            overflow: 'hidden'
          }}
          className="hover-card-effects"
          onClick={() => navigate('/dashboard/ai-assistant')}
        >
          {/* Animated Glow Spot */}
          <div className="glow-spot glow-spot-1" style={{ top: '-100px', left: '-100px', opacity: 0.5, background: 'radial-gradient(circle, #0ea5e9 0%, rgba(14,165,233,0) 70%)' }}></div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '20px', zIndex: 1 }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '20px',
              background: 'linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white',
              boxShadow: '0 8px 16px rgba(14, 165, 233, 0.3)'
            }}>
              <Sparkles size={32} />
            </div>
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, margin: '0 0 6px 0', color: '#0f172a' }}>MyRA Health Assistant</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '14px', margin: 0, fontWeight: 500 }}>
                Ask questions about your care plan, medications, or symptoms.
              </p>
            </div>
          </div>
          <div style={{ background: '#ffffff', borderRadius: '50%', padding: '12px', boxShadow: 'var(--shadow-md)', zIndex: 1 }}>
            <ChevronRight size={24} color="#0ea5e9" />
          </div>
        </motion.div>

      {/* Daily Check-in Section */}
      <div>
        <h3 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '16px', color: '#000000' }}>Daily Check-in</h3>
        <div style={{
          background: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '24px',
          boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
          border: '1px solid rgba(0,0,0,0.03)',
          textAlign: 'center'
        }}>
          <p style={{ fontSize: '16px', fontWeight: 600, color: '#334155', marginBottom: '20px' }}>
            Have you taken your medications today?
          </p>
          <div style={{ display: 'flex', gap: '16px', justifyContent: 'center' }}>
            <button 
              onClick={(e) => handleMedsCheckIn('YES', e)}
              disabled={loadingBtn !== null}
              className={`btn-dynamic btn-success-dyn${medsCheckIn === 'YES' ? ' btn-selected' : ''}${loadingBtn === 'checkin-YES' ? ' btn-loading' : ''}`}
              style={{ flex: 1, maxWidth: '180px' }}
            >
              {loadingBtn === 'checkin-YES'
                ? <span className="btn-spinner btn-spinner-dark" style={{ borderTopColor: '#10b981' }} />
                : <CheckCircle2 size={18} fill={medsCheckIn === 'YES' ? '#10b981' : 'transparent'} color="#10b981" />
              }
              YES
            </button>
            <button 
              onClick={(e) => handleMedsCheckIn('NO', e)}
              disabled={loadingBtn !== null}
              className={`btn-dynamic btn-danger-dyn${medsCheckIn === 'NO' ? ' btn-selected' : ''}${loadingBtn === 'checkin-NO' ? ' btn-loading' : ''}`}
              style={{ flex: 1, maxWidth: '180px' }}
            >
              {loadingBtn === 'checkin-NO'
                ? <span className="btn-spinner btn-spinner-dark" style={{ borderTopColor: '#ef4444' }} />
                : <XCircle size={18} fill={medsCheckIn === 'NO' ? '#ef4444' : 'transparent'} color="#ef4444" />
              }
              NO
            </button>
          </div>
        </div>
      </div>

      {/* Health Services Section */}
      <div>
        <h3 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '16px', color: '#000000' }}>Health Services</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
          {/* Medications Card */}
          <div className="service-card-dyn" onClick={() => navigate('/dashboard/medications')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#e6fbf4', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#10b981' }}>
              <Pill size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Medications</h4>
          </div>

          {/* Rehab Plan Card */}
          <div className="service-card-dyn" onClick={() => navigate('/dashboard/exercises')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#e0f2fe', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#0ea5e9' }}>
              <Activity size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Rehab Plan</h4>
          </div>

          {/* Clinical Reports Card */}
          <div className="service-card-dyn" onClick={() => navigate('/dashboard/reports')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#ecfeff', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#0891b2' }}>
              <FileText size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Clinical Reports</h4>
          </div>

          {/* Schedule Card */}
          <div className="service-card-dyn" onClick={() => navigate('/dashboard/schedule')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#fff7ed', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#f97316' }}>
              <Calendar size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Schedule</h4>
          </div>
        </div>
      </div>
      </div>
      
      {/* Right Column */}
      <div style={{ flex: '1 1 350px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Upcoming Appointments Section */}
        <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ fontSize: '20px', fontWeight: 700, margin: 0, color: '#000000' }}>Upcoming Appointments</h3>
          <span 
            onClick={() => navigate('/dashboard/schedule')}
            style={{ color: '#0ea5e9', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}
          >
            View All
          </span>
        </div>
        
        {appointments.map(appt => (
          <div 
            key={appt.id}
            style={{
              background: '#ffffff',
              borderRadius: 'var(--radius-lg)',
              padding: '20px 24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
              border: '1px solid rgba(0,0,0,0.03)'
            }}
          >
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <h4 style={{ margin: 0, fontSize: '18px', fontWeight: 700, color: '#0f172a' }}>{appt.title}</h4>
              <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '14px' }}>{appt.date}</p>
              <p style={{ margin: 0, color: '#0f172a', fontSize: '18px', fontWeight: 850 }}>{appt.time}</p>
              
              <div style={{ 
                display: 'inline-flex', 
                alignItems: 'center', 
                gap: '6px', 
                background: '#eff6ff', 
                color: '#2563eb', 
                padding: '6px 12px', 
                borderRadius: '8px',
                fontSize: '13px',
                fontWeight: 600,
                alignSelf: 'flex-start',
                marginTop: '4px'
              }}>
                <User size={14} />
                {appt.doctorName}
              </div>
            </div>
            
            <div style={{
              width: '48px',
              height: '48px',
              borderRadius: '50%',
              background: '#eff6ff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#3b82f6'
            }}>
              <Calendar size={22} />
            </div>
          </div>
        ))}
        </div>
      </div>
    </div>
  );

  // DOCTOR PORTAL RENDERING
  const renderDoctorPortal = () => (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px' }}>
      
      {/* Left Column */}
      <div style={{ flex: '1 1 500px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Greeting */}
        <div style={{ marginTop: '8px' }}>
          <p style={{ color: 'var(--text-secondary)', fontSize: '16px', margin: 0 }}>Welcome back,</p>
          <h1 style={{ fontSize: '36px', fontWeight: 800, margin: '4px 0 0 0', color: '#000000' }}>
            Doctor!
          </h1>
        </div>

        {/* Quick Actions Grid */}
        <div>
        <h3 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '16px', color: '#000000' }}>Quick Actions</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
          <div className="service-card-dyn" onClick={() => navigate('/dashboard/create-patient')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#2563eb' }}>
              <UserPlus size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>New Patient</h4>
          </div>

          <div className="service-card-dyn" onClick={() => navigate('/dashboard/schedule')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#f3e8ff', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#9333ea' }}>
              <Calendar size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Schedule</h4>
          </div>

          <div className="service-card-dyn" onClick={() => navigate('/dashboard/reports')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#ecfeff', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#0891b2' }}>
              <FileText size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Clinical Reports</h4>
          </div>

          <div className="service-card-dyn" onClick={() => navigate('/dashboard/care')}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: '#fff7ed', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 12px', color: '#f97316' }}>
              <HeartPulse size={24} />
            </div>
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: '#1e293b' }}>Manage Care</h4>
          </div>
        </div>
      </div>

      {/* Recent Activity Section */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ fontSize: '20px', fontWeight: 700, margin: 0, color: '#000000' }}>Recent Activity</h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <button 
              onClick={(e) => { addRipple(e); triggerToast('Syncing data...'); }}
              className="btn-dynamic btn-icon-circle"
              title="Sync data"
            >
              <RefreshCw size={14} />
            </button>
            <span 
              onClick={() => navigate('/dashboard/notifications')}
              style={{ color: '#0ea5e9', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}
            >
              View All
            </span>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {recentActivities.map(act => (
            <div 
              key={act.id}
              style={{
                background: '#ffffff',
                borderRadius: 'var(--radius-lg)',
                padding: '20px',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '16px',
                boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
                border: '1px solid rgba(0,0,0,0.03)',
                position: 'relative'
              }}
            >
              <div style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                background: '#eff6ff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#2563eb',
                flexShrink: 0
              }}>
                <FileText size={18} />
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', flexGrow: 1 }}>
                <h4 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: '#0f172a', paddingRight: '16px' }}>
                  {act.title}
                </h4>
                <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '13px', lineHeight: 1.4 }}>
                  {act.description}
                </p>
                <span style={{ color: 'var(--text-muted)', fontSize: '11px', marginTop: '4px' }}>
                  {act.time}
                </span>
              </div>

              {act.unread && (
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: '#ef4444',
                  position: 'absolute',
                  top: '24px',
                  right: '24px'
                }}></div>
              )}
            </div>
          ))}
        </div>
      </div>
        
        {/* My Patients Section */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: 700, margin: 0, color: '#000000' }}>My Patients</h3>
            <span 
              onClick={() => navigate('/dashboard/patients')}
              style={{ color: '#0ea5e9', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}
            >
              View All
            </span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {patients.map(p => (
              <div 
                key={p.id}
                style={{
                  background: '#ffffff',
                  borderRadius: 'var(--radius-lg)',
                  padding: '16px 20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
                  border: '1px solid rgba(0,0,0,0.03)',
                  cursor: 'pointer'
                }}
                onClick={() => navigate(`/dashboard/patients/${p.id}`)}
                className="hover-card-effects"
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div style={{
                    width: '44px',
                    height: '44px',
                    borderRadius: '50%',
                    background: '#eff6ff',
                    color: '#2563eb',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 800,
                    fontSize: '16px'
                  }}>
                    {p.name.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <h4 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: '#0f172a' }}>{p.name}</h4>
                    <p style={{ margin: '2px 0 0 0', color: 'var(--text-secondary)', fontSize: '12px' }}>
                      Age: {p.age} • {p.email}
                    </p>
                  </div>
                </div>
                <ChevronRight size={18} color="var(--text-muted)" />
              </div>
            ))}
          </div>
        </div>
      </div>
      
      {/* Right Column */}
      <div style={{ flex: '1 1 350px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Upcoming Appointments Section */}
        <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ fontSize: '20px', fontWeight: 700, margin: 0, color: '#000000' }}>Upcoming Appointments</h3>
          <span 
            onClick={() => navigate('/dashboard/schedule')}
            style={{ color: '#0ea5e9', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}
          >
            View All
          </span>
        </div>
        
        {appointments.map(appt => (
          <div 
            key={appt.id}
            style={{
              background: '#ffffff',
              borderRadius: 'var(--radius-lg)',
              padding: '24px',
              boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
              border: '1px solid rgba(0,0,0,0.03)',
              display: 'flex',
              flexDirection: 'column',
              gap: '16px'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <h4 style={{ margin: 0, fontSize: '20px', fontWeight: 800, color: '#0f172a' }}>{appt.patientName}</h4>
                <p style={{ margin: '2px 0 0 0', color: 'var(--text-secondary)', fontSize: '14px' }}>{appt.title}</p>
              </div>
              <span style={{
                background: '#e6fbf4',
                color: '#10b981',
                padding: '4px 12px',
                borderRadius: '20px',
                fontSize: '12px',
                fontWeight: 700,
                border: '1px solid rgba(16, 185, 129, 0.2)'
              }}>
                {appt.status}
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#334155', fontSize: '14px' }}>
                <Calendar size={16} color="var(--text-secondary)" />
                <span>{appt.date}</span>
                <span style={{ margin: '0 4px', color: 'var(--text-muted)' }}>|</span>
                <Clock size={16} color="var(--text-secondary)" />
                <span>{appt.time}</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', fontSize: '13px' }}>
                <MapPin size={16} color="var(--text-muted)" />
                <span>{appt.type}</span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '4px' }}>
              <button 
                onClick={(e) => {
                  addRipple(e);
                  setReschedulingAppt(appt);
                  setRescheduleTime(new Date().toISOString().slice(0, 16));
                }}
                className="btn-dynamic btn-ghost-blue"
                style={{ flex: 1, padding: '12px', borderRadius: '10px', fontSize: '14px' }}
              >
                <ArrowLeftRight size={15} />
                Reschedule
              </button>
              <button 
                onClick={async (e) => {
                  addRipple(e);
                  setLoadingBtn(`cancel-${appt.id}`);
                  await handleCancelAppointment(appt.id);
                  setLoadingBtn(null);
                }}
                disabled={loadingBtn === `cancel-${appt.id}`}
                className={`btn-dynamic btn-ghost-red${loadingBtn === `cancel-${appt.id}` ? ' btn-loading' : ''}`}
                style={{ flex: 1, padding: '12px', borderRadius: '10px', fontSize: '14px' }}
              >
                {loadingBtn === `cancel-${appt.id}`
                  ? <span className="btn-spinner btn-spinner-dark" style={{ borderTopColor: '#ef4444' }} />
                  : <XCircle size={15} />
                }
                Cancel
              </button>
            </div>

          </div>
        ))}
      </div>
      </div>
    </div>
  );

  // ADMIN PORTAL RENDERING
  const renderAdminPortal = () => (
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '24px' }}>
      
      {/* Left Column */}
      <div style={{ flex: '1 1 500px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        {/* Greeting */}
        <div style={{ marginTop: '8px' }}>
          <p style={{ color: 'var(--text-secondary)', fontSize: '16px', margin: 0 }}>Welcome back,</p>
          <h1 style={{ fontSize: '36px', fontWeight: 800, margin: '4px 0 0 0', color: '#000000' }}>
            Admin!
          </h1>
        </div>

        {/* Quick Actions Stack matching Screen 5 */}
        <div>
          <h3 style={{ fontSize: '20px', fontWeight: 700, marginBottom: '20px', color: '#000000' }}>Quick Actions</h3>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {[{
            label: 'Create Patient',
            icon: <UserPlus size={18} />,
            action: () => navigate('/dashboard/create-patient'),
            variant: 'btn-admin-dyn'
          }, {
            label: 'Create Doctor',
            icon: <Stethoscope size={18} />,
            action: () => handleActionClick('Create Doctor'),
            variant: 'btn-admin-dyn'
          }, {
            label: 'Assign Patients',
            icon: <ArrowLeftRight size={18} />,
            action: () => handleActionClick('Assign Patients'),
            variant: 'btn-admin-dyn'
          }, {
            label: 'View All Patients',
            icon: <Users size={18} />,
            action: () => navigate('/dashboard/patients'),
            variant: 'btn-admin-outline-dyn'
          }, {
            label: 'Manage Users',
            icon: <UserX size={18} />,
            action: () => handleActionClick('Manage Users'),
            variant: 'btn-admin-dyn'
          }].map(({ label, icon, action, variant }) => (
            <button
              key={label}
              onClick={(e) => { addRipple(e); action(); }}
              className={`btn-dynamic btn-full ${variant}`}
              style={{ padding: '16px 24px', borderRadius: '12px', fontSize: '16px', justifyContent: 'flex-start', gap: '16px' }}
            >
              <span style={{
                width: '34px', height: '34px', borderRadius: '50%',
                background: variant === 'btn-admin-outline-dyn' ? 'rgba(10,102,194,0.1)' : 'rgba(255,255,255,0.18)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
              }}>{icon}</span>
              {label}
            </button>
          ))}
          </div>
        </div>
      </div>
      
      {/* Right Column (Placeholder for future admin stats if needed, or recent activity) */}
      <div style={{ flex: '1 1 350px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        <div style={{ padding: '24px', background: '#ffffff', borderRadius: '14px', border: '1px solid rgba(0,0,0,0.03)' }}>
           <h3 style={{ fontSize: '18px', fontWeight: 700, margin: '0 0 16px 0', color: '#0f172a' }}>System Status</h3>
           <p style={{ margin: 0, color: 'var(--text-secondary)' }}>All systems operational.</p>
        </div>
      </div>
    </div>
  );

  return (
    <div style={{ position: 'relative', width: '100%', minHeight: '100%', paddingBottom: '40px' }}>
      
      {/* CSS Styles Scoped for Premium Effects */}
      <style>{`
        @keyframes slideUp {
          from { opacity: 0; transform: translateY(16px); }
          to   { opacity: 1; transform: translateY(0); }
        }
        .toast-in { animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
      `}</style>

      {/* View Toggle Bar (Fixed in bottom corner or top bar) */}


      {/* Success Toast */}
      {toast && (
        <div className="toast-in" style={{
          position: 'fixed',
          bottom: '24px',
          right: '24px',
          background: 'rgba(15, 23, 42, 0.95)',
          color: '#ffffff',
          padding: '14px 24px',
          borderRadius: '12px',
          boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
          zIndex: 1000,
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          fontSize: '14px',
          fontWeight: 600,
          backdropFilter: 'blur(8px)'
        }}>
          <BellRing size={16} color="#0ea5e9" />
          {toast}
        </div>
      )}

      {/* Modal Dialog for Admin Actions */}
      {activeModal && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.5)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 2000,
          padding: '20px'
        }}>
          <div style={{
            background: '#ffffff',
            borderRadius: 'var(--radius-lg)',
            padding: '32px',
            maxWidth: activeModal === 'Manage Users' ? '800px' : '480px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            boxShadow: 'var(--shadow-lg)',
            border: '1px solid var(--card-border)'
          }}>
            <h3 style={{ fontSize: '22px', fontWeight: 800, marginBottom: '24px', color: '#0f172a' }}>
              {activeModal}
            </h3>
            
            {activeModal === 'Manage Users' ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {loadingUsers ? (
                  <div style={{ padding: '20px', textAlign: 'center' }}>Loading users...</div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                          <tr style={{ borderBottom: '2px solid #e2e8f0' }}>
                            <th style={{ padding: '8px' }}>Name</th>
                            <th style={{ padding: '8px' }}>Email</th>
                            <th style={{ padding: '8px' }}>Role</th>
                            <th style={{ padding: '8px', textAlign: 'right' }}>Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {adminUsers.map(u => (
                            <tr key={u.id} style={{ borderBottom: '1px solid #edf2f7' }}>
                              <td style={{ padding: '8px', fontWeight: 600 }}>{u.name || 'N/A'}</td>
                              <td style={{ padding: '8px' }}>{u.email}</td>
                              <td style={{ padding: '8px' }}>
                                <span style={{
                                  padding: '2px 8px',
                                  borderRadius: '6px',
                                  fontSize: '11px',
                                  fontWeight: 700,
                                  background: u.role === 'ADMIN' ? '#fee2e2' : (u.role === 'DOCTOR' ? '#e0f2fe' : '#e6fbf4'),
                                  color: u.role === 'ADMIN' ? '#ef4444' : (u.role === 'DOCTOR' ? '#0ea5e9' : '#10b981')
                                }}>
                                  {u.role}
                                </span>
                              </td>
                              <td style={{ padding: '8px', textAlign: 'right' }}>
                                {u.id !== user?.id && (
                                  <button
                                    onClick={() => handleDeleteUser(u.id)}
                                    style={{
                                      background: '#fdf2f2',
                                      border: 'none',
                                      color: '#ef4444',
                                      padding: '6px 12px',
                                      borderRadius: '6px',
                                      fontWeight: 600,
                                      cursor: 'pointer'
                                    }}
                                  >
                                    Delete
                                  </button>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '12px' }}>
                  <button 
                    type="button" 
                    onClick={(e) => { addRipple(e); setActiveModal(null); }}
                    className="btn-dynamic btn-outline-neutral"
                    style={{ padding: '12px 24px' }}
                  >
                    Close
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleModalSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {activeModal === 'Assign Patients' ? (
                  <>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Select Patient</label>
                      <select required className="input-field" style={{ height: '50px' }} value={modalInput.patient_id || ''} onChange={(e) => setModalInput({...modalInput, patient_id: e.target.value})}>
                        <option value="">-- Select Patient --</option>
                        {adminPatients.map(p => (
                          <option key={p.id} value={p.id}>{p.name} ({p.email})</option>
                        ))}
                      </select>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Select Physician</label>
                      <select required className="input-field" style={{ height: '50px' }} value={modalInput.doctor_id || ''} onChange={(e) => setModalInput({...modalInput, doctor_id: e.target.value})}>
                        <option value="">-- Select Physician --</option>
                        {adminDoctors.map(d => (
                          <option key={d.id} value={d.id}>{d.name} ({d.specialization || 'General'})</option>
                        ))}
                      </select>
                    </div>
                  </>
                ) : (
                  <>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Full Name</label>
                      <input required type="text" className="input-field" placeholder="e.g. Grishma Patel" value={modalInput.name || ''} onChange={(e) => setModalInput({...modalInput, name: e.target.value})} />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Email Address</label>
                      <input required type="email" className="input-field" placeholder="name@domain.com" value={modalInput.email || ''} onChange={(e) => setModalInput({...modalInput, email: e.target.value})} />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Mobile / Phone</label>
                      <input type="text" className="input-field" placeholder="e.g. +123456789" value={modalInput.phone || ''} onChange={(e) => setModalInput({...modalInput, phone: e.target.value})} />
                    </div>
                    
                    {activeModal === 'Create Patient' && (
                      <>
                        <div style={{ display: 'flex', gap: '12px' }}>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
                            <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Age</label>
                            <input type="number" className="input-field" placeholder="30" value={modalInput.age || ''} onChange={(e) => setModalInput({...modalInput, age: e.target.value})} />
                          </div>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
                            <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Gender</label>
                            <select className="input-field" style={{ height: '50px' }} value={modalInput.gender || 'OTHER'} onChange={(e) => setModalInput({...modalInput, gender: e.target.value})}>
                              <option value="MALE">Male</option>
                              <option value="FEMALE">Female</option>
                              <option value="OTHER">Other</option>
                            </select>
                          </div>
                        </div>
                        
                        {isAdmin && (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Assign to Physician (Optional)</label>
                            <select className="input-field" style={{ height: '50px' }} value={modalInput.assigned_doctor_id || ''} onChange={(e) => setModalInput({...modalInput, assigned_doctor_id: e.target.value})}>
                              <option value="">-- Unassigned --</option>
                              {adminDoctors.map(d => (
                                <option key={d.id} value={d.id}>{d.name}</option>
                              ))}
                            </select>
                          </div>
                        )}
                      </>
                    )}

                    {activeModal === 'Create Doctor' && (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Specialization</label>
                        <input type="text" className="input-field" placeholder="e.g. Rheumatologist" value={modalInput.specialization || ''} onChange={(e) => setModalInput({...modalInput, specialization: e.target.value})} />
                      </div>
                    )}

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>Password</label>
                      <input type="password" className="input-field" placeholder="Default: password123" value={modalInput.password || ''} onChange={(e) => setModalInput({...modalInput, password: e.target.value})} />
                    </div>
                  </>
                )}

                <div style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
                  <button 
                    type="button" 
                    onClick={(e) => { addRipple(e); setActiveModal(null); }}
                    className="btn-dynamic btn-outline-neutral"
                    style={{ flex: 1, padding: '12px' }}
                  >
                    Cancel
                  </button>
                  <button 
                    type="submit"
                    onClick={(e) => addRipple(e)}
                    className={`btn-dynamic btn-admin-dyn${loadingBtn === 'modal-submit' ? ' btn-loading' : ''}`}
                    disabled={loadingBtn === 'modal-submit'}
                    style={{ flex: 1, padding: '12px' }}
                  >
                    {loadingBtn === 'modal-submit'
                      ? <><span className="btn-spinner" />Submitting...</>
                      : 'Submit'
                    }
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Reschedule Modal */}
      {reschedulingAppt && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.5)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 2000,
          padding: '20px'
        }}>
          <div style={{
            background: '#ffffff',
            borderRadius: 'var(--radius-lg)',
            padding: '32px',
            maxWidth: '480px',
            width: '100%',
            boxShadow: 'var(--shadow-lg)',
            border: '1px solid var(--card-border)'
          }}>
            <h3 style={{ fontSize: '22px', fontWeight: 800, marginBottom: '24px', color: '#0f172a' }}>
              Reschedule Appointment
            </h3>
            <div style={{ marginBottom: '16px', fontSize: '15px', color: 'var(--text-secondary)' }}>
              Rescheduling appointment for <strong>{reschedulingAppt.patientName}</strong>
            </div>
            <form onSubmit={async (e) => {
              e.preventDefault();
              try {
                const start_time = new Date(rescheduleTime).toISOString().replace('T', ' ').slice(0, 19);
                const end_time = new Date(new Date(rescheduleTime).getTime() + 30 * 60000).toISOString().replace('T', ' ').slice(0, 19);
                
                const res = await apiClient.patch(`/appointments/${reschedulingAppt.id}`, {
                  start_time,
                  end_time
                });
                if (res.data.success) {
                  triggerToast("Appointment rescheduled successfully!");
                  setReschedulingAppt(null);
                  const apptRes = await apiClient.get('/appointments?limit=3');
                  if (apptRes.data.success && apptRes.data.data) {
                    const fetchedAppts = apptRes.data.data.map(apiAppt => ({
                      id: apiAppt.id,
                      patientName: apiAppt.patient_name || user?.name || "Patient",
                      doctorName: apiAppt.doctor_name || "Doctor",
                      title: apiAppt.title || "Medical Consultation",
                      date: apiAppt.formatted_date || (apiAppt.start_time ? new Date(apiAppt.start_time).toLocaleDateString() : "TBD"),
                      time: apiAppt.formatted_time_slot || (apiAppt.start_time ? new Date(apiAppt.start_time).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "TBD"),
                      type: apiAppt.description || "Consultation",
                      status: apiAppt.status || "SCHEDULED"
                    }));
                    setAppointments(fetchedAppts);
                  }
                }
              } catch (err) {
                console.error("Failed to reschedule", err);
                triggerToast(err.response?.data?.error?.message || "Rescheduling failed.");
              }
            }} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>New Date & Time</label>
                <input required type="datetime-local" className="input-field" value={rescheduleTime} onChange={(e) => setRescheduleTime(e.target.value)} />
              </div>
              <div style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
                <button 
                  type="button" 
                  onClick={(e) => { addRipple(e); setReschedulingAppt(null); }}
                  className="btn-dynamic btn-outline-neutral"
                  style={{ flex: 1, padding: '12px' }}
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  onClick={(e) => addRipple(e)}
                  className={`btn-dynamic btn-primary-dyn${loadingBtn === 'reschedule' ? ' btn-loading' : ''}`}
                  disabled={loadingBtn === 'reschedule'}
                  style={{ flex: 1, padding: '12px' }}
                >
                  {loadingBtn === 'reschedule'
                    ? <><span className="btn-spinner" />Saving...</>
                    : 'Confirm'
                  }
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Main Container - Desktop web view layout with premium grids */}
      <div style={{
        background: '#ffffff',
        borderRadius: 'var(--radius-lg)',
        padding: '40px',
        boxShadow: '0 8px 30px rgba(0,0,0,0.02)',
        border: '1px solid rgba(0,0,0,0.03)'
      }}>
        {isPatient && renderPatientPortal()}
        {isDoctor && renderDoctorPortal()}
        {isAdmin && renderAdminPortal()}
      </div>
    </div>
  );
};

export default Dashboard;
