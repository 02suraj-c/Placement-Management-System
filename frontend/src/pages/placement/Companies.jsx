import React, { useState, useEffect } from 'react';
import {
  getCompanies,
  searchCompanies,
  createCompany,
  updateCompany,
  deleteCompany,
} from '../../services/placementService';

export default function Companies() {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [activeFilter, setActiveFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  // Modal / Form state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCompany, setEditingCompany] = useState(null);
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [modalError, setModalError] = useState('');

  const initialFormData = {
    name: '',
    description: '',
    industry: '',
    website: '',
    location: '',
    contactEmail: '',
    contactPhone: '',
    active: true,
  };

  const [formData, setFormData] = useState(initialFormData);
  const [formErrors, setFormErrors] = useState({});

  // ---------------------------------------------------------------------------
  // Data Fetching
  // ---------------------------------------------------------------------------

  const fetchCompanies = async () => {
    setLoading(true);
    setError('');
    try {
      let data;
      if (searchTerm.trim()) {
        data = await searchCompanies(searchTerm.trim());
        // If an active filter is also selected, apply it in-memory to the search results
        if (activeFilter === 'ACTIVE') {
          data = data.filter((c) => c.active === true);
        } else if (activeFilter === 'INACTIVE') {
          data = data.filter((c) => c.active === false);
        }
      } else {
        const filterParam =
          activeFilter === 'ACTIVE' ? true : activeFilter === 'INACTIVE' ? false : null;
        data = await getCompanies(filterParam);
      }
      setCompanies(data || []);
    } catch (err) {
      setError(err.message || 'Failed to load companies.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCompanies();
  }, [activeFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchCompanies();
  };

  const handleClearSearch = () => {
    setSearchTerm('');
    // Trigger fresh fetch without search term
    setError('');
    setLoading(true);
    const filterParam =
      activeFilter === 'ACTIVE' ? true : activeFilter === 'INACTIVE' ? false : null;
    getCompanies(filterParam)
      .then((data) => setCompanies(data || []))
      .catch((err) => setError(err.message || 'Failed to load companies.'))
      .finally(() => setLoading(false));
  };

  // ---------------------------------------------------------------------------
  // Modal Handlers & Validation
  // ---------------------------------------------------------------------------

  const openAddModal = () => {
    setEditingCompany(null);
    setFormData(initialFormData);
    setFormErrors({});
    setModalError('');
    setIsModalOpen(true);
  };

  const openEditModal = (company) => {
    setEditingCompany(company);
    setFormData({
      name: company.name || '',
      description: company.description || '',
      industry: company.industry || '',
      website: company.website || '',
      location: company.location || '',
      contactEmail: company.contactEmail || '',
      contactPhone: company.contactPhone || '',
      active: company.active !== undefined ? company.active : true,
    });
    setFormErrors({});
    setModalError('');
    setIsModalOpen(true);
  };

  const closeModal = () => {
    if (!formSubmitting) {
      setIsModalOpen(false);
      setEditingCompany(null);
      setFormData(initialFormData);
      setFormErrors({});
      setModalError('');
    }
  };

  const validateForm = () => {
    const errors = {};
    if (!formData.name.trim()) {
      errors.name = 'Company name is required';
    } else if (formData.name.length > 150) {
      errors.name = 'Name must not exceed 150 characters';
    }

    if (!formData.contactEmail.trim()) {
      errors.contactEmail = 'Contact email is required';
    } else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.contactEmail.trim())) {
        errors.contactEmail = 'Please enter a valid email address';
      }
    }

    if (formData.description && formData.description.length > 1000) {
      errors.description = 'Description must not exceed 1000 characters';
    }
    if (formData.industry && formData.industry.length > 100) {
      errors.industry = 'Industry must not exceed 100 characters';
    }
    if (formData.website && formData.website.length > 255) {
      errors.website = 'Website URL must not exceed 255 characters';
    }
    if (formData.location && formData.location.length > 200) {
      errors.location = 'Location must not exceed 200 characters';
    }
    if (formData.contactPhone && formData.contactPhone.length > 20) {
      errors.contactPhone = 'Contact phone must not exceed 20 characters';
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

    setFormSubmitting(true);
    try {
      const payload = {
        name: formData.name.trim(),
        description: formData.description.trim() || null,
        industry: formData.industry.trim() || null,
        website: formData.website.trim() || null,
        location: formData.location.trim() || null,
        contactEmail: formData.contactEmail.trim(),
        contactPhone: formData.contactPhone.trim() || null,
        active: Boolean(formData.active),
      };

      if (editingCompany) {
        await updateCompany(editingCompany.id, payload);
        setSuccessMessage(`Company "${payload.name}" updated successfully.`);
      } else {
        await createCompany(payload);
        setSuccessMessage(`Company "${payload.name}" created successfully.`);
      }

      setIsModalOpen(false);
      fetchCompanies();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setModalError(err.message || 'Operation failed. Please check form details.');
    } finally {
      setFormSubmitting(false);
    }
  };

  // ---------------------------------------------------------------------------
  // Toggle Status & Delete
  // ---------------------------------------------------------------------------

  const handleToggleStatus = async (company) => {
    setError('');
    try {
      const updatedPayload = {
        name: company.name,
        description: company.description,
        industry: company.industry,
        website: company.website,
        location: company.location,
        contactEmail: company.contactEmail,
        contactPhone: company.contactPhone,
        active: !company.active,
      };

      await updateCompany(company.id, updatedPayload);
      setSuccessMessage(
        `"${company.name}" marked as ${!company.active ? 'Active' : 'Inactive'}.`
      );
      fetchCompanies();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setError(err.message || `Failed to update status for ${company.name}.`);
    }
  };

  const handleDelete = async (company) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${company.name}"?\nThis action cannot be undone.`
    );
    if (!confirmed) return;

    setError('');
    try {
      await deleteCompany(company.id);
      setSuccessMessage(`Company "${company.name}" deleted successfully.`);
      fetchCompanies();
      setTimeout(() => setSuccessMessage(''), 4000);
    } catch (err) {
      setError(err.message || `Failed to delete "${company.name}".`);
    }
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
            <h1 style={styles.pageTitle}>Company Management</h1>
            <p style={styles.pageSubtitle}>
              Manage companies participating in campus placement drives.
            </p>
          </div>
          <button type="button" onClick={openAddModal} style={styles.primaryButton}>
            + Add Company
          </button>
        </div>

        {/* Global Notifications */}
        {successMessage && <div style={styles.successBanner}>{successMessage}</div>}
        {error && (
          <div style={styles.errorBanner}>
            <span>{error}</span>
            <button type="button" onClick={fetchCompanies} style={styles.retryButton}>
              Retry
            </button>
          </div>
        )}

        {/* Search & Filters */}
        <div style={styles.filterCard}>
          <form onSubmit={handleSearchSubmit} style={styles.searchForm}>
            <div style={styles.searchGroup}>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search companies by name..."
                style={styles.searchInput}
              />
              <button type="submit" style={styles.searchButton}>
                Search
              </button>
              {searchTerm && (
                <button
                  type="button"
                  onClick={handleClearSearch}
                  style={styles.clearButton}
                >
                  Clear
                </button>
              )}
            </div>

            <div style={styles.filterGroup}>
              <label style={styles.filterLabel}>Status:</label>
              <select
                value={activeFilter}
                onChange={(e) => setActiveFilter(e.target.value)}
                style={styles.filterSelect}
              >
                <option value="ALL">All Companies</option>
                <option value="ACTIVE">Active Only</option>
                <option value="INACTIVE">Inactive Only</option>
              </select>
            </div>
          </form>
        </div>

        {/* Table / Content Section */}
        <div style={styles.tableCard}>
          {loading ? (
            <div style={styles.messageBox}>
              <div style={styles.spinner}></div>
              <p style={styles.messageText}>Loading companies...</p>
            </div>
          ) : companies.length === 0 ? (
            <div style={styles.messageBox}>
              <p style={styles.messageText}>
                {searchTerm || activeFilter !== 'ALL'
                  ? 'No companies match your current filters.'
                  : 'No companies found. Click "Add Company" to register one.'}
              </p>
              {searchTerm && (
                <button
                  type="button"
                  onClick={handleClearSearch}
                  style={styles.secondaryButton}
                >
                  Clear Search
                </button>
              )}
            </div>
          ) : (
            <div style={styles.tableResponsive}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>Company</th>
                    <th style={styles.th}>Industry</th>
                    <th style={styles.th}>Location</th>
                    <th style={styles.th}>Contact</th>
                    <th style={styles.th}>Website</th>
                    <th style={styles.th}>Status</th>
                    <th style={{ ...styles.th, textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {companies.map((company) => (
                    <tr key={company.id} style={styles.tr}>
                      <td style={styles.td}>
                        <div style={styles.companyName}>{company.name}</div>
                        {company.description && (
                          <div style={styles.companyDesc} title={company.description}>
                            {company.description.length > 70
                              ? `${company.description.substring(0, 70)}...`
                              : company.description}
                          </div>
                        )}
                      </td>
                      <td style={styles.td}>{company.industry || '—'}</td>
                      <td style={styles.td}>{company.location || '—'}</td>
                      <td style={styles.td}>
                        <div style={styles.contactEmail}>{company.contactEmail}</div>
                        {company.contactPhone && (
                          <div style={styles.contactPhone}>{company.contactPhone}</div>
                        )}
                      </td>
                      <td style={styles.td}>
                        {company.website ? (
                          <a
                            href={
                              company.website.startsWith('http')
                                ? company.website
                                : `https://${company.website}`
                            }
                            target="_blank"
                            rel="noopener noreferrer"
                            style={styles.tableLink}
                          >
                            Visit Site ↗
                          </a>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td style={styles.td}>
                        <span
                          style={
                            company.active
                              ? styles.badgeActive
                              : styles.badgeInactive
                          }
                        >
                          {company.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td style={{ ...styles.td, textAlign: 'right' }}>
                        <div style={styles.actionsContainer}>
                          <button
                            type="button"
                            onClick={() => openEditModal(company)}
                            style={styles.actionBtnEdit}
                            title="Edit Company"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => handleToggleStatus(company)}
                            style={styles.actionBtnToggle}
                            title="Toggle Active / Inactive"
                          >
                            {company.active ? 'Deactivate' : 'Activate'}
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(company)}
                            style={styles.actionBtnDelete}
                            title="Delete Company"
                          >
                            Delete
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
      {/* ADD / EDIT COMPANY MODAL                                              */}
      {/* ===================================================================== */}
      {isModalOpen && (
        <div style={styles.modalOverlay} onClick={closeModal}>
          <div style={styles.modalCard} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalHeader}>
              <h2 style={styles.modalTitle}>
                {editingCompany ? 'Edit Company' : 'Add New Company'}
              </h2>
              <button
                type="button"
                onClick={closeModal}
                disabled={formSubmitting}
                style={styles.modalCloseBtn}
                aria-label="Close Modal"
              >
                ✕
              </button>
            </div>

            {modalError && <div style={styles.errorBanner}>{modalError}</div>}

            <form onSubmit={handleFormSubmit} style={styles.modalForm}>
              <div style={styles.formRow}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Company Name <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={150}
                    value={formData.name}
                    onChange={(e) =>
                      setFormData({ ...formData, name: e.target.value })
                    }
                    placeholder="e.g. Google, Microsoft, Infosys"
                    style={formErrors.name ? styles.inputError : styles.input}
                  />
                  {formErrors.name && (
                    <span style={styles.errorText}>{formErrors.name}</span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>Industry</label>
                  <input
                    type="text"
                    maxLength={100}
                    value={formData.industry}
                    onChange={(e) =>
                      setFormData({ ...formData, industry: e.target.value })
                    }
                    placeholder="e.g. Information Technology, Finance"
                    style={styles.input}
                  />
                </div>
              </div>

              <div style={styles.formGroup}>
                <label style={styles.label}>Description</label>
                <textarea
                  rows={3}
                  maxLength={1000}
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                  placeholder="Overview of the company, domains, and operations..."
                  style={styles.textarea}
                />
              </div>

              <div style={styles.formRow}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>Location / Headquarters</label>
                  <input
                    type="text"
                    maxLength={200}
                    value={formData.location}
                    onChange={(e) =>
                      setFormData({ ...formData, location: e.target.value })
                    }
                    placeholder="e.g. Bangalore, India"
                    style={styles.input}
                  />
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>Website URL</label>
                  <input
                    type="url"
                    maxLength={255}
                    value={formData.website}
                    onChange={(e) =>
                      setFormData({ ...formData, website: e.target.value })
                    }
                    placeholder="https://example.com"
                    style={styles.input}
                  />
                </div>
              </div>

              <div style={styles.formRow}>
                <div style={styles.formGroup}>
                  <label style={styles.label}>
                    Contact Email <span style={styles.required}>*</span>
                  </label>
                  <input
                    type="email"
                    required
                    maxLength={255}
                    value={formData.contactEmail}
                    onChange={(e) =>
                      setFormData({ ...formData, contactEmail: e.target.value })
                    }
                    placeholder="recruiter@example.com"
                    style={
                      formErrors.contactEmail
                        ? styles.inputError
                        : styles.input
                    }
                  />
                  {formErrors.contactEmail && (
                    <span style={styles.errorText}>
                      {formErrors.contactEmail}
                    </span>
                  )}
                </div>

                <div style={styles.formGroup}>
                  <label style={styles.label}>Contact Phone</label>
                  <input
                    type="tel"
                    maxLength={20}
                    value={formData.contactPhone}
                    onChange={(e) =>
                      setFormData({ ...formData, contactPhone: e.target.value })
                    }
                    placeholder="+91 9876543210"
                    style={styles.input}
                  />
                </div>
              </div>

              <div style={styles.checkboxGroup}>
                <label style={styles.checkboxLabel}>
                  <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) =>
                      setFormData({ ...formData, active: e.target.checked })
                    }
                    style={styles.checkbox}
                  />
                  Active (Allow company to host drives and appear to students)
                </label>
              </div>

              <div style={styles.modalFooter}>
                <button
                  type="button"
                  onClick={closeModal}
                  disabled={formSubmitting}
                  style={styles.cancelButton}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={formSubmitting}
                  style={styles.primaryButton}
                >
                  {formSubmitting
                    ? 'Saving...'
                    : editingCompany
                    ? 'Save Changes'
                    : 'Create Company'}
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
// Styles Object (Dark Slate Theme)
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
    maxWidth: '1200px',
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
  searchForm: {
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
    maxWidth: '540px',
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
  clearButton: {
    background: 'transparent',
    color: '#94a3b8',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 14px',
    fontSize: '14px',
    cursor: 'pointer',
  },
  filterGroup: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  filterLabel: {
    fontSize: '14px',
    color: '#cbd5e1',
    fontWeight: '500',
  },
  filterSelect: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 14px',
    color: '#f8fafc',
    fontSize: '14px',
    outline: 'none',
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
    padding: '14px 18px',
    fontWeight: '600',
    borderBottom: '1px solid #334155',
    whiteSpace: 'nowrap',
  },
  tr: {
    borderBottom: '1px solid #334155',
    transition: 'background 0.15s',
  },
  td: {
    padding: '14px 18px',
    verticalAlign: 'middle',
    color: '#cbd5e1',
  },
  companyName: {
    fontWeight: '600',
    color: '#f8fafc',
    fontSize: '15px',
  },
  companyDesc: {
    fontSize: '12px',
    color: '#94a3b8',
    marginTop: '3px',
  },
  contactEmail: {
    color: '#f8fafc',
  },
  contactPhone: {
    fontSize: '12px',
    color: '#94a3b8',
    marginTop: '2px',
  },
  tableLink: {
    color: '#60a5fa',
    textDecoration: 'none',
    fontWeight: '500',
  },
  badgeActive: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'rgba(34, 197, 94, 0.15)',
    color: '#4ade80',
    border: '1px solid rgba(74, 222, 128, 0.4)',
  },
  badgeInactive: {
    display: 'inline-block',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '600',
    background: 'rgba(148, 163, 184, 0.15)',
    color: '#94a3b8',
    border: '1px solid #475569',
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
  actionBtnToggle: {
    background: 'transparent',
    color: '#e2e8f0',
    border: '1px solid #475569',
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
    animation: 'spin 1s linear infinite',
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
    maxWidth: '620px',
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
  errorText: {
    fontSize: '12px',
    color: '#f87171',
  },
  checkboxGroup: {
    marginTop: '4px',
  },
  checkboxLabel: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '13px',
    color: '#cbd5e1',
    cursor: 'pointer',
  },
  checkbox: {
    width: '16px',
    height: '16px',
    cursor: 'pointer',
    accentColor: '#3b82f6',
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
