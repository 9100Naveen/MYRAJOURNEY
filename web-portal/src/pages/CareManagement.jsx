import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { HeartPulse, Activity, LineChart, TrendingUp, AlertCircle, X } from 'lucide-react';

const CareManagement = () => {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState([]);
  const [symptoms, setSymptoms] = useState([]);
  const [loading, setLoading] = useState(true);

  // Search Param Parsing
  const [searchParams, setSearchParams] = useSearchParams();
  const patientIdFromParam = searchParams.get('patientId');

  const [patients, setPatients] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState(patientIdFromParam || '');
  const isPatient = user?.role === 'PATIENT';

  // Modal States
  const [showSymptomModal, setShowSymptomModal] = useState(false);
  const [showMetricModal, setShowMetricModal] = useState(false);

  // Form States
  const [symptomForm, setSymptomForm] = useState({
    pain_level: 0,
    stiffness_level: 0,
    fatigue_level: 0,
    notes: '',
    date: new Date().toISOString().split('T')[0]
  });

  const [metricForm, setMetricForm] = useState({
    metric_type: 'Blood Pressure',
    value: '',
    unit: 'mmHg',
    recorded_at: new Date().toISOString().slice(0, 16)
  });

  // Load patients list for selector
  useEffect(() => {
    const fetchPatientsList = async () => {
      if (!isPatient) {
        try {
          const res = await apiClient.get('/patients');
          if (res.data.success && res.data.data) {
            setPatients(res.data.data);
            if (!patientIdFromParam && res.data.data.length > 0) {
              setSelectedPatientId(res.data.data[0].id.toString());
            }
          }
        } catch (err) {
          console.error("Failed to fetch patients list", err);
        }
      }
    };
    fetchPatientsList();
  }, [isPatient, patientIdFromParam]);

  const fetchCareData = async (targetPid) => {
    setLoading(true);
    try {
      const pidToUse = isPatient ? '' : targetPid;
      const querySuffix = pidToUse ? `?patient_id=${pidToUse}` : '';
      
      const [metricsRes, symptomsRes] = await Promise.all([
        apiClient.get(`/health-metrics${querySuffix}`),
        apiClient.get(`/symptoms${querySuffix}`)
      ]);
      
      if (metricsRes.data.success) setMetrics(metricsRes.data.data || []);
      if (symptomsRes.data.success) setSymptoms(symptomsRes.data.data || []);
    } catch (err) {
      console.error("Backend fetch failed:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isPatient) {
      fetchCareData('');
    } else if (selectedPatientId) {
      fetchCareData(selectedPatientId);
    } else {
      setLoading(false);
    }
  }, [isPatient, selectedPatientId]);

  const handleSymptomSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await apiClient.post('/symptoms', symptomForm);
      if (res.data.success) {
        setShowSymptomModal(false);
        setSymptomForm({ pain_level: 0, stiffness_level: 0, fatigue_level: 0, notes: '', date: new Date().toISOString().split('T')[0] });
        fetchCareData(isPatient ? '' : selectedPatientId); // Refresh list
      }
    } catch (err) {
      console.error("Failed to log symptom", err);
      alert("Failed to log symptom");
    }
  };

  const handleMetricSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...metricForm,
        recorded_at: new Date(metricForm.recorded_at).toISOString().replace('T', ' ').slice(0, 19)
      };
      const res = await apiClient.post('/health-metrics', payload);
      if (res.data.success) {
        setShowMetricModal(false);
        setMetricForm({ metric_type: 'Blood Pressure', value: '', unit: 'mmHg', recorded_at: new Date().toISOString().slice(0, 16) });
        fetchCareData(isPatient ? '' : selectedPatientId); // Refresh list
      }
    } catch (err) {
      console.error("Failed to add metric", err);
      alert("Failed to add metric");
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {!isPatient && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', background: '#ffffff', padding: '16px 24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 4px 12px rgba(0,0,0,0.01)' }}>
          <label style={{ fontWeight: 700, fontSize: '15px', color: '#334155' }}>Active Patient Selector:</label>
          <select 
            value={selectedPatientId} 
            onChange={(e) => {
              setSelectedPatientId(e.target.value);
              setSearchParams({ patientId: e.target.value });
            }}
            style={{
              padding: '10px 16px',
              borderRadius: '8px',
              border: '1px solid #cbd5e1',
              fontSize: '14px',
              fontWeight: 600,
              minWidth: '240px',
              outline: 'none',
              background: '#f8fafc'
            }}
          >
            <option value="">-- Select Patient --</option>
            {patients.map(p => (
              <option key={p.id} value={p.id}>{p.name} ({p.email})</option>
            ))}
          </select>
        </div>
      )}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <div>
          <h1 style={{ fontSize: '32px', fontWeight: 800, margin: '0 0 8px 0', color: '#0f172a' }}>Care Management</h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '15px' }}>
            Monitor your health metrics, track symptoms, and manage your overall well-being.
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#fff1f2', padding: '8px 16px', borderRadius: 'var(--radius-md)', color: '#e11d48', fontWeight: 700 }}>
          <HeartPulse size={18} />
          Care Overview
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading care data...</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {/* Quick Stats */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
            <div style={{ background: '#ffffff', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 8px 24px rgba(0,0,0,0.02)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
                <div style={{ padding: '10px', background: '#eff6ff', borderRadius: 'var(--radius-md)', color: '#2563eb' }}>
                  <LineChart size={20} />
                </div>
                <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: 'var(--text-secondary)' }}>Metrics Logged</h3>
              </div>
              <p style={{ margin: 0, fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>{metrics.length}</p>
            </div>
            
            <div style={{ background: '#ffffff', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 8px 24px rgba(0,0,0,0.02)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
                <div style={{ padding: '10px', background: '#fef2f2', borderRadius: 'var(--radius-md)', color: '#ef4444' }}>
                  <AlertCircle size={20} />
                </div>
                <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: 'var(--text-secondary)' }}>Symptom Entries</h3>
              </div>
              <p style={{ margin: 0, fontSize: '28px', fontWeight: 800, color: '#0f172a' }}>{symptoms.length}</p>
            </div>
            
            <div style={{ background: '#ffffff', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 8px 24px rgba(0,0,0,0.02)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
                <div style={{ padding: '10px', background: '#e6fbf4', borderRadius: 'var(--radius-md)', color: '#10b981' }}>
                  <TrendingUp size={20} />
                </div>
                <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: 'var(--text-secondary)' }}>Overall Trend</h3>
              </div>
              <p style={{ margin: 0, fontSize: '20px', fontWeight: 800, color: '#10b981' }}>Stable</p>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
            {/* Symptoms Section */}
            <div style={{ background: '#ffffff', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 8px 24px rgba(0,0,0,0.02)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2 style={{ fontSize: '20px', fontWeight: 800, margin: 0, color: '#0f172a' }}>Recent Symptoms</h2>
                {isPatient && (
                  <button 
                    onClick={() => setShowSymptomModal(true)}
                    style={{ background: '#eff6ff', color: '#2563eb', border: 'none', padding: '8px 16px', borderRadius: 'var(--radius-md)', fontWeight: 600, cursor: 'pointer' }}
                  >
                    Log Symptom
                  </button>
                )}
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {symptoms.slice(0, 5).map(sym => (
                  <div key={sym.id} style={{ padding: '16px', borderRadius: 'var(--radius-md)', background: '#f8fafc', border: '1px solid rgba(0,0,0,0.03)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                      <span style={{ fontWeight: 600, color: '#334155' }}>Pain Level: {sym.pain_level}/10</span>
                      <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>{new Date(sym.created_at).toLocaleDateString()}</span>
                    </div>
                    {sym.notes && <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '14px' }}>{sym.notes}</p>}
                  </div>
                ))}
                {symptoms.length === 0 && (
                  <div style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>No symptoms logged recently.</div>
                )}
              </div>
            </div>

            {/* Health Metrics Section */}
            <div style={{ background: '#ffffff', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(0,0,0,0.03)', boxShadow: '0 8px 24px rgba(0,0,0,0.02)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2 style={{ fontSize: '20px', fontWeight: 800, margin: 0, color: '#0f172a' }}>Health Metrics</h2>
                {isPatient && (
                  <button 
                    onClick={() => setShowMetricModal(true)}
                    style={{ background: '#eff6ff', color: '#2563eb', border: 'none', padding: '8px 16px', borderRadius: 'var(--radius-md)', fontWeight: 600, cursor: 'pointer' }}
                  >
                    Add Metric
                  </button>
                )}
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {metrics.slice(0, 5).map(metric => (
                  <div key={metric.id} style={{ display: 'flex', alignItems: 'center', gap: '16px', padding: '16px', borderRadius: 'var(--radius-md)', background: '#f8fafc', border: '1px solid rgba(0,0,0,0.03)' }}>
                    <div style={{ width: '40px', height: '40px', borderRadius: 'var(--radius-md)', background: '#e0f2fe', color: '#0ea5e9', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <Activity size={20} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <h4 style={{ margin: '0 0 4px 0', fontSize: '16px', fontWeight: 700, color: '#0f172a' }}>{metric.metric_type}</h4>
                      <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '13px' }}>{new Date(metric.recorded_at).toLocaleString()}</p>
                    </div>
                    <div style={{ fontSize: '18px', fontWeight: 800, color: '#0ea5e9' }}>
                      {metric.value} <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{metric.unit}</span>
                    </div>
                  </div>
                ))}
                {metrics.length === 0 && (
                  <div style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>No health metrics recorded yet.</div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SYMPTOM MODAL */}
      {showSymptomModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: 'white', padding: '32px', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '500px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <h2 style={{ margin: 0, fontSize: '24px', fontWeight: 800 }}>Log Symptom</h2>
              <button onClick={() => setShowSymptomModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleSymptomSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Date</label>
                <input type="date" value={symptomForm.date} onChange={(e) => setSymptomForm({...symptomForm, date: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required />
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Pain Level (0-10): {symptomForm.pain_level}</label>
                <input type="range" min="0" max="10" value={symptomForm.pain_level} onChange={(e) => setSymptomForm({...symptomForm, pain_level: Number(e.target.value)})} />
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Stiffness Level (0-10): {symptomForm.stiffness_level}</label>
                <input type="range" min="0" max="10" value={symptomForm.stiffness_level} onChange={(e) => setSymptomForm({...symptomForm, stiffness_level: Number(e.target.value)})} />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Fatigue Level (0-10): {symptomForm.fatigue_level}</label>
                <input type="range" min="0" max="10" value={symptomForm.fatigue_level} onChange={(e) => setSymptomForm({...symptomForm, fatigue_level: Number(e.target.value)})} />
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Notes</label>
                <textarea value={symptomForm.notes} onChange={(e) => setSymptomForm({...symptomForm, notes: e.target.value})} rows={3} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1', resize: 'none' }} placeholder="Any additional details..." />
              </div>

              <button type="submit" style={{ padding: '16px', background: '#2563eb', color: 'white', border: 'none', borderRadius: 'var(--radius-md)', fontWeight: 700, fontSize: '16px', cursor: 'pointer', marginTop: '8px' }}>
                Save Symptom
              </button>
            </form>
          </div>
        </div>
      )}

      {/* METRIC MODAL */}
      {showMetricModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: 'white', padding: '32px', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '500px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <h2 style={{ margin: 0, fontSize: '24px', fontWeight: 800 }}>Add Health Metric</h2>
              <button onClick={() => setShowMetricModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleMetricSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Metric Type</label>
                <select value={metricForm.metric_type} onChange={(e) => setMetricForm({...metricForm, metric_type: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }}>
                  <option value="Blood Pressure">Blood Pressure</option>
                  <option value="Heart Rate">Heart Rate</option>
                  <option value="Weight">Weight</option>
                  <option value="Temperature">Temperature</option>
                </select>
              </div>
              
              <div style={{ display: 'flex', gap: '16px' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
                  <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Value</label>
                  <input type="text" value={metricForm.value} onChange={(e) => setMetricForm({...metricForm, value: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required placeholder="e.g. 120/80" />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', width: '100px' }}>
                  <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Unit</label>
                  <input type="text" value={metricForm.unit} onChange={(e) => setMetricForm({...metricForm, unit: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} />
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Date & Time</label>
                <input type="datetime-local" value={metricForm.recorded_at} onChange={(e) => setMetricForm({...metricForm, recorded_at: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required />
              </div>

              <button type="submit" style={{ padding: '16px', background: '#2563eb', color: 'white', border: 'none', borderRadius: 'var(--radius-md)', fontWeight: 700, fontSize: '16px', cursor: 'pointer', marginTop: '8px' }}>
                Save Metric
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CareManagement;
