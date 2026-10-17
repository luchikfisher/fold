package com.fold.modules.claims.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Version-one integration contract announcing that an authoritative Claim
 * has been created.
 *
 * <p>The event intentionally remains thin. Consumers that require Claim
 * details should use the public Claims query boundary rather than depending
 * on Claims domain internals.</p>
 *
 * @param claimId    authoritative Claim identity
 * @param occurredAt when the Claim was created
 */
public record ClaimCreatedV1(
        ClaimId claimId,
        Timestamp occurredAt
) implements IntegrationEvent {

    public static final String TYPE =
            "claims.claim-created";

    public ClaimCreatedV1 {
        Objects.requireNonNull(
                claimId,
                "claimId must not be null"
        );

        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );
    }

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    public EventVersion version() {
        return EventVersion.initial();
    }
}