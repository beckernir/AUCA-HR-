package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.ChatMessageDTO;
import com.auca_hr.AUCA_HR_System.dtos.SendGroupMessageRequest;
import com.auca_hr.AUCA_HR_System.dtos.SendMessageRequest;
import com.auca_hr.AUCA_HR_System.dtos.TypingIndicator;
import com.auca_hr.AUCA_HR_System.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/chat.private")
    @SendToUser("/queue/messages")
    public ChatMessageDTO sendPrivateMessage(
            SendMessageRequest request,
            Principal principal) {

        return chatService.sendPrivateMessage(
                principal.getName(),
                request.getRecipientId(),
                request.getContent()
        );
    }

    @MessageMapping("/chat.group")
    @SendTo("/topic/room/{chatRoom}")
    public ChatMessageDTO sendGroupMessage(
            SendGroupMessageRequest request,
            Principal principal) {

        return chatService.sendGroupMessage(
                principal.getName(),
                request.getChatRoom(),
                request.getContent()
        );
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(TypingIndicator indicator, Principal principal) {
        // Handle typing indicators
        if (indicator.getRecipientId() != null) {
            // Private chat typing
            messagingTemplate.convertAndSendToUser(
                    indicator.getRecipientUsername(),
                    "/queue/typing",
                    Map.of("senderId", principal.getName(), "isTyping", indicator.isTyping())
            );
        } else if (indicator.getChatRoom() != null) {
            // Group chat typing
            messagingTemplate.convertAndSend(
                    "/topic/room/" + indicator.getChatRoom() + "/typing",
                    Map.of("sender", principal.getName(), "isTyping", indicator.isTyping())
            );
        }
    }
}

