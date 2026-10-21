import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

export const resumeService = {
  /**
   * Fetch active resume metadata for authenticated student
   * GET /api/resumes/me
   */
  async getMyResume() {
    if (!isMockMode()) {
      try {
        const res = await apiClient.get('/resumes/me');
        return res.data?.data || res.data;
      } catch (err) {
        if (err.response?.status === 404) {
          return null; // No resume uploaded yet
        }
        console.warn('API /resumes/me failed, checking fallback...', err.message);
      }
    }

    const store = getMockStore();
    const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');
    const student = store.users.find(
      (u) => u.email?.toLowerCase() === storedUser.email?.toLowerCase() || u.role === 'ROLE_STUDENT'
    );

    if (student?.resumeMetadata) {
      return student.resumeMetadata;
    }

    // Default mock resume if present in mock data
    return {
      id: 1,
      studentId: student?.id || 1,
      studentName: student?.name || 'Aarav Sharma',
      rollNumber: student?.rollNumber || 'CS2026-042',
      originalFileName: 'Aarav_Sharma_Resume_2026.pdf',
      contentType: 'application/pdf',
      fileSize: 1425000,
      formattedSize: '1.36 MB',
      active: true,
      uploadedAt: new Date(Date.now() - 86400000 * 3).toISOString(),
      downloadUrl: '/api/resumes/me/download',
    };
  },

  /**
   * Upload or replace student resume
   * POST /api/resumes/upload
   * @param {File} file
   * @param {Function} onProgress
   */
  async uploadResume(file, onProgress) {
    if (!file) {
      throw new Error('Please select a file to upload');
    }

    const allowedExtensions = ['.pdf', '.docx'];
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    if (!allowedExtensions.includes(ext)) {
      throw new Error('Unsupported file extension. Allowed formats are PDF (.pdf) and Word (.docx).');
    }

    const maxBytes = 5 * 1024 * 1024; // 5 MB
    if (file.size > maxBytes) {
      throw new Error(`File size (${(file.size / (1024 * 1024)).toFixed(2)} MB) exceeds maximum allowed limit of 5 MB`);
    }

    if (!isMockMode()) {
      try {
        const formData = new FormData();
        formData.append('file', file);

        const res = await apiClient.post('/resumes/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          onUploadProgress: (progressEvent) => {
            if (onProgress && progressEvent.total) {
              const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
              onProgress(percentCompleted);
            }
          },
        });

        return res.data?.data || res.data;
      } catch (err) {
        console.warn('API /resumes/upload failed, falling back...', err.message);
        if (err.response?.data?.message) {
          throw new Error(err.response.data.message);
        }
        throw err;
      }
    }

    // Mock mode upload simulation
    if (onProgress) {
      onProgress(50);
      await new Promise((r) => setTimeout(r, 200));
      onProgress(100);
    }

    const formattedSize = file.size < 1024 * 1024
      ? `${(file.size / 1024).toFixed(1)} KB`
      : `${(file.size / (1024 * 1024)).toFixed(2)} MB`;

    const mockResume = {
      id: Date.now(),
      studentId: 1,
      studentName: 'Aarav Sharma',
      rollNumber: 'CS2026-042',
      originalFileName: file.name,
      contentType: file.type || 'application/pdf',
      fileSize: file.size,
      formattedSize,
      active: true,
      uploadedAt: new Date().toISOString(),
      downloadUrl: '/api/resumes/me/download',
    };

    const store = getMockStore();
    const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');
    const idx = store.users.findIndex(
      (u) => u.email?.toLowerCase() === storedUser.email?.toLowerCase() || u.role === 'ROLE_STUDENT'
    );
    if (idx !== -1) {
      store.users[idx].resumeMetadata = mockResume;
      store.users[idx].resumeUrl = `/resumes/${file.name}`;
      saveMockStore(store);
    }

    return mockResume;
  },

  /**
   * Download active resume file as blob and trigger browser download
   * GET /api/resumes/me/download
   * @param {string} fallbackFilename
   */
  async downloadMyResume(fallbackFilename = 'resume.pdf') {
    if (!isMockMode()) {
      try {
        const res = await apiClient.get('/resumes/me/download', {
          responseType: 'blob',
        });

        // Extract filename from Content-Disposition if present
        let filename = fallbackFilename;
        const disposition = res.headers['content-disposition'];
        if (disposition && disposition.includes('filename=')) {
          const match = disposition.match(/filename="?([^"]+)"?/);
          if (match && match[1]) {
            filename = match[1];
          }
        }

        this.triggerBlobDownload(res.data, filename);
        return true;
      } catch (err) {
        console.warn('API /resumes/me/download failed, checking mock fallback...', err.message);
        if (err.response?.status === 404) {
          throw new Error('No active resume available for download');
        }
      }
    }

    // Mock mode download
    const mockContent = `JECRC Placement Portal - Student Resume\nGenerated: ${new Date().toLocaleString()}\nFile: ${fallbackFilename}`;
    const blob = new Blob([mockContent], { type: 'text/plain' });
    this.triggerBlobDownload(blob, fallbackFilename.endsWith('.docx') ? fallbackFilename : `${fallbackFilename.replace(/\.pdf$/, '')}.txt`);
    return true;
  },

  /**
   * Helper to trigger native browser file download
   */
  triggerBlobDownload(blob, filename) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  /**
   * Get resume metadata by student ID (Staff or owner)
   * GET /api/resumes/student/{studentId}
   */
  async getResumeByStudentId(studentId) {
    if (!isMockMode()) {
      const res = await apiClient.get(`/resumes/student/${studentId}`);
      return res.data?.data || res.data;
    }
    return this.getMyResume();
  },
};

export default resumeService;
