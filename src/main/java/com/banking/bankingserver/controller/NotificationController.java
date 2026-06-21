package com.banking.bankingserver.controller;

import com.banking.bankingserver.event.EventType;
import com.banking.bankingserver.kafka.EventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private EventProducer eventProducer;

    @Value("${notifications.service.url}")
    private String notificationsServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/events/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEvents", 0);
            stats.put("deliveredCount", 0);
            stats.put("failedCount", 0);
            stats.put("retryingCount", 0);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Get stats failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/type/{type}")
    public ResponseEntity<Map<String, Object>> getEventsByType(@PathVariable String type) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("events", new Object[0]);
            response.put("type", type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get events by type failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserEvents(@PathVariable Long userId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("events", new Object[0]);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get user events failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/failed")
    public ResponseEntity<Map<String, Object>> getFailedEvents() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("events", new Object[0]);
            response.put("status", "failed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get failed events failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/test/bulk")
    public ResponseEntity<Map<String, Object>> generateTestNotifications(
            @RequestParam(defaultValue = "100000") int count,
            @RequestParam(defaultValue = "20") int concurrency) {
        if (count < 1 || count > 1_000_000) {
            return ResponseEntity.badRequest().body(Map.of("error", "count must be between 1 and 1,000,000"));
        }
        log.info("Generate test notifications: delegating {} records to notifications-service", count);

        String url = notificationsServiceUrl + "/api/notifications/load-test/start?count=" + count + "&concurrency=" + concurrency;
        try {
            ResponseEntity<Map> nsResponse = restTemplate.postForEntity(url, null, Map.class);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "started");
            response.put("count", count);
            response.put("concurrency", concurrency);
            response.put("message", "Test notification generation started — " + count + " records will be inserted into the DB");
            response.put("notificationsServiceStatus", nsResponse.getBody());
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            log.error("Failed to delegate to notifications-service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Could not reach notifications-service: " + e.getMessage()));
        }
    }

    @GetMapping("/test/bulk/status")
    public ResponseEntity<Map<String, Object>> getGenerateStatus() {
        String url = notificationsServiceUrl + "/api/notifications/load-test/status";
        try {
            ResponseEntity<Map> nsResponse = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(new HashMap<>(nsResponse.getBody()));
        } catch (Exception e) {
            log.error("Failed to get generate status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Could not reach notifications-service: " + e.getMessage()));
        }
    }

    @PostMapping("/resend/start")
    public ResponseEntity<Map<String, Object>> startResend(
            @RequestParam(defaultValue = "100000") int limit) {
        if (limit < 1 || limit > 1_000_000) {
            return ResponseEntity.badRequest().body(Map.of("error", "limit must be between 1 and 1,000,000"));
        }
        log.info("Resend job triggered: limit={}", limit);

        String url = notificationsServiceUrl + "/api/notifications/resend/start?limit=" + limit;
        try {
            ResponseEntity<Map> nsResponse = restTemplate.postForEntity(url, null, Map.class);
            return ResponseEntity.accepted().body(new HashMap<>(nsResponse.getBody()));
        } catch (Exception e) {
            log.error("Failed to start resend job: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Could not reach notifications-service: " + e.getMessage()));
        }
    }

    @GetMapping("/resend/status")
    public ResponseEntity<Map<String, Object>> getResendStatus() {
        String url = notificationsServiceUrl + "/api/notifications/resend/status";
        try {
            ResponseEntity<Map> nsResponse = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(new HashMap<>(nsResponse.getBody()));
        } catch (Exception e) {
            log.error("Failed to get resend status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Could not reach notifications-service: " + e.getMessage()));
        }
    }

    @PostMapping("/test/send-notification")
    public ResponseEntity<Map<String, String>> sendNotification(@RequestBody Map<String, Object> request) {
        try {
            String userId    = String.valueOf(request.getOrDefault("userId", ""));
            String email     = String.valueOf(request.getOrDefault("email", ""));
            String eventType = String.valueOf(request.getOrDefault("eventType", "USER_REGISTERED"));
            String firstName = String.valueOf(request.getOrDefault("firstName", ""));
            String accountNumber = String.valueOf(request.getOrDefault("accountNumber", ""));

            Double amount = null;
            if (request.get("amount") != null) {
                amount = Double.parseDouble(String.valueOf(request.get("amount")));
            }
            String currency = String.valueOf(request.getOrDefault("currency", "USD"));

            com.banking.bankingserver.event.NotificationEvent event =
                    com.banking.bankingserver.event.NotificationEvent.builder()
                            .userId(userId)
                            .email(email)
                            .firstName(firstName)
                            .eventType(eventType)
                            .amount(amount)
                            .currency(currency)
                            .accountNumber(accountNumber)
                            .timestamp(java.time.LocalDateTime.now())
                            .build();

            eventProducer.sendNotificationEvent(event);
            log.info("Notification event sent: type={}, userId={}, email={}", eventType, userId, email);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification event sent via Kafka");
            response.put("eventType", eventType);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Send notification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/test/user-registered")
    public ResponseEntity<Map<String, String>> triggerUserRegistered(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            eventProducer.sendEvent(userId, EventType.USER_REGISTERED, "User registered successfully");
            log.info("User registered event triggered for userId: {}", userId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User registered event triggered");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Trigger user registered failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/test/deposit")
    public ResponseEntity<Map<String, String>> triggerDeposit(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            eventProducer.sendEvent(userId, EventType.DEPOSIT_COMPLETED, "Deposit completed: " + request.get("amount"));
            log.info("Deposit event triggered for userId: {}", userId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Deposit event triggered");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Trigger deposit failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/test/withdrawal")
    public ResponseEntity<Map<String, String>> triggerWithdrawal(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            eventProducer.sendEvent(userId, EventType.WITHDRAWAL_COMPLETED, "Withdrawal completed: " + request.get("amount"));
            log.info("Withdrawal event triggered for userId: {}", userId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Withdrawal event triggered");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Trigger withdrawal failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/test/transfer")
    public ResponseEntity<Map<String, String>> triggerTransfer(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            eventProducer.sendEvent(userId, EventType.TRANSFER_COMPLETED, "Transfer completed: " + request.get("amount"));
            log.info("Transfer event triggered for userId: {}", userId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transfer event triggered");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Trigger transfer failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/test/payment")
    public ResponseEntity<Map<String, String>> triggerPayment(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            eventProducer.sendEvent(userId, EventType.PAYMENT_PROCESSED, "Payment processed: " + request.get("amount"));
            log.info("Payment event triggered for userId: {}", userId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment event triggered");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Trigger payment failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> getPreferences(@PathVariable Long userId) {
        try {
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("userId", userId);
            preferences.put("emailNotifications", true);
            preferences.put("smsNotifications", false);
            preferences.put("pushNotifications", false);
            preferences.put("phoneNumber", "");
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            log.error("Get preferences failed", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> updatePreferences(@PathVariable Long userId, @RequestBody Map<String, Object> prefs) {
        try {
            Map<String, Object> response = new HashMap<>(prefs);
            response.put("userId", userId);
            response.put("updated", true);
            log.info("Preferences updated for user: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Update preferences failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
