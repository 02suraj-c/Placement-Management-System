import apiClient, { isMockMode } from './api';

// Comprehensive dictionary of recognized tech skills with strict word-boundary matching
export const STRICT_TECH_SKILLS = [
  { name: 'Python', regex: /\bpython(3)?\b/i },
  { name: 'Java', regex: /\bjava\b(?!(\s*script|\.js|\/script))/i }, // Strictly Java, never JavaScript!
  { name: 'JavaScript', regex: /\b(javascript|ecmascript|\bjs\b)\b/i },
  { name: 'TypeScript', regex: /\b(typescript|\bts\b)\b/i },
  { name: 'C++', regex: /\b(c\+\+|cpp|cplusplus)\b/i },
  { name: 'C#', regex: /\b(c#|csharp|\.net|dotnet)\b/i },
  { name: 'C Language', regex: /\b(c\s+(?:programming|language)|ansi\s+c|c99|c11)\b/i },
  { name: 'React', regex: /\breact(\.js)?\b(?!(\s*native))/i },
  { name: 'React Native', regex: /\breact\s*native\b/i },
  { name: 'Angular', regex: /\bangular(\.js)?\b/i },
  { name: 'Vue.js', regex: /\bvue(\.js)?\b/i },
  { name: 'Node.js', regex: /\b(node(\.js)?|nodejs)\b/i },
  { name: 'Express.js', regex: /\bexpress(\.js)?\b/i },
  { name: 'Spring Boot', regex: /\bspring\s*boot\b/i },
  { name: 'SQL', regex: /\b(sql|mysql|postgresql|postgres|sqlite|pl\/sql)\b/i },
  { name: 'MongoDB', regex: /\b(mongodb|nosql)\b/i },
  { name: 'Redis', regex: /\bredis\b/i },
  { name: 'Docker', regex: /\bdocker\b/i },
  { name: 'Kubernetes', regex: /\b(kubernetes|k8s)\b/i },
  { name: 'AWS', regex: /\b(aws|amazon\s+web\s+services)\b/i },
  { name: 'Azure', regex: /\b(azure|microsoft\s+azure)\b/i },
  { name: 'Google Cloud', regex: /\b(gcp|google\s+cloud)\b/i },
  { name: 'Git', regex: /\b(git|github|gitlab)\b/i },
  { name: 'Linux', regex: /\b(linux|ubuntu|debian|bash|shell\s+scripting)\b/i },
  { name: 'Data Structures', regex: /\b(data\s+structures|dsa|linked\s+list|binary\s+tree|trie|graph\s+traversal|hash\s*map|hash\s*table)\b/i },
  { name: 'Algorithms', regex: /\b(algorithms|dynamic\s+programming|greedy|binary\s+search|sorting\s+algorithms)\b/i },
  { name: 'System Design', regex: /\b(system\s+design|microservices|distributed\s+systems|load\s+balancing)\b/i },
  { name: 'REST APIs', regex: /\b(rest|restful|rest\s+api|api\s+design|graphql)\b/i },
  { name: 'Object-Oriented Programming', regex: /\b(oop|oops|object\s+oriented\s+programming)\b/i },
  { name: 'Machine Learning', regex: /\b(machine\s+learning|\bml\b|deep\s+learning|tensorflow|pytorch|scikit-learn)\b/i },
  { name: 'HTML & CSS', regex: /\b(html5?|css3?|tailwind|bootstrap|sass)\b/i },
  { name: 'CI/CD', regex: /\b(ci\/cd|jenkins|github\s+actions)\b/i },
  { name: 'Kafka', regex: /\b(kafka|apache\s+kafka)\b/i },
];

// Helper to escape strings for regex
function escapeRegex(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// Build candidate text strictly from resume if uploaded, or profile skills if no resume
export function getCandidateText(studentProfile, resumeMetadata) {
  if (resumeMetadata?.extractedText && resumeMetadata.extractedText.trim().length > 0) {
    return resumeMetadata.extractedText;
  }
  if (studentProfile?.resumeMetadata?.extractedText && studentProfile.resumeMetadata.extractedText.trim().length > 0) {
    return studentProfile.resumeMetadata.extractedText;
  }
  // Fallback to explicit profile skills only if no resume exists
  if (studentProfile?.skills) {
    return Array.isArray(studentProfile.skills)
      ? studentProfile.skills.join(', ')
      : String(studentProfile.skills);
  }
  return '';
}

export const atsService = {
  /**
   * Calculate Accurate Generic ATS Score for student's active resume & profile
   * Based strictly on verified content, word-boundary regex detection, and structural completeness
   */
  async calculateGenericAts(studentProfile, resumeMetadata) {
    if (!resumeMetadata && !studentProfile?.resumeUrl) {
      return null;
    }

    // Strictly scan ONLY the text of the resume file!
    const resumeText = getCandidateText(studentProfile, resumeMetadata);

    // Strict keyword detection (only keywords with exact word-boundary matches in candidate's resume)
    const detectedKeywords = [];
    if (resumeText && resumeText.trim().length > 0) {
      STRICT_TECH_SKILLS.forEach((skillItem) => {
        if (skillItem.regex.test(resumeText)) {
          detectedKeywords.push(skillItem.name);
        }
      });
    }

    // 1. Contact Completeness (Max 20 pts)
    let contactScore = 8;
    if (studentProfile?.email && /\S+@\S+\.\S+/.test(studentProfile.email)) contactScore += 3;
    if (studentProfile?.phoneNumber && studentProfile.phoneNumber.trim().length >= 8) contactScore += 3;
    if (studentProfile?.linkedinUrl && studentProfile.linkedinUrl.trim().length > 10) contactScore += 3;
    if (studentProfile?.githubUrl && studentProfile.githubUrl.trim().length > 10) contactScore += 3;

    // 2. Technical Skills Breadth (Max 35 pts)
    // Evaluates count of strictly verified technical proficiencies found IN the resume
    let skillsScore = 0;
    const skillCount = detectedKeywords.length;
    if (skillCount >= 8) skillsScore = 35;
    else if (skillCount >= 6) skillsScore = 28;
    else if (skillCount >= 4) skillsScore = 20;
    else if (skillCount >= 2) skillsScore = 12;
    else if (skillCount >= 1) skillsScore = 6;
    else skillsScore = 0;

    // 3. Structural Sections & Formatting Completeness (Max 25 pts)
    let structureScore = 10;
    const lowerCorpus = resumeText.toLowerCase();
    const hasEducation = /\b(education|b\.?tech|m\.?tech|degree|cgpa|university|college|school)\b/i.test(lowerCorpus);
    const hasProjects = /\b(project|projects|developed|implemented|built|created)\b/i.test(lowerCorpus);
    const hasExperience = /\b(experience|internship|intern|work history|organization|freelance)\b/i.test(lowerCorpus);
    const hasActionVerbs = /\b(designed|architected|engineered|optimized|created|solved|scaled|led|built)\b/i.test(lowerCorpus);

    if (hasEducation) structureScore += 4;
    if (hasProjects) structureScore += 4;
    if (hasExperience) structureScore += 4;
    if (hasActionVerbs) structureScore += 3;

    // 4. Academic Qualification (Max 20 pts)
    let academicScore = 10;
    const cgpa = Number(studentProfile?.cgpa) || 8.0;
    const backlogs = Number(studentProfile?.activeBacklogs || 0);
    if (cgpa >= 8.5) academicScore = 20;
    else if (cgpa >= 7.5) academicScore = 16;
    else if (cgpa >= 6.5) academicScore = 12;
    else academicScore = 8;

    if (backlogs > 0) {
      academicScore = Math.max(4, academicScore - backlogs * 4);
    }

    const totalScore = Math.min(98, Math.max(20, contactScore + skillsScore + structureScore + academicScore));

    let scoreBand = 'Good';
    if (totalScore >= 80) scoreBand = 'Excellent';
    else if (totalScore >= 65) scoreBand = 'Good';
    else if (totalScore >= 50) scoreBand = 'Needs Optimization';
    else scoreBand = 'Critical Gaps';

    // Tailored actionable improvements based on what is genuinely missing
    const improvements = [];
    if (detectedKeywords.length === 0) {
      improvements.push('No technical keywords detected in your uploaded resume file. Add a clear "Technical Skills" section listing your programming languages, frameworks, and tools.');
    } else if (detectedKeywords.length < 4) {
      improvements.push('Expand your technical skill listings with relevant tools (e.g., SQL, Git, DSA, Frameworks).');
    }
    if (!studentProfile?.githubUrl) {
      improvements.push('Add a live GitHub / Portfolio URL to verify hands-on coding repositories.');
    }
    if (!studentProfile?.linkedinUrl) {
      improvements.push('Include your public LinkedIn profile link for recruiter verification.');
    }
    if (!hasActionVerbs) {
      improvements.push('Incorporate strong impact verbs (e.g. Architected, Engineered, Optimized, Deployed) in project descriptions.');
    }
    if (improvements.length === 0) {
      improvements.push('Ensure project achievements feature measurable metrics (e.g. "Improved query performance by 35%").');
    }

    return {
      atsScore: totalScore,
      scoreBand,
      detectedKeywordsCount: detectedKeywords.length,
      matchedKeywords: detectedKeywords,
      sectionScores: {
        contact: Math.round((contactScore / 20) * 100),
        skills: Math.round((skillsScore / 35) * 100),
        structure: Math.round((structureScore / 25) * 100),
        academic: Math.round((academicScore / 20) * 100),
      },
      topImprovements: improvements,
    };
  },

  /**
   * Calculate Accurate Company-Specific ATS Score
   * Strictly compares student's real resume text against that company's specific job description stack
   */
  calculateCompanyAts(studentProfile, companyName = '', targetRole = '', driveRequiredSkills = []) {
    const candidateText = getCandidateText(studentProfile);

    // Determine target required skills for the company
    let targetSkills = [];
    if (driveRequiredSkills && driveRequiredSkills.length > 0) {
      targetSkills = driveRequiredSkills;
    } else {
      const compLower = companyName.toLowerCase();
      if (compLower.includes('google')) {
        targetSkills = ['Data Structures', 'Algorithms', 'C++', 'Python', 'System Design'];
      } else if (compLower.includes('microsoft')) {
        targetSkills = ['Data Structures', 'Algorithms', 'C#', 'SQL', 'Azure'];
      } else if (compLower.includes('amazon')) {
        targetSkills = ['Data Structures', 'Object-Oriented Programming', 'AWS', 'Java', 'Distributed Systems'];
      } else if (compLower.includes('tcs')) {
        targetSkills = ['Java', 'SQL', 'Data Structures', 'Web Development', 'OOP'];
      } else if (compLower.includes('infosys')) {
        targetSkills = ['Java', 'Python', 'SQL', 'Spring Boot', 'Problem Solving'];
      } else if (compLower.includes('accenture')) {
        targetSkills = ['Java', 'SQL', 'Cloud Fundamentals', 'Agile', 'Web Architecture'];
      } else {
        targetSkills = ['Data Structures', 'Algorithms', 'SQL', 'Git', 'Problem Solving'];
      }
    }

    const matched = [];
    const missing = [];

    targetSkills.forEach((skillName) => {
      // Find matching item in strict dictionary or build exact regex
      const foundInDict = STRICT_TECH_SKILLS.find(
        (s) => s.name.toLowerCase() === skillName.toLowerCase()
      );

      let isMatched = false;
      if (candidateText && candidateText.trim().length > 0) {
        if (foundInDict) {
          isMatched = foundInDict.regex.test(candidateText);
        } else {
          const safeRegex = new RegExp(`\\b${escapeRegex(skillName)}\\b`, 'i');
          isMatched = safeRegex.test(candidateText);
        }
      }

      if (isMatched) {
        matched.push(skillName);
      } else {
        missing.push(skillName);
      }
    });

    // Accurate Match Percentage
    const matchRatio = targetSkills.length > 0 ? matched.length / targetSkills.length : 0.5;
    const rawMatchPercentage = Math.round(matchRatio * 100);

    let matchQuality = 'Low Fit';
    if (rawMatchPercentage >= 80) matchQuality = 'Strong Match';
    else if (rawMatchPercentage >= 60) matchQuality = 'Good Fit';
    else if (rawMatchPercentage >= 40) matchQuality = 'Moderate Fit';
    else matchQuality = 'Needs Preparation';

    return {
      companyName,
      targetRole: targetRole || 'Software Engineer',
      atsScore: rawMatchPercentage,
      matchQuality,
      matchedKeywords: matched,
      missingKeywords: missing,
      recommendation:
        matched.length === targetSkills.length
          ? `100% key skill alignment for ${companyName}. Candidate profile matches all core criteria.`
          : matched.length > 0
          ? `Matched ${matched.length} of ${targetSkills.length} core criteria for ${companyName}. Recommended to highlight: ${missing.slice(0, 3).join(', ')}.`
          : `Candidate profile lacks core required keywords for ${companyName}: ${missing.slice(0, 3).join(', ')}.`,
    };
  },
};
