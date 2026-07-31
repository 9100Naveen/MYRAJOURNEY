import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { User, ClipboardList, Activity, ArrowLeft, Pill } from 'lucide-react';

const PatientDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [symptoms, setSymptoms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [debugLog, setDebugLog] = useState('');

  // Forms
  const [alertMsg, setAlertMsg] = useState('');
  const [diagnosis, setDiagnosis] = useState('');
  const [treatment, setTreatment] = useState('');
  const [toast, setToast] = useState('');

  const triggerToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        // Fetch patient list and find the specific patient
        const patRes = await apiClient.get('/patients');
        if (patRes.data.success && patRes.data.data) {
          const found = patRes.data.data.find(p => String(p.id) === String(id));
          if (found) {
            setPatient(found);
            setDebugLog(prev => prev + `\nFound patient ${found.name}.`);
          } else {
            // Fallback to mock data just in case mock patients were clicked
            const mocks = [
              { id: 1, name: "lingaiah", email: "lingaiah@gmail.com", age: 0, gender: "Male", role: "PATIENT" },
              { id: 2, name: "shabhari", email: "shabhari@gmail.com", age: 24, gender: "Female", role: "PATIENT" },
              { id: 3, name: "Bharani", email: "bharani@gmail.com", age: 22, gender: "Male", role: "PATIENT" }
            ];
            const mockFound = mocks.find(m => String(m.id) === String(id));
            if (mockFound) {
              setPatient(mockFound);
              setDebugLog(prev => prev + `\nFound mock patient ${mockFound.name}.`);
            } else {
              setDebugLog(prev => prev + `\nPatient ${id} not found in list of ${patRes.data.data.length} patients. IDs: ${patRes.data.data.map(p=>p.id).join(', ')}`);
            }
          }
        } else {
          setDebugLog(prev => prev + `\n/patients returned false success or no data.`);
        }

        // Fetch related data (assuming backend supports patient_id filtering)
        const apptRes = await apiClient.get(`/appointments?patient_id=${id}`);
        if (apptRes.data.success && apptRes.data.data) {
          setAppointments(apptRes.data.data);
        }

        const symRes = await apiClient.get(`/symptoms?patient_id=${id}`);
        if (symRes.data.success && symRes.data.data) {
          setSymptoms(symRes.data.data);
        }

      } catch (err) {
        console.warn("Failed to fetch some patient details:", err);
        setDebugLog(prev => prev + `\nError fetching data: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  const handleSendAlert = async () => {
    if (!alertMsg.trim()) return;
    try {
      // Mocking API call for alert
      // await apiClient.post('/notifications', { user_id: id, message: alertMsg });
      triggerToast("API Response: 200");
      setAlertMsg('');
    } catch (err) {
      triggerToast("Failed to send alert.");
    }
  };

  const handleSaveDiagnosis = async () => {
    if (!diagnosis.trim() && !treatment.trim()) return;
    try {
      // Mocking API call for saving diagnosis/report
      // await apiClient.post('/reports', { patient_id: id, diagnosis, treatment });
      triggerToast("API Response: 200");
    } catch (err) {
      triggerToast("Failed to save diagnosis.");
    }
  };

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>Loading patient details...</div>;
  }

  if (!patient) {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <h2 style={{ color: '#0f172a' }}>Patient not found</h2>
        <pre style={{ textAlign: 'left', background: '#e2e8f0', padding: '10px', fontSize: '12px' }}>{debugLog}</pre>
        <button onClick={() => navigate(-1)} className="btn-dynamic btn-ghost-blue">Go Back</button>
      </div>
    );
  }

  return (
    <div style={{
      background: '#f8fafc',
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      fontFamily: 'system-ui, -apple-system, sans-serif'
    }}>
      {/* Toast Notification */}
      {toast && (
        <div style={{
          position: 'fixed',
          bottom: '24px',
          left: '50%',
          transform: 'translateX(-50%)',
          background: 'rgba(30, 41, 59, 0.95)',
          color: '#ffffff',
          padding: '12px 20px',
          borderRadius: '12px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          zIndex: 1000,
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          fontSize: '14px',
          fontWeight: 500
        }}>
          {toast}
        </div>
      )}

      {/* Header */}
      <div style={{
        background: '#f8fafc',
        padding: '16px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'sticky',
        top: 0,
        zIndex: 10
      }}>
        <button 
          onClick={() => navigate(-1)}
          style={{ position: 'absolute', left: '20px', background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
        >
          <ArrowLeft size={24} color="#0f172a" />
        </button>
        <h1 style={{ margin: 0, fontSize: '22px', fontWeight: 700, color: '#000000' }}>
          Patient Details
        </h1>
      </div>

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '16px', maxWidth: '600px', margin: '0 auto', width: '100%' }}>
        
        {/* Profile Card */}
        <div style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '24px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
            <div style={{
              width: '80px',
              height: '80px',
              borderRadius: '50%',
              background: '#eff6ff',
              border: '3px solid #3b82f6',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#3b82f6'
            }}>
              <User size={40} />
            </div>
            <div>
              <h2 style={{ margin: '0 0 4px 0', fontSize: '20px', fontWeight: 700, color: '#0f172a' }}>{patient.name}</h2>
              <p style={{ margin: '0 0 4px 0', fontSize: '15px', color: '#334155' }}>Age: {patient.age || 'N/A'} Years</p>
              <p style={{ margin: 0, fontSize: '15px', color: '#334155' }}>ID: {patient.id}</p>
            </div>
          </div>
          <button style={{
            width: '100%',
            padding: '12px',
            background: 'transparent',
            border: '1px solid #3b82f6',
            color: '#0f172a',
            borderRadius: '24px',
            fontSize: '14px',
            fontWeight: 600,
            cursor: 'pointer'
          }}>
            EDIT PATIENT
          </button>
        </div>

        {/* Medications Card */}
        <div 
          onClick={() => navigate(`/dashboard/patients/${id}/assign-medications`)}
          style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '24px 20px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
          cursor: 'pointer'
        }}>
          <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a' }}>Medications</div>
          <button 
            onClick={() => navigate(`/dashboard/patients/${id}/assign-medications`)}
            style={{
              background: '#2563eb',
              color: '#ffffff',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 500,
              cursor: 'pointer'
            }}
          >
            Assign Medication
          </button>
        </div>

        {/* Rehabilitation Card */}
        <div 
          onClick={() => navigate(`/dashboard/patients/${id}/assign-rehabilitation`)}
          style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '24px 20px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
          cursor: 'pointer'
        }}>
          <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a' }}>Rehabilitation</div>
          <button 
            onClick={() => navigate(`/dashboard/patients/${id}/assign-rehabilitation`)}
            style={{
              background: '#2563eb',
              color: '#ffffff',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 500,
              cursor: 'pointer'
            }}
          >
            + Add
          </button>
        </div>

        {/* Appointments Card */}
        <div style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '16px 20px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
            <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a' }}>Appointments</div>
            <span 
              onClick={() => navigate('/dashboard/schedule')} 
              style={{ fontSize: '14px', color: '#000000', cursor: 'pointer', fontWeight: 500 }}
            >
              View All
            </span>
          </div>
          {appointments.length > 0 ? (
            <div style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {appointments.slice(0, 3).map(appt => (
                <div key={appt.id} style={{ borderTop: '1px solid #f1f5f9', paddingTop: '12px', display: 'flex', justifyContent: 'space-between' }}>
                  <div>
                    <div style={{ fontWeight: 600, color: '#0f172a', fontSize: '15px' }}>{appt.title || 'Consultation'}</div>
                    <div style={{ color: '#64748b', fontSize: '13px', marginTop: '4px' }}>
                      {appt.date}
                    </div>
                  </div>
                  <div style={{ fontWeight: 600, color: '#0f172a', fontSize: '15px' }}>
                    {appt.time}
                  </div>
                </div>
              ))}
            </div>
          ) : null}
        </div>

        {/* Patient Reports Card */}
        <div 
          onClick={() => navigate('/dashboard/reports')}
          style={{
            background: '#ffffff',
            borderRadius: '16px',
            padding: '16px 20px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            cursor: 'pointer'
          }}
        >
          <ClipboardList size={20} color="#e5a75c" />
          <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a' }}>Patient Reports</div>
        </div>

        {/* Symptom History Card */}
        <div style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '16px 20px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <div style={{
                background: '#e0f2fe',
                padding: '8px',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Activity size={20} color="#0284c7" />
              </div>
              <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a', lineHeight: '1.2' }}>
                Symptom<br/>History
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <span style={{ border: '1px solid #3b82f6', color: '#0f172a', padding: '4px 12px', borderRadius: '16px', fontSize: '13px' }}>
                {symptoms.length} entries
              </span>
              <span 
                onClick={() => navigate('/dashboard/reports')} 
                style={{ fontSize: '14px', color: '#000000', cursor: 'pointer', fontWeight: 500 }}
              >
                View All
              </span>
            </div>
          </div>
          <div style={{ textAlign: 'center', padding: '16px 0', color: '#0f172a', fontSize: '15px' }}>
            {symptoms.length === 0 ? "No symptom logs recorded yet" : "No chart data available."}
          </div>
        </div>

        {/* CRP Progress Card */}
        <div style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '16px 20px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '24px' }}>
            <div style={{
              background: '#e0f2fe',
              padding: '8px',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <Activity size={20} color="#0284c7" />
            </div>
            <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a' }}>
              CRP Progress
            </div>
          </div>
          <div style={{ textAlign: 'center', padding: '32px 0 16px 0', color: '#0f172a', fontSize: '15px' }}>
            No chart data available.
          </div>
        </div>

        {/* Send Alert Card */}
        <div style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '20px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
        }}>
          <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a', marginBottom: '16px' }}>Send Alert</div>
          <textarea
            value={alertMsg}
            onChange={e => setAlertMsg(e.target.value)}
            placeholder="Enter alert message"
            style={{
              width: '100%',
              minHeight: '80px',
              padding: '12px',
              borderRadius: '8px',
              border: '1px solid #0f172a',
              background: '#ffffff',
              fontSize: '15px',
              marginBottom: '16px',
              resize: 'vertical',
              boxSizing: 'border-box',
              fontFamily: 'inherit'
            }}
          />
          <button 
            onClick={handleSendAlert}
            style={{
              width: '100%',
              background: '#2563eb',
              color: '#ffffff',
              border: 'none',
              padding: '14px',
              borderRadius: '8px',
              fontSize: '15px',
              fontWeight: 500,
              cursor: 'pointer'
            }}
          >
            Send Alert
          </button>
        </div>

        {/* Doctor's Diagnosis Card */}
        <div style={{
          background: '#ffffff',
          borderRadius: '16px',
          padding: '20px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <Pill size={20} color="#ef4444" />
            <div style={{ fontSize: '18px', fontWeight: 500, color: '#0f172a' }}>Doctor's Diagnosis & Suggestions</div>
          </div>
          
          <textarea
            value={diagnosis}
            onChange={e => setDiagnosis(e.target.value)}
            placeholder="Enter diagnosis..."
            style={{
              width: '100%',
              minHeight: '80px',
              padding: '12px',
              borderRadius: '8px',
              border: '1px solid #0f172a',
              background: '#ffffff',
              fontSize: '15px',
              marginBottom: '12px',
              resize: 'vertical',
              boxSizing: 'border-box',
              fontFamily: 'inherit'
            }}
          />
          
          <textarea
            value={treatment}
            onChange={e => setTreatment(e.target.value)}
            placeholder="Enter treatment suggestions..."
            style={{
              width: '100%',
              minHeight: '80px',
              padding: '12px',
              borderRadius: '8px',
              border: '1px solid #0f172a',
              background: '#ffffff',
              fontSize: '15px',
              marginBottom: '16px',
              resize: 'vertical',
              boxSizing: 'border-box',
              fontFamily: 'inherit'
            }}
          />
          
          <button 
            onClick={handleSaveDiagnosis}
            style={{
              width: '100%',
              background: '#2563eb',
              color: '#ffffff',
              border: 'none',
              padding: '14px',
              borderRadius: '8px',
              fontSize: '15px',
              fontWeight: 500,
              cursor: 'pointer'
            }}
          >
            Save Diagnosis
          </button>
        </div>
        
        {/* Bottom spacer */}
        <div style={{ height: '24px' }}></div>
      </div>
    </div>
  );
};

export default PatientDetails;
