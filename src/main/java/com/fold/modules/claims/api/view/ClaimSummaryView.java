package com.fold.modules.claims.api.view;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * One Claim occurrence's proposition, immutable creation context and current
 * lifecycle state at the page's database snapshot. Detail lookup supplies the
 * provenance references and full lifecycle history and may return a newer revision.
 */
public record ClaimSummaryView(
        ClaimId id,
        ClaimView.Subject subject,
        String predicate,
        ClaimObjectView object,
        ClaimView.Derivation derivation,
        Double confidence,
        Timestamp observedAt,
        ClaimView.Validity validTime,
        Timestamp createdAt,
        String status,
        long revision
) {
    public ClaimSummaryView {
        Objects.requireNonNull(id);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(object);
        Objects.requireNonNull(derivation);
        Objects.requireNonNull(createdAt);
        Objects.requireNonNull(status);
        if (revision < 1) {
            throw new IllegalArgumentException("revision must be positive");
        }
    }
}
