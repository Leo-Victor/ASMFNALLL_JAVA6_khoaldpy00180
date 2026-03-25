package com.poly.ASM.controller.admin;

import com.poly.ASM.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/camera")
@RequiredArgsConstructor
public class CameraAController {

    @Value("${camera.hls-url:http://10.86.42.36:8888/index.m3u8}")
    private String cameraHlsUrl;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> index() {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin camera thành công", Map.of("cameraStreamUrl", cameraHlsUrl)));
    }
}
