package com.auca_hr.AUCA_HR_System.repositories;

import com.auca_hr.AUCA_HR_System.entities.Notification;
import com.auca_hr.AUCA_HR_System.entities.User;
import com.auca_hr.AUCA_HR_System.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByRecipient(@Param("recipient") User recipient);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false")
    long countUnreadNotificationsByRecipient(@Param("recipient") User recipient);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsReadByRecipient(@Param("recipient") User recipient);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient = :recipient AND n.isRead = true")
    void deleteReadNotificationsByRecipient(@Param("recipient") User recipient);

    @Query("SELECT n FROM Notification n WHERE n.leaveRequest.id = :leaveRequestId ORDER BY n.createdAt DESC")
    List<Notification> findByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);
}


