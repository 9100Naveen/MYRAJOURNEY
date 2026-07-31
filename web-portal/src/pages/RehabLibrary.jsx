import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { Activity, Play, CheckCircle2, Search } from 'lucide-react';

const RehabLibrary = () => {
  const { user } = useAuth();
  const [exercises, setExercises] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeVideo, setActiveVideo] = useState(null);

  const isPatient = user?.role === 'PATIENT';

  const markAsCompleted = (id) => {
    // Update local state
    setExercises(exercises.map(ex => ex.id === id ? { ...ex, status: 'COMPLETED' } : ex));
    
    // In a full implementation, this would also POST to the backend to mark it completed.
    
    setActiveVideo(null);
  };

  useEffect(() => {
    const fetchExercises = async () => {
      try {
        const endpoint = isPatient ? '/exercise-assignments/patient' : '/exercises';
        const response = await apiClient.get(endpoint);
        
        let loadedExercises = [];
        if (response.data.success && response.data.data) {
          loadedExercises = response.data.data;
        }

        // Fallback to local storage if empty (for demo/offline mode)
        if (loadedExercises.length === 0) {
          const stored = localStorage.getItem(`mock_rehab_exercises_${user?.id}`);
          if (stored) {
            try {
              const names = JSON.parse(stored);
              loadedExercises = names.map((name, idx) => ({
                id: 2000 + idx,
                name: name,
                category: 'REHAB',
                difficulty_level: 'BEGINNER',
                status: 'PENDING'
              }));
            } catch (e) {
              console.error("Failed to parse mock data");
            }
          }
        }

        setExercises(loadedExercises);
      } catch (err) {
        console.error("Failed to fetch exercises:", err);
        
        // Fallback to local storage on error
        const stored = localStorage.getItem(`mock_rehab_exercises_${user?.id}`);
        if (stored) {
          try {
            const names = JSON.parse(stored);
            setExercises(names.map((name, idx) => ({
              id: 2000 + idx,
              name: name,
              category: 'REHAB',
              difficulty_level: 'BEGINNER',
              status: 'PENDING'
            })));
          } catch (e) {}
        }
      } finally {
        setLoading(false);
      }
    };

    fetchExercises();
  }, [isPatient]);

  const filteredExercises = exercises.filter(ex => {
    const nameMatch = ex.name ? ex.name.toLowerCase().includes(searchQuery.toLowerCase()) : false;
    const catMatch = ex.category ? ex.category.toLowerCase().includes(searchQuery.toLowerCase()) : false;
    return nameMatch || catMatch;
  });

  // Calculate progress
  const completedCount = exercises.filter(ex => ex.status === 'COMPLETED').length;
  const progressPercent = exercises.length > 0 ? Math.round((completedCount / exercises.length) * 100) : 0;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button onClick={() => window.history.back()} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, display: 'flex', alignItems: 'center' }}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#000000" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
          </button>
          <div>
            <h1 style={{ fontSize: '32px', fontWeight: 800, margin: '0 0 8px 0', color: '#0f172a' }}>Rehab Exercises</h1>
            <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '15px' }}>
              Your prescribed rehabilitation plan.
            </p>
          </div>
        </div>
      </div>

      {/* Progress Bar Card */}
      {isPatient && (
        <div style={{ background: '#ffffff', borderRadius: '16px', padding: '20px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
            <span style={{ fontSize: '15px', fontWeight: 800, color: '#1e293b' }}>Overall Progress: {progressPercent}%</span>
          </div>
          <div style={{ height: '8px', background: '#e2e8f0', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{ height: '100%', width: `${progressPercent}%`, background: '#148074', borderRadius: '4px', transition: 'width 0.3s ease' }}></div>
          </div>
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading exercises...</div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '24px' }}>
          {filteredExercises.length > 0 ? filteredExercises.map(ex => (
            <div 
              key={ex.id}
              className="hover-card-effects"
              style={{
                background: '#ffffff',
                borderRadius: '16px',
                padding: '24px',
                border: '1px solid var(--card-border)',
                display: 'flex',
                flexDirection: 'column',
                gap: '16px'
              }}
            >
              {/* Category Tag */}
              <div>
                <span style={{ 
                  background: '#e0f2fe', 
                  color: '#0f172a', 
                  padding: '4px 8px', 
                  borderRadius: '4px', 
                  fontSize: '11px', 
                  fontWeight: 800,
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px'
                }}>
                  {ex.category || 'HAND'}
                </span>
              </div>

              {/* Title & Description */}
              <div>
                <h3 style={{ fontSize: '20px', fontWeight: 800, margin: '0 0 12px 0', color: '#0f172a' }}>{ex.name}</h3>
                <p style={{ margin: 0, color: '#334155', fontSize: '15px', lineHeight: 1.4 }}>
                  {ex.description || (ex.instructions && ex.instructions[0]) || 'Follow the instructions carefully.'}
                </p>
              </div>

              {/* Benefits */}
              <div>
                <span style={{ fontWeight: 800, fontSize: '15px', color: '#0f172a' }}>Benefits:</span>
                <p style={{ margin: '6px 0 0 0', fontSize: '15px', color: '#334155' }}>
                  • {ex.ra_benefits ? (Array.isArray(ex.ra_benefits) ? ex.ra_benefits[0] : ex.ra_benefits) : 'Improves flexibility'}
                </p>
              </div>

              {/* Divider */}
              <div style={{ height: '1px', background: '#f1f5f9', margin: '4px 0' }}></div>

              {/* Sets, Reps, Watch Video */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', gap: '48px' }}>
                  <div>
                    <div style={{ fontSize: '10px', fontWeight: 800, color: '#000000', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '4px', textShadow: '0 0 1px rgba(0,0,0,0.3)' }}>SETS</div>
                    <div style={{ fontSize: '20px', fontWeight: 800, color: '#000000' }}>{ex.sets || 3}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '10px', fontWeight: 800, color: '#000000', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '4px', textShadow: '0 0 1px rgba(0,0,0,0.3)' }}>REPS</div>
                    <div style={{ fontSize: '20px', fontWeight: 800, color: '#000000' }}>{ex.reps || 10}</div>
                  </div>
                </div>
                
                <button 
                  onClick={() => setActiveVideo(ex)}
                  style={{ 
                    background: 'none', 
                    border: 'none', 
                    color: '#000000', 
                    fontWeight: 800, 
                    fontSize: '15px',
                    cursor: 'pointer',
                    padding: 0,
                    letterSpacing: '0.5px'
                  }}
                >
                  Watch Video
                </button>
              </div>

              {/* Action Button */}
              <button 
                onClick={() => markAsCompleted(ex.id)}
                disabled={ex.status === 'COMPLETED'}
                style={{
                  marginTop: '12px',
                  width: '100%',
                  padding: '16px',
                  borderRadius: '24px',
                  border: 'none',
                  background: ex.status === 'COMPLETED' ? '#69a69b' : '#69a69b',
                  color: '#ffffff',
                  fontSize: '16px',
                  fontWeight: 600,
                  cursor: ex.status === 'COMPLETED' ? 'default' : 'pointer',
                  opacity: ex.status === 'COMPLETED' ? 0.9 : 1,
                  letterSpacing: '0.5px'
                }}
              >
                {ex.status === 'COMPLETED' ? 'Completed' : 'Completed'}
              </button>

            </div>
          )) : (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
              No exercises found.
            </div>
          )}
        </div>
      )}

      {/* Dynamic Video Player Modal */}
      {activeVideo && (
        <div style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(15, 23, 42, 0.8)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
          padding: '20px'
        }}>
          <div style={{
            background: '#ffffff',
            borderRadius: 'var(--radius-lg)',
            width: '100%',
            maxWidth: '800px',
            overflow: 'hidden',
            boxShadow: '0 25px 50px -12px rgba(0,0,0,0.5)'
          }}>
            {/* Video Placeholder Area */}
            <div style={{
              width: '100%',
              aspectRatio: '16/9',
              background: '#000000',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexDirection: 'column',
              color: '#ffffff'
            }}>
              <Play size={48} color="rgba(255,255,255,0.8)" style={{ marginBottom: '16px' }} />
              <p style={{ fontSize: '16px', color: 'rgba(255,255,255,0.7)' }}>Simulating Native Video Player...</p>
            </div>
            
            {/* Video Controls & Information */}
            <div style={{ padding: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h2 style={{ margin: '0 0 4px 0', fontSize: '22px', fontWeight: 800, color: '#0f172a' }}>
                  {activeVideo.name}
                </h2>
                <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '15px' }}>
                  {activeVideo.target_muscle_group} • {activeVideo.difficulty_level}
                </p>
              </div>
              
              <div style={{ display: 'flex', gap: '12px' }}>
                <button 
                  onClick={() => setActiveVideo(null)}
                  style={{
                    padding: '12px 20px',
                    borderRadius: 'var(--radius-md)',
                    border: 'none',
                    background: '#f1f5f9',
                    color: '#475569',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  Close
                </button>
                {isPatient && activeVideo.status !== 'COMPLETED' && (
                  <button 
                    onClick={() => markAsCompleted(activeVideo.id)}
                    style={{
                      padding: '12px 24px',
                      borderRadius: 'var(--radius-md)',
                      border: 'none',
                      background: '#10b981',
                      color: '#ffffff',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      cursor: 'pointer'
                    }}
                  >
                    <CheckCircle2 size={18} />
                    Mark Completed
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RehabLibrary;
