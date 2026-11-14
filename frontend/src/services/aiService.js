import apiClient, { isMockMode } from './api';
import { questionService } from './questionService';

// Fallback company knowledge base for AI simulation if live Gemini key is not configured
const COMPANY_AI_PATTERNS = {
  infosys: [
    {
      questionText: 'Implement a function to find the maximum sub-array sum with at least K elements (Kadane variation).',
      answer: 'Use a modified sliding window + prefix sum algorithm to maintain a running window of size K and add Kadane max sum for the prefix.',
      hint: 'Precompute prefix sums and track min prefix sum seen so far.',
      followUp: 'How do you handle negative values and edge cases where all numbers are negative?',
      relatedSkills: ['Data Structures & Algorithms', 'C++ / Java'],
      estimatedMinutes: 15,
    },
    {
      questionText: 'Explain microservices communication patterns: Synchronous REST/gRPC vs. Asynchronous Kafka event streams in banking architectures.',
      answer: 'Synchronous is suitable for immediate read requests, while asynchronous event streaming with Kafka guarantees eventual consistency, fault isolation, and queue decoupling.',
      hint: 'Compare latency, backpressure, and fault tolerance trade-offs.',
      followUp: 'How do you prevent duplicate message consumption in Kafka consumer groups?',
      relatedSkills: ['Microservices', 'Spring Boot', 'Kafka'],
      estimatedMinutes: 12,
    },
    {
      questionText: 'How does Spring Boot handle transaction management across multiple database writes with @Transactional?',
      answer: 'Spring uses AOP proxies to intercept method execution, opening a transaction via PlatformTransactionManager, committing on successful exit, and triggering rollback on RuntimeException.',
      hint: 'Mention AOP proxies and rollback rules (checked vs. unchecked exceptions).',
      followUp: 'What happens if you invoke a @Transactional method from another method within the same class?',
      relatedSkills: ['Java', 'Spring Boot', 'Hibernate'],
      estimatedMinutes: 10,
    },
  ],
  tcs: [
    {
      questionText: 'How does the Java Garbage Collector differentiate between Minor, Major, and Full GC? Explain the G1 Collector regions.',
      answer: 'Minor GC collects the Young Generation (Eden + Survivor spaces). Major GC targets the Tenured/Old generation. G1 partitions the heap into equal-sized regions and prioritizes regions with the most garbage.',
      hint: 'Discuss generational hypothesis and memory layout in HotSpot JVM.',
      followUp: 'What flags do you tune when you encounter frequent Stop-The-World pauses?',
      relatedSkills: ['Core Java', 'JVM Architecture'],
      estimatedMinutes: 10,
    },
    {
      questionText: 'Write an SQL query using Window Functions (ROW_NUMBER vs DENSE_RANK) to find the top 3 highest revenue-generating projects per department.',
      answer: 'SELECT * FROM (SELECT project_id, dept_id, revenue, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY revenue DESC) as rnk FROM Projects) WHERE rnk <= 3;',
      hint: 'Use PARTITION BY and DENSE_RANK inside a subquery or CTE.',
      followUp: 'Why prefer DENSE_RANK over RANK when handling tie scores?',
      relatedSkills: ['SQL', 'DBMS', 'PostgreSQL / MySQL'],
      estimatedMinutes: 8,
    },
    {
      questionText: 'Describe how you would design an automated CI/CD pipeline using GitHub Actions / Jenkins with automated sonar code quality gates.',
      answer: 'Pipeline triggers on pull request: executes unit test suite, builds Docker image, runs SonarQube analysis enforcing 80% coverage and zero vulnerabilities, and deploys to staging.',
      hint: 'Focus on automated verification and fail-early checkpoints.',
      followUp: 'How do you manage secret credentials in GitHub Actions safely?',
      relatedSkills: ['DevOps', 'CI/CD', 'Docker'],
      estimatedMinutes: 12,
    },
  ],
  amazon: [
    {
      questionText: 'Design a distributed rate limiter for Amazon API Gateway that allows 10,000 requests per second per customer token.',
      answer: 'Use Redis Token Bucket or Sliding Window Log algorithm. Redis Lua script ensures atomic token deduction across multi-region edge caches.',
      hint: 'Discuss Token Bucket vs Sliding Window Counter and distributed concurrency.',
      followUp: 'How do you prevent a single hot customer key from saturating one Redis shard?',
      relatedSkills: ['System Design', 'Redis', 'Distributed Systems'],
      estimatedMinutes: 20,
    },
    {
      questionText: 'Given a stream of incoming order events, return the top K most popular products over a rolling 1-hour window.',
      answer: 'Use a Count-Min Sketch or sliding window hash map paired with a Min-Heap of size K, updated every sliding interval.',
      hint: 'Stream processing with sliding time windows.',
      followUp: 'How would you scale this across multiple worker nodes in AWS Flink?',
      relatedSkills: ['Algorithms', 'Data Structures', 'Data Streaming'],
      estimatedMinutes: 18,
    },
  ],
  default: [
    {
      questionText: 'Explain the ACID properties of relational databases and how Isolation levels affect concurrency (Dirty Read, Non-repeatable Read, Phantom Read).',
      answer: 'Atomicity, Consistency, Isolation, Durability. Isolation levels (Read Uncommitted, Read Committed, Repeatable Read, Serializable) progressively prevent concurrency anomalies using MVCC or shared/exclusive locks.',
      hint: 'Define each anomaly and connect with standard SQL isolation levels.',
      followUp: 'How does MySQL InnoDB implement Repeatable Read without locking rows?',
      relatedSkills: ['DBMS', 'SQL', 'Database Architecture'],
      estimatedMinutes: 12,
    },
    {
      questionText: 'How would you optimize a slow database query in production? Walk through indexing, query execution plans, and caching.',
      answer: 'Analyze slow query logs using EXPLAIN / EXPLAIN ANALYZE to identify full table scans, check composite index order (leftmost prefix rule), eliminate N+1 query patterns, and introduce Redis for read-heavy hotspots.',
      hint: 'Structure into: Measuring (EXPLAIN), Schema/Index Tuning, Caching.',
      followUp: 'What are the trade-offs of adding too many indexes on high-write tables?',
      relatedSkills: ['Performance Tuning', 'MySQL', 'Backend Architecture'],
      estimatedMinutes: 15,
    },
    {
      questionText: 'Explain the principles of RESTful API design. What are idempotent HTTP methods, and why is POST not idempotent while PUT is?',
      answer: 'PUT replaces the resource state completely and subsequent identical calls yield the same state (idempotent). POST creates sub-resources each time, yielding new IDs with each submission (non-idempotent).',
      hint: 'Define idempotence mathematically and apply to HTTP verbs.',
      followUp: 'How do you make an inherently non-idempotent POST request idempotent in payment systems?',
      relatedSkills: ['REST APIs', 'Web Architecture', 'HTTP'],
      estimatedMinutes: 10,
    },
  ],
};

