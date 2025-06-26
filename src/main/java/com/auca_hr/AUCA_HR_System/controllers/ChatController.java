package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.ChatMessageDTO;
import com.auca_hr.AUCA_HR_System.dtos.SendGroupMessageRequest;
import com.auca_hr.AUCA_HR_System.dtos.SendMessageRequest;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

// 6. Chat Controller
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/private")
    public ResponseEntity<ChatMessageDTO> sendPrivateMessage(
            @RequestBody SendMessageRequest request,
            Principal principal) {

        ChatMessageDTO message = chatService.sendPrivateMessage(
                principal.getName(),
                request.getRecipientId(),
                request.getContent()
        );

        return ResponseEntity.ok(message);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatMessageDTO> sendGroupMessage(
            @RequestBody SendGroupMessageRequest request,
            Principal principal) {

        ChatMessageDTO message = chatService.sendGroupMessage(
                principal.getName(),
                request.getChatRoom(),
                request.getContent()
        );

        return ResponseEntity.ok(message);
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getPrivateConversation(
            @PathVariable Long userId,
            Principal principal) {

        List<ChatMessageDTO> messages = chatService.getPrivateConversation(principal.getName(), userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/room/{chatRoom}")
    public ResponseEntity<List<ChatMessageDTO>> getGroupConversation(@PathVariable String chatRoom) {
        List<ChatMessageDTO> messages = chatService.getGroupConversation(chatRoom);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/partners")
    public ResponseEntity<List<User>> getConversationPartners(Principal principal) {
        List<User> partners = chatService.getConversationPartners(principal.getName());
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessagesCount(Principal principal) {
        long count = chatService.getUnreadMessagesCount(principal.getName());
        return ResponseEntity.ok(count);
    }

    @PostMapping("/mark-read/{senderId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long senderId,
            Principal principal) {

        chatService.markMessagesAsRead(principal.getName(), senderId);
        return ResponseEntity.ok().build();
    }
}
