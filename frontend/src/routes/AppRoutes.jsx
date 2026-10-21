import React from 'react';
import { useAuth } from '../context/AuthContext';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#0f172a', color: '#fff' }}>
        Loading session...
      </div>
    );
  }

  if (!isAuthenticated) {
    window.location.href = '/login';
    return null;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return (
      <div style={{ padding: '40px', textAlign: 'center', background: '#0f172a', color: '#f87171', minHeight: '100vh' }}>
        <h2>403 - Access Denied</h2>
        <p>You do not have permission to view this section.</p>
        <a href="/login" style={{ color: '#60a5fa' }}>Return to Login</a>
      </div>
    );
  }

  return children;
};

export default function AppRoutes() {
  const pathname = window.location.pathname;

  if (pathname === '/register') {
    return <Register />;
  }

  if (pathname === '/login' || pathname === '/') {
    return <Login />;
  }

  return (
    <div style={{ background: '#0f172a', color: '#f8fafc', minHeight: '100vh', padding: '24px' }}>
      <h1>Dashboard Placeholder</h1>
      <p>Current Path: {pathname}</p>
      <a href="/login" style={{ color: '#60a5fa' }}>Back to Login</a>
    </div>
  );
}
