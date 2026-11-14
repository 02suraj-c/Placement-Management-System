import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const questionService = {
  /**
   * Fetch all questions with optional filters: company, topic, difficulty, search
   */
  async getQuestions(filters = {}) {
    if (isMockMode()) {
      const store = getMockStore();
      let list = store.questions || [];

      if (filters.company && filters.company !== 'ALL') {
        list = list.filter((q) =>
          q.company?.toLowerCase().includes(filters.company.toLowerCase())
        );
      }

      if (filters.topic && filters.topic !== 'ALL') {
        list = list.filter((q) =>
          q.topic?.toLowerCase().includes(filters.topic.toLowerCase())
        );
      }

      if (filters.difficulty && filters.difficulty !== 'ALL') {
        list = list.filter((q) => q.difficulty === filters.difficulty);
      }

      if (filters.search) {
        const s = filters.search.toLowerCase();
        list = list.filter(
          (q) =>
            q.questionText?.toLowerCase().includes(s) ||
            q.company?.toLowerCase().includes(s) ||
            q.topic?.toLowerCase().includes(s) ||
            q.targetRole?.toLowerCase().includes(s)
        );
      }

      return list;
    }

    try {
      const params = {};
      if (filters.company && filters.company !== 'ALL') params.company = filters.company;
      if (filters.topic && filters.topic !== 'ALL') params.topic = filters.topic;
      if (filters.difficulty && filters.difficulty !== 'ALL') params.difficulty = filters.difficulty;
      if (filters.search) params.keyword = filters.search;

      const res = await apiClient.get('/questions', { params });
      return res.data?.content || res.data || [];
    } catch (err) {
      console.warn('GET /questions failed, falling back to mock store', err.message);
      const store = getMockStore();
      return store.questions || [];
    }
  },

  /**
   * Create / upload a new interview question (company specific)
   */
  async createQuestion(questionData) {
    const payload = {
      questionText: questionData.questionText,
      answer: questionData.answer || '',
      company: questionData.company?.trim(),
      topic: questionData.topic?.trim() || 'General',
      targetRole: questionData.targetRole?.trim() || 'Software Engineer',
      difficulty: questionData.difficulty || 'MEDIUM',
      questionType: questionData.questionType || 'TECHNICAL',
      tags: questionData.tags || '',
      yearAsked: parseInt(questionData.yearAsked || new Date().getFullYear(), 10),
      source: 'STUDENT_CONTRIBUTED',
    };

    if (isMockMode()) {
      const store = getMockStore();
      const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');

      const newQ = {
        id: Date.now(),
        ...payload,
        createdBy: storedUser.email || 'student@jecrc.edu',
        createdAt: new Date().toISOString(),
      };

      if (!store.questions) store.questions = [];
      store.questions.unshift(newQ);
      saveMockStore(store);
      return newQ;
    }

    try {
      const res = await apiClient.post('/questions', payload);
      return res.data;
    } catch (err) {
      console.warn('POST /questions failed, falling back to mock store', err.message);
      const store = getMockStore();
      const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');

      const newQ = {
        id: Date.now(),
        ...payload,
        createdBy: storedUser.email || 'student@jecrc.edu',
        createdAt: new Date().toISOString(),
      };

      if (!store.questions) store.questions = [];
      store.questions.unshift(newQ);
      saveMockStore(store);
      return newQ;
    }
  },

  /**
   * Get distinct list of companies in the question bank
   */
  async getCompanies() {
    if (isMockMode()) {
      const store = getMockStore();
      const list = store.questions || [];
      const companies = [...new Set(list.map((q) => q.company).filter(Boolean))];
      return companies;
    }

    try {
      const res = await apiClient.get('/questions/meta/companies');
      return res.data || [];
    } catch (err) {
      const store = getMockStore();
      return [...new Set((store.questions || []).map((q) => q.company).filter(Boolean))];
    }
  },
};
