package com.fold.modules.claims.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimLifecycleEventTest {
    private static final ClaimId ID = ClaimId.random();
    private static final ClaimId SUCCESSOR = ClaimId.random();
    private static final Timestamp NOW = Timestamp.parse("2026-10-16T10:00:00.123456789Z");
    private static final ClaimLifecycleReasonV1 REASON = new ClaimLifecycleReasonV1("CORRECTION", "Explicit correction");

    @Test
    void versionOneEventsShouldRoundTripWithStableTypesAndCanonicalTimes() {
        ObjectMapper mapper = new ObjectMapper();
        List<IntegrationEvent> events = List.of(
                new ClaimInvalidatedV1(ID, 2, REASON, NOW),
                new ClaimReinstatedV1(ID, 3, REASON, NOW),
                new ClaimSupersededV1(ID, SUCCESSOR, 4, REASON, NOW),
                new ClaimSupersededV1(ID, SUCCESSOR, 4, null, NOW));
        assertThat(events).extracting(IntegrationEvent::eventType).containsExactly(
                "claims.claim-invalidated", "claims.claim-reinstated",
                "claims.claim-superseded", "claims.claim-superseded");
        for (IntegrationEvent event : events) {
            String json = mapper.writeValueAsString(event);
            assertThat(event.version()).isEqualTo(EventVersion.initial());
            assertThat(json).contains("2026-10-16T10:00:00.123456Z");
            assertThat(mapper.readValue(json, event.getClass())).isEqualTo(event);
        }
    }

    @Test
    void invalidPublicContractsShouldBeRejected() {
        assertThatThrownBy(() -> new ClaimInvalidatedV1(ID, 1, REASON, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimReinstatedV1(ID, 0, REASON, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimSupersededV1(ID, SUCCESSOR, 1, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimInvalidatedV1(ID, 2, null, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ClaimReinstatedV1(ID, 2, null, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ClaimSupersededV1(ID, ID, 2, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimSupersededV1(ID, null, 2, null, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ClaimInvalidatedV1(null, 2, REASON, NOW))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ClaimReinstatedV1(ID, 2, REASON, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ClaimLifecycleReasonV1(" ", "Explanation"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimLifecycleReasonV1("CODE", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
