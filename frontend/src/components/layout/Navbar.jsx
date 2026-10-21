import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { notificationService } from '../../services/notificationService';
import ThemeToggle from '../common/ThemeToggle';
import { Bell, LogOut, Menu, User, Sparkles } from 'lucide-react';

export default function Navbar({ onToggleSidebar }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (user && (user.role === 'ROLE_STUDENT' || !user.role)) {
      notificationService.getNotificationsByStudent(user.id || 1).then((notifs) => {
        const unread = notifs.filter((n) => !n.isRead).length;
        setUnreadCount(unread);
      }).catch(() => {});
    }
  }, [user]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleDisplayName = (role) => {
    switch (role) {
      case 'ROLE_ADMIN': return 'Administrator';
      case 'ROLE_OFFICER':
      case 'ROLE_PLACEMENT_OFFICER': return 'Placement Officer';
      case 'ROLE_FACULTY': return 'Faculty Coordinator';
      case 'ROLE_HOD': return 'Head of Department';
      case 'ROLE_STUDENT':
      default: return 'Student';
    }
  };

  return (
    <header className="top-navbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        <button
          onClick={onToggleSidebar}
          style={{
            background: 'transparent',
            border: 'none',
            color: 'var(--text-secondary)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            padding: '6px',
            borderRadius: 'var(--radius-sm)',
          }}
          className="mobile-menu-btn"
          aria-label="Toggle navigation menu"
        >
          <Menu size={22} />
        </button>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '15px', fontWeight: '800', color: 'var(--text-primary)', letterSpacing: '-0.01em' }}>
            JECRC Placement Portal
          </span>
          <span style={{
            fontSize: '12px',
            color: 'var(--text-secondary)',
            fontWeight: '500',
            padding: '2px 8px',
            borderRadius: '6px',
            background: 'var(--bg-main)',
            border: '1px solid var(--border-subtle)',
          }}>
            {getRoleDisplayName(user?.role)}
          </span>
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '4px',
            fontSize: '11px',
            padding: '2px 8px',
            borderRadius: '12px',
            background: 'var(--success-bg)',
            color: 'var(--success)',
            fontWeight: '600',
            border: '1px solid rgba(16, 185, 129, 0.3)',
          }}>
            <Sparkles size={11} />
            Demo Mode
          </span>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        {/* Global Dark / Light Theme Toggle */}
        <ThemeToggle />

        {user?.role === 'ROLE_STUDENT' && (
          <Link
            to="/student/notifications"
            style={{
              position: 'relative',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '38px',
              height: '38px',
              borderRadius: 'var(--radius-md)',
              background: 'var(--bg-card)',
              color: 'var(--text-secondary)',
              border: '1px solid var(--border-subtle)',
              transition: 'all 0.15s ease',
            }}
            title="Notifications"
            aria-label="View notifications"
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span
                style={{
                  position: 'absolute',
                  top: '-4px',
                  right: '-4px',
                  background: 'var(--danger)',
                  color: '#fff',
                  fontSize: '10px',
                  fontWeight: '700',
                  borderRadius: '10px',
                  minWidth: '18px',
                  height: '18px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  padding: '0 4px',
                  border: '2px solid var(--bg-surface)',
                }}
              >
                {unreadCount}
              </span>
            )}
          </Link>
        )}

        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          padding: '6px 12px',
          background: 'var(--bg-card)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)',
        }}>
          <div style={{
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            background: 'var(--primary)',
            color: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '12px',
            fontWeight: '700',
            flexShrink: 0,
          }}>
            {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-primary)', lineHeight: 1.2 }}>
              {user?.name || 'User'}
            </span>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
              {user?.email || ''}
            </span>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="btn btn-outline btn-sm"
          style={{ gap: '6px' }}
          title="Sign out of portal"
          aria-label="Log out"
        >
          <LogOut size={14} />
          <span>Logout</span>
        </button>
      </div>
    </header>
  );
}
