import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { driveService } from '../../services/driveService';
import { applicationService } from '../../services/applicationService';
import {
  Search,
  Filter,
  Briefcase,
  CheckCircle,
  XCircle,
  Clock,
  MapPin,
  Building,
  GraduationCap,
  X,
  AlertCircle,
} from 'lucide-react';

export default function EligibleDrives() {
  const { user } = useAuth();
  const [drives, setDrives] = useState([]);
  const [applications, setApplications] = useState([]);
  const [search, setSearch] = useState('');
  const [minLpa, setMinLpa] = useState('0');
  const [selectedDrive, setSelectedDrive] = useState(null);
  const [applyLoading, setApplyLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const studentId = user?.id || 1;
  const studentCgpa = user?.cgpa || 8.85;
  const studentBacklogs = user?.backlogs ?? 0;

  useEffect(() => {
    loadDrivesAndApplications();
  }, [user]);

  const loadDrivesAndApplications = async () => {
    try {
      const [drivesData, appsData] = await Promise.all([
        driveService.getAllDrives(),
        applicationService.getApplicationsByStudent(studentId),
      ]);
      setDrives(drivesData);
      setApplications(appsData);
    } catch (err) {
      console.error('Error fetching drives', err);
    }
  };

  const appliedMap = new Map(applications.map((app) => [app.driveId, app]));

  const handleApply = async (drive) => {
    setMessage({ type: '', text: '' });
    setApplyLoading(true);

    try {
      await applicationService.apply(studentId, drive.id);
      setMessage({
        type: 'success',
        text: `Successfully registered for ${drive.company} - ${drive.role}! View status in My Applications.`,
      });
      await loadDrivesAndApplications();
      setSelectedDrive(null);
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Failed to apply for drive.',
      });
    } finally {
      setApplyLoading(false);
    }
  };

  const filteredDrives = drives.filter((drive) => {
    const matchesSearch =
      drive.company.toLowerCase().includes(search.toLowerCase()) ||
      drive.role.toLowerCase().includes(search.toLowerCase()) ||
      drive.location.toLowerCase().includes(search.toLowerCase());
    const matchesLpa = drive.packageLpa >= parseFloat(minLpa);
    return matchesSearch && matchesLpa;
  });

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Placement Drives
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Explore open recruitment opportunities for the 2026 graduation batch. Eligibility is verified automatically.
        </p>
      </div>

      {/* Alert banner if any */}
      {message.text && (
        <div
          style={{
            padding: '14px 18px',
            borderRadius: 'var(--radius-md)',
            background: message.type === 'success' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
            border: `1px solid ${message.type === 'success' ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
            color: message.type === 'success' ? '#34d399' : '#f87171',
            marginBottom: '20px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            fontSize: '14px',
          }}
        >
          {message.type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
          <span>{message.text}</span>
          <button
            onClick={() => setMessage({ type: '', text: '' })}
            style={{ marginLeft: 'auto', background: 'transparent', border: 'none', color: 'inherit', cursor: 'pointer' }}
          >
            <X size={16} />
          </button>
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
            placeholder="Search by company, role or location..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '38px' }}
          />
        </div>

        <div style={{ width: '200px' }}>
          <select
            value={minLpa}
            onChange={(e) => setMinLpa(e.target.value)}
            className="form-select"
          >
            <option value="0">All Packages</option>
            <option value="6">Min 6.0 LPA</option>
            <option value="8">Min 8.0 LPA</option>
            <option value="12">Min 12.0 LPA (Dream)</option>
            <option value="20">Min 20.0 LPA (Super Dream)</option>
          </select>
        </div>
      </div>

      {/* Drives Grid */}
      <div className="grid-2">
        {filteredDrives.map((drive) => {
          const isEligible = studentCgpa >= drive.minCgpa && studentBacklogs <= drive.maxBacklogs;
          const application = appliedMap.get(drive.id);
          const hasApplied = !!application;

          return (
            <div
              key={drive.id}
              className="card"
              style={{
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                position: 'relative',
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                  <div>
                    <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#fff', marginBottom: '4px' }}>
                      {drive.company}
                    </h3>
                    <div style={{ fontSize: '14px', color: '#93c5fd', fontWeight: '500' }}>
                      {drive.role}
                    </div>
                  </div>
                  <span
                    style={{
                      background: 'rgba(16, 185, 129, 0.15)',
                      color: '#34d399',
                      padding: '4px 12px',
                      borderRadius: 'var(--radius-full)',
                      fontWeight: '700',
                      fontSize: '14px',
                    }}
                  >
                    ₹{drive.packageLpa} LPA
                  </span>
                </div>

                <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px', lineHeight: 1.5 }}>
                  {drive.description}
                </p>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', marginBottom: '18px', fontSize: '12px', color: 'var(--text-muted)' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <MapPin size={13} /> {drive.location}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Clock size={13} /> Deadline: {new Date(drive.deadline).toLocaleDateString()}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <GraduationCap size={13} /> Branches: {drive.allowedBranches.join(', ')}
                  </span>
                </div>

                {/* Eligibility Meter */}
                <div
                  style={{
                    padding: '10px 14px',
                    borderRadius: 'var(--radius-md)',
                    background: isEligible ? 'rgba(16, 185, 129, 0.08)' : 'rgba(239, 68, 68, 0.08)',
                    border: `1px solid ${isEligible ? 'rgba(16, 185, 129, 0.2)' : 'rgba(239, 68, 68, 0.2)'}`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    fontSize: '12px',
                    marginBottom: '16px',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    {isEligible ? (
                      <CheckCircle size={16} color="#10b981" />
                    ) : (
                      <XCircle size={16} color="#ef4444" />
                    )}
                    <span style={{ color: isEligible ? '#34d399' : '#f87171', fontWeight: '600' }}>
                      {isEligible ? 'Eligible to Apply' : 'Eligibility Criteria Not Met'}
                    </span>
                  </div>
                  <span style={{ color: 'var(--text-muted)' }}>
                    Req: {drive.minCgpa} CGPA • Backlogs &le; {drive.maxBacklogs}
                  </span>
                </div>
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', gap: '10px', marginTop: 'auto' }}>
                <button
                  type="button"
                  onClick={() => setSelectedDrive(drive)}
                  className="btn btn-secondary btn-sm"
                  style={{ flex: 1 }}
                >
                  Drive Details
                </button>

                {hasApplied ? (
                  <button
                    disabled
                    className="btn btn-outline btn-sm"
                    style={{ flex: 1, borderColor: '#38bdf8', color: '#38bdf8', cursor: 'default' }}
                  >
                    ✓ Applied ({application.status})
                  </button>
                ) : (
                  <button
                    type="button"
                    disabled={!isEligible || applyLoading}
                    onClick={() => handleApply(drive)}
                    className="btn btn-primary btn-sm"
                    style={{ flex: 1, opacity: !isEligible ? 0.5 : 1 }}
                  >
                    {isEligible ? 'Apply Now' : 'Not Eligible'}
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Drive Details Modal */}
      {selectedDrive && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '20px', fontWeight: '800', color: '#fff' }}>
                  {selectedDrive.company}
                </h3>
                <span style={{ fontSize: '14px', color: '#60a5fa' }}>{selectedDrive.role}</span>
              </div>
              <button
                onClick={() => setSelectedDrive(null)}
                style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginBottom: '24px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div style={{ padding: '10px', background: 'var(--bg-main)', borderRadius: '8px' }}>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Package</div>
                  <div style={{ fontSize: '16px', fontWeight: '700', color: '#34d399' }}>
                    ₹{selectedDrive.packageLpa} LPA
                  </div>
                </div>
                <div style={{ padding: '10px', background: 'var(--bg-main)', borderRadius: '8px' }}>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Locations</div>
                  <div style={{ fontSize: '13px', fontWeight: '600', color: '#fff' }}>
                    {selectedDrive.location}
                  </div>
                </div>
              </div>

              <div>
                <h4 style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Role Overview & Requirements
                </h4>
                <p style={{ fontSize: '13px', color: '#e2e8f0', lineHeight: 1.6 }}>
                  {selectedDrive.description}
                </p>
              </div>

              <div>
                <h4 style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Eligibility Matrix
                </h4>
                <ul style={{ fontSize: '13px', color: '#cbd5e1', paddingLeft: '18px', lineHeight: 1.6 }}>
                  <li>Minimum Cumulative GPA: <strong>{selectedDrive.minCgpa}</strong> (Your CGPA: {studentCgpa})</li>
                  <li>Maximum Active Backlogs: <strong>{selectedDrive.maxBacklogs}</strong> (Your Backlogs: {studentBacklogs})</li>
                  <li>Allowed Engineering Disciplines: <strong>{selectedDrive.allowedBranches.join(', ')}</strong></li>
                  <li>Registration Deadline: <strong>{new Date(selectedDrive.deadline).toLocaleString()}</strong></li>
                </ul>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button
                onClick={() => setSelectedDrive(null)}
                className="btn btn-secondary"
              >
                Close
              </button>
              {appliedMap.has(selectedDrive.id) ? (
                <button disabled className="btn btn-outline" style={{ cursor: 'default' }}>
                  Already Registered
                </button>
              ) : (
                <button
                  disabled={!(studentCgpa >= selectedDrive.minCgpa && studentBacklogs <= selectedDrive.maxBacklogs) || applyLoading}
                  onClick={() => handleApply(selectedDrive)}
                  className="btn btn-primary"
                >
                  {applyLoading ? 'Submitting...' : 'Confirm & Apply'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
