import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { driveService } from '../../services/driveService';
import { applicationService } from '../../services/applicationService';
import { interviewService } from '../../services/interviewService';
import {
  Briefcase,
  FileCheck2,
  Calendar,
  CheckCircle2,
  ArrowRight,
  Clock,
  Sparkles,
  TrendingUp,
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

export default function StudentDashboard() {
  const { user } = useAuth();
  const [drives, setDrives] = useState([]);
  const [applications, setApplications] = useState([]);
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadDashboardData() {
      try {
        const studentId = user?.id || 1;
        const [drivesData, appsData, interviewsData] = await Promise.all([
          driveService.getAllDrives(),
          applicationService.getApplicationsByStudent(studentId),
          interviewService.getInterviewsByStudent(studentId),
        ]);
        setDrives(drivesData);
        setApplications(appsData);
        setInterviews(interviewsData);
      } catch (err) {
        console.error('Failed to load dashboard data', err);
      } finally {
        setLoading(false);
      }
    }
    loadDashboardData();
  }, [user]);

  // Eligibility calculation
  const studentCgpa = user?.cgpa || 8.85;
  const studentBacklogs = user?.backlogs ?? 0;

  const eligibleDrivesCount = drives.filter(
    (d) => studentCgpa >= d.minCgpa && studentBacklogs <= d.maxBacklogs
  ).length;

  const appliedDriveIds = new Set(applications.map((a) => a.driveId));

  // Chart data
  const statusCounts = {
    APPLIED: applications.filter((a) => a.status === 'APPLIED').length,
    SHORTLISTED: applications.filter((a) => a.status === 'SHORTLISTED').length,
    INTERVIEW_SCHEDULED: applications.filter((a) => a.status === 'INTERVIEW_SCHEDULED').length,
    SELECTED: applications.filter((a) => a.status === 'SELECTED').length,
  };

  const chartData = [
    { name: 'Applied', count: statusCounts.APPLIED, color: '#38bdf8' },
    { name: 'Shortlisted', count: statusCounts.SHORTLISTED, color: '#c084fc' },
    { name: 'Interviews', count: statusCounts.INTERVIEW_SCHEDULED, color: '#fbbf24' },
    { name: 'Selected', count: statusCounts.SELECTED, color: '#34d399' },
  ];

  const upcomingInterviews = interviews.filter((i) => i.status === 'SCHEDULED');

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading student portal...</div>;
  }

  return (
    <div>
      {/* Welcome Hero */}
      <div
        style={{
          background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(139, 92, 246, 0.1))',
          border: '1px solid rgba(59, 130, 246, 0.25)',
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
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <span
              style={{
                fontSize: '12px',
                fontWeight: '700',
                padding: '3px 10px',
                borderRadius: 'var(--radius-full)',
                background: 'rgba(59, 130, 246, 0.2)',
                color: '#60a5fa',
              }}
            >
              JECRC • Academic Year 2026 Batch
            </span>
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              {user?.department || 'Computer Science & Engineering'}
            </span>
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '6px' }}>
            Welcome back, {user?.name || 'Aarav Sharma'}!
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', maxWidth: '600px' }}>
            You are currently eligible for {eligibleDrivesCount} on-campus drives. Your academic profile meets placement criteria with a CGPA of {studentCgpa}.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <Link to="/student/drives" className="btn btn-primary">
            <Briefcase size={16} />
            <span>Browse Drives</span>
          </Link>
          <Link to="/student/profile" className="btn btn-secondary">
            <span>View Profile</span>
          </Link>
        </div>
      </div>

      {/* KPI Stat Cards */}
      <div className="stat-grid">
        <div className="stat-card">
          <div>
            <div className="stat-label">Eligible Drives</div>
            <div className="stat-value">{eligibleDrivesCount}</div>
            <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '4px' }}>
              ✓ All criteria satisfied
            </div>
          </div>
          <div className="stat-icon">
            <Briefcase size={24} />
          </div>
        </div>

        <div className="stat-card stat-info">
          <div>
            <div className="stat-label">Active Applications</div>
            <div className="stat-value">{applications.length}</div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
              {statusCounts.SHORTLISTED} Shortlisted
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>
            <FileCheck2 size={24} />
          </div>
        </div>

        <div className="stat-card stat-warning">
          <div>
            <div className="stat-label">Scheduled Interviews</div>
            <div className="stat-value">{upcomingInterviews.length}</div>
            <div style={{ fontSize: '12px', color: '#fbbf24', marginTop: '4px' }}>
              Next on Oct 24
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>
            <Calendar size={24} />
          </div>
        </div>

        <div className="stat-card stat-success">
          <div>
            <div className="stat-label">Eligibility CGPA</div>
            <div className="stat-value">{studentCgpa}</div>
            <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '4px' }}>
              0 Active Backlogs
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <CheckCircle2 size={24} />
          </div>
        </div>
      </div>

      {/* Analytics & Impending Action Section */}
      <div className="grid-2" style={{ marginBottom: '28px' }}>
        {/* Application Pipeline Chart */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)' }}>
              Application Funnel
            </h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Real-time status</span>
          </div>

          <div style={{ height: '220px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} tickLine={false} />
                <YAxis stroke="var(--text-muted)" fontSize={12} allowDecimals={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'var(--bg-card)',
                    borderColor: 'var(--border-subtle)',
                    borderRadius: '8px',
                    color: 'var(--text-primary)',
                    boxShadow: 'var(--shadow-card)',
                  }}
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Upcoming Interview Highlight */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)' }}>
              Upcoming Interviews
            </h3>
            <Link to="/student/interviews" style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
              View all <ArrowRight size={13} />
            </Link>
          </div>

          {upcomingInterviews.length === 0 ? (
            <div style={{ color: 'var(--text-muted)', fontSize: '14px', margin: 'auto 0', textAlign: 'center', padding: '30px' }}>
              No interviews scheduled currently. Apply for drives to receive interview calls.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', flex: 1 }}>
              {upcomingInterviews.slice(0, 2).map((interview) => (
                <div
                  key={interview.id}
                  style={{
                    padding: '14px',
                    borderRadius: 'var(--radius-md)',
                    background: 'var(--bg-main)',
                    border: '1px solid var(--border-subtle)',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <span style={{ fontWeight: '600', color: 'var(--text-primary)', fontSize: '14px' }}>
                      {interview.company}
                    </span>
                    <span className="badge badge-interview_scheduled">
                      {interview.status}
                    </span>
                  </div>
                  <div style={{ fontSize: '13px', color: '#93c5fd', marginBottom: '6px' }}>
                    {interview.roundName}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '14px', fontSize: '12px', color: 'var(--text-secondary)' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Clock size={13} />
                      {new Date(interview.interviewDateTime).toLocaleDateString()} at{' '}
                      {new Date(interview.interviewDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    <span>•</span>
                    <span>{interview.mode}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Recommended Placement Drives */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)' }}>
              Active Drives Available for Registration
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              Top recruiting partners actively hiring for the 2026 graduation batch
            </p>
          </div>
          <Link to="/student/drives" className="btn btn-outline btn-sm">
            View All Drives ({drives.length})
          </Link>
        </div>

        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Company</th>
                <th>Role</th>
                <th>Package</th>
                <th>Eligibility Criteria</th>
                <th>Application Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {drives.slice(0, 4).map((drive) => {
                const isEligible = studentCgpa >= drive.minCgpa && studentBacklogs <= drive.maxBacklogs;
                const hasApplied = appliedDriveIds.has(drive.id);

                return (
                  <tr key={drive.id}>
                    <td>
                      <span style={{ fontWeight: '600', color: 'var(--text-primary)' }}>{drive.company}</span>
                      <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{drive.location}</div>
                    </td>
                    <td>{drive.role}</td>
                    <td>
                      <span style={{ color: '#34d399', fontWeight: '700' }}>
                        ₹{drive.packageLpa} LPA
                      </span>
                    </td>
                    <td>
                      <div style={{ fontSize: '12px' }}>
                        Min CGPA: <strong>{drive.minCgpa}</strong> • Max Backlogs: {drive.maxBacklogs}
                      </div>
                      <span style={{ fontSize: '11px', color: isEligible ? 'var(--success)' : 'var(--danger)' }}>
                        {isEligible ? '✓ You qualify' : '✗ Criteria not met'}
                      </span>
                    </td>
                    <td>
                      {hasApplied ? (
                        <span className="badge badge-applied">Applied</span>
                      ) : (
                        <span className="badge badge-inactive">Not Applied</span>
                      )}
                    </td>
                    <td>
                      {hasApplied ? (
                        <Link to="/student/applications" className="btn btn-outline btn-sm">
                          Track
                        </Link>
                      ) : (
                        <Link
                          to="/student/drives"
                          className={`btn btn-sm ${isEligible ? 'btn-primary' : 'btn-secondary'}`}
                        >
                          Details
                        </Link>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
