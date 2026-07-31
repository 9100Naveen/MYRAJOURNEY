import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { Pill, Search, CheckCircle2, Clock, Calendar, Check, X } from 'lucide-react';

const Medications = () => {
  const { user } = useAuth();
  const [medications, setMedications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchMedications = async () => {
      try {
        const response = await apiClient.get('/patient-medications');
        if (response.data.success && response.data.data) {
          setMedications(response.data.data);
        } else {
          setMedications([]);
        }
      } catch (err) {
        console.error("Backend fetch failed:", err);
        setMedications([]);
      } finally {
        setLoading(false);
      }
    };

    fetchMedications();
  }, []);

  const handleLogIntake = async (id, status) => {
    try {
      const response = await apiClient.post('/medications/log', {
        patient_medication_id: id,
        status: status,
      });
      if (response.data.success) {
        // Optimistically update UI or show toast
        alert(`Medication marked as ${status}`);
      }
    } catch (error) {
      console.error("Failed to log medication intake:", error);
      alert("Failed to log medication. Please try again.");
    }
  };

  const filteredMeds = medications.filter(m => 
    m.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <div>
          <h1 style={{ fontSize: '32px', fontWeight: 800, margin: '0 0 8px 0', color: '#0f172a' }}>Medications Manager</h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '15px' }}>
            Track and manage your prescribed medications.
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#e6fbf4', padding: '8px 16px', borderRadius: 'var(--radius-md)', color: '#10b981', fontWeight: 700 }}>
          <Pill size={18} />
          {medications.length} Active
        </div>
      </div>

      <div style={{ position: 'relative' }}>
        <Search size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)' }} />
        <input 
          type="text" 
          placeholder="Search medications..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="input-field"
          style={{ paddingLeft: '48px', height: '54px' }}
        />
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading medications...</div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
          {filteredMeds.length > 0 ? filteredMeds.map(med => (
            <div 
              key={med.id}
              style={{
                background: '#ffffff',
                borderRadius: 'var(--radius-lg)',
                padding: '24px',
                boxShadow: '0 8px 24px rgba(0,0,0,0.02)',
                border: '1px solid rgba(0,0,0,0.03)',
                display: 'flex',
                flexDirection: 'column',
                gap: '16px',
                transition: 'transform 0.2s, box-shadow 0.2s'
              }}
              className="hover-card-effects hover:scale-[1.02] hover:shadow-lg"
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{
                  width: '48px',
                  height: '48px',
                  borderRadius: 'var(--radius-md)',
                  background: '#e6fbf4',
                  color: '#10b981',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <Pill size={24} />
                </div>
                {med.active === 1 && (
                  <span style={{
                    padding: '4px 10px',
                    borderRadius: 'var(--radius-md)',
                    fontSize: '12px',
                    fontWeight: 700,
                    background: '#e6fbf4',
                    color: '#10b981',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}>
                    <CheckCircle2 size={12} />
                    Active
                  </span>
                )}
              </div>

              <div>
                <h3 style={{ fontSize: '20px', fontWeight: 800, margin: '0 0 4px 0', color: '#0f172a' }}>{med.name}</h3>
                <p style={{ margin: 0, color: '#0ea5e9', fontSize: '15px', fontWeight: 600 }}>{med.dosage}</p>
                {med.instructions && (
                  <p style={{ margin: '8px 0 0 0', color: 'var(--text-secondary)', fontSize: '14px', lineHeight: 1.4 }}>
                    {med.instructions}
                  </p>
                )}
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', background: '#f8fafc', padding: '12px', borderRadius: 'var(--radius-md)', marginTop: 'auto' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#475569', fontSize: '13px', fontWeight: 500 }}>
                  <Clock size={14} color="var(--text-muted)" />
                  {med.frequency || 'Daily'}
                </div>
                {med.food_relation && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#475569', fontSize: '13px', fontWeight: 500 }}>
                    <Calendar size={14} color="var(--text-muted)" />
                    {med.food_relation}
                  </div>
                )}
              </div>
              
              {user?.role === 'PATIENT' && (
                <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
                  <button 
                    onClick={() => handleLogIntake(med.id, 'TAKEN')}
                    style={{ flex: 1, padding: '10px', borderRadius: 'var(--radius-md)', background: '#10b981', color: 'white', border: 'none', fontWeight: 600, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '6px', cursor: 'pointer' }}
                  >
                    <Check size={16} /> Taken
                  </button>
                  <button 
                    onClick={() => handleLogIntake(med.id, 'SKIPPED')}
                    style={{ flex: 1, padding: '10px', borderRadius: 'var(--radius-md)', background: '#fef2f2', color: '#ef4444', border: '1px solid #fee2e2', fontWeight: 600, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '6px', cursor: 'pointer' }}
                  >
                    <X size={16} /> Skipped
                  </button>
                </div>
              )}
            </div>
          )) : (
            <div style={{ gridColumn: '1 / -1', padding: '40px', textAlign: 'center', color: 'var(--text-secondary)', background: '#ffffff', borderRadius: 'var(--radius-lg)', border: '1px solid var(--card-border)' }}>
              No medications found.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Medications;
