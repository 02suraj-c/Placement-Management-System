// Initial Mock Seed Data adhering to backend models
const INITIAL_USERS = [
  {
    id: 1,
    name: "Aarav Sharma",
    email: "student@placement.edu",
    password: "student123",
    role: "ROLE_STUDENT",
    department: "Computer Science & Engineering",
    cgpa: 8.85,
    backlogs: 0,
    rollNumber: "CS2026-042",
    batch: "2026",
    phone: "+91 98765 43210",
    skills: ["Java", "Spring Boot", "React.js", "MySQL", "Data Structures", "Docker"],
    resumeUrl: "/resumes/Aarav_Sharma_Resume.pdf"
  },
  {
    id: 2,
    name: "Dr. Rajesh Verma",
    email: "officer@placement.edu",
    password: "officer123",
    role: "ROLE_PLACEMENT_OFFICER",
    department: "Central Placement Cell",
    designation: "Head Training & Placement Officer",
    phone: "+91 98765 11223"
  },
  {
    id: 3,
    name: "Prof. Sunita Rao",
    email: "faculty@placement.edu",
    password: "faculty123",
    role: "ROLE_FACULTY",
    department: "Computer Science & Engineering",
    designation: "Associate Professor & Placement Coordinator",
    phone: "+91 98765 33445"
  },
  {
    id: 4,
    name: "Dr. K. Venkatraman",
    email: "hod@placement.edu",
    password: "hod123",
    role: "ROLE_HOD",
    department: "Computer Science & Engineering",
    designation: "Head of Department (CSE)",
    phone: "+91 98765 55667"
  },
  {
    id: 5,
    name: "System Administrator",
    email: "admin@placement.edu",
    password: "admin123",
    role: "ROLE_ADMIN",
    department: "IT Infrastructure",
    phone: "+91 98765 99887"
  }
];

const INITIAL_DRIVES = [
  {
    id: 101,
    company: "Tata Consultancy Services (TCS)",
    role: "Digital Software Engineer",
    packageLpa: 7.5,
    minCgpa: 7.0,
    maxBacklogs: 0,
    allowedBranches: ["CSE", "IT", "ECE"],
    location: "Bengaluru / Pune / Hyderabad",
    deadline: "2026-10-28T23:59:59",
    driveDate: "2026-11-05",
    description: "Design and implement modern enterprise cloud applications using Java, Spring Microservices, and Cloud Native stacks.",
    status: "ACTIVE",
    totalPositions: 45
  },
  {
    id: 102,
    company: "Infosys",
    role: "Specialist Programmer (Power Programmer)",
    packageLpa: 9.5,
    minCgpa: 7.5,
    maxBacklogs: 0,
    allowedBranches: ["CSE", "IT"],
    location: "Bengaluru / Mysuru / Hyderabad",
    deadline: "2026-10-30T23:59:59",
    driveDate: "2026-11-08",
    description: "Full stack engineering, competitive algorithms, high performance data engineering, and distributed architectures.",
    status: "ACTIVE",
    totalPositions: 25
  },
  {
    id: 103,
    company: "Wipro",
    role: "Project Engineer (Turbo Cadre)",
    packageLpa: 6.5,
    minCgpa: 6.5,
    maxBacklogs: 1,
    allowedBranches: ["CSE", "IT", "ECE", "EEE"],
    location: "Hyderabad / Chennai",
    deadline: "2026-11-02T23:59:59",
    driveDate: "2026-11-12",
    description: "Cloud migrations, API engineering, DevOps automated delivery pipelines, and quality assurance automation.",
    status: "ACTIVE",
    totalPositions: 30
  },
  {
    id: 104,
    company: "HCLTech",
    role: "Graduate Engineer Trainee",
    packageLpa: 5.5,
    minCgpa: 6.0,
    maxBacklogs: 1,
    allowedBranches: ["CSE", "IT", "ECE", "MECH"],
    location: "Noida / Chennai / Lucknow",
    deadline: "2026-11-05T23:59:59",
    driveDate: "2026-11-15",
    description: "Systems infrastructure engineering, client solution deployments, and platform operations.",
    status: "ACTIVE",
    totalPositions: 50
  },
  {
    id: 105,
    company: "Amazon India",
    role: "Software Development Engineer (SDE I)",
    packageLpa: 28.5,
    minCgpa: 8.0,
    maxBacklogs: 0,
    allowedBranches: ["CSE", "IT"],
    location: "Bengaluru / Hyderabad",
    deadline: "2026-10-25T23:59:59",
    driveDate: "2026-10-31",
    description: "Design customer-centric scalable systems handling millions of transactions with low latency. Strong DSA & OOP proficiency.",
    status: "ACTIVE",
    totalPositions: 8
  }
];

