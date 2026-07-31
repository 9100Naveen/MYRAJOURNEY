import React, { useEffect, useState, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { Plus, ArrowLeft } from 'lucide-react';

const ClinicalReports = () => {
  const { user } = useAuth();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Upload State
  const [showUpload, setShowUpload] = useState(false);
  const [reportName, setReportName] = useState('');
  const [uploadDate, setUploadDate] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const fileInputRef = useRef(null);

  const isPatient = user?.role === 'PATIENT';

  const fetchReports = async () => {
    try {
      setLoading(true);
      const endpoint = '/reports';
      const response = await apiClient.get(endpoint);
      
      if (response.data.success && response.data.data) {
        setReports(response.data.data);
      } else {
        setReports([]);
      }
    } catch (err) {
      console.error("Live backend fetch failed:", err);
      setReports([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, [isPatient]);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reportName || !selectedFile) {
      alert("Please provide a report name and select a file.");
      return;
    }
    
    try {
      setIsSubmitting(true);
      const formData = new FormData();
      formData.append('title', reportName);
      formData.append('file', selectedFile);
      const token = localStorage.getItem('jwt_token');
      const res = await apiClient.post('/reports', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      const data = res.data;
      
      if (data.success) {
        setShowUpload(false);
        setReportName('');
        setUploadDate('');
        setSelectedFile(null);
        fetchReports(); // Refresh list
      } else {
        alert(data.message || "Failed to upload.");
      }
    } catch (err) {
      console.error("Upload error details:", err);
      alert(err.message || "Error uploading file.");
    } finally {
      setIsSubmitting(false);
    }
  };

  // -----------------------------------------
  // UPLOAD VIEW (Screenshot 2)
  // -----------------------------------------
  if (showUpload) {
    return (
      <div style={{ background: '#ffffff', minHeight: '100vh', padding: '16px' }}>
        {/* Header / Back */}
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '40px' }}>
          <button 
            onClick={() => setShowUpload(false)}
            style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center' }}
          >
            {/* The screenshot doesn't explicitly show a back button, but we need one for navigation */}
            <ArrowLeft size={28} color="#000000" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '600px', margin: '0 auto' }}>
          {/* Report Name Input */}
          <input 
            type="text" 
            placeholder="Report Name" 
            value={reportName}
            onChange={(e) => setReportName(e.target.value)}
            style={{
              width: '100%',
              padding: '16px',
              background: '#e5e5e5', 
              border: 'none',
              borderRadius: '2px',
              fontSize: '16px',
              color: '#000'
            }}
          />

          {/* Date & Time Input */}
          <input 
            type="text"
            onFocus={(e) => e.target.type = 'datetime-local'}
            onBlur={(e) => { if (!e.target.value) e.target.type = 'text'; }}
            placeholder="Select Upload Date & Time" 
            value={uploadDate}
            onChange={(e) => setUploadDate(e.target.value)}
            style={{
              width: '100%',
              padding: '16px',
              background: '#ffffff',
              border: '1px solid #999',
              borderRadius: '4px',
              fontSize: '16px',
              color: '#000'
            }}
          />

          {/* Drag & Drop Area */}
          <div 
            onClick={() => fileInputRef.current?.click()}
            style={{
              width: '100%',
              height: '180px',
              border: '1px solid #999',
              borderRadius: '4px',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'center',
              alignItems: 'center',
              cursor: 'pointer',
              background: '#ffffff'
            }}
          >
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#999" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 19V5M5 12l7-7 7 7"/>
              <path d="M5 19h14"/>
            </svg>
            <p style={{ margin: '16px 0 0 0', fontSize: '15px', color: '#000000' }}>
              {selectedFile ? selectedFile.name : 'Drag & Drop file here or tap to select'}
            </p>
            <input 
              type="file" 
              ref={fileInputRef} 
              onChange={handleFileChange} 
              style={{ display: 'none' }} 
            />
          </div>

          {/* Submit Button */}
          <button 
            type="submit"
            disabled={isSubmitting}
            style={{
              width: '100%',
              padding: '16px',
              background: '#5900e6',
              color: '#ffffff',
              border: 'none',
              borderRadius: '2px',
              fontSize: '15px',
              fontWeight: 600,
              letterSpacing: '1px',
              marginTop: '16px',
              cursor: isSubmitting ? 'not-allowed' : 'pointer'
            }}
          >
            {isSubmitting ? 'SUBMITTING...' : 'SUBMIT REPORT'}
          </button>
        </form>
      </div>
    );
  }

  // -----------------------------------------
  // LIST VIEW (Screenshot 1)
  // -----------------------------------------
  return (
    <div style={{ background: '#f5f5f5', minHeight: '100vh', padding: '16px', display: 'flex', flexDirection: 'column', maxWidth: '600px', margin: '0 auto' }}>
      
      {/* Header / Add Button */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '24px' }}>
        <button 
          onClick={() => setShowUpload(true)}
          style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex' }}
        >
          <Plus size={32} color="#7bb93b" strokeWidth={3} />
        </button>
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>Loading reports...</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {reports.length > 0 ? reports.map(report => (
            <div 
              key={report.id}
              style={{
                background: '#ffffff',
                borderRadius: '12px',
                padding: '16px',
                display: 'flex',
                gap: '16px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                alignItems: 'flex-start'
              }}
            >
              {/* DP Icon */}
              <div style={{
                background: '#e23d3d', // red
                borderRadius: '4px',
                width: '36px',
                height: '44px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                position: 'relative'
              }}>
                <span style={{ color: '#ffffff', fontWeight: 800, fontSize: '13px' }}>DP</span>
                {/* Folded corner effect */}
                <div style={{ position: 'absolute', top: 0, right: 0, width: 0, height: 0, borderTop: '12px solid #ffffff', borderLeft: '12px solid transparent' }}></div>
              </div>

              {/* Report Info */}
              <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column', gap: '2px', overflow: 'hidden' }}>
                <h3 style={{ margin: 0, fontSize: '17px', fontWeight: 800, color: '#000000' }}>
                  {report.title || report.report_title || 'Report'}
                </h3>
                <span style={{ 
                  fontSize: '14px', 
                  color: '#000000',
                  textShadow: '0 0 1px rgba(0,0,0,0.4)', // Creates the slightly outlined look
                  marginTop: '4px'
                }}>
                  {report.created_at || '2026-05-11 12:21:22'}
                </span>
                <a 
                  href={report.file_url} 
                  target="_blank" 
                  rel="noreferrer"
                  style={{ 
                    fontSize: '13px', 
                    color: '#333333', 
                    textDecoration: 'none',
                    wordBreak: 'break-all',
                    lineHeight: '1.4',
                    marginTop: '2px'
                  }}
                >
                  {report.file_url || 'http://14.139.187.229:8081/uploads/reports/dummy.pdf'}
                </a>
              </div>

              {/* Pending Badge */}
              <div style={{ display: 'flex', alignItems: 'center', alignSelf: 'center' }}>
                <span style={{
                  background: '#f59e0b', // orange/yellow
                  color: '#ffffff',
                  padding: '4px 12px',
                  borderRadius: '16px',
                  fontSize: '13px',
                  fontWeight: 600,
                  textShadow: '-1px -1px 0 #000, 1px -1px 0 #000, -1px 1px 0 #000, 1px 1px 0 #000', // Black outline on text
                }}>
                  Pending
                </span>
              </div>
            </div>
          )) : (
            <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
              No clinical reports found.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ClinicalReports;
