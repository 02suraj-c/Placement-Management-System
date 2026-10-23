import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { questionService } from '../../services/questionService';
import { useAuth } from '../../context/AuthContext';
import {
  BookOpen,
  Search,
  Filter,
  PlusCircle,
  CheckCircle,
  HelpCircle,
  ChevronDown,
  ChevronUp,
  X,
  Building2,
  Calendar,
  Sparkles,
  Tag,
  Award,
} from 'lucide-react';

export default function QuestionBank() {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const initialCompany = searchParams.get('company') || 'ALL';

  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [companyFilter, setCompanyFilter] = useState(initialCompany);
  const [difficultyFilter, setDifficultyFilter] = useState('ALL');
  const [expandedId, setExpandedId] = useState(null);

  // Modal State for Adding Company Questions
  const [showAddModal, setShowAddModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [successToast, setSuccessToast] = useState(null);
  const [newQuestion, setNewQuestion] = useState({
    company: initialCompany !== 'ALL' ? initialCompany : '',
    targetRole: '',
    topic: 'Data Structures & Algorithms',
    difficulty: 'MEDIUM',
    questionType: 'TECHNICAL',
    questionText: '',
    answer: '',
    yearAsked: new Date().getFullYear(),
    tags: '',
  });

  useEffect(() => {
    loadQuestions();
  }, [companyFilter, difficultyFilter]);

  useEffect(() => {
    if (searchParams.get('add') === 'true') {
      setShowAddModal(true);
      if (searchParams.get('company')) {
        setNewQuestion((prev) => ({
          ...prev,
          company: searchParams.get('company'),
          targetRole: searchParams.get('role') || '',
        }));
      }
    }
  }, [searchParams]);

  const loadQuestions = async () => {
    try {
      setLoading(true);
      const data = await questionService.getQuestions({
        company: companyFilter,
        difficulty: difficultyFilter,
      });
      setQuestions(data);
    } catch (err) {
      console.error('Failed to load questions', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddSubmit = async (e) => {
    e.preventDefault();
    if (!newQuestion.questionText.trim()) return;

    try {
      setSubmitting(true);
      const created = await questionService.createQuestion(newQuestion);
      setShowAddModal(false);
      setSuccessToast(`Question for ${newQuestion.company || 'Campus Drive'} submitted successfully!`);
      setTimeout(() => setSuccessToast(null), 4000);
      setNewQuestion({
        company: '',
        targetRole: '',
        topic: 'Data Structures & Algorithms',
        difficulty: 'MEDIUM',
        questionType: 'TECHNICAL',
        questionText: '',
        answer: '',
        yearAsked: new Date().getFullYear(),
        tags: '',
      });
      loadQuestions();
    } catch (err) {
      alert('Failed to submit question: ' + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredQuestions = questions.filter((q) => {
    const text = (q.questionText || q.question || '').toLowerCase();
    const comp = (q.company || '').toLowerCase();
    const top = (q.topic || '').toLowerCase();
    const role = (q.targetRole || '').toLowerCase();
    const s = search.toLowerCase();

    return text.includes(s) || comp.includes(s) || top.includes(s) || role.includes(s);
  });

  const distinctCompanies = [
    'ALL',
    ...new Set(questions.map((q) => q.company).filter(Boolean)),
  ];

  return (
    <div>
      {/* Toast Alert */}
      {successToast && (
        <div
          style={{
            position: 'fixed',
            top: '24px',
            right: '24px',
            zIndex: 9999,
            background: 'var(--success-bg, #064e3b)',
            border: '1px solid #10b981',
            color: '#6ee7b7',
            padding: '12px 20px',
            borderRadius: 'var(--radius-md, 8px)',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
          }}
        >
          <CheckCircle size={18} />
          <span>{successToast}</span>
        </div>
      )}

      {/* Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '16px',
          marginBottom: '24px',
        }}
      >
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
            Interview Question Bank
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Curated repository of actual technical, coding, and HR questions asked by recruiting companies.
          </p>
        </div>

        <button
          className="btn btn-primary"
          onClick={() => setShowAddModal(true)}
          style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          <PlusCircle size={16} />
          <span>Contribute Company Question</span>
        </button>
      </div>

      {/* Filters & Search */}
      <div
        style={{
          display: 'flex',
          gap: '14px',
          flexWrap: 'wrap',
          marginBottom: '24px',
          background: 'var(--bg-card)',
          padding: '16px',
          borderRadius: 'var(--radius-lg, 12px)',
          border: '1px solid var(--border-subtle)',
        }}
      >
        <div style={{ flex: '1', minWidth: '240px', position: 'relative' }}>
          <Search
            size={16}
            style={{
              position: 'absolute',
              left: '12px',
              top: '50%',
              transform: 'translateY(-50%)',
              color: 'var(--text-muted)',
            }}
          />
          <input
            type="text"
            placeholder="Search questions, topics, companies, or roles..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '38px', width: '100%' }}
          />
        </div>

        <div style={{ width: '220px' }}>
          <select
            value={companyFilter}
            onChange={(e) => setCompanyFilter(e.target.value)}
            className="form-select"
          >
            {distinctCompanies.map((c) => (
              <option key={c} value={c}>
                {c === 'ALL' ? 'All Hiring Companies' : c}
              </option>
            ))}
          </select>
        </div>

        <div style={{ width: '170px' }}>
          <select
            value={difficultyFilter}
            onChange={(e) => setDifficultyFilter(e.target.value)}
            className="form-select"
          >
            <option value="ALL">All Difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </div>
      </div>

      {/* Question Cards List */}
      {loading ? (
        <div style={{ padding: '36px', textAlign: 'center', color: 'var(--text-secondary)' }}>
          Loading questions from company interview rounds...
        </div>
      ) : filteredQuestions.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <BookOpen size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
          <h3 style={{ fontSize: '16px', fontWeight: '600', color: '#fff', marginBottom: '6px' }}>
            No Questions Found Matching Criteria
          </h3>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
            Be the first to upload and share questions from your interview experience!
          </p>
          <button
            className="btn btn-primary btn-sm"
            onClick={() => setShowAddModal(true)}
            style={{ margin: '0 auto' }}
          >
            + Add Question for this Company
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {filteredQuestions.map((q) => {
            const isExpanded = expandedId === q.id;
            const qText = q.questionText || q.question;

            return (
              <div key={q.id} className="card" style={{ transition: 'all 0.2s ease' }}>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-start',
                    marginBottom: '10px',
                    gap: '12px',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                      <span
                        style={{
                          fontSize: '11px',
                          fontWeight: '700',
                          color: '#60a5fa',
                          textTransform: 'uppercase',
                          letterSpacing: '0.05em',
                          background: 'rgba(59, 130, 246, 0.1)',
                          padding: '2px 8px',
                          borderRadius: '4px',
                        }}
                      >
                        {q.company || 'General / Multi-Company'}
                      </span>
                      {q.targetRole && (
                        <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                          • {q.targetRole}
                        </span>
                      )}
                      {q.source === 'STUDENT_CONTRIBUTED' && (
                        <span
                          style={{
                            fontSize: '11px',
                            color: '#a78bfa',
                            background: 'rgba(139, 92, 246, 0.15)',
                            padding: '2px 6px',
                            borderRadius: '4px',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '4px',
                          }}
                        >
                          <Sparkles size={11} /> Student Contribution
                        </span>
                      )}
                    </div>

                    <h3
                      style={{
                        fontSize: '16px',
                        fontWeight: '600',
                        color: '#fff',
                        marginTop: '8px',
                        lineHeight: 1.5,
                      }}
                    >
                      {qText}
                    </h3>
                  </div>

                  <span
                    style={{
                      fontSize: '11px',
                      fontWeight: '700',
                      padding: '3px 9px',
                      borderRadius: 'var(--radius-full, 9999px)',
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
                      flexShrink: 0,
                    }}
                  >
                    {q.difficulty}
                  </span>
                </div>

                {/* Model Answer toggle if present */}
                {q.answer && (
                  <div style={{ marginTop: '12px', marginBottom: '8px' }}>
                    <button
                      type="button"
                      onClick={() => setExpandedId(isExpanded ? null : q.id)}
                      style={{
                        background: 'transparent',
                        border: 'none',
                        color: '#38bdf8',
                        fontSize: '13px',
                        fontWeight: '500',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        padding: 0,
                      }}
                    >
                      <span>{isExpanded ? 'Hide Model Answer & Approach' : 'View Model Answer & Approach'}</span>
                      {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </button>

                    {isExpanded && (
                      <div
                        style={{
                          marginTop: '8px',
                          padding: '12px 14px',
                          borderRadius: '8px',
                          background: 'rgba(15, 23, 42, 0.6)',
                          border: '1px solid rgba(56, 189, 248, 0.2)',
                          color: '#e2e8f0',
                          fontSize: '13px',
                          lineHeight: 1.6,
                        }}
                      >
                        <div style={{ fontWeight: '600', color: '#38bdf8', marginBottom: '4px' }}>
                          Key Solution Points / Architecture Notes:
                        </div>
                        {q.answer}
                      </div>
                    )}
                  </div>
                )}

                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    fontSize: '12px',
                    color: 'var(--text-muted)',
                    borderTop: '1px solid var(--border-subtle)',
                    paddingTop: '10px',
                    marginTop: '8px',
                    flexWrap: 'wrap',
                    gap: '8px',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span>
                      Topic: <strong style={{ color: '#cbd5e1' }}>{q.topic}</strong>
                    </span>
                    <span>•</span>
                    <span>Asked in {q.yearAsked || 2026} Drive</span>
                  </div>

                  {q.questionType && (
                    <span
                      style={{
                        background: 'var(--bg-main)',
                        padding: '2px 8px',
                        borderRadius: '4px',
                        border: '1px solid var(--border-subtle)',
                        fontSize: '11px',
                      }}
                    >
                      {q.questionType}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal: Contribute Company-Specific Interview Question */}
      {showAddModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(4px)',
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '20px',
          }}
        >
          <div
            className="card"
            style={{
              width: '100%',
              maxWidth: '600px',
              maxHeight: '90vh',
              overflowY: 'auto',
              position: 'relative',
              boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
              border: '1px solid var(--border-subtle)',
            }}
          >
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '18px',
                borderBottom: '1px solid var(--border-subtle)',
                paddingBottom: '12px',
              }}
            >
              <div>
                <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#fff' }}>
                  Contribute Interview Question
                </h3>
                <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  Share questions you encountered during your campus interview rounds.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'var(--text-muted)',
                  cursor: 'pointer',
                  padding: '4px',
                }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleAddSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '14px', marginBottom: '14px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Company Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Infosys, TCS, Amazon, Wipro"
                    className="form-input"
                    value={newQuestion.company}
                    onChange={(e) => setNewQuestion({ ...newQuestion, company: e.target.value })}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Target Role
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Specialist Programmer, SDE"
                    className="form-input"
                    value={newQuestion.targetRole}
                    onChange={(e) => setNewQuestion({ ...newQuestion, targetRole: e.target.value })}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Topic / Domain *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Java, DSA, DBMS, HR"
                    className="form-input"
                    value={newQuestion.topic}
                    onChange={(e) => setNewQuestion({ ...newQuestion, topic: e.target.value })}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Interview Year
                  </label>
                  <select
                    className="form-select"
                    value={newQuestion.yearAsked}
                    onChange={(e) => setNewQuestion({ ...newQuestion, yearAsked: e.target.value })}
                  >
                    <option value="2026">2026 Drive</option>
                    <option value="2025">2025 Drive</option>
                    <option value="2024">2024 Drive</option>
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Difficulty Level
                  </label>
                  <select
                    className="form-select"
                    value={newQuestion.difficulty}
                    onChange={(e) => setNewQuestion({ ...newQuestion, difficulty: e.target.value })}
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Question Type
                  </label>
                  <select
                    className="form-select"
                    value={newQuestion.questionType}
                    onChange={(e) => setNewQuestion({ ...newQuestion, questionType: e.target.value })}
                  >
                    <option value="TECHNICAL">Technical</option>
                    <option value="CODING">Coding / Algorithm</option>
                    <option value="HR">HR / Behavioral</option>
                    <option value="APTITUDE">Aptitude / Logic</option>
                  </select>
                </div>
              </div>

              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Question Description *
                </label>
                <textarea
                  rows="3"
                  required
                  placeholder="State the interview problem or discussion prompt clearly..."
                  className="form-textarea"
                  value={newQuestion.questionText}
                  onChange={(e) => setNewQuestion({ ...newQuestion, questionText: e.target.value })}
                />
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Model Answer / Tips / Expected Key Points
                </label>
                <textarea
                  rows="3"
                  placeholder="Provide brief hints, code approach, or expected answers to assist fellow students..."
                  className="form-textarea"
                  value={newQuestion.answer}
                  onChange={(e) => setNewQuestion({ ...newQuestion, answer: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowAddModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="btn btn-primary"
                >
                  {submitting ? 'Submitting Question...' : 'Submit to Question Bank'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
