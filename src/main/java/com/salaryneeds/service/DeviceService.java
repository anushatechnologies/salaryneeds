package com.salaryneeds.service;

import com.salaryneeds.dto.DeviceTokenRequest;
import com.salaryneeds.entity.DeviceToken;
import com.salaryneeds.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void registerDeviceToken(String workerId, DeviceTokenRequest request) {
        DeviceToken token = deviceTokenRepository.findByWorkerId(workerId).orElseGet(() ->
                DeviceToken.builder()
                        .id("dev-" + UUID.randomUUID().toString())
                        .workerId(workerId)
                        .build()
        );

        token.setToken(request.getToken());
        token.setPlatform(request.getPlatform());
        token.setDeviceName(request.getDeviceName());
        deviceTokenRepository.save(token);
    }
}
