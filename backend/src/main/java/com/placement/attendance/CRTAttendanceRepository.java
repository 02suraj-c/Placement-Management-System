package com.placement.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CRTAttendanceRepository extends JpaRepository<CRTAttendance, Long> {

    List<CRTAttendance> findByStudentIdOrderBySessionDateDesc(Long studentId);

    List<CRTAttendance> findByStudentIdAndSemesterOrderBySessionDateDesc(Long studentId, Integer semester);

    List<CRTAttendance> findByStudentUserEmailOrderBySessionDateDesc(String userEmail);

    boolean existsByStudentIdAndSessionNameAndSessionDate(Long studentId, String sessionName, LocalDate sessionDate);

    long countByStudentId(Long studentId);

    long countByStudentIdAndPresentTrue(Long studentId);

    long countByStudentIdAndSemester(Long studentId, Integer semester);

    long countByStudentIdAndSemesterAndPresentTrue(Long studentId, Integer semester);
}
