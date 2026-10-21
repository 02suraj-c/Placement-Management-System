import React, { useState } from 'react';
import {
  BookOpen,
  Search,
  Filter,
  CheckCircle,
  HelpCircle,
  Code,
} from 'lucide-react';

export default function QuestionBank() {
  const [questions, setQuestions] = useState([
    {
      id: 1,
      company: 'Tata Consultancy Services (TCS)',
      topic: 'Java & OOP',
      targetRole: 'Digital Software Engineer',
      difficulty: 'MEDIUM',
      question: 'Explain the internal working of HashMap in Java 8. How are collisions handled using red-black trees?',
      yearAsked: 2025,
    },
    {
      id: 2,
      company: 'Infosys',
      topic: 'Data Structures & Algorithms',
      targetRole: 'Specialist Programmer',
      difficulty: 'HARD',
      question: 'Find the lowest common ancestor (LCA) in a binary tree with duplicate values. Optimize time complexity to O(N).',
      yearAsked: 2026,
    },
    {
      id: 3,
      company: 'Amazon',
      topic: 'System Design & Distributed Systems',
      targetRole: 'SDE I',
      difficulty: 'HARD',
      question: 'Design an idempotent payment processing microservice architecture handling network partitions and retries.',
      yearAsked: 2025,
    },
    {
      id: 4,
      company: 'Wipro',
      topic: 'Database Management Systems',
      targetRole: 'Project Engineer (Turbo)',
      difficulty: 'EASY',
      question: 'Write an SQL query to retrieve the second highest salary without using the LIMIT clause.',
      yearAsked: 2026,
    },
    {
      id: 5,
      company: 'HCLTech',
      topic: 'Operating Systems & Concurrency',
      targetRole: 'Graduate Engineer Trainee',
      difficulty: 'MEDIUM',
      question: 'What is the dining philosophers problem? How can deadlocks be prevented using resource hierarchy?',
      yearAsked: 2025,
    },
  ]);

  const [companyFilter, setCompanyFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  const filteredQuestions = questions.filter((q) => {
    const matchesComp = companyFilter === 'ALL' || q.company.includes(companyFilter);
    const matchesSearch =
      q.question.toLowerCase().includes(search.toLowerCase()) ||
      q.topic.toLowerCase().includes(search.toLowerCase());
    return matchesComp && matchesSearch;
  });

  return (
    <div>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          Interview Question Bank
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Curated repository of past interview questions asked by hiring partners during campus drives.
        </p>
      </div>

      <div
        style={{
          display: 'flex',
          gap: '14px',
          flexWrap: 'wrap',
          marginBottom: '24px',
          background: 'var(--bg-card)',
          padding: '16px',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border-subtle)',
        }}
      >
        <div style={{ flex: '1', minWidth: '240px', position: 'relative' }}>
          <Search
            size={16}
            style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}
          />
          <input
            type="text"
            placeholder="Search questions or topics..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '38px' }}
          />
        </div>

        <div style={{ width: '220px' }}>
          <select
            value={companyFilter}
            onChange={(e) => setCompanyFilter(e.target.value)}
            className="form-select"
          >
            <option value="ALL">All Companies</option>
            <option value="TCS">TCS</option>
            <option value="Infosys">Infosys</option>
            <option value="Amazon">Amazon</option>
            <option value="Wipro">Wipro</option>
            <option value="HCLTech">HCLTech</option>
          </select>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {filteredQuestions.map((q) => (
          <div key={q.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
              <div>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: '700',
                    color: '#60a5fa',
                    textTransform: 'uppercase',
                    letterSpacing: '0.05em',
                  }}
                >
                  {q.company} • {q.targetRole}
                </span>
                <h3 style={{ fontSize: '16px', fontWeight: '600', color: '#fff', marginTop: '4px', lineHeight: 1.5 }}>
                  {q.question}
                </h3>
              </div>

              <span
                style={{
                  fontSize: '11px',
                  fontWeight: '700',
                  padding: '3px 8px',
                  borderRadius: 'var(--radius-full)',
                  background:
                    q.difficulty === 'EASY'
                      ? 'rgba(16, 185, 129, 0.15)'
                      : q.difficulty === 'MEDIUM'
                      ? 'rgba(245, 158, 11, 0.15)'
                      : 'rgba(239, 68, 68, 0.15)',
                  color:
                    q.difficulty === 'EASY'
                      ? '#34d399'
                      : q.difficulty === 'MEDIUM'
                      ? '#fbbf24'
                      : '#f87171',
                }}
              >
                {q.difficulty}
              </span>
            </div>

            <div style={{ display: 'flex', gap: '14px', fontSize: '12px', color: 'var(--text-muted)', borderTop: '1px solid var(--border-subtle)', paddingTop: '10px' }}>
              <span>Topic: <strong style={{ color: '#cbd5e1' }}>{q.topic}</strong></span>
              <span>•</span>
              <span>Asked in {q.yearAsked} Campus Drive</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
