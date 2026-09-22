package com.salaryneeds.service;

import com.salaryneeds.dto.LocationHeartbeatRequest;
import com.salaryneeds.dto.LocationPingRequest;

import java.util.Map;

public interface DutyLocationService {

    Map<String, Object> toggleDuty(String workerId, Boolean requestedDuty);

    Map<String, Object> recordHeartbeat(String workerId, LocationHeartbeatRequest request);

    Map<String, Object> recordPing(String workerId, LocationPingRequest request);
}
