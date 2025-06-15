package com.auca_hr.AUCA_HR_System.dtos;

import com.auca_hr.AUCA_HR_System.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    private Long recipientId;

    private String recipientName;

    private Long senderId;

    private String senderName;

    private Long leaveRequestId;

    private String title;

    private String message;

    private NotificationType type;

    private boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    // Constructor for creating new notification
    public NotificationDTO(String title, String message, NotificationType type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}