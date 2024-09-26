package com.wooteco.nolto.notification.domain.repository;

import com.wooteco.nolto.notification.domain.Notification;
import com.wooteco.nolto.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserAndIsReadFalse(User user);
}
