package com.auca_hr.AUCA_HR_System.controllers;

import com.auca_hr.AUCA_HR_System.dtos.ChatMessageDTO;
import com.auca_hr.AUCA_HR_System.dtos.SendMessageRequest;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.MessageType;
import com.auca_hr.AUCA_HR_System.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"*"}) // Add your frontend URLs
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/conversation/private/{otherUserId}")
    public ResponseEntity<List<ChatMessageDTO>> getPrivateConversation(
            @PathVariable Long otherUserId,
            Principal principal) {

        List<ChatMessageDTO> messages = chatService.getPrivateConversation(
                principal.getName(),
                otherUserId
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/group/{chatRoom}")
    public ResponseEntity<List<ChatMessageDTO>> getGroupConversation(
            @PathVariable String chatRoom) {

        List<ChatMessageDTO> messages = chatService.getGroupConversation(chatRoom);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/partners")
    public ResponseEntity<List<User>> getConversationPartners(Principal principal) {
        List<User> partners = chatService.getConversationPartners(principal.getName());
        return ResponseEntity.ok(partners);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadMessagesCount(Principal principal) {
        long count = chatService.getUnreadMessagesCount(principal.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @GetMapping("/unread/sender/{senderId}")
    public ResponseEntity<Map<String, Long>> getUnreadMessagesFromSender(
            @PathVariable Long senderId,
            Principal principal) {

        long count = chatService.getUnreadMessagesFromSender(principal.getName(), senderId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/messages/read/{senderId}")
    public ResponseEntity<Map<String, String>> markMessagesAsRead(
            @PathVariable Long senderId,
            Principal principal) {

        chatService.markMessagesAsRead(principal.getName(), senderId);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/messages")
    public ResponseEntity<Map<String, String>> sendMessage(
            @RequestBody SendMessageRequest request,
            Principal principal) {

        // You'll need to implement this in your ChatService
        chatService.sendPrivateMessage(
                principal.getName(),
                request.getRecipientId(),
                request.getContent()
//                request.ge(MessageType.TEXT)
        );

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable String messageId,
            Principal principal) {

        // You'll need to implement this in your ChatService
        chatService.deleteMessage(messageId, principal.getName());

        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
