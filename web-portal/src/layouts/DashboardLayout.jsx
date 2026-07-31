import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate, useLocation, useOutlet } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../contexts/AuthContext';
import { 
  HeartPulse, 
  LayoutDashboard, 
  Users, 
  Activity, 
  Settings, 
  LogOut,
  FileText,
  Menu,
  Bell,
  Search,
  Bot,
  Calendar,
  Pill,
  Sparkles,
  ShieldCheck,
  ChevronRight
} from 'lucide-react';

const DashboardLayout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const location = useLocation();
  const currentOutlet = useOutlet();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isPatient = user?.role === 'PATIENT';
  const roleTheme = user?.role === 'ADMIN' ? 'admin' : isPatient ? 'patient' : 'doctor';

  const getPortalTitle = () => {
    switch (user?.role) {
      case 'PATIENT':
        return { main: 'Patient Portal', sub: 'MyRA Journey', badge: 'Patient' };
      case 'DOCTOR':
        return { main: 'Physician Center', sub: 'MyRA Journey', badge: 'Doctor' };
      case 'ADMIN':
        return { main: 'Admin Dashboard', sub: 'Management', badge: 'Admin' };
      default:
        return { main: 'Patient Portal', sub: 'MyRA Journey', badge: 'User' };
    }
  };

  const titleInfo = getPortalTitle();

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="app-container">
      {/* Sidebar Navigation */}
      <aside className={`sidebar ${sidebarOpen ? 'open' : 'closed'}`} style={{
        transition: 'width 0.25s cubic-bezier(0.16, 1, 0.3, 1)',
        width: sidebarOpen ? '260px' : '0px',
        overflow: 'hidden'
      }}>
        <div className="sidebar-mesh"></div>
        <div style={{ minWidth: '260px', display: 'flex', flexDirection: 'column', height: '100%', position: 'relative', zIndex: 1 }}>
          {/* Logo & Brand Header */}
          <div className="portal-branding">
            <div className="portal-logo-container">
              <HeartPulse size={22} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-sidebar)', textTransform: 'uppercase', letterSpacing: '0.08em', fontWeight: 700 }}>
                  {titleInfo.sub}
                </span>
                <span style={{
                  fontSize: '10px',
                  fontWeight: 700,
                  padding: '1px 6px',
                  borderRadius: '99px',
                  background: user?.role === 'ADMIN' ? 'rgba(225,29,72,0.2)' : isPatient ? 'rgba(37,99,235,0.2)' : 'rgba(79,70,229,0.2)',
                  color: user?.role === 'ADMIN' ? '#f43f5e' : isPatient ? '#60a5fa' : '#818cf8'
                }}>
                  {titleInfo.badge}
                </span>
              </div>
              <h1 className="portal-title">{titleInfo.main}</h1>
            </div>
          </div>

          {/* Navigation Menu */}
          <nav style={{ flexGrow: 1, padding: '8px 0' }}>
            <ul className="menu-list">
              <li>
                <NavLink 
                  to="/dashboard" 
                  end
                  className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                >
                  <LayoutDashboard size={19} />
                  Overview
                </NavLink>
              </li>
              
              {isPatient ? (
                <>
                  <li>
                    <NavLink 
                      to="/dashboard/medications" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <Pill size={19} />
                      Medications
                    </NavLink>
                  </li>
                  <li>
                    <NavLink 
                      to="/dashboard/schedule" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <Calendar size={19} />
                      Schedule
                    </NavLink>
                  </li>
                  <li>
                    <NavLink 
                      to="/dashboard/care" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <HeartPulse size={19} />
                      Care Management
                    </NavLink>
                  </li>
                  <li>
                    <NavLink 
                      to="/dashboard/ai-assistant" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <Bot size={19} />
                      AI Assistant
                    </NavLink>
                  </li>
                </>
              ) : (
                <>
                  <li>
                    <NavLink 
                      to="/dashboard/patients" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <Users size={19} />
                      My Patients
                    </NavLink>
                  </li>
                  <li>
                    <NavLink 
                      to="/dashboard/schedule" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <Calendar size={19} />
                      Schedule
                    </NavLink>
                  </li>
                  <li>
                    <NavLink 
                      to="/dashboard/reports" 
                      className={({ isActive }) => `menu-item ${isActive ? `active active-${roleTheme}` : ''}`}
                    >
                      <FileText size={19} />
                      Clinical Reports
                    </NavLink>
                  </li>
                </>
              )}
            </ul>
          </nav>

          {/* User Footer Settings & Logout */}
          <div style={{ padding: '16px 14px', borderTop: '1px solid rgba(255,255,255,0.08)', display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <button className="menu-item" style={{ width: '100%', marginBottom: 0, justifyContent: 'flex-start', borderRadius: '8px' }}>
              <Settings size={18} />
              Settings
            </button>
            <button className="menu-item" onClick={handleLogout} style={{ width: '100%', color: '#f43f5e', justifyContent: 'flex-start', borderRadius: '8px' }}>
              <LogOut size={18} />
              Sign Out
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="main-content">
        {/* Modern Top Header Bar */}
        <header className="topbar">
          <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <button 
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="btn-dynamic btn-outline-neutral"
              style={{
                borderRadius: '8px',
                padding: '8px',
                width: '38px',
                height: '38px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
              title="Toggle sidebar"
            >
              <Menu size={20} />
            </button>
            
            {/* Search Input Bar */}
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', pointerEvents: 'none' }} />
              <input 
                type="text" 
                className="topbar-search-input" 
                placeholder="Search patient records, care plans..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            {/* Notifications Button */}
            <button 
              className="btn-dynamic btn-outline-neutral" 
              style={{ width: '38px', height: '38px', borderRadius: '8px', padding: 0, position: 'relative' }}
              title="Notifications"
            >
              <Bell size={18} />
              <span style={{
                position: 'absolute',
                top: '6px',
                right: '6px',
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                background: '#e11d48',
                border: '2px solid white'
              }}></span>
            </button>

            {/* Profile Pill Badge */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              padding: '6px 14px 6px 8px',
              borderRadius: '99px',
              background: '#f8fafc',
              border: '1px solid var(--card-border)'
            }}>
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                background: user?.role === 'ADMIN' ? '#e11d48' : isPatient ? '#2563eb' : '#4f46e5',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 700,
                fontSize: '13px',
                boxShadow: '0 2px 6px rgba(0,0,0,0.15)'
              }}>
                {getInitials(user?.name)}
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)', lineHeight: 1.2 }}>
                  {user?.name || 'User'}
                </span>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 500 }}>
                  {user?.role || 'Patient'}
                </span>
              </div>
            </div>

            <button 
              onClick={handleLogout}
              className="btn-dynamic btn-outline-neutral"
              style={{ gap: '8px', padding: '8px 16px', fontSize: '13.5px', fontWeight: 600 }}
            >
              <LogOut size={16} color="#e11d48" />
              Logout
            </button>
          </div>
        </header>

        {/* Page Content Outlet */}
        <AnimatePresence mode="wait">
          <motion.div 
            key={location.pathname}
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -15 }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
            style={{ display: 'flex', flexDirection: 'column', gap: '24px', flexGrow: 1, paddingTop: '16px' }}
          >
            {currentOutlet}
          </motion.div>
        </AnimatePresence>
      </main>
    </div>
  );
};

export default DashboardLayout;
