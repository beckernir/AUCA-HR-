package com.auca_hr.AUCA_HR_System.dtos;

import com.auca_hr.AUCA_HR_System.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceDTO {
    private LeaveType leaveType;
    private int totalAllocated;
    private int used;
    private int remaining;
}
