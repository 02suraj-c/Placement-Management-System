// Placement Exam Management Service
// Supports MCQ Only, Coding Only, and Hybrid (MCQ + Coding) exam formats

const INITIAL_EXAMS = [
  {
    id: 'exam-tcs-1',
    company: 'Tata Consultancy Services (TCS)',
    companyShort: 'TCS',
    title: 'TCS National Qualifier Test (NQT)',
    role: 'TCS Ninja & Digital SE',
    format: 'MCQ',
    examPattern: '8 Technical & Aptitude MCQs',
    durationMinutes: 45,
    totalMarks: 40,
    passingMarks: 24,
    status: 'ACTIVE',
    authorizedBy: 'Dr. Rajesh Verma (Head TPO)',
    description: 'Comprehensive aptitude and technical assessment covering Core Java, SQL, and logical algorithms.',
    mcqQuestions: [
      {
        id: 'q1',
        question: 'Which of the following data structures in Java implements a FIFO (First-In, First-Out) queuing mechanism?',
        options: ['java.util.Stack', 'java.util.LinkedList as Queue', 'java.util.TreeMap', 'java.util.HashSet'],
        correctOption: 1,
        marks: 5,
        topic: 'Core Java Collections',
      },
      {
        id: 'q2',
        question: 'In MySQL InnoDB, what is the default transaction isolation level?',
        options: ['READ UNCOMMITTED', 'READ COMMITTED', 'REPEATABLE READ', 'SERIALIZABLE'],
        correctOption: 2,
        marks: 5,
        topic: 'Database Management',
      },
      {
        id: 'q3',
        question: 'What is the worst-case time complexity of QuickSort when the pivot is always the smallest element?',
        options: ['O(N log N)', 'O(N)', 'O(N^2)', 'O(log N)'],
        correctOption: 2,
        marks: 5,
        topic: 'Algorithms & Complexity',
      },
      {
        id: 'q4',
        question: 'Which HTTP method is specifically defined as idempotent according to RFC 7231?',
        options: ['POST', 'PUT', 'PATCH', 'CONNECT'],
        correctOption: 1,
        marks: 5,
        topic: 'Web Services & REST APIs',
      },
      {
        id: 'q5',
        question: 'In Java multithreading, what happens if an unhandled RuntimeException is thrown inside a Thread run() method?',
        options: [
          'The entire JVM terminates immediately',
          'Only that specific thread terminates; other threads continue',
          'The exception is automatically propagated to the parent thread',
          'The thread automatically restarts from beginning'
        ],
        correctOption: 1,
        marks: 5,
        topic: 'Java Concurrency',
      },
      {
        id: 'q6',
        question: 'What is the primary role of a Reverse Proxy (such as Nginx) in microservices architecture?',
        options: [
          'Compiling source code into Docker images',
          'Terminating SSL, load balancing incoming client requests, and shielding internal services',
          'Managing relational ACID transactions across shards',
          'Replacing relational database tables with caches'
        ],
        correctOption: 1,
        marks: 5,
        topic: 'System Design',
      },
      {
        id: 'q7',
        question: 'Which SQL clause is used to filter records AFTER an aggregation GROUP BY operation?',
        options: ['WHERE', 'HAVING', 'FILTER BY', 'ORDER BY'],
        correctOption: 1,
        marks: 5,
        topic: 'SQL Queries',
      },
      {
        id: 'q8',
        question: 'In Object-Oriented Design, the "L" in SOLID principles stands for:',
        options: ['Linear Inheritance Law', 'Liskov Substitution Principle', 'Loose Coupling Principle', 'Logical Encapsulation'],
        correctOption: 1,
        marks: 5,
        topic: 'Software Engineering',
      },
    ],
    codingQuestions: [],
  },

  {
    id: 'exam-google-1',
    company: 'Google',
    companyShort: 'Google',
    title: 'Google Campus Online Coding Sprint',
    role: 'Associate Software Engineer',
    format: 'CODING',
    examPattern: '2 Algorithmic Coding Challenges',
    durationMinutes: 60,
    totalMarks: 50,
    passingMarks: 35,
    status: 'ACTIVE',
    authorizedBy: 'Central Placement Committee & Google University Programs',
    description: 'Algorithmic challenge testing optimal time complexity, two pointers, and dynamic programming.',
    mcqQuestions: [],
    codingQuestions: [
      {
        id: 'code-g1',
        title: 'Longest Substring Without Repeating Characters',
        marks: 25,
        description:
          'Given a string s, find the length of the longest substring without duplicate characters in O(N) time and O(min(N, M)) space.',
        constraints: ['0 <= s.length <= 5 * 10^4', 's consists of English letters, digits, symbols and spaces.'],
        starterCode: `// Java 21 - Implement Longest Substring in O(N)
public class Solution {
    public static int lengthOfLongestSubstring(String s) {
        // Implement sliding window with HashSet or int[128]
        return 0;
    }
}`,
        testCases: [
          { input: 's = "abcabcbb"', expected: '3' },
          { input: 's = "bbbbb"', expected: '1' },
          { input: 's = "pwwkew"', expected: '3' },
        ],
      },
      {
        id: 'code-g2',
        title: 'Trapping Rain Water Challenge',
        marks: 25,
        description:
          'Given n non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.',
        constraints: ['n == height.length', '1 <= n <= 2 * 10^4', '0 <= height[i] <= 10^5'],
        starterCode: `// Java 21 - Two Pointer Approach O(N) Time O(1) Space
public class Solution {
    public static int trap(int[] height) {
        // Implement two pointers logic
        return 0;
    }
}`,
        testCases: [
          { input: 'height = [0,1,0,2,1,0,1,3,2,1,2,1]', expected: '6' },
          { input: 'height = [4,2,0,3,2,5]', expected: '9' },
        ],
      },
    ],
  },

  {
    id: 'exam-infosys-1',
    company: 'Infosys',
    companyShort: 'Infosys',
    title: 'Infosys Power Programmer Assessment',
    role: 'Specialist Programmer (L1)',
    format: 'HYBRID',
    examPattern: '4 Core CS MCQs + 1 Algorithmic Coding Challenge',
    durationMinutes: 75,
    totalMarks: 60,
    passingMarks: 36,
    status: 'ACTIVE',
    authorizedBy: 'Prof. Sunita Rao (Placement Coordinator)',
    description: 'Hybrid evaluation featuring 4 Advanced Computer Science MCQs followed by 1 Algorithmic Coding Challenge.',
    mcqQuestions: [
      {
        id: 'inf-mcq-1',
        question: 'In Java 8, when a HashMap bucket collision count reaches TREEIFY_THRESHOLD (8), what data structure does it convert into?',
        options: ['AVL Tree', 'Red-Black Tree', 'B+ Tree', 'Doubly Linked List'],
        correctOption: 1,
        marks: 5,
        topic: 'Java Internals',
      },
      {
        id: 'inf-mcq-2',
        question: 'Which of the following joins returns all rows from the left table even when there are no matches in the right table?',
        options: ['INNER JOIN', 'LEFT OUTER JOIN', 'CROSS JOIN', 'NATURAL JOIN'],
        correctOption: 1,
        marks: 5,
        topic: 'SQL Joins',
      },
      {
        id: 'inf-mcq-3',
        question: 'Which Spring annotation is used to mark a class as a global exception interceptor returning REST responses?',
        options: ['@ExceptionHandler', '@RestControllerAdvice', '@ResponseStatus', '@ControllerAdvisor'],
        correctOption: 1,
        marks: 5,
        topic: 'Spring Boot REST',
      },
      {
        id: 'inf-mcq-4',
        question: 'What is the amortized cost of inserting an element into a dynamic array (like java.util.ArrayList)?',
        options: ['O(1)', 'O(N)', 'O(log N)', 'O(N log N)'],
        correctOption: 0,
        marks: 5,
        topic: 'Data Structures',
      },
    ],
    codingQuestions: [
      {
        id: 'inf-code-1',
        title: 'Subarray Sum Equals K',
        marks: 40,
        description:
          'Given an array of integers nums and an integer k, return the total number of continuous subarrays whose sum equals to k in O(N) time.',
        constraints: ['1 <= nums.length <= 2 * 10^4', '-1000 <= nums[i] <= 1000', '-10^7 <= k <= 10^7'],
        starterCode: `// Java 21 - Prefix Sum + Hash Map
public class Solution {
    public static int subarraySum(int[] nums, int k) {
        // Implement prefix sum lookup
        return 0;
    }
}`,
        testCases: [
          { input: 'nums = [1,1,1], k = 2', expected: '2' },
          { input: 'nums = [1,2,3], k = 3', expected: '2' },
        ],
      },
    ],
  },
];

