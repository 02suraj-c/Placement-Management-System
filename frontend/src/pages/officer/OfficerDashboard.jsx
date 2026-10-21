import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { driveService } from '../../services/driveService';
import { applicationService } from '../../services/applicationService';
import { interviewService } from '../../services/interviewService';
import {
  Building2,
  Users,
  Calendar,
  CheckCircle2,
  TrendingUp,
  PlusCircle,
  ArrowRight,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

export default function OfficerDashboard() {
  const [drives, setDrives] = useState([]);
  const [applications, setApplications] = useState([]);
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [drivesData, appsData, interviewsData] = await Promise.all([
          driveService.getAllDrives(),
          applicationService.getAllApplications(),
          interviewService.getAllInterviews(),
        ]);
        setDrives(drivesData);
        setApplications(appsData);
        setInterviews(interviewsData);
      } catch (err) {
        console.error('Failed to load placement officer data', err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  const totalSelected = applications.filter((a) => a.status === 'SELECTED').length;
  const totalShortlisted = applications.filter((a) => a.status === 'SHORTLISTED').length;

  const chartData = [
    { name: 'Applied', count: applications.filter((a) => a.status === 'APPLIED').length, fill: '#38bdf8' },
    { name: 'Shortlisted', count: totalShortlisted, fill: '#c084fc' },
    { name: 'Interviewing', count: applications.filter((a) => a.status === 'INTERVIEW_SCHEDULED').length, fill: '#fbbf24' },
    { name: 'Offers / Selected', count: totalSelected, fill: '#34d399' },
  ];

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading placement officer portal...</div>;
  }

  return (
    <div>
      {/* Hero / Header */}
      <div
        style={{
          background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.12), rgba(59, 130, 246, 0.1))',
          border: '1px solid rgba(16, 185, 129, 0.25)',
          borderRadius: 'var(--radius-xl)',
          padding: '28px',
          marginBottom: '28px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '20px',
        }}
      >
        <div>
          <span
            style={{
              fontSize: '12px',
              fontWeight: '700',
              padding: '3px 10px',
              borderRadius: 'var(--radius-full)',
              background: 'rgba(16, 185, 129, 0.2)',
              color: '#34d399',
              display: 'inline-block',
              marginBottom: '8px',
            }}
          >
            Training & Placement Cell (TPO)
          </span>
          <h2 style={{ fontSize: '24px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
            Campus Placement Command Center
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', maxWidth: '650px' }}>
            Coordinate on-campus recruitment, review student candidate shortlists, configure company criteria, and oversee interview scheduling.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <Link to="/placement/drives" className="btn btn-primary">
            <PlusCircle size={16} />
            <span>Create Drive</span>
          </Link>
          <Link to="/placement/applications" className="btn btn-secondary">
            <span>Review Applicants</span>
          </Link>
        </div>
      </div>

      {/* KPI Stats Grid */}
      <div className="stat-grid">
        <div className="stat-card">
          <div>
            <div className="stat-label">Active Drives</div>
            <div className="stat-value">{drives.length}</div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
              TCS, Infosys, Wipro, Amazon
            </div>
          </div>
          <div className="stat-icon">
            <Building2 size={24} />
          </div>
        </div>

        <div className="stat-card stat-info">
          <div>
            <div className="stat-label">Total Candidate Apps</div>
            <div className="stat-value">{applications.length}</div>
            <div style={{ fontSize: '12px', color: 'var(--info)', marginTop: '4px' }}>
              {totalShortlisted} currently shortlisted
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>
            <Users size={24} />
          </div>
        </div>

        <div className="stat-card stat-warning">
          <div>
            <div className="stat-label">Interview Rounds</div>
            <div className="stat-value">{interviews.length}</div>
            <div style={{ fontSize: '12px', color: '#fbbf24', marginTop: '4px' }}>
              Conducted & Scheduled
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>
            <Calendar size={24} />
          </div>
        </div>

        <div className="stat-card stat-success">
          <div>
            <div className="stat-label">Final Job Offers</div>
            <div className="stat-value">{totalSelected}</div>
            <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '4px' }}>
              Candidates Placed
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <CheckCircle2 size={24} />
          </div>
        </div>
      </div>

      {/* Charts & Drive Quick List */}
      <div className="grid-2" style={{ marginBottom: '28px' }}>
        <div className="card">
          <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '18px' }}>
            Recruitment Funnel Overview
          </h3>
          <div style={{ height: '220px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="name" stroke="#64748b" fontSize={12} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={12} allowDecimals={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#1e293b',
                    borderColor: 'rgba(255,255,255,0.1)',
                    borderRadius: '8px',
                    color: '#fff',
                  }}
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Quick Link Navigation */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '14px' }}>
              Officer Workflow Shortcuts
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <Link
                to="/placement/applications"
                style={{
                  padding: '12px 16px',
                  background: 'var(--bg-main)',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <div>
                  <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px' }}>
                    Process Applicant Roster
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Update applicant statuses from Applied to Shortlisted / Interview
                  </div>
                </div>
                <ArrowRight size={16} />
              </Link>

              <Link
                to="/placement/interviews"
                style={{
                  padding: '12px 16px',
                  background: 'var(--bg-main)',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <div>
                  <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px' }}>
                    Schedule Interview Round
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Assign date, time, Google Meet link or campus interview room
                  </div>
                </div>
                <ArrowRight size={16} />
              </Link>

              <Link
                to="/placement/drives"
                style={{
                  padding: '12px 16px',
                  background: 'var(--bg-main)',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <div>
                  <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px' }}>
                    Post New Placement Drive
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Set compensation package, CGPA cutoff, and deadlines
                  </div>
                </div>
                <ArrowRight size={16} />
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
