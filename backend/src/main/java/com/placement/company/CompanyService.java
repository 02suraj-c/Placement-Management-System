package com.placement.company;

import com.placement.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Company management.
 *
 * Package  : com.placement.company
 * Owner    : Person 3
 */
@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /**
     * Creates a new company record.
     * Validates that no company with the same name already exists.
     */
    public Company createCompany(Company company) {
        companyRepository.findByNameIgnoreCase(company.getName()).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "A company with the name '" + existing.getName() + "' already exists."
            );
        });
        return companyRepository.save(company);
    }

    /**
     * Retrieves all company records in the database.
     */
    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Retrieves a single company by its primary key.
     * Throws ResourceNotFoundException if not found.
     */
    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    /**
     * Updates an existing company's fields.
     */
    public Company updateCompany(Long id, Company updated) {
        Company existing = getCompanyById(id);

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setIndustry(updated.getIndustry());
        existing.setWebsite(updated.getWebsite());
        existing.setLocation(updated.getLocation());
        existing.setContactEmail(updated.getContactEmail());
        existing.setContactPhone(updated.getContactPhone());

        if (updated.getActive() != null) {
            existing.setActive(updated.getActive());
        }

        return companyRepository.save(existing);
    }

    /**
     * Deletes a company by its primary key.
     */
    public void deleteCompany(Long id) {
        Company existing = getCompanyById(id);
        companyRepository.delete(existing);
    }

    /**
     * Searches companies whose name contains the search string (case-insensitive).
     */
    @Transactional(readOnly = true)
    public List<Company> searchCompaniesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return companyRepository.findAll();
        }
        return companyRepository.findByNameContainingIgnoreCase(name.trim());
    }

    /**
     * Retrieves companies filtered by their active status.
     */
    @Transactional(readOnly = true)
    public List<Company> getCompaniesByActiveStatus(Boolean active) {
        return companyRepository.findByActive(active);
    }
}
