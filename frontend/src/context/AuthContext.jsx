import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      const storedUser = authService.getUser();
      if (storedUser) {
        setUser(storedUser);
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    const data = await authService.login(email, password);
    setUser(data);
    return data;
  };

  const register = async (nameOrData, email, password, role) => {
    const payload =
      typeof nameOrData === 'object' && nameOrData !== null
        ? nameOrData
        : { name: nameOrData, email, password, role };
    const data = await authService.register(payload);
    setUser(data);
    return data;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const hasRole = (role) => {
    if (role === 'ROLE_PLACEMENT_OFFICER' || role === 'ROLE_OFFICER') {
      return user?.role === 'ROLE_PLACEMENT_OFFICER' || user?.role === 'ROLE_OFFICER';
    }
    return user?.role === role;
  };

  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
    hasRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
