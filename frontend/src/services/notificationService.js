import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const notificationService = {
  async getNotificationsByStudent(studentId) {
    if (isMockMode()) {
      const store = getMockStore();
      return store.notifications.filter(
        (n) => n.studentId === Number(studentId)
      );
    }

    const res = await apiClient.get(`/notifications/student/${studentId}`);
    return res.data;
  },

  async markAsRead(id) {
    if (isMockMode()) {
      const store = getMockStore();
      const notification = store.notifications.find((n) => n.id === Number(id));
      if (notification) {
        notification.isRead = true;
        saveMockStore(store);
      }
      return notification;
    }

    const res = await apiClient.put(`/notifications/${id}/read`);
    return res.data;
  },

  async markAllAsRead(studentId) {
    if (isMockMode()) {
      const store = getMockStore();
      store.notifications.forEach((n) => {
        if (n.studentId === Number(studentId)) {
          n.isRead = true;
        }
      });
      saveMockStore(store);
      return true;
    }

    // In real mode, mark each
    const notifs = await this.getNotificationsByStudent(studentId);
    await Promise.all(
      notifs.filter((n) => !n.isRead).map((n) => this.markAsRead(n.id))
    );
    return true;
  },
};
