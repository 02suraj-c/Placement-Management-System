package org.example.service;

import org.example.model.Company;

import java.util.List;

/**
 * Service managing company registrations and profiles.
 */
public interface CompanyService {
    Company addCompany(Company company);
    Company getCompanyById(String companyId);
    List<Company> getAllCompanies();
    List<Company> searchCompaniesByName(String query);
    Company updateCompany(Company company);
    void removeCompany(String companyId);
}
