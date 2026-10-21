import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const interviewService = {
  async scheduleInterview(scheduleData) {
    if (isMockMode()) {
      const store = getMockStore();
      const app = store.applications.find(
        (a) => a.id === Number(scheduleData.applicationId)
      );

      const newRound = {
        id: Date.now(),
        applicationId: Number(scheduleData.applicationId),
        studentId: app?.studentId || 1,
        studentName: app?.studentName || 'Student',
        company: app?.company || 'Company',
        role: app?.role || 'Engineer',
        roundName: scheduleData.roundName || 'Technical Round 1',
        interviewDateTime: scheduleData.interviewDateTime || new Date().toISOString(),
        mode: scheduleData.mode || 'Virtual',
        location: scheduleData.location || 'Google Meet',
        status: 'SCHEDULED',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      store.interviews.unshift(newRound);

      // Automatically advance application status to INTERVIEW_SCHEDULED
      if (app) {
        app.status = 'INTERVIEW_SCHEDULED';
        app.updatedAt = new Date().toISOString();
      }

      // Add student notification
      store.notifications.unshift({
        id: Date.now() + 3,
        studentId: newRound.studentId,
        message: `New Interview Call: ${newRound.company} scheduled "${newRound.roundName}" on ${new Date(newRound.interviewDateTime).toLocaleDateString()} at ${new Date(newRound.interviewDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} (${newRound.mode}).`,
        isRead: false,
        createdAt: new Date().toISOString(),
      });

      saveMockStore(store);
      return newRound;
    }

    const res = await apiClient.post('/interviews/schedule', scheduleData);
    return res.data;
  },

  async getInterviewsByApplication(applicationId) {
    if (isMockMode()) {
      const store = getMockStore();
      return store.interviews.filter(
        (i) => i.applicationId === Number(applicationId)
      );
    }

    const res = await apiClient.get(`/interviews/application/${applicationId}`);
    return res.data;
  },

  async getInterviewsByStudent(studentId) {
    if (isMockMode()) {
      const store = getMockStore();
      return store.interviews.filter((i) => i.studentId === Number(studentId));
    }
    // In real mode, query student applications first then fetch rounds
    const res = await apiClient.get(`/applications/student/${studentId}`);
    const apps = res.data || [];
    const interviewPromises = apps.map((app) =>
      apiClient.get(`/interviews/application/${app.id}`).then((r) => r.data).catch(() => [])
    );
    const results = await Promise.all(interviewPromises);
    return results.flat();
  },

  async getAllInterviews() {
    if (isMockMode()) {
      const store = getMockStore();
      return [...store.interviews];
    }
    const res = await apiClient.get('/interviews');
    return res.data;
  },

  async recordResult(roundId, resultData) {
    if (isMockMode()) {
      const store = getMockStore();
      const round = store.interviews.find((i) => i.id === Number(roundId));
      if (!round) throw new Error('Interview round not found');

      round.status = resultData.status || 'COMPLETED';
      round.score = resultData.score;
      round.feedback = resultData.feedback;
      round.updatedAt = new Date().toISOString();

      saveMockStore(store);
      return round;
    }

    const res = await apiClient.post(`/interviews/${roundId}/result`, resultData);
    return res.data;
  },
};
