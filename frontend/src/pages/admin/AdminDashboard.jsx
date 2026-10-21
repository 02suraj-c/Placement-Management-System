import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import {
  Shield,
  Users,
  Building,
  CheckCircle,
  Trash2,
  UserCog,
  Activity,
  AlertCircle,
} from 'lucide-react';

export default function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    try {
      const [usersData, statsData] = await Promise.all([
        adminService.getAllUsers(),
        adminService.getSystemStats(),
      ]);
      setUsers(usersData);
      setStats(statsData);
    } catch (err) {
      console.error('Failed to load admin data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    try {
      await adminService.updateUserRole(userId, newRole);
      setUsers((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u))
      );
      setMessage(`User role updated to ${newRole}`);
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error('Failed to update user role', err);
    }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;
    try {
      await adminService.deleteUser(userId);
      setUsers((prev) => prev.filter((u) => u.id !== userId));
      setMessage('User removed from system.');
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error('Failed to delete user', err);
    }
  };

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading admin dashboard...</div>;
  }

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          System Administration & RBAC
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Manage user permissions, monitor system activity, and assign security roles across the campus network.
        </p>
      </div>

      {message && (
        <div
          style={{
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            background: 'rgba(16, 185, 129, 0.15)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            color: '#34d399',
            marginBottom: '20px',
            fontSize: '14px',
          }}
        >
          {message}
        </div>
      )}

      {/* Stats Cards */}
      <div className="stat-grid">
        <div className="stat-card">
          <div>
            <div className="stat-label">Total Accounts</div>
            <div className="stat-value">{users.length}</div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
              All campus roles
            </div>
          </div>
          <div className="stat-icon">
            <Users size={24} />
          </div>
        </div>

        <div className="stat-card stat-success">
          <div>
            <div className="stat-label">Platform Placement Rate</div>
            <div className="stat-value">{stats?.placementRate || '78.5%'}</div>
            <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '4px' }}>
              Batch of 2026
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--success-bg)', color: 'var(--success)' }}>
            <Activity size={24} />
          </div>
        </div>

        <div className="stat-card stat-info">
          <div>
            <div className="stat-label">Security Protocol</div>
            <div className="stat-value" style={{ fontSize: '20px' }}>JWT + RBAC</div>
            <div style={{ fontSize: '12px', color: 'var(--info)', marginTop: '4px' }}>
              Spring Boot 3.3.4
            </div>
          </div>
          <div className="stat-icon" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>
            <Shield size={24} />
          </div>
        </div>
      </div>

      {/* User Management Table */}
      <div className="card">
        <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <UserCog size={18} color="#3b82f6" />
          <span>User Directory & Role Assignment</span>
        </h3>

        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>User Details</th>
                <th>Email</th>
                <th>Current Role</th>
                <th>Reassign Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>
                    <div style={{ fontWeight: '600', color: '#fff' }}>{u.name}</div>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{u.department || 'General'}</div>
                  </td>
                  <td>{u.email}</td>
                  <td>
                    <span className="badge badge-shortlisted">
                      {u.role.replace('ROLE_', '')}
                    </span>
                  </td>
                  <td>
                    <select
                      value={u.role}
                      onChange={(e) => handleRoleChange(u.id, e.target.value)}
                      className="form-select"
                      style={{ fontSize: '12px', padding: '6px 10px', width: 'auto' }}
                    >
                      <option value="ROLE_STUDENT">STUDENT</option>
                      <option value="ROLE_PLACEMENT_OFFICER">PLACEMENT OFFICER</option>
                      <option value="ROLE_FACULTY">FACULTY</option>
                      <option value="ROLE_HOD">HOD</option>
                      <option value="ROLE_ADMIN">ADMIN</option>
                    </select>
                  </td>
                  <td>
                    <button
                      onClick={() => handleDeleteUser(u.id)}
                      className="btn btn-danger btn-sm"
                      style={{ padding: '6px 10px' }}
                      title="Delete User"
                    >
                      <Trash2 size={14} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
