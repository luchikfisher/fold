package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimLifecycleReasonTest {

    @Test
    void shouldPreserveCodeAndMessage() {
        ClaimLifecycleReason reason =
                new ClaimLifecycleReason(
                        "SOURCE_CORRECTION",
                        "The source corrected the original record"
                );

        assertThat(reason.code())
                .isEqualTo("SOURCE_CORRECTION");

        assertThat(reason.message())
                .isEqualTo(
                        "The source corrected the original record"
                );
    }

    @Test
    void shouldRejectBlankCode() {
        assertThatThrownBy(
                () -> new ClaimLifecycleReason(
                        " ",
                        "reason"
                )
        ).isInstanceOf(
                IllegalArgumentException.class
        );
    }

    @Test
    void shouldRejectBlankMessage() {
        assertThatThrownBy(
                () -> new ClaimLifecycleReason(
                        "INVALID",
                        " "
                )
        ).isInstanceOf(
                IllegalArgumentException.class
        );
    }
}