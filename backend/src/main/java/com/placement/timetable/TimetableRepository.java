package com.placement.timetable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<TimetableSlot, Long> {

    List<TimetableSlot> findByAcademicYearAndSemesterAndDivisionOrderByDayAscStartTimeAsc(
            Integer academicYear, Integer semester, String division);

    List<TimetableSlot> findByAcademicYearAndSemesterOrderByDayAscStartTimeAsc(
            Integer academicYear, Integer semester);

    List<TimetableSlot> findByAcademicYearOrderByDayAscStartTimeAsc(Integer academicYear);

    List<TimetableSlot> findByAcademicYearAndSemesterAndDivisionAndPublishedTrueOrderByDayAscStartTimeAsc(
            Integer academicYear, Integer semester, String division);

    List<TimetableSlot> findByAcademicYearAndSemesterAndPublishedTrueOrderByDayAscStartTimeAsc(
            Integer academicYear, Integer semester);

    List<TimetableSlot> findByDay(String day);

    List<TimetableSlot> findByDayAndFacultyId(String day, Long facultyId);

    List<TimetableSlot> findByDayAndRoomIgnoreCase(String day, String room);

    List<TimetableSlot> findByDayAndAcademicYearAndSemesterAndDivision(
            String day, Integer academicYear, Integer semester, String division);

    @Query("SELECT DISTINCT t.division FROM TimetableSlot t WHERE t.academicYear = :year AND t.semester = :sem ORDER BY t.division ASC")
    List<String> findDistinctDivisionsByYearAndSemester(@Param("year") Integer year, @Param("sem") Integer sem);
}

