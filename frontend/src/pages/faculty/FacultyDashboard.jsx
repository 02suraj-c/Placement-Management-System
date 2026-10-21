import React, { useState } from 'react';
import {
  CheckCircle,
  FileCheck,
  AlertCircle,
  Users,
  Award,
} from 'lucide-react';

export default function FacultyDashboard() {
  const [students, setStudents] = useState([
    { id: 1, name: 'Aarav Sharma', roll: 'CS2026-042', cgpa: 8.85, backlogs: 0, status: 'VERIFIED' },
    { id: 6, name: 'Priya Patel', roll: 'CS2026-015', cgpa: 8.20, backlogs: 0, status: 'VERIFIED' },
    { id: 7, name: 'Rohan Gupta', roll: 'CS2026-088', cgpa: 7.50, backlogs: 1, status: 'PENDING' },
    { id: 8, name: 'Ananya Iyer', roll: 'CS2026-004', cgpa: 9.10, backlogs: 0, status: 'VERIFIED' },
    { id: 9, name: 'Vikram Seth', roll: 'CS2026-102', cgpa: 6.95, backlogs: 0, status: 'PENDING' },
  ]);

  const handleVerify = (id) => {
    setStudents((prev) =>
      prev.map((s) => (s.id === id ? { ...s, status: 'VERIFIED' } : s))
    );
  };

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Faculty Coordinator Portal
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Review department student academic clearances, verify CGPA documentation, and authorize placement drive eligibility.
        </p>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div>
            <div className="stat-label">Department Students</div>
            <div className="stat-value">148</div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
              Batch of 2026 (CSE)
            </div>
          </div>
          <div className="stat-icon">
            <Users size={24} />
          </div>
        </div>

        <div className="stat-card stat-success">
          <div>
            <div className="stat-label">Verified Profiles</div>
            <div className="stat-value">136</div>
            <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '4px' }}>
              Academic credentials approved
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <FileCheck size={24} />
          </div>
        </div>

        <div className="stat-card stat-warning">
          <div>
            <div className="stat-label">Pending Clearance</div>
            <div className="stat-value">12</div>
            <div style={{ fontSize: '12px', color: '#fbbf24', marginTop: '4px' }}>
              Mark sheet inspection needed
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>
            <AlertCircle size={24} />
          </div>
        </div>
      </div>

      <div className="card">
        <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '16px' }}>
          Student Verification Queue (CSE Batch 2026)
        </h3>

        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Roll Number</th>
                <th>CGPA</th>
                <th>Active Backlogs</th>
                <th>Academic Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {students.map((st) => (
                <tr key={st.id}>
                  <td style={{ fontWeight: '600', color: '#fff' }}>{st.name}</td>
                  <td>{st.roll}</td>
                  <td>
                    <strong style={{ color: '#34d399' }}>{st.cgpa}</strong>
                  </td>
                  <td>{st.backlogs}</td>
                  <td>
                    <span className={`badge ${st.status === 'VERIFIED' ? 'badge-selected' : 'badge-interview_scheduled'}`}>
                      {st.status}
                    </span>
                  </td>
                  <td>
                    {st.status === 'VERIFIED' ? (
                      <span style={{ fontSize: '12px', color: 'var(--success)' }}>✓ Approved</span>
                    ) : (
                      <button
                        onClick={() => handleVerify(st.id)}
                        className="btn btn-primary btn-sm"
                      >
                        Verify Academic Record
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
