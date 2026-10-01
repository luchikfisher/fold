package com.fold.modules.observations.domain.repository;

import com.fold.kernel.ids.ObservationId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationAddResultTest {

    @Test
    void addedResultShouldReportSuccessfulInsertion() {
        ObservationAddResult result =
                ObservationAddResult.added();

        assertThat(result.wasAdded()).isTrue();
        assertThat(result.isDuplicate()).isFalse();
    }

    @Test
    void duplicateResultShouldExposeExistingObservation() {
        ObservationId existing =
                ObservationId.random();

        ObservationAddResult.Duplicate result =
                ObservationAddResult.duplicate(
                        existing
                );

        assertThat(result.wasAdded()).isFalse();
        assertThat(result.isDuplicate()).isTrue();

        assertThat(
                result.existingObservationId()
        ).isEqualTo(existing);
    }

    @Test
    void duplicateResultShouldRequireExistingObservation() {
        assertThatThrownBy(
                () -> ObservationAddResult.duplicate(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }
}