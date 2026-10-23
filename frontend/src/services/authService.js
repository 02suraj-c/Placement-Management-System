import apiClient, { API_BASE_URL, isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const authService = {
  async login(email, password) {
    if (isMockMode()) {
      const store = getMockStore();
      const user = store.users.find(
        (u) => u.email.toLowerCase() === email.toLowerCase()
      );

      // In mock mode, allow predefined users with any password or student123/officer123/admin123/etc.
      if (!user) {
        throw new Error('User not found. Use a demo account or register.');
      }
      if (user.password && password && user.password !== password) {
        throw new Error('Invalid password. Check demo credentials.');
      }

      const authData = {
        token: `mock_jwt_token_${user.id}_${Date.now()}`,
        type: 'Bearer',
        id: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
        department: user.department,
        cgpa: user.cgpa,
        backlogs: user.backlogs,
        rollNumber: user.rollNumber,
      };

      localStorage.setItem('auth_token', authData.token);
      localStorage.setItem('auth_user', JSON.stringify(authData));
      return authData;
    }

    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || 'Login failed');
    }

    if (data.data?.token) {
      localStorage.setItem('auth_token', data.data.token);
      localStorage.setItem('auth_user', JSON.stringify(data.data));
    }
    return data.data;
  },

  async register(name, email, password, role, year) {
    const gradYear = parseInt(year || 2026, 10);

    if (isMockMode()) {
      const store = getMockStore();
      const existing = store.users.find(
        (u) => u.email.toLowerCase() === email.toLowerCase()
      );
      if (existing) {
        throw new Error('An account with this email already exists.');
      }

      const roll = `CS${gradYear}-${Math.floor(100 + Math.random() * 900)}`;
      const newUser = {
        id: Date.now(),
        name,
        email,
        password,
        role: role || 'ROLE_STUDENT',
        department: 'Computer Science & Engineering',
        course: 'B.Tech',
        graduationYear: gradYear,
        passingYear: gradYear,
        batch: String(gradYear),
        cgpa: 8.5,
        backlogs: 0,
        rollNumber: roll,
        enrollmentNumber: `EN${gradYear - 4}-${Math.floor(1000 + Math.random() * 9000)}`,
        skills: ['Java', 'SQL', 'Web Development'],
      };

      store.users.push(newUser);
      saveMockStore(store);

      const authData = {
        token: `mock_jwt_token_${newUser.id}_${Date.now()}`,
        type: 'Bearer',
        id: newUser.id,
        name: newUser.name,
        email: newUser.email,
        role: newUser.role,
        department: newUser.department,
        course: newUser.course,
        graduationYear: newUser.graduationYear,
        passingYear: newUser.passingYear,
        cgpa: newUser.cgpa,
        backlogs: newUser.backlogs,
        rollNumber: newUser.rollNumber,
      };

      localStorage.setItem('auth_token', authData.token);
      localStorage.setItem('auth_user', JSON.stringify(authData));
      return authData;
    }

    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name, email, password, role, graduationYear: gradYear }),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || 'Registration failed');
    }

    if (data.data?.token) {
      const userPayload = { ...data.data, graduationYear: gradYear, passingYear: gradYear };
      localStorage.setItem('auth_token', data.data.token);
      localStorage.setItem('auth_user', JSON.stringify(userPayload));
    }
    return data.data;
  },

  async getCurrentUser() {
    if (isMockMode()) {
      return this.getUser();
    }

    const token = this.getToken();
    if (!token) return null;

    try {
      const response = await apiClient.get('/auth/me');
      return response.data?.data;
    } catch (err) {
      this.logout();
      return null;
    }
  },

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  },

  getToken() {
    return localStorage.getItem('auth_token');
  },

  getUser() {
    const userStr = localStorage.getItem('auth_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  hasRole(role) {
    const user = this.getUser();
    if (!user) return false;
    if (role === 'ROLE_PLACEMENT_OFFICER') {
      return user.role === 'ROLE_PLACEMENT_OFFICER' || user.role === 'ROLE_OFFICER';
    }
    return user.role === role;
  },
};
