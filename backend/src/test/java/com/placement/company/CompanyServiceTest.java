package com.placement.company;

import com.placement.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company sampleCompany;

    @BeforeEach
    void setUp() {
        sampleCompany = new Company(
                1L,
                "Acme Corp",
                "Leading Tech Solutions",
                "IT",
                "https://acme.example.com",
                "Bangalore",
                "careers@acme.example.com",
                "+91 9876543210",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Create company succeeds when company name is unique")
    void createCompany_Success() {
        when(companyRepository.findByNameIgnoreCase("Acme Corp")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);

        Company created = companyService.createCompany(sampleCompany);

        assertNotNull(created);
        assertEquals("Acme Corp", created.getName());
        verify(companyRepository, times(1)).save(sampleCompany);
    }

    @Test
    @DisplayName("Create company fails when company name already exists")
    void createCompany_DuplicateName() {
        when(companyRepository.findByNameIgnoreCase("Acme Corp")).thenReturn(Optional.of(sampleCompany));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                companyService.createCompany(sampleCompany));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Get company by ID returns company when found")
    void getCompanyById_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));

        Company found = companyService.getCompanyById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    @DisplayName("Get company by ID throws ResourceNotFoundException when not found")
    void getCompanyById_NotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                companyService.getCompanyById(99L));
        assertTrue(ex.getMessage().contains("Company not found with id: 99"));
    }

    @Test
    @DisplayName("Delete company deletes existing company")
    void deleteCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        doNothing().when(companyRepository).delete(sampleCompany);

        companyService.deleteCompany(1L);

        verify(companyRepository, times(1)).delete(sampleCompany);
    }

    @Test
    @DisplayName("Search companies by name returns matching list")
    void searchCompaniesByName_Success() {
        when(companyRepository.findByNameContainingIgnoreCase("Acme")).thenReturn(List.of(sampleCompany));

        List<Company> results = companyService.searchCompaniesByName("Acme");

        assertEquals(1, results.size());
        assertEquals("Acme Corp", results.get(0).getName());
    }
}
