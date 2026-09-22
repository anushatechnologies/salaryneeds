package com.salaryneeds.service;

import com.salaryneeds.dto.CallResponseDTO;
import com.salaryneeds.dto.InitiateCallRequestDTO;

public interface CallBridgeService {

    CallResponseDTO initiateMaskedCall(Long bookingId, String customerIdHeader, String workerIdHeader, InitiateCallRequestDTO request);
}
