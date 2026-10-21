import React from 'react';
import { Navigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
          background: 'var(--bg-main)',
          color: 'var(--text-primary)',
          fontSize: '15px',
        }}
      >
        Initializing placement session...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = user?.role;
    const hasPermission = allowedRoles.some((role) => {
      if (role === 'ROLE_PLACEMENT_OFFICER' || role === 'ROLE_OFFICER') {
        return userRole === 'ROLE_PLACEMENT_OFFICER' || userRole === 'ROLE_OFFICER';
      }
      return userRole === role;
    });

    if (!hasPermission) {
      return (
        <div
          style={{
            padding: '60px 20px',
            textAlign: 'center',
            background: 'var(--bg-main)',
            color: '#f87171',
            minHeight: '100vh',
          }}
        >
          <h2 style={{ fontSize: '28px', fontWeight: '800', marginBottom: '10px' }}>
            403 — Unauthorized Role
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginBottom: '24px' }}>
            Your account ({user?.role}) does not have security credentials to access this portal area.
          </p>
          <Link to="/login" className="btn btn-primary">
            Switch Account
          </Link>
        </div>
      );
    }
  }

  return children;
}
