package com.wooteco.nolto.notification.controller;

import com.wooteco.nolto.notification.domain.Notification;
import com.wooteco.nolto.notification.service.NotificationService;
import com.wooteco.nolto.response.Response;
import com.wooteco.nolto.user.domain.User;
import com.wooteco.nolto.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public Response getUnreadNotifications(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        User user = principalDetails.getUser();
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        return Response.success(notifications);
    }

    @PostMapping("/notifications/{notificationId}/read")
    @ResponseStatus(HttpStatus.OK)
    public Response markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return Response.success();
    }
}
