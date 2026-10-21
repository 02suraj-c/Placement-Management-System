package org.example.service;

import org.example.exception.CompanyNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.Company;
import org.example.repository.CompanyRepository;
import org.example.repository.PlacementDriveRepository;
import org.example.util.ValidationUtil;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of CompanyService.
 */
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository driveRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this(companyRepository, null);
    }

    public CompanyServiceImpl(CompanyRepository companyRepository, PlacementDriveRepository driveRepository) {
        this.companyRepository = Objects.requireNonNull(companyRepository, "CompanyRepository must not be null");
        this.driveRepository = driveRepository;
    }

    @Override
    public Company addCompany(Company company) {
        if (company == null) {
            throw new ValidationException("Company details must not be null");
        }
        ValidationUtil.requireNonBlank(company.getName(), "Company name");
        ValidationUtil.validateWebsite(company.getWebsite());

        return companyRepository.save(company);
    }

    @Override
    public Company getCompanyById(String companyId) {
        ValidationUtil.requireNonBlank(companyId, "Company ID");
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + companyId));
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public List<Company> searchCompaniesByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return companyRepository.findAll();
        }
        return companyRepository.findByNameContainingIgnoreCase(query);
    }

    @Override
    public Company updateCompany(Company company) {
        if (company == null) {
            throw new ValidationException("Company cannot be null for update");
        }
        ValidationUtil.requireNonBlank(company.getId(), "Company ID");
        ValidationUtil.requireNonBlank(company.getName(), "Company name");
        ValidationUtil.validateWebsite(company.getWebsite());

        Company existing = companyRepository.findById(company.getId())
                .orElseThrow(() -> new CompanyNotFoundException("Cannot update. Company not found with ID: " + company.getId()));

        existing.setName(company.getName().trim());
        existing.setDescription(company.getDescription() != null ? company.getDescription().trim() : "");
        existing.setLocation(company.getLocation() != null ? company.getLocation().trim() : "");
        existing.setWebsite(ValidationUtil.validateWebsite(company.getWebsite()));

        return companyRepository.save(existing);
    }

    @Override
    public void removeCompany(String companyId) {
        ValidationUtil.requireNonBlank(companyId, "Company ID");
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Cannot remove. Company not found with ID: " + companyId);
        }

        if (driveRepository != null) {
            boolean hasDrives = !driveRepository.findByCompanyId(companyId).isEmpty();
            if (hasDrives) {
                throw new ValidationException("Cannot remove company with ID " + companyId +
                        " because placement drives are linked to it.");
            }
        }

        companyRepository.deleteById(companyId);
    }
}