const INITIAL_APPLICATIONS = [
  {
    id: 501,
    studentId: 1,
    studentName: "Aarav Sharma",
    studentEmail: "student@placement.edu",
    driveId: 102,
    company: "Infosys",
    role: "Specialist Programmer (Power Programmer)",
    packageLpa: 9.5,
    status: "INTERVIEW_SCHEDULED",
    createdAt: "2026-10-18T10:15:00",
    updatedAt: "2026-10-20T16:00:00"
  },
  {
    id: 502,
    studentId: 1,
    studentName: "Aarav Sharma",
    studentEmail: "student@placement.edu",
    driveId: 101,
    company: "Tata Consultancy Services (TCS)",
    role: "Digital Software Engineer",
    packageLpa: 7.5,
    status: "SHORTLISTED",
    createdAt: "2026-10-19T14:30:00",
    updatedAt: "2026-10-21T11:20:00"
  },
  {
    id: 503,
    studentId: 1,
    studentName: "Aarav Sharma",
    studentEmail: "student@placement.edu",
    driveId: 105,
    company: "Amazon India",
    role: "Software Development Engineer (SDE I)",
    packageLpa: 28.5,
    status: "APPLIED",
    createdAt: "2026-10-21T09:00:00",
    updatedAt: "2026-10-21T09:00:00"
  },
  {
    id: 504,
    studentId: 6,
    studentName: "Priya Patel",
    studentEmail: "priya.patel@placement.edu",
    driveId: 101,
    company: "Tata Consultancy Services (TCS)",
    role: "Digital Software Engineer",
    packageLpa: 7.5,
    status: "SELECTED",
    createdAt: "2026-10-15T11:00:00",
    updatedAt: "2026-10-21T15:30:00"
  },
  {
    id: 505,
    studentId: 7,
    studentName: "Rohan Gupta",
    studentEmail: "rohan.gupta@placement.edu",
    driveId: 103,
    company: "Wipro",
    role: "Project Engineer (Turbo Cadre)",
    packageLpa: 6.5,
    status: "APPLIED",
    createdAt: "2026-10-20T16:45:00",
    updatedAt: "2026-10-20T16:45:00"
  }
];

const INITIAL_INTERVIEWS = [
  {
    id: 901,
    applicationId: 501,
    studentId: 1,
    studentName: "Aarav Sharma",
    company: "Infosys",
    role: "Specialist Programmer (Power Programmer)",
    roundName: "Technical Round 1 (DSA & Architecture)",
    interviewDateTime: "2026-10-24T10:30:00",
    mode: "Virtual (Google Meet)",
    location: "https://meet.google.com/inf-tech-2026",
    status: "SCHEDULED",
    createdAt: "2026-10-20T16:00:00",
    updatedAt: "2026-10-20T16:00:00"
  },
  {
    id: 902,
    applicationId: 502,
    studentId: 1,
    studentName: "Aarav Sharma",
    company: "Tata Consultancy Services (TCS)",
    role: "Digital Software Engineer",
    roundName: "Managerial & Technical Discussion",
    interviewDateTime: "2026-10-26T14:00:00",
    mode: "Campus In-Person",
    location: "Placement Block - Conference Hall 2",
    status: "SCHEDULED",
    createdAt: "2026-10-21T11:20:00",
    updatedAt: "2026-10-21T11:20:00"
  }
];

const INITIAL_NOTIFICATIONS = [
  {
    id: 701,
    studentId: 1,
    message: "Interview Scheduled: Infosys Specialist Programmer Technical Round 1 on Oct 24, 2026 at 10:30 AM via Google Meet.",
    isRead: false,
    createdAt: "2026-10-20T16:05:00"
  },
  {
    id: 702,
    studentId: 1,
    message: "Shortlisted! Congratulations, TCS has shortlisted your profile for the Digital Software Engineer profile.",
    isRead: false,
    createdAt: "2026-10-21T11:25:00"
  },
  {
    id: 703,
    studentId: 1,
    message: "New Drive: Amazon India has opened applications for SDE I (28.5 LPA). Applications close on Oct 25.",
    isRead: true,
    createdAt: "2026-10-21T08:30:00"
  },
  {
    id: 704,
    studentId: 1,
    message: "Academic Verification: Your 6th-semester marksheet has been approved by the Department Placement Coordinator.",
    isRead: true,
    createdAt: "2026-10-17T09:15:00"
  }
];

// LocalStorage Persistence Layer
const STORAGE_KEY = "pms_mock_store_v1";

export const getMockStore = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      return JSON.parse(raw);
    }
  } catch (e) {
    console.error("Failed to load mock data from localStorage", e);
  }

  const initial = {
    users: INITIAL_USERS,
    drives: INITIAL_DRIVES,
    applications: INITIAL_APPLICATIONS,
    interviews: INITIAL_INTERVIEWS,
    notifications: INITIAL_NOTIFICATIONS
  };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(initial));
  return initial;
};

export const saveMockStore = (store) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(store));
  } catch (e) {
    console.error("Failed to save mock data to localStorage", e);
  }
};

export const resetMockStore = () => {
  localStorage.removeItem(STORAGE_KEY);
  return getMockStore();
};
