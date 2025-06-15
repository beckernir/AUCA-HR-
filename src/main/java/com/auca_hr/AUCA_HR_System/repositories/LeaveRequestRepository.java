package com.auca_hr.AUCA_HR_System.repositories;

import com.auca_hr.AUCA_HR_System.entities.LeaveRequest;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.LeaveStatus;
import com.auca_hr.AUCA_HR_System.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByLecturerOrderByCreatedAtDesc(User lecturer);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status);

    List<LeaveRequest> findByLecturerAndStatusOrderByCreatedAtDesc(User lecturer, LeaveStatus status);

    // Add this method to your LeaveRequestRepository interface

//    @Query("SELECT l FROM LeaveRequest l WHERE l.lecturer = :userId AND l.status = :status AND YEAR(l.startDate) = :year")
//    List<LeaveRequest> findByUserIdAndYearAndStatus(@Param("userId") String userId,
//                                             @Param("year") int year,
//                                             @Param("status") LeaveStatus status);
@Query("SELECT l FROM LeaveRequest l WHERE l.lecturer.email = :userEmail AND l.status = :status AND YEAR(l.startDate) = :year")
List<LeaveRequest> findByUserEmailAndYearAndStatus(@Param("userEmail") String userEmail,
                                                   @Param("year") int year,
                                                   @Param("status") LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.lecturer = :lecturer " +
            "AND lr.status = 'APPROVED' " +
            "AND ((lr.startDate >= :startDate AND lr.startDate <= :endDate) " +
            "OR (lr.endDate >= :startDate AND lr.endDate <= :endDate) " +
            "OR (lr.startDate <= :startDate AND lr.endDate >= :endDate))")
    List<LeaveRequest> findApprovedLeavesByLecturerAndDateRange(
            @Param("lecturer") User lecturer,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findAllPendingLeaveRequests();

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.lecturer = :lecturer AND lr.status = 'APPROVED' " +
            "AND ((lr.startDate BETWEEN :startDate AND :endDate) OR (lr.endDate BETWEEN :startDate AND :endDate) " +
            "OR (lr.startDate <= :startDate AND lr.endDate >= :endDate))")
    List<LeaveRequest> findOverlappingApprovedLeaves(@Param("lecturer") User lecturer,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.leaveType = :leaveType AND lr.status = 'APPROVED' " +
            "AND lr.lecturer = :lecturer AND YEAR(lr.startDate) = :year")
    List<LeaveRequest> findApprovedLeavesByTypeAndYear(@Param("lecturer") User lecturer,
                                                       @Param("leaveType") LeaveType leaveType,
                                                       @Param("year") int year);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = 'PENDING'")
    long countPendingRequests();

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :date AND lr.endDate >= :date " +
            "AND lr.status = 'APPROVED' ORDER BY lr.startDate")
    List<LeaveRequest> findLeaveRequestsForDate(@Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.lecturer.fullNames LIKE %:searchTerm% " +
          "OR lr.description LIKE %:searchTerm% " +
            "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> searchLeaveRequests(@Param("searchTerm") String searchTerm);
}