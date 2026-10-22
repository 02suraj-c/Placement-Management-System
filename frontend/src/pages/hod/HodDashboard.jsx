import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import {
  GraduationCap,
  Award,
  Building,
  TrendingUp,
} from 'lucide-react';

export default function HodDashboard() {
  const branchData = [
    { company: 'TCS', placed: 42 },
    { company: 'Infosys', placed: 35 },
    { company: 'Wipro', placed: 28 },
    { company: 'HCLTech', placed: 20 },
    { company: 'Amazon', placed: 8 },
  ];

  const ctcDistribution = [
    { range: '< 5 LPA', count: 18, color: '#64748b' },
    { range: '5 - 10 LPA', count: 85, color: '#38bdf8' },
    { range: '10 - 20 LPA', count: 24, color: '#c084fc' },
    { range: '20+ LPA (Dream)', count: 9, color: '#34d399' },
  ];

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '6px' }}>
          JECRC Head of Department (HOD) Analytics
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Comprehensive department-level placement performance, recruiter engagement, and compensation statistics.
        </p>
      </div>

      <div className="stat-grid">
        <div className="stat-card stat-success">
          <div>
            <div className="stat-label">Department Placement Rate</div>
            <div className="stat-value">84.2%</div>
            <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '4px' }}>
              136 of 148 Students Placed
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <Award size={24} />
          </div>
        </div>

        <div className="stat-card stat-info">
          <div>
            <div className="stat-label">Average CTC Package</div>
            <div className="stat-value">₹9.2 LPA</div>
            <div style={{ fontSize: '12px', color: 'var(--info)', marginTop: '4px' }}>
              +14% increase from 2025
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>
            <TrendingUp size={24} />
          </div>
        </div>

        <div className="stat-card stat-purple">
          <div>
            <div className="stat-label">Highest Package Offered</div>
            <div className="stat-value">₹28.5 LPA</div>
            <div style={{ fontSize: '12px', color: '#c084fc', marginTop: '4px' }}>
              Amazon SDE I
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--purple-bg)', color: 'var(--purple)' }}>
            <Building size={24} />
          </div>
        </div>
      </div>

      <div className="grid-2">
        <div className="card">
          <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '18px' }}>
            Top Recruiter Placements (CSE 2026)
          </h3>
          <div style={{ height: '240px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={branchData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="company" stroke="var(--text-muted)" fontSize={12} tickLine={false} />
                <YAxis stroke="var(--text-muted)" fontSize={12} allowDecimals={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'var(--bg-card)',
                    borderColor: 'var(--border-subtle)',
                    borderRadius: '8px',
                    color: 'var(--text-primary)',
                    boxShadow: 'var(--shadow-card)',
                  }}
                />
                <Bar dataKey="placed" fill="var(--primary)" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card">
          <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '18px' }}>
            CTC Package Distribution
          </h3>
          <div style={{ height: '240px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={ctcDistribution} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="range" stroke="var(--text-muted)" fontSize={11} tickLine={false} />
                <YAxis stroke="var(--text-muted)" fontSize={12} allowDecimals={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: 'var(--bg-card)',
                    borderColor: 'var(--border-subtle)',
                    borderRadius: '8px',
                    color: 'var(--text-primary)',
                    boxShadow: 'var(--shadow-card)',
                  }}
                />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {ctcDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