export const aiService = {
  /**
   * Suggest company-tailored interview questions using AI (Gemini / AIController).
   *
   * @param {Object} options
   * @param {string} options.targetCompany e.g. "Infosys"
   * @param {string} options.targetRole e.g. "Specialist Programmer"
   * @param {string[]} options.skills e.g. ["Java", "DSA", "System Design"]
   * @param {string} options.difficulty e.g. "MEDIUM"
   * @param {number} options.count default 3
   */
  async suggestInterviewQuestions({
    targetCompany = '',
    targetRole = 'Software Engineer',
    skills = ['Java', 'Data Structures', 'System Design'],
    difficulty = 'MEDIUM',
    count = 3,
  }) {
    const payload = {
      targetCompany,
      targetRole,
      skills: Array.isArray(skills) && skills.length > 0 ? skills : ['Java', 'Algorithms'],
      difficulty,
      questionType: 'TECHNICAL',
      count,
      saveToBank: false,
    };

    if (!isMockMode()) {
      try {
        const response = await apiClient.post('/ai/interview/generate', payload);
        if (response.data?.questions && response.data.questions.length > 0) {
          return response.data.questions;
        }
      } catch (err) {
        console.warn('Backend AI generation endpoint failed, falling back to AI patterns:', err.message);
      }
    }

    // AI simulation based on company patterns
    const compLower = (targetCompany || '').toLowerCase();
    let questions = COMPANY_AI_PATTERNS[compLower];
    if (!questions) {
      if (compLower.includes('tcs') || compLower.includes('tata')) {
        questions = COMPANY_AI_PATTERNS.tcs;
      } else if (compLower.includes('info') || compLower.includes('infosys')) {
        questions = COMPANY_AI_PATTERNS.infosys;
      } else if (compLower.includes('amazon') || compLower.includes('aws')) {
        questions = COMPANY_AI_PATTERNS.amazon;
      } else {
        questions = COMPANY_AI_PATTERNS.default;
      }
    }

    // Add company-specific flavor dynamically
    return questions.map((q) => ({
      ...q,
      company: targetCompany || 'Hiring Partner',
      targetRole: targetRole || 'Software Engineer',
    }));
  },

  /**
   * Save an AI-generated question directly to the Question Bank
   */
  async saveQuestionToBank(question, company, role) {
    return questionService.createQuestion({
      company: company || question.company,
      targetRole: role || question.targetRole,
      topic: question.relatedSkills?.[0] || 'Technical Interview',
      difficulty: 'MEDIUM',
      questionType: 'TECHNICAL',
      questionText: question.questionText,
      answer: `${question.answer}\n\nHint: ${question.hint || ''}\nFollow-Up: ${question.followUp || ''}`,
      yearAsked: new Date().getFullYear(),
      source: 'STUDENT_CONTRIBUTED',
    });
  },
};
