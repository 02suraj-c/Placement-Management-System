import React, { useState } from 'react';
import { checkEligibility, getPlacementDrives } from '../../services/placementService';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const statusColor = {
  UPCOMING: { bg: 'rgba(59,130,246,0.15)', border: 'rgba(59,130,246,0.35)', text: '#93c5fd' },
  ONGOING: { bg: 'rgba(16,185,129,0.15)', border: 'rgba(16,185,129,0.35)', text: '#6ee7b7' },
  COMPLETED: { bg: 'rgba(148,163,184,0.12)', border: 'rgba(148,163,184,0.25)', text: '#94a3b8' },
  CANCELLED: { bg: 'rgba(239,68,68,0.12)', border: 'rgba(239,68,68,0.3)', text: '#f87171' },
};

function StatusBadge({ status }) {
  const c = statusColor[status] || statusColor.COMPLETED;
  return (
    <span style={{
      padding: '3px 10px',
      borderRadius: '99px',
      fontSize: '11px',
      fontWeight: '700',
      letterSpacing: '0.05em',
      background: c.bg,
      border: `1px solid ${c.border}`,
      color: c.text,
      textTransform: 'uppercase',
    }}>
      {status || '—'}
    </span>
  );
}

function InfoRow({ label, value, mono }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', padding: '10px 0', borderBottom: '1px solid rgba(51,65,85,0.4)' }}>
      <span style={{ color: '#94a3b8', fontSize: '13px', minWidth: '200px' }}>{label}</span>
      <span style={{
        color: '#f8fafc',
        fontSize: '13px',
        fontWeight: '500',
        textAlign: 'right',
        fontFamily: mono ? 'monospace' : 'inherit',
        wordBreak: 'break-word',
        maxWidth: '300px',
      }}>
        {value ?? '—'}
      </span>
    </div>
  );
}

// ---------------------------------------------------------------------------
// Main Component
// ---------------------------------------------------------------------------

/**
 * Eligibility.jsx
 *
 * Placement Officer eligibility checker tool.
 * Calls GET /api/eligibility/check?studentId={id}&driveId={id}
 * and renders the structured EligibilityCheckResponse returned by EligibilityService.
 *
 * Package : frontend/src/pages/placement
 * Owner   : Person 3
 */
