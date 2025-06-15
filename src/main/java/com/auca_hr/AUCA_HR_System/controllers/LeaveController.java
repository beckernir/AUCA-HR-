

package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.LeaveRequestDTO;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.services.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<LeaveRequestDTO> submitLeaveRequest(
             @RequestBody LeaveRequestDTO leaveRequestDTO,
            Authentication authentication) {
        log.info("Submitting leave request for lecturer: {}", authentication.getName());
        try {
            LeaveRequestDTO savedRequest = leaveService.submitLeaveRequest(leaveRequestDTO, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (RuntimeException e) {
            log.error("Error submitting leave request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaveRequests(Authentication authentication) {
        log.info("Fetching leave requests for lecturer: {}", authentication.getName());
        List<LeaveRequestDTO> requests = leaveService.getLeaveRequestsByLecturer(authentication.getName());
        return ResponseEntity.ok(requests);
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaveRequests() {
        log.info("Fetching all leave requests");
        List<LeaveRequestDTO> requests = leaveService.getAllLeaveRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDTO>> getPendingLeaveRequests() {
        log.info("Fetching pending leave requests");
        List<LeaveRequestDTO> requests = leaveService.getAllPendingLeaveRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending/count")
    public ResponseEntity<Long> getPendingRequestsCount() {
        log.info("Fetching pending requests count");
        long count = leaveService.getPendingRequestsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<LeaveRequestDTO> getLeaveRequestById(@PathVariable Long requestId) {
        log.info("Fetching leave request by ID: {}", requestId);
        try {
            LeaveRequestDTO request = leaveService.getLeaveRequestById(requestId);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            log.error("Leave request not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<LeaveRequestDTO>> searchLeaveRequests(@RequestParam String searchTerm) {
        log.info("Searching leave requests with term: {}", searchTerm);
        List<LeaveRequestDTO> requests = leaveService.searchLeaveRequests(searchTerm);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<LeaveRequestDTO> approveLeaveRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String comments,
            Authentication authentication) {
        log.info("Approving leave request ID: {} by HR: {}", requestId, authentication.getName());
        try {
            LeaveRequestDTO approvedRequest = leaveService.approveLeaveRequest(requestId, authentication.getName(), comments);
            return ResponseEntity.ok(approvedRequest);
        } catch (RuntimeException e) {
            log.error("Error approving leave request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<LeaveRequestDTO> rejectLeaveRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String comments,
            Authentication authentication) {
        log.info("Rejecting leave request ID: {} by HR: {}", requestId, authentication.getName());
        try {
            LeaveRequestDTO rejectedRequest = leaveService.rejectLeaveRequest(requestId, authentication.getName(), comments);
            return ResponseEntity.ok(rejectedRequest);
        } catch (RuntimeException e) {
            log.error("Error rejecting leave request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<LeaveRequestDTO> cancelLeaveRequest(
            @PathVariable Long requestId,
            Authentication authentication) {
        log.info("Cancelling leave request ID: {} by lecturer: {}", requestId, authentication.getName());
        try {
            LeaveRequestDTO cancelledRequest = leaveService.cancelLeaveRequest(requestId, authentication.getName());
            return ResponseEntity.ok(cancelledRequest);
        } catch (RuntimeException e) {
            log.error("Error cancelling leave request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

//    @GetMapping("/balance")
//    public ResponseEntity<Integer> getLeaveBalance(@RequestParam Long userId,
//                                                   @RequestParam int year) {
//        int balance = leaveService.getRemainingLeaveBalance(userId, year);
//        return ResponseEntity.ok(balance);
//    }
@GetMapping("/balance")
public ResponseEntity<Integer> getLeaveBalance(@RequestParam int year,
                                               Authentication authentication) {
    try {
        log.info("Fetching leave balance for user: {} for year: {}", authentication.getName(), year);

//        // Get user from authentication
//        String username = authentication.getName();
//        User user = userService.findByUsername(username);
//
//        if (user == null) {
//            log.error("User not found: {}", username);
//            return ResponseEntity.notFound().build();
//        }

        // Call service method - adjust based on your actual method signature
        int balance = leaveService.getRemainingLeaveBalance(authentication.getName(), year);
        return ResponseEntity.ok(balance);

    } catch (RuntimeException e) {
        log.error("Error fetching leave balance: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
}