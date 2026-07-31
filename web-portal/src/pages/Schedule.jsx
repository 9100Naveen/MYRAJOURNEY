import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { Calendar, Clock, User, Search, X, Plus } from 'lucide-react';

const Schedule = () => {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    title: 'Follow-up Consultation',
    start_time: new Date().toISOString().slice(0, 16),
    end_time: new Date(Date.now() + 3600000).toISOString().slice(0, 16),
    description: '',
    target_id: '' // patient_id if doctor, doctor_id if patient
  });

  const [reschedulingAppt, setReschedulingAppt] = useState(null);
  const [rescheduleTime, setRescheduleTime] = useState('');

  const handleCancelAppointment = async (apptId) => {
    if (!window.confirm("Are you sure you want to cancel this appointment?")) return;
    try {
      const res = await apiClient.delete(`/appointments/${apptId}`);
      if (res.data.success) {
        alert("Appointment cancelled successfully!");
        fetchAppointments();
      }
    } catch (err) {
      console.error("Failed to cancel appointment", err);
      alert("Failed to cancel appointment.");
    }
  };

  const fetchAppointments = async () => {
    try {
      const response = await apiClient.get('/appointments');
      if (response.data.success && response.data.data) {
        setAppointments(response.data.data);
      } else {
        setAppointments([]);
      }
    } catch (err) {
      console.error("Backend fetch failed:", err);
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = {
        title: form.title,
        start_time: new Date(form.start_time).toISOString().replace('T', ' ').slice(0, 19),
        end_time: new Date(form.end_time).toISOString().replace('T', ' ').slice(0, 19),
        description: form.description
      };

      if (user?.role === 'PATIENT') {
        payload.doctor_id = parseInt(form.target_id) || 1; // Default fallback
      } else {
        payload.patient_id = parseInt(form.target_id) || 1; // Default fallback
      }

      const res = await apiClient.post('/appointments', payload);
      if (res.data.success) {
        setShowModal(false);
        setForm({ ...form, description: '', target_id: '' });
        fetchAppointments();
        alert('Appointment created successfully!');
      }
    } catch (err) {
      console.error("Failed to create appointment", err);
      const msg = err.response?.data?.error?.message || err.message || 'Unknown error';
      alert(`Failed to create appointment: ${msg}`);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredAppointments = appointments.filter(appt => 
    (appt.title && appt.title.toLowerCase().includes(searchQuery.toLowerCase())) ||
    (appt.doctor_name && appt.doctor_name.toLowerCase().includes(searchQuery.toLowerCase())) ||
    (appt.patient_name && appt.patient_name.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <div>
          <h1 style={{ fontSize: '32px', fontWeight: 800, margin: '0 0 8px 0', color: '#0f172a' }}>Schedule & Appointments</h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '15px' }}>
            Manage your upcoming appointments and consultations.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#fff7ed', padding: '8px 16px', borderRadius: 'var(--radius-md)', color: '#f97316', fontWeight: 700 }}>
            <Calendar size={18} />
            {appointments.length} Upcoming
          </div>
          <button 
            onClick={() => setShowModal(true)}
            style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#2563eb', padding: '8px 16px', borderRadius: 'var(--radius-md)', color: '#ffffff', fontWeight: 700, border: 'none', cursor: 'pointer' }}>
            <Plus size={18} />
            New Appointment
          </button>
        </div>
      </div>

      <div style={{ position: 'relative' }}>
        <Search size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)' }} />
        <input 
          type="text" 
          placeholder="Search appointments..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="input-field"
          style={{ paddingLeft: '48px', height: '54px' }}
        />
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading schedule...</div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '16px' }}>
          {filteredAppointments.length > 0 ? filteredAppointments.map(appt => (
            <div 
              key={appt.id}
              style={{
                background: '#ffffff',
                borderRadius: 'var(--radius-lg)',
                padding: '24px',
                boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
                border: '1px solid rgba(0,0,0,0.03)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                transition: 'transform 0.2s, box-shadow 0.2s'
              }}
              className="hover-card-effects hover:translate-y-[-2px] hover:shadow-lg"
            >
              <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                <div style={{
                  width: '56px',
                  height: '56px',
                  borderRadius: 'var(--radius-md)',
                  background: '#fff7ed',
                  color: '#f97316',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0
                }}>
                  <Calendar size={24} />
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  <div>
                    <h3 style={{ fontSize: '20px', fontWeight: 800, margin: '0 0 4px 0', color: '#0f172a' }}>{appt.title}</h3>
                    <span style={{ 
                      padding: '2px 8px', 
                      background: appt.status === 'CONFIRMED' ? '#e6fbf4' : '#f1f5f9', 
                      color: appt.status === 'CONFIRMED' ? '#10b981' : '#64748b', 
                      borderRadius: '8px', 
                      fontSize: '11px', 
                      fontWeight: 700 
                    }}>
                      {appt.status}
                    </span>
                  </div>
                  
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Calendar size={16} color="var(--text-muted)" />
                      <span style={{ fontWeight: 600, color: '#334155' }}>{appt.formatted_date || new Date(appt.start_time).toLocaleDateString()}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Clock size={16} color="var(--text-muted)" />
                      <span style={{ fontWeight: 600, color: '#334155' }}>{appt.formatted_time_slot || new Date(appt.start_time).toLocaleTimeString()}</span>
                    </div>
                    {appt.doctor_name && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <User size={16} color="var(--text-muted)" />
                        Dr. {appt.doctor_name}
                      </div>
                    )}
                    {appt.patient_name && user?.role === 'DOCTOR' && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <User size={16} color="var(--text-muted)" />
                        Pt. {appt.patient_name}
                      </div>
                    )}
                  </div>
                  
                  {appt.description && (
                    <p style={{ margin: '4px 0 0 0', color: 'var(--text-muted)', fontSize: '13px' }}>
                      {appt.description}
                    </p>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', gap: '12px' }}>
                <button 
                  onClick={() => {
                    setReschedulingAppt(appt);
                    setRescheduleTime(new Date(appt.start_time).toISOString().slice(0, 16));
                  }}
                  style={{
                    padding: '12px 20px',
                    borderRadius: 'var(--radius-md)',
                    border: 'none',
                    background: '#eff6ff',
                    color: '#2563eb',
                    fontSize: '14px',
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                  }}
                >
                  Reschedule
                </button>
                <button 
                  onClick={() => handleCancelAppointment(appt.id)}
                  style={{
                    padding: '12px 20px',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid #fee2e2',
                    background: '#fdf2f2',
                    color: '#ef4444',
                    fontSize: '14px',
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                  }}
                >
                  Cancel
                </button>
              </div>
            </div>
          )) : (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)', background: '#ffffff', borderRadius: 'var(--radius-lg)', border: '1px solid var(--card-border)' }}>
              No upcoming appointments.
            </div>
          )}
        </div>
      )}

      {/* CREATE APPOINTMENT MODAL */}
      {showModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: 'white', padding: '32px', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '500px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <h2 style={{ margin: 0, fontSize: '24px', fontWeight: 800 }}>New Appointment</h2>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                <X size={24} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Title</label>
                <input type="text" value={form.title} onChange={(e) => setForm({...form, title: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required />
              </div>
              
              <div style={{ display: 'flex', gap: '16px' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
                  <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Start Time</label>
                  <input type="datetime-local" value={form.start_time} onChange={(e) => setForm({...form, start_time: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
                  <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>End Time</label>
                  <input type="datetime-local" value={form.end_time} onChange={(e) => setForm({...form, end_time: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required />
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>
                  {user?.role === 'PATIENT' ? 'Doctor ID' : 'Patient ID'} (Optional)
                </label>
                <input type="number" placeholder="1" value={form.target_id} onChange={(e) => setForm({...form, target_id: e.target.value})} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Description</label>
                <textarea value={form.description} onChange={(e) => setForm({...form, description: e.target.value})} rows={3} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1', resize: 'none' }} placeholder="Purpose of visit..." />
              </div>

              <button 
                type="submit" 
                disabled={submitting}
                style={{ padding: '16px', background: submitting ? '#93c5fd' : '#2563eb', color: 'white', border: 'none', borderRadius: 'var(--radius-md)', fontWeight: 700, fontSize: '16px', cursor: submitting ? 'not-allowed' : 'pointer', marginTop: '8px', transition: 'background 0.2s' }}>
                {submitting ? 'Scheduling...' : 'Schedule Appointment'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* RESCHEDULE APPOINTMENT MODAL */}
      {reschedulingAppt && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: 'white', padding: '32px', borderRadius: 'var(--radius-lg)', width: '100%', maxWidth: '500px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <h2 style={{ margin: 0, fontSize: '24px', fontWeight: 800 }}>Reschedule Appointment</h2>
              <button onClick={() => setReschedulingAppt(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                <X size={24} />
              </button>
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
                  alert("Appointment rescheduled successfully!");
                  setReschedulingAppt(null);
                  fetchAppointments();
                }
              } catch (err) {
                console.error("Failed to reschedule", err);
                alert("Failed to reschedule appointment.");
              }
            }} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <label style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>New Date & Time</label>
                <input type="datetime-local" value={rescheduleTime} onChange={(e) => setRescheduleTime(e.target.value)} style={{ padding: '12px', borderRadius: 'var(--radius-md)', border: '1px solid #cbd5e1' }} required />
              </div>
              
              <button type="submit" style={{ padding: '16px', background: '#2563eb', color: 'white', border: 'none', borderRadius: 'var(--radius-md)', fontWeight: 700, fontSize: '16px', cursor: 'pointer', marginTop: '8px' }}>
                Confirm Reschedule
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};


export default Schedule;
