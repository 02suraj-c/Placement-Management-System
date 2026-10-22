package com.placement.company;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Company Management endpoints.
 *
 * Base Path: /api/companies
 * Package  : com.placement.company
 * Owner    : Person 3
 *
 * Security:
 *   - Read operations   : isAuthenticated()
 *   - Create/Update     : hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR')
 *   - Delete            : hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO')
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // =========================================================================
    // POST /api/companies
    // =========================================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR')")
    public ResponseEntity<Company> createCompany(@Valid @RequestBody Company company) {
        Company created = companyService.createCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================================================================
    // GET /api/companies
    // =========================================================================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Company>> getAllCompanies(
            @RequestParam(required = false) Boolean active) {

        List<Company> companies;
        if (active != null) {
            companies = companyService.getCompaniesByActiveStatus(active);
        } else {
            companies = companyService.getAllCompanies();
        }
        return ResponseEntity.ok(companies);
    }

    // =========================================================================
    // GET /api/companies/{id}
    // =========================================================================
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    // =========================================================================
    // GET /api/companies/search?name={name}
    // =========================================================================
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Company>> searchCompaniesByName(
            @RequestParam(required = false, defaultValue = "") String name) {

        List<Company> companies = companyService.searchCompaniesByName(name);
        return ResponseEntity.ok(companies);
    }

    // =========================================================================
    // PUT /api/companies/{id}
    // =========================================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR')")
    public ResponseEntity<Company> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody Company company) {

        Company updated = companyService.updateCompany(id, company);
        return ResponseEntity.ok(updated);
    }

    // =========================================================================
    // DELETE /api/companies/{id}
    // =========================================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
