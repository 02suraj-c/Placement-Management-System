import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  GraduationCap,
  LayoutDashboard,
  Briefcase,
  FileText,
  CalendarCheck,
  Bell,
  User,
  Users,
  Building2,
  Calendar,
  CheckCircle,
  BarChart3,
  BookOpen,
  Shield,
  Layers,
} from 'lucide-react';

export default function Sidebar({ isOpen, onClose }) {
  const { user } = useAuth();
  const role = user?.role || 'ROLE_STUDENT';

  const getNavItems = () => {
    switch (role) {
      case 'ROLE_OFFICER':
      case 'ROLE_PLACEMENT_OFFICER':
        return [
          { label: 'Overview', to: '/placement/dashboard', icon: LayoutDashboard },
          { label: 'Drive Management', to: '/placement/drives', icon: Building2 },
          { label: 'Applicant Roster', to: '/placement/applications', icon: FileText },
          { label: 'Interview Scheduler', to: '/placement/interviews', icon: Calendar },
          { label: 'Question Bank', to: '/placement/questions', icon: BookOpen },
          { label: 'Eligibility Checker', to: '/placement/eligibility', icon: CheckCircle },
        ];
      case 'ROLE_FACULTY':
        return [
          { label: 'Faculty Dashboard', to: '/faculty/dashboard', icon: LayoutDashboard },
          { label: 'Student Verification', to: '/faculty/students', icon: CheckCircle },
          { label: 'Department Analytics', to: '/faculty/analytics', icon: BarChart3 },
        ];
      case 'ROLE_HOD':
        return [
          { label: 'HOD Analytics', to: '/hod/dashboard', icon: LayoutDashboard },
          { label: 'Department Placements', to: '/hod/records', icon: BarChart3 },
          { label: 'Company Insights', to: '/hod/companies', icon: Building2 },
        ];
      case 'ROLE_ADMIN':
        return [
          { label: 'Admin Dashboard', to: '/admin/dashboard', icon: LayoutDashboard },
          { label: 'User Directory', to: '/admin/users', icon: Users },
          { label: 'System Analytics', to: '/admin/stats', icon: Shield },
        ];
      case 'ROLE_STUDENT':
      default:
        return [
          { label: 'Dashboard', to: '/student/dashboard', icon: LayoutDashboard },
          { label: 'Question Bank & DSA', to: '/student/questions', icon: BookOpen },
          { label: 'Eligible Drives', to: '/student/drives', icon: Briefcase },
          { label: 'My Applications', to: '/student/applications', icon: FileText },
          { label: 'Interviews Hub', to: '/student/interviews', icon: CalendarCheck },
          { label: 'Interview Questions', to: '/student/questions', icon: BookOpen },
          { label: 'Notifications', to: '/student/notifications', icon: Bell },
          { label: 'Profile & Resume', to: '/student/profile', icon: User },
        ];
    }
  };

  const navItems = getNavItems();

  return (
    <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
      <div className="brand-header">
        <div className="brand-icon-box">
          <GraduationCap size={22} />
        </div>
        <div>
          <h1 className="brand-title">JECRC Placement Portal</h1>
          <p className="brand-sub">Campus Placement Cell</p>
        </div>
      </div>

      <nav className="nav-menu">
        <span className="nav-section-title">Navigation</span>
        {navItems.map((item) => {
          const IconComponent = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              onClick={onClose}
            >
              <IconComponent size={18} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-user">
        <div className="user-card">
          <div className="user-avatar">
            {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="user-info">
            <div className="user-name">{user?.name || 'User'}</div>
            <div className="user-role-badge">
              {role.replace('ROLE_', '').replace('_', ' ')}
            </div>
          </div>
        </div>
      </div>
    </aside>
  );
}
