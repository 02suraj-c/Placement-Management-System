import { authService } from './authService';

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Internal helper for issuing authenticated HTTP requests using native fetch.
 *
 * @param {string} endpoint - Relative API endpoint starting with '/'
 * @param {object} options - Fetch options (method, headers, body)
 * @returns {Promise<any>} Parsed response data or null
 */
async function request(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const token = authService.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  // Handle 204 No Content
  if (response.status === 204) {
    return null;
  }

  // Handle non-2xx error responses
  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}: Request failed`;
    try {
      const errorData = await response.json();
      if (errorData) {
        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (typeof errorData === 'string') {
          errorMessage = errorData;
        }

        // Append field validation errors if provided by GlobalExceptionHandler
        if (errorData.fieldErrors && typeof errorData.fieldErrors === 'object') {
          const fieldDetails = Object.entries(errorData.fieldErrors)
            .map(([field, msg]) => `${field}: ${msg}`)
            .join(', ');
          if (fieldDetails) {
            errorMessage = `${errorMessage} (${fieldDetails})`;
          }
        }
      }
    } catch {
      // Body may not be valid JSON or may be empty; retain status-based message
    }
    throw new Error(errorMessage);
  }

  // Parse JSON response if present
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

// =============================================================================
// COMPANY APIs
// =============================================================================

/**
 * Retrieves list of all companies.
 *
 * @param {boolean|null} active - Optional boolean filter for active status
 * @returns {Promise<Array>} List of companies
 */
export async function getCompanies(active = null) {
  let endpoint = '/companies';
  if (active !== null && active !== undefined) {
    endpoint += `?active=${encodeURIComponent(Boolean(active))}`;
  }
  return request(endpoint, { method: 'GET' });
}

/**
 * Retrieves a company by primary key ID.
 *
 * @param {number|string} id - Company ID
 * @returns {Promise<object>} Company record
 */
export async function getCompanyById(id) {
  return request(`/companies/${encodeURIComponent(id)}`, { method: 'GET' });
}

/**
 * Searches companies by name (case-insensitive partial match).
 *
 * @param {string} name - Search keyword
 * @returns {Promise<Array>} Matching companies
 */
export async function searchCompanies(name) {
  const query = encodeURIComponent(name || '');
  return request(`/companies/search?name=${query}`, { method: 'GET' });
}

/**
 * Creates a new company record.
 *
 * @param {object} companyData - Company payload
 * @returns {Promise<object>} Persisted company record
 */
export async function createCompany(companyData) {
  return request('/companies', {
    method: 'POST',
    body: JSON.stringify(companyData),
  });
}

/**
 * Updates an existing company record.
 *
 * @param {number|string} id - Company ID
 * @param {object} companyData - Updated company payload
 * @returns {Promise<object>} Updated company record
 */
export async function updateCompany(id, companyData) {
  return request(`/companies/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(companyData),
  });
}

/**
 * Deletes a company record by primary key ID.
 *
 * @param {number|string} id - Company ID
 * @returns {Promise<null>}
 */
export async function deleteCompany(id) {
  return request(`/companies/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  });
}

// =============================================================================
// PLACEMENT DRIVE APIs
// =============================================================================

/**
 * Retrieves all placement drives.
 *
 * @returns {Promise<Array>} List of placement drives
 */
export async function getPlacementDrives() {
  return request('/placement-drives', { method: 'GET' });
}

/**
 * Retrieves a placement drive by primary key ID.
 *
 * @param {number|string} id - Placement drive ID
 * @returns {Promise<object>} Placement drive record
 */
export async function getPlacementDriveById(id) {
  return request(`/placement-drives/${encodeURIComponent(id)}`, { method: 'GET' });
}

/**
 * Retrieves all placement drives conducted by a specific company.
 *
 * @param {number|string} companyId - Company ID
 * @returns {Promise<Array>} Placement drives for the company
 */
export async function getDrivesByCompany(companyId) {
  return request(`/placement-drives/company/${encodeURIComponent(companyId)}`, {
    method: 'GET',
  });
}

/**
 * Retrieves placement drives filtered by operational status.
 *
 * @param {string} status - Status value (UPCOMING, ONGOING, COMPLETED, CANCELLED)
 * @returns {Promise<Array>} Matching placement drives
 */
export async function getDrivesByStatus(status) {
  return request(`/placement-drives/status/${encodeURIComponent(status)}`, {
    method: 'GET',
  });
}

/**
 * Searches placement drives by job title keyword.
 *
 * @param {string} jobTitle - Job title search query
 * @returns {Promise<Array>} Matching placement drives
 */
export async function searchPlacementDrives(jobTitle) {
  const query = encodeURIComponent(jobTitle || '');
  return request(`/placement-drives/search?jobTitle=${query}`, {
    method: 'GET',
  });
}

/**
 * Creates a new placement drive.
 *
 * @param {object} driveData - Drive payload
 * @returns {Promise<object>} Persisted placement drive record
 */
export async function createPlacementDrive(driveData) {
  return request('/placement-drives', {
    method: 'POST',
    body: JSON.stringify(driveData),
  });
}

/**
 * Updates an existing placement drive record.
 *
 * @param {number|string} id - Drive ID
 * @param {object} driveData - Updated drive payload
 * @returns {Promise<object>} Updated placement drive record
 */
export async function updatePlacementDrive(id, driveData) {
  return request(`/placement-drives/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: JSON.stringify(driveData),
  });
}

/**
 * Deletes a placement drive by primary key ID.
 *
 * @param {number|string} id - Drive ID
 * @returns {Promise<null>}
 */
export async function deletePlacementDrive(id) {
  return request(`/placement-drives/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  });
}

// =============================================================================
// ELIGIBILITY APIs
// =============================================================================

/**
 * Checks eligibility of a student for a specific placement drive.
 *
 * Calls: GET /api/eligibility/check?studentId={studentId}&driveId={driveId}
 *
 * @param {number|string} studentId - Student primary key ID
 * @param {number|string} driveId   - Placement drive primary key ID
 * @returns {Promise<object>} EligibilityCheckResponse with eligible flag, reasons, and details
 */
export async function checkEligibility(studentId, driveId) {
  const params = new URLSearchParams({
    studentId: encodeURIComponent(studentId),
    driveId: encodeURIComponent(driveId),
  });
  return request(`/eligibility/check?${params.toString()}`, { method: 'GET' });
}

// Default export consolidating all API functions
export const placementService = {
  getCompanies,
  getCompanyById,
  searchCompanies,
  createCompany,
  updateCompany,
  deleteCompany,
  getPlacementDrives,
  getPlacementDriveById,
  getDrivesByCompany,
  getDrivesByStatus,
  searchPlacementDrives,
  createPlacementDrive,
  updatePlacementDrive,
  deletePlacementDrive,
  checkEligibility,
};

export default placementService;
