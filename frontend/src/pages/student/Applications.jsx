import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { applicationService } from '../../services/applicationService';
import {
  FileText,
  Calendar,
  Building,
  CheckCircle2,
  Clock,
  ArrowRight,
  Filter,
} from 'lucide-react';

export default function Applications() {
  const { user } = useAuth();
  const [applications, setApplications] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  const studentId = user?.id || 1;

  useEffect(() => {
    async function loadApplications() {
      try {
        const apps = await applicationService.getApplicationsByStudent(studentId);
        setApplications(apps);
      } catch (err) {
        console.error('Failed to load applications', err);
      } finally {
        setLoading(false);
      }
    }
    loadApplications();
  }, [user]);

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'SELECTED': return 'badge-selected';
      case 'SHORTLISTED': return 'badge-shortlisted';
      case 'INTERVIEW_SCHEDULED': return 'badge-interview_scheduled';
      case 'REJECTED': return 'badge-rejected';
      case 'APPLIED':
      default: return 'badge-applied';
    }
  };

  const getStepState = (currentStatus, stepName) => {
    const order = ['APPLIED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'SELECTED'];
    const currentIndex = order.indexOf(currentStatus);
    const stepIndex = order.indexOf(stepName);

    if (currentStatus === 'REJECTED') {
      if (stepName === 'APPLIED') return 'completed';
      return 'rejected';
    }

    if (currentIndex > stepIndex) return 'completed';
    if (currentIndex === stepIndex) return 'current';
    return 'pending';
  };

  const filteredApps = applications.filter((app) => {
    if (filter === 'ALL') return true;
    if (filter === 'ACTIVE') return app.status !== 'REJECTED' && app.status !== 'SELECTED';
    return app.status === filter;
  });

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading application status...</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          My Applications
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Track recruitment stages, interview invitations, and status milestones in real-time.
        </p>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px', flexWrap: 'wrap' }}>
        {[
          { key: 'ALL', label: `All (${applications.length})` },
          { key: 'ACTIVE', label: 'Active Pipeline' },
          { key: 'SHORTLISTED', label: 'Shortlisted' },
          { key: 'INTERVIEW_SCHEDULED', label: 'Interviews' },
          { key: 'SELECTED', label: 'Selected / Offers' },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`btn btn-sm ${filter === tab.key ? 'btn-primary' : 'btn-secondary'}`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Applications List */}
      {filteredApps.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <FileText size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
          <h3 style={{ fontSize: '16px', fontWeight: '600', color: '#fff', marginBottom: '6px' }}>
            No applications found
          </h3>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            You haven't submitted any applications under this category yet.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {filteredApps.map((app) => (
            <div key={app.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '16px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#fff' }}>
                      {app.company}
                    </h3>
                    <span className={`badge ${getStatusBadgeClass(app.status)}`}>
                      {app.status.replace('_', ' ')}
                    </span>
                  </div>
                  <div style={{ fontSize: '14px', color: '#93c5fd', fontWeight: '500' }}>
                    {app.role}
                  </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '16px', fontWeight: '700', color: '#34d399' }}>
                    ₹{app.packageLpa} LPA
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Applied: {new Date(app.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </div>

              {/* Status Visualizer Pipeline */}
              <div style={{ background: 'var(--bg-main)', padding: '16px 20px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                <div style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-muted)', marginBottom: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Recruitment Progression Pipeline
                </div>

                <div className="pipeline-steps">
                  <div className={`pipeline-step ${getStepState(app.status, 'APPLIED')}`}>
                    <div className="pipeline-dot" />
                    <span>Application Submitted</span>
                  </div>
                  <div className="pipeline-line" />

                  <div className={`pipeline-step ${getStepState(app.status, 'SHORTLISTED')}`}>
                    <div className="pipeline-dot" />
                    <span>Resume Shortlisted</span>
                  </div>
                  <div className="pipeline-line" />

                  <div className={`pipeline-step ${getStepState(app.status, 'INTERVIEW_SCHEDULED')}`}>
                    <div className="pipeline-dot" />
                    <span>Interview Scheduled</span>
                  </div>
                  <div className="pipeline-line" />

                  <div className={`pipeline-step ${getStepState(app.status, 'SELECTED')}`}>
                    <div className="pipeline-dot" />
                    <span>{app.status === 'REJECTED' ? 'Not Selected' : 'Final Selection'}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
