import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { interviewService } from '../../services/interviewService';
import {
  Calendar,
  Clock,
  Video,
  MapPin,
  ExternalLink,
  BookOpen,
  CheckCircle,
  HelpCircle,
} from 'lucide-react';

export default function Interviews() {
  const { user } = useAuth();
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(true);

  const studentId = user?.id || 1;

  useEffect(() => {
    async function loadInterviews() {
      try {
        const data = await interviewService.getInterviewsByStudent(studentId);
        setInterviews(data);
      } catch (err) {
        console.error('Failed to load interviews', err);
      } finally {
        setLoading(false);
      }
    }
    loadInterviews();
  }, [user]);

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading interview schedule...</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Interviews Hub
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Review upcoming technical and HR interview rounds, joining links, and preparation recommendations.
        </p>
      </div>

      <div className="grid-3" style={{ marginBottom: '28px' }}>
        {interviews.length === 0 ? (
          <div className="card" style={{ gridColumn: 'span 3', textAlign: 'center', padding: '40px' }}>
            <Calendar size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
            <h3 style={{ fontSize: '16px', fontWeight: '600', color: '#fff', marginBottom: '6px' }}>
              No Interviews Scheduled Yet
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              When a recruiter shortlists your profile and schedules a round, the details will appear here.
            </p>
          </div>
        ) : (
          interviews.map((item) => {
            const dateObj = new Date(item.interviewDateTime);
            const isVirtual = item.mode.toLowerCase().includes('virtual') || item.mode.toLowerCase().includes('meet');

            return (
              <div key={item.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                  <div>
                    <h3 style={{ fontSize: '17px', fontWeight: '700', color: '#fff' }}>
                      {item.company}
                    </h3>
                    <div style={{ fontSize: '13px', color: '#93c5fd' }}>{item.role}</div>
                  </div>
                  <span className="badge badge-interview_scheduled">
                    {item.status}
                  </span>
                </div>

                <div
                  style={{
                    background: 'var(--bg-main)',
                    padding: '12px 14px',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-subtle)',
                    marginBottom: '14px',
                  }}
                >
                  <div style={{ fontSize: '13px', fontWeight: '600', color: '#f8fafc', marginBottom: '6px' }}>
                    {item.roundName}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                    <Calendar size={13} />
                    <span>{dateObj.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-secondary)' }}>
                    <Clock size={13} />
                    <span>{dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} IST</span>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                  {isVirtual ? <Video size={15} color="#38bdf8" /> : <MapPin size={15} color="#fbbf24" />}
                  <span>{item.mode}: <strong>{item.location}</strong></span>
                </div>

                <div style={{ marginTop: 'auto' }}>
                  {isVirtual && item.location.startsWith('http') ? (
                    <a
                      href={item.location}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-primary btn-sm"
                      style={{ width: '100%', gap: '6px' }}
                    >
                      <span>Join Meeting Link</span>
                      <ExternalLink size={13} />
                    </a>
                  ) : (
                    <button
                      disabled
                      className="btn btn-secondary btn-sm"
                      style={{ width: '100%', cursor: 'default' }}
                    >
                      Venue: Campus Placement Block
                    </button>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Quick Preparation Toolkit */}
      <div className="card">
        <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BookOpen size={18} color="#3b82f6" />
          <span>Interview Preparation Checklist</span>
        </h3>
        <div className="grid-3">
          <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
            <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px', marginBottom: '6px' }}>
              1. Core Computer Science
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Review Data Structures, Algorithms (Trees, Graphs, DP), DBMS normalization, and OOP principles.
            </p>
          </div>
          <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
            <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px', marginBottom: '6px' }}>
              2. System Design & Projects
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Be ready to explain architecture decisions, database schema, and challenges faced in your capstone project.
            </p>
          </div>
          <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
            <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px', marginBottom: '6px' }}>
              3. Behavioral & STAR Method
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Formulate answers using Situation, Task, Action, and Result for managerial and HR rounds.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
