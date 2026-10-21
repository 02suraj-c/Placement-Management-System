import React, { useState, useEffect } from 'react';
import { driveService } from '../../services/driveService';
import {
  Building2,
  PlusCircle,
  MapPin,
  Clock,
  GraduationCap,
  X,
  CheckCircle,
} from 'lucide-react';

export default function DriveManagement() {
  const [drives, setDrives] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    company: '',
    role: '',
    packageLpa: '',
    minCgpa: '7.0',
    maxBacklogs: '0',
    location: '',
    allowedBranches: 'CSE, IT, ECE',
    deadline: '',
    description: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    loadDrives();
  }, []);

  const loadDrives = async () => {
    try {
      const data = await driveService.getAllDrives();
      setDrives(data);
    } catch (err) {
      console.error('Failed to load drives', err);
    }
  };

  const handleCreateDrive = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const branches = formData.allowedBranches.split(',').map((b) => b.trim());
      await driveService.createDrive({
        ...formData,
        allowedBranches: branches,
      });
      setSuccessMsg(`Drive for ${formData.company} (${formData.role}) created successfully!`);
      setShowModal(false);
      setFormData({
        company: '',
        role: '',
        packageLpa: '',
        minCgpa: '7.0',
        maxBacklogs: '0',
        location: '',
        allowedBranches: 'CSE, IT, ECE',
        deadline: '',
        description: '',
      });
      await loadDrives();
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err) {
      console.error('Failed to create drive', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '14px' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
            Drive Management
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Configure and schedule corporate recruitment drives for the campus.
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="btn btn-primary"
          style={{ gap: '8px' }}
        >
          <PlusCircle size={16} />
          <span>New Placement Drive</span>
        </button>
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
          <CheckCircle size={18} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Drives Grid */}
      <div className="grid-2">
        {drives.map((drive) => (
          <div key={drive.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
              <div>
                <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#fff' }}>
                  {drive.company}
                </h3>
                <div style={{ fontSize: '14px', color: '#93c5fd' }}>{drive.role}</div>
              </div>
              <span
                style={{
                  background: 'rgba(16, 185, 129, 0.15)',
                  color: '#34d399',
                  padding: '4px 10px',
                  borderRadius: 'var(--radius-full)',
                  fontWeight: '700',
                  fontSize: '13px',
                }}
              >
                ₹{drive.packageLpa} LPA
              </span>
            </div>

            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '14px', lineHeight: 1.5 }}>
              {drive.description}
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '12px', color: 'var(--text-muted)', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <MapPin size={13} /> Location: <strong style={{ color: '#fff' }}>{drive.location}</strong>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Clock size={13} /> Registration Deadline: <strong style={{ color: '#fff' }}>{new Date(drive.deadline).toLocaleDateString()}</strong>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <GraduationCap size={13} /> Minimum CGPA: <strong style={{ color: '#fff' }}>{drive.minCgpa}</strong> • Max Backlogs: <strong style={{ color: '#fff' }}>{drive.maxBacklogs}</strong>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-subtle)', paddingTop: '12px' }}>
              <span className="badge badge-active">{drive.status}</span>
              <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                Branches: {drive.allowedBranches.join(', ')}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Create Drive Modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '640px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#fff' }}>
                Create New Campus Drive
              </h3>
              <button
                onClick={() => setShowModal(false)}
                style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleCreateDrive} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Company Name
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Cisco Systems"
                    value={formData.company}
                    onChange={(e) => setFormData({ ...formData, company: e.target.value })}
                    className="form-input"
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Job Role
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Software Engineer"
                    value={formData.role}
                    onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                    className="form-input"
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    CTC Package (LPA)
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    placeholder="e.g. 14.5"
                    value={formData.packageLpa}
                    onChange={(e) => setFormData({ ...formData, packageLpa: e.target.value })}
                    className="form-input"
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Min CGPA Cutoff
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    required
                    value={formData.minCgpa}
                    onChange={(e) => setFormData({ ...formData, minCgpa: e.target.value })}
                    className="form-input"
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Max Backlogs
                  </label>
                  <input
                    type="number"
                    required
                    value={formData.maxBacklogs}
                    onChange={(e) => setFormData({ ...formData, maxBacklogs: e.target.value })}
                    className="form-input"
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Work Location(s)
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Bengaluru / Hyderabad"
                    value={formData.location}
                    onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                    className="form-input"
                  />
                </div>
                <div>
                  <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                    Allowed Branches (comma-separated)
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.allowedBranches}
                    onChange={(e) => setFormData({ ...formData, allowedBranches: e.target.value })}
                    className="form-input"
                  />
                </div>
              </div>

              <div>
                <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                  Application Deadline
                </label>
                <input
                  type="date"
                  required
                  value={formData.deadline}
                  onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
                  className="form-input"
                />
              </div>

              <div>
                <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                  Job Description & Candidate Expectations
                </label>
                <textarea
                  rows="3"
                  required
                  placeholder="Detail responsibilities, tech stack requirements, and selection process..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="form-textarea"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="btn btn-primary"
                >
                  {submitting ? 'Creating Drive...' : 'Publish Drive'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
