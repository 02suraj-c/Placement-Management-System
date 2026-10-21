import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const driveService = {
  async getAllDrives() {
    if (isMockMode()) {
      const store = getMockStore();
      return [...store.drives];
    }
    // Live backend endpoint
    const res = await apiClient.get('/drives');
    return res.data?.data || res.data;
  },

  async getDriveById(id) {
    if (isMockMode()) {
      const store = getMockStore();
      const drive = store.drives.find((d) => d.id === Number(id));
      if (!drive) throw new Error('Drive not found');
      return drive;
    }
    const res = await apiClient.get(`/drives/${id}`);
    return res.data?.data || res.data;
  },

  async createDrive(driveData) {
    if (isMockMode()) {
      const store = getMockStore();
      const newDrive = {
        id: Date.now(),
        company: driveData.company,
        role: driveData.role,
        packageLpa: parseFloat(driveData.packageLpa) || 6.0,
        minCgpa: parseFloat(driveData.minCgpa) || 6.5,
        maxBacklogs: parseInt(driveData.maxBacklogs, 10) || 0,
        allowedBranches: driveData.allowedBranches || ['CSE', 'IT', 'ECE'],
        location: driveData.location || 'Multiple Locations',
        deadline: driveData.deadline || new Date(Date.now() + 7 * 86400000).toISOString(),
        driveDate: driveData.driveDate || new Date(Date.now() + 14 * 86400000).toISOString().split('T')[0],
        description: driveData.description || 'Campus recruitment drive.',
        status: 'ACTIVE',
        totalPositions: parseInt(driveData.totalPositions, 10) || 20,
      };
      store.drives.unshift(newDrive);

      // Also trigger a notification to all students in mock store
      const students = store.users.filter((u) => u.role === 'ROLE_STUDENT');
      students.forEach((s) => {
        store.notifications.unshift({
          id: Date.now() + Math.floor(Math.random() * 1000),
          studentId: s.id,
          message: `New Drive Announced: ${newDrive.company} (${newDrive.role} - ${newDrive.packageLpa} LPA). Check eligibility and apply!`,
          isRead: false,
          createdAt: new Date().toISOString(),
        });
      });

      saveMockStore(store);
      return newDrive;
    }

    const res = await apiClient.post('/drives', driveData);
    return res.data?.data || res.data;
  },
};
