package com.fold.modules.claims.api.view;

import com.fold.kernel.pagination.Cursor;

import java.util.List;
import java.util.Objects;

/**
 * A bounded, coherent page without a total count. Null nextCursor means no
 * further match was visible in this statement's snapshot, not that future
 * commits cannot add matches. Items are ordered by createdAt DESC, id DESC.
 */
public record ClaimSearchPage(List<ClaimSummaryView> items, Cursor nextCursor) {
    public ClaimSearchPage {
        Objects.requireNonNull(items);
        items = List.copyOf(items);
    }
}
