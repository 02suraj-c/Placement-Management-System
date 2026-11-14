import apiClient, { isMockMode } from './api';
import { getMockStore, saveMockStore } from './mockData';

// Accurate in-browser text extractor for TXT, MD, DOCX, and PDF resume documents
export async function extractTextFromFile(file) {
  if (!file) return '';
  try {
    const fileName = (file.name || '').toLowerCase();

    // 1. Plain Text or Markdown files
    if (file.type?.includes('text') || fileName.endsWith('.txt') || fileName.endsWith('.md')) {
      return await file.text();
    }

    const buffer = await file.arrayBuffer();
    const bytes = new Uint8Array(buffer);

    // 2. Word Document (.docx) Parser
    // A DOCX is a ZIP package; the true resume text resides in word/document.xml inside <w:t> tags
    if (fileName.endsWith('.docx')) {
      try {
        const docxText = await extractDocxText(bytes);
        if (docxText && docxText.trim().length > 5) {
          return docxText;
        }
      } catch (e) {
        console.warn('DOCX extraction fallback:', e);
      }
    }

    // 3. PDF Document (.pdf) Parser
    if (fileName.endsWith('.pdf')) {
      try {
        const pdfText = await extractPdfText(bytes);
        if (pdfText && pdfText.trim().length > 5) {
          return pdfText;
        }
      } catch (e) {
        console.warn('PDF stream extraction fallback:', e);
      }
    }

    // 4. Fallback: Clean printable words (strictly discarding binary markers and PDF dictionary headers)
    return extractSanitizedWords(bytes);
  } catch (e) {
    console.warn('Resume file text extraction failed:', e);
    return '';
  }
}

/**
 * Extracts plain text from word/document.xml inside a .docx ZIP container
 */
async function extractDocxText(bytes) {
  let offset = 0;
  const len = bytes.length;

  while (offset + 30 <= len) {
    // Check for ZIP Local File Header magic bytes PK\x03\x04
    if (bytes[offset] === 0x50 && bytes[offset + 1] === 0x4b && bytes[offset + 2] === 0x03 && bytes[offset + 3] === 0x04) {
      const method = bytes[offset + 8] | (bytes[offset + 9] << 8);
      const compSize = (bytes[offset + 18]) | (bytes[offset + 19] << 8) | (bytes[offset + 20] << 16) | (bytes[offset + 21] << 24);
      const fnLen = bytes[offset + 26] | (bytes[offset + 27] << 8);
      const extraLen = bytes[offset + 28] | (bytes[offset + 29] << 8);

      const fnBytes = bytes.slice(offset + 30, offset + 30 + fnLen);
      const fn = new TextDecoder().decode(fnBytes);
      const dataStart = offset + 30 + fnLen + extraLen;

      if (fn === 'word/document.xml') {
        const compData = bytes.slice(dataStart, dataStart + compSize);
        let xmlStr = '';

        if (method === 8 && typeof DecompressionStream !== 'undefined') {
          try {
            const ds = new DecompressionStream('deflate-raw');
            const writer = ds.writable.getWriter();
            writer.write(compData);
            writer.close();
            const reader = ds.readable.getReader();
            const chunks = [];
            while (true) {
              const { done, value } = await reader.read();
              if (done) break;
              chunks.push(value);
            }
            const totalBytes = chunks.reduce((acc, c) => acc + c.length, 0);
            const merged = new Uint8Array(totalBytes);
            let p = 0;
            for (const c of chunks) {
              merged.set(c, p);
              p += c.length;
            }
            xmlStr = new TextDecoder().decode(merged);
          } catch (err) {
            xmlStr = new TextDecoder().decode(compData);
          }
        } else {
          xmlStr = new TextDecoder().decode(compData);
        }

        const matches = [];
        const reg = /<w:t[^>]*>(.*?)<\/w:t>/g;
        let m;
        while ((m = reg.exec(xmlStr)) !== null) {
          matches.push(m[1]);
        }
        return matches.join(' ');
      }

      offset = dataStart + Math.max(0, compSize);
    } else {
      offset++;
    }
  }
  return '';
}

/**
 * Extracts plain text from decompressed PDF content streams and text blocks
 */
