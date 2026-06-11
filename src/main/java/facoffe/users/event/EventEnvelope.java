package facoffe.users.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventEnvelope<T>(
    String eventId,
    String eventType,
    String occurredAt, 
    String version,
    T payload
) {
    public EventEnvelope(String eventType, T payload) {
        this(
            "evt_" + UUID.randomUUID().toString(),
            eventType,
            LocalDateTime.now().toString(), 
            "1.0",
            payload
        );
    }
}