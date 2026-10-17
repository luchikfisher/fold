package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimStatusTest {

    @Test
    void lifecycleVocabularyShouldContainOnlyAuthoritativeStates() {
        assertThat(ClaimStatus.values())
                .containsExactly(
                        ClaimStatus.ACTIVE,
                        ClaimStatus.SUPERSEDED,
                        ClaimStatus.INVALIDATED
                );
    }
}