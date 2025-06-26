package com.auca_hr.AUCA_HR_System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {
    private Long recipientId;
    private String recipientUsername;
    private String chatRoom;
    private boolean isTyping;
}