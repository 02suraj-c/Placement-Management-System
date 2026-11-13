package com.placement.attendance;

import com.placement.attendance.dto.*;
import com.placement.exception.ResourceNotFoundException;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import com.placement.util.YearUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final ClassAttendanceRepository classAttendanceRepository;
    private final CRTAttendanceRepository crtAttendanceRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional
    public AttendanceRecordResponse recordClassAttendance(ClassAttendanceRequest request, String staffEmail) {
        log.info("Recording class attendance for student id: {} by staff: {}", request.getStudentId(), staffEmail);

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        if (classAttendanceRepository.existsByStudentIdAndSubjectAndAttendanceDate(
                student.getId(), request.getSubject().trim(), request.getAttendanceDate())) {
            throw new IllegalArgumentException(String.format(
                    "Class attendance already recorded for student '%s' on %s for subject '%s'",
                    student.getRollNumber(), request.getAttendanceDate(), request.getSubject()));
        }

        ClassAttendance attendance = ClassAttendance.builder()
                .student(student)
                .subject(request.getSubject().trim())
                .attendanceDate(request.getAttendanceDate())
                .semester(student.getCurrentSemester())
                .academicYear(student.getAcademicYear())
                .present(request.getPresent())
                .remarks(request.getRemarks())
                .recordedBy(staffEmail)
                .build();

        ClassAttendance saved = classAttendanceRepository.save(attendance);
        log.info("Successfully recorded class attendance id: {}", saved.getId());

        return mapClassAttendanceToResponse(saved);
    }

    @Transactional
    public AttendanceRecordResponse recordCRTAttendance(CRTAttendanceRequest request, String staffEmail) {
        log.info("Recording CRT attendance for student id: {} by staff: {}", request.getStudentId(), staffEmail);

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        if (crtAttendanceRepository.existsByStudentIdAndSessionNameAndSessionDate(
                student.getId(), request.getSessionName().trim(), request.getSessionDate())) {
            throw new IllegalArgumentException(String.format(
                    "CRT attendance already recorded for student '%s' on %s for session '%s'",
                    student.getRollNumber(), request.getSessionDate(), request.getSessionName()));
        }

        CRTAttendance attendance = CRTAttendance.builder()
                .student(student)
                .sessionName(request.getSessionName().trim())
                .sessionDate(request.getSessionDate())
                .semester(student.getCurrentSemester())
                .academicYear(student.getAcademicYear())
                .present(request.getPresent())
                .trainerName(request.getTrainerName())
                .remarks(request.getRemarks())
                .recordedBy(staffEmail)
                .build();

        CRTAttendance saved = crtAttendanceRepository.save(attendance);
        log.info("Successfully recorded CRT attendance id: {}", saved.getId());

        return mapCrtAttendanceToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentClassAttendance(Long studentId, String currentUserEmail) {
        return getStudentClassAttendance(studentId, currentUserEmail, null);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentCRTAttendance(Long studentId, String currentUserEmail) {
        return getStudentCRTAttendance(studentId, currentUserEmail, null);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getStudentAttendanceSummary(Long studentId, String currentUserEmail) {
        return getStudentAttendanceSummary(studentId, currentUserEmail, null);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentClassAttendance(Long studentId, String currentUserEmail, Integer semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        validateStudentAccess(student, currentUserEmail);

        List<ClassAttendance> list = (semester != null)
                ? classAttendanceRepository.findByStudentIdAndSemesterOrderByAttendanceDateDesc(studentId, semester)
                : classAttendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);

        return list.stream()
                .map(this::mapClassAttendanceToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentCRTAttendance(Long studentId, String currentUserEmail, Integer semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        validateStudentAccess(student, currentUserEmail);

        List<CRTAttendance> list = (semester != null)
                ? crtAttendanceRepository.findByStudentIdAndSemesterOrderBySessionDateDesc(studentId, semester)
                : crtAttendanceRepository.findByStudentIdOrderBySessionDateDesc(studentId);

        return list.stream()
                .map(this::mapCrtAttendanceToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getStudentAttendanceSummary(Long studentId, String currentUserEmail, Integer semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        validateStudentAccess(student, currentUserEmail);

        return calculateSummary(student, semester);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getCurrentStudentAttendanceSummary(String userEmail, Integer semester) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        Integer targetSemester = (semester != null) ? semester : student.getCurrentSemester();
        return calculateSummary(student, targetSemester);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> getCurrentStudentAttendanceHistory(String userEmail) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        List<Integer> recordedSemesters = classAttendanceRepository.findDistinctSemestersByStudentId(student.getId());
        if (recordedSemesters.isEmpty()) {
            recordedSemesters = List.of(student.getCurrentSemester());
        }

        List<AttendanceSummaryResponse> history = new ArrayList<>();
        for (Integer sem : recordedSemesters) {
            history.add(calculateSummary(student, sem));
        }
        return history;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getCurrentStudentClassAttendance(String userEmail, Integer semester) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        Integer targetSem = (semester != null) ? semester : student.getCurrentSemester();
        return classAttendanceRepository.findByStudentIdAndSemesterOrderByAttendanceDateDesc(student.getId(), targetSem).stream()
                .map(this::mapClassAttendanceToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getCurrentStudentCRTAttendance(String userEmail, Integer semester) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        Integer targetSem = (semester != null) ? semester : student.getCurrentSemester();
        return crtAttendanceRepository.findByStudentIdAndSemesterOrderBySessionDateDesc(student.getId(), targetSem).stream()
                .map(this::mapCrtAttendanceToResponse)
                .toList();
    }

    private void validateStudentAccess(Student student, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        if (currentUser.getRole() == UserRole.ROLE_STUDENT) {
            if (!student.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to view another student's attendance records");
            }
        }
    }

    private AttendanceSummaryResponse calculateSummary(Student student, Integer semester) {
        Long studentId = student.getId();
        int sem = (semester != null) ? semester : student.getCurrentSemester();
        int year = YearUtils.calculateYearFromSemester(sem);

        long totalClass = classAttendanceRepository.countByStudentIdAndSemester(studentId, sem);
        long attendedClass = classAttendanceRepository.countByStudentIdAndSemesterAndPresentTrue(studentId, sem);
        double classPct = totalClass > 0 ? roundDouble(((double) attendedClass / totalClass) * 100.0) : 0.0;

        long totalCrt = crtAttendanceRepository.countByStudentIdAndSemester(studentId, sem);
        long attendedCrt = crtAttendanceRepository.countByStudentIdAndSemesterAndPresentTrue(studentId, sem);
        double crtPct = totalCrt > 0 ? roundDouble(((double) attendedCrt / totalCrt) * 100.0) : 0.0;

        long totalSessions = totalClass + totalCrt;
        long totalAttended = attendedClass + attendedCrt;
        double overallPct = totalSessions > 0 ? roundDouble(((double) totalAttended / totalSessions) * 100.0) : 0.0;

        // Calculate subject-wise breakdown for this semester
        List<String> subjects = classAttendanceRepository.findDistinctSubjectsByStudentIdAndSemester(studentId, sem);
        List<AttendanceSummaryResponse.SubjectAttendanceSummary> subjectBreakdowns = new ArrayList<>();

        if (subjects != null) {
            for (String subject : subjects) {
                long totalSub = classAttendanceRepository.countByStudentIdAndSemesterAndSubject(studentId, sem, subject);
                long attendedSub = classAttendanceRepository.countByStudentIdAndSemesterAndSubjectAndPresentTrue(studentId, sem, subject);
                double subPct = totalSub > 0 ? roundDouble(((double) attendedSub / totalSub) * 100.0) : 0.0;

                subjectBreakdowns.add(AttendanceSummaryResponse.SubjectAttendanceSummary.builder()
                        .subject(subject)
                        .totalSessions(totalSub)
                        .attendedSessions(attendedSub)
                        .percentage(subPct)
                        .build());
            }
        }

        return AttendanceSummaryResponse.builder()
                .studentId(studentId)
                .studentName(student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student.getRollNumber())
                .department(student.getDepartment())
                .academicYear(year)
                .semester(sem)
                .totalClassSessions(totalClass)
                .attendedClassSessions(attendedClass)
                .classAttendancePercentage(classPct)
                .totalCrtSessions(totalCrt)
                .attendedCrtSessions(attendedCrt)
                .crtAttendancePercentage(crtPct)
                .totalSessions(totalSessions)
                .totalAttended(totalAttended)
                .overallAttendancePercentage(overallPct)
                .eligibleForPlacements(overallPct >= 75.0)
                .subjectBreakdowns(subjectBreakdowns)
                .build();
    }

    private double roundDouble(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    public AttendanceRecordResponse mapClassAttendanceToResponse(ClassAttendance record) {
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getId() : null)
                .studentName(record.getStudent() != null && record.getStudent().getUser() != null ? record.getStudent().getUser().getName() : null)
                .rollNumber(record.getStudent() != null ? record.getStudent().getRollNumber() : null)
                .academicYear(record.getAcademicYear())
                .semester(record.getSemester())
                .type("CLASS")
                .title(record.getSubject())
                .date(record.getAttendanceDate())
                .present(record.isPresent())
                .status(record.isPresent() ? "PRESENT" : "ABSENT")
                .remarks(record.getRemarks())
                .recordedBy(record.getRecordedBy())
                .createdAt(record.getCreatedAt())
                .build();
    }

    public AttendanceRecordResponse mapCrtAttendanceToResponse(CRTAttendance record) {
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent() != null ? record.getStudent().getId() : null)
                .studentName(record.getStudent() != null && record.getStudent().getUser() != null ? record.getStudent().getUser().getName() : null)
                .rollNumber(record.getStudent() != null ? record.getStudent().getRollNumber() : null)
                .academicYear(record.getAcademicYear())
                .semester(record.getSemester())
                .type("CRT")
                .title(record.getSessionName())
                .date(record.getSessionDate())
                .present(record.isPresent())
                .status(record.isPresent() ? "PRESENT" : "ABSENT")
                .trainerName(record.getTrainerName())
                .remarks(record.getRemarks())
                .recordedBy(record.getRecordedBy())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
