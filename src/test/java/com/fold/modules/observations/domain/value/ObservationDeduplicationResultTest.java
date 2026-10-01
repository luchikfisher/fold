package com.fold.modules.observations.domain.value;

import com.fold.kernel.ids.ObservationId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationDeduplicationResultTest {

    @Test
    void uniqueResultShouldReportUnique() {
        ObservationDeduplicationResult result =
                ObservationDeduplicationResult.unique();

        assertThat(result.isUnique()).isTrue();
        assertThat(result.isDuplicate()).isFalse();
    }

    @Test
    void duplicateResultShouldExposeExistingObservation() {
        ObservationId id =
                ObservationId.random();

        ObservationDeduplicationResult.Duplicate result =
                ObservationDeduplicationResult.duplicate(id);

        assertThat(result.isDuplicate()).isTrue();
        assertThat(result.isUnique()).isFalse();

        assertThat(result.existingObservationId())
                .isEqualTo(id);
    }

    @Test
    void duplicateResultShouldRequireObservationId() {
        assertThatThrownBy(
                () -> ObservationDeduplicationResult
                        .duplicate(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }
}