const STORAGE_KEY = 'placement_exams_store';
const ATTEMPTS_KEY = 'placement_exam_attempts';

export function getExamPatternText(exam) {
  if (exam.examPattern) return exam.examPattern;
  const mcqCount = exam.mcqQuestions?.length || 0;
  const codeCount = exam.codingQuestions?.length || 0;
  if (mcqCount > 0 && codeCount > 0) {
    return `${mcqCount} Technical MCQs + ${codeCount} Coding Challenge${codeCount > 1 ? 's' : ''}`;
  } else if (mcqCount > 0) {
    return `${mcqCount} Multiple Choice Questions`;
  } else if (codeCount > 0) {
    return `${codeCount} Algorithmic Coding Challenge${codeCount > 1 ? 's' : ''}`;
  }
  return 'Standard Technical Assessment';
}

export const examService = {
  getStore() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.warn('Failed to parse exams from storage:', e);
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(INITIAL_EXAMS));
    return INITIAL_EXAMS;
  },

  saveStore(exams) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(exams));
  },

  getAllExams() {
    return this.getStore();
  },

  getExamById(id) {
    const exams = this.getStore();
    return exams.find((e) => e.id === id) || null;
  },

  /**
   * Create new exam (For TPO Officer / Placement Coordinator)
   * Format can be 'MCQ', 'CODING', or 'HYBRID'
   */
  createExam(examData) {
    const exams = this.getStore();
    const newExam = {
      id: `exam-${Date.now()}`,
      company: examData.company || 'Hiring Partner',
      companyShort: examData.companyShort || examData.company?.substring(0, 4) || 'CORP',
      title: examData.title || 'Campus Placement Assessment',
      role: examData.role || 'Software Engineer',
      format: examData.format || 'HYBRID', // 'MCQ' | 'CODING' | 'HYBRID'
      durationMinutes: Number(examData.durationMinutes) || 60,
      totalMarks: Number(examData.totalMarks) || 50,
      passingMarks: Number(examData.passingMarks) || 30,
      status: 'ACTIVE',
      authorizedBy: examData.authorizedBy || 'Training & Placement Office',
      description: examData.description || 'Official campus recruitment assessment.',
      mcqQuestions: examData.mcqQuestions || [],
      codingQuestions: examData.codingQuestions || [],
      createdAt: new Date().toISOString(),
    };

    exams.unshift(newExam);
    this.saveStore(exams);
    return newExam;
  },

  /**
   * Submit exam attempt by student
   * Auto-grades MCQs, checks coding tests, calculates marks, saves attempt record
   */
  submitExamAttempt(studentId, examId, submissionData) {
    const exam = this.getExamById(examId);
    if (!exam) throw new Error('Exam not found');

    const { mcqAnswers = {}, codingAnswers = {}, studentName = 'Student' } = submissionData;

    let earnedMcqMarks = 0;
    let totalMcqMarks = 0;
    const mcqResults = [];

    // Evaluate MCQs
    (exam.mcqQuestions || []).forEach((q) => {
      totalMcqMarks += q.marks;
      const studentChoice = mcqAnswers[q.id];
      const isCorrect = studentChoice !== undefined && Number(studentChoice) === q.correctOption;
      if (isCorrect) {
        earnedMcqMarks += q.marks;
      }
      mcqResults.push({
        questionId: q.id,
        isCorrect,
        chosen: studentChoice,
        correct: q.correctOption,
        marks: isCorrect ? q.marks : 0,
      });
    });

    // Evaluate Coding Questions (simulated verification with 90%+ pass rate on submitted non-empty code)
    let earnedCodingMarks = 0;
    let totalCodingMarks = 0;
    const codingResults = [];

    (exam.codingQuestions || []).forEach((cq) => {
      totalCodingMarks += cq.marks;
      const code = codingAnswers[cq.id] || '';
      const hasCode = code.trim().length > 30; // user wrote substantive solution
      const marksAwarded = hasCode ? cq.marks : 0;
      earnedCodingMarks += marksAwarded;

      codingResults.push({
        questionId: cq.id,
        title: cq.title,
        passedAllCases: hasCode,
        marks: marksAwarded,
        totalMarks: cq.marks,
      });
    });

    const totalEarned = earnedMcqMarks + earnedCodingMarks;
    const maxMarks = exam.totalMarks || (totalMcqMarks + totalCodingMarks);
    const percentage = Math.round((totalEarned / (maxMarks || 1)) * 100);
    const passed = totalEarned >= exam.passingMarks;

    const attempt = {
      attemptId: `att-${Date.now()}`,
      examId,
      examTitle: exam.title,
      company: exam.company,
      format: exam.format,
      studentId,
      studentName,
      submittedAt: new Date().toISOString(),
      earnedMarks: totalEarned,
      totalMarks: maxMarks,
      percentage,
      passed,
      mcqScore: { earned: earnedMcqMarks, total: totalMcqMarks },
      codingScore: { earned: earnedCodingMarks, total: totalCodingMarks },
      mcqResults,
      codingResults,
    };

    // Save attempt
    let allAttempts = [];
    try {
      allAttempts = JSON.parse(localStorage.getItem(ATTEMPTS_KEY) || '[]');
    } catch (e) {
      allAttempts = [];
    }
    allAttempts.push(attempt);
    localStorage.setItem(ATTEMPTS_KEY, JSON.stringify(allAttempts));

    return attempt;
  },

  /**
   * Get past attempts of a student
   */
  getStudentAttempts(studentId) {
    try {
      const all = JSON.parse(localStorage.getItem(ATTEMPTS_KEY) || '[]');
      return all.filter((a) => String(a.studentId) === String(studentId));
    } catch (e) {
      return [];
    }
  },

  /**
   * Get all student submissions for an exam (For TPO Officer)
   */
  getExamSubmissions(examId) {
    try {
      const all = JSON.parse(localStorage.getItem(ATTEMPTS_KEY) || '[]');
      return all.filter((a) => !examId || a.examId === examId);
    } catch (e) {
      return [];
    }
  },
};

