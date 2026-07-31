import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { BellRing, ArrowLeft, CheckCircle, Clock, Check, RefreshCw } from 'lucide-react';

const Notifications = () => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState('');

  const triggerToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/notifications');
      if (res.data.success && res.data.data) {
        setNotifications(res.data.data);
      }
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
      triggerToast("Failed to load notifications.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const markAsRead = async (id) => {
    try {
      const res = await apiClient.post(`/notifications/${id}/read`);
      if (res.data.success) {
        setNotifications(prev => 
          prev.map(n => n.id === id ? { ...n, read_at: new Date().toISOString() } : n)
        );
      }
    } catch (err) {
      console.error("Failed to mark as read:", err);
      triggerToast("Failed to mark notification as read.");
    }
  };

  const unreadCount = notifications.filter(n => !n.read_at).length;

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
        background: '#ffffff',
        padding: '16px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'sticky',
        top: 0,
        boxShadow: '0 2px 4px rgba(0,0,0,0.02)',
        zIndex: 10
      }}>
        <button 
          onClick={() => navigate(-1)}
          style={{ position: 'absolute', left: '20px', background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
        >
          <ArrowLeft size={24} color="#0f172a" />
        </button>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 700, color: '#000000', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BellRing size={20} color="#2563eb" />
          Notifications
          {unreadCount > 0 && (
            <span style={{
              background: '#ef4444',
              color: 'white',
              fontSize: '12px',
              padding: '2px 8px',
              borderRadius: '12px',
              fontWeight: 600
            }}>
              {unreadCount}
            </span>
          )}
        </h1>
        <button 
          onClick={fetchNotifications}
          style={{ position: 'absolute', right: '20px', background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
        >
          <RefreshCw size={20} color="#64748b" className={loading ? "spin" : ""} />
        </button>
      </div>

      {/* Scrollable Content */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
        <div style={{ maxWidth: '800px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {loading && notifications.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#64748b' }}>
              Loading notifications...
            </div>
          ) : notifications.length === 0 ? (
            <div style={{
              textAlign: 'center',
              padding: '60px 20px',
              background: '#ffffff',
              borderRadius: '12px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.02)'
            }}>
              <BellRing size={48} color="#cbd5e1" style={{ marginBottom: '16px' }} />
              <h3 style={{ margin: '0 0 8px 0', color: '#0f172a', fontSize: '18px' }}>No notifications yet</h3>
              <p style={{ margin: 0, color: '#64748b', fontSize: '14px' }}>You're all caught up!</p>
            </div>
          ) : (
            notifications.map(notif => {
              const isUnread = !notif.read_at;
              return (
                <div 
                  key={notif.id}
                  style={{
                    background: '#ffffff',
                    borderRadius: '12px',
                    padding: '20px',
                    boxShadow: isUnread ? '0 4px 12px rgba(37, 99, 235, 0.08)' : '0 2px 8px rgba(0,0,0,0.02)',
                    border: isUnread ? '1px solid #bfdbfe' : '1px solid #f1f5f9',
                    display: 'flex',
                    gap: '16px',
                    position: 'relative',
                    transition: 'all 0.2s ease'
                  }}
                >
                  {isUnread && (
                    <div style={{
                      width: '8px',
                      height: '8px',
                      borderRadius: '50%',
                      background: '#3b82f6',
                      position: 'absolute',
                      top: '24px',
                      left: '8px'
                    }} />
                  )}
                  
                  <div style={{
                    width: '48px',
                    height: '48px',
                    borderRadius: '50%',
                    background: isUnread ? '#eff6ff' : '#f8fafc',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: isUnread ? '#2563eb' : '#94a3b8',
                    flexShrink: 0,
                    marginLeft: isUnread ? '8px' : '0'
                  }}>
                    <BellRing size={24} />
                  </div>
                  
                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <h4 style={{ margin: 0, fontSize: '16px', fontWeight: isUnread ? 700 : 600, color: '#0f172a' }}>
                        {notif.title}
                      </h4>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#94a3b8', fontSize: '12px' }}>
                        <Clock size={12} />
                        {new Date(notif.created_at).toLocaleDateString()}
                      </div>
                    </div>
                    
                    <p style={{ margin: 0, color: '#475569', fontSize: '14px', lineHeight: '1.5' }}>
                      {notif.body}
                    </p>
                    
                    {isUnread && (
                      <button
                        onClick={() => markAsRead(notif.id)}
                        style={{
                          alignSelf: 'flex-start',
                          marginTop: '8px',
                          background: 'none',
                          border: 'none',
                          color: '#2563eb',
                          fontSize: '13px',
                          fontWeight: 600,
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '4px',
                          padding: '4px 8px',
                          borderRadius: '6px',
                          transition: 'background 0.2s'
                        }}
                        onMouseOver={(e) => e.currentTarget.style.background = '#eff6ff'}
                        onMouseOut={(e) => e.currentTarget.style.background = 'none'}
                      >
                        <Check size={14} />
                        Mark as read
                      </button>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
      
      {/* CSS for Spin Animation */}
      <style>{`
        @keyframes spin { 100% { transform: rotate(360deg); } }
        .spin { animation: spin 1s linear infinite; }
      `}</style>
    </div>
  );
};

export default Notifications;
