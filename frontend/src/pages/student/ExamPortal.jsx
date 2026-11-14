import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../context/AuthContext';
import { examService, getExamPatternText } from '../../services/examService';
import {
  FileText,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Play,
  RotateCcw,
  Check,
  ChevronRight,
  ChevronLeft,
  Award,
  Building2,
  Layers,
  Sparkles,
  Terminal,
  HelpCircle,
  ShieldCheck,
  ArrowLeft,
  Send,
} from 'lucide-react';

export default function ExamPortal() {
  const { user } = useAuth();
  const studentId = user?.id || 1;
  const studentName = user?.name || 'Student';

  const [exams, setExams] = useState([]);
  const [attempts, setAttempts] = useState([]);
  const [activeTab, setActiveTab] = useState('ALL'); // 'ALL' | 'MCQ' | 'CODING' | 'HYBRID' | 'ATTEMPTED'

  // Exam Taking State
  const [currentExam, setCurrentExam] = useState(null);
  const [examState, setExamState] = useState('list'); // 'list' | 'taking' | 'result'
  const [secondsRemaining, setSecondsRemaining] = useState(0);
  const [isTimerRunning, setIsTimerRunning] = useState(false);
  const timerRef = useRef(null);

  // Active question navigation within an ongoing exam
  const [activeSection, setActiveSection] = useState('mcq'); // 'mcq' | 'coding'
  const [activeQuestionIdx, setActiveQuestionIdx] = useState(0);
  const [mcqAnswers, setMcqAnswers] = useState({});
  const [codingAnswers, setCodingAnswers] = useState({});

  // Submitting / Result State
  const [latestAttempt, setLatestAttempt] = useState(null);
  const [showSubmitModal, setShowSubmitModal] = useState(false);

  // Pre-Exam Instructions Modal State
  const [showInstructionsModal, setShowInstructionsModal] = useState(false);
  const [selectedExamForInstructions, setSelectedExamForInstructions] = useState(null);
  const [agreedInstructions, setAgreedInstructions] = useState(false);

  useEffect(() => {
    loadExamsAndAttempts();
  }, [user]);

  const loadExamsAndAttempts = () => {
    const allExams = examService.getAllExams();
    const studentAttempts = examService.getStudentAttempts(studentId);
    setExams(allExams);
    setAttempts(studentAttempts);
  };

  // Timer Effect during ongoing exam
  useEffect(() => {
    if (examState === 'taking' && isTimerRunning && secondsRemaining > 0) {
      timerRef.current = setInterval(() => {
        setSecondsRemaining((prev) => {
          if (prev <= 1) {
            clearInterval(timerRef.current);
            setIsTimerRunning(false);
            autoSubmitExam();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [examState, isTimerRunning, secondsRemaining]);

  const formatTimer = (totalSec) => {
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  // Start Exam
  const handleStartExam = (exam) => {
    setCurrentExam(exam);
    setSecondsRemaining((exam.durationMinutes || 45) * 60);
    setIsTimerRunning(true);
    setMcqAnswers({});

    // Populate initial starter code for coding questions
    const initialCode = {};
    (exam.codingQuestions || []).forEach((cq) => {
      initialCode[cq.id] = cq.starterCode || '// Write your code solution here';
    });
    setCodingAnswers(initialCode);

    if (exam.format === 'CODING') {
      setActiveSection('coding');
    } else {
      setActiveSection('mcq');
    }
    setActiveQuestionIdx(0);
    setExamState('taking');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSelectMcq = (questionId, optionIdx) => {
    setMcqAnswers((prev) => ({
      ...prev,
      [questionId]: optionIdx,
    }));
  };

  const handleCodingChange = (questionId, code) => {
    setCodingAnswers((prev) => ({
      ...prev,
      [questionId]: code,
    }));
  };

  // Submit Exam
  const handleSubmitConfirm = () => {
    setShowSubmitModal(false);
    clearInterval(timerRef.current);
    setIsTimerRunning(false);

    const attempt = examService.submitExamAttempt(studentId, currentExam.id, {
      mcqAnswers,
      codingAnswers,
      studentName,
    });

    setLatestAttempt(attempt);
    setExamState('result');
    loadExamsAndAttempts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const autoSubmitExam = () => {
    handleSubmitConfirm();
  };

  const handleOpenInstructions = (exam) => {
    setSelectedExamForInstructions(exam);
    setAgreedInstructions(false);
    setShowInstructionsModal(true);
  };

  const handleConfirmBeginExam = () => {
    if (!agreedInstructions || !selectedExamForInstructions) return;
    const examToStart = selectedExamForInstructions;
    setShowInstructionsModal(false);
    setSelectedExamForInstructions(null);
    handleStartExam(examToStart);
  };

  // Attempts map for quick status lookup
  const attemptMap = new Map(attempts.map((a) => [a.examId, a]));

  const filteredExams = exams.filter((e) => {
    if (activeTab === 'MCQ') return e.format === 'MCQ';
    if (activeTab === 'CODING') return e.format === 'CODING';
    if (activeTab === 'COMBINED') return e.format === 'HYBRID' || (e.mcqQuestions?.length > 0 && e.codingQuestions?.length > 0);
    if (activeTab === 'ATTEMPTED') return attemptMap.has(e.id);
    return true;
  });

  const renderPatternBadge = (exam) => {
    const patternText = getExamPatternText(exam);
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '5px',
          fontSize: '11.5px',
          fontWeight: '700',
          padding: '3px 10px',
          borderRadius: '20px',
          background: 'rgba(99, 102, 241, 0.12)',
          color: '#818cf8',
          border: '1px solid rgba(99, 102, 241, 0.25)',
        }}
      >
        <Layers size={11} />
        {patternText}
      </span>
    );
  };

  // ==========================================
  // VIEW: RESULT / SCORECARD VIEW
  // ==========================================
  if (examState === 'result' && latestAttempt) {
    const isPassed = latestAttempt.passed;
    return (
      <div style={styles.container}>
        <div style={styles.resultBanner}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '14px' }}>
            {isPassed ? (
              <CheckCircle2 size={36} color="#10b981" />
            ) : (
              <AlertCircle size={36} color="#f59e0b" />
            )}
            <div>
              <h1 style={{ fontSize: '22px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                {isPassed ? 'Assessment Completed — Qualified!' : 'Assessment Completed — Needs Improvement'}
              </h1>
              <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0 0' }}>
                {latestAttempt.examTitle} • {latestAttempt.company}
              </p>
            </div>
          </div>

          <div style={styles.scoreCardsRow}>
            <div style={styles.scoreBox}>
              <span style={styles.scoreLabel}>Total Score</span>
              <span style={{ ...styles.scoreValue, color: isPassed ? '#10b981' : '#f59e0b' }}>
                {latestAttempt.earnedMarks} / {latestAttempt.totalMarks}
              </span>
              <span style={styles.scoreSub}>{latestAttempt.percentage}% Overall</span>
            </div>

            {latestAttempt.mcqScore.total > 0 && (
              <div style={styles.scoreBox}>
                <span style={styles.scoreLabel}>MCQ Section</span>
                <span style={{ ...styles.scoreValue, color: '#38bdf8' }}>
                  {latestAttempt.mcqScore.earned} / {latestAttempt.mcqScore.total}
                </span>
                <span style={styles.scoreSub}>
                  {Math.round((latestAttempt.mcqScore.earned / latestAttempt.mcqScore.total) * 100)}% Accuracy
                </span>
              </div>
            )}

            {latestAttempt.codingScore.total > 0 && (
              <div style={styles.scoreBox}>
                <span style={styles.scoreLabel}>Coding Section</span>
                <span style={{ ...styles.scoreValue, color: '#a78bfa' }}>
                  {latestAttempt.codingScore.earned} / {latestAttempt.codingScore.total}
                </span>
                <span style={styles.scoreSub}>Test Cases Evaluated</span>
              </div>
            )}
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
            <button
              onClick={() => {
                setExamState('list');
                setCurrentExam(null);
              }}
              style={styles.primaryBtn}
            >
              <ArrowLeft size={15} />
              <span>Back to Placement Exam Portal</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // ==========================================
  // VIEW: TAKING EXAM WORKSPACE
  // ==========================================
  if (examState === 'taking' && currentExam) {
    const isTimerLow = secondsRemaining <= 300;
    const hasMcq = currentExam.mcqQuestions && currentExam.mcqQuestions.length > 0;
    const hasCoding = currentExam.codingQuestions && currentExam.codingQuestions.length > 0;

    const currentMcq = hasMcq ? currentExam.mcqQuestions[activeQuestionIdx] : null;
    const currentCodeQuestion = hasCoding ? currentExam.codingQuestions[activeQuestionIdx] : null;

    return (
      <div style={styles.container}>
        {/* Exam Navigation Bar & Countdown Timer */}
        <div style={styles.examTopBar}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={styles.companyPill}>
                <Building2 size={13} /> {currentExam.companyShort || currentExam.company}
              </span>
              {renderPatternBadge(currentExam)}
            </div>
            <h2 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)', margin: '4px 0 0 0' }}>
              {currentExam.title}
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div
              style={{
                ...styles.timerBox,
                ...(isTimerLow ? styles.timerBoxWarning : {}),
              }}
            >
              <Clock size={16} color={isTimerLow ? '#ef4444' : '#6366f1'} />
              <span style={styles.timerDigits}>{formatTimer(secondsRemaining)}</span>
              <span style={styles.timerLabel}>Remaining</span>
            </div>

            <button
              onClick={() => setShowSubmitModal(true)}
              style={styles.submitExamBtn}
            >
              <Send size={14} />
              <span>Submit Assessment</span>
            </button>
          </div>
        </div>

        {/* Section Tabs (if Assessment includes both MCQs and Coding) */}
        {hasMcq && hasCoding && (
          <div style={{ display: 'flex', gap: '10px', marginBottom: '16px' }}>
            <button
              onClick={() => {
                setActiveSection('mcq');
                setActiveQuestionIdx(0);
              }}
              style={{
                ...styles.sectionTabBtn,
                ...(activeSection === 'mcq' ? styles.sectionTabBtnActive : {}),
              }}
            >
              <FileText size={14} />
              <span>Section 1: Technical MCQs ({currentExam.mcqQuestions.length} Questions)</span>
            </button>
            <button
              onClick={() => {
                setActiveSection('coding');
                setActiveQuestionIdx(0);
              }}
              style={{
                ...styles.sectionTabBtn,
                ...(activeSection === 'coding' ? styles.sectionTabBtnActive : {}),
              }}
            >
              <Terminal size={14} />
              <span>Section 2: Coding Challenges ({currentExam.codingQuestions.length} Questions)</span>
            </button>
          </div>
        )}

        {/* Main Exam Question Area */}
        {activeSection === 'mcq' && currentMcq && (
          <div style={styles.questionWorkspace}>
            {/* Question Card */}
            <div style={styles.card}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                <span style={styles.qIndexBadge}>
                  Question {activeQuestionIdx + 1} of {currentExam.mcqQuestions.length}
                </span>
                <span style={styles.marksBadge}>+{currentMcq.marks} Marks</span>
              </div>

              <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '8px' }}>
                Topic: {currentMcq.topic}
              </div>

              <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)', lineHeight: 1.5, marginBottom: '20px' }}>
                {currentMcq.question}
              </h3>

              {/* Options */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {currentMcq.options.map((opt, oIdx) => {
                  const isSelected = mcqAnswers[currentMcq.id] === oIdx;
                  return (
                    <label
                      key={oIdx}
                      onClick={() => handleSelectMcq(currentMcq.id, oIdx)}
                      style={{
                        ...styles.optionCard,
                        ...(isSelected ? styles.optionCardSelected : {}),
                      }}
                    >
                      <input
                        type="radio"
                        name={currentMcq.id}
                        checked={isSelected}
                        onChange={() => {}}
                        style={{ marginRight: '12px' }}
                      />
                      <span style={{ fontSize: '14px', color: isSelected ? '#6366f1' : 'var(--text-primary)', fontWeight: isSelected ? '600' : '400' }}>
                        {opt}
                      </span>
                    </label>
                  );
                })}
              </div>

              {/* Navigation Controls */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '24px', paddingTop: '16px', borderTop: '1px solid var(--border-subtle)' }}>
                <button
                  onClick={() => setActiveQuestionIdx((prev) => Math.max(0, prev - 1))}
                  disabled={activeQuestionIdx === 0}
                  style={styles.navBtn}
                >
                  <ChevronLeft size={16} /> Previous
                </button>

                {activeQuestionIdx < currentExam.mcqQuestions.length - 1 ? (
                  <button
                    onClick={() => setActiveQuestionIdx((prev) => Math.min(currentExam.mcqQuestions.length - 1, prev + 1))}
                    style={styles.primaryBtn}
                  >
                    Next <ChevronRight size={16} />
                  </button>
                ) : hasCoding ? (
                  <button
                    onClick={() => {
                      setActiveSection('coding');
                      setActiveQuestionIdx(0);
                    }}
                    style={styles.primaryBtn}
                  >
                    Proceed to Coding Section <ChevronRight size={16} />
                  </button>
                ) : (
                  <button onClick={() => setShowSubmitModal(true)} style={styles.submitExamBtn}>
                    Finish & Submit
                  </button>
                )}
              </div>
            </div>

            {/* Question Palette Sidebar */}
            <div style={styles.paletteCard}>
              <h4 style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '12px' }}>
                Question Palette
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px' }}>
                {currentExam.mcqQuestions.map((q, idx) => {
                  const isAnswered = mcqAnswers[q.id] !== undefined;
                  const isCurrent = idx === activeQuestionIdx;
                  return (
                    <button
                      key={q.id}
                      onClick={() => setActiveQuestionIdx(idx)}
                      style={{
                        ...styles.paletteBtn,
                        ...(isAnswered ? styles.paletteBtnAnswered : {}),
                        ...(isCurrent ? styles.paletteBtnCurrent : {}),
                      }}
                    >
                      {idx + 1}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Coding Question Area */}
        {activeSection === 'coding' && currentCodeQuestion && (
          <div style={styles.codingGrid}>
            {/* Left: Problem & Constraints */}
            <div style={styles.card}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={styles.qIndexBadge}>
                  Coding Challenge #{activeQuestionIdx + 1}
                </span>
                <span style={styles.marksBadge}>+{currentCodeQuestion.marks} Marks</span>
              </div>

              <h2 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)', marginBottom: '10px' }}>
                {currentCodeQuestion.title}
              </h2>
              <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                {currentCodeQuestion.description}
              </p>

              {currentCodeQuestion.constraints && (
                <div style={{ marginTop: '14px' }}>
                  <h4 style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)', marginBottom: '6px' }}>
                    Constraints:
                  </h4>
                  <ul style={{ paddingLeft: '18px', margin: 0, fontSize: '12px', color: 'var(--text-secondary)' }}>
                    {currentCodeQuestion.constraints.map((c, i) => (
                      <li key={i}>{c}</li>
                    ))}
                  </ul>
                </div>
              )}

              <div style={{ marginTop: '16px' }}>
                <h4 style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)', marginBottom: '8px' }}>
                  Verification Test Cases:
                </h4>
                {currentCodeQuestion.testCases?.map((tc, idx) => (
                  <div key={idx} style={styles.testCaseItem}>
                    <div><strong>Input:</strong> <code>{tc.input}</code></div>
                    <div><strong>Expected:</strong> <code style={{ color: '#10b981' }}>{tc.expected}</code></div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right: Code Editor */}
            <div style={styles.card}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Terminal size={14} color="#6366f1" /> Solution Editor (Java / Python)
                </span>
                <button
                  type="button"
                  onClick={() => handleCodingChange(currentCodeQuestion.id, currentCodeQuestion.starterCode)}
                  style={styles.smallTextBtn}
                >
                  <RotateCcw size={12} /> Reset Template
                </button>
              </div>

              <textarea
                value={codingAnswers[currentCodeQuestion.id] || ''}
                onChange={(e) => handleCodingChange(currentCodeQuestion.id, e.target.value)}
                placeholder="// Implement your solution logic here..."
                rows={18}
                style={styles.codeTextarea}
              />

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '16px' }}>
                <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Code auto-saves continuously
                </div>
                <button
                  onClick={() => setShowSubmitModal(true)}
                  style={styles.submitExamBtn}
                >
                  <Send size={13} /> Complete & Submit
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Confirmation Modal */}
        {showSubmitModal && (
          <div style={styles.modalOverlay}>
            <div style={styles.modalCard}>
              <h3 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)', margin: '0 0 8px 0' }}>
                Submit Assessment?
              </h3>
              <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5, margin: '0 0 16px 0' }}>
                Are you sure you want to finalize and submit your assessment for <strong>{currentExam.company}</strong>?
                You will receive your graded scorecard immediately.
              </p>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button onClick={() => setShowSubmitModal(false)} style={styles.navBtn}>
                  Cancel & Review
                </button>
                <button onClick={handleSubmitConfirm} style={styles.primaryBtn}>
                  Yes, Submit Now
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  // ==========================================
  // VIEW: EXAM PORTAL ROSTER (DEFAULT)
  // ==========================================
  return (
    <div style={styles.container}>
      {/* Top Banner */}
      <div style={styles.banner}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '12px', fontWeight: '700', color: '#6366f1', marginBottom: '6px' }}>
            <Award size={14} />
            <span>Campus Recruitment Assessment Engine</span>
          </div>
          <h1 style={styles.bannerTitle}>Online Placement Exam Portal</h1>
          <p style={styles.bannerSubtitle}>
            Take timed technical screening examinations scheduled for campus placement drives.
            Assessment patterns and evaluation structures are authorized directly by the placement cell for each participating company.
          </p>
        </div>
      </div>

      {/* Filter Tabs */}
      <div style={styles.tabsRow}>
        {[
          { id: 'ALL', label: `All Assessments (${exams.length})` },
          { id: 'MCQ', label: 'Objective MCQs' },
          { id: 'CODING', label: 'Coding Challenges' },
          { id: 'COMBINED', label: 'MCQs & Coding Combined' },
          { id: 'ATTEMPTED', label: `My Attempts (${attempts.length})` },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              ...styles.tabBtn,
              ...(activeTab === tab.id ? styles.tabBtnActive : {}),
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Exam Cards Grid */}
      <div style={styles.examsGrid}>
        {filteredExams.map((exam) => {
          const attempt = attemptMap.get(exam.id);

          return (
            <div key={exam.id} style={styles.examCard}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                    <span style={styles.companyPill}>
                      <Building2 size={12} /> {exam.company}
                    </span>
                    {renderPatternBadge(exam)}
                  </div>
                  <h3 style={styles.examCardTitle}>{exam.title}</h3>
                  <div style={{ fontSize: '12.5px', color: 'var(--text-muted)' }}>
                    Target Role: <strong>{exam.role}</strong>
                  </div>
                </div>
              </div>

              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.45, marginBottom: '16px' }}>
                {exam.description}
              </p>

              <div style={styles.metaRow}>
                <div style={styles.metaItem}>
                  <Clock size={13} />
                  <span>{exam.durationMinutes} Minutes</span>
                </div>
                <div style={styles.metaItem}>
                  <Award size={13} />
                  <span>{exam.totalMarks} Marks (Pass: {exam.passingMarks})</span>
                </div>
                <div style={styles.metaItem}>
                  <ShieldCheck size={13} color="#10b981" />
                  <span>By: {exam.authorizedBy}</span>
                </div>
              </div>

              {/* Action / Status Footer */}
              <div style={styles.cardFooter}>
                {attempt ? (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <CheckCircle2 size={16} color={attempt.passed ? '#10b981' : '#f59e0b'} />
                      <span style={{ fontSize: '13px', fontWeight: '700', color: attempt.passed ? '#10b981' : '#f59e0b' }}>
                        Score: {attempt.earnedMarks} / {attempt.totalMarks} ({attempt.passed ? 'Qualified' : 'Attempted'})
                      </span>
                    </div>

                    <button
                      onClick={() => {
                        setLatestAttempt(attempt);
                        setExamState('result');
                      }}
                      style={styles.secondaryBtn}
                    >
                      View Scorecard
                    </button>
                  </div>
                ) : (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                    <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                      Ready to start anytime
                    </span>
                    <button
                      onClick={() => handleOpenInstructions(exam)}
                      style={styles.primaryBtn}
                    >
                      <Play size={13} />
                      <span>Take Assessment</span>
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Pre-Exam Instructions Modal */}
      {showInstructionsModal && selectedExamForInstructions && (
        <div style={styles.modalOverlay}>
          <div style={styles.instructionsModal}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                  <span style={styles.companyPill}>
                    <Building2 size={12} /> {selectedExamForInstructions.company}
                  </span>
                  {renderPatternBadge(selectedExamForInstructions)}
                </div>
                <h2 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                  {selectedExamForInstructions.title}
                </h2>
                <div style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '2px' }}>
                  Target Role: <strong>{selectedExamForInstructions.role}</strong> • Authorized By: <strong>{selectedExamForInstructions.authorizedBy}</strong>
                </div>
              </div>
              <button
                onClick={() => {
                  setShowInstructionsModal(false);
                  setSelectedExamForInstructions(null);
                }}
                style={styles.closeBtn}
              >
                ✕
              </button>
            </div>

            {/* Assessment Structure Overview Bar */}
            <div style={styles.instMetaGrid}>
              <div style={styles.instMetaCard}>
                <span style={styles.instMetaLabel}>Scheduled Duration</span>
                <span style={styles.instMetaValue}>
                  <Clock size={15} color="#6366f1" /> {selectedExamForInstructions.durationMinutes} Minutes
                </span>
                <span style={styles.instMetaSub}>Non-stop countdown</span>
              </div>
              <div style={styles.instMetaCard}>
                <span style={styles.instMetaLabel}>Assessment Pattern</span>
                <span style={styles.instMetaValue}>
                  <Layers size={15} color="#38bdf8" /> {getExamPatternText(selectedExamForInstructions)}
                </span>
                <span style={styles.instMetaSub}>Company configured</span>
              </div>
              <div style={styles.instMetaCard}>
                <span style={styles.instMetaLabel}>Total Marks</span>
                <span style={styles.instMetaValue}>
                  <Award size={15} color="#10b981" /> {selectedExamForInstructions.totalMarks} Marks
                </span>
                <span style={styles.instMetaSub}>Cutoff: {selectedExamForInstructions.passingMarks} Marks</span>
              </div>
            </div>

            {/* Section Breakdown */}
            <div style={styles.sectionBreakdownBox}>
              <div style={{ fontSize: '12.5px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '6px' }}>
                Assessment Structure & Marking Scheme:
              </div>
              <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', fontSize: '12.5px', color: 'var(--text-secondary)' }}>
                {selectedExamForInstructions.mcqQuestions?.length > 0 && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <FileText size={14} color="#38bdf8" />
                    <span><strong>{selectedExamForInstructions.mcqQuestions.length} Technical MCQs</strong> (+{selectedExamForInstructions.mcqQuestions[0]?.marks || 5} marks each, no negative marking)</span>
                  </div>
                )}
                {selectedExamForInstructions.codingQuestions?.length > 0 && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Terminal size={14} color="#a78bfa" />
                    <span><strong>{selectedExamForInstructions.codingQuestions.length} Algorithmic Coding Problem(s)</strong> (evaluated against automated test cases)</span>
                  </div>
                )}
              </div>
            </div>

            {/* Mandatory Guidelines & Honor Code */}
            <div style={styles.instructionsRulesBox}>
              <h4 style={{ fontSize: '13px', fontWeight: '800', color: 'var(--text-primary)', margin: '0 0 10px 0', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <ShieldCheck size={16} color="#6366f1" /> Mandatory Assessment Rules & Instructions:
              </h4>
              <ol style={{ margin: 0, paddingLeft: '18px', fontSize: '12.5px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                <li><strong>Continuous Countdown Timer:</strong> The timer begins immediately when you click <em>"Begin Assessment & Start Timer"</em>. It runs continuously and cannot be paused.</li>
                <li><strong>Single Window Session:</strong> You must maintain focus on this assessment window throughout the test. Tab switching or minimizing may be flagged in invigilation logs.</li>
                <li><strong>Automatic Real-time Saving:</strong> All selected MCQ options and written code solutions are auto-saved in real time.</li>
                <li><strong>Code Testing:</strong> For coding challenges, use "Run & Test Solution" to validate your logic against sample test cases before submitting.</li>
                <li><strong>Automated Submission:</strong> At 00:00 remaining, your assessment will be submitted automatically. Your score and performance breakdown will appear immediately.</li>
                <li><strong>Academic Integrity:</strong> By proceeding, you affirm that all solutions will be entirely your own work, adhering to the Placement Cell code of conduct.</li>
              </ol>
            </div>

            {/* Acknowledgment Checkbox */}
            <label
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px',
                padding: '12px 14px',
                borderRadius: '8px',
                background: agreedInstructions ? 'rgba(99, 102, 241, 0.08)' : 'var(--bg-subtle)',
                border: agreedInstructions ? '1px solid #6366f1' : '1px solid var(--border-subtle)',
                cursor: 'pointer',
                marginTop: '16px',
                marginBottom: '18px',
                transition: 'all 0.2s ease',
              }}
            >
              <input
                type="checkbox"
                checked={agreedInstructions}
                onChange={(e) => setAgreedInstructions(e.target.checked)}
                style={{ marginTop: '2px', cursor: 'pointer' }}
              />
              <span style={{ fontSize: '12.5px', color: 'var(--text-primary)', fontWeight: agreedInstructions ? '600' : '400', lineHeight: 1.45 }}>
                I have read and agree to all the assessment instructions, timing rules, and honor code above. I am ready to start my timed session.
              </span>
            </label>

            {/* Footer Buttons */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button
                onClick={() => {
                  setShowInstructionsModal(false);
                  setSelectedExamForInstructions(null);
                }}
                style={styles.cancelBtn}
              >
                Cancel / Return
              </button>
              <button
                onClick={handleConfirmBeginExam}
                disabled={!agreedInstructions}
                style={{
                  ...styles.primaryBtn,
                  opacity: agreedInstructions ? 1 : 0.45,
                  cursor: agreedInstructions ? 'pointer' : 'not-allowed',
                  padding: '10px 20px',
                  fontSize: '13px',
                  fontWeight: '700',
                }}
              >
                <Play size={14} />
                <span>Begin Assessment & Start Timer ({selectedExamForInstructions.durationMinutes} Mins)</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: {
    padding: '0 0 40px 0',
    fontFamily: 'var(--font-sans)',
  },
  banner: {
    background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(56, 189, 248, 0.04) 100%)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '16px',
    padding: '24px 28px',
    marginBottom: '20px',
  },
  bannerTitle: {
    fontSize: '22px',
    fontWeight: '800',
    color: 'var(--text-primary)',
    margin: '0 0 6px 0',
  },
  bannerSubtitle: {
    fontSize: '13px',
    color: 'var(--text-secondary)',
    maxWidth: '750px',
    lineHeight: 1.5,
    margin: 0,
  },
  tabsRow: {
    display: 'flex',
    gap: '8px',
    overflowX: 'auto',
    borderBottom: '1px solid var(--border-subtle)',
    marginBottom: '18px',
    paddingBottom: '4px',
  },
  tabBtn: {
    padding: '8px 14px',
    fontSize: '13px',
    fontWeight: '500',
    color: 'var(--text-muted)',
    background: 'none',
    border: 'none',
    borderBottom: '2px solid transparent',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
  },
  tabBtnActive: {
    color: '#6366f1',
    fontWeight: '700',
    borderBottom: '2px solid #6366f1',
  },
  examsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))',
    gap: '16px',
  },
  examCard: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '14px',
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
  },
  examCardTitle: {
    fontSize: '16px',
    fontWeight: '800',
    color: 'var(--text-primary)',
    margin: '0 0 4px 0',
  },
  metaRow: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
    marginBottom: '16px',
    padding: '10px 12px',
    background: 'var(--bg-subtle, rgba(0, 0, 0, 0.03))',
    borderRadius: '8px',
  },
  metaItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '12px',
    color: 'var(--text-muted)',
  },
  cardFooter: {
    paddingTop: '12px',
    borderTop: '1px solid var(--border-subtle)',
    display: 'flex',
    alignItems: 'center',
  },
  companyPill: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    padding: '2px 8px',
    borderRadius: '6px',
    fontSize: '11px',
    fontWeight: '600',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
  },
  primaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '8px 14px',
    borderRadius: '8px',
    fontSize: '12.5px',
    fontWeight: '600',
    background: '#6366f1',
    border: 'none',
    color: '#ffffff',
    cursor: 'pointer',
  },
  secondaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '7px 12px',
    borderRadius: '8px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    cursor: 'pointer',
  },
  // Active Taking Exam Styles
  examTopBar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '14px',
    padding: '14px 20px',
    marginBottom: '16px',
    flexWrap: 'wrap',
    gap: '12px',
  },
  timerBox: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    padding: '6px 14px',
    borderRadius: '8px',
    background: 'rgba(99, 102, 241, 0.1)',
    border: '1px solid rgba(99, 102, 241, 0.25)',
  },
  timerBoxWarning: {
    background: 'rgba(239, 68, 68, 0.12)',
    border: '1px solid rgba(239, 68, 68, 0.35)',
  },
  timerDigits: {
    fontSize: '16px',
    fontWeight: '800',
    fontFamily: 'monospace',
    color: 'var(--text-primary)',
  },
  timerLabel: {
    fontSize: '11px',
    color: 'var(--text-muted)',
  },
  submitExamBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '8px 14px',
    borderRadius: '8px',
    fontSize: '12.5px',
    fontWeight: '700',
    background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    border: 'none',
    color: '#ffffff',
    cursor: 'pointer',
  },
  sectionTabBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '8px 14px',
    borderRadius: '8px',
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-muted)',
    fontSize: '13px',
    cursor: 'pointer',
  },
  sectionTabBtnActive: {
    borderColor: '#6366f1',
    background: 'rgba(99, 102, 241, 0.08)',
    color: '#6366f1',
    fontWeight: '700',
  },
  questionWorkspace: {
    display: 'grid',
    gridTemplateColumns: '1fr 260px',
    gap: '16px',
  },
  card: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '14px',
    padding: '20px 24px',
  },
  qIndexBadge: {
    fontSize: '12px',
    fontWeight: '700',
    color: '#6366f1',
    background: 'rgba(99, 102, 241, 0.1)',
    padding: '3px 8px',
    borderRadius: '6px',
  },
  marksBadge: {
    fontSize: '11.5px',
    fontWeight: '700',
    color: '#10b981',
    background: 'rgba(16, 185, 129, 0.1)',
    padding: '3px 8px',
    borderRadius: '6px',
  },
  optionCard: {
    display: 'flex',
    alignItems: 'center',
    padding: '12px 16px',
    borderRadius: '10px',
    border: '1px solid var(--border-subtle)',
    background: 'var(--bg-subtle, rgba(0, 0, 0, 0.02))',
    cursor: 'pointer',
    transition: 'all 0.15s ease',
  },
  optionCardSelected: {
    borderColor: '#6366f1',
    background: 'rgba(99, 102, 241, 0.06)',
  },
  navBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    padding: '7px 12px',
    borderRadius: '8px',
    background: 'none',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    fontSize: '12.5px',
    cursor: 'pointer',
  },
  paletteCard: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '14px',
    padding: '16px',
    height: 'fit-content',
  },
  paletteBtn: {
    width: '100%',
    aspectRatio: '1',
    borderRadius: '8px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-muted)',
    fontSize: '12px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  paletteBtnAnswered: {
    background: 'rgba(16, 185, 129, 0.15)',
    color: '#10b981',
    borderColor: 'rgba(16, 185, 129, 0.4)',
  },
  paletteBtnCurrent: {
    borderColor: '#6366f1',
    boxShadow: '0 0 0 2px rgba(99, 102, 241, 0.3)',
  },
  // Coding Workspace Layout
  codingGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '16px',
  },
  codeTextarea: {
    width: '100%',
    padding: '14px',
    borderRadius: '10px',
    background: '#0f172a',
    color: '#f8fafc',
    fontFamily: 'Consolas, Monaco, "Courier New", monospace',
    fontSize: '13px',
    lineHeight: 1.5,
    border: '1px solid var(--border-subtle)',
    outline: 'none',
    boxSizing: 'border-box',
    resize: 'vertical',
  },
  testCaseItem: {
    padding: '8px 12px',
    borderRadius: '6px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    fontSize: '12px',
    marginBottom: '6px',
  },
  smallTextBtn: {
    background: 'none',
    border: 'none',
    color: 'var(--text-muted)',
    fontSize: '11px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
  },
  // Result View
  resultBanner: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '16px',
    padding: '28px',
  },
  scoreCardsRow: {
    display: 'flex',
    gap: '14px',
    flexWrap: 'wrap',
    margin: '20px 0',
  },
  scoreBox: {
    padding: '16px 20px',
    borderRadius: '12px',
    background: 'var(--bg-subtle, rgba(0,0,0,0.03))',
    border: '1px solid var(--border-subtle)',
    minWidth: '150px',
  },
  scoreLabel: {
    display: 'block',
    fontSize: '11px',
    color: 'var(--text-muted)',
    textTransform: 'uppercase',
    fontWeight: '700',
    marginBottom: '6px',
  },
  scoreValue: {
    display: 'block',
    fontSize: '22px',
    fontWeight: '800',
    marginBottom: '4px',
  },
  scoreSub: {
    fontSize: '12px',
    color: 'var(--text-muted)',
  },
  // Modal
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0, 0, 0, 0.6)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 9999,
  },
  modalCard: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '14px',
    padding: '24px',
    maxWidth: '440px',
    width: '90%',
  },
  instructionsModal: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '16px',
    padding: '28px',
    maxWidth: '640px',
    width: '92%',
    maxHeight: '90vh',
    overflowY: 'auto',
    boxShadow: '0 20px 40px rgba(0, 0, 0, 0.25)',
  },
  closeBtn: {
    background: 'none',
    border: 'none',
    fontSize: '18px',
    color: 'var(--text-muted)',
    cursor: 'pointer',
    padding: '4px',
    borderRadius: '6px',
    lineHeight: 1,
  },
  cancelBtn: {
    padding: '9px 18px',
    borderRadius: '8px',
    border: '1px solid var(--border-subtle)',
    background: 'var(--bg-subtle)',
    color: 'var(--text-secondary)',
    fontSize: '13px',
    fontWeight: '600',
    cursor: 'pointer',
  },
  instMetaGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: '12px',
    margin: '18px 0 14px 0',
  },
  instMetaCard: {
    padding: '12px 14px',
    borderRadius: '10px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
  },
  instMetaLabel: {
    fontSize: '11px',
    fontWeight: '700',
    color: 'var(--text-muted)',
    textTransform: 'uppercase',
  },
  instMetaValue: {
    fontSize: '13.5px',
    fontWeight: '800',
    color: 'var(--text-primary)',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  instMetaSub: {
    fontSize: '11px',
    color: 'var(--text-muted)',
  },
  sectionBreakdownBox: {
    padding: '12px 14px',
    background: 'rgba(99, 102, 241, 0.05)',
    borderRadius: '10px',
    border: '1px solid rgba(99, 102, 241, 0.18)',
    marginBottom: '14px',
  },
  instructionsRulesBox: {
    padding: '14px 16px',
    background: 'var(--bg-subtle)',
    borderRadius: '10px',
    border: '1px solid var(--border-subtle)',
  },
};

