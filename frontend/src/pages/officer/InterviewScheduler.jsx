import React, { useState, useEffect } from 'react';
import { interviewService } from '../../services/interviewService';
import { applicationService } from '../../services/applicationService';
import {
  Calendar,
  Clock,
  Video,
  MapPin,
  PlusCircle,
  CheckCircle,
  ExternalLink,
} from 'lucide-react';

export default function InterviewScheduler() {
  const [interviews, setInterviews] = useState([]);
  const [applications, setApplications] = useState([]);
  const [selectedAppId, setSelectedAppId] = useState('');
  const [roundName, setRoundName] = useState('Technical Round 1 (Coding & DSA)');
  const [interviewDateTime, setInterviewDateTime] = useState('');
  const [mode, setMode] = useState('Virtual (Google Meet)');
  const [location, setLocation] = useState('https://meet.google.com/xyz-demo-interview');
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [interviewsData, appsData] = await Promise.all([
        interviewService.getAllInterviews(),
        applicationService.getAllApplications(),
      ]);
      setInterviews(interviewsData);
      setApplications(appsData);
      if (appsData.length > 0 && !selectedAppId) {
        setSelectedAppId(appsData[0].id);
      }
    } catch (err) {
      console.error('Failed to load scheduler data', err);
    }
  };

  const handleSchedule = async (e) => {
    e.preventDefault();
    if (!selectedAppId || !interviewDateTime) return;

    setSubmitting(true);
    try {
      await interviewService.scheduleInterview({
        applicationId: selectedAppId,
        roundName,
        interviewDateTime,
        mode,
        location,
      });

      setSuccessMsg('Interview round scheduled and candidate notified!');
      await loadData();
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err) {
      console.error('Failed to schedule interview', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Interview Scheduler
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Coordinate campus evaluation rounds, generate Google Meet links or assign placement block conference rooms.
        </p>
      </div>

      {successMsg && (
        <div
          style={{
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(16, 185, 129, 0.15)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            color: '#34d399',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            fontSize: '14px',
          }}
        >
          <CheckCircle size={16} />
          <span>{successMsg}</span>
        </div>
      )}

      <div className="grid-3" style={{ marginBottom: '28px' }}>
        {/* Schedule Form */}
        <div className="card" style={{ gridColumn: 'span 1' }}>
          <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <PlusCircle size={18} color="#3b82f6" />
            <span>Schedule New Round</span>
          </h3>

          <form onSubmit={handleSchedule} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Select Candidate & Drive
              </label>
              <select
                value={selectedAppId}
                onChange={(e) => setSelectedAppId(e.target.value)}
                className="form-select"
                required
              >
                {applications.map((app) => (
                  <option key={app.id} value={app.id}>
                    {app.studentName} — {app.company} ({app.status})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Round Title
              </label>
              <input
                type="text"
                required
                value={roundName}
                onChange={(e) => setRoundName(e.target.value)}
                className="form-input"
              />
            </div>

            <div>
              <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Date & Time
              </label>
              <input
                type="datetime-local"
                required
                value={interviewDateTime}
                onChange={(e) => setInterviewDateTime(e.target.value)}
                className="form-input"
              />
            </div>

            <div>
              <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Evaluation Mode
              </label>
              <select
                value={mode}
                onChange={(e) => setMode(e.target.value)}
                className="form-select"
              >
                <option value="Virtual (Google Meet)">Virtual (Google Meet)</option>
                <option value="Campus In-Person">Campus In-Person</option>
                <option value="Online Coding Platform">Online Coding Assessment</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Meeting URL or Venue Hall
              </label>
              <input
                type="text"
                required
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                className="form-input"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="btn btn-primary"
              style={{ marginTop: '8px' }}
            >
              {submitting ? 'Scheduling...' : 'Dispatch Schedule'}
            </button>
          </form>
        </div>

        {/* Existing Scheduled Rounds */}
        <div className="card" style={{ gridColumn: 'span 2' }}>
          <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '16px' }}>
            Scheduled Interview Roster ({interviews.length})
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {interviews.length === 0 ? (
              <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '30px' }}>
                No interview sessions currently scheduled.
              </div>
            ) : (
              interviews.map((item) => (
                <div
                  key={item.id}
                  style={{
                    padding: '14px 18px',
                    borderRadius: 'var(--radius-md)',
                    background: 'var(--bg-main)',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: '12px',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                      <span style={{ fontWeight: '600', color: '#fff', fontSize: '15px' }}>
                        {item.studentName || 'Candidate'}
                      </span>
                      <span style={{ color: 'var(--text-muted)', fontSize: '12px' }}>for</span>
                      <span style={{ color: '#60a5fa', fontWeight: '600', fontSize: '14px' }}>
                        {item.company}
                      </span>
                    </div>

                    <div style={{ fontSize: '13px', color: '#cbd5e1', marginBottom: '6px' }}>
                      {item.roundName}
                    </div>

                    <div style={{ display: 'flex', gap: '14px', fontSize: '12px', color: 'var(--text-muted)' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Calendar size={12} />
                        {new Date(item.interviewDateTime).toLocaleDateString()} at{' '}
                        {new Date(item.interviewDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                      <span>•</span>
                      <span>{item.mode}: {item.location}</span>
                    </div>
                  </div>

                  <span className="badge badge-interview_scheduled">
                    {item.status}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
