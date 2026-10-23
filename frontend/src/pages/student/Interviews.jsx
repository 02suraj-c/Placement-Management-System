import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { interviewService } from '../../services/interviewService';
import { questionService } from '../../services/questionService';
import {
  Calendar,
  Clock,
  Video,
  MapPin,
  ExternalLink,
  BookOpen,
  CheckCircle,
  AlertCircle,
  HelpCircle,
  PlusCircle,
  X,
  AlertTriangle,
  Sparkles,
} from 'lucide-react';

export default function Interviews() {
  const { user } = useAuth();
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal State for Adding Company Questions
  const [showQuestionModal, setShowQuestionModal] = useState(false);
  const [selectedInterview, setSelectedInterview] = useState(null);
  const [submittingQuestion, setSubmittingQuestion] = useState(false);
  const [toastMessage, setToastMessage] = useState(null);

  const [questionData, setQuestionData] = useState({
    company: '',
    targetRole: '',
    topic: 'Technical Interview',
    difficulty: 'MEDIUM',
    questionType: 'TECHNICAL',
    questionText: '',
    answer: '',
    yearAsked: new Date().getFullYear(),
  });

  const studentId = user?.id || 1;

  useEffect(() => {
    async function loadInterviews() {
      try {
        const data = await interviewService.getInterviewsByStudent(studentId);
        setInterviews(data);
      } catch (err) {
        console.error('Failed to load interviews', err);
      } finally {
        setLoading(false);
      }
    }
    loadInterviews();
  }, [user]);

  const handleOpenAddQuestion = (item) => {
    setSelectedInterview(item);
    setQuestionData({
      company: item.company || '',
      targetRole: item.role || '',
      topic: item.roundName?.includes('HR') ? 'HR & Behavioral' : 'Data Structures & Algorithms',
      difficulty: 'MEDIUM',
      questionType: item.roundName?.includes('HR') ? 'HR' : 'TECHNICAL',
      questionText: '',
      answer: '',
      yearAsked: new Date(item.interviewDateTime || Date.now()).getFullYear() || new Date().getFullYear(),
    });
    setShowQuestionModal(true);
  };

  const handleQuestionSubmit = async (e) => {
    e.preventDefault();
    if (!questionData.questionText.trim()) return;

    try {
      setSubmittingQuestion(true);
      await questionService.createQuestion({
        ...questionData,
        source: 'STUDENT_CONTRIBUTED',
      });
      setShowQuestionModal(false);
      setToastMessage(`Question for ${questionData.company} added to Question Bank!`);
      setTimeout(() => setToastMessage(null), 4500);
    } catch (err) {
      alert('Failed to save question: ' + err.message);
    } finally {
      setSubmittingQuestion(false);
    }
  };

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading interview schedule...</div>;
  }

  const now = new Date();
  const ONE_DAY_MS = 24 * 60 * 60 * 1000;

  // RULE: Vanish after 1 day of expiration
  // If interviewDateTime + 24 hours < now, the interview has vanished.
  const activeInterviews = interviews.filter((item) => {
    if (!item.interviewDateTime) return true;
    const interviewTime = new Date(item.interviewDateTime).getTime();
    const timeSinceInterview = now.getTime() - interviewTime;

    // Vanish after 1 day of expiration
    if (timeSinceInterview > ONE_DAY_MS) {
      return false;
    }
    return true;
  });

  return (
    <div>
      {/* Toast Alert */}
      {toastMessage && (
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
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
            Interviews Hub
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Review upcoming and scheduled interview rounds, joining links, and contribute questions you were asked.
          </p>
        </div>

        <Link
          to="/student/questions"
          className="btn btn-secondary"
          style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          <BookOpen size={16} color="#60a5fa" />
          <span>Browse Interview Questions Bank</span>
        </Link>
      </div>

      {/* Interview Cards Grid */}
      <div className="grid-3" style={{ marginBottom: '28px' }}>
        {activeInterviews.length === 0 ? (
          <div className="card" style={{ gridColumn: 'span 3', textAlign: 'center', padding: '40px' }}>
            <Calendar size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
            <h3 style={{ fontSize: '16px', fontWeight: '600', color: '#fff', marginBottom: '6px' }}>
              No Active Interviews Scheduled
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', maxWidth: '480px', margin: '0 auto' }}>
              When a recruiter shortlists your profile and schedules a round, joining details appear here. Completed interviews automatically vanish 1 day after expiration.
            </p>
          </div>
        ) : (
          activeInterviews.map((item) => {
            const dateObj = new Date(item.interviewDateTime);
            const isVirtual =
              item.mode?.toLowerCase().includes('virtual') ||
              item.mode?.toLowerCase().includes('meet') ||
              item.location?.startsWith('http');

            // Expiration checks:
            // "if meet link is exceed then student sees the interview link is expired"
            const isExceeded = now > dateObj;

            return (
              <div
                key={item.id}
                className="card"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  border: isExceeded ? '1px solid rgba(239, 68, 68, 0.25)' : '1px solid var(--border-subtle)',
                }}
              >
                {/* Header */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                  <div>
                    <h3 style={{ fontSize: '17px', fontWeight: '700', color: '#fff' }}>
                      {item.company}
                    </h3>
                    <div style={{ fontSize: '13px', color: '#93c5fd' }}>{item.role}</div>
                  </div>

                  {isExceeded ? (
                    <span
                      style={{
                        fontSize: '11px',
                        fontWeight: '700',
                        padding: '3px 8px',
                        borderRadius: 'var(--radius-full, 9999px)',
                        background: 'rgba(239, 68, 68, 0.15)',
                        color: '#f87171',
                        border: '1px solid rgba(239, 68, 68, 0.3)',
                      }}
                    >
                      EXPIRED
                    </span>
                  ) : (
                    <span className="badge badge-interview_scheduled">
                      {item.status || 'SCHEDULED'}
                    </span>
                  )}
                </div>

                {/* Schedule Info Box */}
                <div
                  style={{
                    background: 'var(--bg-main)',
                    padding: '12px 14px',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-subtle)',
                    marginBottom: '14px',
                  }}
                >
                  <div style={{ fontSize: '13px', fontWeight: '600', color: '#f8fafc', marginBottom: '6px' }}>
                    {item.roundName}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                    <Calendar size={13} />
                    <span>{dateObj.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--text-secondary)' }}>
                    <Clock size={13} />
                    <span>{dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} IST</span>
                  </div>
                </div>

                {/* Mode & Venue / Link Info */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                  {isVirtual ? <Video size={15} color="#38bdf8" /> : <MapPin size={15} color="#fbbf24" />}
                  <span>{item.mode}: <strong>{item.location}</strong></span>
                </div>

                {/* Meet Link Status / Join Button */}
                <div style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {isExceeded ? (
                    /* Meet Link Has Exceeded / Expired */
                    <div>
                      <button
                        disabled
                        className="btn btn-secondary btn-sm"
                        style={{
                          width: '100%',
                          cursor: 'not-allowed',
                          opacity: 0.7,
                          color: '#f87171',
                          borderColor: 'rgba(239, 68, 68, 0.35)',
                          background: 'rgba(239, 68, 68, 0.08)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          gap: '6px',
                        }}
                      >
                        <AlertTriangle size={13} color="#f87171" />
                        <span>Interview Link Expired</span>
                      </button>
                      <p style={{ fontSize: '11px', color: '#fca5a5', marginTop: '4px', textAlign: 'center' }}>
                        Scheduled time has exceeded. Will vanish after 1 day of expiration.
                      </p>
                    </div>
                  ) : isVirtual && item.location?.startsWith('http') ? (
                    /* Active Virtual Link */
                    <a
                      href={item.location}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-primary btn-sm"
                      style={{ width: '100%', gap: '6px' }}
                    >
                      <span>Join Meeting Link</span>
                      <ExternalLink size={13} />
                    </a>
                  ) : (
                    /* In-Person Campus Venue */
                    <button
                      disabled
                      className="btn btn-secondary btn-sm"
                      style={{ width: '100%', cursor: 'default' }}
                    >
                      Venue: Campus Placement Block
                    </button>
                  )}

                  {/* Student Can Add the Company Question In Which They Have Appeared */}
                  <button
                    type="button"
                    onClick={() => handleOpenAddQuestion(item)}
                    className="btn btn-secondary btn-sm"
                    style={{
                      width: '100%',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '6px',
                      borderColor: 'rgba(59, 130, 246, 0.3)',
                      color: '#93c5fd',
                    }}
                  >
                    <PlusCircle size={14} />
                    <span>Add Questions Asked in {item.company}</span>
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Quick Preparation Toolkit */}
      <div className="card">
        <h3 style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BookOpen size={18} color="#3b82f6" />
          <span>Interview Preparation Checklist</span>
        </h3>
        <div className="grid-3">
          <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
            <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px', marginBottom: '6px' }}>
              1. Core Computer Science
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Review Data Structures, Algorithms (Trees, Graphs, DP), DBMS normalization, and OOP principles.
            </p>
          </div>
          <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
            <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px', marginBottom: '6px' }}>
              2. System Design & Projects
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Be ready to explain architecture decisions, database schema, and challenges faced in your capstone project.
            </p>
          </div>
          <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
            <div style={{ fontWeight: '600', color: '#fff', fontSize: '14px', marginBottom: '6px' }}>
              3. Behavioral & STAR Method
            </div>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Formulate answers using Situation, Task, Action, and Result for managerial and HR rounds.
            </p>
          </div>
        </div>
      </div>

      {/* Modal: Add Company Question From Appeared Interview */}
      {showQuestionModal && (
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
              maxWidth: '560px',
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
                  Add Questions for {questionData.company}
                </h3>
                <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  Help your peers by adding questions you encountered in this round.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowQuestionModal(false)}
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

            <form onSubmit={handleQuestionSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '14px', marginBottom: '14px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Company
                  </label>
                  <input
                    type="text"
                    required
                    readOnly
                    className="form-input"
                    value={questionData.company}
                    style={{ background: 'var(--bg-main)', opacity: 0.9 }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Target Role
                  </label>
                  <input
                    type="text"
                    className="form-input"
                    value={questionData.targetRole}
                    onChange={(e) => setQuestionData({ ...questionData, targetRole: e.target.value })}
                    placeholder="e.g. SDE 1"
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Topic *
                  </label>
                  <input
                    type="text"
                    required
                    className="form-input"
                    value={questionData.topic}
                    onChange={(e) => setQuestionData({ ...questionData, topic: e.target.value })}
                    placeholder="e.g. Java, DBMS, Trees, HR"
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Difficulty
                  </label>
                  <select
                    className="form-select"
                    value={questionData.difficulty}
                    onChange={(e) => setQuestionData({ ...questionData, difficulty: e.target.value })}
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>
              </div>

              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Question Asked *
                </label>
                <textarea
                  rows="3"
                  required
                  placeholder="Enter the question or problem statement you were asked..."
                  className="form-textarea"
                  value={questionData.questionText}
                  onChange={(e) => setQuestionData({ ...questionData, questionText: e.target.value })}
                />
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  Your Solution / Tips / Discussion Summary
                </label>
                <textarea
                  rows="3"
                  placeholder="Share how you solved it or what the interviewer was looking for..."
                  className="form-textarea"
                  value={questionData.answer}
                  onChange={(e) => setQuestionData({ ...questionData, answer: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowQuestionModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingQuestion}
                  className="btn btn-primary"
                >
                  {submittingQuestion ? 'Saving Question...' : 'Add to Question Bank'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
