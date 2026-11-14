import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { applicationService } from '../../services/applicationService';
import { aiService } from '../../services/aiService';
import {
  FileText,
  Calendar,
  Building,
  CheckCircle2,
  Clock,
  ArrowRight,
  Filter,
  Sparkles,
  BookOpen,
  ChevronDown,
  ChevronUp,
  X,
  CheckCircle,
  HelpCircle,
  BookmarkPlus,
  Loader2,
} from 'lucide-react';

export default function Applications() {
  const { user } = useAuth();
  const [applications, setApplications] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);

  // AI Suggestions Modal State
  const [activeAiApp, setActiveAiApp] = useState(null);
  const [aiQuestions, setAiQuestions] = useState([]);
  const [loadingAi, setLoadingAi] = useState(false);
  const [expandedAiIdx, setExpandedAiIdx] = useState(null);
  const [savedSuccessMap, setSavedSuccessMap] = useState({});
  const [toastMessage, setToastMessage] = useState(null);

  const studentId = user?.id || 1;

  useEffect(() => {
    async function loadApplications() {
      try {
        const apps = await applicationService.getApplicationsByStudent(studentId);
        setApplications(apps);
      } catch (err) {
        console.error('Failed to load applications', err);
      } finally {
        setLoading(false);
      }
    }
    loadApplications();
  }, [user]);

  const handleOpenAiQuestions = async (app) => {
    setActiveAiApp(app);
    setLoadingAi(true);
    setAiQuestions([]);
    setExpandedAiIdx(0);
    setSavedSuccessMap({});

    try {
      const generated = await aiService.suggestInterviewQuestions({
        targetCompany: app.company,
        targetRole: app.role,
        skills: ['Java', 'Data Structures & Algorithms', 'System Design', 'SQL'],
        difficulty: 'MEDIUM',
        count: 3,
      });
      setAiQuestions(generated);
    } catch (err) {
      console.error('Failed to generate AI questions', err);
    } finally {
      setLoadingAi(false);
    }
  };

  const handleSaveToBank = async (q, idx) => {
    try {
      await aiService.saveQuestionToBank(q, activeAiApp.company, activeAiApp.role);
      setSavedSuccessMap((prev) => ({ ...prev, [idx]: true }));
      setToastMessage(`Saved to Question Bank for ${activeAiApp.company}!`);
      setTimeout(() => setToastMessage(null), 3500);
    } catch (err) {
      alert('Failed to save to question bank: ' + err.message);
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'SELECTED': return 'badge-selected';
      case 'SHORTLISTED': return 'badge-shortlisted';
      case 'INTERVIEW_SCHEDULED': return 'badge-interview_scheduled';
      case 'REJECTED': return 'badge-rejected';
      case 'APPLIED':
      default: return 'badge-applied';
    }
  };

  const getStepState = (currentStatus, stepName) => {
    const order = ['APPLIED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'SELECTED'];
    const currentIndex = order.indexOf(currentStatus);
    const stepIndex = order.indexOf(stepName);

    if (currentStatus === 'REJECTED') {
      if (stepName === 'APPLIED') return 'completed';
      return 'rejected';
    }

    if (currentIndex > stepIndex) return 'completed';
    if (currentIndex === stepIndex) return 'current';
    return 'pending';
  };

  const filteredApps = applications.filter((app) => {
    if (filter === 'ALL') return true;
    if (filter === 'ACTIVE') return app.status !== 'REJECTED' && app.status !== 'SELECTED';
    return app.status === filter;
  });

  if (loading) {
    return <div style={{ color: 'var(--text-secondary)', padding: '20px' }}>Loading application status...</div>;
  }

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

      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '24px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
          My Applications
        </h2>
        <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          Track recruitment stages, interview invitations, and access AI-generated interview preparation questions for every applied company.
        </p>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px', flexWrap: 'wrap' }}>
        {[
          { key: 'ALL', label: `All (${applications.length})` },
          { key: 'ACTIVE', label: 'Active Pipeline' },
          { key: 'SHORTLISTED', label: 'Shortlisted' },
          { key: 'INTERVIEW_SCHEDULED', label: 'Interviews' },
          { key: 'SELECTED', label: 'Selected / Offers' },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`btn btn-sm ${filter === tab.key ? 'btn-primary' : 'btn-secondary'}`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Applications List */}
      {filteredApps.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <FileText size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px' }} />
          <h3 style={{ fontSize: '16px', fontWeight: '600', color: '#fff', marginBottom: '6px' }}>
            No applications found
          </h3>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            You haven't submitted any applications under this category yet.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {filteredApps.map((app) => (
            <div key={app.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '16px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: '700', color: '#fff' }}>
                      {app.company}
                    </h3>
                    <span className={`badge ${getStatusBadgeClass(app.status)}`}>
                      {app.status.replace('_', ' ')}
                    </span>
                  </div>
                  <div style={{ fontSize: '14px', color: '#93c5fd', fontWeight: '500' }}>
                    {app.role}
                  </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '16px', fontWeight: '700', color: '#34d399' }}>
                    ₹{app.packageLpa} LPA
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                    Applied: {new Date(app.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </div>

              {/* Status Visualizer Pipeline */}
              <div style={{ background: 'var(--bg-main)', padding: '16px 20px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)', marginBottom: '16px' }}>
                <div style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-muted)', marginBottom: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Recruitment Progression Pipeline
                </div>

                <div className="pipeline-steps">
                  <div className={`pipeline-step ${getStepState(app.status, 'APPLIED')}`}>
                    <div className="pipeline-dot" />
                    <span>Application Submitted</span>
                  </div>
                  <div className="pipeline-line" />

                  <div className={`pipeline-step ${getStepState(app.status, 'SHORTLISTED')}`}>
                    <div className="pipeline-dot" />
                    <span>Resume Shortlisted</span>
                  </div>
                  <div className="pipeline-line" />

                  <div className={`pipeline-step ${getStepState(app.status, 'INTERVIEW_SCHEDULED')}`}>
                    <div className="pipeline-dot" />
                    <span>Interview Scheduled</span>
                  </div>
                  <div className="pipeline-line" />

                  <div className={`pipeline-step ${getStepState(app.status, 'SELECTED')}`}>
                    <div className="pipeline-dot" />
                    <span>{app.status === 'REJECTED' ? 'Not Selected' : 'Final Selection'}</span>
                  </div>
                </div>
              </div>

              {/* AI Interview Preparation & Question Bank Toolbar */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '12px',
                  paddingTop: '12px',
                  borderTop: '1px solid var(--border-subtle)',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  <Sparkles size={15} color="#818cf8" />
                  <span>AI-powered interview practice available for this role</span>
                </div>

                <div style={{ display: 'flex', gap: '10px' }}>
                  <Link
                    to={`/student/questions?company=${encodeURIComponent(app.company)}`}
                    className="btn btn-secondary btn-sm"
                    style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                  >
                    <BookOpen size={14} />
                    <span>Past Questions Bank</span>
                  </Link>

                  <button
                    type="button"
                    onClick={() => handleOpenAiQuestions(app)}
                    className="btn btn-primary btn-sm"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      background: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
                      border: 'none',
                    }}
                  >
                    <Sparkles size={14} />
                    <span>Suggest AI Questions</span>
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal: AI Suggested Interview Questions for Applied Company */}
      {activeAiApp && (
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
              maxWidth: '680px',
              maxHeight: '90vh',
              overflowY: 'auto',
              position: 'relative',
              boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
              border: '1px solid var(--border-subtle)',
            }}
          >
            {/* Modal Header */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                marginBottom: '18px',
                borderBottom: '1px solid var(--border-subtle)',
                paddingBottom: '14px',
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                  <span
                    style={{
                      background: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
                      color: '#fff',
                      fontSize: '11px',
                      fontWeight: '700',
                      padding: '3px 8px',
                      borderRadius: '4px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px',
                    }}
                  >
                    <Sparkles size={12} /> Gemini AI Suggested
                  </span>
                  <span style={{ fontSize: '13px', color: '#93c5fd', fontWeight: '600' }}>
                    {activeAiApp.company}
                  </span>
                </div>
                <h3 style={{ fontSize: '19px', fontWeight: '800', color: '#fff' }}>
                  {activeAiApp.role} • Technical & Coding Interview Prep
                </h3>
                <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                  Personalized questions tailored to {activeAiApp.company}'s recruitment style, patterns, and tech stack.
                </p>
              </div>

              <button
                type="button"
                onClick={() => setActiveAiApp(null)}
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

            {/* Questions Content */}
            {loadingAi ? (
              <div style={{ padding: '48px 20px', textAlign: 'center' }}>
                <Loader2 size={32} style={{ animation: 'spin 1s linear infinite', color: '#818cf8', margin: '0 auto 12px' }} />
                <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginBottom: '4px' }}>
                  Generating AI Interview Questions...
                </div>
                <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                  Analyzing {activeAiApp.company}'s hiring patterns and job description criteria...
                </p>
              </div>
            ) : aiQuestions.length === 0 ? (
              <div style={{ padding: '36px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                No questions generated. Please try again.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                {aiQuestions.map((q, idx) => {
                  const isExpanded = expandedAiIdx === idx;
                  const isSaved = savedSuccessMap[idx];

                  return (
                    <div
                      key={idx}
                      style={{
                        background: 'var(--bg-main)',
                        borderRadius: 'var(--radius-md, 8px)',
                        border: '1px solid var(--border-subtle)',
                        padding: '16px',
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '12px', marginBottom: '8px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                          <span style={{ fontSize: '12px', fontWeight: '700', color: '#818cf8' }}>
                            Question {idx + 1}
                          </span>
                          {q.estimatedMinutes && (
                            <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '3px' }}>
                              <Clock size={11} /> {q.estimatedMinutes} mins
                            </span>
                          )}
                          {q.relatedSkills?.map((s, sIdx) => (
                            <span
                              key={sIdx}
                              style={{
                                fontSize: '11px',
                                background: 'rgba(59, 130, 246, 0.1)',
                                color: '#93c5fd',
                                padding: '1px 6px',
                                borderRadius: '4px',
                              }}
                            >
                              {s}
                            </span>
                          ))}
                        </div>

                        <button
                          type="button"
                          onClick={() => handleSaveToBank(q, idx)}
                          disabled={isSaved}
                          className="btn btn-secondary btn-sm"
                          style={{
                            fontSize: '11px',
                            padding: '4px 8px',
                            color: isSaved ? '#34d399' : 'var(--text-primary)',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                          }}
                        >
                          {isSaved ? <CheckCircle size={12} color="#34d399" /> : <BookmarkPlus size={12} />}
                          <span>{isSaved ? 'Saved to Bank' : 'Save to Bank'}</span>
                        </button>
                      </div>

                      <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', lineHeight: 1.5, marginBottom: '10px' }}>
                        {q.questionText}
                      </div>

                      {q.hint && (
                        <div style={{ fontSize: '12px', color: '#cbd5e1', background: 'rgba(51, 65, 85, 0.4)', padding: '8px 12px', borderRadius: '6px', marginBottom: '10px' }}>
                          <strong style={{ color: '#38bdf8' }}>Approach Hint:</strong> {q.hint}
                        </div>
                      )}

                      {/* Accordion Toggle for Model Answer & Follow-up */}
                      <div>
                        <button
                          type="button"
                          onClick={() => setExpandedAiIdx(isExpanded ? null : idx)}
                          style={{
                            background: 'transparent',
                            border: 'none',
                            color: '#a78bfa',
                            fontSize: '12px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            padding: 0,
                          }}
                        >
                          <span>{isExpanded ? 'Hide Model Answer & Follow-up' : 'Reveal Model Answer & Follow-up'}</span>
                          {isExpanded ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                        </button>

                        {isExpanded && (
                          <div style={{ marginTop: '10px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <div style={{ padding: '10px 12px', background: 'rgba(15, 23, 42, 0.8)', borderRadius: '6px', border: '1px solid rgba(139, 92, 246, 0.25)' }}>
                              <div style={{ fontSize: '12px', fontWeight: '700', color: '#c084fc', marginBottom: '4px' }}>
                                Model Answer & Key Points:
                              </div>
                              <p style={{ fontSize: '13px', color: '#e2e8f0', lineHeight: 1.5, margin: 0 }}>
                                {q.answer}
                              </p>
                            </div>

                            {q.followUp && (
                              <div style={{ padding: '8px 12px', background: 'rgba(30, 41, 59, 0.6)', borderRadius: '6px' }}>
                                <div style={{ fontSize: '12px', fontWeight: '700', color: '#fbbf24', marginBottom: '2px' }}>
                                  Likely Interviewer Follow-Up:
                                </div>
                                <p style={{ fontSize: '12px', color: '#cbd5e1', margin: 0 }}>
                                  {q.followUp}
                                </p>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Modal Footer */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '20px', borderTop: '1px solid var(--border-subtle)', paddingTop: '14px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setActiveAiApp(null)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
