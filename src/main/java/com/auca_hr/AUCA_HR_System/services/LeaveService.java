package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.dtos.LeaveBalanceDTO;
import com.auca_hr.AUCA_HR_System.dtos.LeaveRequestDTO;
import com.auca_hr.AUCA_HR_System.entities.LeaveRequest;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.LeaveStatus;
import com.auca_hr.AUCA_HR_System.enums.LeaveType;
import com.auca_hr.AUCA_HR_System.exceptions.ResourceNotFoundException;
import com.auca_hr.AUCA_HR_System.repositories.LeaveRequestRepository;
import com.auca_hr.AUCA_HR_System.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Maximum annual leave days allowed
    private static final int MAX_ANNUAL_LEAVE_DAYS = 30;


    // Get leave balance for a specific user
    public int getRemainingLeaveBalance(String userId, int year) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByUserEmailAndYearAndStatus(userId, year, LeaveStatus.APPROVED);

        int usedDays = approvedLeaves.stream()
                .mapToInt(leave -> (int) ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1)
                .sum();

        return MAX_ANNUAL_LEAVE_DAYS - usedDays;
    }

    public LeaveRequestDTO submitLeaveRequest(LeaveRequestDTO leaveRequestDTO, String lecturerUsername) {
        log.info("Submitting leave request for lecturer: {}", lecturerUsername);

        User lecturer = userRepository.findByEmail(lecturerUsername)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        // Validate leave dates
        validateLeaveDates(leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate());

        // Check for overlapping leaves
        checkForOverlappingLeaves(lecturer, leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate());

        // Check annual leave limit
        checkAnnualLeaveLimit(lecturer, leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate());

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setLecturer(lecturer);
        leaveRequest.setLeaveType(leaveRequestDTO.getLeaveType());
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        leaveRequest.setDescription(leaveRequestDTO.getDescription());
        leaveRequest.setStatus(LeaveStatus.PENDING);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        // Send notification to HR
        notificationService.notifyHROfNewLeaveRequest(savedRequest);

        log.info("Leave request submitted successfully with ID: {}", savedRequest.getId());

        return convertToDTO(savedRequest);
    }

    public List<LeaveRequestDTO> getLeaveRequestsByLecturer(String lecturerUsername) {
        User lecturer = userRepository.findByEmail(lecturerUsername)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        List<LeaveRequest> requests = leaveRequestRepository.findByLecturerOrderByCreatedAtDesc(lecturer);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getAllPendingLeaveRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findAllPendingLeaveRequests();
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getAllLeaveRequests() {
        List<LeaveRequest> requests = leaveRequestRepository.findAll();
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public LeaveRequestDTO approveLeaveRequest(Long requestId, String hrUsername, String comments) {
        log.info("Approving leave request ID: {} by HR: {}", requestId, hrUsername);

        User hrUser = userRepository.findByEmail(hrUsername)
                .orElseThrow(() -> new RuntimeException("HR user not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in pending status");
        }

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(hrUser);
        leaveRequest.setHrComments(comments);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        // Send notification to lecturer
        notificationService.notifyLecturerOfLeaveApproval(savedRequest);

        log.info("Leave request approved successfully");

        return convertToDTO(savedRequest);
    }

    public LeaveRequestDTO rejectLeaveRequest(Long requestId, String hrUsername, String comments) {
        log.info("Rejecting leave request ID: {} by HR: {}", requestId, hrUsername);

        User hrUser = userRepository.findByEmail(hrUsername)
                .orElseThrow(() -> new RuntimeException("HR user not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Leave request is not in pending status");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(hrUser);
        leaveRequest.setHrComments(comments);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        // Send notification to lecturer
        notificationService.notifyLecturerOfLeaveRejection(savedRequest);

        log.info("Leave request rejected successfully");

        return convertToDTO(savedRequest);
    }

    public LeaveRequestDTO cancelLeaveRequest(Long requestId, String lecturerUsername) {
        log.info("Cancelling leave request ID: {} by lecturer: {}", requestId, lecturerUsername);

        User lecturer = userRepository.findByEmail(lecturerUsername)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leaveRequest.getLecturer().getId().equals(lecturer.getId())) {
            throw new RuntimeException("You can only cancel your own leave requests");
        }

        if (leaveRequest.getStatus() == LeaveStatus.CANCELLED) {
            throw new RuntimeException("Leave request is already cancelled");
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        // Send notification to HR
        notificationService.notifyHROfLeaveCancellation(savedRequest);

        log.info("Leave request cancelled successfully");

        return convertToDTO(savedRequest);
    }

    public LeaveRequestDTO getLeaveRequestById(Long requestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        return convertToDTO(leaveRequest);
    }

    public List<LeaveRequestDTO> searchLeaveRequests(String searchTerm) {
        List<LeaveRequest> requests = leaveRequestRepository.searchLeaveRequests(searchTerm);
        return requests.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public long getPendingRequestsCount() {
        return leaveRequestRepository.countPendingRequests();
    }

    /**
     * Get remaining annual leave days for a lecturer
     */
    public int getRemainingAnnualLeaveDays(String lecturerUsername) {
        User lecturer = userRepository.findByEmail(lecturerUsername)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        int usedDays = getUsedAnnualLeaveDays(lecturer, LocalDate.now().getYear());
        return MAX_ANNUAL_LEAVE_DAYS - usedDays;
    }

    /**
     * Get used annual leave days for a specific year
     */
    public int getUsedAnnualLeaveDays(User lecturer, int year) {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesByLecturerAndDateRange(
                lecturer, yearStart, yearEnd);

        return approvedLeaves.stream()
                .mapToInt(this::calculateLeaveDays)
                .sum();
    }

    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
    }

    private void checkForOverlappingLeaves(User lecturer, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingApprovedLeaves(
                lecturer, startDate, endDate);

        if (!overlappingLeaves.isEmpty()) {
            throw new RuntimeException("You have overlapping approved leave requests for the selected dates");
        }
    }

    /**
     * Check if the requested leave would exceed the annual limit
     */
    private void checkAnnualLeaveLimit(User lecturer, LocalDate startDate, LocalDate endDate) {
        int requestedDays = calculateLeaveDays(startDate, endDate);

        // Check for each year that the leave spans
        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        for (int year = startYear; year <= endYear; year++) {
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);

            // Calculate the portion of requested leave that falls in this year
            LocalDate effectiveStart = startDate.isBefore(yearStart) ? yearStart : startDate;
            LocalDate effectiveEnd = endDate.isAfter(yearEnd) ? yearEnd : endDate;

            int daysInThisYear = calculateLeaveDays(effectiveStart, effectiveEnd);
            int usedDaysInYear = getUsedAnnualLeaveDays(lecturer, year);

            if (usedDaysInYear + daysInThisYear > MAX_ANNUAL_LEAVE_DAYS) {
                int remainingDays = MAX_ANNUAL_LEAVE_DAYS - usedDaysInYear;
                throw new RuntimeException(
                        String.format("Leave request exceeds annual limit for year %d. " +
                                        "You have %d days remaining out of %d annual leave days.",
                                year, remainingDays, MAX_ANNUAL_LEAVE_DAYS));
            }
        }
    }

    /**
     * Calculate number of leave days between two dates (inclusive)
     */
    private int calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Calculate leave days for a LeaveRequest object
     */
    private int calculateLeaveDays(LeaveRequest leaveRequest) {
        return calculateLeaveDays(leaveRequest.getStartDate(), leaveRequest.getEndDate());
    }

    private LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leaveRequest.getId());
        dto.setLecturerId(leaveRequest.getLecturer().getId());
        dto.setLecturerName(leaveRequest.getLecturer().getFullNames());
        dto.setLecturerEmail(leaveRequest.getLecturer().getEmail());
//        dto.setLecturerDepartment(leaveRequest.getLecturer().getDepartment());
        dto.setLeaveType(leaveRequest.getLeaveType());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setDescription(leaveRequest.getDescription());
        dto.setStatus(leaveRequest.getStatus());
        dto.setHrComments(leaveRequest.getHrComments());
        dto.setCreatedAt(leaveRequest.getCreatedAt());
        dto.setUpdatedAt(leaveRequest.getUpdatedAt());
        dto.setApprovedAt(leaveRequest.getApprovedAt());
        dto.setLeaveDuration(leaveRequest.getLeaveDuration());

        if (leaveRequest.getApprovedBy() != null) {
            dto.setApprovedById(leaveRequest.getApprovedBy().getId());
            dto.setApprovedByName(leaveRequest.getApprovedBy().getFullNames());
        }

        return dto;
    }
}