package com.salaryneeds.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // Active worker sessions map: workerId -> WebSocketSession
    private static final Map<String, WebSocketSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        String workerId = (token != null && !token.isBlank()) ? token : "worker-" + session.getId();

        if (workerId == null) {
            workerId = "worker-" + session.getId();
        }

        session.getAttributes().put("workerId", workerId);
        ACTIVE_SESSIONS.put(workerId, session);
        log.info("WebSocket connection established for worker: {}", workerId);

        // Send welcome/connection ack
        session.sendMessage(new TextMessage("{\"event\":\"connected\",\"message\":\"Connected to /ws/worker telemetry stream\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String event = node.has("event") ? node.get("event").asText() : "";

            if ("location:update".equalsIgnoreCase(event)) {
                // High-frequency telemetry stream
                log.debug("Received location telemetry: {}", node);
            }
        } catch (Exception e) {
            log.warn("Failed to parse websocket message: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String workerId = (String) session.getAttributes().get("workerId");
        if (workerId != null) {
            ACTIVE_SESSIONS.remove(workerId);
            log.info("WebSocket connection closed for worker: {}", workerId);
        }
    }

    /**
     * Broadcasts a new dispatch lead to all connected technicians.
     */
    public static void broadcastDispatchLead(Object lead, ObjectMapper mapper) {
        broadcastEvent("dispatch:new_lead", Map.of("lead", lead), mapper);
    }

    /**
     * Dismisses radar lead modal if claimed by another technician.
     */
    public static void broadcastBookingClaimed(String bookingId, ObjectMapper mapper) {
        broadcastEvent("booking:claimed", Map.of("booking_id", bookingId), mapper);
    }

    /**
     * Notifies technicians of customer cancellation.
     */
    public static void broadcastBookingCancelled(String bookingId, String reason, ObjectMapper mapper) {
        broadcastEvent("booking:cancelled", Map.of("booking_id", bookingId, "reason", reason != null ? reason : "Customer cancelled"), mapper);
    }

    /**
     * Sends settlement confirmation to a specific worker.
     */
    public static void sendSettlementCompleted(String workerId, Object settlementData, ObjectMapper mapper) {
        WebSocketSession session = ACTIVE_SESSIONS.get(workerId);
        if (session != null && session.isOpen()) {
            try {
                String payload = mapper.writeValueAsString(Map.of("event", "settlement:completed", "data", settlementData));
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                log.warn("Failed to send settlement:completed to worker {}: {}", workerId, e.getMessage());
            }
        }
    }

    private static void broadcastEvent(String eventName, Object data, ObjectMapper mapper) {
        String payload;
        try {
            payload = mapper.writeValueAsString(Map.of("event", eventName, "data", data));
        } catch (Exception e) {
            return;
        }
        for (WebSocketSession session : ACTIVE_SESSIONS.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(payload));
                } catch (IOException ignored) {}
            }
        }
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            for (String param : uri.getQuery().split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equalsIgnoreCase(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}
