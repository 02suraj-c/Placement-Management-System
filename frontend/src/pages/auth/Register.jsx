import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { GraduationCap } from 'lucide-react';
import ThemeToggle from '../../components/common/ThemeToggle';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_STUDENT');
  const [year, setYear] = useState('2026');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await register(name, email, password, role, year);
      // Route user to appropriate portal or login
      navigate('/login?registered=true');
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.themeToggleContainer}>
        <ThemeToggle showLabel />
      </div>
      <div style={styles.card}>
        <div style={styles.logoBadge}>
          <GraduationCap size={28} color="#3b82f6" />
        </div>
        <h2 style={styles.title}>Create an Account</h2>
        <p style={styles.subtitle}>Register for JECRC Placement Portal</p>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Full Name</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Ananya Iyer"
              style={styles.input}
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="student@placement.edu"
              style={styles.input}
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Password</label>
            <input
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minimum 6 characters"
              style={styles.input}
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Select Role</label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value)}
              style={styles.select}
            >
              <option value="ROLE_STUDENT">Student</option>
              <option value="ROLE_FACULTY">Faculty Coordinator</option>
              <option value="ROLE_HOD">Head of Department (HOD)</option>
              <option value="ROLE_PLACEMENT_OFFICER">Placement Officer (TPO)</option>
              <option value="ROLE_COMPANY">Company Recruiter</option>
            </select>
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Graduation / Passing Year</label>
            <select
              value={year}
              onChange={(e) => setYear(e.target.value)}
              style={styles.select}
            >
              <option value="2024">2024 (Alumni)</option>
              <option value="2025">2025 (Graduating Soon)</option>
              <option value="2026">2026 (Batch of 2026 - Current)</option>
              <option value="2027">2027 (Pre-Final Year)</option>
              <option value="2028">2028 (Sophomore)</option>
              <option value="2029">2029 (Freshman)</option>
            </select>
          </div>

          <button type="submit" disabled={loading} style={styles.button}>
            {loading ? 'Creating account...' : 'Register'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" style={styles.link}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--bg-main)',
    color: 'var(--text-primary)',
    fontFamily: 'Inter, system-ui, sans-serif',
    padding: '24px',
    position: 'relative',
    transition: 'background-color 0.2s ease, color 0.2s ease',
  },
  themeToggleContainer: {
    position: 'absolute',
    top: '20px',
    right: '24px',
    zIndex: 10,
  },
  card: {
    width: '100%',
    maxWidth: '460px',
    background: 'var(--bg-card)',
    padding: '36px',
    borderRadius: '16px',
    boxShadow: 'var(--shadow-card)',
    border: '1px solid var(--border-subtle)',
    transition: 'background-color 0.2s ease, border-color 0.2s ease',
  },
  logoBadge: {
    width: '48px',
    height: '48px',
    margin: '0 auto 16px',
    borderRadius: '12px',
    background: 'var(--primary-subtle)',
    border: '1px solid rgba(59, 130, 246, 0.25)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    margin: '0 0 6px 0',
    fontSize: '22px',
    fontWeight: '700',
    textAlign: 'center',
    color: 'var(--text-primary)',
    letterSpacing: '-0.02em',
  },
  subtitle: {
    margin: '0 0 22px 0',
    fontSize: '13px',
    textAlign: 'center',
    color: 'var(--text-secondary)',
  },
  error: {
    padding: '12px',
    borderRadius: '8px',
    background: 'var(--danger-subtle)',
    border: '1px solid rgba(239, 68, 68, 0.3)',
    color: 'var(--danger)',
    fontSize: '13px',
    marginBottom: '18px',
    textAlign: 'center',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '13px',
    fontWeight: '500',
    color: 'var(--text-secondary)',
  },
  input: {
    padding: '11px 14px',
    borderRadius: '8px',
    background: 'var(--bg-input)',
    border: '1px solid var(--border-input)',
    color: 'var(--text-primary)',
    fontSize: '14px',
    outline: 'none',
    transition: 'border-color 0.15s ease, background-color 0.2s ease',
  },
  select: {
    padding: '11px 14px',
    borderRadius: '8px',
    background: 'var(--bg-input)',
    border: '1px solid var(--border-input)',
    color: 'var(--text-primary)',
    fontSize: '14px',
    outline: 'none',
    transition: 'border-color 0.15s ease, background-color 0.2s ease',
  },
  button: {
    padding: '12px',
    borderRadius: '8px',
    background: 'var(--primary)',
    border: 'none',
    color: '#ffffff',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    marginTop: '6px',
    transition: 'background-color 0.15s ease',
  },
  footer: {
    marginTop: '22px',
    fontSize: '13px',
    textAlign: 'center',
    color: 'var(--text-secondary)',
  },
  link: {
    color: 'var(--primary)',
    textDecoration: 'none',
    fontWeight: '500',
  },
};
