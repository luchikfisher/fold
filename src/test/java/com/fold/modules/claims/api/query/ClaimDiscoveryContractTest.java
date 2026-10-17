package com.fold.modules.claims.api.query;

import com.fold.kernel.pagination.Cursor;
import com.fold.modules.claims.api.view.ClaimOriginReferenceView;
import com.fold.modules.claims.api.view.ClaimView;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ClaimDiscoveryContractTest {
    @Test
    void shouldRejectInvalidFiltersAndPageBoundsWithoutNormalizingExactValues() {
        assertThatThrownBy(() -> new FindClaimsQuery(null, 10, null)).isInstanceOf(NullPointerException.class);
        for (int limit : new int[] {0, -1, 101, Integer.MAX_VALUE}) {
            assertThatThrownBy(() -> new FindClaimsQuery(ClaimFilters.all(), limit, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new ClaimFilters(new ClaimView.Subject(" ", "x"), null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimFilters(null, " ", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimFilters(null, null, "active", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClaimFilters(null, null, null,
                new ClaimOriginReferenceView("source", UUID.randomUUID()), null))
                .isInstanceOf(IllegalArgumentException.class);
        var filters = new ClaimFilters(new ClaimView.Subject(" A ", "B"), " P ", "ACTIVE", null, null);
        assertThat(new FindClaimsQuery(filters, 100, new Cursor("opaque")).filters()).isEqualTo(filters);
        assertThat(filters.subject().namespace()).isEqualTo(" A ");
        assertThat(filters.predicate()).isEqualTo(" P ");
    }
}
