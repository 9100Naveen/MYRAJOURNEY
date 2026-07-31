import React, { useState } from 'react';
import { useNavigate, Navigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Lock, Mail, Eye, EyeOff, ShieldAlert, ArrowRight, ArrowLeft } from 'lucide-react';

const ForgotPassword = () => {
  const { requestPasswordReset, resetPassword, login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleContinue = async (e) => {
    e.preventDefault();
    setError('');
    
    setLoading(true);
    // Check if the email exists in DB by requesting a reset
    const result = await requestPasswordReset(email);
    if (result.success) {
      setStep(2);
    } else {
      setError(result.message || 'Email not found.');
    }
    setLoading(false);
  };

  const handleReset = async (e) => {
    e.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    setLoading(true);
    const result = await resetPassword({ email, code: '123456', password: newPassword });
    if (result.success) {
      // Automatically log the user in with the new credentials to fully authenticate and store tokens
      const loginResult = await login(email, newPassword);
      if (loginResult.success) {
        navigate('/dashboard');
      } else {
        navigate('/login');
      }
    } else {
      setError(result.message || 'Failed to reset password');
    }
    setLoading(false);
  };

  return (
    <div className="auth-wrapper">
      <div className="glow-spot glow-spot-1"></div>
      <div className="glow-spot glow-spot-2"></div>

      <div className="auth-card animate-slide-up" style={{ position: 'relative' }}>
        
        {/* Back Button */}
        <button 
          onClick={() => {
            if (step === 2) setStep(1);
            else navigate('/login');
          }}
          style={{
            position: 'absolute',
            top: '24px',
            left: '24px',
            background: 'none',
            border: 'none',
            color: '#94a3b8',
            cursor: 'pointer',
            padding: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: '50%',
            transition: 'all 0.2s ease',
          }}
          onMouseEnter={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.05)'; e.currentTarget.style.color = '#fff'; }}
          onMouseLeave={(e) => { e.currentTarget.style.background = 'none'; e.currentTarget.style.color = '#94a3b8'; }}
        >
          <ArrowLeft size={20} />
        </button>

        {/* Header */}
        <div className="auth-header" style={{ marginTop: '10px' }}>
          <div className="auth-logo">
            <Lock size={36} color="white" />
          </div>
          <h2 style={{ fontSize: '28px', marginBottom: '6px', color: '#ffffff', fontFamily: 'var(--font-heading)' }}>
            Reset Password
          </h2>
          <p style={{ color: '#94a3b8', fontSize: '14px', maxWidth: '300px', margin: '0 auto' }}>
            {step === 1 
              ? 'Enter your registered email address to reset your password.'
              : 'Create a new password for your account.'}
          </p>
        </div>

        {/* Error Notification */}
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
            lineHeight: 1.4,
          }}>
            <ShieldAlert size={18} style={{ flexShrink: 0 }} />
            {error}
          </div>
        )}

        {/* Step 2 Email Display */}
        {step === 2 && (
          <div style={{
            background: 'rgba(255,255,255,0.03)',
            border: '1px solid rgba(255,255,255,0.08)',
            padding: '12px 16px',
            borderRadius: '12px',
            marginBottom: '24px',
            textAlign: 'center'
          }}>
            <span style={{ color: '#e2e8f0', fontSize: '14px', display: 'block', marginBottom: '4px' }}>Resetting password for:</span>
            <span style={{ color: '#60a5fa', fontWeight: 600, fontSize: '15px' }}>{email}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={step === 1 ? handleContinue : handleReset}>
          
          {step === 1 && (
            <div className="form-group">
              <label className="form-label" style={{ color: '#e2e8f0' }}>Email Address</label>
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <Mail size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
                <input
                  type="email"
                  className="register-input-field"
                  style={{ paddingLeft: '42px' }}
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>
          )}

          {step === 2 && (
            <>
              <div className="form-group">
                <label className="form-label" style={{ color: '#e2e8f0' }}>New Password</label>
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                  <Lock size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    className="register-input-field"
                    style={{ paddingLeft: '42px', paddingRight: '42px' }}
                    placeholder="At least 8 characters"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                  />
                  <button 
                    type="button" 
                    onClick={() => setShowPassword(!showPassword)}
                    style={{ position: 'absolute', right: '14px', background: 'none', border: 'none', cursor: 'pointer', padding: 0, color: '#94a3b8' }}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              <div className="form-group" style={{ marginTop: '16px' }}>
                <label className="form-label" style={{ color: '#e2e8f0' }}>Confirm Password</label>
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                  <Lock size={18} color="#94a3b8" style={{ position: 'absolute', left: '14px', pointerEvents: 'none' }} />
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    className="register-input-field"
                    style={{ paddingLeft: '42px', paddingRight: '42px' }}
                    placeholder="Re-enter new password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />
                  <button 
                    type="button" 
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    style={{ position: 'absolute', right: '14px', background: 'none', border: 'none', cursor: 'pointer', padding: 0, color: '#94a3b8' }}
                  >
                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>
            </>
          )}

          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
            style={{ 
              marginTop: '28px', 
              padding: '14px 20px', 
              borderRadius: '14px', 
              fontSize: '15px',
              width: '100%',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            {loading ? 'Please wait...' : (
              <>
                <span>{step === 1 ? 'Continue' : 'Reset Password'}</span>
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '24px' }}>
          <span style={{ color: '#94a3b8', fontSize: '14px' }}>
            Remembered your password?{' '}
          </span>
          <Link to="/login" style={{ color: '#60a5fa', fontWeight: 700, textDecoration: 'none' }}>
            Back to Login
          </Link>
        </div>

      </div>
    </div>
  );
};

export default ForgotPassword;
