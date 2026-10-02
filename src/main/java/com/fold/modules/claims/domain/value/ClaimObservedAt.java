package com.fold.modules.claims.domain.value;

import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Represents the time associated with observation or reporting of the
 * assertion from which a claim was produced.
 *
 * <p>This value must not be confused with claim valid time. Observed time
 * describes when information was observed or reported; valid time describes
 * when the asserted proposition is considered applicable in the world.</p>
 *
 * <p>Unlike {@code observations.ObservedAt}, this value object does not impose
 * PostgreSQL-specific microsecond canonicalization. Claims owns its own
 * temporal semantics.</p>
 *
 * @param value observed timestamp
 */
public record ClaimObservedAt(
        Timestamp value
) {

    public ClaimObservedAt {
        Objects.requireNonNull(
                value,
                "value must not be null"
        );
    }

    public static ClaimObservedAt of(
            Timestamp value
    ) {
        return new ClaimObservedAt(value);
    }
}