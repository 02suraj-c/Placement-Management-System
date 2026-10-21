import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';

export default function Register() {
  const { register } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_STUDENT');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await register(name, email, password, role);
      window.location.href = '/login?registered=true';
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h2 style={styles.title}>Create an Account</h2>
        <p style={styles.subtitle}>Register for Placement Management System</p>

        {error && <div style={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Full Name</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="John Doe"
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
              placeholder="user@example.com"
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
              <option value="ROLE_FACULTY">Faculty</option>
              <option value="ROLE_HOD">Head of Department (HOD)</option>
              <option value="ROLE_OFFICER">Placement Officer</option>
              <option value="ROLE_COMPANY">Company / Recruiter</option>
            </select>
          </div>

          <button type="submit" disabled={loading} style={styles.button}>
            {loading ? 'Creating account...' : 'Register'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account? <a href="/login" style={styles.link}>Sign in</a>
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
    maxWidth: '440px',
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
  select: {
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
