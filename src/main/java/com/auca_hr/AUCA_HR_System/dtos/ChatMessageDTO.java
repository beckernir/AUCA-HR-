package com.auca_hr.AUCA_HR_System.dtos;

import com.auca_hr.AUCA_HR_System.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long recipientId;
    private String recipientName;
    private String content;
    private MessageType messageType;
    private String chatRoom;
    private LocalDateTime createdAt;
    private boolean isRead;
    private LocalDateTime readAt;
}