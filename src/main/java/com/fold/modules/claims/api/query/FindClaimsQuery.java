package com.fold.modules.claims.api.query;

import com.fold.kernel.pagination.Cursor;

import java.util.Objects;

/**
 * Discovers distinct Claim occurrences in createdAt DESC, id DESC order.
 * Limit is 1..100; null cursor starts a traversal. Continuations must retain
 * the original filters, but may change the limit. Cursors are opaque positions,
 * not snapshots: concurrent creation and lifecycle changes can change later
 * page membership. This is live browsing, not a complete snapshot export.
 */
public record FindClaimsQuery(ClaimFilters filters, int limit, Cursor cursor) {
    public static final int MAX_LIMIT = 100;

    public FindClaimsQuery {
        Objects.requireNonNull(filters, "filters must not be null");
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    }
}
