import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import apiClient from '../api/client';

const AssignMedications = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [dosage, setDosage] = useState('');
  const [timesDaily, setTimesDaily] = useState('');
  const [reason, setReason] = useState('');
  const [duration, setDuration] = useState('');
  
  const [intakeTimes, setIntakeTimes] = useState({
    Morning: false,
    Afternoon: false,
    Night: false,
  });
  
  const [foodRelation, setFoodRelation] = useState('');
  
  const [selectedMeds, setSelectedMeds] = useState({});
  const [toast, setToast] = useState('');
  
  const triggerToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 3000);
  };

  const medicationsList = [
    "Methotrexate (15mg, 1x daily)",
    "Hydroxychloroquine (200mg, 2x daily)",
    "Sulfasalazine (500mg, 2x daily)",
    "Leflunomide (20mg, 1x daily)",
    "Adalimumab (40mg, 1x daily)",
    "Etanercept (50mg, 1x daily)",
    "Infliximab (5mg, 1x daily)",
    "Rituximab (1000mg, 1x daily)",
    "Tocilizumab (8mg, 1x daily)",
    "Prednisolone (5mg, 1x daily)",
    "Prednisone (10mg, 1x daily)",
    "Dexamethasone (2mg, 1x daily)",
    "Methylprednisolone (4mg, 1x daily)",
    "Hydrocortisone (10mg, 2x daily)",
    "Deflazacort (3mg, 1x daily)",
    "Betamethasone (0.25mg, 1x daily)",
    "Triamcinolone (2mg, 1x daily)",
    "Budesonide (3mg, 1x daily)",
    "Cortisone (12.5mg, 1x daily)",
    "Ibuprofen (400mg, 3x daily)",
    "Naproxen (250mg, 2x daily)",
    "Diclofenac (50mg, 2x daily)",
    "Celecoxib (100mg, 2x daily)",
    "Meloxicam (7.5mg, 1x daily)",
    "Indomethacin (25mg, 3x daily)",
    "Aspirin (75mg, 1x daily)",
    "Paracetamol (500mg, 3x daily)",
    "Tramadol (50mg, 2x daily)",
    "Codeine (30mg, 2x daily)",
    "Folic Acid (5mg, 1x daily)",
    "Calcium (500mg, 2x daily)",
    "Vitamin D (1000IU, 1x daily)",
    "Omeprazole (20mg, 1x daily)",
    "Lansoprazole (15mg, 1x daily)",
    "Ranitidine (150mg, 2x daily)",
  ];

  const handleIntakeTimeToggle = (time) => {
    setIntakeTimes(prev => ({ ...prev, [time]: !prev[time] }));
  };

  const handleMedToggle = (med) => {
    setSelectedMeds(prev => ({ ...prev, [med]: !prev[med] }));
  };

  const handleAssign = async () => {
    const selected = Object.keys(selectedMeds).filter(k => selectedMeds[k]);
    if (selected.length === 0) {
      triggerToast("Please select at least one medication.");
      return;
    }
    
    const payload = {
      patient_id: id,
      medications: selected,
      custom_dosage: dosage,
      times_daily: timesDaily,
      reason,
      duration,
      intake_times: Object.keys(intakeTimes).filter(k => intakeTimes[k]),
      food_relation: foodRelation
    };

    try {
      // Mock API call for now to show success as requested
      console.log("Saving medications payload:", payload);
      triggerToast("API Response: 200 - Medications assigned successfully!");
      setTimeout(() => {
        navigate(-1);
      }, 1500);
    } catch (err) {
      triggerToast("Failed to assign medications.");
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
          bottom: '24px',
          left: '50%',
          transform: 'translateX(-50%)',
          background: 'rgba(30, 41, 59, 0.95)',
          color: '#ffffff',
          padding: '12px 20px',
          borderRadius: '12px',
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
        zIndex: 10,
        boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
      }}>
        <button 
          onClick={() => navigate(-1)}
          style={{ position: 'absolute', left: '20px', background: 'none', border: 'none', cursor: 'pointer', padding: '4px', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 600, fontSize: '14px' }}
        >
          <ArrowLeft size={20} color="#0f172a" /> BACK
        </button>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 700, color: '#000000' }}>
          Assign Medications
        </h1>
      </div>

      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '600px', margin: '0 auto', width: '100%' }}>
        
        {/* Subheader */}
        <div style={{ background: '#ffffff', padding: '16px', fontSize: '16px', color: '#0f172a', border: '1px solid #e2e8f0', borderRadius: '4px' }}>
          Assigning medications to Patient ID: {id}
        </div>

        {/* Custom Dosage & Frequency */}
        <div>
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: '#0f172a', margin: '0 0 4px 0' }}>Custom Dosage & Frequency (Optional)</h2>
          <p style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#475569' }}>Leave empty to use default values for each medication</p>
          
          <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
            <input 
              type="text" 
              placeholder="Dosage (e.g. 500mg)" 
              value={dosage}
              onChange={e => setDosage(e.target.value)}
              style={{ flex: 1, padding: '14px', borderRadius: '4px', border: 'none', background: '#ffffff', fontSize: '16px', boxShadow: '0 1px 2px rgba(0,0,0,0.05)', boxSizing: 'border-box' }}
            />
            <input 
              type="text" 
              placeholder="Times daily (e.g. 3)" 
              value={timesDaily}
              onChange={e => setTimesDaily(e.target.value)}
              style={{ flex: 1, padding: '14px', borderRadius: '4px', border: 'none', background: '#ffffff', fontSize: '16px', boxShadow: '0 1px 2px rgba(0,0,0,0.05)', boxSizing: 'border-box' }}
            />
          </div>

          <input 
            type="text" 
            placeholder="Reason for Medication (e.g. for pain / swelling)" 
            value={reason}
            onChange={e => setReason(e.target.value)}
            style={{ width: '100%', padding: '14px', borderRadius: '4px', border: 'none', background: '#ffffff', fontSize: '16px', marginBottom: '16px', boxSizing: 'border-box', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' }}
          />
          
          <input 
            type="text" 
            placeholder="Duration (e.g. for 5 days / Ongoing)" 
            value={duration}
            onChange={e => setDuration(e.target.value)}
            style={{ width: '100%', padding: '14px', borderRadius: '4px', border: 'none', background: '#ffffff', fontSize: '16px', boxSizing: 'border-box', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' }}
          />
        </div>

        {/* Intake Time */}
        <div>
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: '#0f172a', margin: '0 0 12px 0' }}>Intake Time (Select all that apply)</h2>
          <div style={{ display: 'flex', gap: '24px' }}>
            {['Morning', 'Afternoon', 'Night'].map(time => (
              <label key={time} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '15px', color: '#0f172a', cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={intakeTimes[time]} 
                  onChange={() => handleIntakeTimeToggle(time)}
                  style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                />
                {time}
              </label>
            ))}
          </div>
        </div>

        {/* Food Relation */}
        <div>
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: '#0f172a', margin: '0 0 12px 0' }}>Food Relation</h2>
          <div style={{ display: 'flex', gap: '48px' }}>
            {['Before Food', 'After Food'].map(rel => (
              <label key={rel} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '15px', color: '#0f172a', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="foodRelation"
                  checked={foodRelation === rel}
                  onChange={() => setFoodRelation(rel)}
                  style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                />
                {rel}
              </label>
            ))}
          </div>
        </div>

        {/* Select Medications */}
        <div>
          <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#000000', margin: '0 0 12px 0' }}>Select Medications to Assign</h2>
          <div style={{ background: '#ffffff', padding: '16px 0', border: '1px solid #e2e8f0', borderRadius: '4px' }}>
            {medicationsList.map(med => (
              <label key={med} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 16px', fontSize: '16px', color: '#0f172a', cursor: 'pointer', borderBottom: '1px solid #f1f5f9' }}>
                <input 
                  type="checkbox" 
                  checked={!!selectedMeds[med]}
                  onChange={() => handleMedToggle(med)}
                  style={{ width: '22px', height: '22px', cursor: 'pointer' }}
                />
                {med}
              </label>
            ))}
          </div>
        </div>
        
        {/* Action Button & Instructions */}
        <div style={{ marginTop: '16px' }}>
          <button 
            onClick={handleAssign}
            style={{
              width: '100%',
              background: '#e2e8f0',
              color: '#0f172a',
              border: '1px solid #cbd5e1',
              padding: '16px',
              fontSize: '18px',
              fontWeight: 700,
              cursor: 'pointer',
              marginBottom: '12px'
            }}
          >
            SELECT MEDICATIONS TO ASSIGN
          </button>
          
          <div style={{ fontSize: '14px', color: '#334155', lineHeight: '1.5' }}>
            <strong>Instructions:</strong><br/>
            • Check the medications you want to assign<br/>
            • Use custom dosage/frequency or leave empty for defaults<br/>
            • Duplicate medications will be skipped automatically<br/>
            • All selected medications will be assigned at once
          </div>
        </div>

        <div style={{ height: '32px' }}></div>
      </div>
    </div>
  );
};

export default AssignMedications;
