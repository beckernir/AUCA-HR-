package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.dtos.ChatMessageDTO;
import com.auca_hr.AUCA_HR_System.entities.ChatMessage;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.MessageType;
import com.auca_hr.AUCA_HR_System.repositories.ChatMessageRepository;
import com.auca_hr.AUCA_HR_System.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void deleteMessage(String messageId, String username) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("Message not found"));

        // Only allow deletion if the user is the sender
        if (!message.getSender().getUsername().equals(username)) {
            throw new AccessDeniedException("You can only delete your own messages");
        }

        chatMessageRepository.delete(message);
    }
    public ChatMessageDTO sendPrivateMessage(String senderUsername, Long recipientId, String content) {
        // Add validation first
        if (recipientId == null) {
            throw new IllegalArgumentException("Recipient ID cannot be null");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        User sender = userRepository.findByEmail(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));
        System.out.println("selected user"+ sender);

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found with ID: " + recipientId));

        System.out.println("selected user"+ recipient);

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)  // Changed from .recipient to .receiver
                .content(content.trim())
                .messageType(MessageType.TEXT)
//                .timestamp(LocalDateTime.now())  // Add timestamp
                .isRead(false)  // Add read status
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);
        ChatMessageDTO messageDTO = convertToDTO(savedMessage);

        // Send to recipient via WebSocket
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/messages",
                messageDTO
        );

        // Also send to sender for confirmation
        messagingTemplate.convertAndSendToUser(
                sender.getUsername(),
                "/queue/messages",
                messageDTO
        );

        log.info("Private message sent from {} to {} with ID: {}",
                sender.getUsername(), recipient.getUsername(), savedMessage.getId());
        return messageDTO;
    }


    public ChatMessageDTO sendGroupMessage(String senderUsername, String chatRoom, String content) {
        User sender = userRepository.findByEmail(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .chatRoom(chatRoom)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);
        ChatMessageDTO messageDTO = convertToDTO(savedMessage);

        // Broadcast to all users in the chat room
        messagingTemplate.convertAndSend("/topic/room/" + chatRoom, messageDTO);

        log.info("Group message sent by {} to room {}", sender.getUsername(), chatRoom);
        return messageDTO;
    }

    public List<ChatMessageDTO> getPrivateConversation(String username, Long otherUserId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatMessage> messages = chatMessageRepository.findPrivateConversation(user.getId(), otherUserId);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getGroupConversation(String chatRoom) {
        List<ChatMessage> messages = chatMessageRepository.findGroupConversation(chatRoom);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<User> getConversationPartners(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatMessageRepository.findConversationPartners(user.getId());
    }

    public long getUnreadMessagesCount(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatMessageRepository.countUnreadMessages(user.getId());
    }

    public long getUnreadMessagesFromSender(String username, Long senderId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatMessageRepository.countUnreadMessagesFromSender(user.getId(), senderId);
    }

    public void markMessagesAsRead(String username, Long senderId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        chatMessageRepository.markMessagesAsRead(user.getId(), senderId, LocalDateTime.now());

        // Notify sender that messages have been read
        messagingTemplate.convertAndSendToUser(
                userRepository.findById(senderId).get().getUsername(),
                "/queue/read-receipt",
                Map.of("recipientId", user.getId(), "readAt", LocalDateTime.now())
        );
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullNames())
//                .senderAvatar(message.getSender().getProfilePicture()) // if you have this field
                .recipientId(message.getRecipient() != null ? message.getRecipient().getId() : null)
                .recipientName(message.getRecipient() != null ? message.getRecipient().getFullNames() : null)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .chatRoom(message.getChatRoom())
                .createdAt(message.getCreatedAt())
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .build();
    }
}


