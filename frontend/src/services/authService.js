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

  async register(registrationData, legacyEmail, legacyPassword, legacyRole) {
    let payload = {};
    if (typeof registrationData === 'object' && registrationData !== null) {
      payload = { ...registrationData };
    } else {
      payload = {
        name: registrationData,
        email: legacyEmail,
        password: legacyPassword,
        role: legacyRole || 'ROLE_STUDENT',
      };
    }

    const email = payload.email?.trim() || '';
    const name = payload.name?.trim() || '';
    const password = payload.password || '';
    const role = payload.role || 'ROLE_STUDENT';

    if (isMockMode()) {
      const store = getMockStore();
      const existing = store.users.find(
        (u) => u.email.toLowerCase() === email.toLowerCase()
      );
      if (existing) {
        throw new Error('An account with this email already exists.');
      }

      const rollNumber =
        payload.rollNumber?.trim() ||
        `ST${Math.floor(1000 + Math.random() * 9000)}`;

      const newUser = {
        id: Date.now(),
        name,
        email,
        password,
        role,
        department: payload.department || 'Computer Science & Engineering',
        course: payload.course || 'B.Tech',
        studentYear: payload.studentYear || '4th Year',
        currentSemester: payload.currentSemester ? Number(payload.currentSemester) : 7,
        cgpa: payload.cgpa !== undefined && payload.cgpa !== '' ? Number(payload.cgpa) : 8.5,
        backlogs: payload.activeBacklogs !== undefined && payload.activeBacklogs !== '' ? Number(payload.activeBacklogs) : 0,
        activeBacklogs: payload.activeBacklogs !== undefined && payload.activeBacklogs !== '' ? Number(payload.activeBacklogs) : 0,
        rollNumber,
        enrollmentNumber: payload.enrollmentNumber || `ENR-${rollNumber}`,
        batch: payload.graduationYear ? String(payload.graduationYear) : '2026',
        graduationYear: payload.graduationYear ? Number(payload.graduationYear) : 2026,
        phone: payload.phoneNumber || '+91 98765 43210',
        phoneNumber: payload.phoneNumber || '+91 98765 43210',
        address: payload.address || 'JECRC Campus, Jaipur, Rajasthan',
        skills: Array.isArray(payload.skills)
          ? payload.skills
          : payload.skills
          ? payload.skills.split(',').map((s) => s.trim()).filter(Boolean)
          : ['Java', 'SQL', 'Web Development'],
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
        studentYear: newUser.studentYear,
        currentSemester: newUser.currentSemester,
        cgpa: newUser.cgpa,
        backlogs: newUser.backlogs,
        rollNumber: newUser.rollNumber,
        enrollmentNumber: newUser.enrollmentNumber,
        graduationYear: newUser.graduationYear,
        phoneNumber: newUser.phoneNumber,
        address: newUser.address,
        skills: newUser.skills,
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
      body: JSON.stringify(payload),
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || 'Registration failed');
    }

    if (data.data?.token) {
      localStorage.setItem('auth_token', data.data.token);
      localStorage.setItem('auth_user', JSON.stringify(data.data));
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
