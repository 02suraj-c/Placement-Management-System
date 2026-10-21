import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const adminService = {
  async getAllUsers(role) {
    if (isMockMode()) {
      const store = getMockStore();
      if (role) {
        return store.users.filter((u) => u.role === role);
      }
      return [...store.users];
    }

    const params = role ? { role } : {};
    const res = await apiClient.get('/admin/users', { params });
    return res.data?.data || res.data;
  },

  async updateUserRole(id, role) {
    if (isMockMode()) {
      const store = getMockStore();
      const user = store.users.find((u) => u.id === Number(id));
      if (!user) throw new Error('User not found');
      user.role = role;
      saveMockStore(store);
      return user;
    }

    const res = await apiClient.put(`/admin/users/${id}/role`, null, {
      params: { role },
    });
    return res.data?.data || res.data;
  },

  async toggleUserStatus(id, enabled) {
    if (isMockMode()) {
      const store = getMockStore();
      const user = store.users.find((u) => u.id === Number(id));
      if (!user) throw new Error('User not found');
      user.enabled = enabled;
      saveMockStore(store);
      return user;
    }

    const res = await apiClient.put(`/admin/users/${id}/status`, null, {
      params: { enabled },
    });
    return res.data?.data || res.data;
  },

  async deleteUser(id) {
    if (isMockMode()) {
      const store = getMockStore();
      store.users = store.users.filter((u) => u.id !== Number(id));
      saveMockStore(store);
      return true;
    }

    const res = await apiClient.delete(`/admin/users/${id}`);
    return res.data?.data || res.data;
  },

  async getSystemStats() {
    if (isMockMode()) {
      const store = getMockStore();
      const totalStudents = store.users.filter((u) => u.role === 'ROLE_STUDENT').length;
      const totalPlaced = store.applications.filter((a) => a.status === 'SELECTED').length;
      return {
        totalUsers: store.users.length,
        totalStudents: totalStudents || 140,
        totalPlaced: totalPlaced || 92,
        activeDrives: store.drives.length,
        placementRate: '78.5%',
        averagePackageLpa: 8.4,
        highestPackageLpa: 28.5,
      };
    }

    const res = await apiClient.get('/admin/stats');
    return res.data?.data || res.data;
  },
};
