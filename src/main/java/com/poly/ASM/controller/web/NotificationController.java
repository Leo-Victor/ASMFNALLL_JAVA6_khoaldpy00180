package com.poly.ASM.controller.web;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final AuthService authService;
    private final NotificationService notificationService;

    @GetMapping("/read/{id}")
    public ResponseEntity<ApiResponse<?>> read(@PathVariable("id") Long id) {
        Account user = authService.getUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Vui lòng đăng nhập", null));
        }

        Optional<Notification> notificationOpt = notificationService.findByIdAndUsername(id, user.getUsername());
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy thông báo", null));
        }

        Notification notification = notificationOpt.get();
        if (notification.getRead() == null || !notification.getRead()) {
            notificationService.markRead(notification);
        }

        String redirectUrl = "/api/home/index";
        if (notification.getOrder() != null && notification.getOrder().getId() != null) {
            if (authService.hasRole("ADMIN")) {
                redirectUrl = "/api/admin/orders/" + notification.getOrder().getId();
            } else {
                redirectUrl = "/api/orders/" + notification.getOrder().getId();
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notification.getId());
        data.put("orderId", notification.getOrder() == null ? null : notification.getOrder().getId());
        data.put("redirectUrl", redirectUrl);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công", data));
    }
}
