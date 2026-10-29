import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ProtectedRoute from './ProtectedRoute';

// Auth Pages
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';

// Layout
import AppLayout from '../components/layout/AppLayout';

// Student Pages
import StudentDashboard from '../pages/student/StudentDashboard';
import EligibleDrives from '../pages/student/EligibleDrives';
import Applications from '../pages/student/Applications';
import Interviews from '../pages/student/Interviews';
import Notifications from '../pages/student/Notifications';
import StudentProfile from '../pages/student/StudentProfile';

// Officer Pages
import OfficerDashboard from '../pages/officer/OfficerDashboard';
import DriveManagement from '../pages/officer/DriveManagement';
import ApplicantReview from '../pages/officer/ApplicantReview';
import InterviewScheduler from '../pages/officer/InterviewScheduler';
import QuestionBank from '../pages/officer/QuestionBank';

// Placement Module Pages (Person 3)
import Eligibility from '../pages/placement/Eligibility';

// Faculty, HOD & Admin Pages
import FacultyDashboard from '../pages/faculty/FacultyDashboard';
import HodDashboard from '../pages/hod/HodDashboard';
import AdminDashboard from '../pages/admin/AdminDashboard';

// Helper component for default root path redirection
const RootRedirect = () => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'var(--bg-main)', color: 'var(--text-primary)' }}>
        Loading session...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  switch (user?.role) {
    case 'ROLE_ADMIN':
      return <Navigate to="/admin/dashboard" replace />;
    case 'ROLE_OFFICER':
    case 'ROLE_PLACEMENT_OFFICER':
      return <Navigate to="/placement/dashboard" replace />;
    case 'ROLE_FACULTY':
      return <Navigate to="/faculty/dashboard" replace />;
    case 'ROLE_HOD':
      return <Navigate to="/hod/dashboard" replace />;
    case 'ROLE_STUDENT':
    default:
      return <Navigate to="/student/dashboard" replace />;
  }
};

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public Auth Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<RootRedirect />} />

      {/* Student Protected Routes */}
      <Route
        path="/student"
        element={
          <ProtectedRoute allowedRoles={['ROLE_STUDENT']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<StudentDashboard />} />
        <Route path="questions" element={<QuestionBank />} />
        <Route path="drives" element={<EligibleDrives />} />
        <Route path="applications" element={<Applications />} />
        <Route path="interviews" element={<Interviews />} />
        <Route path="questions" element={<QuestionBank />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="profile" element={<StudentProfile />} />
      </Route>

      {/* Placement Officer Protected Routes */}
      <Route
        path="/placement"
        element={
          <ProtectedRoute allowedRoles={['ROLE_OFFICER', 'ROLE_PLACEMENT_OFFICER']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<OfficerDashboard />} />
        <Route path="drives" element={<DriveManagement />} />
        <Route path="applications" element={<ApplicantReview />} />
        <Route path="interviews" element={<InterviewScheduler />} />
        <Route path="questions" element={<QuestionBank />} />
        <Route path="eligibility" element={<Eligibility />} />
      </Route>

      {/* Faculty Protected Routes */}
      <Route
        path="/faculty"
        element={
          <ProtectedRoute allowedRoles={['ROLE_FACULTY']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<FacultyDashboard />} />
        <Route path="students" element={<FacultyDashboard />} />
        <Route path="analytics" element={<FacultyDashboard />} />
      </Route>

      {/* HOD Protected Routes */}
      <Route
        path="/hod"
        element={
          <ProtectedRoute allowedRoles={['ROLE_HOD']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<HodDashboard />} />
        <Route path="records" element={<HodDashboard />} />
        <Route path="companies" element={<HodDashboard />} />
      </Route>

      {/* Admin Protected Routes */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="users" element={<AdminDashboard />} />
        <Route path="stats" element={<AdminDashboard />} />
      </Route>

      {/* Catch-all Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
