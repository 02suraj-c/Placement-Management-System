import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { studentService } from '../../services/studentService';
import { resumeService } from '../../services/resumeService';
import { atsService } from '../../services/atsService';
import {
  User,
  GraduationCap,
  Award,
  FileCheck,
  FileText,
  Upload,
  Download,
  Loader2,
  Edit2,
  Save,
  X,
  AlertCircle,
  CheckCircle,
  Phone,
  Mail,
  Hash,
  BookOpen,
  Calendar,
  Sparkles,
  Linkedin,
  Github,
  Globe,
  ExternalLink,
  Briefcase,
} from 'lucide-react';

export default function StudentProfile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [validationErrors, setValidationErrors] = useState({});

  // Editable Form State
  const [formData, setFormData] = useState({
    department: '',
    course: '',
    passingYear: '',
    cgpa: '',
    tenthPercentage: '',
    twelfthPercentage: '',
    activeBacklogs: '0',
    totalBacklogs: '0',
    phoneNumber: '',
    skills: '',
    linkedinUrl: '',
    githubUrl: '',
    preferredDomain: 'Software Development & Engineering',
    // Initial profile creation fields
    rollNumber: '',
    enrollmentNumber: '',
  });

  // Resume state (Module 2)
  const [resume, setResume] = useState(null);
  const [resumeLoading, setResumeLoading] = useState(true);
  const [uploadingResume, setUploadingResume] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [downloadingResume, setDownloadingResume] = useState(false);
  const [resumeError, setResumeError] = useState(null);
  const [resumeSuccess, setResumeSuccess] = useState(null);

  // ATS Score State
  const [atsData, setAtsData] = useState(null);
  const [loadingAts, setLoadingAts] = useState(false);

  useEffect(() => {
    loadProfile();
    loadResume();
  }, []);

  useEffect(() => {
    if (resume && profile) {
      loadAtsScore(profile, resume);
    } else {
      setAtsData(null);
    }
  }, [resume, profile]);

  const loadAtsScore = async (prof, res) => {
    try {
      setLoadingAts(true);
      const score = await atsService.calculateGenericAts(prof, res);
      setAtsData(score);
    } catch (e) {
      console.warn('Failed to calculate ATS score:', e);
    } finally {
      setLoadingAts(false);
    }
  };

  const loadResume = async () => {
    try {
      setResumeLoading(true);
      setResumeError(null);
      const data = await resumeService.getMyResume();
      setResume(data);
    } catch (err) {
      console.warn('Failed to load resume:', err.message);
    } finally {
      setResumeLoading(false);
    }
  };

  const loadProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await studentService.getMyProfile();
      setProfile(data);
      populateFormData(data);
    } catch (err) {
      if (err.response?.status === 404) {
        setIsCreating(true);
        setError('No student profile found for your account. Please complete your profile below.');
      } else {
        setError(err.message || 'Failed to load profile. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const populateFormData = (data) => {
    if (!data) return;
    setFormData({
      department: data.department || '',
      course: data.course || '',
      passingYear: data.passingYear ? String(data.passingYear) : '',
      cgpa: data.cgpa !== undefined && data.cgpa !== null ? String(data.cgpa) : '',
      tenthPercentage: data.tenthPercentage !== undefined && data.tenthPercentage !== null ? String(data.tenthPercentage) : '',
      twelfthPercentage: data.twelfthPercentage !== undefined && data.twelfthPercentage !== null ? String(data.twelfthPercentage) : '',
      activeBacklogs: data.activeBacklogs !== undefined && data.activeBacklogs !== null ? String(data.activeBacklogs) : '0',
      totalBacklogs: data.totalBacklogs !== undefined && data.totalBacklogs !== null ? String(data.totalBacklogs) : '0',
      phoneNumber: data.phoneNumber || '',
      skills: data.skills || '',
      linkedinUrl: data.linkedinUrl || '',
      githubUrl: data.githubUrl || '',
      preferredDomain: data.preferredDomain || 'Software Development & Engineering',
      rollNumber: data.rollNumber || '',
      enrollmentNumber: data.enrollmentNumber || '',
    });
  };

  const validateForm = () => {
    const errors = {};

    if (isCreating) {
      if (!formData.rollNumber?.trim()) errors.rollNumber = 'Roll number is required';
      if (!formData.enrollmentNumber?.trim()) errors.enrollmentNumber = 'Enrollment number is required';
    }

    if (!formData.department?.trim()) errors.department = 'Department is required';
    if (!formData.course?.trim()) errors.course = 'Degree/Course is required';

    const year = parseInt(formData.passingYear, 10);
    if (!year || year < 2000 || year > 2040) {
      errors.passingYear = 'Enter a valid passing year (2000 - 2040)';
    }

    const cgpaVal = parseFloat(formData.cgpa);
    if (isNaN(cgpaVal) || cgpaVal < 0 || cgpaVal > 10.0) {
      errors.cgpa = 'CGPA must be between 0.0 and 10.0';
    }

    if (formData.tenthPercentage) {
      const tenth = parseFloat(formData.tenthPercentage);
      if (isNaN(tenth) || tenth < 0 || tenth > 100.0) {
        errors.tenthPercentage = '10th percentage must be between 0.0 and 100.0';
      }
    }

    if (formData.twelfthPercentage) {
      const twelfth = parseFloat(formData.twelfthPercentage);
      if (isNaN(twelfth) || twelfth < 0 || twelfth > 100.0) {
        errors.twelfthPercentage = '12th percentage must be between 0.0 and 100.0';
      }
    }

    const activeB = parseInt(formData.activeBacklogs, 10);
    if (isNaN(activeB) || activeB < 0) {
      errors.activeBacklogs = 'Active backlogs cannot be negative';
    }

    const totalB = parseInt(formData.totalBacklogs, 10);
    if (isNaN(totalB) || totalB < 0) {
      errors.totalBacklogs = 'Total backlogs cannot be negative';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (validationErrors[name]) {
      setValidationErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      setSaving(true);
      setError(null);

      let saved;
      if (isCreating) {
        saved = await studentService.createProfile(formData);
        setIsCreating(false);
      } else {
        saved = await studentService.updateMyProfile(formData);
      }

      setProfile(saved);
      populateFormData(saved);
      setIsEditing(false);
      setSuccessMessage('Student profile updated successfully!');
      setTimeout(() => setSuccessMessage(null), 4000);
    } catch (err) {
      setError(err.message || 'Failed to save student profile.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    populateFormData(profile);
    setValidationErrors({});
    setIsEditing(false);
    if (profile) setIsCreating(false);
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploadingResume(true);
      setUploadProgress(0);
      setResumeError(null);
      setResumeSuccess(null);

      const uploaded = await resumeService.uploadResume(file, (pct) => {
        setUploadProgress(pct);
      });

      setResume(uploaded);
      setResumeSuccess('Resume uploaded and activated successfully!');
      setTimeout(() => setResumeSuccess(null), 4000);
    } catch (err) {
      setResumeError(err.message || 'Failed to upload resume. Please check file format and size.');
    } finally {
      setUploadingResume(false);
      setUploadProgress(0);
      e.target.value = '';
    }
  };

  const handleDownloadResume = async () => {
    if (!resume) return;
    try {
      setDownloadingResume(true);
      setResumeError(null);
      await resumeService.downloadMyResume(resume.originalFileName);
    } catch (err) {
      setResumeError(err.message || 'Failed to download resume file.');
    } finally {
      setDownloadingResume(false);
    }
  };

  const parsedSkills = profile?.skills
    ? profile.skills.split(',').map((s) => s.trim()).filter(Boolean)
    : ['Java', 'Spring Boot', 'React.js', 'MySQL', 'Docker'];

  if (loading) {
    return (
      <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <div style={{ fontSize: '18px', fontWeight: '600', marginBottom: '8px', color: 'var(--text-primary)' }}>
          Loading Student Profile...
        </div>
        <div style={{ fontSize: '14px' }}>Connecting to JECRC Placement Portal...</div>
      </div>
    );
  }

  return (
    <div>
      {/* Header with Title and Action */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: '800', color: '#fff', marginBottom: '6px' }}>
            Student Profile & Academic Records
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Verified academic credentials, placement readiness metrics, and background details.
          </p>
        </div>

        {!isEditing && !isCreating && (
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setIsEditing(true)}
            style={{ gap: '8px' }}
          >
            <Edit2 size={16} />
            <span>Edit Profile</span>
          </button>
        )}
      </div>

      {/* Alerts */}
      {error && (
        <div
          style={{
            padding: '14px 18px',
            marginBottom: '20px',
            borderRadius: 'var(--radius-md)',
            background: 'var(--danger-bg)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            color: '#fca5a5',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            fontSize: '14px',
          }}
        >
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {successMessage && (
        <div
          style={{
            padding: '14px 18px',
            marginBottom: '20px',
            borderRadius: 'var(--radius-md)',
            background: 'var(--success-bg)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            color: '#6ee7b7',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            fontSize: '14px',
          }}
        >
          <CheckCircle size={18} />
          <span>{successMessage}</span>
        </div>
      )}

      {/* Main Grid */}
      <div className="grid-3" style={{ marginBottom: '24px' }}>
        {/* Left Column: Academic & Personal Profile */}
        <div className="card" style={{ gridColumn: 'span 2' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3 style={{ fontSize: '17px', fontWeight: '700', color: '#fff', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <GraduationCap size={20} color="#3b82f6" />
              <span>{isEditing || isCreating ? 'Edit Profile Details' : 'Academic Credentials'}</span>
            </h3>
            {profile?.rollNumber && (
              <span className="badge badge-active" style={{ fontSize: '12px' }}>
                Verified Student
              </span>
            )}
          </div>

          {isEditing || isCreating ? (
            /* Edit Form */
            <form onSubmit={handleSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', marginBottom: '20px' }}>
                {isCreating && (
                  <>
                    <div>
                      <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                        Roll Number *
                      </label>
                      <input
                        type="text"
                        name="rollNumber"
                        className="form-input"
                        value={formData.rollNumber}
                        onChange={handleInputChange}
                        placeholder="e.g. CS2026-042"
                      />
                      {validationErrors.rollNumber && (
                        <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                          {validationErrors.rollNumber}
                        </div>
                      )}
                    </div>

                    <div>
                      <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                        Enrollment Number *
                      </label>
                      <input
                        type="text"
                        name="enrollmentNumber"
                        className="form-input"
                        value={formData.enrollmentNumber}
                        onChange={handleInputChange}
                        placeholder="e.g. EN2022-9876"
                      />
                      {validationErrors.enrollmentNumber && (
                        <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                          {validationErrors.enrollmentNumber}
                        </div>
                      )}
                    </div>
                  </>
                )}

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Department *
                  </label>
                  <input
                    type="text"
                    name="department"
                    className="form-input"
                    value={formData.department}
                    onChange={handleInputChange}
                    placeholder="e.g. Computer Science & Engineering"
                  />
                  {validationErrors.department && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.department}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Degree / Course *
                  </label>
                  <input
                    type="text"
                    name="course"
                    className="form-input"
                    value={formData.course}
                    onChange={handleInputChange}
                    placeholder="e.g. B.Tech"
                  />
                  {validationErrors.course && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.course}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Passing Year *
                  </label>
                  <input
                    type="number"
                    name="passingYear"
                    className="form-input"
                    value={formData.passingYear}
                    onChange={handleInputChange}
                    placeholder="e.g. 2026"
                  />
                  {validationErrors.passingYear && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.passingYear}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Cumulative GPA (CGPA 0.0 - 10.0) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    name="cgpa"
                    className="form-input"
                    value={formData.cgpa}
                    onChange={handleInputChange}
                    placeholder="e.g. 8.85"
                  />
                  {validationErrors.cgpa && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.cgpa}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    10th Percentage (%)
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    name="tenthPercentage"
                    className="form-input"
                    value={formData.tenthPercentage}
                    onChange={handleInputChange}
                    placeholder="e.g. 92.5"
                  />
                  {validationErrors.tenthPercentage && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.tenthPercentage}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    12th / Diploma Percentage (%)
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    name="twelfthPercentage"
                    className="form-input"
                    value={formData.twelfthPercentage}
                    onChange={handleInputChange}
                    placeholder="e.g. 89.0"
                  />
                  {validationErrors.twelfthPercentage && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.twelfthPercentage}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Active Backlogs
                  </label>
                  <input
                    type="number"
                    name="activeBacklogs"
                    className="form-input"
                    value={formData.activeBacklogs}
                    onChange={handleInputChange}
                    min="0"
                  />
                  {validationErrors.activeBacklogs && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.activeBacklogs}
                    </div>
                  )}
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Total Backlogs
                  </label>
                  <input
                    type="number"
                    name="totalBacklogs"
                    className="form-input"
                    value={formData.totalBacklogs}
                    onChange={handleInputChange}
                    min="0"
                  />
                  {validationErrors.totalBacklogs && (
                    <div style={{ color: 'var(--danger)', fontSize: '12px', marginTop: '4px' }}>
                      {validationErrors.totalBacklogs}
                    </div>
                  )}
                </div>

                <div style={{ gridColumn: 'span 2' }}>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Contact Phone Number
                  </label>
                  <input
                    type="text"
                    name="phoneNumber"
                    className="form-input"
                    value={formData.phoneNumber}
                    onChange={handleInputChange}
                    placeholder="e.g. +91 98765 43210"
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Preferred Career Domain
                  </label>
                  <select
                    name="preferredDomain"
                    className="form-input"
                    value={formData.preferredDomain}
                    onChange={handleInputChange}
                  >
                    <option value="Software Development & Engineering">Software Development & Engineering</option>
                    <option value="Full Stack Web Development">Full Stack Web Development</option>
                    <option value="Data Science, AI & Machine Learning">Data Science, AI & Machine Learning</option>
                    <option value="Cloud Architecture & DevOps">Cloud Architecture & DevOps</option>
                    <option value="Cybersecurity & Network Security">Cybersecurity & Network Security</option>
                    <option value="Core Engineering & Embedded Systems">Core Engineering & Embedded Systems</option>
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    LinkedIn Profile URL
                  </label>
                  <input
                    type="url"
                    name="linkedinUrl"
                    className="form-input"
                    value={formData.linkedinUrl}
                    onChange={handleInputChange}
                    placeholder="https://linkedin.com/in/username"
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    GitHub / Portfolio URL
                  </label>
                  <input
                    type="url"
                    name="githubUrl"
                    className="form-input"
                    value={formData.githubUrl}
                    onChange={handleInputChange}
                    placeholder="https://github.com/username"
                  />
                </div>

                <div style={{ gridColumn: 'span 2' }}>
                  <label style={{ display: 'block', fontSize: '13px', fontWeight: '500', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Technical Skills & Proficiencies (Comma-separated)
                  </label>
                  <textarea
                    name="skills"
                    rows="3"
                    className="form-textarea"
                    value={formData.skills}
                    onChange={handleInputChange}
                    placeholder="e.g. Java, Spring Boot, React.js, MySQL, Docker, System Design"
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleCancel}
                  disabled={saving}
                >
                  <X size={16} />
                  <span>Cancel</span>
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={saving}
                >
                  <Save size={16} />
                  <span>{saving ? 'Saving...' : 'Save Profile'}</span>
                </button>
              </div>
            </form>
          ) : (
            /* Read-Only Profile View */
            <>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', marginBottom: '24px' }}>
                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <User size={14} /> Full Name
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginTop: '2px' }}>
                    {profile?.name || user?.name || 'Student Profile'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Mail size={14} /> Institutional Email
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginTop: '2px' }}>
                    {profile?.email || user?.email || 'student@college.edu'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Hash size={14} /> Roll / Registration Number
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginTop: '2px' }}>
                    {profile?.rollNumber || user?.rollNumber || 'CS2026-001'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <BookOpen size={14} /> Enrollment / University ID
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginTop: '2px' }}>
                    {profile?.enrollmentNumber || user?.enrollmentNumber || 'EN2022-001'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Degree & Department</div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginTop: '2px' }}>
                    {profile?.course || user?.course || 'B.Tech'} in {profile?.department || user?.department || 'Computer Science & Engineering'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Calendar size={14} /> Graduating Batch
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#fff', marginTop: '2px' }}>
                    Class of {profile?.passingYear || profile?.graduationYear || user?.graduationYear || 2026}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', borderLeft: '4px solid #10b981' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Cumulative GPA (CGPA)</div>
                  <div style={{ fontSize: '20px', fontWeight: '800', color: '#34d399', marginTop: '2px' }}>
                    {profile?.cgpa !== undefined && profile?.cgpa !== null ? Number(profile.cgpa).toFixed(2) : '8.85'} / 10.0
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)', borderLeft: '4px solid #06b6d4' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Active Backlogs</div>
                  <div style={{ fontSize: '20px', fontWeight: '800', color: '#38bdf8', marginTop: '2px' }}>
                    {profile?.activeBacklogs ?? 0}{' '}
                    <span style={{ fontSize: '13px', fontWeight: '500', color: 'var(--text-muted)' }}>
                      ({profile?.activeBacklogs === 0 ? 'Clean Record' : 'Attention Required'})
                    </span>
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>10th Standard Percentage</div>
                  <div style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginTop: '2px' }}>
                    {profile?.tenthPercentage !== undefined && profile?.tenthPercentage !== null ? `${profile.tenthPercentage}%` : 'N/A'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>12th / Diploma Percentage</div>
                  <div style={{ fontSize: '16px', fontWeight: '700', color: '#fff', marginTop: '2px' }}>
                    {profile?.twelfthPercentage !== undefined && profile?.twelfthPercentage !== null ? `${profile.twelfthPercentage}%` : 'N/A'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Phone size={14} /> Contact Phone Number
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: 'var(--text-primary)', marginTop: '2px' }}>
                    {profile?.phoneNumber || '+91 98765 43210'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Briefcase size={14} color="#6366f1" /> Preferred Career Domain
                  </div>
                  <div style={{ fontSize: '15px', fontWeight: '600', color: '#818cf8', marginTop: '2px' }}>
                    {profile?.preferredDomain || user?.preferredDomain || 'Software Development & Engineering'}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Linkedin size={14} color="#0a66c2" /> LinkedIn Profile
                  </div>
                  <div style={{ fontSize: '14px', marginTop: '4px' }}>
                    {profile?.linkedinUrl || user?.linkedinUrl ? (
                      <a
                        href={profile?.linkedinUrl || user?.linkedinUrl}
                        target="_blank"
                        rel="noreferrer"
                        style={{ color: '#38bdf8', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '4px', fontWeight: '500' }}
                      >
                        <span>View LinkedIn</span>
                        <ExternalLink size={12} />
                      </a>
                    ) : (
                      <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Not linked yet</span>
                    )}
                  </div>
                </div>

                <div style={{ padding: '14px', background: 'var(--bg-main)', borderRadius: 'var(--radius-md)' }}>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Github size={14} /> GitHub / Portfolio
                  </div>
                  <div style={{ fontSize: '14px', marginTop: '4px' }}>
                    {profile?.githubUrl || user?.githubUrl ? (
                      <a
                        href={profile?.githubUrl || user?.githubUrl}
                        target="_blank"
                        rel="noreferrer"
                        style={{ color: '#a78bfa', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '4px', fontWeight: '500' }}
                      >
                        <span>View Portfolio</span>
                        <ExternalLink size={12} />
                      </a>
                    ) : (
                      <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Not linked yet</span>
                    )}
                  </div>
                </div>
              </div>

              {/* Skills */}
              <div>
                <h4 style={{ fontSize: '14px', fontWeight: '700', color: '#fff', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Sparkles size={16} color="#f59e0b" />
                  <span>Technical Skills & Proficiencies</span>
                </h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {parsedSkills.map((skill, idx) => (
                    <span
                      key={idx}
                      style={{
                        padding: '6px 14px',
                        borderRadius: 'var(--radius-full)',
                        background: 'var(--bg-subtle)',
                        border: '1px solid var(--border-subtle)',
                        color: '#e2e8f0',
                        fontSize: '13px',
                        fontWeight: '500',
                      }}
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>

        {/* Right Column: Resume Card (Module 2: Resume Management) */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <h3 style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-primary)', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FileCheck size={18} color="#10b981" />
              <span>Placement Resume</span>
            </h3>

            {resumeLoading ? (
              <div style={{ padding: '28px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                <Loader2 size={24} style={{ margin: '0 auto 8px', animation: 'spin 1s linear infinite' }} />
                <div style={{ fontSize: '12px' }}>Loading resume...</div>
              </div>
            ) : resume ? (
              <div
                style={{
                  padding: '16px',
                  background: 'var(--bg-main)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-subtle)',
                  marginBottom: '16px',
                  textAlign: 'center',
                }}
              >
                <FileText size={36} color="#3b82f6" style={{ margin: '0 auto 8px' }} />
                <div style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', wordBreak: 'break-all' }}>
                  {resume.originalFileName}
                </div>
                {resume.formattedSize && (
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                    Size: {resume.formattedSize}
                    {resume.uploadedAt && ` • ${new Date(resume.uploadedAt).toLocaleDateString()}`}
                  </div>
                )}
                <div style={{ fontSize: '12px', color: 'var(--success)', marginTop: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px' }}>
                  <CheckCircle size={14} /> Active Placement Resume
                </div>

                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleDownloadResume}
                  disabled={downloadingResume}
                  style={{
                    width: '100%',
                    marginTop: '12px',
                    fontSize: '12px',
                    padding: '7px 12px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '6px',
                  }}
                >
                  {downloadingResume ? (
                    <>
                      <Loader2 size={13} style={{ animation: 'spin 1s linear infinite' }} />
                      <span>Downloading...</span>
                    </>
                  ) : (
                    <>
                      <Download size={13} />
                      <span>Download Active Resume</span>
                    </>
                  )}
                </button>

                {/* Generic ATS Score Section */}
                <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid var(--border-subtle)', textAlign: 'left' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Sparkles size={14} color="#6366f1" /> Resume ATS Score
                    </span>
                    {atsData && (
                      <span
                        style={{
                          fontSize: '11px',
                          fontWeight: '700',
                          padding: '2px 8px',
                          borderRadius: '6px',
                          background: atsData.atsScore >= 80 ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                          color: atsData.atsScore >= 80 ? '#10b981' : '#f59e0b',
                        }}
                      >
                        {atsData.scoreBand}
                      </span>
                    )}
                  </div>

                  {loadingAts ? (
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 0' }}>
                      <Loader2 size={13} style={{ animation: 'spin 1s linear infinite' }} />
                      <span>Calculating ATS match score...</span>
                    </div>
                  ) : atsData ? (
                    <div>
                      <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginBottom: '6px' }}>
                        <span style={{ fontSize: '26px', fontWeight: '800', color: atsData.atsScore >= 80 ? '#10b981' : '#6366f1' }}>
                          {atsData.atsScore}
                        </span>
                        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>/ 100 Overall Score</span>
                      </div>

                      <div style={{ height: '6px', background: 'var(--border-subtle)', borderRadius: '4px', overflow: 'hidden', marginBottom: '12px' }}>
                        <div
                          style={{
                            height: '100%',
                            width: `${atsData.atsScore}%`,
                            background: atsData.atsScore >= 80 ? '#10b981' : '#6366f1',
                            borderRadius: '4px',
                            transition: 'width 0.4s ease',
                          }}
                        />
                      </div>

                      {/* Matched Keywords */}
                      {atsData.matchedKeywords && atsData.matchedKeywords.length > 0 ? (
                        <div style={{ marginBottom: '10px' }}>
                          <span style={{ fontSize: '11px', fontWeight: '600', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
                            Detected ATS Keywords ({atsData.matchedKeywords.length}):
                          </span>
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                            {atsData.matchedKeywords.slice(0, 8).map((kw, i) => (
                              <span
                                key={i}
                                style={{
                                  fontSize: '10.5px',
                                  padding: '1px 6px',
                                  borderRadius: '4px',
                                  background: 'rgba(99, 102, 241, 0.1)',
                                  color: '#818cf8',
                                  fontWeight: '600',
                                }}
                              >
                                {kw}
                              </span>
                            ))}
                          </div>
                        </div>
                      ) : (
                        <div style={{ marginBottom: '10px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
                          No core technical keywords detected in the uploaded resume.
                        </div>
                      )}

                      {/* Top Actionable Improvements */}
                      {atsData.topImprovements && atsData.topImprovements.length > 0 && (
                        <div>
                          <span style={{ fontSize: '11px', fontWeight: '600', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
                            Top ATS Recommendation:
                          </span>
                          <div style={{ fontSize: '11.5px', color: 'var(--text-secondary)', lineHeight: 1.4, background: 'var(--bg-subtle)', padding: '6px 8px', borderRadius: '6px' }}>
                            💡 {atsData.topImprovements[0]}
                          </div>
                        </div>
                      )}
                    </div>
                  ) : null}
                </div>
              </div>
            ) : (
              <div
                style={{
                  padding: '24px 14px',
                  background: 'var(--bg-main)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px dashed var(--border-subtle)',
                  marginBottom: '16px',
                  textAlign: 'center',
                }}
              >
                <FileText size={36} color="var(--text-muted)" style={{ margin: '0 auto 8px' }} />
                <div style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-secondary)' }}>
                  No Resume Uploaded
                </div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Upload your resume to be eligible for placement drives and company shortlists.
                </div>
              </div>
            )}

            {/* Error & Success Feedback Alerts */}
            {resumeSuccess && (
              <div
                style={{
                  padding: '8px 12px',
                  background: 'rgba(16, 185, 129, 0.1)',
                  border: '1px solid rgba(16, 185, 129, 0.3)',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '12px',
                  color: '#34d399',
                  textAlign: 'center',
                  marginBottom: '12px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                }}
              >
                <CheckCircle size={13} />
                <span>{resumeSuccess}</span>
              </div>
            )}

            {resumeError && (
              <div
                style={{
                  padding: '8px 12px',
                  background: 'rgba(239, 68, 68, 0.1)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '12px',
                  color: '#f87171',
                  textAlign: 'center',
                  marginBottom: '12px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                }}
              >
                <AlertCircle size={13} />
                <span>{resumeError}</span>
              </div>
            )}

            {uploadingResume && uploadProgress > 0 && (
              <div style={{ marginBottom: '12px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                  <span>Uploading resume...</span>
                  <span>{uploadProgress}%</span>
                </div>
                <div style={{ height: '4px', background: 'var(--border-subtle)', borderRadius: '2px', overflow: 'hidden' }}>
                  <div
                    style={{
                      width: `${uploadProgress}%`,
                      height: '100%',
                      background: 'var(--primary)',
                      transition: 'width 0.2s ease',
                    }}
                  />
                </div>
              </div>
            )}
          </div>

          <div>
            <label
              className={`btn btn-primary ${uploadingResume ? 'disabled' : ''}`}
              style={{
                width: '100%',
                cursor: uploadingResume ? 'not-allowed' : 'pointer',
                gap: '8px',
                opacity: uploadingResume ? 0.7 : 1,
              }}
            >
              {uploadingResume ? (
                <>
                  <Loader2 size={15} style={{ animation: 'spin 1s linear infinite' }} />
                  <span>Uploading Resume...</span>
                </>
              ) : (
                <>
                  <Upload size={15} />
                  <span>{resume ? 'Replace Resume (PDF / DOCX)' : 'Upload Resume (PDF / DOCX)'}</span>
                </>
              )}
              <input
                type="file"
                accept=".pdf,.docx"
                disabled={uploadingResume}
                onChange={handleFileUpload}
                style={{ display: 'none' }}
              />
            </label>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', textAlign: 'center', marginTop: '8px' }}>
              Accepted formats: PDF, DOCX (Max 5MB)
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
