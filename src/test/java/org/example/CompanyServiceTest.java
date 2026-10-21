package org.example;

import org.example.exception.CompanyNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.Company;
import org.example.model.PlacementDrive;
import org.example.repository.InMemoryCompanyRepository;
import org.example.repository.InMemoryPlacementDriveRepository;
import org.example.service.CompanyService;
import org.example.service.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompanyServiceTest {

    private InMemoryCompanyRepository companyRepository;
    private InMemoryPlacementDriveRepository driveRepository;
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyRepository = new InMemoryCompanyRepository();
        driveRepository = new InMemoryPlacementDriveRepository();
        companyService = new CompanyServiceImpl(companyRepository, driveRepository);
    }

    @Test
    @DisplayName("Should add company successfully with valid details")
    void testAddCompanySuccess() {
        Company company = new Company("Google", "Tech giant", "Mountain View, CA", "https://google.com");
        Company created = companyService.addCompany(company);

        assertNotNull(created.getId());
        assertEquals("Google", created.getName());
        assertEquals("Tech giant", created.getDescription());
        assertEquals("Mountain View, CA", created.getLocation());
        assertEquals("https://google.com", created.getWebsite());

        Company fetched = companyService.getCompanyById(created.getId());
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    @DisplayName("Should reject company with empty name")
    void testAddCompanyEmptyNameFails() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                new Company("", "Desc", "Bangalore", "https://example.com"));
        assertTrue(ex.getMessage().contains("Company name"));
    }

    @Test
    @DisplayName("Should reject company with invalid website format")
    void testAddCompanyInvalidWebsite() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                new Company("MyCompany", "Desc", "Delhi", "invalid-web-site"));
        assertTrue(ex.getMessage().contains("Invalid website URL format"));
    }

    @Test
    @DisplayName("Should view all registered companies")
    void testGetAllCompanies() {
        companyService.addCompany(new Company("Amazon", "E-commerce & Cloud", "Seattle", "https://amazon.com"));
        companyService.addCompany(new Company("Microsoft", "Software & Cloud", "Redmond", "https://microsoft.com"));

        List<Company> companies = companyService.getAllCompanies();
        assertEquals(2, companies.size());
    }

    @Test
    @DisplayName("Should search companies by name case-insensitively")
    void testSearchCompaniesByName() {
        companyService.addCompany(new Company("Infosys", "IT Services", "Bangalore", "https://infosys.com"));
        companyService.addCompany(new Company("InfoTech Solutions", "Consulting", "Hyderabad", "https://infotech.com"));
        companyService.addCompany(new Company("TCS", "IT Services", "Mumbai", "https://tcs.com"));

        List<Company> results = companyService.searchCompaniesByName("info");
        assertEquals(2, results.size());

        List<Company> tcsResults = companyService.searchCompaniesByName("TCS");
        assertEquals(1, tcsResults.size());
        assertEquals("TCS", tcsResults.get(0).getName());
    }

    @Test
    @DisplayName("Should update existing company")
    void testUpdateCompany() {
        Company company = companyService.addCompany(new Company("Tata Motors", "Automotive", "Pune", "https://tatamotors.com"));

        company.setDescription("Leading Automotive & EV Manufacturer");
        company.setLocation("Mumbai");
        Company updated = companyService.updateCompany(company);

        assertEquals("Leading Automotive & EV Manufacturer", updated.getDescription());
        assertEquals("Mumbai", updated.getLocation());

        Company fetched = companyService.getCompanyById(company.getId());
        assertEquals("Leading Automotive & EV Manufacturer", fetched.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent company")
    void testUpdateNonExistentCompanyThrows() {
        Company company = new Company("Ghost Corp", "Ghost Town", "Delhi", "https://ghost.com");
        company.setId("NON_EXISTENT_ID");
        assertThrows(CompanyNotFoundException.class, () -> companyService.updateCompany(company));
    }

    @Test
    @DisplayName("Should remove company when no placement drives are linked")
    void testRemoveCompanySuccess() {
        Company company = companyService.addCompany(new Company("Adobe", "Creative Suite", "Noida", "https://adobe.com"));
        String id = company.getId();

        companyService.removeCompany(id);
        assertThrows(CompanyNotFoundException.class, () -> companyService.getCompanyById(id));
    }

    @Test
    @DisplayName("Should prevent removing company when placement drives are linked")
    void testRemoveCompanyWithLinkedDrivesFails() {
        Company company = companyService.addCompany(new Company("Goldman Sachs", "Investment Banking", "Bangalore", "https://gs.com"));

        PlacementDrive drive = new PlacementDrive();
        drive.setCompanyId(company.getId());
        drive.setJobRole("Analyst");
        driveRepository.save(drive);

        ValidationException ex = assertThrows(ValidationException.class, () -> companyService.removeCompany(company.getId()));
        assertTrue(ex.getMessage().contains("because placement drives are linked to it"));
    }
}