async function extractPdfText(bytes) {
  // Convert byte buffer to string for structure parsing
  const decoder = new TextDecoder('latin1');
  const binaryString = decoder.decode(bytes);

  const textSnippets = [];
  const streamRegex = /<<([^>]*)>>\s*stream[\r\n]+([\s\S]*?)[\r\n]+endstream/g;
  let m;

  while ((m = streamRegex.exec(binaryString)) !== null) {
    const dict = m[1] || '';
    // Skip binary resources, images, font descriptors, and metadata
    if (dict.includes('/Image') || dict.includes('/Metadata') || dict.includes('/FontDescriptor') || dict.includes('/FontFile')) {
      continue;
    }

    const streamStart = m.index + m[0].indexOf('stream') + 6;
    let actualStart = streamStart;
    while (actualStart < bytes.length && (bytes[actualStart] === 13 || bytes[actualStart] === 10)) {
      actualStart++;
    }

    const endstreamPos = binaryString.indexOf('endstream', actualStart);
    if (endstreamPos === -1) continue;
    let actualEnd = endstreamPos;
    while (actualEnd > actualStart && (bytes[actualEnd - 1] === 13 || bytes[actualEnd - 1] === 10)) {
      actualEnd--;
    }

    const streamBytes = bytes.slice(actualStart, actualEnd);
    let decoded = '';

    if (dict.includes('/FlateDecode') && typeof DecompressionStream !== 'undefined') {
      try {
        const ds = new DecompressionStream('deflate');
        const writer = ds.writable.getWriter();
        writer.write(streamBytes);
        writer.close();
        const reader = ds.readable.getReader();
        const chunks = [];
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          chunks.push(value);
        }
        const totalBytes = chunks.reduce((acc, c) => acc + c.length, 0);
        const merged = new Uint8Array(totalBytes);
        let p = 0;
        for (const c of chunks) {
          merged.set(c, p);
          p += c.length;
        }
        decoded = new TextDecoder('utf-8', { fatal: false }).decode(merged);
      } catch (err) {
        // Fallback: raw deflate
        try {
          const ds2 = new DecompressionStream('deflate-raw');
          const writer2 = ds2.writable.getWriter();
          writer2.write(streamBytes);
          writer2.close();
          const reader2 = ds2.readable.getReader();
          const chunks2 = [];
          while (true) {
            const { done, value } = await reader2.read();
            if (done) break;
            chunks2.push(value);
          }
          const totalBytes2 = chunks2.reduce((acc, c) => acc + c.length, 0);
          const merged2 = new Uint8Array(totalBytes2);
          let p2 = 0;
          for (const c of chunks2) {
            merged2.set(c, p2);
            p2 += c.length;
          }
          decoded = new TextDecoder('utf-8', { fatal: false }).decode(merged2);
        } catch (e2) {}
      }
    } else {
      decoded = new TextDecoder('utf-8', { fatal: false }).decode(streamBytes);
    }

    if (decoded && (decoded.includes('BT') || decoded.includes('Tj') || decoded.includes('TJ'))) {
      // 1. Extract from TJ arrays: [(text) 10 (text2)] TJ
      const tjArrRegex = /\[(.*?)\]\s*TJ/g;
      let tjArrMatch;
      while ((tjArrMatch = tjArrRegex.exec(decoded)) !== null) {
        const inner = tjArrMatch[1];
        const strRegex = /\((.*?)\)/g;
        let strMatch;
        let segment = '';
        while ((strMatch = strRegex.exec(inner)) !== null) {
          segment += (segment ? ' ' : '') + strMatch[1].replace(/\\([()\\])/g, '$1');
        }
        if (segment.trim()) textSnippets.push(segment);
      }

      // 2. Extract from standard Tj operators: (text) Tj
      const tjSingleRegex = /\((.*?)\)\s*(?:Tj|'|")/g;
      let singleMatch;
      while ((singleMatch = tjSingleRegex.exec(decoded)) !== null) {
        const unescaped = singleMatch[1].replace(/\\([()\\])/g, '$1');
        if (unescaped.trim()) textSnippets.push(unescaped);
      }
    }
  }

  return textSnippets.join(' ');
}

/**
 * Fallback: Extracts natural readable words while filtering out PDF syntax, fonts, and tool metadata
 */
function extractSanitizedWords(bytes) {
  const IGNORED_PDF_KEYWORDS = new Set([
    'obj', 'endobj', 'stream', 'endstream', 'xref', 'trailer', 'startxref',
    'flatedecode', 'length', 'type', 'subtype', 'pages', 'catalog', 'font',
    'fontdescriptor', 'fontfile', 'producer', 'creator', 'creationdate', 'moddate',
    'mediabox', 'contents', 'resources', 'procset', 'xobject', 'metadata'
  ]);

  let current = '';
  const words = [];

  for (let i = 0; i < bytes.length; i++) {
    const c = bytes[i];
    if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || (c >= 48 && c <= 57) || c === 43 || c === 35 || c === 46) {
      current += String.fromCharCode(c);
    } else {
      if (current.length >= 2) {
        const lower = current.toLowerCase();
        if (!IGNORED_PDF_KEYWORDS.has(lower) && !lower.startsWith('/')) {
          words.push(current);
        }
      }
      current = '';
    }
  }
  if (current.length >= 2 && !IGNORED_PDF_KEYWORDS.has(current.toLowerCase())) {
    words.push(current);
  }

  return words.join(' ');
}

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
      (u) =>
        (storedUser.email && u.email?.toLowerCase() === storedUser.email?.toLowerCase()) ||
        (storedUser.id && u.id === storedUser.id)
    );

    if (student?.resumeMetadata) {
      return student.resumeMetadata;
    }

    // Return null if no resume uploaded (never show fake demo resume)
    return null;
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

    const store = getMockStore();
    const storedUser = JSON.parse(localStorage.getItem('auth_user') || '{}');
    const idx = store.users.findIndex(
      (u) =>
        (storedUser.email && u.email?.toLowerCase() === storedUser.email?.toLowerCase()) ||
        (storedUser.id && u.id === storedUser.id)
    );

    const studentName = (idx !== -1 ? store.users[idx].name : storedUser.name) || 'Student';
    const rollNumber = (idx !== -1 ? store.users[idx].rollNumber : storedUser.rollNumber) || 'CS-NEW';
    const studentId = (idx !== -1 ? store.users[idx].id : storedUser.id) || Date.now();

    const extractedText = await extractTextFromFile(file);

    const mockResume = {
      id: Date.now(),
      studentId,
      studentName,
      rollNumber,
      originalFileName: file.name,
      contentType: file.type || 'application/pdf',
      fileSize: file.size,
      formattedSize,
      active: true,
      uploadedAt: new Date().toISOString(),
      downloadUrl: '/api/resumes/me/download',
      extractedText,
    };

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
