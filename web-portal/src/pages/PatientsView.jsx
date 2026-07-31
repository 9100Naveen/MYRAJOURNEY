import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { Users, Search, ChevronRight, User } from 'lucide-react';

const PatientsView = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchPatients = async () => {
      try {
        const response = await apiClient.get('/patients');
        if (response.data.success && response.data.data) {
          setPatients(response.data.data);
        } else {
          setPatients([]);
        }
      } catch (err) {
        console.error("Live backend fetch failed:", err);
        setPatients([]);
      } finally {
        setLoading(false);
      }
    };

    fetchPatients();
  }, []);

  const filteredPatients = patients.filter(p => 
    p.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    p.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <div>
          <h1 style={{ fontSize: '32px', fontWeight: 800, margin: '0 0 8px 0', color: '#0f172a' }}>Patient Directory</h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '15px' }}>
            Manage and view all your assigned patients.
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#eff6ff', padding: '8px 16px', borderRadius: 'var(--radius-md)', color: '#2563eb', fontWeight: 700 }}>
          <Users size={18} />
          {patients.length} Total
        </div>
      </div>

      <div style={{ position: 'relative' }}>
        <Search size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)' }} />
        <input 
          type="text" 
          placeholder="Search by name or email..." 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="input-field"
          style={{ paddingLeft: '48px', height: '54px' }}
        />
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading patients...</div>
      ) : (
        <div style={{ background: '#ffffff', borderRadius: 'var(--radius-lg)', boxShadow: '0 8px 30px rgba(0,0,0,0.02)', border: '1px solid rgba(0,0,0,0.03)', overflow: 'hidden' }}>
          <div className="custom-table-wrapper" style={{ border: 'none', borderRadius: 0 }}>
            <table className="custom-table">
              <thead>
                <tr>
                  <th>Patient</th>
                  <th>Contact Info</th>
                  <th>Age</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredPatients.length > 0 ? filteredPatients.map(p => (
                  <tr key={p.id} style={{ cursor: 'pointer', transition: 'background 0.2s' }} className="hover:bg-slate-50" onClick={() => navigate(`/dashboard/care?patientId=${p.id}`)}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#eff6ff', color: '#2563eb', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>
                          {p.name.charAt(0).toUpperCase()}
                        </div>
                        <span style={{ fontWeight: 700, color: '#0f172a' }}>{p.name}</span>
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span style={{ color: '#0f172a' }}>{p.email}</span>
                        <span style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>{p.phone || 'N/A'}</span>
                      </div>
                    </td>
                    <td>{p.age > 0 ? p.age : 'N/A'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button style={{ background: 'transparent', border: 'none', color: '#0ea5e9', cursor: 'pointer' }}>
                        <ChevronRight size={20} />
                      </button>
                    </td>
                  </tr>
                )) : (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', padding: '32px', color: 'var(--text-secondary)' }}>
                      No patients found matching your search.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientsView;
