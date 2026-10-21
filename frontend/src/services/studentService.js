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
      (u) => u.email?.toLowerCase() === storedUser.email?.toLowerCase() || u.role === 'ROLE_STUDENT'
    );

    return {
      id: student?.id || 1,
      name: student?.name || 'Aarav Sharma',
      email: student?.email || 'student@placement.edu',
      rollNumber: student?.rollNumber || 'CS2026-042',
      enrollmentNumber: student?.enrollmentNumber || 'EN2022-9876',
      department: student?.department || 'Computer Science & Engineering',
      course: student?.course || 'B.Tech',
      passingYear: student?.passingYear || 2026,
      cgpa: student?.cgpa !== undefined ? student.cgpa : 8.85,
      tenthPercentage: student?.tenthPercentage !== undefined ? student.tenthPercentage : 92.5,
      twelfthPercentage: student?.twelfthPercentage !== undefined ? student.twelfthPercentage : 89.0,
      activeBacklogs: student?.backlogs !== undefined ? student.backlogs : 0,
      totalBacklogs: student?.totalBacklogs !== undefined ? student.totalBacklogs : 0,
      phoneNumber: student?.phone || '+91 98765 43210',
      skills: Array.isArray(student?.skills) ? student.skills.join(', ') : (student?.skills || 'Java, Spring Boot, React.js, MySQL, Docker'),
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
      (u) => u.email?.toLowerCase() === storedUser.email?.toLowerCase() || u.role === 'ROLE_STUDENT'
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
      };
      localStorage.setItem('auth_user', JSON.stringify(updatedUser));
    }

    return {
      ...payload,
      id: storedUser.id || 1,
      name: storedUser.name || 'Aarav Sharma',
      email: storedUser.email || 'student@placement.edu',
      rollNumber: storedUser.rollNumber || 'CS2026-042',
      enrollmentNumber: storedUser.enrollmentNumber || 'EN2022-9876',
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
