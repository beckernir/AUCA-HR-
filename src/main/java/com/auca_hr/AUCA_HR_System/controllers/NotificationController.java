//package com.auca_hr.AUCA_HR_System.controllers;
//
//import com.auca_hr.AUCA_HR_System.dtos.NotificationDTO;
//import com.auca_hr.AUCA_HR_System.services.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/notifications")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*", maxAge = 3600)
//public class NotificationController {
//
//    private final NotificationService notificationService;
//
//    @GetMapping
//    public ResponseEntity<List<NotificationDTO>> getAllNotifications(Authentication authentication) {
//        log.info("Fetching all notifications for user: {}", authentication.getName());
//        List<NotificationDTO> notifications = notificationService.getNotificationsByUser(authentication.getName());
//        return ResponseEntity.ok(notifications);
//    }
//
//    @GetMapping("/unread")
//    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
//        log.info("Fetching unread notifications for user: {}", authentication.getName());
//        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByUser(authentication.getName());
//        return ResponseEntity.ok(notifications);
//    }
//
//    @GetMapping("/unread/count")
//    public ResponseEntity<Long> getUnreadNotificationsCount(Authentication authentication) {
//        log.info("Fetching unread notifications count for user: {}", authentication.getName());
//        long count = notificationService.getUnreadNotificationsCount(authentication.getName());
//        return ResponseEntity.ok(count);
//    }
//
//    @PutMapping("/{notificationId}/read")
//    public ResponseEntity<NotificationDTO> markNotificationAsRead(
//            @PathVariable Long notificationId,
//            Authentication authentication) {
//        log.info("Marking notification {} as read for user: {}", notificationId, authentication.getName());
//        NotificationDTO notification = notificationService.markNotificationAsRead(notificationId, authentication.getName());
//        return ResponseEntity.ok(notification);
//    }
//
//    @PutMapping("/mark-all-read")
//    public ResponseEntity<Void> markAllNotificationsAsRead(Authentication authentication) {
//        log.info("Marking all notifications as read for user: {}", authentication.getName());
//        notificationService.markAllNotificationsAsRead(authentication.getName());
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/read")
//    public ResponseEntity<Void> deleteReadNotifications(Authentication authentication) {
//        log.info("Deleting read notifications for user: {}", authentication.getName());
//        notificationService.deleteReadNotifications(authentication.getName());
//        return ResponseEntity.ok().build();
//    }
//
//    // WebSocket message mapping for real-time notifications
//    @MessageMapping("/notifications/subscribe")
//    @SendTo("/queue/notifications")
//    public String subscribeToNotifications(SimpMessageHeaderAccessor headerAccessor) {
//        String username = headerAccessor.getUser().getName();
//        log.info("User {} subscribed to notifications", username);
//        return "Subscribed to notifications";
//    }
//
//    // Endpoint to test WebSocket notifications (for development/testing)
//    @PostMapping("/test")
//    public ResponseEntity<String> testNotification(
//            @RequestParam String message,
//            Authentication authentication) {
//        log.info("Sending test notification to user: {}", authentication.getName());
//        // This would typically be called internally by the service
//        return ResponseEntity.ok("Test notification sent");
//    }
//}


package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.NotificationDTO;
import com.auca_hr.AUCA_HR_System.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(Authentication authentication) {
        log.info("Fetching all notifications for user: {}", authentication.getName());
        List<NotificationDTO> notifications = notificationService.getNotificationsByUser(authentication.getName());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        log.info("Fetching unread notifications for user: {}", authentication.getName());
        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByUser(authentication.getName());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationsCount(Authentication authentication) {
        log.info("Fetching unread notifications count for user: {}", authentication.getName());
        long count = notificationService.getUnreadNotificationsCount(authentication.getName());
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        log.info("Marking notification {} as read for user: {}", notificationId, authentication.getName());
        NotificationDTO notification = notificationService.markNotificationAsRead(notificationId, authentication.getName());
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(Authentication authentication) {
        log.info("Marking all notifications as read for user: {}", authentication.getName());
        notificationService.markAllNotificationsAsRead(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteReadNotifications(Authentication authentication) {
        log.info("Deleting read notifications for user: {}", authentication.getName());
        notificationService.deleteReadNotifications(authentication.getName());
        return ResponseEntity.ok().build();
    }
}