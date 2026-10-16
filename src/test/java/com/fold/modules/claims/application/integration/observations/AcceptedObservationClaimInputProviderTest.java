package com.fold.modules.claims.application.integration.observations;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcceptedObservationClaimInputProviderTest {

    @Test
    void shouldQueryExactlyEventObservation() {
        ObservationView observation =
                observation("ACCEPTED");

        AtomicReference<ObservationId> queried =
                new AtomicReference<>();

        ObservationQueries queries =
                query -> {
                    queried.set(
                            query.observationId()
                    );

                    return Optional.of(
                            observation
                    );
                };

        AcceptedObservationClaimInputProvider provider =
                new AcceptedObservationClaimInputProvider(
                        queries,
                        new ObservationClaimDerivationInputMapper()
                );

        ClaimDerivationInput input =
                provider.load(
                        event(observation.id())
                );

        assertThat(queried.get())
                .isEqualTo(
                        observation.id()
                );

        assertThat(input.observationId())
                .isEqualTo(
                        observation.id()
                );
    }

    @Test
    void missingObservationShouldFailExplicitly() {
        ObservationId id =
                ObservationId.random();

        AcceptedObservationClaimInputProvider provider =
                new AcceptedObservationClaimInputProvider(
                        query -> Optional.empty(),
                        new ObservationClaimDerivationInputMapper()
                );

        assertThatThrownBy(
                () -> provider.load(
                        event(id)
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        id.toString()
                );
    }

    @Test
    void eventMustNotOverrideAuthoritativeStatus() {
        ObservationView rejected =
                observation("REJECTED");

        AcceptedObservationClaimInputProvider provider =
                new AcceptedObservationClaimInputProvider(
                        query ->
                                Optional.of(rejected),
                        new ObservationClaimDerivationInputMapper()
                );

        assertThatThrownBy(
                () -> provider.load(
                        event(rejected.id())
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "REJECTED"
                );
    }

    private static ObservationAcceptedV1 event(
            ObservationId id
    ) {
        return new ObservationAcceptedV1(
                id,
                Timestamp.parse(
                        "2026-10-03T10:00:00Z"
                )
        );
    }

    private static ObservationView observation(
            String status
    ) {
        return new ObservationView(
                ObservationId.random(),
                "test-type",
                new ObservationView.Subject(
                        "organization",
                        "registry:123"
                ),
                List.of(),
                new ObservationView.Origin(
                        SourceId.random(),
                        null,
                        null
                ),
                new ObservationView.Fingerprint(
                        1,
                        "SHA-256",
                        "a".repeat(64)
                ),
                Timestamp.parse(
                        "2026-10-03T08:00:00Z"
                ),
                Timestamp.parse(
                        "2026-10-03T08:01:00Z"
                ),
                status,
                Timestamp.parse(
                        "2026-10-03T08:02:00Z"
                ),
                null
        );
    }
}