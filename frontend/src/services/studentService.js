import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const studentService = {
  /**
   * Fetch authenticated student profile
   * GET /api/students/me
   */
  async getMyProfile() {
    if (!isMockMode()) {
      try {
        const res = await apiClient.get('/students/me');
        return res.data?.data || res.data;
      } catch (err) {
        // If live backend returned 404 (profile not created yet), rethrow so UI can offer create profile
        if (err.response?.status === 404) {
          throw err;
        }
        // Fall back to mock store on network/server failure
        console.warn('API /students/me failed, checking fallback...', err.message);
      }
    }

    const store = getMockStore();
    const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');
    const student = store.users.find(
      (u) => u.email?.toLowerCase() === storedUser.email?.toLowerCase()
    );

    const gradYear = student?.passingYear || student?.graduationYear || storedUser?.graduationYear || storedUser?.batch || 2026;
    const defaultRoll = student?.rollNumber || storedUser?.rollNumber || `CS${gradYear}-${String(storedUser?.id || student?.id || 101).slice(-3)}`;

    return {
      id: storedUser?.id || student?.id || 1,
      name: storedUser?.name || student?.name || 'Student',
      email: storedUser?.email || student?.email || 'student@college.edu',
      rollNumber: defaultRoll,
      enrollmentNumber: student?.enrollmentNumber || storedUser?.enrollmentNumber || `EN${gradYear - 4}-001`,
      department: student?.department || storedUser?.department || 'Computer Science & Engineering',
      course: student?.course || storedUser?.course || 'B.Tech',
      passingYear: gradYear,
      cgpa: student?.cgpa !== undefined ? student.cgpa : (storedUser?.cgpa !== undefined ? storedUser.cgpa : 8.5),
      tenthPercentage: student?.tenthPercentage !== undefined ? student.tenthPercentage : 90.0,
      twelfthPercentage: student?.twelfthPercentage !== undefined ? student.twelfthPercentage : 88.0,
      activeBacklogs: student?.backlogs !== undefined ? student.backlogs : (storedUser?.backlogs || 0),
      totalBacklogs: student?.totalBacklogs !== undefined ? student.totalBacklogs : 0,
      phoneNumber: student?.phone || storedUser?.phone || '+91 98765 43210',
      skills: Array.isArray(student?.skills) ? student.skills.join(', ') : (student?.skills || 'Java, Spring Boot, React.js, MySQL'),
      resumeUrl: student?.resumeUrl || null,
    };
  },

  /**
   * Update authenticated student profile
   * PUT /api/students/me
   */
  async updateMyProfile(updateData) {
    const payload = {
      department: updateData.department,
      course: updateData.course,
      passingYear: parseInt(updateData.passingYear, 10),
      cgpa: parseFloat(updateData.cgpa),
      tenthPercentage: updateData.tenthPercentage ? parseFloat(updateData.tenthPercentage) : null,
      twelfthPercentage: updateData.twelfthPercentage ? parseFloat(updateData.twelfthPercentage) : null,
      activeBacklogs: parseInt(updateData.activeBacklogs || 0, 10),
      totalBacklogs: parseInt(updateData.totalBacklogs || 0, 10),
      phoneNumber: updateData.phoneNumber,
      skills: updateData.skills,
    };

    if (!isMockMode()) {
      try {
        const res = await apiClient.put('/students/me', payload);
        return res.data?.data || res.data;
      } catch (err) {
        console.warn('API /students/me PUT failed, updating local state...', err.message);
        if (err.response?.data?.message) {
          throw new Error(err.response.data.message);
        }
        throw err;
      }
    }

    // In mock mode, update mock store and auth_user
    const store = getMockStore();
    const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');
    const idx = store.users.findIndex(
      (u) => u.email?.toLowerCase() === storedUser.email?.toLowerCase()
    );

    if (idx !== -1) {
      store.users[idx] = {
        ...store.users[idx],
        ...payload,
        backlogs: payload.activeBacklogs,
        phone: payload.phoneNumber,
      };
      saveMockStore(store);

      const updatedUser = {
        ...storedUser,
        department: payload.department,
        cgpa: payload.cgpa,
        backlogs: payload.activeBacklogs,
        graduationYear: payload.passingYear,
      };
      localStorage.setItem('auth_user', JSON.stringify(updatedUser));
    }

    return {
      ...payload,
      id: storedUser.id || 1,
      name: storedUser.name || 'Student',
      email: storedUser.email || 'student@college.edu',
      rollNumber: storedUser.rollNumber || `CS${payload.passingYear || 2026}-${storedUser.id || '101'}`,
      enrollmentNumber: storedUser.enrollmentNumber || `EN${(payload.passingYear || 2026) - 4}-001`,
    };
  },

  /**
   * Create student profile if not exists
   * POST /api/students
   */
  async createProfile(profileData) {
    if (!isMockMode()) {
      const res = await apiClient.post('/students', profileData);
      return res.data?.data || res.data;
    }
    return this.updateMyProfile(profileData);
  },

  /**
   * Get student profile by ID (Staff or owner)
   * GET /api/students/{id}
   */
  async getStudentById(id) {
    if (!isMockMode()) {
      const res = await apiClient.get(`/students/${id}`);
      return res.data?.data || res.data;
    }
    return this.getMyProfile();
  },
};

export default studentService;