export default function Eligibility() {
  const [studentId, setStudentId] = useState('');
  const [driveId, setDriveId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  // ---------------------------------------------------------------------------
  // Validation
  // ---------------------------------------------------------------------------

  const isPositiveInt = (val) => {
    const n = Number(val);
    return Number.isInteger(n) && n > 0;
  };

  const validate = () => {
    if (!studentId.trim()) return 'Student ID is required.';
    if (!isPositiveInt(studentId.trim())) return 'Student ID must be a positive integer.';
    if (!driveId.trim()) return 'Drive ID is required.';
    if (!isPositiveInt(driveId.trim())) return 'Drive ID must be a positive integer.';
    return '';
  };

  // ---------------------------------------------------------------------------
  // API Call
  // ---------------------------------------------------------------------------

  const handleCheck = async (e) => {
    e.preventDefault();
    setError('');
    setResult(null);

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    try {
      const response = await checkEligibility(Number(studentId.trim()), Number(driveId.trim()));
      // placementService returns the full ApiResponse body; data property holds EligibilityCheckResponse
      const payload = response?.data ?? response;
      setResult(payload);
    } catch (err) {
      setError(err.message || 'Failed to evaluate eligibility. Check that both IDs exist in the system.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setStudentId('');
    setDriveId('');
    setError('');
    setResult(null);
  };

  // ---------------------------------------------------------------------------
  // Styles
  // ---------------------------------------------------------------------------

  const styles = {
    container: {
      fontFamily: "'Inter', system-ui, sans-serif",
      color: '#f8fafc',
      minHeight: '100vh',
    },
    header: {
      marginBottom: '28px',
    },
    headerTitle: {
      fontSize: '22px',
      fontWeight: '800',
      color: '#f8fafc',
      marginBottom: '6px',
    },
    headerSub: {
      fontSize: '14px',
      color: '#94a3b8',
    },
    card: {
      background: '#1e293b',
      border: '1px solid #334155',
      borderRadius: '12px',
      padding: '28px',
      marginBottom: '24px',
    },
    cardTitle: {
      fontSize: '15px',
      fontWeight: '700',
      color: '#f8fafc',
      marginBottom: '20px',
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
    },
    formRow: {
      display: 'flex',
      gap: '16px',
      flexWrap: 'wrap',
      marginBottom: '20px',
    },
    fieldGroup: {
      flex: '1',
      minWidth: '200px',
    },
    label: {
      display: 'block',
      fontSize: '12px',
      fontWeight: '600',
      color: '#94a3b8',
      marginBottom: '6px',
      textTransform: 'uppercase',
      letterSpacing: '0.05em',
    },
    input: {
      width: '100%',
      background: '#0f172a',
      border: '1px solid #334155',
      borderRadius: '8px',
      padding: '10px 14px',
      color: '#f8fafc',
      fontSize: '14px',
      outline: 'none',
      boxSizing: 'border-box',
      transition: 'border-color 0.15s ease',
    },
    primaryBtn: {
      padding: '11px 28px',
      background: 'linear-gradient(135deg, #3b82f6, #2563eb)',
      color: '#fff',
      border: 'none',
      borderRadius: '8px',
      fontSize: '14px',
      fontWeight: '600',
      cursor: 'pointer',
      transition: 'opacity 0.15s ease',
      letterSpacing: '0.02em',
    },
    ghostBtn: {
      padding: '11px 22px',
      background: 'transparent',
      color: '#94a3b8',
      border: '1px solid #334155',
      borderRadius: '8px',
      fontSize: '14px',
      fontWeight: '600',
      cursor: 'pointer',
      transition: 'border-color 0.15s ease',
    },
    errorBox: {
      background: 'rgba(239,68,68,0.1)',
      border: '1px solid rgba(239,68,68,0.35)',
      borderRadius: '8px',
      padding: '13px 16px',
      color: '#f87171',
      fontSize: '13px',
      marginBottom: '20px',
      display: 'flex',
      alignItems: 'flex-start',
      gap: '10px',
    },
    eligibleBanner: {
      background: 'rgba(16,185,129,0.12)',
      border: '1px solid rgba(16,185,129,0.35)',
      borderRadius: '10px',
      padding: '18px 22px',
      marginBottom: '20px',
      display: 'flex',
      alignItems: 'center',
      gap: '14px',
    },
    ineligibleBanner: {
      background: 'rgba(239,68,68,0.12)',
      border: '1px solid rgba(239,68,68,0.35)',
      borderRadius: '10px',
      padding: '18px 22px',
      marginBottom: '20px',
      display: 'flex',
      alignItems: 'flex-start',
      gap: '14px',
    },
    reasonsList: {
      listStyle: 'none',
      padding: 0,
      margin: '10px 0 0 0',
    },
    reasonItem: {
      background: 'rgba(239,68,68,0.08)',
      border: '1px solid rgba(239,68,68,0.2)',
      borderRadius: '6px',
      padding: '10px 14px',
      marginBottom: '8px',
      color: '#fca5a5',
      fontSize: '13px',
      display: 'flex',
      alignItems: 'flex-start',
      gap: '10px',
    },
    sectionTitle: {
      fontSize: '13px',
      fontWeight: '700',
      color: '#94a3b8',
      textTransform: 'uppercase',
      letterSpacing: '0.08em',
      marginBottom: '12px',
    },
    divider: {
      borderTop: '1px solid #334155',
      margin: '20px 0',
    },
    helperText: {
      fontSize: '12px',
      color: '#64748b',
      marginTop: '5px',
    },
  };

  // ---------------------------------------------------------------------------
  // Render
  // ---------------------------------------------------------------------------

  return (
    <div style={styles.container}>
      {/* Page Header */}
      <div style={styles.header}>
        <h1 style={styles.headerTitle}>Eligibility Checker</h1>
        <p style={styles.headerSub}>
          Evaluate a student's eligibility for a specific placement drive using live backend criteria.
          Results reflect the exact rules configured on each drive — CGPA, backlogs, eligible branches, and status.
        </p>
      </div>

      {/* Input Form Card */}
      <div style={styles.card}>
        <div style={styles.cardTitle}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
          </svg>
          Enter Student &amp; Drive IDs
        </div>

        {/* Error Banner */}
        {error && (
          <div style={styles.errorBox} role="alert">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0, marginTop: '1px' }}>
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleCheck} noValidate>
          <div style={styles.formRow}>
            <div style={styles.fieldGroup}>
              <label htmlFor="eligibility-studentId" style={styles.label}>Student ID *</label>
              <input
                id="eligibility-studentId"
                type="number"
                min="1"
                step="1"
                placeholder="e.g. 1"
                value={studentId}
                onChange={(e) => { setStudentId(e.target.value); setError(''); setResult(null); }}
                style={styles.input}
                disabled={loading}
                autoComplete="off"
              />
              <p style={styles.helperText}>Primary key ID of the Student entity (from /api/students)</p>
            </div>

            <div style={styles.fieldGroup}>
              <label htmlFor="eligibility-driveId" style={styles.label}>Placement Drive ID *</label>
              <input
                id="eligibility-driveId"
                type="number"
                min="1"
                step="1"
                placeholder="e.g. 10"
                value={driveId}
                onChange={(e) => { setDriveId(e.target.value); setError(''); setResult(null); }}
                style={styles.input}
                disabled={loading}
                autoComplete="off"
              />
              <p style={styles.helperText}>Primary key ID of the PlacementDrive entity (from /api/placement-drives)</p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <button
              id="eligibility-check-btn"
              type="submit"
              style={{ ...styles.primaryBtn, opacity: loading ? 0.65 : 1 }}
              disabled={loading}
            >
              {loading ? 'Checking…' : 'Check Eligibility'}
            </button>

            <button
              id="eligibility-reset-btn"
              type="button"
              style={styles.ghostBtn}
              onClick={handleReset}
              disabled={loading}
            >
              Reset
            </button>
          </div>
        </form>
      </div>

      {/* Result Card */}
      {result && (
        <div style={styles.card}>
          {/* Verdict Banner */}
          {result.eligible ? (
            <div style={styles.eligibleBanner}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#34d399" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}>
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <div>
                <div style={{ fontSize: '16px', fontWeight: '700', color: '#34d399', marginBottom: '3px' }}>
                  Eligible
                </div>
                <div style={{ fontSize: '13px', color: '#a7f3d0' }}>
                  Student #{result.studentId} meets all eligibility criteria for Drive #{result.driveId}.
                </div>
              </div>
            </div>
          ) : (
            <div style={styles.ineligibleBanner}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#f87171" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0, marginTop: '2px' }}>
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '16px', fontWeight: '700', color: '#f87171', marginBottom: '6px' }}>
                  Not Eligible
                </div>
                <div style={{ fontSize: '13px', color: '#fca5a5', marginBottom: '14px' }}>
                  Student #{result.studentId} does not meet {result.reasons?.length === 1 ? 'the following criterion' : 'the following criteria'} for Drive #{result.driveId}:
                </div>
                <ul style={styles.reasonsList}>
                  {(result.reasons || []).map((reason, idx) => (
                    <li key={idx} style={styles.reasonItem}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#f87171" strokeWidth="2.5" strokeLinecap="round" style={{ flexShrink: 0, marginTop: '1px' }}>
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                      {reason}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}

          <div style={styles.divider} />

          {/* CGPA Section */}
          <div style={styles.sectionTitle}>CGPA &amp; Backlogs</div>
          <InfoRow label="Student CGPA" value={result.studentCgpa !== null && result.studentCgpa !== undefined ? result.studentCgpa.toFixed(2) : '—'} mono />
          <InfoRow label="Minimum Required CGPA" value={result.minimumCgpa !== null && result.minimumCgpa !== undefined ? result.minimumCgpa.toFixed(2) : '—'} mono />
          <InfoRow label="Student Active Backlogs" value={result.studentActiveBacklogs ?? '—'} mono />
          <InfoRow label="Maximum Allowed Backlogs" value={result.maximumAllowedBacklogs ?? '—'} mono />

          <div style={styles.divider} />

          {/* Department Section */}
          <div style={styles.sectionTitle}>Department &amp; Branch</div>
          <InfoRow label="Student Department" value={result.studentDepartment || '—'} />
          <InfoRow
            label="Eligible Branches"
            value={result.eligibleBranches && result.eligibleBranches.trim() ? result.eligibleBranches : 'All branches eligible'}
          />

          <div style={styles.divider} />

          {/* Drive Status Section */}
          <div style={styles.sectionTitle}>Drive Status</div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0' }}>
            <span style={{ color: '#94a3b8', fontSize: '13px' }}>Drive Status</span>
            <StatusBadge status={result.driveStatus} />
          </div>
          <InfoRow label="Student ID" value={result.studentId} mono />
          <InfoRow label="Drive ID" value={result.driveId} mono />
        </div>
      )}

      {/* Usage Guide Card (shown when no result yet) */}
      {!result && !loading && (
        <div style={{ ...styles.card, border: '1px dashed #334155', background: 'rgba(30,41,59,0.4)' }}>
          <div style={{ ...styles.sectionTitle, marginBottom: '16px' }}>How it works</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {[
              ['1', 'Enter the Student\'s primary key ID (find it via GET /api/students)'],
              ['2', 'Enter the Placement Drive\'s primary key ID (find it via GET /api/placement-drives)'],
              ['3', 'Click "Check Eligibility" to query the live backend'],
              ['4', 'The backend evaluates: drive status, CGPA, backlogs, and department — all reasons are returned'],
            ].map(([num, text]) => (
              <div key={num} style={{ display: 'flex', gap: '14px', alignItems: 'flex-start' }}>
                <div style={{
                  width: '22px', height: '22px', borderRadius: '50%',
                  background: 'rgba(59,130,246,0.2)', border: '1px solid rgba(59,130,246,0.4)',
                  color: '#60a5fa', fontSize: '11px', fontWeight: '700',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  {num}
                </div>
                <p style={{ fontSize: '13px', color: '#94a3b8', margin: 0 }}>{text}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
