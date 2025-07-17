
package com.auca_hr.AUCA_HR_System.repositories;

import com.auca_hr.AUCA_HR_System.entities.ChatMessage;
import com.auca_hr.AUCA_HR_System.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> { // Fixed: String instead of Long

    @Query("SELECT cm FROM ChatMessage cm WHERE " +
            "(cm.sender.id = :userId1 AND cm.recipient.id = :userId2) OR " +
            "(cm.sender.id = :userId2 AND cm.recipient.id = :userId1) " +
            "ORDER BY cm.createdAt ASC")
    List<ChatMessage> findPrivateConversation(@Param("userId1") Long userId1,
                                              @Param("userId2") Long userId2);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.createdAt ASC")
    List<ChatMessage> findGroupConversation(@Param("chatRoom") String chatRoom);

    // Fixed: Simplified query to avoid Hibernate casting issues
    @Query("SELECT DISTINCT cm.sender FROM ChatMessage cm WHERE " +
            "cm.recipient.id = :userId AND cm.sender.id != :userId " +
            "UNION " +
            "SELECT DISTINCT cm.recipient FROM ChatMessage cm WHERE " +
            "cm.sender.id = :userId AND cm.recipient.id != :userId AND cm.recipient IS NOT NULL")
    List<User> findConversationPartners(@Param("userId") Long userId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
            "cm.recipient.id = :userId AND cm.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
            "cm.recipient.id = :recipientId AND cm.sender.id = :senderId AND cm.isRead = false")
    long countUnreadMessagesFromSender(@Param("recipientId") Long recipientId,
                                       @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true, cm.readAt = :readAt WHERE " +
            "cm.recipient.id = :recipientId AND cm.sender.id = :senderId AND cm.isRead = false")
    void markMessagesAsRead(@Param("recipientId") Long recipientId,
                            @Param("senderId") Long senderId,
                            @Param("readAt") LocalDateTime readAt);

    // Alternative approach - get conversation partners using separate queries
    @Query("SELECT DISTINCT cm.sender FROM ChatMessage cm WHERE cm.recipient.id = :userId")
    List<User> findMessageSenders(@Param("userId") Long userId);

    @Query("SELECT DISTINCT cm.recipient FROM ChatMessage cm WHERE cm.sender.id = :userId AND cm.recipient IS NOT NULL")
    List<User> findMessageRecipients(@Param("userId") Long userId);
}
//
//package com.auca_hr.AUCA_HR_System.repositories;
//
//import com.auca_hr.AUCA_HR_System.entities.ChatMessage;
//import com.auca_hr.AUCA_HR_System.entities.User;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
//
//    // Find private conversation between two users
//    @Query("{ $or: [ " +
//            "{ 'sender.id': ?0, 'recipient.id': ?1 }, " +
//            "{ 'sender.id': ?1, 'recipient.id': ?0 } " +
//            "] }")
//    List<ChatMessage> findPrivateConversation(Long userId1, Long userId2);
//
//    // Find group conversation by chat room
//    @Query("{ 'chatRoom': ?0 }")
//    List<ChatMessage> findGroupConversation(String chatRoom);
//
//    // Count unread messages for a user
//    @Query(value = "{ 'recipient.id': ?0, 'isRead': false }", count = true)
//    long countUnreadMessages(Long userId);
//
//    // Count unread messages from a specific sender
//    @Query(value = "{ 'recipient.id': ?0, 'sender.id': ?1, 'isRead': false }", count = true)
//    long countUnreadMessagesFromSender(Long recipientId, Long senderId);
//
//    // Find messages to mark as read
//    @Query("{ 'recipient.id': ?0, 'sender.id': ?1, 'isRead': false }")
//    List<ChatMessage> findUnreadMessagesBetweenUsers(Long recipientId, Long senderId);
//
//    // Find message senders for a user
//    @Query(value = "{ 'recipient.id': ?0 }", fields = "{ 'sender': 1 }")
//    List<ChatMessage> findMessageSenders(Long userId);
//
//    // Find message recipients for a user
//    @Query(value = "{ 'sender.id': ?0, 'recipient': { $ne: null } }", fields = "{ 'recipient': 1 }")
//    List<ChatMessage> findMessageRecipients(Long userId);
//
//    // Additional useful queries for MongoDB
//    List<ChatMessage> findBySenderIdAndRecipientIdOrderByCreatedAtAsc(Long senderId, Long recipientId);
//
//    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(String chatRoom);
//
//    List<ChatMessage> findByRecipientIdAndIsReadFalse(Long recipientId);
//}