//package com.auca_hr.AUCA_HR_System.services;
//
//import com.auca_hr.AUCA_HR_System.dtos.ChatMessageDTO;
//import com.auca_hr.AUCA_HR_System.entities.ChatMessage;
//import com.auca_hr.AUCA_HR_System.entities.User;
//import com.auca_hr.AUCA_HR_System.enums.MessageType;
//import com.auca_hr.AUCA_HR_System.repositories.ChatMessageRepository;
//import com.auca_hr.AUCA_HR_System.repositories.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ChatService {
//
//    private final ChatMessageRepository chatMessageRepository;
//    private final UserRepository userRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MongoTemplate mongoTemplate;
//
//    public void deleteMessage(String messageId, String username) {
//        ChatMessage message = chatMessageRepository.findById(messageId)
//                .orElseThrow(() -> new NoSuchElementException("Message not found"));
//
//        // Only allow deletion if the user is the sender
//        if (!message.getSender().getUsername().equals(username)) {
//            throw new AccessDeniedException("You can only delete your own messages");
//        }
//
//        chatMessageRepository.delete(message);
//    }
//
//    public ChatMessageDTO sendPrivateMessage(String senderUsername, Long recipientId, String content) {
//        // Add validation first
//        if (recipientId == null) {
//            throw new IllegalArgumentException("Recipient ID cannot be null");
//        }
//
//        if (content == null || content.trim().isEmpty()) {
//            throw new IllegalArgumentException("Message content cannot be empty");
//        }
//
//        User sender = userRepository.findByEmail(senderUsername)
//                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));
//        System.out.println("selected user" + sender);
//
//        User recipient = userRepository.findById(recipientId)
//                .orElseThrow(() -> new RuntimeException("Recipient not found with ID: " + recipientId));
//
//        System.out.println("selected user" + recipient);
//
//        ChatMessage message = ChatMessage.builder()
//                .sender(sender)
//                .recipient(recipient)
//                .content(content.trim())
//                .messageType(MessageType.TEXT)
//                .isRead(false)
//                .build();
//
//        // Set creation time before saving
//        message.onCreate();
//        ChatMessage savedMessage = chatMessageRepository.save(message);
//        ChatMessageDTO messageDTO = convertToDTO(savedMessage);
//
//        // Send to recipient via WebSocket
//        messagingTemplate.convertAndSendToUser(
//                recipient.getUsername(),
//                "/queue/messages",
//                messageDTO
//        );
//
//        // Also send to sender for confirmation
//        messagingTemplate.convertAndSendToUser(
//                sender.getUsername(),
//                "/queue/messages",
//                messageDTO
//        );
//
//        log.info("Private message sent from {} to {} with ID: {}",
//                sender.getUsername(), recipient.getUsername(), savedMessage.getId());
//        return messageDTO;
//    }
//
//    public ChatMessageDTO sendGroupMessage(String senderUsername, String chatRoom, String content) {
//        User sender = userRepository.findByEmail(senderUsername)
//                .orElseThrow(() -> new RuntimeException("Sender not found"));
//
//        ChatMessage message = ChatMessage.builder()
//                .sender(sender)
//                .content(content)
//                .messageType(MessageType.TEXT)
//                .chatRoom(chatRoom)
//                .build();
//
//        // Set creation time before saving
//        message.onCreate();
//        ChatMessage savedMessage = chatMessageRepository.save(message);
//        ChatMessageDTO messageDTO = convertToDTO(savedMessage);
//
//        // Broadcast to all users in the chat room
//        messagingTemplate.convertAndSend("/topic/room/" + chatRoom, messageDTO);
//
//        log.info("Group message sent by {} to room {}", sender.getUsername(), chatRoom);
//        return messageDTO;
//    }
//
//    public List<ChatMessageDTO> getPrivateConversation(String username, Long otherUserId) {
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        List<ChatMessage> messages = chatMessageRepository.findPrivateConversation(user.getId(), otherUserId)
//                .stream()
//                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
//                .collect(Collectors.toList());
//
//        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
//    }
//
//    public List<ChatMessageDTO> getGroupConversation(String chatRoom) {
//        List<ChatMessage> messages = chatMessageRepository.findGroupConversation(chatRoom)
//                .stream()
//                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
//                .collect(Collectors.toList());
//
//        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
//    }
//
//    public List<User> getConversationPartners(String username) {
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // MongoDB implementation using aggregation-like approach
//        List<ChatMessage> senderMessages = chatMessageRepository.findMessageSenders(user.getId());
//        List<ChatMessage> recipientMessages = chatMessageRepository.findMessageRecipients(user.getId());
//
//        List<User> partners = senderMessages.stream()
//                .map(ChatMessage::getSender)
//                .filter(u -> !u.getId().equals(user.getId()))
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<User> recipients = recipientMessages.stream()
//                .map(ChatMessage::getRecipient)
//                .filter(u -> u != null && !u.getId().equals(user.getId()))
//                .distinct()
//                .collect(Collectors.toList());
//
//        // Combine and remove duplicates
//        partners.addAll(recipients);
//        return partners.stream().distinct().collect(Collectors.toList());
//    }
//
//    public long getUnreadMessagesCount(String username) {
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return chatMessageRepository.countUnreadMessages(user.getId());
//    }
//
//    public long getUnreadMessagesFromSender(String username, Long senderId) {
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return chatMessageRepository.countUnreadMessagesFromSender(user.getId(), senderId);
//    }
//
//    public void markMessagesAsRead(String username, Long senderId) {
//        User user = userRepository.findByEmail(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Using MongoTemplate for bulk update operation
//        Query query = new Query(Criteria.where("recipient.id").is(user.getId())
//                .and("sender.id").is(senderId)
//                .and("isRead").is(false));
//
//        Update update = new Update()
//                .set("isRead", true)
//                .set("readAt", LocalDateTime.now());
//
//        mongoTemplate.updateMulti(query, update, ChatMessage.class);
//
//        // Notify sender that messages have been read
//        User sender = userRepository.findById(senderId).orElse(null);
//        if (sender != null) {
//            messagingTemplate.convertAndSendToUser(
//                    sender.getUsername(),
//                    "/queue/read-receipt",
//                    Map.of("recipientId", user.getId(), "readAt", LocalDateTime.now())
//            );
//        }
//    }
//
//    private ChatMessageDTO convertToDTO(ChatMessage message) {
//        return ChatMessageDTO.builder()
//                .id(message.getId())
//                .senderId(message.getSender().getId())
//                .senderName(message.getSender().getFullNames())
//                .recipientId(message.getRecipient() != null ? message.getRecipient().getId() : null)
//                .recipientName(message.getRecipient() != null ? message.getRecipient().getFullNames() : null)
//                .content(message.getContent())
//                .messageType(message.getMessageType())
//                .chatRoom(message.getChatRoom())
//                .createdAt(message.getCreatedAt())
//                .isRead(message.isRead())
//                .readAt(message.getReadAt())
//                .build();
//    }
//}