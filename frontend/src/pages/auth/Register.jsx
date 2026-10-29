import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  GraduationCap,
  User,
  Mail,
  MapPin,
  Calendar,
  BookOpen,
  Award,
  Lock,
  Phone,
  Hash,
  Sparkles,
  CheckCircle2,
  Building,
  Briefcase,
  AlertCircle,
} from 'lucide-react';
import ThemeToggle from '../../components/common/ThemeToggle';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  // Selected Role
  const [role, setRole] = useState('ROLE_STUDENT');

  // Form State
  const [formData, setFormData] = useState({
    // Basic & Contact
    name: '',
    email: '',
    phoneNumber: '',
    address: '',
    // Academic Details (for Student)
    rollNumber: '',
    enrollmentNumber: '',
    department: 'Computer Science & Engineering',
    course: 'B.Tech',
    studentYear: '4th Year',
    currentSemester: '7',
    graduationYear: '2026',
    cgpa: '8.5',
    activeBacklogs: '0',
    skills: 'Java, Python, Data Structures, React, SQL',
    // Professional details (for Faculty / Officer / HOD)
    designation: '',
    employeeId: '',
    companyName: '',
    // Security
    password: '',
    confirmPassword: '',
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validations
    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match. Please re-enter.');
      return;
    }

    if (role === 'ROLE_STUDENT') {
      if (!formData.rollNumber.trim()) {
        setError('College Roll Number is required for student registration.');
        return;
      }
      if (!formData.address.trim()) {
        setError('Residential Address is required.');
        return;
      }
      if (!formData.studentYear) {
        setError('Please select your current student year.');
        return;
      }
      if (!formData.currentSemester) {
        setError('Please select your current semester.');
        return;
      }
    }

    setLoading(true);

    try {
      const payload = {
        name: formData.name.trim(),
        email: formData.email.trim().toLowerCase(),
        password: formData.password,
        role,
        phoneNumber: formData.phoneNumber.trim(),
        address: formData.address.trim(),
        // Student details
        rollNumber: formData.rollNumber.trim(),
        enrollmentNumber: formData.enrollmentNumber.trim() || undefined,
        department: formData.department,
        course: formData.course,
        studentYear: formData.studentYear,
        currentSemester: Number(formData.currentSemester) || 7,
        graduationYear: Number(formData.graduationYear) || 2026,
        cgpa: formData.cgpa ? parseFloat(formData.cgpa) : 8.0,
        activeBacklogs: formData.activeBacklogs ? parseInt(formData.activeBacklogs, 10) : 0,
        skills: formData.skills,
        // Staff details
        designation: formData.designation,
        employeeId: formData.employeeId,
      };

      await register(payload);
      navigate('/login?registered=true');
    } catch (err) {
      setError(err.message || 'Registration failed. Please check details.');
    } finally {
      setLoading(false);
    }
  };

  const isStudent = role === 'ROLE_STUDENT';

  return (
    <div style={styles.container}>
      {/* Background ambient soothing glow */}
      <div style={styles.ambientGlowTop} />
      <div style={styles.ambientGlowBottom} />

      <div style={styles.themeToggleContainer}>
        <ThemeToggle showLabel />
      </div>

      <div style={styles.wrapper}>
        {/* Header Branding */}
        <div style={styles.header}>
          <div style={styles.logoBadge}>
            <GraduationCap size={28} color="#6366f1" />
          </div>
          <h1 style={styles.title}>Campus Placement Portal</h1>
          <p style={styles.subtitle}>
            Create your account to unlock eligible drives, online tests, and placement tracking.
          </p>
        </div>

        {/* Card */}
        <div style={styles.card}>
          {error && (
            <div style={styles.errorBox}>
              <AlertCircle size={16} style={{ flexShrink: 0 }} />
              <span>{error}</span>
            </div>
          )}

          {/* Role Pill Switcher */}
          <div style={styles.roleContainer}>
            <label style={styles.sectionLabel}>Select Your Account Role</label>
            <div style={styles.roleGrid}>
              {[
                { id: 'ROLE_STUDENT', label: 'Student', icon: GraduationCap },
                { id: 'ROLE_FACULTY', label: 'Faculty', icon: User },
                { id: 'ROLE_HOD', label: 'HOD', icon: Award },
                { id: 'ROLE_PLACEMENT_OFFICER', label: 'TPO Officer', icon: Briefcase },
              ].map((r) => {
                const Icon = r.icon;
                const isSelected = role === r.id;
                return (
                  <button
                    key={r.id}
                    type="button"
                    onClick={() => setRole(r.id)}
                    style={{
                      ...styles.roleBtn,
                      ...(isSelected ? styles.roleBtnActive : {}),
                    }}
                  >
                    <Icon size={16} color={isSelected ? '#6366f1' : 'var(--text-muted)'} />
                    <span style={{ fontWeight: isSelected ? '700' : '500' }}>{r.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <form onSubmit={handleSubmit} style={styles.form}>
            {/* Section 1: Personal & Contact Information */}
            <div style={styles.sectionCard}>
              <div style={styles.sectionHeader}>
                <div style={styles.sectionIcon}>
                  <User size={16} color="#6366f1" />
                </div>
                <div>
                  <h3 style={styles.sectionTitle}>1. Personal & Contact Details</h3>
                  <p style={styles.sectionSub}>Required communication and identity details</p>
                </div>
              </div>

              <div style={styles.formGrid}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Full Name <span style={styles.required}>*</span>
                  </label>
                  <div style={styles.inputWrapper}>
                    <User size={15} style={styles.inputIcon} />
                    <input
                      type="text"
                      name="name"
                      required
                      value={formData.name}
                      onChange={handleChange}
                      placeholder="e.g. Aarav Sharma"
                      style={styles.inputWithIcon}
                    />
                  </div>
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Gmail / Email Address <span style={styles.required}>*</span>
                  </label>
                  <div style={styles.inputWrapper}>
                    <Mail size={15} style={styles.inputIcon} />
                    <input
                      type="email"
                      name="email"
                      required
                      value={formData.email}
                      onChange={handleChange}
                      placeholder="aarav.jecrc@gmail.com"
                      style={styles.inputWithIcon}
                    />
                  </div>
                  <span style={styles.helperText}>Used for campus drive invites and notifications</span>
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Mobile / Phone Number <span style={styles.required}>*</span>
                  </label>
                  <div style={styles.inputWrapper}>
                    <Phone size={15} style={styles.inputIcon} />
                    <input
                      type="tel"
                      name="phoneNumber"
                      required
                      value={formData.phoneNumber}
                      onChange={handleChange}
                      placeholder="+91 98765 43210"
                      style={styles.inputWithIcon}
                    />
                  </div>
                </div>

                <div style={{ ...styles.formGroup, gridColumn: 'span 2' }}>
                  <label style={styles.label}>
                    Complete Residential Address <span style={styles.required}>*</span>
                  </label>
                  <div style={styles.inputWrapper}>
                    <MapPin size={15} style={{ ...styles.inputIcon, top: '16px' }} />
                    <textarea
                      name="address"
                      required
                      rows={2}
                      value={formData.address}
                      onChange={handleChange}
                      placeholder="Street, Apartment/Hostel, City, State, PIN Code"
                      style={styles.textareaWithIcon}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* Section 2: Complete Academic Information (If Student) */}
            {isStudent && (
              <div style={styles.sectionCard}>
                <div style={styles.sectionHeader}>
                  <div style={styles.sectionIcon}>
                    <GraduationCap size={16} color="#38bdf8" />
                  </div>
                  <div>
                    <h3 style={styles.sectionTitle}>2. Academic Credentials</h3>
                    <p style={styles.sectionSub}>College enrollment, department, semester & eligibility records</p>
                  </div>
                </div>

                <div style={styles.formGrid}>
                  <div style={styles.formGroup}>
                    <label style={styles.label}>
                      College Roll Number <span style={styles.required}>*</span>
                    </label>
                    <div style={styles.inputWrapper}>
                      <Hash size={15} style={styles.inputIcon} />
                      <input
                        type="text"
                        name="rollNumber"
                        required
                        value={formData.rollNumber}
                        onChange={handleChange}
                        placeholder="e.g. 22EJCCS042"
                        style={styles.inputWithIcon}
                      />
                    </div>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>University Enrollment No.</label>
                    <div style={styles.inputWrapper}>
                      <Hash size={15} style={styles.inputIcon} />
                      <input
                        type="text"
                        name="enrollmentNumber"
                        value={formData.enrollmentNumber}
                        onChange={handleChange}
                        placeholder="e.g. JECRC/2022/1042"
                        style={styles.inputWithIcon}
                      />
                    </div>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>
                      Department / Branch <span style={styles.required}>*</span>
                    </label>
                    <select
                      name="department"
                      value={formData.department}
                      onChange={handleChange}
                      style={styles.select}
                    >
                      <option value="Computer Science & Engineering">Computer Science & Engineering (CSE)</option>
                      <option value="Information Technology">Information Technology (IT)</option>
                      <option value="Artificial Intelligence & Data Science">AI & Data Science (AI-DS)</option>
                      <option value="Electronics & Communication">Electronics & Comm. (ECE)</option>
                      <option value="Electrical Engineering">Electrical Engineering (EE)</option>
                      <option value="Mechanical Engineering">Mechanical Engineering (ME)</option>
                      <option value="Civil Engineering">Civil Engineering (CE)</option>
                    </select>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>Course / Degree</label>
                    <select
                      name="course"
                      value={formData.course}
                      onChange={handleChange}
                      style={styles.select}
                    >
                      <option value="B.Tech">B.Tech (Bachelor of Technology)</option>
                      <option value="M.Tech">M.Tech (Master of Technology)</option>
                      <option value="BCA">BCA (Bachelor of Computer App)</option>
                      <option value="MCA">MCA (Master of Computer App)</option>
                    </select>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>
                      Student Year <span style={styles.required}>*</span>
                    </label>
                    <select
                      name="studentYear"
                      value={formData.studentYear}
                      onChange={handleChange}
                      style={styles.select}
                    >
                      <option value="1st Year">1st Year (Freshman)</option>
                      <option value="2nd Year">2nd Year (Sophomore)</option>
                      <option value="3rd Year">3rd Year (Pre-Final)</option>
                      <option value="4th Year">4th Year (Final Year)</option>
                    </select>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>
                      Current Semester <span style={styles.required}>*</span>
                    </label>
                    <select
                      name="currentSemester"
                      value={formData.currentSemester}
                      onChange={handleChange}
                      style={styles.select}
                    >
                      <option value="1">Semester 1</option>
                      <option value="2">Semester 2</option>
                      <option value="3">Semester 3</option>
                      <option value="4">Semester 4</option>
                      <option value="5">Semester 5</option>
                      <option value="6">Semester 6</option>
                      <option value="7">Semester 7</option>
                      <option value="8">Semester 8</option>
                    </select>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>
                      Current CGPA <span style={styles.required}>*</span>
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      max="10"
                      name="cgpa"
                      required
                      value={formData.cgpa}
                      onChange={handleChange}
                      placeholder="e.g. 8.75"
                      style={styles.input}
                    />
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>
                      Active Backlogs <span style={styles.required}>*</span>
                    </label>
                    <input
                      type="number"
                      min="0"
                      name="activeBacklogs"
                      required
                      value={formData.activeBacklogs}
                      onChange={handleChange}
                      placeholder="0"
                      style={styles.input}
                    />
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>Passing / Graduation Year</label>
                    <select
                      name="graduationYear"
                      value={formData.graduationYear}
                      onChange={handleChange}
                      style={styles.select}
                    >
                      <option value="2025">2025</option>
                      <option value="2026">2026</option>
                      <option value="2027">2027</option>
                      <option value="2028">2028</option>
                    </select>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>Key Technical Skills</label>
                    <input
                      type="text"
                      name="skills"
                      value={formData.skills}
                      onChange={handleChange}
                      placeholder="Java, Python, DSA, React, SQL"
                      style={styles.input}
                    />
                  </div>
                </div>
              </div>
            )}

            {/* If Staff / Faculty Role */}
            {!isStudent && (
              <div style={styles.sectionCard}>
                <div style={styles.sectionHeader}>
                  <div style={styles.sectionIcon}>
                    <Building size={16} color="#8b5cf6" />
                  </div>
                  <div>
                    <h3 style={styles.sectionTitle}>2. Institutional Details</h3>
                    <p style={styles.sectionSub}>Department affiliation and employee verification</p>
                  </div>
                </div>

                <div style={styles.formGrid}>
                  <div style={styles.formGroup}>
                    <label style={styles.label}>Department</label>
                    <select
                      name="department"
                      value={formData.department}
                      onChange={handleChange}
                      style={styles.select}
                    >
                      <option value="Computer Science & Engineering">Computer Science & Engineering</option>
                      <option value="Information Technology">Information Technology</option>
                      <option value="Central Placement Cell">Central Placement Cell</option>
                      <option value="Electronics & Communication">Electronics & Communication</option>
                    </select>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>Designation</label>
                    <input
                      type="text"
                      name="designation"
                      value={formData.designation}
                      onChange={handleChange}
                      placeholder="e.g. Associate Professor / Officer"
                      style={styles.input}
                    />
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>Employee / Staff ID</label>
                    <input
                      type="text"
                      name="employeeId"
                      value={formData.employeeId}
                      onChange={handleChange}
                      placeholder="e.g. EMP-2024-88"
                      style={styles.input}
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Section 3: Password & Account Security */}
            <div style={styles.sectionCard}>
              <div style={styles.sectionHeader}>
                <div style={styles.sectionIcon}>
                  <Lock size={16} color="#10b981" />
                </div>
                <div>
                  <h3 style={styles.sectionTitle}>3. Account Security</h3>
                  <p style={styles.sectionSub}>Create a secure password for portal access</p>
                </div>
              </div>

              <div style={styles.formGrid}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Password <span style={styles.required}>*</span>
                  </label>
                  <div style={styles.inputWrapper}>
                    <Lock size={15} style={styles.inputIcon} />
                    <input
                      type="password"
                      name="password"
                      required
                      minLength={6}
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="At least 6 characters"
                      style={styles.inputWithIcon}
                    />
                  </div>
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Confirm Password <span style={styles.required}>*</span>
                  </label>
                  <div style={styles.inputWrapper}>
                    <Lock size={15} style={styles.inputIcon} />
                    <input
                      type="password"
                      name="confirmPassword"
                      required
                      minLength={6}
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      placeholder="Re-enter password"
                      style={styles.inputWithIcon}
                    />
                  </div>
                  {formData.confirmPassword && (
                    <span
                      style={{
                        fontSize: '11px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        marginTop: '4px',
                        color:
                          formData.password === formData.confirmPassword
                            ? '#10b981'
                            : '#f43f5e',
                      }}
                    >
                      <CheckCircle2 size={12} />
                      {formData.password === formData.confirmPassword
                        ? 'Passwords match'
                        : 'Passwords do not match'}
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Submit button */}
            <button type="submit" disabled={loading} style={styles.submitBtn}>
              {loading ? (
                <span>Registering your account...</span>
              ) : (
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                  <Sparkles size={16} />
                  Complete {isStudent ? 'Student' : ''} Registration
                </span>
              )}
            </button>
          </form>

          <p style={styles.footer}>
            Already have an account?{' '}
            <Link to="/login" style={styles.link}>
              Sign in here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--bg-main)',
    color: 'var(--text-primary)',
    fontFamily: 'var(--font-sans)',
    padding: '40px 20px',
    position: 'relative',
    overflow: 'hidden',
  },
  ambientGlowTop: {
    position: 'absolute',
    top: '-15%',
    left: '20%',
    width: '550px',
    height: '550px',
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(99, 102, 241, 0.12) 0%, transparent 70%)',
    pointerEvents: 'none',
    zIndex: 0,
  },
  ambientGlowBottom: {
    position: 'absolute',
    bottom: '-15%',
    right: '15%',
    width: '600px',
    height: '600px',
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(56, 189, 248, 0.08) 0%, transparent 70%)',
    pointerEvents: 'none',
    zIndex: 0,
  },
  themeToggleContainer: {
    position: 'absolute',
    top: '20px',
    right: '24px',
    zIndex: 20,
  },
  wrapper: {
    width: '100%',
    maxWidth: '780px',
    position: 'relative',
    zIndex: 1,
  },
  header: {
    textAlign: 'center',
    marginBottom: '28px',
  },
  logoBadge: {
    width: '56px',
    height: '56px',
    margin: '0 auto 16px',
    borderRadius: '16px',
    background: 'rgba(99, 102, 241, 0.12)',
    border: '1px solid rgba(99, 102, 241, 0.25)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    boxShadow: '0 8px 24px -4px rgba(99, 102, 241, 0.25)',
  },
  title: {
    fontSize: '26px',
    fontWeight: '800',
    color: 'var(--text-primary)',
    letterSpacing: '-0.02em',
    marginBottom: '8px',
  },
  subtitle: {
    fontSize: '14px',
    color: 'var(--text-secondary)',
    maxWidth: '520px',
    margin: '0 auto',
    lineHeight: 1.5,
  },
  card: {
    background: 'var(--bg-card)',
    borderRadius: '20px',
    padding: '36px',
    border: '1px solid var(--border-subtle)',
    boxShadow: 'var(--shadow-lg)',
    backdropFilter: 'blur(16px)',
  },
  errorBox: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '12px 16px',
    borderRadius: '12px',
    background: 'var(--danger-bg)',
    border: '1px solid rgba(244, 63, 94, 0.3)',
    color: 'var(--danger)',
    fontSize: '13px',
    marginBottom: '20px',
  },
  roleContainer: {
    marginBottom: '24px',
  },
  sectionLabel: {
    display: 'block',
    fontSize: '12px',
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
    color: 'var(--text-muted)',
    marginBottom: '10px',
  },
  roleGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
    gap: '10px',
  },
  roleBtn: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    padding: '10px 14px',
    borderRadius: '12px',
    border: '1px solid var(--border-subtle)',
    background: 'var(--bg-subtle)',
    color: 'var(--text-secondary)',
    cursor: 'pointer',
    fontSize: '13px',
    transition: 'all 0.2s ease',
  },
  roleBtnActive: {
    background: 'rgba(99, 102, 241, 0.12)',
    borderColor: '#6366f1',
    color: '#6366f1',
    boxShadow: '0 0 16px -2px rgba(99, 102, 241, 0.25)',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '22px',
  },
  sectionCard: {
    background: 'rgba(255, 255, 255, 0.015)',
    border: '1px solid var(--border-subtle)',
    borderRadius: '16px',
    padding: '20px',
  },
  sectionHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    marginBottom: '18px',
  },
  sectionIcon: {
    width: '32px',
    height: '32px',
    borderRadius: '10px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sectionTitle: {
    fontSize: '15px',
    fontWeight: '700',
    color: 'var(--text-primary)',
    margin: 0,
  },
  sectionSub: {
    fontSize: '12px',
    color: 'var(--text-muted)',
    margin: 0,
  },
  formGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: '16px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '12.5px',
    fontWeight: '600',
    color: 'var(--text-secondary)',
  },
  required: {
    color: '#f43f5e',
  },
  inputWrapper: {
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
  },
  inputIcon: {
    position: 'absolute',
    left: '12px',
    color: 'var(--text-muted)',
    pointerEvents: 'none',
  },
  inputWithIcon: {
    width: '100%',
    padding: '10px 14px 10px 36px',
    borderRadius: '10px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    fontSize: '13.5px',
    fontFamily: 'inherit',
    outline: 'none',
    transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
  },
  textareaWithIcon: {
    width: '100%',
    padding: '10px 14px 10px 36px',
    borderRadius: '10px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    fontSize: '13.5px',
    fontFamily: 'inherit',
    outline: 'none',
    resize: 'vertical',
    transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
  },
  input: {
    width: '100%',
    padding: '10px 14px',
    borderRadius: '10px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    fontSize: '13.5px',
    fontFamily: 'inherit',
    outline: 'none',
    transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
  },
  select: {
    width: '100%',
    padding: '10px 14px',
    borderRadius: '10px',
    background: 'var(--bg-subtle)',
    border: '1px solid var(--border-subtle)',
    color: 'var(--text-primary)',
    fontSize: '13.5px',
    fontFamily: 'inherit',
    outline: 'none',
    cursor: 'pointer',
    transition: 'border-color 0.2s ease',
  },
  helperText: {
    fontSize: '11px',
    color: 'var(--text-muted)',
  },
  submitBtn: {
    padding: '14px 20px',
    borderRadius: '12px',
    background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
    border: 'none',
    color: '#ffffff',
    fontSize: '14px',
    fontWeight: '700',
    cursor: 'pointer',
    marginTop: '8px',
    boxShadow: '0 8px 24px -4px rgba(99, 102, 241, 0.4)',
    transition: 'transform 0.15s ease, box-shadow 0.15s ease',
  },
  footer: {
    marginTop: '24px',
    fontSize: '13px',
    textAlign: 'center',
    color: 'var(--text-secondary)',
  },
  link: {
    color: '#6366f1',
    textDecoration: 'none',
    fontWeight: '600',
  },
};
