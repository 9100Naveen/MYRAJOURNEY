import React, { useState } from 'react';
import { useNavigate, Navigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Eye, EyeOff, ShieldAlert, CheckCircle2, HeartPulse, User, Mail, Lock, Phone } from 'lucide-react';

const Register = () => {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('PATIENT');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (!fullName.trim()) {
      setError('Please enter your full name.');
      return;
    }
    if (!email.trim()) {
      setError('Please enter a valid email address.');
      return;
    }
    if (!role) {
      setError('Please select a role.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);

    const result = await register({
      name: fullName,
      email,
      phone,
      password,
      role,
    });

    if (result.success) {
      setSuccessMsg('Account created successfully! Redirecting...');
      setTimeout(() => {
        navigate('/dashboard');
      }, 1000);
    } else {
      setError(result.message || 'Registration failed. Please try again.');
    }

    setLoading(false);
  };

  return (
    <div className="auth-wrapper">
      <div className="glow-spot glow-spot-1"></div>
      <div className="glow-spot glow-spot-2"></div>

      <div className="auth-card animate-slide-up" style={{ maxWidth: '480px', padding: '36px 32px' }}>
        
        {/* Header */}
        <div className="auth-header" style={{ marginBottom: '24px' }}>
          <div className="auth-logo">
            <HeartPulse size={36} color="white" />
          </div>
          <h1 style={{ fontSize: '28px', fontWeight: 800, color: '#ffffff', marginBottom: '6px', fontFamily: 'var(--font-heading)' }}>
            Create Account
          </h1>
          <p style={{ color: '#94a3b8', fontSize: '14px', lineHeight: 1.45, maxWidth: '360px', margin: '0 auto' }}>
            Choose your role and register your credentials to get started.
          </p>
        </div>

        {/* Alerts */}
        {error && (
          <div style={{
            background: 'rgba(244, 63, 94, 0.15)',
            border: '1px solid rgba(244, 63, 94, 0.4)',
            padding: '12px 16px',
            borderRadius: '12px',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            color: '#fb7185',
            fontSize: '13.5px',
          }}>
            <ShieldAlert size={18} style={{ flexShrink: 0 }} />
            {error}
          </div>
        )}

        {successMsg && (
          <div style={{
            background: 'rgba(16, 185, 129, 0.15)',
            border: '1px solid rgba(16, 185, 129, 0.4)',
            padding: '12px 16px',
            borderRadius: '12px',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            color: '#34d399',
            fontSize: '13.5px',
          }}>
            <CheckCircle2 size={18} style={{ flexShrink: 0 }} />
            {successMsg}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          
          {/* Role selector */}
          <div className="auth-role-select" style={{ marginBottom: '8px' }}>
            {['PATIENT', 'DOCTOR'].map((r) => (
              <button
                key={r}
                type="button"
                className={`auth-role-btn ${role === r ? 'active' : ''}`}
                onClick={() => setRole(r)}
              >
                {r.charAt(0) + r.slice(1).toLowerCase()}
              </button>
            ))}
          </div>

          {/* Full Name */}
          <div className="form-group" style={{ marginBottom: 0 }}>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <User size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
              <input
                type="text"
                className="register-input-field"
                style={{ paddingLeft: '42px' }}
                placeholder="Full Name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Email */}
          <div className="form-group" style={{ marginBottom: 0 }}>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Mail size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
              <input
                type="email"
                className="register-input-field"
                style={{ paddingLeft: '42px' }}
                placeholder="Email Address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          {/* Phone */}
          <div className="form-group" style={{ marginBottom: 0 }}>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Phone size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
              <input
                type="tel"
                className="register-input-field"
                style={{ paddingLeft: '42px' }}
                placeholder="Phone Number (Optional)"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
            </div>
          </div>

          {/* Password */}
          <div className="form-group" style={{ marginBottom: 0, position: 'relative' }}>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Lock size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
              <input
                type={showPassword ? 'text' : 'password'}
                className="register-input-field"
                style={{ paddingLeft: '42px', paddingRight: '48px' }}
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              style={{
                position: 'absolute',
                right: '14px',
                top: '50%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#94a3b8',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 2
              }}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {/* Confirm Password */}
          <div className="form-group" style={{ marginBottom: 0, position: 'relative' }}>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Lock size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                className="register-input-field"
                style={{ paddingLeft: '42px', paddingRight: '48px' }}
                placeholder="Confirm Password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              style={{
                position: 'absolute',
                right: '14px',
                top: '50%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#94a3b8',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 2
              }}
            >
              {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="btn-primary btn-patient"
            style={{
              marginTop: '12px',
              padding: '14px',
              borderRadius: '14px',
              fontSize: '15px',
              fontWeight: 700,
            }}
          >
            {loading ? 'REGISTERING...' : 'CREATE ACCOUNT'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '20px' }}>
          <Link
            to="/login"
            style={{
              color: '#60a5fa',
              fontWeight: 700,
              fontSize: '14px',
              textDecoration: 'none',
            }}
          >
            Back To Login
          </Link>
        </div>

      </div>
    </div>
  );
};

export default Register;
