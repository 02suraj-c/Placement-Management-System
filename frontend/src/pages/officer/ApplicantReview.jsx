import React, { useState, useEffect } from 'react';
import { applicationService } from '../../services/applicationService';
import { driveService } from '../../services/driveService';
import {
  Users,
  Search,
  Filter,
  CheckCircle,
  Clock,
  XCircle,
  Calendar,
  AlertCircle,
} from 'lucide-react';

export default function ApplicantReview() {
  const [applications, setApplications] = useState([]);
  const [drives, setDrives] = useState([]);
  const [selectedDriveId, setSelectedDriveId] = useState('ALL');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [feedback, setFeedback] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [appsData, drivesData] = await Promise.all([
        applicationService.getAllApplications(),
        driveService.getAllDrives(),
      ]);
      setApplications(appsData);
      setDrives(drivesData);
    } catch (err) {
      console.error('Failed to load applicant data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (appId, newStatus) => {
    setUpdatingId(appId);
    try {
      await applicationService.updateStatus(appId, newStatus);
      setApplications((prev) =>
        prev.map((a) => (a.id === appId ? { ...a, status: newStatus } : a))
      );
      setFeedback(`Status updated to ${newStatus.replace('_', ' ')}`);
      setTimeout(() => setFeedback(''), 3000);
    } catch (err) {
      console.error('Failed to update status', err);
    } finally {
      setUpdatingId(null);
    }
  };

  const getBadgeClass = (status) => {
    switch (status) {
      case 'SELECTED': return 'badge-selected';
      case 'SHORTLISTED': return 'badge-shortlisted';
      case 'INTERVIEW_SCHEDULED': return 'badge-interview_scheduled';
      case 'REJECTED': return 'badge-rejected';
      case 'APPLIED':
      default: return 'badge-applied';
    }
  };

  const filteredApps = applications.filter((app) => {
    const matchesDrive =
      selectedDriveId === 'ALL' || app.driveId === Number(selectedDriveId);
    const matchesSearch =
      (app.studentName && app.studentName.toLowerCase().includes(search.toLowerCase())) ||
      (app.company && app.company.toLowerCase().includes(search.toLowerCase())) ||
      (app.studentEmail && app.studentEmail.toLowerCase().includes(search.toLowerCase()));
    return matchesDrive && matchesSearch;
  });

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading applicant roster...</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Applicant Roster & Shortlisting
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Review candidate submissions, update recruitment statuses, and progress students through selection rounds.
        </p>
      </div>

      {feedback && (
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
          <span>{feedback}</span>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div
        style={{
          display: 'flex',
          gap: '14px',
          flexWrap: 'wrap',
          marginBottom: '24px',
          background: 'var(--bg-card)',
          padding: '16px',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border-subtle)',
        }}
      >
        <div style={{ flex: '1', minWidth: '240px', position: 'relative' }}>
          <Search
            size={16}
            style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}
          />
          <input
            type="text"
            placeholder="Search by candidate name or company..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '38px' }}
          />
        </div>

        <div style={{ width: '240px' }}>
          <select
            value={selectedDriveId}
            onChange={(e) => setSelectedDriveId(e.target.value)}
            className="form-select"
          >
            <option value="ALL">All Drives ({drives.length})</option>
            {drives.map((d) => (
              <option key={d.id} value={d.id}>
                {d.company} - {d.role}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Applicants Table */}
      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>Applicant</th>
              <th>Drive / Company</th>
              <th>Package</th>
              <th>Applied Date</th>
              <th>Current Status</th>
              <th>Change Status</th>
            </tr>
          </thead>
          <tbody>
            {filteredApps.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                  No applicants matching the selected criteria.
                </td>
              </tr>
            ) : (
              filteredApps.map((app) => (
                <tr key={app.id}>
                  <td>
                    <div style={{ fontWeight: '600', color: '#fff' }}>
                      {app.studentName || 'Student'}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                      {app.studentEmail}
                    </div>
                  </td>
                  <td>
                    <div style={{ fontWeight: '600', color: '#fff' }}>{app.company}</div>
                    <div style={{ fontSize: '12px', color: '#93c5fd' }}>{app.role}</div>
                  </td>
                  <td>
                    <span style={{ color: '#34d399', fontWeight: '700' }}>
                      ₹{app.packageLpa} LPA
                    </span>
                  </td>
                  <td>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                      {new Date(app.createdAt).toLocaleDateString()}
                    </div>
                  </td>
                  <td>
                    <span className={`badge ${getBadgeClass(app.status)}`}>
                      {app.status.replace('_', ' ')}
                    </span>
                  </td>
                  <td>
                    <select
                      value={app.status}
                      disabled={updatingId === app.id}
                      onChange={(e) => handleStatusChange(app.id, e.target.value)}
                      className="form-select"
                      style={{ fontSize: '12px', padding: '6px 10px', width: 'auto' }}
                    >
                      <option value="APPLIED">Applied</option>
                      <option value="SHORTLISTED">Shortlisted</option>
                      <option value="INTERVIEW_SCHEDULED">Interview Scheduled</option>
                      <option value="SELECTED">Selected / Offered</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
