package com.auca_hr.AUCA_HR_System.dtos;

import com.auca_hr.AUCA_HR_System.enums.LeaveStatus;
import com.auca_hr.AUCA_HR_System.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    private Long id;

    private Long lecturerId;

    private String lecturerName;

    private String lecturerEmail;

    private String lecturerDepartment;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String description;

    private LeaveStatus status;

    private Long approvedById;

    private String approvedByName;

    private String hrComments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    private long leaveDuration;
}