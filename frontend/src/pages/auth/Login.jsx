import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ShieldCheck, UserCheck, GraduationCap, Briefcase, Award } from 'lucide-react';
import ThemeToggle from '../../components/common/ThemeToggle';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const routeUserByRole = (role) => {
    switch (role) {
      case 'ROLE_ADMIN':
        navigate('/admin/dashboard');
        break;
      case 'ROLE_OFFICER':
      case 'ROLE_PLACEMENT_OFFICER':
        navigate('/placement/dashboard');
        break;
      case 'ROLE_FACULTY':
        navigate('/faculty/dashboard');
        break;
      case 'ROLE_HOD':
        navigate('/hod/dashboard');
        break;
      case 'ROLE_STUDENT':
      default:
        navigate('/student/dashboard');
        break;
    }
  };

  const handleSubmit = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(email, password);
      routeUserByRole(user.role);
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError('');
    setLoading(true);
    try {
      const user = await login(demoEmail, demoPass);
      routeUserByRole(user.role);
    } catch (err) {
      setError(err.message || 'Quick login failed.');
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
        <h2 style={styles.title}>JECRC Placement Portal</h2>
        <p style={styles.subtitle}>Sign in to access your role-based portal</p>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@placement.edu"
              style={styles.input}
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              style={styles.input}
            />
          </div>

          <button type="submit" disabled={loading} style={styles.button}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        {/* Quick Demo Sign-in Selector for Demo */}
        <div style={styles.demoSection}>
          <div style={styles.demoHeader}>
            <span style={styles.demoTitle}>QUICK DEMO SIGN-IN (OCT 23 DEMO)</span>
          </div>
          <div style={styles.demoGrid}>
            <button
              type="button"
              onClick={() => handleQuickLogin('student@placement.edu', 'student123')}
              style={styles.demoBtn}
            >
              <GraduationCap size={14} color="#60a5fa" />
              <span>Student (Aarav)</span>
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('officer@placement.edu', 'officer123')}
              style={styles.demoBtn}
            >
              <Briefcase size={14} color="#34d399" />
              <span>Placement Officer</span>
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('faculty@placement.edu', 'faculty123')}
              style={styles.demoBtn}
            >
              <UserCheck size={14} color="#c084fc" />
              <span>Faculty (Coordinator)</span>
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('hod@placement.edu', 'hod123')}
              style={styles.demoBtn}
            >
              <Award size={14} color="#fbbf24" />
              <span>HOD (CSE)</span>
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('admin@placement.edu', 'admin123')}
              style={{ ...styles.demoBtn, gridColumn: 'span 2' }}
            >
              <ShieldCheck size={14} color="#f87171" />
              <span>System Administrator</span>
            </button>
          </div>
        </div>

        <p style={styles.footer}>
          Don't have an account?{' '}
          <Link to="/register" style={styles.link}>
            Register here
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
  button: {
    padding: '12px',
    borderRadius: '8px',
    background: 'var(--primary)',
    border: 'none',
    color: '#ffffff',
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    marginTop: '4px',
    transition: 'background-color 0.15s ease',
  },
  demoSection: {
    marginTop: '24px',
    paddingTop: '20px',
    borderTop: '1px solid var(--border-subtle)',
  },
  demoHeader: {
    textAlign: 'center',
    marginBottom: '12px',
  },
  demoTitle: {
    fontSize: '11px',
    fontWeight: '700',
    letterSpacing: '0.06em',
    color: 'var(--text-muted)',
  },
  demoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, 1fr)',
    gap: '8px',
  },
  demoBtn: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    padding: '8px 10px',
    borderRadius: '8px',
    background: 'var(--bg-input)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    fontSize: '12px',
    fontWeight: '500',
    cursor: 'pointer',
    transition: 'all 0.15s ease',
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
