package com.ead.notification.controllers;

import com.ead.notification.dtos.NotificationDto;
import com.ead.notification.models.NotificationModel;
import com.ead.notification.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController("/users/{userId}/notifications")
public class UserNotificationController {

    private final NotificationService notificationService;

    public UserNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping
    public ResponseEntity<Page<NotificationModel>> getAllNotificationsByUser(
            @PathVariable UUID userId,
            @PageableDefault Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(notificationService.findAllNotificationsByUser(userId, pageable));
    }


    @PreAuthorize("hasAnyRole('STUDENT')")
    @PutMapping("/{notificationId}")
    public ResponseEntity<Object> updateNotification(
            @PathVariable UUID userId,
            @PathVariable UUID notificationId,
            @RequestBody @Valid NotificationDto notificationDto
    ) {
        var notificationOptional = notificationService.findByNotificationIdAndUserId(notificationId, userId);

        if (notificationOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        notificationOptional.get().setNotificationStatus(notificationDto.getNotificationStatus());
        notificationService.saveNotification(notificationOptional.get());
        return ResponseEntity.ok(notificationOptional.get());
    }
}
