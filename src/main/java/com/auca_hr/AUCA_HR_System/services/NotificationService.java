package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.dtos.NotificationDTO;
import com.auca_hr.AUCA_HR_System.entities.LeaveRequest;
import com.auca_hr.AUCA_HR_System.entities.Notification;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.NotificationType;
import com.auca_hr.AUCA_HR_System.repositories.NotificationRepository;
import com.auca_hr.AUCA_HR_System.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyHROfNewLeaveRequest(LeaveRequest leaveRequest) {
        log.info("Sending notification to HR for new leave request ID: {}", leaveRequest.getId());

        List<User> hrUsers = userRepository.findAllActiveHRUsers();

        String title = "New Leave Request Submitted";
        // Fixed: Added the missing lecturer's full name argument
        String message = String.format("%s has submitted a new %s request from %s to %s. Reason: %s",
                leaveRequest.getLecturer().getFullNames(),
                leaveRequest.getLeaveType().toString().replace("_", " ").toLowerCase(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getDescription());

        for (User hrUser : hrUsers) {
            Notification notification = createNotification(
                    hrUser,
                    leaveRequest.getLecturer(),
                    leaveRequest,
                    title,
                    message,
                    NotificationType.LEAVE_REQUEST_SUBMITTED
            );

            Notification savedNotification = notificationRepository.save(notification);

            // Send real-time notification via WebSocket
            NotificationDTO notificationDTO = convertToDTO(savedNotification);
            messagingTemplate.convertAndSendToUser(
                    hrUser.getUsername(),
                    "/queue/notifications",
                    notificationDTO
            );
        }

        log.info("Notifications sent to {} HR users", hrUsers.size());
    }

    public void notifyLecturerOfLeaveApproval(LeaveRequest leaveRequest) {
        log.info("Sending approval notification to lecturer for leave request ID: {}", leaveRequest.getId());

        String title = "Leave Request Approved";
        // Fixed: Corrected the format string structure
        String message = String.format("Your %s request from %s to %s has been approved by %s. %s",
                leaveRequest.getLeaveType().toString().replace("_", " ").toLowerCase(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getApprovedBy().getFullNames(),
                leaveRequest.getHrComments() != null ? "Comments: " + leaveRequest.getHrComments() : "");

        Notification notification = createNotification(
                leaveRequest.getLecturer(),
                leaveRequest.getApprovedBy(),
                leaveRequest,
                title,
                message,
                NotificationType.LEAVE_REQUEST_APPROVED
        );

        Notification savedNotification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationDTO notificationDTO = convertToDTO(savedNotification);
        messagingTemplate.convertAndSendToUser(
                leaveRequest.getLecturer().getUsername(),
                "/queue/notifications",
                notificationDTO
        );

        log.info("Approval notification sent to lecturer: {}", leaveRequest.getLecturer().getUsername());
    }

    public void notifyLecturerOfLeaveRejection(LeaveRequest leaveRequest) {
        log.info("Sending rejection notification to lecturer for leave request ID: {}", leaveRequest.getId());

        String title = "Leave Request Rejected";
        // Fixed: Corrected the format string structure
        String message = String.format("Your %s request from %s to %s has been rejected by %s. %s",
                leaveRequest.getLeaveType().toString().replace("_", " ").toLowerCase(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getApprovedBy().getFullNames(),
                leaveRequest.getHrComments() != null ? "Reason: " + leaveRequest.getHrComments() : "");

        Notification notification = createNotification(
                leaveRequest.getLecturer(),
                leaveRequest.getApprovedBy(),
                leaveRequest,
                title,
                message,
                NotificationType.LEAVE_REQUEST_REJECTED
        );

        Notification savedNotification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationDTO notificationDTO = convertToDTO(savedNotification);
        messagingTemplate.convertAndSendToUser(
                leaveRequest.getLecturer().getUsername(),
                "/queue/notifications",
                notificationDTO
        );

        log.info("Rejection notification sent to lecturer: {}", leaveRequest.getLecturer().getUsername());
    }

    public void notifyHROfLeaveCancellation(LeaveRequest leaveRequest) {
        log.info("Sending cancellation notification to HR for leave request ID: {}", leaveRequest.getId());

        List<User> hrUsers = userRepository.findAllActiveHRUsers();

        String title = "Leave Request Cancelled";
        String message = String.format("%s has cancelled their %s request from %s to %s.",
                leaveRequest.getLecturer().getFullNames(),
                leaveRequest.getLeaveType().toString().replace("_", " ").toLowerCase(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate());

        for (User hrUser : hrUsers) {
            Notification notification = createNotification(
                    hrUser,
                    leaveRequest.getLecturer(),
                    leaveRequest,
                    title,
                    message,
                    NotificationType.LEAVE_REQUEST_CANCELLED
            );

            Notification savedNotification = notificationRepository.save(notification);

            // Send real-time notification via WebSocket
            NotificationDTO notificationDTO = convertToDTO(savedNotification);
            messagingTemplate.convertAndSendToUser(
                    hrUser.getUsername(),
                    "/queue/notifications",
                    notificationDTO
            );
        }

        log.info("Cancellation notifications sent to {} HR users", hrUsers.size());
    }

    public List<NotificationDTO> getNotificationsByUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotificationsByUser(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findUnreadNotificationsByRecipient(user);
        return notifications.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public long getUnreadNotificationsCount(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countUnreadNotificationsByRecipient(user);
    }

    public NotificationDTO markNotificationAsRead(Long notificationId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("You can only mark your own notifications as read");
        }

        notification.markAsRead();
        Notification savedNotification = notificationRepository.save(notification);

        return convertToDTO(savedNotification);
    }

    public void markAllNotificationsAsRead(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.markAllAsReadByRecipient(user);

        log.info("All notifications marked as read for user: {}", username);
    }

    public void deleteReadNotifications(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.deleteReadNotificationsByRecipient(user);

        log.info("Read notifications deleted for user: {}", username);
    }

    private Notification createNotification(User recipient, User sender, LeaveRequest leaveRequest,
                                            String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setLeaveRequest(leaveRequest);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        return notification;
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setRecipientName(notification.getRecipient().getFullNames());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());

        if (notification.getSender() != null) {
            dto.setSenderId(notification.getSender().getId());
            dto.setSenderName(notification.getSender().getFullNames());
        }

        if (notification.getLeaveRequest() != null) {
            dto.setLeaveRequestId(notification.getLeaveRequest().getId());
        }

        return dto;
    }
}