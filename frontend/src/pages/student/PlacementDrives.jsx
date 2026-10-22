import React, { useState, useEffect } from 'react';
import {
  getPlacementDrives,
  getPlacementDriveById,
  getDrivesByCompany,
  getDrivesByStatus,
  searchPlacementDrives,
  getCompanies,
} from '../../services/placementService';

const STATUS_OPTIONS = ['UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'];

export default function PlacementDrives() {
  const [drives, setDrives] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [companyFilter, setCompanyFilter] = useState('ALL');

  // Details Modal state
  const [selectedDrive, setSelectedDrive] = useState(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState('');

  // ---------------------------------------------------------------------------
  // Data Fetching
  // ---------------------------------------------------------------------------

  const loadFilterCompanies = async () => {
    try {
      // Only active companies are loaded for the student company filter
      const data = await getCompanies(true);
      setCompanies(data || []);
    } catch (err) {
      console.error('Failed to load companies for filter', err);
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

      // In-memory combined filtering if multiple filters are selected
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
      setError(err.message || 'Failed to load placement opportunities.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFilterCompanies();
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
  // View Details Modal Handler
  // ---------------------------------------------------------------------------

  const handleViewDetails = async (drive) => {
    setSelectedDrive(drive);
    setIsDetailsOpen(true);
    setDetailsError('');

    // If company information or description is already complete, use it directly
    if (drive.company && drive.jobDescription) {
      return;
    }

    // Otherwise, fetch full drive details by ID
    setDetailsLoading(true);
    try {
      const fullDrive = await getPlacementDriveById(drive.id);
      setSelectedDrive(fullDrive);
    } catch (err) {
      setDetailsError(err.message || 'Failed to load full drive details.');
    } finally {
      setDetailsLoading(false);
    }
  };

  const closeDetailsModal = () => {
    setIsDetailsOpen(false);
    setSelectedDrive(null);
    setDetailsError('');
  };

  // ---------------------------------------------------------------------------
  // Formatting & Deadline Calculations
  // ---------------------------------------------------------------------------

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

  /**
   * Calculates calendar days remaining until the deadline date (YYYY-MM-DD).
   * For completed or cancelled drives, returns appropriate status text.
   */
  const getDeadlineIndicator = (deadlineStr, status) => {
    if (status === 'COMPLETED' || status === 'CANCELLED') {
      return { text: 'Drive Closed', type: 'closed' };
    }
    if (!deadlineStr) {
      return { text: 'No Deadline', type: 'normal' };
    }

    try {
      const [year, month, day] = deadlineStr.split('-').map(Number);
      const deadline = new Date(year, month - 1, day);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      deadline.setHours(0, 0, 0, 0);

      const diffTime = deadline.getTime() - today.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

      if (diffDays < 0) {
        return { text: 'Deadline passed', type: 'expired' };
      }
      if (diffDays === 0) {
        return { text: 'Deadline today', type: 'urgent' };
      }
      if (diffDays === 1) {
        return { text: '1 day left', type: 'urgent' };
      }
      if (diffDays <= 3) {
        return { text: `${diffDays} days left`, type: 'warning' };
      }
      return { text: `${diffDays} days left`, type: 'normal' };
    } catch {
      return { text: deadlineStr, type: 'normal' };
    }
  };

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

  const getDeadlineBadgeStyle = (type) => {
    switch (type) {
      case 'urgent':
        return styles.deadlineUrgent;
      case 'warning':
        return styles.deadlineWarning;
      case 'expired':
      case 'closed':
        return styles.deadlineExpired;
      default:
        return styles.deadlineNormal;
    }
  };

  // ---------------------------------------------------------------------------
  // Render
  // ---------------------------------------------------------------------------

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        {/* Page Header */}
        <div style={styles.header}>
          <h1 style={styles.pageTitle}>Placement Drives</h1>
          <p style={styles.pageSubtitle}>
            Explore available campus placement opportunities.
          </p>
        </div>

        {/* Global Error Banner */}
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
                placeholder="Search jobs by title..."
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

        {/* Content Area */}
        {loading ? (
          <div style={styles.messageBox}>
            <div style={styles.spinner}></div>
            <p style={styles.messageText}>Loading placement drives...</p>
          </div>
        ) : drives.length === 0 ? (
          <div style={styles.emptyCard}>
            <p style={styles.messageText}>
              {searchTerm || statusFilter !== 'ALL' || companyFilter !== 'ALL'
                ? 'No placement drives match your current filters.'
                : 'No placement drives available.'}
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
          <div style={styles.cardGrid}>
            {drives.map((drive) => {
              const deadlineInfo = getDeadlineIndicator(
                drive.applicationEndDate,
                drive.status
              );
              const companyName = drive.company?.name || 'Company information unavailable';

              return (
                <div key={drive.id} style={styles.card}>
                  {/* Card Header */}
                  <div style={styles.cardHeader}>
                    <div>
                      <span style={styles.companyNameText}>{companyName}</span>
                      <h2 style={styles.jobTitleText}>{drive.jobTitle}</h2>
                    </div>
                    <span style={getStatusBadgeStyle(drive.status)}>
                      {drive.status}
                    </span>
                  </div>

                  {/* Highlights Row */}
                  <div style={styles.highlightsRow}>
                    <div style={styles.packageText}>
                      ₹{drive.salaryPackage} LPA
                    </div>
                    <div style={styles.badgeRow}>
                      <span style={styles.typeBadge}>
                        {formatEmploymentType(drive.employmentType)}
                      </span>
                    </div>
                  </div>

                  {/* Location & Vacancies */}
                  <div style={styles.metaRow}>
                    <span style={styles.locationText}>
                      📍 {drive.jobLocation || 'Location unspecified'}
                    </span>
                    <span style={styles.vacanciesText}>
                      {drive.vacancies} {drive.vacancies === 1 ? 'vacancy' : 'vacancies'}
                    </span>
                  </div>

                  {/* Divider */}
                  <div style={styles.cardDivider}></div>

                  {/* Timelines & Deadline */}
                  <div style={styles.timelineSection}>
                    <div style={styles.timelineRow}>
                      <span style={styles.timelineLabel}>Deadline:</span>
                      <div style={styles.deadlineContainer}>
                        <span style={styles.timelineValue}>
                          {formatDate(drive.applicationEndDate)}
                        </span>
                        <span style={getDeadlineBadgeStyle(deadlineInfo.type)}>
                          {deadlineInfo.text}
                        </span>
                      </div>
                    </div>

                    <div style={styles.timelineRow}>
                      <span style={styles.timelineLabel}>Drive Date:</span>
                      <span style={styles.timelineValue}>
                        {formatDate(drive.driveDate)}
                      </span>
                    </div>
                  </div>

                  {/* Action Button */}
                  <div style={styles.cardFooter}>
                    <button
                      type="button"
                      onClick={() => handleViewDetails(drive)}
                      style={styles.detailsButton}
                    >
                      View Details
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* ===================================================================== */}
      {/* DRIVE DETAILS MODAL                                                   */}
      {/* ===================================================================== */}
      {isDetailsOpen && selectedDrive && (
        <div style={styles.modalOverlay} onClick={closeDetailsModal}>
          <div style={styles.modalCard} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalHeader}>
              <div>
                <span style={styles.modalCompanyText}>
                  {selectedDrive.company?.name || 'Company information unavailable'}
                </span>
                <h2 style={styles.modalJobTitle}>{selectedDrive.jobTitle}</h2>
              </div>
              <button
                type="button"
                onClick={closeDetailsModal}
                style={styles.modalCloseBtn}
                aria-label="Close details"
              >
                ✕
              </button>
            </div>

            {detailsLoading ? (
              <div style={styles.modalLoadingBox}>
                <div style={styles.spinner}></div>
                <p style={styles.messageText}>Loading drive details...</p>
              </div>
            ) : detailsError ? (
              <div style={styles.errorBanner}>{detailsError}</div>
            ) : (
              <div style={styles.modalBody}>
                {/* Status & Package Banner */}
                <div style={styles.modalHighlightsBar}>
                  <div>
                    <span style={styles.modalLabel}>CTC Package</span>
                    <div style={styles.modalPackageValue}>
                      ₹{selectedDrive.salaryPackage} LPA
                    </div>
                  </div>
                  <div>
                    <span style={styles.modalLabel}>Employment Type</span>
                    <div style={styles.modalMetaValue}>
                      {formatEmploymentType(selectedDrive.employmentType)}
                    </div>
                  </div>
                  <div>
                    <span style={styles.modalLabel}>Location</span>
                    <div style={styles.modalMetaValue}>
                      📍 {selectedDrive.jobLocation || '—'}
                    </div>
                  </div>
                  <div>
                    <span style={styles.modalLabel}>Status</span>
                    <div>
                      <span style={getStatusBadgeStyle(selectedDrive.status)}>
                        {selectedDrive.status}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Role Description */}
                <div style={styles.modalSection}>
                  <h3 style={styles.modalSectionHeading}>Job Description</h3>
                  <div style={styles.descriptionBox}>
                    {selectedDrive.jobDescription || 'No description provided.'}
                  </div>
                </div>

                {/* Key Recruitment Timelines */}
                <div style={styles.modalSection}>
                  <h3 style={styles.modalSectionHeading}>Drive Schedule</h3>
                  <div style={styles.infoGrid}>
                    <div style={styles.infoItem}>
                      <span style={styles.infoLabel}>Applications Open</span>
                      <span style={styles.infoValue}>
                        {formatDate(selectedDrive.applicationStartDate)}
                      </span>
                    </div>
                    <div style={styles.infoItem}>
                      <span style={styles.infoLabel}>Application Deadline</span>
                      <span style={styles.infoValue}>
                        {formatDate(selectedDrive.applicationEndDate)}
                      </span>
                    </div>
                    <div style={styles.infoItem}>
                      <span style={styles.infoLabel}>Drive / Interview Date</span>
                      <span style={styles.infoValue}>
                        {formatDate(selectedDrive.driveDate)}
                      </span>
                    </div>
                    <div style={styles.infoItem}>
                      <span style={styles.infoLabel}>Open Vacancies</span>
                      <span style={styles.infoValue}>
                        {selectedDrive.vacancies} positions
                      </span>
                    </div>
                  </div>
                </div>

                {/* Company Information */}
                {selectedDrive.company && (
                  <div style={styles.modalSection}>
                    <h3 style={styles.modalSectionHeading}>Company Overview</h3>
                    <div style={styles.companyInfoCard}>
                      <div style={styles.companyInfoGrid}>
                        <div>
                          <span style={styles.infoLabel}>Industry</span>
                          <span style={styles.infoValue}>
                            {selectedDrive.company.industry || '—'}
                          </span>
                        </div>
                        <div>
                          <span style={styles.infoLabel}>Headquarters</span>
                          <span style={styles.infoValue}>
                            {selectedDrive.company.location || '—'}
                          </span>
                        </div>
                        <div>
                          <span style={styles.infoLabel}>Website</span>
                          {selectedDrive.company.website ? (
                            <a
                              href={
                                selectedDrive.company.website.startsWith('http')
                                  ? selectedDrive.company.website
                                  : `https://${selectedDrive.company.website}`
                              }
                              target="_blank"
                              rel="noopener noreferrer"
                              style={styles.modalLink}
                            >
                              Visit Website ↗
                            </a>
                          ) : (
                            <span style={styles.infoValue}>—</span>
                          )}
                        </div>
                      </div>
                      {selectedDrive.company.description && (
                        <p style={styles.companyDescriptionText}>
                          {selectedDrive.company.description}
                        </p>
                      )}
                    </div>
                  </div>
                )}

                {/* Eligibility Notice (Informational element, no fake logic) */}
                <div style={styles.eligibilityNotice}>
                  <div style={styles.noticeIcon}>ℹ</div>
                  <div>
                    <span style={styles.noticeTitle}>Eligibility Verification</span>
                    <p style={styles.noticeText}>
                      Eligibility checking will be available soon.
                    </p>
                  </div>
                </div>

                {/* Modal Actions */}
                <div style={styles.modalFooter}>
                  <button
                    type="button"
                    onClick={closeDetailsModal}
                    style={styles.closeFooterButton}
                  >
                    Close
                  </button>
                </div>
              </div>
            )}
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
    maxWidth: '1240px',
    margin: '0 auto',
  },
  header: {
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
  filterCard: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: '12px',
    padding: '16px 20px',
    marginBottom: '24px',
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

  // Grid & Cards
  cardGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(330px, 1fr))',
    gap: '20px',
  },
  card: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: '14px',
    padding: '22px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.2)',
    transition: 'border-color 0.2s, transform 0.15s',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: '12px',
    marginBottom: '14px',
  },
  companyNameText: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#60a5fa',
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  jobTitleText: {
    margin: '4px 0 0 0',
    fontSize: '18px',
    fontWeight: '700',
    color: '#f8fafc',
    lineHeight: '1.3',
  },
  highlightsRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '12px',
  },
  packageText: {
    fontSize: '20px',
    fontWeight: '700',
    color: '#34d399',
  },
  badgeRow: {
    display: 'flex',
    gap: '6px',
  },
  typeBadge: {
    display: 'inline-block',
    padding: '3px 8px',
    borderRadius: '6px',
    fontSize: '11px',
    fontWeight: '600',
    background: '#334155',
    color: '#cbd5e1',
    textTransform: 'uppercase',
  },
  metaRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '13px',
    color: '#cbd5e1',
    marginBottom: '14px',
  },
  locationText: {
    color: '#cbd5e1',
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
  },
  vacanciesText: {
    fontWeight: '500',
    color: '#94a3b8',
  },
  cardDivider: {
    height: '1px',
    background: '#334155',
    margin: '0 0 14px 0',
  },
  timelineSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    marginBottom: '18px',
    fontSize: '13px',
  },
  timelineRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  timelineLabel: {
    color: '#94a3b8',
  },
  timelineValue: {
    color: '#f8fafc',
    fontWeight: '500',
  },
  deadlineContainer: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  cardFooter: {
    marginTop: 'auto',
  },
  detailsButton: {
    width: '100%',
    background: '#3b82f6',
    color: '#ffffff',
    border: 'none',
    padding: '10px 16px',
    borderRadius: '8px',
    fontWeight: '600',
    fontSize: '14px',
    cursor: 'pointer',
    textAlign: 'center',
    transition: 'background 0.15s',
  },

  // Deadline Badges
  deadlineNormal: {
    display: 'inline-block',
    padding: '2px 7px',
    borderRadius: '10px',
    fontSize: '11px',
    fontWeight: '600',
    background: 'rgba(59, 130, 246, 0.15)',
    color: '#60a5fa',
    border: '1px solid rgba(96, 165, 250, 0.3)',
  },
  deadlineWarning: {
    display: 'inline-block',
    padding: '2px 7px',
    borderRadius: '10px',
    fontSize: '11px',
    fontWeight: '600',
    background: 'rgba(251, 191, 36, 0.15)',
    color: '#fbbf24',
    border: '1px solid rgba(251, 191, 36, 0.3)',
  },
  deadlineUrgent: {
    display: 'inline-block',
    padding: '2px 7px',
    borderRadius: '10px',
    fontSize: '11px',
    fontWeight: '600',
    background: 'rgba(239, 68, 68, 0.2)',
    color: '#f87171',
    border: '1px solid rgba(239, 68, 68, 0.4)',
  },
  deadlineExpired: {
    display: 'inline-block',
    padding: '2px 7px',
    borderRadius: '10px',
    fontSize: '11px',
    fontWeight: '600',
    background: 'rgba(148, 163, 184, 0.15)',
    color: '#94a3b8',
    border: '1px solid #475569',
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

  // State Notifications & Boxes
  emptyCard: {
    background: '#1e293b',
    border: '1px solid #334155',
    borderRadius: '12px',
    padding: '48px 24px',
    textAlign: 'center',
  },
  messageBox: {
    padding: '60px 24px',
    textAlign: 'center',
    color: '#94a3b8',
  },
  messageText: {
    fontSize: '15px',
    margin: 0,
    color: '#94a3b8',
  },
  spinner: {
    width: '32px',
    height: '32px',
    border: '3px solid #334155',
    borderTopColor: '#3b82f6',
    borderRadius: '50%',
    margin: '0 auto 16px auto',
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
    maxWidth: '720px',
    maxHeight: '90vh',
    overflowY: 'auto',
    padding: '28px',
    boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)',
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '20px',
    borderBottom: '1px solid #334155',
    paddingBottom: '16px',
  },
  modalCompanyText: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#60a5fa',
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  modalJobTitle: {
    margin: '4px 0 0 0',
    fontSize: '22px',
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
  modalLoadingBox: {
    padding: '40px 20px',
    textAlign: 'center',
  },
  modalBody: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  modalHighlightsBar: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
    gap: '14px',
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '10px',
    padding: '16px',
  },
  modalLabel: {
    display: 'block',
    fontSize: '11px',
    color: '#94a3b8',
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
    marginBottom: '4px',
  },
  modalPackageValue: {
    fontSize: '18px',
    fontWeight: '700',
    color: '#34d399',
  },
  modalMetaValue: {
    fontSize: '14px',
    fontWeight: '500',
    color: '#f8fafc',
  },
  modalSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  modalSectionHeading: {
    margin: 0,
    fontSize: '14px',
    fontWeight: '600',
    color: '#cbd5e1',
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  descriptionBox: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '14px 16px',
    fontSize: '14px',
    lineHeight: '1.6',
    color: '#cbd5e1',
    whiteSpace: 'pre-wrap',
  },
  infoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '12px',
  },
  infoItem: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '10px 14px',
    display: 'flex',
    flexDirection: 'column',
    gap: '3px',
  },
  infoLabel: {
    fontSize: '12px',
    color: '#94a3b8',
  },
  infoValue: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#f8fafc',
  },
  companyInfoCard: {
    background: '#0f172a',
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '14px 16px',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  companyInfoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: '12px',
  },
  companyDescriptionText: {
    margin: 0,
    fontSize: '13px',
    color: '#94a3b8',
    lineHeight: '1.5',
    borderTop: '1px solid #1e293b',
    paddingTop: '10px',
  },
  modalLink: {
    color: '#60a5fa',
    textDecoration: 'none',
    fontWeight: '500',
    fontSize: '14px',
  },
  eligibilityNotice: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    background: 'rgba(59, 130, 246, 0.1)',
    border: '1px solid rgba(59, 130, 246, 0.3)',
    borderRadius: '8px',
    padding: '12px 16px',
  },
  noticeIcon: {
    width: '24px',
    height: '24px',
    borderRadius: '50%',
    background: 'rgba(59, 130, 246, 0.2)',
    color: '#60a5fa',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '13px',
    fontWeight: '700',
  },
  noticeTitle: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#93c5fd',
  },
  noticeText: {
    margin: '2px 0 0 0',
    fontSize: '12px',
    color: '#cbd5e1',
  },
  modalFooter: {
    display: 'flex',
    justifyContent: 'flex-end',
    borderTop: '1px solid #334155',
    paddingTop: '16px',
  },
  closeFooterButton: {
    background: '#334155',
    color: '#f8fafc',
    border: 'none',
    padding: '10px 20px',
    borderRadius: '8px',
    fontWeight: '500',
    fontSize: '14px',
    cursor: 'pointer',
  },
};
