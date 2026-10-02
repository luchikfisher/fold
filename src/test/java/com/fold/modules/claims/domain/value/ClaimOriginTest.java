package com.fold.modules.claims.domain.value;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimOriginTest {

    @Test
    void noneShouldCreateEmptyOrigin() {
        ClaimOrigin origin =
                ClaimOrigin.none();

        assertThat(origin.isEmpty()).isTrue();
        assertThat(origin.size()).isZero();
    }

    @Test
    void shouldContainObservationReference() {
        ObservationId observationId =
                ObservationId.random();

        ClaimOriginReference.Observation reference =
                new ClaimOriginReference.Observation(
                        observationId
                );

        ClaimOrigin origin =
                ClaimOrigin.of(reference);

        assertThat(origin.contains(reference))
                .isTrue();
    }

    @Test
    void shouldSupportMultipleKindsOfProvenance() {
        ClaimOrigin origin =
                ClaimOrigin.of(
                        new ClaimOriginReference.Observation(
                                ObservationId.random()
                        ),
                        new ClaimOriginReference.Source(
                                SourceId.random()
                        ),
                        new ClaimOriginReference.Evidence(
                                EvidenceId.random()
                        ),
                        new ClaimOriginReference.Claim(
                                ClaimId.random()
                        )
                );

        assertThat(origin.size()).isEqualTo(4);
    }

    @Test
    void duplicateReferencesShouldCollapseNaturally() {
        ClaimOriginReference.Source source =
                new ClaimOriginReference.Source(
                        SourceId.random()
                );

        ClaimOrigin origin =
                ClaimOrigin.of(
                        source,
                        source
                );

        assertThat(origin.size()).isEqualTo(1);
    }

    @Test
    void constructorShouldDefensivelyCopySourceSet() {
        Set<ClaimOriginReference> source =
                new LinkedHashSet<>();

        source.add(
                new ClaimOriginReference.Source(
                        SourceId.random()
                )
        );

        ClaimOrigin origin =
                new ClaimOrigin(source);

        source.clear();

        assertThat(origin.size()).isEqualTo(1);
    }

    @Test
    void exposedReferencesShouldBeImmutable() {
        ClaimOrigin origin =
                ClaimOrigin.of(
                        new ClaimOriginReference.Source(
                                SourceId.random()
                        )
                );

        assertThatThrownBy(
                () -> origin.references().clear()
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void shouldRejectNullReferenceInsideSet() {
        Set<ClaimOriginReference> references =
                new LinkedHashSet<>();

        references.add(null);

        assertThatThrownBy(
                () -> new ClaimOrigin(references)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "references must not contain null"
                );
    }

    @Test
    void originReferenceTypesShouldPreserveIdentity() {
        ObservationId observationId =
                ObservationId.random();

        SourceId sourceId =
                SourceId.random();

        EvidenceId evidenceId =
                EvidenceId.random();

        ClaimId claimId =
                ClaimId.random();

        assertThat(
                new ClaimOriginReference.Observation(
                        observationId
                ).observationId()
        ).isEqualTo(observationId);

        assertThat(
                new ClaimOriginReference.Source(
                        sourceId
                ).sourceId()
        ).isEqualTo(sourceId);

        assertThat(
                new ClaimOriginReference.Evidence(
                        evidenceId
                ).evidenceId()
        ).isEqualTo(evidenceId);

        assertThat(
                new ClaimOriginReference.Claim(
                        claimId
                ).claimId()
        ).isEqualTo(claimId);
    }
}