package org.example.cli;

import org.example.exception.PlacementException;
import org.example.model.*;
import org.example.service.PlacementManagementSystem;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Interactive console-based runner for the Placement Management System.
 */
public class PlacementConsoleApp {

    private final PlacementManagementSystem pms;
    private final Scanner scanner;
    private final PrintStream out;

    public PlacementConsoleApp() {
        this(new PlacementManagementSystem(), System.in, System.out);
    }

    public PlacementConsoleApp(PlacementManagementSystem pms, InputStream in, PrintStream out) {
        this.pms = Objects.requireNonNull(pms, "PlacementManagementSystem cannot be null");
        this.scanner = new Scanner(in);
        this.out = out != null ? out : System.out;
    }

    public void start() {
        out.println("==================================================");
        out.println("       PLACEMENT MANAGEMENT SYSTEM CONSOLE        ");
        out.println("==================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter choice");
            try {
                switch (choice) {
                    case "1" -> handleCompanyMenu();
                    case "2" -> handleDriveMenu();
                    case "3" -> handleEligibilityCheck();
                    case "4" -> handleApplicationMenu();
                    case "5" -> handleInterviewRoundMenu();
                    case "6" -> handleStudentMenu();
                    case "7" -> handleSeedSampleData();
                    case "0" -> {
                        out.println("\nExiting Placement Management System. Goodbye!");
                        running = false;
                    }
                    default -> out.println("Invalid selection. Please enter a valid menu option.");
                }
            } catch (PlacementException pe) {
                out.println("\n[ERROR] " + pe.getMessage());
            } catch (Exception e) {
                out.println("\n[UNEXPECTED ERROR] " + e.getMessage());
            }
        }
    }

    private void printMainMenu() {
        out.println("\n----------------- MAIN MENU -----------------");
        out.println("1. Company Management");
        out.println("2. Placement Drive Management");
        out.println("3. Student Eligibility Check");
        out.println("4. Application Management");
        out.println("5. Interview Round Management");
        out.println("6. Student Registration & Directory");
        out.println("7. Load Sample Demo Data (Quick Setup)");
        out.println("0. Exit");
        out.println("---------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // 1. COMPANY MANAGEMENT
    // -------------------------------------------------------------------------
    private void handleCompanyMenu() {
        boolean inMenu = true;
        while (inMenu) {
            out.println("\n--- Company Management ---");
            out.println("1. Add New Company");
            out.println("2. View All Companies");
            out.println("3. Search Companies by Name");
            out.println("4. Update Company Details");
            out.println("5. Remove Company");
            out.println("0. Back to Main Menu");

            String choice = prompt("Select option");
            switch (choice) {
                case "1" -> {
                    String name = prompt("Enter Company Name");
                    String desc = prompt("Enter Description");
                    String loc = prompt("Enter Location");
                    String web = prompt("Enter Website (e.g. https://company.com)");
                    Company company = pms.getCompanyService().addCompany(new Company(name, desc, loc, web));
                    out.println("Company registered successfully! Generated ID: " + company.getId());
                }
                case "2" -> {
                    List<Company> companies = pms.getCompanyService().getAllCompanies();
                    if (companies.isEmpty()) {
                        out.println("No companies registered yet.");
                    } else {
                        out.println("\n--- Registered Companies (" + companies.size() + ") ---");
                        for (Company c : companies) {
                            out.printf("• [%s] %s | Location: %s | Web: %s\n  Desc: %s\n",
                                    c.getId(), c.getName(), c.getLocation(), c.getWebsite(), c.getDescription());
                        }
                    }
                }
                case "3" -> {
                    String query = prompt("Enter search query (company name)");
                    List<Company> results = pms.getCompanyService().searchCompaniesByName(query);
                    out.println("Found " + results.size() + " matching companies:");
                    for (Company c : results) {
                        out.printf("• [%s] %s (%s)\n", c.getId(), c.getName(), c.getLocation());
                    }
                }
                case "4" -> {
                    String id = prompt("Enter Company ID to update");
                    Company company = pms.getCompanyService().getCompanyById(id);
                    out.println("Updating Company: " + company.getName());
                    String name = promptOptional("New Name", company.getName());
                    String desc = promptOptional("New Description", company.getDescription());
                    String loc = promptOptional("New Location", company.getLocation());
                    String web = promptOptional("New Website", company.getWebsite());

                    company.setName(name);
                    company.setDescription(desc);
                    company.setLocation(loc);
                    company.setWebsite(web);
                    pms.getCompanyService().updateCompany(company);
                    out.println("Company updated successfully!");
                }
                case "5" -> {
                    String id = prompt("Enter Company ID to remove");
                    pms.getCompanyService().removeCompany(id);
                    out.println("Company removed successfully.");
                }
                case "0" -> inMenu = false;
                default -> out.println("Invalid option. Please try again.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // 2. PLACEMENT DRIVE MANAGEMENT
    // -------------------------------------------------------------------------
    private void handleDriveMenu() {
        boolean inMenu = true;
        while (inMenu) {
            out.println("\n--- Placement Drive Management ---");
            out.println("1. Create Placement Drive");
            out.println("2. View All Placement Drives");
            out.println("3. View Drives by Company");
            out.println("4. Cancel a Placement Drive");
            out.println("0. Back to Main Menu");

            String choice = prompt("Select option");
            switch (choice) {
                case "1" -> {
                    String companyId = prompt("Enter Company ID");
                    String jobRole = prompt("Enter Job Role (e.g. Software Engineer)");
                    double ctc = promptDouble("Enter Package CTC (in LPA, e.g. 14.5)");
                    String desc = prompt("Enter Job Description");
                    LocalDate deadline = promptDate("Enter Application Deadline (YYYY-MM-DD)");
                    LocalDate driveDate = promptDate("Enter Drive Date (YYYY-MM-DD)");
                    double minCgpa = promptDouble("Enter Minimum CGPA required (0.0 - 10.0)");
                    int maxBacklogs = promptInt("Enter Maximum Allowed Backlogs");
                    String branchStr = prompt("Enter Eligible Branches (comma-separated, e.g. CSE, IT, ECE)");

                    Set<String> branches = new HashSet<>();
                    for (String b : branchStr.split(",")) {
                        if (!b.trim().isEmpty()) branches.add(b.trim());
                    }

                    PlacementDrive drive = new PlacementDrive(
                            null, companyId, jobRole, ctc, desc, deadline, driveDate, minCgpa, maxBacklogs, branches
                    );
                    PlacementDrive created = pms.getPlacementDriveService().createDrive(drive);
                    out.println("Placement Drive created successfully! Drive ID: " + created.getId());
                }
                case "2" -> {
                    List<PlacementDrive> drives = pms.getPlacementDriveService().getAllDrives();
                    if (drives.isEmpty()) {
                        out.println("No placement drives scheduled yet.");
                    } else {
                        out.println("\n--- All Placement Drives (" + drives.size() + ") ---");
                        for (PlacementDrive d : drives) {
                            printDriveSummary(d);
                        }
                    }
                }
                case "3" -> {
                    String companyId = prompt("Enter Company ID");
                    List<PlacementDrive> drives = pms.getPlacementDriveService().getDrivesByCompany(companyId);
                    out.println("Found " + drives.size() + " drives for Company " + companyId + ":");
                    for (PlacementDrive d : drives) {
                        printDriveSummary(d);
                    }
                }
                case "4" -> {
                    String driveId = prompt("Enter Drive ID to cancel");
                    PlacementDrive cancelled = pms.getPlacementDriveService().cancelDrive(driveId);
                    out.println("Drive " + cancelled.getId() + " status updated to CANCELLED.");
                }
                case "0" -> inMenu = false;
                default -> out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printDriveSummary(PlacementDrive d) {
        out.printf("• [%s] Role: %s | Company: %s | CTC: %.2f LPA | Status: %s\n" +
                        "  Deadline: %s | Drive Date: %s | Min CGPA: %.2f | Max Backlogs: %d | Branches: %s\n",
                d.getId(), d.getJobRole(), d.getCompanyId(), d.getPackageCTC(), d.getStatus(),
                d.getApplicationDeadline(), d.getDriveDate(), d.getMinimumCGPA(), d.getMaximumAllowedBacklogs(), d.getEligibleBranches());
    }

    // -------------------------------------------------------------------------
    // 3. STUDENT ELIGIBILITY CHECK
    // -------------------------------------------------------------------------
    private void handleEligibilityCheck() {
        out.println("\n--- Student Eligibility Check ---");
        String studentId = prompt("Enter Student ID");
        String driveId = prompt("Enter Placement Drive ID");

        EligibilityResult result = pms.getStudentEligibilityService().checkEligibility(studentId, driveId);
        out.println("\nResult: " + (result.isEligible() ? "ELIGIBLE" : "INELIGIBLE"));
        out.println("Details: " + result.getFormattedReason());
    }

    // -------------------------------------------------------------------------
    // 4. APPLICATION MANAGEMENT
    // -------------------------------------------------------------------------
    private void handleApplicationMenu() {
        boolean inMenu = true;
        while (inMenu) {
            out.println("\n--- Application Management ---");
            out.println("1. Apply for Placement Drive");
            out.println("2. View Applications by Student");
            out.println("3. View Applications by Drive");
            out.println("4. View All Applications");
            out.println("5. Update Application Status");
            out.println("0. Back to Main Menu");

            String choice = prompt("Select option");
            switch (choice) {
                case "1" -> {
                    String studentId = prompt("Enter Student ID");
                    String driveId = prompt("Enter Placement Drive ID");
                    Application app = pms.getApplicationService().applyForDrive(studentId, driveId);
                    out.println("Application submitted successfully! Application ID: " + app.getId() + " | Status: " + app.getStatus());
                }
                case "2" -> {
                    String studentId = prompt("Enter Student ID");
                    List<Application> apps = pms.getApplicationService().getApplicationsByStudent(studentId);
                    out.println("Applications for Student " + studentId + " (" + apps.size() + "):");
                    for (Application a : apps) {
                        out.printf("• [App ID: %s] Drive: %s | Applied: %s | Status: %s | Remarks: %s\n",
                                a.getId(), a.getDriveId(), a.getAppliedDate(), a.getStatus(), a.getRemarks());
                    }
                }
                case "3" -> {
                    String driveId = prompt("Enter Drive ID");
                    List<Application> apps = pms.getApplicationService().getApplicationsByDrive(driveId);
                    out.println("Applications for Drive " + driveId + " (" + apps.size() + "):");
                    for (Application a : apps) {
                        out.printf("• [App ID: %s] Student: %s | Applied: %s | Status: %s | Remarks: %s\n",
                                a.getId(), a.getStudentId(), a.getAppliedDate(), a.getStatus(), a.getRemarks());
                    }
                }
                case "4" -> {
                    List<Application> apps = pms.getApplicationService().getAllApplications();
                    out.println("Total Applications in System (" + apps.size() + "):");
                    for (Application a : apps) {
                        out.printf("• [App ID: %s] Student: %s -> Drive: %s | Status: %s\n",
                                a.getId(), a.getStudentId(), a.getDriveId(), a.getStatus());
                    }
                }
                case "5" -> {
                    String appId = prompt("Enter Application ID");
                    out.println("Select New Status: 1. APPLIED | 2. SHORTLISTED | 3. REJECTED | 4. SELECTED");
                    String sChoice = prompt("Choice");
                    ApplicationStatus newStatus = switch (sChoice) {
                        case "1" -> ApplicationStatus.APPLIED;
                        case "2" -> ApplicationStatus.SHORTLISTED;
                        case "3" -> ApplicationStatus.REJECTED;
                        case "4" -> ApplicationStatus.SELECTED;
                        default -> throw new PlacementException("Invalid status option");
                    };
                    String remarks = prompt("Enter Remarks / Feedback");
                    Application updated = pms.getApplicationService().updateApplicationStatus(appId, newStatus, remarks);
                    out.println("Application " + updated.getId() + " updated to status: " + updated.getStatus());
                }
                case "0" -> inMenu = false;
                default -> out.println("Invalid option. Please try again.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // 5. INTERVIEW ROUND MANAGEMENT
    // -------------------------------------------------------------------------
    private void handleInterviewRoundMenu() {
        boolean inMenu = true;
        while (inMenu) {
            out.println("\n--- Interview Round Management ---");
            out.println("1. Add Interview Round to Drive");
            out.println("2. List Rounds for a Drive");
            out.println("3. Record Candidate Round Result");
            out.println("4. View Candidate Progress in Drive");
            out.println("5. View All Candidates in a Round");
            out.println("0. Back to Main Menu");

            String choice = prompt("Select option");
            switch (choice) {
                case "1" -> {
                    String driveId = prompt("Enter Drive ID");
                    int roundNum = promptInt("Enter Round Number (0 for auto-assign)");
                    String name = prompt("Enter Round Name (e.g. Technical Round 1)");
                    out.println("Select Round Type: 1. ONLINE_ASSESSMENT | 2. TECHNICAL | 3. GROUP_DISCUSSION | 4. HR | 5. OTHER");
                    String tChoice = prompt("Choice");
                    RoundType type = switch (tChoice) {
                        case "1" -> RoundType.ONLINE_ASSESSMENT;
                        case "2" -> RoundType.TECHNICAL;
                        case "3" -> RoundType.GROUP_DISCUSSION;
                        case "4" -> RoundType.HR;
                        default -> RoundType.OTHER;
                    };
                    LocalDate date = promptDate("Enter Round Date (YYYY-MM-DD)");

                    InterviewRound round = new InterviewRound(roundNum, name, type, date);
                    InterviewRound created = pms.getInterviewRoundService().addRoundToDrive(driveId, round);
                    out.printf("Interview Round added! ID: %s | Round #%d: %s (%s)\n",
                            created.getId(), created.getRoundNumber(), created.getRoundName(), created.getRoundType());
                }
                case "2" -> {
                    String driveId = prompt("Enter Drive ID");
                    List<InterviewRound> rounds = pms.getInterviewRoundService().getRoundsByDrive(driveId);
                    out.println("Interview Rounds for Drive " + driveId + " (" + rounds.size() + "):");
                    for (InterviewRound r : rounds) {
                        out.printf("• [Round ID: %s] #%d: %s | Type: %s | Date: %s\n",
                                r.getId(), r.getRoundNumber(), r.getRoundName(), r.getRoundType(), r.getRoundDate());
                    }
                }
                case "3" -> {
                    String roundId = prompt("Enter Round ID");
                    String studentId = prompt("Enter Student ID");
                    out.println("Select Result: 1. CLEARED | 2. FAILED | 3. PENDING");
                    String rChoice = prompt("Choice");
                    RoundResult res = switch (rChoice) {
                        case "1" -> RoundResult.CLEARED;
                        case "2" -> RoundResult.FAILED;
                        case "3" -> RoundResult.PENDING;
                        default -> throw new PlacementException("Invalid result selection");
                    };
                    String remarks = prompt("Enter Evaluator Remarks");

                    CandidateRoundEvaluation eval = pms.getInterviewRoundService().recordCandidateResult(roundId, studentId, res, remarks);
                    out.println("Result recorded successfully! Outcome: " + eval.getResult());
                }
                case "4" -> {
                    String driveId = prompt("Enter Drive ID");
                    String studentId = prompt("Enter Student ID");
                    List<CandidateRoundEvaluation> evals = pms.getInterviewRoundService().getCandidateProgress(driveId, studentId);
                    out.println("Progression for Student " + studentId + " in Drive " + driveId + " (" + evals.size() + " rounds evaluated):");
                    for (CandidateRoundEvaluation e : evals) {
                        out.printf("• Round: %s | Result: %s | Remarks: %s | Date: %s\n",
                                e.getRoundId(), e.getResult(), e.getRemarks(), e.getEvaluationDate());
                    }
                }
                case "5" -> {
                    String roundId = prompt("Enter Round ID");
                    List<CandidateRoundEvaluation> evals = pms.getInterviewRoundService().getCandidatesByRound(roundId);
                    out.println("Candidates evaluated for Round " + roundId + " (" + evals.size() + "):");
                    for (CandidateRoundEvaluation e : evals) {
                        out.printf("• Student: %s | Result: %s | Remarks: %s\n",
                                e.getStudentId(), e.getResult(), e.getRemarks());
                    }
                }
                case "0" -> inMenu = false;
                default -> out.println("Invalid option. Please try again.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // 6. STUDENT MANAGEMENT (HELPER / DIRECTORY)
    // -------------------------------------------------------------------------
    private void handleStudentMenu() {
        boolean inMenu = true;
        while (inMenu) {
            out.println("\n--- Student Registration & Directory ---");
            out.println("1. Register a Student");
            out.println("2. View All Registered Students");
            out.println("0. Back to Main Menu");

            String choice = prompt("Select option");
            switch (choice) {
                case "1" -> {
                    String id = prompt("Enter Student ID (e.g. STU-101)");
                    String name = prompt("Enter Student Full Name");
                    String email = prompt("Enter Student Email");
                    String branch = prompt("Enter Branch (e.g. CSE, IT, ECE)");
                    double cgpa = promptDouble("Enter CGPA (0.0 - 10.0)");
                    int backlogs = promptInt("Enter Active Backlog Count");

                    Student s = new Student(id, name, email, branch, cgpa, backlogs);
                    pms.getStudentRepository().save(s);
                    out.println("Student registered successfully! ID: " + s.getId());
                }
                case "2" -> {
                    List<Student> students = pms.getStudentRepository().findAll();
                    if (students.isEmpty()) {
                        out.println("No students registered yet.");
                    } else {
                        out.println("\n--- Registered Students (" + students.size() + ") ---");
                        for (Student s : students) {
                            out.printf("• [%s] %s | %s | Branch: %s | CGPA: %.2f | Backlogs: %d\n",
                                    s.getId(), s.getName(), s.getEmail(), s.getBranch(), s.getCgpa(), s.getBacklogCount());
                        }
                    }
                }
                case "0" -> inMenu = false;
                default -> out.println("Invalid option. Please try again.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // 7. LOAD SAMPLE DEMO DATA
    // -------------------------------------------------------------------------
    public void handleSeedSampleData() {
        // 1. Companies
        Company c1 = pms.getCompanyService().addCompany(
                new Company("Google", "Global technology company specializing in internet-related services", "Bangalore, India", "https://google.com")
        );
        Company c2 = pms.getCompanyService().addCompany(
                new Company("Microsoft", "Multinational technology corporation providing cloud and AI solutions", "Hyderabad, India", "https://microsoft.com")
        );

        // 2. Drives
        PlacementDrive d1 = pms.getPlacementDriveService().createDrive(new PlacementDrive(
                null, c1.getId(), "Software Development Engineer", 22.0,
                "Backend services and systems engineering",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(20),
                7.5, 0, new HashSet<>(Arrays.asList("CSE", "IT", "ECE"))
        ));

        PlacementDrive d2 = pms.getPlacementDriveService().createDrive(new PlacementDrive(
                null, c2.getId(), "Cloud & DevOps Engineer", 18.0,
                "Azure cloud infrastructure engineering",
                LocalDate.now().plusDays(14), LocalDate.now().plusDays(25),
                7.0, 1, new HashSet<>(Arrays.asList("CSE", "IT"))
        ));

        // 3. Interview Rounds for Drive 1
        InterviewRound r1 = pms.getInterviewRoundService().addRoundToDrive(d1.getId(),
                new InterviewRound(1, "Online Coding Assessment", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(12)));
        InterviewRound r2 = pms.getInterviewRoundService().addRoundToDrive(d1.getId(),
                new InterviewRound(2, "Technical Problem Solving & DSA", RoundType.TECHNICAL, LocalDate.now().plusDays(15)));

        // 4. Students
        Student s1 = pms.getStudentRepository().save(new Student("STU-1", "Aarav Sharma", "aarav@college.edu", "CSE", 8.8, 0));
        Student s2 = pms.getStudentRepository().save(new Student("STU-2", "Priya Verma", "priya@college.edu", "IT", 7.2, 0));
        Student s3 = pms.getStudentRepository().save(new Student("STU-3", "Rohan Mehta", "rohan@college.edu", "MECH", 6.2, 2));

        out.println("\n✓ Sample demo data successfully loaded!");
        out.printf("  • 2 Companies: %s (%s), %s (%s)\n", c1.getName(), c1.getId(), c2.getName(), c2.getId());
        out.printf("  • 2 Drives: %s (%s), %s (%s)\n", d1.getJobRole(), d1.getId(), d2.getJobRole(), d2.getId());
        out.printf("  • 2 Rounds for Drive %s: %s (%s), %s (%s)\n", d1.getId(), r1.getRoundName(), r1.getId(), r2.getRoundName(), r2.getId());
        out.printf("  • 3 Students: %s (Eligible for all), %s (Eligible for Drive 2), %s (Ineligible)\n",
                s1.getName(), s2.getName(), s3.getName());
    }

    // -------------------------------------------------------------------------
    // INPUT HELPERS
    // -------------------------------------------------------------------------
    private String prompt(String message) {
        out.print(message + ": ");
        String line = scanner.nextLine();
        return line != null ? line.trim() : "";
    }

    private String promptOptional(String message, String currentVal) {
        out.print(message + " [" + currentVal + "]: ");
        String line = scanner.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return currentVal;
        }
        return line.trim();
    }

    private double promptDouble(String message) {
        while (true) {
            String val = prompt(message);
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException e) {
                out.println("Invalid numeric input. Please enter a valid decimal number.");
            }
        }
    }

    private int promptInt(String message) {
        while (true) {
            String val = prompt(message);
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                out.println("Invalid integer input. Please enter a valid integer.");
            }
        }
    }

    private LocalDate promptDate(String message) {
        while (true) {
            String val = prompt(message);
            try {
                return LocalDate.parse(val);
            } catch (DateTimeParseException e) {
                out.println("Invalid date format. Please use YYYY-MM-DD (e.g. " + LocalDate.now().plusDays(7) + ").");
            }
        }
    }
}
