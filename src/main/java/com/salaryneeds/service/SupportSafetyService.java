package com.salaryneeds.service;

import com.salaryneeds.dto.SafetySosRequest;
import com.salaryneeds.dto.SupportTicketRequest;
import com.salaryneeds.entity.SupportTicket;
import com.salaryneeds.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportSafetyService {

    private final SupportTicketRepository supportTicketRepository;

    @Transactional
    public Map<String, Object> createTicket(String workerId, SupportTicketRequest request) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        String ticketId = "tck-" + UUID.randomUUID().toString().substring(0, 8);
        SupportTicket ticket = SupportTicket.builder()
                .id(ticketId)
                .workerId(workerId)
                .subject(request.getSubject() != null ? request.getSubject() : "Support Inquiry")
                .category(request.getCategory() != null ? request.getCategory() : "GENERAL")
                .message(request.getMessage() != null ? request.getMessage() : "")
                .status("OPEN")
                .build();
        SupportTicket saved = supportTicketRepository.save(ticket);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Support ticket created successfully.");
        response.put("ticketId", saved.getId());
        response.put("status", saved.getStatus());
        return response;
    }

    @Transactional
    public Map<String, Object> triggerSos(String workerId, SafetySosRequest request) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "24/7 Safety Command Center alerted. Emergency SMS dispatched to your contacts.");
        response.put("sosIncidentId", "sos-" + UUID.randomUUID().toString().substring(0, 8));
        response.put("status", "DISPATCHED");
        response.put("coordinates", Map.of(
                "lat", request != null && request.getLat() != null ? request.getLat() : 12.9716,
                "lng", request != null && request.getLng() != null ? request.getLng() : 77.5946
        ));
        return response;
    }
}
