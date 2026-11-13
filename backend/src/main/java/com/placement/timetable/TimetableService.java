package com.placement.timetable;

import com.placement.exception.ResourceNotFoundException;
import com.placement.notification.NotificationService;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.timetable.dto.*;
import com.placement.util.YearUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    // ─────────────────────────────────────────────────────────────────────────
    // CLASH DETECTION
    // ─────────────────────────────────────────────────────────────────────────

    public ClashCheckResponse checkClashes(ClashCheckRequest req) {
        if (req.getType() == SlotType.FREE) {
            return ClashCheckResponse.builder().hasClash(false).clashDetails(Collections.emptyList()).build();
        }

        if (req.getStartTime() == null || req.getEndTime() == null || req.getDay() == null) {
            return ClashCheckResponse.builder().hasClash(false).clashDetails(Collections.emptyList()).build();
        }

        List<TimetableSlot> daySlots = timetableRepository.findByDay(req.getDay().trim().toUpperCase());
        List<String> clashes = new ArrayList<>();

        for (TimetableSlot existing : daySlots) {
            // Exclude current slot if updating
            if (req.getSlotIdToExclude() != null && req.getSlotIdToExclude().equals(existing.getId())) {
                continue;
            }

            // If existing slot is FREE, it produces no conflict
            if (existing.getType() == SlotType.FREE) {
                continue;
            }

            // Check time overlap: (startA < endB && endA > startB)
            boolean overlap = req.getStartTime().isBefore(existing.getEndTime()) &&
                              req.getEndTime().isAfter(existing.getStartTime());

            if (!overlap) {
                continue;
            }

            String timeSpan = String.format("%02d:%02d-%02d:%02d",
                    existing.getStartTime().getHour(), existing.getStartTime().getMinute(),
                    existing.getEndTime().getHour(), existing.getEndTime().getMinute());

            // 1. Faculty clash
            if (req.getFacultyId() != null && existing.getFacultyId() != null &&
                    req.getFacultyId().equals(existing.getFacultyId())) {
                clashes.add(String.format("Faculty clash: %s is already assigned to '%s' in room %s (%s, Div %s).",
                        existing.getFacultyName() != null ? existing.getFacultyName() : "Faculty #" + req.getFacultyId(),
                        existing.getSubject(), existing.getRoom(), timeSpan, existing.getDivision()));
            }

            // 2. Room clash
            if (req.getRoom() != null && !req.getRoom().isBlank() && existing.getRoom() != null &&
                    req.getRoom().trim().equalsIgnoreCase(existing.getRoom().trim())) {
                clashes.add(String.format("Room clash: Room %s is already booked for '%s' (%s, Div %s).",
                        existing.getRoom(), existing.getSubject(), timeSpan, existing.getDivision()));
            }

            // 3. Division clash
            if (req.getAcademicYear() != null && req.getSemester() != null && req.getDivision() != null &&
                    req.getAcademicYear().equals(existing.getAcademicYear()) &&
                    req.getSemester().equals(existing.getSemester()) &&
                    req.getDivision().trim().equalsIgnoreCase(existing.getDivision().trim())) {
                clashes.add(String.format("Division clash: Section %s already has '%s' scheduled at %s.",
                        existing.getDivision(), existing.getSubject(), timeSpan));
            }
        }

        return ClashCheckResponse.builder()
                .hasClash(!clashes.isEmpty())
                .clashDetails(clashes)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public TimetableSlotResponse createSlot(TimetableSlotRequest req) {
        if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().equals(req.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        ClashCheckRequest clashReq = ClashCheckRequest.builder()
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .division(req.getDivision())
                .day(req.getDay())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .facultyId(req.getFacultyId())
                .facultyName(req.getFacultyName())
                .room(req.getRoom())
                .type(req.getType())
                .build();

        ClashCheckResponse clash = checkClashes(clashReq);
        if (clash.isHasClash()) {
            throw new IllegalArgumentException("Cannot schedule class due to clashes: " +
                    String.join(" | ", clash.getClashDetails()));
        }

        TimetableSlot slot = TimetableSlot.builder()
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .division(req.getDivision().trim().toUpperCase())
                .day(req.getDay().trim().toUpperCase())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .subject(req.getSubject().trim())
                .facultyId(req.getFacultyId())
                .facultyName(req.getFacultyName() != null ? req.getFacultyName().trim() : null)
                .room(req.getRoom() != null ? req.getRoom().trim() : null)
                .type(req.getType() != null ? req.getType() : SlotType.LECTURE)
                .published(req.getPublished() != null ? req.getPublished() : true)
                .build();

        TimetableSlot saved = timetableRepository.save(slot);
        log.info("Created timetable slot ID: {} for Year {} Sem {} Div {}",
                saved.getId(), saved.getAcademicYear(), saved.getSemester(), saved.getDivision());
        return TimetableSlotResponse.from(saved);
    }

    @Transactional
    public TimetableSlotResponse updateSlot(Long id, TimetableSlotRequest req) {
        TimetableSlot slot = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", id));

        if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().equals(req.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        ClashCheckRequest clashReq = ClashCheckRequest.builder()
                .slotIdToExclude(id)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .division(req.getDivision())
                .day(req.getDay())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .facultyId(req.getFacultyId())
                .facultyName(req.getFacultyName())
                .room(req.getRoom())
                .type(req.getType())
                .build();

        ClashCheckResponse clash = checkClashes(clashReq);
        if (clash.isHasClash()) {
            throw new IllegalArgumentException("Cannot update class due to clashes: " +
                    String.join(" | ", clash.getClashDetails()));
        }

        slot.setAcademicYear(req.getAcademicYear());
        slot.setSemester(req.getSemester());
        slot.setDivision(req.getDivision().trim().toUpperCase());
        slot.setDay(req.getDay().trim().toUpperCase());
        slot.setStartTime(req.getStartTime());
        slot.setEndTime(req.getEndTime());
        slot.setSubject(req.getSubject().trim());
        slot.setFacultyId(req.getFacultyId());
        slot.setFacultyName(req.getFacultyName() != null ? req.getFacultyName().trim() : null);
        slot.setRoom(req.getRoom() != null ? req.getRoom().trim() : null);
        if (req.getType() != null) {
            slot.setType(req.getType());
        }
        if (req.getPublished() != null) {
            slot.setPublished(req.getPublished());
        }

        TimetableSlot updated = timetableRepository.save(slot);
        log.info("Updated timetable slot ID: {}", updated.getId());
        return TimetableSlotResponse.from(updated);
    }

    @Transactional
    public void deleteSlot(Long id) {
        TimetableSlot slot = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", id));
        timetableRepository.delete(slot);
        log.info("Deleted timetable slot ID: {}", id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLISH TIMETABLE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public int publishTimetable(PublishTimetableRequest req, String publishedBy) {
        List<TimetableSlot> slots = (req.getDivision() != null && !req.getDivision().isBlank())
                ? timetableRepository.findByAcademicYearAndSemesterAndDivisionOrderByDayAscStartTimeAsc(
                        req.getAcademicYear(), req.getSemester(), req.getDivision().trim().toUpperCase())
                : timetableRepository.findByAcademicYearAndSemesterOrderByDayAscStartTimeAsc(
                        req.getAcademicYear(), req.getSemester());

        for (TimetableSlot s : slots) {
            s.setPublished(true);
        }
        timetableRepository.saveAll(slots);

        // Notify affected students in target academic year & semester
        String divLabel = (req.getDivision() != null && !req.getDivision().isBlank()) ? " (Section " + req.getDivision().trim().toUpperCase() + ")" : "";
        String msg = String.format("Official Timetable for %s · Semester %d%s has been published/updated.",
                YearUtils.getYearLabel(req.getAcademicYear()), req.getSemester(), divLabel);

        notificationService.broadcastToYear(String.valueOf(req.getAcademicYear()), msg);
        log.info("Published {} timetable slots for Year {} Sem {} by {}", slots.size(), req.getAcademicYear(), req.getSemester(), publishedBy);
        return slots.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STUDENT QUERY (STRICT YEAR-WISE ENFORCEMENT)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TimetableSlotResponse> getStudentTimetable(String userEmail, Integer requestedSemester, String division) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + userEmail));

        int studentYear = student.getAcademicYear();
        List<Integer> allowedSemesters = YearUtils.getSemestersForYear(studentYear);

        int resolvedSemester;
        if (requestedSemester != null && allowedSemesters.contains(requestedSemester)) {
            resolvedSemester = requestedSemester;
        } else {
            resolvedSemester = (student.getCurrentSemester() != null && allowedSemesters.contains(student.getCurrentSemester()))
                    ? student.getCurrentSemester()
                    : allowedSemesters.get(0);
        }

        List<TimetableSlot> slots;
        if (division != null && !division.isBlank()) {
            slots = timetableRepository.findByAcademicYearAndSemesterAndDivisionAndPublishedTrueOrderByDayAscStartTimeAsc(
                    studentYear, resolvedSemester, division.trim().toUpperCase());
        } else {
            slots = timetableRepository.findByAcademicYearAndSemesterAndPublishedTrueOrderByDayAscStartTimeAsc(
                    studentYear, resolvedSemester);
        }

        return slots.stream()
                .map(TimetableSlotResponse::from)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF QUERY
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TimetableSlotResponse> getStaffTimetable(Integer year, Integer semester, String division) {
        List<TimetableSlot> slots;
        if (year != null && semester != null && division != null && !division.isBlank()) {
            slots = timetableRepository.findByAcademicYearAndSemesterAndDivisionOrderByDayAscStartTimeAsc(
                    year, semester, division.trim().toUpperCase());
        } else if (year != null && semester != null) {
            slots = timetableRepository.findByAcademicYearAndSemesterOrderByDayAscStartTimeAsc(year, semester);
        } else if (year != null) {
            slots = timetableRepository.findByAcademicYearOrderByDayAscStartTimeAsc(year);
        } else {
            slots = timetableRepository.findAll();
        }

        return slots.stream()
                .map(TimetableSlotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getDivisions(Integer year, Integer semester) {
        if (year == null || semester == null) {
            return List.of("A", "B", "C");
        }
        List<String> list = timetableRepository.findDistinctDivisionsByYearAndSemester(year, semester);
        return list.isEmpty() ? List.of("A", "B", "C") : list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PDF GENERATION (PDFBox)
    // ─────────────────────────────────────────────────────────────────────────

    public byte[] generatePdf(Integer year, Integer semester, String division) throws IOException {
        List<TimetableSlotResponse> slots = getStaffTimetable(year, semester, division);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 800;

                // Header Title
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                cs.newLineAtOffset(50, y);
                cs.showText("JECRC UNIVERSITY - DEPARTMENT TIMETABLE");
                cs.endText();
                y -= 25;

                // Subtitle
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, y);
                String meta = String.format("Academic Year: %s  |  Semester: %s  |  Section: %s",
                        YearUtils.getYearLabel(year),
                        semester != null ? "Sem " + semester : "All",
                        division != null ? division : "All");
                cs.showText(meta);
                cs.endText();
                y -= 30;

                // Table Header
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                cs.newLineAtOffset(50, y);
                cs.showText(String.format("%-10s  %-15s  %-26s  %-20s  %-10s  %-8s",
                        "DAY", "TIME", "SUBJECT", "FACULTY", "ROOM", "TYPE"));
                cs.endText();
                y -= 12;

                // Divider Line
                cs.setLineWidth(1.0f);
                cs.moveTo(50, y);
                cs.lineTo(550, y);
                cs.stroke();
                y -= 15;

                // Rows
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (TimetableSlotResponse slot : slots) {
                    if (y < 60) {
                        break; // fit single page preview
                    }
                    cs.beginText();
                    cs.newLineAtOffset(50, y);
                    String day = slot.getDay() != null ? slot.getDay() : "";
                    String time = slot.getTimeDisplay() != null ? slot.getTimeDisplay() : "";
                    String sub = truncate(slot.getSubject(), 24);
                    String fac = truncate(slot.getFacultyName(), 18);
                    String rm = truncate(slot.getRoom(), 8);
                    String tp = slot.getType() != null ? slot.getType().name() : "";

                    cs.showText(String.format("%-10s  %-15s  %-26s  %-20s  %-10s  %-8s",
                            day, time, sub, fac, rm, tp));
                    cs.endText();
                    y -= 16;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return "-";
        return text.length() > max ? text.substring(0, max - 2) + ".." : text;
    }
}

