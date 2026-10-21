package org.example.service;

import org.example.repository.*;

/**
 * Unified facade providing access to all Placement Management modules.
 * Suitable for clean integration with teammate modules or CLI/API entry points.
 */
public class PlacementManagementSystem {
    private final CompanyRepository companyRepository;
    private final PlacementDriveRepository driveRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRoundRepository interviewRoundRepository;

    private final CompanyService companyService;
    private final PlacementDriveService placementDriveService;
    private final StudentEligibilityService studentEligibilityService;
    private final ApplicationService applicationService;
    private final InterviewRoundService interviewRoundService;

    /**
     * Constructs a PlacementManagementSystem initialized with thread-safe in-memory repositories.
     */
    public PlacementManagementSystem() {
        this.companyRepository = new InMemoryCompanyRepository();
        this.driveRepository = new InMemoryPlacementDriveRepository();
        this.studentRepository = new InMemoryStudentRepository();
        this.applicationRepository = new InMemoryApplicationRepository();
        this.interviewRoundRepository = new InMemoryInterviewRoundRepository();

        this.companyService = new CompanyServiceImpl(companyRepository, driveRepository);
        this.placementDriveService = new PlacementDriveServiceImpl(driveRepository, companyRepository);
        this.studentEligibilityService = new StudentEligibilityServiceImpl(studentRepository, driveRepository);
        this.applicationService = new ApplicationServiceImpl(applicationRepository, driveRepository, studentRepository, studentEligibilityService);
        this.interviewRoundService = new InterviewRoundServiceImpl(interviewRoundRepository, driveRepository, studentRepository, applicationRepository);
    }

    /**
     * Constructs a PlacementManagementSystem with custom repositories (e.g. for database integration or mocking).
     */
    public PlacementManagementSystem(CompanyRepository companyRepository,
                                   PlacementDriveRepository driveRepository,
                                   StudentRepository studentRepository,
                                   ApplicationRepository applicationRepository,
                                   InterviewRoundRepository interviewRoundRepository) {
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRoundRepository = interviewRoundRepository;

        this.companyService = new CompanyServiceImpl(companyRepository, driveRepository);
        this.placementDriveService = new PlacementDriveServiceImpl(driveRepository, companyRepository);
        this.studentEligibilityService = new StudentEligibilityServiceImpl(studentRepository, driveRepository);
        this.applicationService = new ApplicationServiceImpl(applicationRepository, driveRepository, studentRepository, studentEligibilityService);
        this.interviewRoundService = new InterviewRoundServiceImpl(interviewRoundRepository, driveRepository, studentRepository, applicationRepository);
    }

    public CompanyService getCompanyService() {
        return companyService;
    }

    public PlacementDriveService getPlacementDriveService() {
        return placementDriveService;
    }

    public StudentEligibilityService getStudentEligibilityService() {
        return studentEligibilityService;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    public InterviewRoundService getInterviewRoundService() {
        return interviewRoundService;
    }

    public CompanyRepository getCompanyRepository() {
        return companyRepository;
    }

    public PlacementDriveRepository getDriveRepository() {
        return driveRepository;
    }

    public StudentRepository getStudentRepository() {
        return studentRepository;
    }

    public ApplicationRepository getApplicationRepository() {
        return applicationRepository;
    }

    public InterviewRoundRepository getInterviewRoundRepository() {
        return interviewRoundRepository;
    }
}
