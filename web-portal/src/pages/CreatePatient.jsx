import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { Camera, ArrowLeft } from 'lucide-react';

const CreatePatient = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    age: '',
    dob: '',
    gender: '',
    email: '',
    address: '',
    agreePrivacy: false,
  });

  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState('');

  const triggerToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.agreePrivacy) {
      triggerToast('You must agree to the Privacy Policy.');
      return;
    }
    
    setLoading(true);
    try {
      const payload = {
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        age: parseInt(formData.age) || 0,
        gender: formData.gender || 'OTHER',
        password: 'welcome1',
        role: 'PATIENT'
      };
      
      const res = await apiClient.post('/admin/users', payload);
      if (res.data.success) {
        triggerToast("Patient registered successfully!");
        setTimeout(() => navigate('/dashboard/patients'), 1500);
      } else {
        triggerToast(res.data?.error?.message || "Registration failed.");
      }
    } catch (err) {
      console.error("Registration failed:", err);
      triggerToast(err.response?.data?.error?.message || "An error occurred.");
    } finally {
      setLoading(false);
    }
  };

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
          bottom: '100px',
          left: '50%',
          transform: 'translateX(-50%)',
          background: 'rgba(30, 41, 59, 0.95)',
          color: '#4ade80',
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
          <img src="https://img.icons8.com/color/48/000000/whatsapp--v1.png" alt="whatsapp" style={{ width: 18, height: 18 }} />
          {toast}
        </div>
      )}

      {/* Header */}
      <div style={{
        background: '#ffffff',
        padding: '16px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        boxShadow: '0 2px 4px rgba(0,0,0,0.02)',
        zIndex: 10
      }}>
        <button 
          onClick={() => navigate(-1)}
          style={{ position: 'absolute', left: '20px', background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
        >
          <ArrowLeft size={24} color="#0f172a" />
        </button>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 700, color: '#000000' }}>Create New Patient</h1>
      </div>

      {/* Scrollable Content */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '20px', paddingBottom: '100px' }}>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px', maxWidth: '600px', margin: '0 auto' }}>
          
          {/* Profile Picture Card */}
          <div style={{
            background: '#ffffff',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '16px'
          }}>
            <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: '#000' }}>Profile Picture</h3>
            
            <div style={{
              width: '120px',
              height: '120px',
              borderRadius: '50%',
              background: '#f0f9ff',
              border: '3px solid #3b82f6',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#94a3b8'
            }}>
              <Camera size={48} strokeWidth={1.5} />
            </div>

            <button
              type="button"
              style={{
                background: '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: '8px',
                padding: '10px 20px',
                fontSize: '14px',
                fontWeight: 600,
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                cursor: 'pointer'
              }}
            >
              <Camera size={16} />
              Add Picture
            </button>
          </div>

          {/* Patient Information Card */}
          <div style={{
            background: '#ffffff',
            borderRadius: '12px',
            padding: '24px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
            display: 'flex',
            flexDirection: 'column',
            gap: '16px'
          }}>
            <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: '#000' }}>Patient Information</h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Full Name</label>
              <input 
                type="text" 
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder="Enter full name"
                required
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Mobile Number</label>
              <input 
                type="tel" 
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="Enter mobile number (starts with 6-9)"
                required
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Age</label>
              <input 
                type="number" 
                name="age"
                value={formData.age}
                onChange={handleChange}
                placeholder="Enter age"
                required
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Date of Birth</label>
              <input 
                type="text" 
                name="dob"
                value={formData.dob}
                onChange={handleChange}
                placeholder="DD/MM/YYYY (Must be 18+ years old)"
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Gender</label>
              <select 
                name="gender"
                value={formData.gender}
                onChange={handleChange}
                required
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box', background: '#fff' }}
              >
                <option value="">Select Gender</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Email Address</label>
              <input 
                type="email" 
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Enter email address"
                required
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box' }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ fontSize: '14px', color: '#334155' }}>Address</label>
              <input 
                type="text" 
                name="address"
                value={formData.address}
                onChange={handleChange}
                placeholder="Enter address"
                style={{ padding: '12px 16px', borderRadius: '8px', border: '1px solid #1e293b', fontSize: '15px', width: '100%', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          {/* Privacy Policy Card */}
          <div style={{
            background: '#ffedd5',
            borderRadius: '0',
            padding: '24px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.02)',
            display: 'flex',
            flexDirection: 'column',
            gap: '16px'
          }}>
            <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#000' }}>Privacy Policy Agreement</h3>
            
            <label style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', cursor: 'pointer' }}>
              <input 
                type="checkbox" 
                name="agreePrivacy"
                checked={formData.agreePrivacy}
                onChange={handleChange}
                style={{ width: '20px', height: '20px', marginTop: '2px', accentColor: '#0f172a' }}
              />
              <span style={{ fontSize: '14px', color: '#1e293b', lineHeight: '1.4' }}>
                I have read and agree to the Privacy Policy (Required)
              </span>
            </label>
            
            <a href="#" style={{ color: '#0f172a', fontSize: '14px', fontWeight: 600, textDecoration: 'none' }}>
              View Privacy Policy
            </a>
          </div>

          {/* Auto-Generated Credentials Card */}
          <div style={{
            background: '#e0f2fe',
            borderRadius: '0',
            padding: '24px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.02)',
            display: 'flex',
            flexDirection: 'column',
            gap: '12px'
          }}>
            <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: '#000' }}>Auto-Generated Credentials</h3>
            
            <div style={{ fontSize: '14px', color: '#000', fontWeight: 700, display: 'flex', gap: '6px' }}>
              Patient ID: <span style={{ fontWeight: 400 }}>(Auto-generated)</span>
            </div>
            <div style={{ fontSize: '14px', color: '#000', fontWeight: 700, display: 'flex', gap: '6px' }}>
              Username: <span style={{ fontWeight: 400 }}>(Will be generated)</span>
            </div>
            <div style={{ fontSize: '14px', color: '#000', fontWeight: 700, display: 'flex', gap: '6px' }}>
              Default Password: <span style={{ fontWeight: 400 }}>welcome1</span>
            </div>
          </div>
        </form>
      </div>

      {/* Sticky Bottom Action */}
      <div style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        background: '#f8fafc',
        padding: '16px 20px',
        borderTop: '1px solid #e2e8f0',
        zIndex: 20
      }}>
        <button
          type="button"
          onClick={handleSubmit}
          disabled={loading}
          style={{
            width: '100%',
            maxWidth: '600px',
            margin: '0 auto',
            display: 'block',
            background: '#2563eb',
            color: '#ffffff',
            border: 'none',
            borderRadius: '12px',
            padding: '16px',
            fontSize: '16px',
            fontWeight: 700,
            cursor: 'pointer',
            opacity: loading ? 0.7 : 1
          }}
        >
          {loading ? 'Registering...' : 'Register Patient'}
        </button>
      </div>
    </div>
  );
};

export default CreatePatient;
