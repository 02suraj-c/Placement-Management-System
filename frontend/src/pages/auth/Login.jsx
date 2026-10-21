import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(email, password);
      // Route user to appropriate portal based on role
      switch (user.role) {
        case 'ROLE_ADMIN':
          window.location.href = '/admin/dashboard';
          break;
        case 'ROLE_OFFICER':
        case 'ROLE_PLACEMENT_OFFICER':
          window.location.href = '/placement/dashboard';
          break;
        case 'ROLE_FACULTY':
          window.location.href = '/faculty/dashboard';
          break;
        case 'ROLE_HOD':
          window.location.href = '/hod/dashboard';
          break;
        case 'ROLE_STUDENT':
        default:
          window.location.href = '/student/dashboard';
          break;
      }
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Placement Management System</h2>
        <p style={styles.subtitle}>Sign in to access your portal</p>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@example.com"
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

        <p style={styles.footer}>
          Don't have an account? <a href="/register" style={styles.link}>Register here</a>
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
    background: '#0f172a',
    color: '#e2e8f0',
    fontFamily: 'Inter, system-ui, sans-serif',
    padding: '20px',
  },
  card: {
    width: '100%',
    maxWidth: '420px',
    background: '#1e293b',
    padding: '36px',
    borderRadius: '16px',
    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
    border: '1px solid #334155',
  },
  title: {
    margin: '0 0 8px 0',
    fontSize: '22px',
    fontWeight: '700',
    textAlign: 'center',
    color: '#f8fafc',
  },
  subtitle: {
    margin: '0 0 24px 0',
    fontSize: '14px',
    textAlign: 'center',
    color: '#94a3b8',
  },
  error: {
    padding: '12px',
    borderRadius: '8px',
    background: 'rgba(239, 68, 68, 0.2)',
    border: '1px solid #ef4444',
    color: '#fca5a5',
    fontSize: '13px',
    marginBottom: '20px',
    textAlign: 'center',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '18px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '13px',
    fontWeight: '500',
    color: '#cbd5e1',
  },
  input: {
    padding: '12px',
    borderRadius: '8px',
    background: '#0f172a',
    border: '1px solid #334155',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
  },
  button: {
    padding: '12px',
    borderRadius: '8px',
    background: '#3b82f6',
    border: 'none',
    color: '#ffffff',
    fontSize: '15px',
    fontWeight: '600',
    cursor: 'pointer',
    marginTop: '6px',
  },
  footer: {
    marginTop: '24px',
    fontSize: '13px',
    textAlign: 'center',
    color: '#94a3b8',
  },
  link: {
    color: '#60a5fa',
    textDecoration: 'none',
  },
};
