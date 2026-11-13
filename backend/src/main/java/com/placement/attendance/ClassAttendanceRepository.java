package com.placement.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassAttendanceRepository extends JpaRepository<ClassAttendance, Long> {

    List<ClassAttendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    List<ClassAttendance> findByStudentIdAndSemesterOrderByAttendanceDateDesc(Long studentId, Integer semester);

    List<ClassAttendance> findByStudentUserEmailOrderByAttendanceDateDesc(String userEmail);

    List<ClassAttendance> findByStudentIdAndSubjectOrderByAttendanceDateDesc(Long studentId, String subject);

    boolean existsByStudentIdAndSubjectAndAttendanceDate(Long studentId, String subject, LocalDate attendanceDate);

    long countByStudentId(Long studentId);

    long countByStudentIdAndPresentTrue(Long studentId);

    long countByStudentIdAndSemester(Long studentId, Integer semester);

    long countByStudentIdAndSemesterAndPresentTrue(Long studentId, Integer semester);

    @Query("SELECT DISTINCT c.subject FROM ClassAttendance c WHERE c.student.id = :studentId")
    List<String> findDistinctSubjectsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT DISTINCT c.subject FROM ClassAttendance c WHERE c.student.id = :studentId AND c.semester = :semester")
    List<String> findDistinctSubjectsByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("semester") Integer semester);

    @Query("SELECT DISTINCT c.semester FROM ClassAttendance c WHERE c.student.id = :studentId AND c.semester IS NOT NULL ORDER BY c.semester ASC")
    List<Integer> findDistinctSemestersByStudentId(@Param("studentId") Long studentId);

    long countByStudentIdAndSubject(Long studentId, String subject);

    long countByStudentIdAndSubjectAndPresentTrue(Long studentId, String subject);

    long countByStudentIdAndSemesterAndSubject(Long studentId, Integer semester, String subject);

    long countByStudentIdAndSemesterAndSubjectAndPresentTrue(Long studentId, Integer semester, String subject);
}
