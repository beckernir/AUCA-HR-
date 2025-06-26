//package com.auca_hr.AUCA_HR_System.repositories;
//
//
//import com.auca_hr.AUCA_HR_System.entities.ChatMessage;
//import com.auca_hr.AUCA_HR_System.entities.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
//
//    @Query("SELECT cm FROM ChatMessage cm WHERE " +
//            "(cm.sender.id = :userId1 AND cm.recipient.id = :userId2) OR " +
//            "(cm.sender.id = :userId2 AND cm.recipient.id = :userId1) " +
//            "ORDER BY cm.createdAt ASC")
//    List<ChatMessage> findPrivateConversation(@Param("userId1") Long userId1,
//                                              @Param("userId2") Long userId2);
//
//    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.createdAt ASC")
//    List<ChatMessage> findGroupConversation(@Param("chatRoom") String chatRoom);
//
//    @Query("SELECT DISTINCT CASE " +
//            "WHEN cm.sender.id = :userId THEN cm.recipient " +
//            "ELSE cm.sender END " +
//            "FROM ChatMessage cm WHERE " +
//            "(cm.sender.id = :userId OR cm.recipient.id = :userId) " +
//            "AND cm.recipient IS NOT NULL")
//    List<User> findConversationPartners(@Param("userId") Long userId);
//
//    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
//            "cm.recipient.id = :userId AND cm.isRead = false")
//    long countUnreadMessages(@Param("userId") Long userId);
//
//    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
//            "cm.recipient.id = :recipientId AND cm.sender.id = :senderId AND cm.isRead = false")
//    long countUnreadMessagesFromSender(@Param("recipientId") Long recipientId,
//                                       @Param("senderId") Long senderId);
//
//    @Modifying
//    @Query("UPDATE ChatMessage cm SET cm.isRead = true, cm.readAt = :readAt WHERE " +
//            "cm.recipient.id = :recipientId AND cm.sender.id = :senderId AND cm.isRead = false")
//    void markMessagesAsRead(@Param("recipientId") Long recipientId,
//                            @Param("senderId") Long senderId,
//                            @Param("readAt") LocalDateTime readAt);
//}

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