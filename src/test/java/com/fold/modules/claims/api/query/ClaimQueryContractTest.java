package com.fold.modules.claims.api.query;

import com.fold.kernel.ids.ClaimId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimQueryContractTest {

    @Test
    void getClaimQueryShouldPreserveClaimIdentity() {
        ClaimId claimId =
                ClaimId.random();

        GetClaimQuery query =
                new GetClaimQuery(
                        claimId
                );

        assertThat(query.claimId())
                .isEqualTo(claimId);
    }

    @Test
    void getClaimQueryShouldRejectNullIdentity() {
        assertThatThrownBy(
                () -> new GetClaimQuery(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "claimId must not be null"
                );
    }
}