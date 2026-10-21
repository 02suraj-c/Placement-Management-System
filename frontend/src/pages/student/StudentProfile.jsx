import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  User,
  GraduationCap,
  Award,
  FileCheck,
  CheckCircle,
  FileText,
  Upload,
} from 'lucide-react';

export default function StudentProfile() {
  const { user } = useAuth();
  const [uploadedResume, setUploadedResume] = useState('Aarav_Sharma_Resume_2026.pdf');
  const [uploadSuccess, setUploadSuccess] = useState(false);

  const handleSimulatedUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      setUploadedResume(file.name);
      setUploadSuccess(true);
      setTimeout(() => setUploadSuccess(false), 3000);
    }
  };

  const skills = user?.skills || ['Java', 'Spring Boot', 'React.js', 'MySQL', 'Data Structures', 'Docker'];

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Student Profile & Resume
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Verified academic details and credentials used for placement drive eligibility screening.
        </p>
      </div>

      <div className="grid-3" style={{ marginBottom: '24px' }}>
        {/* Academic Overview */}
        <div className="card" style={{ gridColumn: 'span 2' }}>
          <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '18px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <GraduationCap size={18} color="#3b82f6" />
            <span>Academic Qualifications</span>
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', marginBottom: '24px' }}>
            <div style={{ padding: '12px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Full Name</div>
              <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff' }}>{user?.name || 'Aarav Sharma'}</div>
            </div>
            <div style={{ padding: '12px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Roll / Registration Number</div>
              <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff' }}>{user?.rollNumber || 'CS2026-042'}</div>
            </div>
            <div style={{ padding: '12px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Degree & Department</div>
              <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff' }}>
                B.Tech in {user?.department || 'Computer Science & Engineering'}
              </div>
            </div>
            <div style={{ padding: '12px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Graduating Batch</div>
              <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff' }}>Class of 2026</div>
            </div>
            <div style={{ padding: '12px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Cumulative GPA (CGPA)</div>
              <div style={{ fontSize: '18px', fontWeight: '700', color: '#34d399' }}>
                {user?.cgpa || 8.85} / 10.0
              </div>
            </div>
            <div style={{ padding: '12px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Active Backlogs</div>
              <div style={{ fontSize: '18px', fontWeight: '700', color: '#38bdf8' }}>
                {user?.backlogs ?? 0} (Clean Record)
              </div>
            </div>
          </div>

          <div>
            <h4 style={{ fontSize: '14px', fontWeight: '600', color: '#fff', marginBottom: '10px' }}>
              Technical Skills & Proficiencies
            </h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {skills.map((skill, idx) => (
                <span
                  key={idx}
                  style={{
                    padding: '6px 14px',
                    borderRadius: 'var(--radius-full)',
                    background: 'var(--bg-subtle)',
                    border: '1px solid var(--border-subtle)',
                    color: '#e2e8f0',
                    fontSize: '13px',
                    fontWeight: '500',
                  }}
                >
                  {skill}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Resume Card */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FileCheck size={18} color="#10b981" />
              <span>Placement Resume</span>
            </h3>

            <div
              style={{
                padding: '16px',
                background: 'var(--bg-main)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-subtle)',
                marginBottom: '16px',
                textAlign: 'center',
              }}
            >
              <FileText size={36} color="#3b82f6" style={{ margin: '0 auto 8px' }} />
              <div style={{ fontSize: '13px', fontWeight: '600', color: '#fff', wordBreak: 'break-all' }}>
                {uploadedResume}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--success)', marginTop: '4px' }}>
                ✓ Verified by Placement Cell
              </div>
            </div>

            {uploadSuccess && (
              <div style={{ fontSize: '12px', color: '#34d399', textAlign: 'center', marginBottom: '12px' }}>
                Resume updated successfully!
              </div>
            )}
          </div>

          <div>
            <label
              className="btn btn-primary"
              style={{ width: '100%', cursor: 'pointer', gap: '8px' }}
            >
              <Upload size={15} />
              <span>Upload New Resume (PDF)</span>
              <input
                type="file"
                accept=".pdf,.docx"
                onChange={handleSimulatedUpload}
                style={{ display: 'none' }}
              />
            </label>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', textAlign: 'center', marginTop: '8px' }}>
              Accepted formats: PDF, DOCX (Max 5MB)
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
