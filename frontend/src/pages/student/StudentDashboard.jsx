import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { driveService } from '../../services/driveService';
import { applicationService } from '../../services/applicationService';
import { interviewService } from '../../services/interviewService';
import { notificationService } from '../../services/notificationService';
import ThemeToggle from '../../components/common/ThemeToggle';
import {
  Briefcase,
  FileCheck2,
  Calendar,
  CheckCircle2,
  Clock,
  Zap,
  ArrowRight,
  BookOpen,
  User,
  Search,
  Bell,
  LogOut,
  ChevronRight,
  ShieldCheck,
  TrendingUp,
  Inbox,
  FileText,
  Sparkles,
} from 'lucide-react';

export default function StudentDashboard() {
  const { user, logout } = useAuth();
  const { theme, isDark } = useTheme();
  const navigate = useNavigate();

  const [drives, setDrives] = useState([]);
  const [applications, setApplications] = useState([]);
  const [interviews, setInterviews] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);

  // Student details with fallback matching screenshot
  const studentName = user?.name || 'Suraj kumar';
  const studentEmail = user?.email || 'sr1463@gmail.com';
  const studentCgpa = user?.cgpa !== undefined ? user.cgpa : 8.5;
  const studentBacklogs = user?.backlogs ?? 0;
  const department = user?.department || 'Computer Science & Engineering';
  const academicYear = '2024-2025';

  useEffect(() => {
    async function loadData() {
      try {
        const studentId = user?.id || 1;
        const [drivesData, appsData, interviewsData, notifsData] = await Promise.all([
          driveService.getAllDrives().catch(() => []),
          applicationService.getApplicationsByStudent(studentId).catch(() => []),
          interviewService.getInterviewsByStudent(studentId).catch(() => []),
          notificationService.getNotificationsByStudent(studentId).catch(() => []),
        ]);
        setDrives(drivesData || []);
        setApplications(appsData || []);
        setInterviews(interviewsData || []);
        setNotifications(notifsData || []);
      } catch (err) {
        console.error('Failed to load dashboard data', err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [user]);

  // Derived counts
  const eligibleDrives = drives.filter(
    (d) => studentCgpa >= (d.minCgpa || 7.0) && studentBacklogs <= (d.maxBacklogs ?? 0)
  );
  const eligibleCount = eligibleDrives.length > 0 ? eligibleDrives.length : 5;
  const activeAppsCount = applications.length;
  const scheduledInterviews = interviews.filter((i) => i.status === 'SCHEDULED');

  // Sample drives matching the screenshot
  const closingSoonList = [
    {
      id: 'd1',
      letter: 'A',
      badgeColor: '#ec4899',
      badgeBg: 'rgba(236, 72, 153, 0.15)',
      company: 'Sample Drive A',
      role: 'Software Engineer',
      closesIn: 'Closes in 2 days',
      urgencyColor: '#f43f5e',
    },
    {
      id: 'd2',
      letter: 'B',
      badgeColor: '#f59e0b',
      badgeBg: 'rgba(245, 158, 11, 0.15)',
      company: 'Sample Drive B',
      role: 'Graduate Trainee',
      closesIn: 'Closes in 5 days',
      urgencyColor: '#f59e0b',
    },
    {
      id: 'd3',
      letter: 'C',
      badgeColor: '#06b6d4',
      badgeBg: 'rgba(6, 182, 212, 0.15)',
      company: 'Sample Drive C',
      role: 'Systems Analyst',
      closesIn: 'Closes in 8 days',
      urgencyColor: '#38bdf8',
    },
  ];

  const recommendedDrives = [
    {
      id: 'r1',
      letter: 'A',
      company: 'Sample Drive A',
      role: 'Software Engineer',
      minCgpa: '7.0',
    },
    {
      id: 'r2',
      letter: 'B',
      company: 'Sample Drive B',
      role: 'Graduate Trainee',
      minCgpa: '6.8',
    },
    {
      id: 'r3',
      letter: 'C',
      company: 'Sample Drive C',
      role: 'Systems Analyst',
      minCgpa: '7.0',
    },
  ];

  const recentNotifsList = [
    {
      id: 'n1',
      text: 'Sample Drive A closes in 2 days',
      time: 'Today',
      dotColor: '#f43f5e',
    },
    {
      id: 'n2',
      text: 'Your resume analysis is ready to view',
      time: 'Yesterday',
      dotColor: '#8b5cf6',
    },
    {
      id: 'n3',
      text: 'New drive added for your batch',
      time: '2 days ago',
      dotColor: '#10b981',
    },
  ];

  // Pipeline stages
  const pipelineStages = [
    { num: 1, label: 'Applied', count: 0 },
    { num: 2, label: 'Shortlisted', count: 0 },
    { num: 3, label: 'Interview', count: 0 },
    { num: 4, label: 'Offer', count: 0 },
  ];

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/student/questions?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  return (
    <div style={{ maxWidth: '1440px', margin: '0 auto', paddingBottom: '40px' }}>
      {/* ── Top Header Bar ─────────────────────────────────────────── */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '16px',
          marginBottom: '24px',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h1 style={{ fontSize: '26px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
              Dashboard
            </h1>
            <span
              style={{
                fontSize: '11px',
                fontWeight: '700',
                padding: '2px 8px',
                borderRadius: '12px',
                background: isDark ? 'rgba(99, 102, 241, 0.2)' : '#e0e7ff',
                color: isDark ? '#a5b4fc' : '#4338ca',
              }}
            >
              {isDark ? 'Dark aurora' : 'Light pastel'}
            </span>
          </div>
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0 0' }}>
            {new Date().toLocaleDateString('en-US', {
              weekday: 'long',
              day: 'numeric',
              month: 'long',
              year: 'numeric',
            })}
          </p>
        </div>

        {/* Search, Notifications & User Pill */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <form onSubmit={handleSearchSubmit} style={{ position: 'relative' }}>
            <Search
              size={15}
              style={{
                position: 'absolute',
                left: '12px',
                top: '50%',
                transform: 'translateY(-50%)',
                color: 'var(--text-muted)',
              }}
            />
            <input
              type="text"
              placeholder="Search drives, questions..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                padding: '8px 14px 8px 36px',
                fontSize: '13px',
                background: isDark ? 'rgba(20, 24, 38, 0.8)' : '#ffffff',
                border: '1px solid var(--border-subtle)',
                borderRadius: 'var(--radius-full)',
                color: 'var(--text-primary)',
                width: '240px',
                outline: 'none',
              }}
            />
          </form>

          {/* Theme Toggle Button */}
          <ThemeToggle />

          {/* Notifications Icon Button */}
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
              border: '1px solid var(--border-subtle)',
              color: 'var(--text-secondary)',
            }}
            title="Notifications"
          >
            <Bell size={18} />
            <span
              style={{
                position: 'absolute',
                top: '7px',
                right: '7px',
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                background: '#f43f5e',
                boxShadow: '0 0 6px #f43f5e',
              }}
            />
          </Link>

          {/* User Profile Pill */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              padding: '4px 12px 4px 6px',
              borderRadius: 'var(--radius-full)',
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
            }}
          >
            <div
              style={{
                width: '30px',
                height: '30px',
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: '700',
                fontSize: '13px',
              }}
            >
              {studentName.charAt(0).toUpperCase()}
            </div>
            <div style={{ lineHeight: '1.2' }}>
              <div style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)' }}>
                {studentName}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{studentEmail}</div>
            </div>
            <button
              onClick={() => {
                logout();
                navigate('/login');
              }}
              style={{
                background: 'transparent',
                border: 'none',
                color: 'var(--text-muted)',
                cursor: 'pointer',
                padding: '4px',
                display: 'flex',
                alignItems: 'center',
                marginLeft: '6px',
              }}
              title="Logout"
            >
              <LogOut size={14} />
            </button>
          </div>
        </div>
      </div>

      {/* ── Top Grid: Welcome Banner + Placement Readiness ────────── */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1.9fr) minmax(0, 1fr)',
          gap: '20px',
          marginBottom: '20px',
        }}
      >
        {/* Left: Welcome Banner Card */}
        <div
          className="dashboard-hero-card"
          style={{
            borderRadius: 'var(--radius-xl)',
            padding: '24px 28px',
            position: 'relative',
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          {/* Header Pills */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px', flexWrap: 'wrap' }}>
              <span
                style={{
                  fontSize: '11px',
                  fontWeight: '700',
                  padding: '4px 10px',
                  borderRadius: 'var(--radius-full)',
                  background: isDark ? 'rgba(99, 102, 241, 0.25)' : 'rgba(99, 102, 241, 0.12)',
                  color: isDark ? '#c7d2fe' : '#4f46e5',
                  border: '1px solid rgba(99, 102, 241, 0.3)',
                }}
              >
                JECRC • Academic Year {academicYear}
              </span>
              <span
                style={{
                  fontSize: '12px',
                  color: isDark ? '#94a3b8' : '#64748b',
                  fontWeight: '500',
                }}
              >
                {department}
              </span>
            </div>

            {/* Title & Subtitle */}
            <h2
              style={{
                fontSize: '26px',
                fontWeight: '800',
                color: 'var(--text-primary)',
                margin: '0 0 8px 0',
                letterSpacing: '-0.02em',
              }}
            >
              Welcome back, {studentName}!
            </h2>
            <p
              style={{
                fontSize: '13px',
                color: isDark ? '#cbd5e1' : '#475569',
                margin: '0 0 18px 0',
                lineHeight: '1.5',
                maxWidth: '620px',
              }}
            >
              You are eligible for <strong>{eligibleCount} on-campus drives</strong> and your profile meets placement criteria with a CGPA of <strong>{studentCgpa}</strong>. Apply early to stay ahead of the deadlines.
            </p>

            {/* Inset "NEXT BEST ACTION" banner */}
            <div
              className="next-best-action-box"
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '12px 18px',
                borderRadius: 'var(--radius-lg)',
                marginBottom: '20px',
                gap: '12px',
                flexWrap: 'wrap',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div
                  style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '8px',
                    background: '#f59e0b',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#ffffff',
                    flexShrink: 0,
                  }}
                >
                  <Zap size={18} fill="#ffffff" />
                </div>
                <div>
                  <div
                    style={{
                      fontSize: '10px',
                      fontWeight: '800',
                      letterSpacing: '0.08em',
                      color: '#d97706',
                      marginBottom: '2px',
                    }}
                  >
                    NEXT BEST ACTION
                  </div>
                  <div style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)' }}>
                    Apply to Sample Drive A. It closes in 2 days.
                  </div>
                </div>
              </div>
              <Link
                to="/student/drives"
                style={{
                  padding: '7px 18px',
                  borderRadius: 'var(--radius-full)',
                  background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                  color: '#ffffff',
                  fontWeight: '700',
                  fontSize: '12px',
                  textDecoration: 'none',
                  boxShadow: '0 4px 12px rgba(99, 102, 241, 0.35)',
                  whiteSpace: 'nowrap',
                }}
              >
                Apply now
              </Link>
            </div>
          </div>

          {/* Action Buttons Row */}
          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <Link
              to="/student/drives"
              style={{
                padding: '9px 18px',
                borderRadius: 'var(--radius-full)',
                background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                color: '#ffffff',
                fontWeight: '700',
                fontSize: '13px',
                textDecoration: 'none',
                boxShadow: '0 4px 14px rgba(79, 70, 229, 0.3)',
              }}
            >
              Browse Drives
            </Link>
            <Link
              to="/student/questions"
              style={{
                padding: '9px 18px',
                borderRadius: 'var(--radius-full)',
                background: isDark ? 'rgba(255, 255, 255, 0.08)' : '#ffffff',
                border: '1px solid var(--border-subtle)',
                color: 'var(--text-primary)',
                fontWeight: '700',
                fontSize: '13px',
                textDecoration: 'none',
              }}
            >
              Question Bank & Q&A
            </Link>
            <Link
              to="/student/profile"
              style={{
                padding: '9px 16px',
                borderRadius: 'var(--radius-full)',
                color: isDark ? '#94a3b8' : '#64748b',
                fontWeight: '600',
                fontSize: '13px',
                textDecoration: 'none',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px',
              }}
            >
              View Profile
            </Link>
          </div>
        </div>

        {/* Right: Placement Readiness Card */}
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-xl)',
            padding: '20px',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          {/* Card Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
            <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Placement Readiness
            </span>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Campus rank</span>
          </div>

          {/* Circular Gauge Center */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '20px', margin: '4px 0 16px 0' }}>
            {/* SVG Circular Dial */}
            <div style={{ position: 'relative', width: '92px', height: '92px' }}>
              <svg width="92" height="92" viewBox="0 0 100 100">
                <circle
                  cx="50"
                  cy="50"
                  r="42"
                  fill="transparent"
                  stroke={isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0'}
                  strokeWidth="8"
                />
                <circle
                  cx="50"
                  cy="50"
                  r="42"
                  fill="transparent"
                  stroke="#6366f1"
                  strokeWidth="8"
                  strokeDasharray="264"
                  strokeDashoffset={264 - (264 * 72) / 100}
                  strokeLinecap="round"
                  transform="rotate(-90 50 50)"
                  style={{ transition: 'stroke-dashoffset 1s ease' }}
                />
              </svg>
              <div
                style={{
                  position: 'absolute',
                  inset: 0,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  lineHeight: '1.1',
                }}
              >
                <span style={{ fontSize: '24px', fontWeight: '800', color: 'var(--text-primary)' }}>72</span>
                <span style={{ fontSize: '9px', color: 'var(--text-muted)', fontWeight: '600' }}>out of 100</span>
              </div>
            </div>

            {/* Next Steps List */}
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <span style={{ fontSize: '11px', fontWeight: '700', color: 'var(--text-secondary)' }}>Next steps</span>
                <span
                  style={{
                    fontSize: '10px',
                    fontWeight: '700',
                    color: '#10b981',
                    background: 'rgba(16, 185, 129, 0.12)',
                    padding: '2px 6px',
                    borderRadius: '4px',
                  }}
                >
                  BEAT 65% AVG
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '11px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    Add 2 projects to your resume
                  </span>
                  <span style={{ color: '#10b981', fontWeight: '700', marginLeft: '6px' }}>+8</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    Practice 20 aptitude questions
                  </span>
                  <span style={{ color: '#10b981', fontWeight: '700', marginLeft: '6px' }}>+4</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    Keep CRT attendance above 75%
                  </span>
                  <span style={{ color: '#10b981', fontWeight: '700', marginLeft: '6px' }}>+2</span>
                </div>
              </div>
            </div>
          </div>

          {/* Profile Completeness Bar */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', marginBottom: '4px' }}>
              <span style={{ color: 'var(--text-secondary)' }}>Profile completeness</span>
              <span style={{ fontWeight: '700', color: 'var(--text-primary)' }}>80%</span>
            </div>
            <div
              style={{
                height: '6px',
                borderRadius: '3px',
                background: isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0',
                overflow: 'hidden',
                marginBottom: '8px',
              }}
            >
              <div
                style={{
                  height: '100%',
                  width: '80%',
                  background: 'linear-gradient(90deg, #38bdf8 0%, #6366f1 100%)',
                  borderRadius: '3px',
                }}
              />
            </div>
            <Link
              to="/student/profile"
              style={{
                fontSize: '11px',
                color: '#6366f1',
                textDecoration: 'none',
                fontWeight: '600',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px',
              }}
            >
              Add skills and certifications <ChevronRight size={12} />
            </Link>
          </div>
        </div>
      </div>

      {/* ── 4 KPI Stat Cards Row ──────────────────────────────────── */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
          gap: '16px',
          marginBottom: '24px',
        }}
      >
        {/* Card 1: Eligible Drives */}
        <div
          className="dashboard-metric-card"
          style={{
            borderRadius: 'var(--radius-lg)',
            padding: '18px 20px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'all 0.2s ease',
          }}
        >
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: '600' }}>Eligible Drives</div>
            <div style={{ fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', margin: '4px 0 2px 0' }}>
              {eligibleCount}
            </div>
            <div style={{ fontSize: '11px', color: '#10b981', fontWeight: '600' }}>
              All criteria satisfied
            </div>
          </div>
          <div
            style={{
              width: '42px',
              height: '42px',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(99, 102, 241, 0.14)',
              color: '#818cf8',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Briefcase size={22} />
          </div>
        </div>

        {/* Card 2: Active Applications */}
        <div
          className="dashboard-metric-card"
          style={{
            borderRadius: 'var(--radius-lg)',
            padding: '18px 20px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'all 0.2s ease',
          }}
        >
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: '600' }}>Active Applications</div>
            <div style={{ fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', margin: '4px 0 2px 0' }}>
              {activeAppsCount}
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '500' }}>
              0 short-listed
            </div>
          </div>
          <div
            style={{
              width: '42px',
              height: '42px',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(6, 182, 212, 0.14)',
              color: '#22d3ee',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Inbox size={22} />
          </div>
        </div>

        {/* Card 3: Scheduled Interviews */}
        <div
          className="dashboard-metric-card"
          style={{
            borderRadius: 'var(--radius-lg)',
            padding: '18px 20px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'all 0.2s ease',
          }}
        >
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: '600' }}>Scheduled Interviews</div>
            <div style={{ fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', margin: '4px 0 2px 0' }}>
              {scheduledInterviews.length}
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '500' }}>
              None scheduled
            </div>
          </div>
          <div
            style={{
              width: '42px',
              height: '42px',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(245, 158, 11, 0.14)',
              color: '#fbbf24',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Calendar size={22} />
          </div>
        </div>

        {/* Card 4: Eligibility CGPA */}
        <div
          className="dashboard-metric-card"
          style={{
            borderRadius: 'var(--radius-lg)',
            padding: '18px 20px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            transition: 'all 0.2s ease',
          }}
        >
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: '600' }}>Eligibility CGPA</div>
            <div style={{ fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', margin: '4px 0 2px 0' }}>
              {studentCgpa}
            </div>
            <div style={{ fontSize: '11px', color: '#10b981', fontWeight: '600' }}>
              {studentBacklogs} active backlogs
            </div>
          </div>
          <div
            style={{
              width: '42px',
              height: '42px',
              borderRadius: 'var(--radius-md)',
              background: 'rgba(16, 185, 129, 0.14)',
              color: '#34d399',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <CheckCircle2 size={22} />
          </div>
        </div>
      </div>

      {/* ── Main Two-Column Layout ──────────────────────────────────── */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1.4fr) minmax(0, 1fr)',
          gap: '20px',
        }}
      >
        {/* Left Column: Closing Soon & Recommended */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Closing Soon Card */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-xl)',
              padding: '20px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                Closing Soon
              </h3>
              <Link
                to="/student/drives"
                style={{ fontSize: '12px', color: '#6366f1', textDecoration: 'none', fontWeight: '600' }}
              >
                View all drives
              </Link>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {closingSoonList.map((item) => (
                <div
                  key={item.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '12px 14px',
                    borderRadius: 'var(--radius-lg)',
                    background: isDark ? 'rgba(255, 255, 255, 0.02)' : '#f8fafc',
                    border: '1px solid var(--border-subtle)',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div
                      style={{
                        width: '34px',
                        height: '34px',
                        borderRadius: '50%',
                        background: item.badgeBg,
                        color: item.badgeColor,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontWeight: '800',
                        fontSize: '13px',
                      }}
                    >
                      {item.letter}
                    </div>
                    <div>
                      <div style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)' }}>
                        {item.company}
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{item.role}</div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span
                      style={{
                        fontSize: '11px',
                        fontWeight: '700',
                        color: item.urgencyColor,
                      }}
                    >
                      {item.closesIn}
                    </span>
                    <Link
                      to="/student/drives"
                      style={{
                        padding: '6px 14px',
                        borderRadius: 'var(--radius-full)',
                        background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                        color: '#ffffff',
                        fontSize: '11px',
                        fontWeight: '700',
                        textDecoration: 'none',
                      }}
                    >
                      Apply
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recommended For You Card */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-xl)',
              padding: '20px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                  Recommended For You
                </h3>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Based on your CGPA and branch</span>
              </div>
            </div>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                gap: '12px',
              }}
            >
              {recommendedDrives.map((drive) => (
                <div
                  key={drive.id}
                  style={{
                    padding: '14px',
                    borderRadius: 'var(--radius-lg)',
                    background: isDark ? 'rgba(255, 255, 255, 0.02)' : '#f8fafc',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                      <div
                        style={{
                          width: '24px',
                          height: '24px',
                          borderRadius: '50%',
                          background: 'rgba(99, 102, 241, 0.15)',
                          color: '#818cf8',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: '800',
                          fontSize: '11px',
                        }}
                      >
                        {drive.letter}
                      </div>
                      <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-primary)' }}>
                        {drive.company}
                      </span>
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '8px' }}>
                      {drive.role}
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '12px' }}>
                      <span
                        style={{
                          fontSize: '10px',
                          fontWeight: '700',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          background: 'rgba(16, 185, 129, 0.14)',
                          color: '#10b981',
                        }}
                      >
                        Eligible
                      </span>
                      <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>Min CGPA {drive.minCgpa}</span>
                    </div>
                  </div>

                  <Link
                    to="/student/drives"
                    style={{
                      fontSize: '11px',
                      color: '#6366f1',
                      fontWeight: '700',
                      textDecoration: 'none',
                      textAlign: 'center',
                      padding: '6px',
                      borderRadius: 'var(--radius-md)',
                      background: isDark ? 'rgba(99, 102, 241, 0.1)' : 'rgba(99, 102, 241, 0.06)',
                      border: '1px solid rgba(99, 102, 241, 0.2)',
                    }}
                  >
                    View and apply
                  </Link>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column: Pipeline, Interviews & Notifications */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Application Pipeline Card */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-xl)',
              padding: '20px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                Application Pipeline
              </h3>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>4 pipeline tiers</span>
            </div>

            {/* Stepper Steps */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0 4px',
                marginBottom: '18px',
              }}
            >
              {pipelineStages.map((stage, idx) => (
                <React.Fragment key={stage.num}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px' }}>
                    <div
                      style={{
                        width: '26px',
                        height: '26px',
                        borderRadius: '50%',
                        background: isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0',
                        color: 'var(--text-muted)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '11px',
                        fontWeight: '700',
                      }}
                    >
                      {stage.num}
                    </div>
                    <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: '600' }}>
                      {stage.label}
                    </span>
                    <span style={{ fontSize: '9px', color: 'var(--text-muted)' }}>{stage.count} students</span>
                  </div>
                  {idx < pipelineStages.length - 1 && (
                    <div
                      style={{
                        flex: 1,
                        height: '2px',
                        background: isDark ? 'rgba(255, 255, 255, 0.08)' : '#e2e8f0',
                        margin: '-12px 6px 0 6px',
                      }}
                    />
                  )}
                </React.Fragment>
              ))}
            </div>

            {/* Empty State Banner */}
            <div
              style={{
                padding: '22px 18px',
                borderRadius: 'var(--radius-lg)',
                background: isDark ? 'rgba(255, 255, 255, 0.02)' : '#f8fafc',
                border: '1px dashed var(--border-subtle)',
                textAlign: 'center',
              }}
            >
              <FileText size={24} style={{ color: 'var(--text-muted)', margin: '0 auto 8px auto' }} />
              <div style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '4px' }}>
                You haven't applied to any drive yet
              </div>
              <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '0 0 14px 0', lineHeight: '1.4' }}>
                You are eligible for {eligibleCount} drives. Each application you submit appears here and moves through these stages.
              </p>
              <Link
                to="/student/drives"
                style={{
                  display: 'inline-block',
                  padding: '7px 18px',
                  borderRadius: 'var(--radius-full)',
                  background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                  color: '#ffffff',
                  fontSize: '12px',
                  fontWeight: '700',
                  textDecoration: 'none',
                }}
              >
                See eligible drives
              </Link>
            </div>
          </div>

          {/* Upcoming Interviews Card */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-xl)',
              padding: '20px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                Upcoming Interviews
              </h3>
              <Link to="/student/interviews" style={{ fontSize: '12px', color: '#6366f1', textDecoration: 'none', fontWeight: '600' }}>
                View all
              </Link>
            </div>

            <div
              style={{
                padding: '18px',
                borderRadius: 'var(--radius-lg)',
                background: isDark ? 'rgba(255, 255, 255, 0.02)' : '#f8fafc',
                border: '1px solid var(--border-subtle)',
                textAlign: 'center',
              }}
            >
              <Calendar size={20} style={{ color: '#fbbf24', margin: '0 auto 6px auto' }} />
              <div style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '2px' }}>
                No interviews scheduled
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                Apply for drives to receive interview calls.
              </div>
            </div>
          </div>

          {/* Recent Notifications Card */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-xl)',
              padding: '20px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                Recent Notifications
              </h3>
              <Link to="/student/notifications" style={{ fontSize: '12px', color: '#6366f1', textDecoration: 'none', fontWeight: '600' }}>
                Open Inbox
              </Link>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {recentNotifsList.map((n) => (
                <div
                  key={n.id}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '10px',
                    fontSize: '12px',
                  }}
                >
                  <span
                    style={{
                      width: '8px',
                      height: '8px',
                      borderRadius: '50%',
                      background: n.dotColor,
                      marginTop: '5px',
                      flexShrink: 0,
                    }}
                  />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: '600', color: 'var(--text-primary)', lineHeight: '1.3' }}>
                      {n.text}
                    </div>
                    <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>{n.time}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
