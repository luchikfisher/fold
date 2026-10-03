package com.fold.modules.claims.application.integration.observations;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationBatch;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationEngine;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeriveClaimsForAcceptedObservationHandlerTest {

    @Test
    void shouldRetrieveAcceptedObservationAndDerive() {
        ObservationView observation =
                observation("ACCEPTED");

        ObservationQueries queries =
                query -> Optional.of(observation);

        ClaimDerivationRule rule =
                new ClaimDerivationRule() {
                    @Override
                    public ClaimDerivation derivation() {
                        return new ClaimDerivation(
                                ClaimDerivationKind.OBSERVED,
                                ClaimDerivationMethod.versionOne(
                                        "test-rule"
                                )
                        );
                    }

                    @Override
                    public boolean supports(
                            ClaimDerivationInput input
                    ) {
                        return true;
                    }

                    @Override
                    public List<ClaimProposal> derive(
                            ClaimDerivationInput input
                    ) {
                        return List.of();
                    }
                };

        DeriveClaimsForAcceptedObservationHandler handler =
                new DeriveClaimsForAcceptedObservationHandler(
                        queries,
                        new ObservationClaimDerivationInputMapper(),
                        new ClaimDerivationEngine(
                                List.of(rule)
                        )
                );

        List<ClaimDerivationBatch> result =
                handler.handle(
                        new ObservationAcceptedV1(
                                observation.id(),
                                Timestamp.parse(
                                        "2026-10-03T10:01:00Z"
                                )
                        )
                );

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().isEmpty())
                .isTrue();
    }

    @Test
    void missingObservationShouldFail() {
        ObservationQueries queries =
                query -> Optional.empty();

        DeriveClaimsForAcceptedObservationHandler handler =
                new DeriveClaimsForAcceptedObservationHandler(
                        queries,
                        new ObservationClaimDerivationInputMapper(),
                        new ClaimDerivationEngine(
                                List.of()
                        )
                );

        ObservationId id =
                ObservationId.random();

        assertThatThrownBy(
                () -> handler.handle(
                        new ObservationAcceptedV1(
                                id,
                                Timestamp.parse(
                                        "2026-10-03T10:00:00Z"
                                )
                        )
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
    void eventForNonAcceptedObservationShouldFail() {
        ObservationView observation =
                observation("REJECTED");

        ObservationQueries queries =
                query -> Optional.of(observation);

        DeriveClaimsForAcceptedObservationHandler handler =
                new DeriveClaimsForAcceptedObservationHandler(
                        queries,
                        new ObservationClaimDerivationInputMapper(),
                        new ClaimDerivationEngine(
                                List.of()
                        )
                );

        assertThatThrownBy(
                () -> handler.handle(
                        new ObservationAcceptedV1(
                                observation.id(),
                                Timestamp.parse(
                                        "2026-10-03T10:00:00Z"
                                )
                        )
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "REJECTED"
                );
    }

    private static ObservationView observation(
            String status
    ) {
        ObservationId id =
                ObservationId.random();

        return new ObservationView(
                id,
                "test-type",
                new ObservationView.Subject(
                        "organization",
                        "123"
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
                        "2026-10-03T09:00:00Z"
                ),
                Timestamp.parse(
                        "2026-10-03T09:01:00Z"
                ),
                status,
                Timestamp.parse(
                        "2026-10-03T09:02:00Z"
                ),
                null
        );
    }
}