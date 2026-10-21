import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const applicationService = {
  async apply(studentId, driveId) {
    if (isMockMode()) {
      const store = getMockStore();
      const existing = store.applications.find(
        (a) => a.studentId === Number(studentId) && a.driveId === Number(driveId)
      );
      if (existing) {
        throw new Error('You have already applied for this drive.');
      }

      const drive = store.drives.find((d) => d.id === Number(driveId));
      const student = store.users.find((u) => u.id === Number(studentId));

      const newApp = {
        id: Date.now(),
        studentId: Number(studentId),
        studentName: student?.name || 'Student',
        studentEmail: student?.email || 'student@placement.edu',
        driveId: Number(driveId),
        company: drive?.company || 'Company',
        role: drive?.role || 'Engineer',
        packageLpa: drive?.packageLpa || 6.0,
        status: 'APPLIED',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      store.applications.unshift(newApp);

      // Add a student notification
      store.notifications.unshift({
        id: Date.now() + 1,
        studentId: Number(studentId),
        message: `Application Submitted: Your application for ${newApp.company} (${newApp.role}) was received successfully.`,
        isRead: false,
        createdAt: new Date().toISOString(),
      });

      saveMockStore(store);
      return newApp;
    }

    const res = await apiClient.post('/applications', { studentId, driveId });
    return res.data;
  },

  async getApplicationsByStudent(studentId) {
    if (isMockMode()) {
      const store = getMockStore();
      return store.applications.filter((a) => a.studentId === Number(studentId));
    }

    const res = await apiClient.get(`/applications/student/${studentId}`);
    return res.data;
  },

  async getApplicationsByDrive(driveId) {
    if (isMockMode()) {
      const store = getMockStore();
      return store.applications.filter((a) => a.driveId === Number(driveId));
    }

    const res = await apiClient.get(`/applications/drive/${driveId}`);
    return res.data;
  },

  async getAllApplications() {
    if (isMockMode()) {
      const store = getMockStore();
      return [...store.applications];
    }
    const res = await apiClient.get('/applications');
    return res.data;
  },

  async updateStatus(applicationId, status) {
    if (isMockMode()) {
      const store = getMockStore();
      const app = store.applications.find((a) => a.id === Number(applicationId));
      if (!app) throw new Error('Application not found');

      app.status = status;
      app.updatedAt = new Date().toISOString();

      // Add notification for the student
      store.notifications.unshift({
        id: Date.now() + 2,
        studentId: app.studentId,
        message: `Application Update: Your status for ${app.company} has been updated to ${status.replace('_', ' ')}.`,
        isRead: false,
        createdAt: new Date().toISOString(),
      });

      saveMockStore(store);
      return app;
    }

    const res = await apiClient.put(`/applications/${applicationId}/status`, { status });
    return res.data;
  },
};
