import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { examService, getExamPatternText } from '../../services/examService';
import {
  FileText,
  Clock,
  Award,
  Building2,
  Plus,
  CheckCircle2,
  X,
  Terminal,
  HelpCircle,
  Users,
  ShieldCheck,
  Code,
  Sparkles,
  Layers,
} from 'lucide-react';

export default function ExamManagement() {
  const { user } = useAuth();
  const [exams, setExams] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [activeTab, setActiveTab] = useState('EXAMS'); // 'EXAMS' | 'SUBMISSIONS'

  // Modal State for Creating Exam
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formData, setFormData] = useState({
    company: '',
    companyShort: '',
    title: '',
    role: '',
    format: 'HYBRID', // 'MCQ' | 'CODING' | 'HYBRID'
    durationMinutes: '60',
    totalMarks: '50',
    passingMarks: '30',
    authorizedBy: user?.name ? `${user.name} (${user.role === 'ROLE_PLACEMENT_OFFICER' ? 'Head TPO' : 'Placement Coordinator'})` : 'Head TPO (Placement Cell)',
    description: '',
    // Quick starter MCQ
    sampleMcqQuestion: 'What is the worst case time complexity of binary search on a sorted array?',
    sampleMcqOptions: ['O(N)', 'O(log N)', 'O(N log N)', 'O(1)'],
    sampleMcqCorrect: 1,
    // Quick starter Coding problem
    sampleCodeTitle: 'Two Sum Optimal Search',
    sampleCodeDesc: 'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target in O(N) time.',
    sampleCodeTest: 'nums = [2,7,11,15], target = 9 -> [0,1]',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = () => {
    const allExams = examService.getAllExams();
    const allSubs = examService.getExamSubmissions();
    setExams(allExams);
    setSubmissions(allSubs);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateExam = (e) => {
    e.preventDefault();

    const mcqQuestions = [];
    if (formData.format === 'MCQ' || formData.format === 'HYBRID') {
      mcqQuestions.push({
        id: `q-${Date.now()}`,
        question: formData.sampleMcqQuestion,
        options: formData.sampleMcqOptions,
        correctOption: Number(formData.sampleMcqCorrect),
        marks: 10,
        topic: 'Computer Science & Algorithms',
      });
    }

    const codingQuestions = [];
    if (formData.format === 'CODING' || formData.format === 'HYBRID') {
      codingQuestions.push({
        id: `cq-${Date.now()}`,
        title: formData.sampleCodeTitle,
        description: formData.sampleCodeDesc,
        marks: formData.format === 'HYBRID' ? 40 : Number(formData.totalMarks),
        starterCode: `// Java 21 Solution Template\npublic class Solution {\n    public static int[] solve() {\n        return new int[]{};\n    }\n}`,
        testCases: [{ input: 'Standard Sample Input', expected: 'Optimal Target Output' }],
        constraints: ['Optimal Time Complexity Expected', 'Handle empty inputs safely'],
      });
    }

    examService.createExam({
      company: formData.company,
      companyShort: formData.companyShort || formData.company.substring(0, 4),
      title: formData.title,
      role: formData.role,
      format: formData.format,
      durationMinutes: Number(formData.durationMinutes),
      totalMarks: Number(formData.totalMarks),
      passingMarks: Number(formData.passingMarks),
      authorizedBy: formData.authorizedBy,
      description: formData.description,
      mcqQuestions,
      codingQuestions,
    });

    loadData();
    setShowCreateModal(false);
  };

  const renderPatternBadge = (exam) => {
    const patternText = typeof exam === 'string' ? exam : getExamPatternText(exam);
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '5px',
          fontSize: '11px',
          fontWeight: '700',
          padding: '2px 8px',
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

  return (
    <div style={styles.container}>
      {/* Top Banner */}
      <div style={styles.banner}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '14px' }}>
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '12px', fontWeight: '700', color: '#6366f1', marginBottom: '6px' }}>
              <ShieldCheck size={14} />
              <span>Placement Cell Authority Console</span>
            </div>
            <h1 style={styles.bannerTitle}>Recruitment Assessment & Pattern Manager</h1>
            <p style={styles.bannerSubtitle}>
              Configure and authorize company-specific placement assessments. Set assessment patterns and testing structures based on participating corporate requirements.
            </p>
          </div>

          <button
            onClick={() => setShowCreateModal(true)}
            style={styles.primaryBtn}
          >
            <Plus size={16} />
            <span>Schedule New Placement Exam</span>
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div style={styles.tabsRow}>
        <button
          onClick={() => setActiveTab('EXAMS')}
          style={{
            ...styles.tabBtn,
            ...(activeTab === 'EXAMS' ? styles.tabBtnActive : {}),
          }}
        >
          Scheduled Company Exams ({exams.length})
        </button>
        <button
          onClick={() => setActiveTab('SUBMISSIONS')}
          style={{
            ...styles.tabBtn,
            ...(activeTab === 'SUBMISSIONS' ? styles.tabBtnActive : {}),
          }}
        >
          Candidate Exam Submissions ({submissions.length})
        </button>
      </div>

      {/* View 1: Active Exams List */}
      {activeTab === 'EXAMS' ? (
        <div style={styles.examsGrid}>
          {exams.map((exam) => {
            const subsForThis = submissions.filter((s) => s.examId === exam.id);

            return (
              <div key={exam.id} style={styles.examCard}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                      <span style={styles.companyPill}>
                        <Building2 size={12} /> {exam.company}
                      </span>
                      {renderPatternBadge(exam)}
                    </div>
                    <h3 style={styles.examCardTitle}>{exam.title}</h3>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
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
                    <span>Duration: {exam.durationMinutes} Mins</span>
                  </div>
                  <div style={styles.metaItem}>
                    <Award size={13} />
                    <span>Total Marks: {exam.totalMarks} (Cutoff: {exam.passingMarks})</span>
                  </div>
                  <div style={styles.metaItem}>
                    <Users size={13} color="#6366f1" />
                    <span>Candidate Attempts: {subsForThis.length}</span>
                  </div>
                  <div style={styles.metaItem}>
                    <ShieldCheck size={13} color="#10b981" />
                    <span>Authorized By: {exam.authorizedBy}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        /* View 2: Submissions Table */
        <div style={styles.tableCard}>
          {submissions.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
              No student submissions logged yet.
            </div>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr style={styles.tableHeaderRow}>
                  <th style={styles.th}>Candidate</th>
                  <th style={styles.th}>Assessment / Company</th>
                  <th style={styles.th}>Assessment Pattern</th>
                  <th style={styles.th}>Score & Percentage</th>
                  <th style={styles.th}>Result Status</th>
                  <th style={styles.th}>Submission Time</th>
                </tr>
              </thead>
              <tbody>
                {submissions.map((sub, idx) => (
                  <tr key={idx} style={styles.tableRow}>
                    <td style={{ ...styles.td, fontWeight: '700', color: 'var(--text-primary)' }}>
                      {sub.studentName}
                    </td>
                    <td style={styles.td}>
                      <div style={{ fontWeight: '600' }}>{sub.examTitle}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{sub.company}</div>
                    </td>
                    <td style={styles.td}>
                      {renderPatternBadge(sub.examPattern || sub.format)}
                    </td>
                    <td style={styles.td}>
                      <span style={{ fontWeight: '800', color: sub.passed ? '#10b981' : '#f59e0b' }}>
                        {sub.earnedMarks} / {sub.totalMarks} ({sub.percentage}%)
                      </span>
                    </td>
                    <td style={styles.td}>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '12px', fontWeight: '700', color: sub.passed ? '#10b981' : '#ef4444' }}>
                        <CheckCircle2 size={13} /> {sub.passed ? 'QUALIFIED' : 'FAILED CUTOFF'}
                      </span>
                    </td>
                    <td style={{ ...styles.td, fontSize: '12px', color: 'var(--text-muted)' }}>
                      {new Date(sub.submittedAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Modal: Create / Authorize New Placement Exam */}
      {showCreateModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalCard}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Sparkles size={18} color="#6366f1" />
                <h2 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text-primary)', margin: 0 }}>
                  Schedule Placement Assessment
                </h2>
              </div>
              <button onClick={() => setShowCreateModal(false)} style={styles.iconBtn}>
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleCreateExam} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={styles.formGrid}>
                <div>
                  <label style={styles.label}>Recruiting Company *</label>
                  <input
                    type="text"
                    name="company"
                    required
                    placeholder="e.g. Microsoft / Cisco / Oracle"
                    value={formData.company}
                    onChange={handleInputChange}
                    style={styles.input}
                  />
                </div>

                <div>
                  <label style={styles.label}>Target Job Role *</label>
                  <input
                    type="text"
                    name="role"
                    required
                    placeholder="e.g. Software Engineer / Cloud Analyst"
                    value={formData.role}
                    onChange={handleInputChange}
                    style={styles.input}
                  />
                </div>

                <div style={{ gridColumn: 'span 2' }}>
                  <label style={styles.label}>Assessment Title *</label>
                  <input
                    type="text"
                    name="title"
                    required
                    placeholder="e.g. Microsoft Campus Code Assessment 2026"
                    value={formData.title}
                    onChange={handleInputChange}
                    style={styles.input}
                  />
                </div>

                {/* Assessment Pattern Selector */}
                <div style={{ gridColumn: 'span 2' }}>
                  <label style={styles.label}>
                    Assessment Pattern (Configured per Company Criteria) *
                  </label>
                  <select
                    name="format"
                    value={formData.format}
                    onChange={handleInputChange}
                    style={{ ...styles.input, fontWeight: '700', color: '#6366f1' }}
                  >
                    <option value="MCQ">Pattern: Objective Multiple Choice Questions (Technical MCQs)</option>
                    <option value="CODING">Pattern: Practical Algorithmic Coding Challenges</option>
                    <option value="HYBRID">Pattern: Comprehensive Evaluation (Technical MCQs + Coding Problems)</option>
                  </select>
                </div>

                <div>
                  <label style={styles.label}>Duration (Minutes) *</label>
                  <input
                    type="number"
                    name="durationMinutes"
                    required
                    value={formData.durationMinutes}
                    onChange={handleInputChange}
                    style={styles.input}
                  />
                </div>

                <div>
                  <label style={styles.label}>Total Marks & Cutoff (Passing) *</label>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <input
                      type="number"
                      name="totalMarks"
                      placeholder="Total"
                      required
                      value={formData.totalMarks}
                      onChange={handleInputChange}
                      style={styles.input}
                    />
                    <input
                      type="number"
                      name="passingMarks"
                      placeholder="Pass Cutoff"
                      required
                      value={formData.passingMarks}
                      onChange={handleInputChange}
                      style={styles.input}
                    />
                  </div>
                </div>

                <div style={{ gridColumn: 'span 2' }}>
                  <label style={styles.label}>Authorized By Designation</label>
                  <input
                    type="text"
                    name="authorizedBy"
                    value={formData.authorizedBy}
                    onChange={handleInputChange}
                    style={styles.input}
                  />
                </div>

                <div style={{ gridColumn: 'span 2' }}>
                  <label style={styles.label}>Assessment Description & Instructions</label>
                  <textarea
                    name="description"
                    rows={2}
                    placeholder="Instructions for students regarding camera proctoring, test cases, and passing criteria..."
                    value={formData.description}
                    onChange={handleInputChange}
                    style={styles.input}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                <button type="button" onClick={() => setShowCreateModal(false)} style={styles.secondaryBtn}>
                  Cancel
                </button>
                <button type="submit" style={styles.primaryBtn}>
                  Authorize & Publish Exam
                </button>
              </div>
            </form>
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
  tableCard: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '14px',
    overflow: 'hidden',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    textAlign: 'left',
  },
  tableHeaderRow: {
    background: 'var(--bg-subtle, rgba(0,0,0,0.04))',
    borderBottom: '1px solid var(--border-subtle)',
  },
  th: {
    padding: '12px 14px',
    fontSize: '12px',
    fontWeight: '600',
    color: 'var(--text-muted)',
    textTransform: 'uppercase',
  },
  tableRow: {
    borderBottom: '1px solid var(--border-subtle)',
  },
  td: {
    padding: '12px 14px',
    fontSize: '13px',
    verticalAlign: 'middle',
  },
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0, 0, 0, 0.65)',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 9999,
    padding: '20px',
  },
  modalCard: {
    background: 'var(--bg-main)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '16px',
    padding: '24px',
    maxWidth: '560px',
    width: '100%',
    maxHeight: '90vh',
    overflowY: 'auto',
  },
  formGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '12px',
  },
  label: {
    display: 'block',
    fontSize: '12px',
    fontWeight: '600',
    color: 'var(--text-secondary)',
    marginBottom: '4px',
  },
  input: {
    width: '100%',
    padding: '8px 12px',
    borderRadius: '8px',
    border: '1px solid var(--border-subtle)',
    background: 'var(--bg-subtle, rgba(0,0,0,0.03))',
    color: 'var(--text-primary)',
    fontSize: '13px',
    outline: 'none',
    boxSizing: 'border-box',
  },
  iconBtn: {
    background: 'none',
    border: 'none',
    color: 'var(--text-muted)',
    cursor: 'pointer',
  },
};

