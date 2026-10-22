import React, { useState, useEffect } from 'react';
import {
  getCompanies,
  getPlacementDrives,
  getDrivesByCompany,
  getDrivesByStatus,
  searchPlacementDrives,
  createPlacementDrive,
  updatePlacementDrive,
  deletePlacementDrive,
} from '../../services/placementService';

const STATUS_OPTIONS = ['UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'];
const EMPLOYMENT_TYPES = ['FULL_TIME', 'INTERNSHIP', 'PART_TIME', 'CONTRACT'];

export default function Drives() {
  const [drives, setDrives] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [companiesLoading, setCompaniesLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [companyFilter, setCompanyFilter] = useState('ALL');

  // Modal & Form state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDrive, setEditingDrive] = useState(null);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [modalError, setModalError] = useState('');

  const initialFormData = {
    companyId: '',
    jobTitle: '',
    jobDescription: '',
    jobLocation: '',
    salaryPackage: '',
    employmentType: 'FULL_TIME',
    applicationStartDate: '',
    applicationEndDate: '',
    driveDate: '',
    vacancies: '',
    status: 'UPCOMING',
    minimumCgpa: '0.0',
    maximumAllowedBacklogs: '0',
    eligibleBranches: '',
  };

  const [formData, setFormData] = useState(initialFormData);
  const [formErrors, setFormErrors] = useState({});

  // ---------------------------------------------------------------------------
  // Data Fetching
  // ---------------------------------------------------------------------------

  const loadCompanies = async () => {
    setCompaniesLoading(true);
    try {
      // Load active companies for drive assignment and filtering
      const data = await getCompanies(true);
      setCompanies(data || []);
    } catch (err) {
      // Non-fatal if viewing drives, but will prevent creating/editing
      console.error('Failed to load active companies', err);
    } finally {
      setCompaniesLoading(false);
    }
  };

  const fetchDrives = async () => {
    setLoading(true);
    setError('');
    try {
      let data;
      const hasSearch = searchTerm.trim().length > 0;
      const hasStatus = statusFilter !== 'ALL';
      const hasCompany = companyFilter !== 'ALL';

      if (hasSearch) {
        data = await searchPlacementDrives(searchTerm.trim());
      } else if (hasStatus && !hasCompany) {
        data = await getDrivesByStatus(statusFilter);
      } else if (hasCompany && !hasStatus) {
        data = await getDrivesByCompany(companyFilter);
      } else {
        data = await getPlacementDrives();
      }

      // If combined filters are active, apply them in-memory
      let filtered = data || [];
      if (hasStatus) {
        filtered = filtered.filter((d) => d.status === statusFilter);
      }
      if (hasCompany) {
        filtered = filtered.filter(
          (d) => String(d.company?.id) === String(companyFilter)
        );
      }

      setDrives(filtered);
    } catch (err) {
      setError(err.message || 'Failed to load placement drives.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCompanies();
    fetchDrives();
  }, [statusFilter, companyFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchDrives();
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setStatusFilter('ALL');
    setCompanyFilter('ALL');
    setError('');
    setLoading(true);
    getPlacementDrives()
      .then((data) => setDrives(data || []))
      .catch((err) => setError(err.message || 'Failed to load placement drives.'))
      .finally(() => setLoading(false));
  };

  // ---------------------------------------------------------------------------
  // Modal Handlers & Form Validation
  // ---------------------------------------------------------------------------

  const openCreateModal = () => {
    setEditingDrive(null);
    setFormData(initialFormData);
    setFormErrors({});
    setModalError('');
    setIsModalOpen(true);
  };

  const openEditModal = (drive) => {
    setEditingDrive(drive);
    setFormData({
      companyId: drive.company?.id ? String(drive.company.id) : '',
      jobTitle: drive.jobTitle || '',
      jobDescription: drive.jobDescription || '',
      jobLocation: drive.jobLocation || '',
      salaryPackage: drive.salaryPackage !== undefined ? String(drive.salaryPackage) : '',
      employmentType: drive.employmentType || 'FULL_TIME',
      applicationStartDate: drive.applicationStartDate || '',
      applicationEndDate: drive.applicationEndDate || '',
      driveDate: drive.driveDate || '',
      vacancies: drive.vacancies !== undefined ? String(drive.vacancies) : '',
      status: drive.status || 'UPCOMING',
      minimumCgpa: drive.minimumCgpa !== undefined && drive.minimumCgpa !== null ? String(drive.minimumCgpa) : '0.0',
      maximumAllowedBacklogs: drive.maximumAllowedBacklogs !== undefined && drive.maximumAllowedBacklogs !== null ? String(drive.maximumAllowedBacklogs) : '0',
      eligibleBranches: drive.eligibleBranches || '',
    });
    setFormErrors({});
    setModalError('');
    setIsModalOpen(true);
  };

  const closeModal = () => {
    if (!saving) {
      setIsModalOpen(false);
      setEditingDrive(null);
      setFormData(initialFormData);
      setFormErrors({});
      setModalError('');
    }
  };

  const validateForm = () => {
    const errors = {};

    if (!formData.companyId) {
      errors.companyId = 'Please select a company';
    }

    if (!formData.jobTitle.trim()) {
      errors.jobTitle = 'Job title is required';
    } else if (formData.jobTitle.length > 150) {
      errors.jobTitle = 'Job title must not exceed 150 characters';
    }

    if (!formData.jobDescription.trim()) {
      errors.jobDescription = 'Job description is required';
    } else if (formData.jobDescription.length > 2000) {
      errors.jobDescription = 'Job description must not exceed 2000 characters';
    }

    if (!formData.jobLocation.trim()) {
      errors.jobLocation = 'Job location is required';
    } else if (formData.jobLocation.length > 150) {
      errors.jobLocation = 'Job location must not exceed 150 characters';
    }

    const salaryNum = parseFloat(formData.salaryPackage);
    if (!formData.salaryPackage || isNaN(salaryNum) || salaryNum <= 0) {
      errors.salaryPackage = 'Salary package must be a positive number';
    }

    if (!formData.employmentType) {
      errors.employmentType = 'Employment type is required';
    }

    if (!formData.applicationStartDate) {
      errors.applicationStartDate = 'Application start date is required';
    }

    if (!formData.applicationEndDate) {
      errors.applicationEndDate = 'Application end date is required';
    }

    if (!formData.driveDate) {
      errors.driveDate = 'Drive date is required';
    }

    const vacNum = parseInt(formData.vacancies, 10);
    if (!formData.vacancies || isNaN(vacNum) || vacNum < 1) {
      errors.vacancies = 'Vacancies must be at least 1';
    }

    const cgpaNum = parseFloat(formData.minimumCgpa);
    if (formData.minimumCgpa === '' || isNaN(cgpaNum) || cgpaNum < 0 || cgpaNum > 10) {
      errors.minimumCgpa = 'Minimum CGPA must be between 0.0 and 10.0';
    }

    const backlogsNum = parseInt(formData.maximumAllowedBacklogs, 10);
    if (formData.maximumAllowedBacklogs === '' || isNaN(backlogsNum) || backlogsNum < 0) {
      errors.maximumAllowedBacklogs = 'Maximum allowed backlogs must be 0 or greater';
    }

    // Cross-field date checks
    if (formData.applicationStartDate && formData.applicationEndDate) {
      if (formData.applicationStartDate > formData.applicationEndDate) {
        errors.applicationStartDate = 'Start date cannot be after application end date';
        errors.applicationEndDate = 'End date cannot be before start date';
      }
    }

    if (formData.applicationEndDate && formData.driveDate) {
      if (formData.applicationEndDate > formData.driveDate) {
        errors.driveDate = 'Drive date cannot be before application deadline';
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setModalError('');

    if (!validateForm()) {
      return;
    }

    setSaving(true);
    try {
      // Backend expects company referenced by object: { company: { id: companyId } }
      const payload = {
        company: {
          id: Number(formData.companyId),
        },
        jobTitle: formData.jobTitle.trim(),
        jobDescription: formData.jobDescription.trim(),
        jobLocation: formData.jobLocation.trim(),
        salaryPackage: parseFloat(formData.salaryPackage),
        employmentType: formData.employmentType,
        applicationStartDate: formData.applicationStartDate,
        applicationEndDate: formData.applicationEndDate,
        driveDate: formData.driveDate,
        vacancies: parseInt(formData.vacancies, 10),
        status: formData.status,
        minimumCgpa: parseFloat(formData.minimumCgpa),
        maximumAllowedBacklogs: parseInt(formData.maximumAllowedBacklogs, 10),
        eligibleBranches: formData.eligibleBranches ? formData.eligibleBranches.trim() : null,
      };

      if (editingDrive) {
        await updatePlacementDrive(editingDrive.id, payload);
        setSuccessMessage(`Placement drive for "${payload.jobTitle}" updated successfully.`);
      } else {
        await createPlacementDrive(payload);
        setSuccessMessage(`Placement drive for "${payload.jobTitle}" created successfully.`);
      }

      setIsModalOpen(false);
      fetchDrives();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setModalError(err.message || 'Operation failed. Please review form fields.');
    } finally {
      setSaving(false);
    }
  };

  // ---------------------------------------------------------------------------
  // Delete Handler
  // ---------------------------------------------------------------------------

  const handleDelete = async (drive) => {
    const companyName = drive.company?.name || 'Company';
    const confirmed = window.confirm(
      `Are you sure you want to delete the placement drive for ${companyName} — "${drive.jobTitle}"?\nThis action cannot be undone.`
    );
    if (!confirmed) return;

    setDeletingId(drive.id);
    setError('');
    try {
      await deletePlacementDrive(drive.id);
      setSuccessMessage(`Placement drive "${drive.jobTitle}" deleted successfully.`);
      fetchDrives();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setError(err.message || `Failed to delete drive "${drive.jobTitle}".`);
    } finally {
      setDeletingId(null);
    }
  };

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  const getStatusBadgeStyle = (status) => {
    switch (status) {
      case 'UPCOMING':
        return styles.badgeUpcoming;
      case 'ONGOING':
        return styles.badgeOngoing;
      case 'COMPLETED':
        return styles.badgeCompleted;
      case 'CANCELLED':
        return styles.badgeCancelled;
      default:
        return styles.badgeDefault;
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    try {
      const [year, month, day] = dateStr.split('-');
      const date = new Date(year, month - 1, day);
      return date.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  const formatEmploymentType = (type) => {
    if (!type) return '—';
    return type.replace('_', ' ');
  };

  // ---------------------------------------------------------------------------
  // Render
  // ---------------------------------------------------------------------------

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        {/* Page Header */}
        <div style={styles.headerRow}>
          <div>
            <h1 style={styles.pageTitle}>Placement Drives</h1>
            <p style={styles.pageSubtitle}>
              Create and manage campus placement opportunities.
            </p>
          </div>
          <button type="button" onClick={openCreateModal} style={styles.primaryButton}>
            + Create Drive
          </button>
        </div>

        {/* Global Notifications */}
        {successMessage && <div style={styles.successBanner}>{successMessage}</div>}
        {error && (
          <div style={styles.errorBanner}>
            <span>{error}</span>
            <button type="button" onClick={fetchDrives} style={styles.retryButton}>
              Retry
            </button>
          </div>
        )}

        {/* Search & Filter Bar */}
        <div style={styles.filterCard}>
          <form onSubmit={handleSearchSubmit} style={styles.filterForm}>
            <div style={styles.searchGroup}>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search job title..."
                style={styles.searchInput}
              />
              <button type="submit" style={styles.searchButton}>
                Search
              </button>
            </div>

            <div style={styles.dropdownGroup}>
              <div style={styles.selectWrapper}>
                <label style={styles.filterLabel}>Status:</label>
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  style={styles.select}
                >
                  <option value="ALL">All Statuses</option>
                  {STATUS_OPTIONS.map((st) => (
                    <option key={st} value={st}>
                      {st}
                    </option>
                  ))}
                </select>
              </div>

              <div style={styles.selectWrapper}>
                <label style={styles.filterLabel}>Company:</label>
                <select
                  value={companyFilter}
                  onChange={(e) => setCompanyFilter(e.target.value)}
                  style={styles.select}
                >
                  <option value="ALL">All Companies</option>
                  {companies.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              {(searchTerm || statusFilter !== 'ALL' || companyFilter !== 'ALL') && (
                <button
                  type="button"
                  onClick={handleClearFilters}
                  style={styles.clearButton}
                >
                  Clear Filters
                </button>
              )}
            </div>
          </form>
        </div>

        {/* Table / Content Card */}
        <div style={styles.tableCard}>
          {loading ? (
            <div style={styles.messageBox}>
              <div style={styles.spinner}></div>
              <p style={styles.messageText}>Loading placement drives...</p>
            </div>
          ) : drives.length === 0 ? (
            <div style={styles.messageBox}>
              <p style={styles.messageText}>
                {searchTerm || statusFilter !== 'ALL' || companyFilter !== 'ALL'
                  ? 'No placement drives match your current filters.'
                  : 'No placement drives found. Click "+ Create Drive" to announce one.'}
              </p>
              {(searchTerm || statusFilter !== 'ALL' || companyFilter !== 'ALL') && (
                <button
                  type="button"
                  onClick={handleClearFilters}
                  style={styles.secondaryButton}
                >
                  Clear Filters
                </button>
              )}
            </div>
          ) : (
            <div style={styles.tableResponsive}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>Company</th>
                    <th style={styles.th}>Job Title</th>
                    <th style={styles.th}>Location</th>
                    <th style={styles.th}>Package</th>
                    <th style={styles.th}>Type</th>
                    <th style={styles.th}>Application Deadline</th>
                    <th style={styles.th}>Drive Date</th>
                    <th style={styles.th}>Vacancies</th>
                    <th style={styles.th}>Status</th>
                    <th style={{ ...styles.th, textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {drives.map((drive) => (
                    <tr key={drive.id} style={styles.tr}>
                      <td style={styles.td}>
                        <div style={styles.companyName}>
                          {drive.company?.name || 'Unknown Company'}
                        </div>
                      </td>
                      <td style={styles.td}>
                        <div style={styles.jobTitle}>{drive.jobTitle}</div>
                        {drive.jobDescription && (
                          <div style={styles.jobDesc} title={drive.jobDescription}>
                            {drive.jobDescription.length > 55
                              ? `${drive.jobDescription.substring(0, 55)}...`
                              : drive.jobDescription}
                          </div>
                        )}
                      </td>
                      <td style={styles.td}>{drive.jobLocation || '—'}</td>
                      <td style={styles.td}>
                        <span style={styles.packageBadge}>
                          ₹{drive.salaryPackage} LPA
                        </span>
                      </td>
                      <td style={styles.td}>
                        <span style={styles.typeBadge}>
                          {formatEmploymentType(drive.employmentType)}
                        </span>
                      </td>
                      <td style={styles.td}>{formatDate(drive.applicationEndDate)}</td>
                      <td style={styles.td}>{formatDate(drive.driveDate)}</td>
                      <td style={styles.td}>
                        <span style={styles.vacancyText}>{drive.vacancies}</span>
                      </td>
                      <td style={styles.td}>
                        <span style={getStatusBadgeStyle(drive.status)}>
                          {drive.status}
                        </span>
                      </td>
                      <td style={{ ...styles.td, textAlign: 'right' }}>
                        <div style={styles.actionsContainer}>
                          <button
                            type="button"
                            onClick={() => openEditModal(drive)}
                            style={styles.actionBtnEdit}
                            title="Edit Placement Drive"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(drive)}
                            disabled={deletingId === drive.id}
                            style={styles.actionBtnDelete}
                            title="Delete Placement Drive"
                          >
                            {deletingId === drive.id ? 'Deleting...' : 'Delete'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* ===================================================================== */}
      {/* CREATE / EDIT PLACEMENT DRIVE MODAL                                   */}
      {/* ===================================================================== */}
      {isModalOpen && (
        <div style={styles.modalOverlay} onClick={closeModal}>
          <div style={styles.modalCard} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalHeader}>
              <h2 style={styles.modalTitle}>
                {editingDrive ? 'Edit Placement Drive' : 'Create Placement Drive'}
              </h2>
              <button
                type="button"
                onClick={closeModal}
                disabled={saving}
                style={styles.modalCloseBtn}
                aria-label="Close modal"
              >
                ✕
              </button>
            </div>

            {modalError && <div style={styles.errorBanner}>{modalError}</div>}

            <form onSubmit={handleFormSubmit} style={styles.modalForm}>
              {/* Company Selection */}
              <div style={styles.formGroup}>
                <label style={styles.label}>
                  Host Company <span style={styles.required}>*</span>
                </label>
                <select
                  required
                  value={formData.companyId}
                  onChange={(e) =>
                    setFormData({ ...formData, companyId: e.target.value })
                  }
                  style={
                    formErrors.companyId ? styles.selectError : styles.selectInput
                  }
                  disabled={companiesLoading}
                >
                  <option value="">-- Select Active Company --</option>
                  {companies.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name} ({c.industry || 'General'})
                    </option>
                  ))}
                </select>
                {companies.length === 0 && !companiesLoading && (
                  <span style={styles.hintText}>
                    No active companies found. Please create/activate a company first.
                  </span>
                )}
                {formErrors.companyId && (
                  <span style={styles.errorText}>{formErrors.companyId}</span>
                )}
              </div>

              {/* Job Title & Location */}
              <div style={styles.formRow}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Job Title <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={150}
                    value={formData.jobTitle}
                    onChange={(e) =>
                      setFormData({ ...formData, jobTitle: e.target.value })
                    }
                    placeholder="e.g. Graduate Software Engineer"
                    style={
                      formErrors.jobTitle ? styles.inputError : styles.input
                    }
                  />
                  {formErrors.jobTitle && (
                    <span style={styles.errorText}>{formErrors.jobTitle}</span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Job Location <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={150}
                    value={formData.jobLocation}
                    onChange={(e) =>
                      setFormData({ ...formData, jobLocation: e.target.value })
                    }
                    placeholder="e.g. Bangalore / Remote"
                    style={
                      formErrors.jobLocation ? styles.inputError : styles.input
                    }
                  />
                  {formErrors.jobLocation && (
                    <span style={styles.errorText}>{formErrors.jobLocation}</span>
                  )}
                </div>
              </div>

              {/* Job Description */}
              <div style={styles.formGroup}>
                <label style={styles.label}>
                  Job Description & Requirements <span style={styles.required}>*</span>
                </label>
                <textarea
                  rows={4}
                  required
                  maxLength={2000}
                  value={formData.jobDescription}
                  onChange={(e) =>
                    setFormData({ ...formData, jobDescription: e.target.value })
                  }
                  placeholder="Detail job responsibilities, tech stacks, and drive expectations..."
                  style={
                    formErrors.jobDescription
                      ? styles.textareaError
                      : styles.textarea
                  }
                />
                {formErrors.jobDescription && (
                  <span style={styles.errorText}>
                    {formErrors.jobDescription}
                  </span>
                )}
              </div>

              {/* Package, Employment Type, Vacancies */}
              <div style={styles.formRowThree}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Package (LPA) <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    value={formData.salaryPackage}
                    onChange={(e) =>
                      setFormData({ ...formData, salaryPackage: e.target.value })
                    }
                    placeholder="e.g. 12.5"
                    style={
                      formErrors.salaryPackage
                        ? styles.inputError
                        : styles.input
                    }
                  />
                  {formErrors.salaryPackage && (
                    <span style={styles.errorText}>
                      {formErrors.salaryPackage}
                    </span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Employment Type <span style={styles.required}>*</span>
                  </label>
                  <select
                    required
                    value={formData.employmentType}
                    onChange={(e) =>
                      setFormData({ ...formData, employmentType: e.target.value })
                    }
                    style={styles.selectInput}
                  >
                    {EMPLOYMENT_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {formatEmploymentType(type)}
                      </option>
                    ))}
                  </select>
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Vacancies <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={formData.vacancies}
                    onChange={(e) =>
                      setFormData({ ...formData, vacancies: e.target.value })
                    }
                    placeholder="e.g. 10"
                    style={
                      formErrors.vacancies ? styles.inputError : styles.input
                    }
                  />
                  {formErrors.vacancies && (
                    <span style={styles.errorText}>{formErrors.vacancies}</span>
                  )}
                </div>
              </div>

              {/* Important Timeline Dates */}
              <div style={styles.formRowThree}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Application Start <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="date"
                    required
                    value={formData.applicationStartDate}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        applicationStartDate: e.target.value,
                      })
                    }
                    style={
                      formErrors.applicationStartDate
                        ? styles.inputError
                        : styles.input
                    }
                  />
                  {formErrors.applicationStartDate && (
                    <span style={styles.errorText}>
                      {formErrors.applicationStartDate}
                    </span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Application Deadline <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="date"
                    required
                    value={formData.applicationEndDate}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        applicationEndDate: e.target.value,
                      })
                    }
                    style={
                      formErrors.applicationEndDate
                        ? styles.inputError
                        : styles.input
                    }
                  />
                  {formErrors.applicationEndDate && (
                    <span style={styles.errorText}>
                      {formErrors.applicationEndDate}
                    </span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Drive Date <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="date"
                    required
                    value={formData.driveDate}
                    onChange={(e) =>
                      setFormData({ ...formData, driveDate: e.target.value })
                    }
                    style={
                      formErrors.driveDate ? styles.inputError : styles.input
                    }
                  />
                  {formErrors.driveDate && (
                    <span style={styles.errorText}>{formErrors.driveDate}</span>
                  )}
                </div>
              </div>

              {/* Eligibility Criteria */}
              <div style={styles.formRowThree}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Min CGPA (0-10) <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    max="10"
                    required
                    value={formData.minimumCgpa}
                    onChange={(e) =>
                      setFormData({ ...formData, minimumCgpa: e.target.value })
                    }
                    placeholder="e.g. 7.0"
                    style={
                      formErrors.minimumCgpa ? styles.inputError : styles.input
                    }
                  />
                  {formErrors.minimumCgpa && (
                    <span style={styles.errorText}>
                      {formErrors.minimumCgpa}
                    </span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Max Backlogs <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="number"
                    min="0"
                    required
                    value={formData.maximumAllowedBacklogs}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        maximumAllowedBacklogs: e.target.value,
                      })
                    }
                    placeholder="e.g. 0"
                    style={
                      formErrors.maximumAllowedBacklogs
                        ? styles.inputError
                        : styles.input
                    }
                  />
                  {formErrors.maximumAllowedBacklogs && (
                    <span style={styles.errorText}>
                      {formErrors.maximumAllowedBacklogs}
                    </span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Eligible Branches
                  </label>
                  <input
                    type="text"
                    maxLength={500}
                    value={formData.eligibleBranches}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        eligibleBranches: e.target.value,
                      })
                    }
                    placeholder="e.g. CSE, ECE, IT (leave blank for all)"
                    style={styles.input}
                  />
                </div>
              </div>

              {/* Status */}
              <div style={styles.formGroup}>
                <label style={styles.label}>Operational Status</label>
                <select
                  value={formData.status}
                  onChange={(e) =>
                    setFormData({ ...formData, status: e.target.value })
                  }
                  style={styles.selectInput}
                >
                  {STATUS_OPTIONS.map((st) => (
                    <option key={st} value={st}>
                      {st}
                    </option>
                  ))}
                </select>
              </div>

              {/* Modal Buttons */}
              <div style={styles.modalFooter}>
                <button
                  type="button"
                  onClick={closeModal}
                  disabled={saving}
                  style={styles.cancelButton}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving || companies.length === 0}
                  style={styles.primaryButton}
                >
                  {saving
                    ? 'Saving...'
                    : editingDrive
                    ? 'Save Changes'
                    : 'Create Drive'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

// -----------------------------------------------------------------------------
// Styles Object (Consistent Dark Slate Design System)
// -----------------------------------------------------------------------------

const styles = {
  page: {
    minHeight: '100vh',
    background: '#0f172a',
    color: '#f8fafc',
    fontFamily: 'Inter, system-ui, sans-serif',
    padding: '32px 20px',
  },
  container: {
    maxWidth: '1280px',
    margin: '0 auto',
  },
  headerRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    flexWrap: 'wrap',
    gap: '16px',
    marginBottom: '24px',
  },
  pageTitle: {
    margin: '0 0 6px 0',
    fontSize: '28px',
    fontWeight: '700',
    color: '#f8fafc',
  },
  pageSubtitle: {
    margin: 0,
    fontSize: '15px',
    color: '#94a3b8',
  },
  primaryButton: {
    background: '#3b82f6',
    color: '#ffffff',
    border: 'none',
    padding: '10px 20px',
    borderRadius: '8px',
    fontWeight: '600',
    fontSize: '14px',
    cursor: 'pointer',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    transition: 'background 0.2s',
  },
  secondaryButton: {
    background: 'transparent',
    color: '#60a5fa',
    border: '1px solid #3b82f6',
    padding: '8px 16px',
    borderRadius: '8px',
    fontSize: '13px',
    cursor: 'pointer',
    marginTop: '12px',
  },
  cancelButton: {
    background: '#334155',
    color: '#e2e8f0',
    border: 'none',
    padding: '10px 18px',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  filterCard: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: '12px',
    padding: '16px 20px',
    marginBottom: '20px',
  },
  filterForm: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '16px',
  },
  searchGroup: {
    display: 'flex',
    gap: '8px',
    flex: '1',
    minWidth: '260px',
    maxWidth: '440px',
  },
  searchInput: {
    flex: 1,
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 14px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
  },
  searchButton: {
    background: '#334155',
    color: '#f8fafc',
    border: '1px solid #475569',
    borderRadius: '8px',
    padding: '10px 16px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  dropdownGroup: {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '16px',
  },
  selectWrapper: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  filterLabel: {
    fontSize: '13px',
    color: '#cbd5e1',
    fontWeight: '500',
  },
  select: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 14px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
    cursor: 'pointer',
  },
  clearButton: {
    background: 'transparent',
    color: '#94a3b8',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 14px',
    fontSize: '14px',
    cursor: 'pointer',
  },
  tableCard: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: '12px',
    overflow: 'hidden',
  },
  tableResponsive: {
    overflowX: 'auto',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    textAlign: 'left',
    fontSize: '14px',
  },
  th: {
    background: '#1e293b',
    color: '#94a3b8',
    padding: '14px 16px',
    fontWeight: '600',
    borderBottom: '1px solid #334155',
    whiteSpace: 'nowrap',
  },
  tr: {
    borderBottom: '1px solid #334155',
  },
  td: {
    padding: '14px 16px',
    verticalAlign: 'middle',
    color: '#cbd5e1',
  },
  companyName: {
    fontWeight: '600',
    color: '#f8fafc',
    fontSize: '14px',
  },
  jobTitle: {
    fontWeight: '600',
    color: '#f8fafc',
  },
  jobDesc: {
    fontSize: '12px',
    color: '#94a3b8',
    marginTop: '2px',
  },
  packageBadge: {
    fontWeight: '600',
    color: '#34d399',
  },
  typeBadge: {
    display: 'inline-block',
    padding: '3px 8px',
    borderRadius: '6px',
    fontSize: '11px',
    fontWeight: '500',
    background: '#334155',
    color: '#cbd5e1',
  },
  vacancyText: {
    fontWeight: '600',
    color: '#f8fafc',
  },

  // Status Badges
  badgeUpcoming: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'rgba(59, 130, 246, 0.15)',
    color: '#60a5fa',
    border: '1px solid rgba(96, 165, 250, 0.4)',
  },
  badgeOngoing: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'rgba(34, 197, 94, 0.15)',
    color: '#4ade80',
    border: '1px solid rgba(74, 222, 128, 0.4)',
  },
  badgeCompleted: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'rgba(168, 85, 247, 0.15)',
    color: '#c084fc',
    border: '1px solid rgba(192, 132, 252, 0.4)',
  },
  badgeCancelled: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'rgba(239, 68, 68, 0.15)',
    color: '#f87171',
    border: '1px solid rgba(248, 113, 113, 0.4)',
  },
  badgeDefault: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: '#334155',
    color: '#cbd5e1',
  },

  actionsContainer: {
    display: 'flex',
    gap: '8px',
    justifyContent: 'flex-end',
    whiteSpace: 'nowrap',
  },
  actionBtnEdit: {
    background: 'transparent',
    color: '#60a5fa',
    border: '1px solid #3b82f6',
    borderRadius: '6px',
    padding: '6px 12px',
    fontSize: '12px',
    cursor: 'pointer',
    fontWeight: '500',
  },
  actionBtnDelete: {
    background: 'transparent',
    color: '#f87171',
    border: '1px solid #ef4444',
    borderRadius: '6px',
    padding: '6px 12px',
    fontSize: '12px',
    cursor: 'pointer',
    fontWeight: '500',
  },

  messageBox: {
    padding: '48px 24px',
    textAlign: 'center',
    color: '#94a3b8',
  },
  messageText: {
    fontSize: '15px',
    margin: 0,
  },
  spinner: {
    width: '28px',
    height: '28px',
    border: '3px solid #334155',
    borderTopColor: '#3b82f6',
    borderRadius: '50%',
    margin: '0 auto 14px auto',
  },
  successBanner: {
    padding: '12px 18px',
    borderRadius: '8px',
    background: 'rgba(34, 197, 94, 0.15)',
    border: '1px solid #22c55e',
    color: '#86efac',
    fontSize: '14px',
    marginBottom: '20px',
  },
  errorBanner: {
    padding: '12px 18px',
    borderRadius: '8px',
    background: 'rgba(239, 68, 68, 0.2)',
    border: '1px solid #ef4444',
    color: '#fca5a5',
    fontSize: '14px',
    marginBottom: '20px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '12px',
  },
  retryButton: {
    background: '#ef4444',
    color: '#ffffff',
    border: 'none',
    borderRadius: '6px',
    padding: '6px 12px',
    fontSize: '12px',
    cursor: 'pointer',
    fontWeight: '600',
  },

  // Modal Styles
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0, 0, 0, 0.75)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '20px',
    zIndex: 1000,
    backdropFilter: 'blur(3px)',
  },
  modalCard: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: '16px',
    width: '100%',
    maxWidth: '680px',
    maxHeight: '90vh',
    overflowY: 'auto',
    padding: '28px',
    boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)',
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
    borderBottom: '1px solid #334155',
    paddingBottom: '12px',
  },
  modalTitle: {
    margin: 0,
    fontSize: '20px',
    fontWeight: '700',
    color: '#f8fafc',
  },
  modalCloseBtn: {
    background: 'transparent',
    border: 'none',
    color: '#94a3b8',
    fontSize: '18px',
    cursor: 'pointer',
    padding: '4px 8px',
  },
  modalForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  formRow: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '16px',
  },
  formRowThree: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr 1fr',
    gap: '16px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '13px',
    fontWeight: '500',
    color: '#cbd5e1',
  },
  required: {
    color: '#f87171',
  },
  input: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
  },
  inputError: {
    background: '#0f172a',
    border: '1px solid #ef4444',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
  },
  selectInput: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
    cursor: 'pointer',
  },
  selectError: {
    background: '#0f172a',
    border: '1px solid #ef4444',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
  },
  textarea: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
    resize: 'vertical',
    fontFamily: 'inherit',
  },
  textareaError: {
    background: '#0f172a',
    border: '1px solid #ef4444',
    borderRadius: '8px',
    padding: '10px 12px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
    resize: 'vertical',
    fontFamily: 'inherit',
  },
  errorText: {
    fontSize: '12px',
    color: '#f87171',
  },
  hintText: {
    fontSize: '12px',
    color: '#eab308',
  },
  modalFooter: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '12px',
    marginTop: '12px',
    borderTop: '1px solid #334155',
    paddingTop: '16px',
  },
};
