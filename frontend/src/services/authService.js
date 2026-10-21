const API_BASE_URL = 'http://localhost:8080/api';

export const authService = {
  async login(email, password) {
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

  async register(name, email, password, role) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name, email, password, role }),
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
    const token = this.getToken();
    if (!token) return null;

    const response = await fetch(`${API_BASE_URL}/auth/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      this.logout();
      return null;
    }

    const data = await response.json();
    return data.data;
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
    return user?.role === role;
  },
};
