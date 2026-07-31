import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../api/client';
import { Sparkles, Send, User, Loader2 } from 'lucide-react';

const AIAssistant = () => {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  // Auto-scroll to bottom
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await apiClient.get('/chatbot/history');
        if (response.data.success && response.data.data) {
          // Format from backend: { user_message, bot_response, created_at }
          // We need to flatten this into a chat list
          const formattedHistory = [];
          response.data.data.forEach(item => {
            formattedHistory.push({ type: 'user', text: item.user_message, time: item.created_at });
            formattedHistory.push({ type: 'bot', text: item.bot_response, time: item.created_at });
          });
          setMessages(formattedHistory);
        }
      } catch (err) {
        console.error("Failed to load chat history:", err);
      }
    };

    fetchHistory();
    
    // Add a welcome message if no history
    setMessages([{ 
      type: 'bot', 
      text: `Hello ${user?.name || 'there'}! I'm MyRA, your AI health assistant. How are you feeling today?` 
    }]);
  }, [user]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!inputMessage.trim()) return;

    const userText = inputMessage.trim();
    setInputMessage('');
    
    // Add user message to UI immediately
    setMessages(prev => [...prev, { type: 'user', text: userText }]);
    setIsLoading(true);

    try {
      const response = await apiClient.post('/chatbot/chat', { message: userText });
      
      if (response.data.success) {
        const botResponse = response.data.data.response || response.data.data.message;
        setMessages(prev => [...prev, { type: 'bot', text: botResponse }]);
      } else {
        setMessages(prev => [...prev, { type: 'bot', text: "I'm having trouble connecting right now. Please try again later." }]);
      }
    } catch (err) {
      console.error("Chat error:", err);
      setMessages(prev => [...prev, { type: 'bot', text: "Sorry, an error occurred while processing your request." }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 140px)', gap: '16px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{
          width: '48px',
          height: '48px',
          borderRadius: 'var(--radius-md)',
          background: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#ffffff',
          boxShadow: '0 4px 12px rgba(14, 165, 233, 0.2)'
        }}>
          <Sparkles size={24} />
        </div>
        <div>
          <h1 style={{ fontSize: '28px', fontWeight: 800, margin: '0 0 4px 0', color: '#0f172a' }}>AI Health Assistant</h1>
          <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '14px' }}>
            Ask questions about your recovery, exercises, or medications.
          </p>
        </div>
      </div>

      <div style={{ 
        flex: 1, 
        background: '#ffffff', 
        borderRadius: 'var(--radius-lg)', 
        border: '1px solid rgba(0,0,0,0.05)',
        boxShadow: '0 8px 30px rgba(0,0,0,0.02)',
        display: 'flex', 
        flexDirection: 'column',
        overflow: 'hidden'
      }}>
        {/* Chat Messages Area */}
        <div style={{ 
          flex: 1, 
          overflowY: 'auto', 
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px'
        }}>
          {messages.map((msg, idx) => (
            <div 
              key={idx} 
              style={{ 
                display: 'flex', 
                gap: '12px',
                alignSelf: msg.type === 'user' ? 'flex-end' : 'flex-start',
                flexDirection: msg.type === 'user' ? 'row-reverse' : 'row',
                maxWidth: '80%'
              }}
            >
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: msg.type === 'user' ? '#f1f5f9' : 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
                color: msg.type === 'user' ? '#64748b' : '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0
              }}>
                {msg.type === 'user' ? <User size={18} /> : <Sparkles size={18} />}
              </div>
              
              <div style={{
                background: msg.type === 'user' ? '#eff6ff' : '#f8fafc',
                color: msg.type === 'user' ? '#1e3a8a' : '#334155',
                padding: '12px 16px',
                borderRadius: 'var(--radius-md)',
                borderBottomRightRadius: msg.type === 'user' ? '4px' : '16px',
                borderBottomLeftRadius: msg.type === 'bot' ? '4px' : '16px',
                fontSize: '15px',
                lineHeight: 1.5,
                border: msg.type === 'bot' ? '1px solid rgba(0,0,0,0.05)' : 'none'
              }}>
                {msg.text}
              </div>
            </div>
          ))}
          
          {isLoading && (
            <div style={{ display: 'flex', gap: '12px', alignSelf: 'flex-start' }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0
              }}>
                <Sparkles size={18} />
              </div>
              <div style={{
                background: '#f8fafc',
                padding: '12px 16px',
                borderRadius: 'var(--radius-md)',
                borderBottomLeftRadius: '4px',
                display: 'flex',
                alignItems: 'center',
                border: '1px solid rgba(0,0,0,0.05)'
              }}>
                <Loader2 size={18} className="animate-spin" color="#0ea5e9" />
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Input Area */}
        <div style={{ 
          padding: '16px 24px', 
          borderTop: '1px solid rgba(0,0,0,0.05)',
          background: '#f8fafc'
        }}>
          <form 
            onSubmit={handleSendMessage}
            style={{ 
              display: 'flex', 
              gap: '12px',
              background: '#ffffff',
              padding: '8px',
              borderRadius: 'var(--radius-md)',
              border: '1px solid rgba(0,0,0,0.1)',
              boxShadow: '0 2px 10px rgba(0,0,0,0.02)'
            }}
          >
            <input 
              type="text" 
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Ask me anything..." 
              style={{
                flex: 1,
                border: 'none',
                background: 'transparent',
                padding: '8px 16px',
                fontSize: '15px',
                outline: 'none',
                color: '#0f172a'
              }}
              disabled={isLoading}
            />
            <button 
              type="submit"
              disabled={isLoading || !inputMessage.trim()}
              style={{
                width: '44px',
                height: '44px',
                borderRadius: 'var(--radius-md)',
                background: inputMessage.trim() ? '#0ea5e9' : '#e2e8f0',
                color: inputMessage.trim() ? '#ffffff' : '#94a3b8',
                border: 'none',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: inputMessage.trim() ? 'pointer' : 'not-allowed',
                transition: 'all 0.2s'
              }}
            >
              <Send size={18} style={{ marginLeft: '2px' }} />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AIAssistant;
