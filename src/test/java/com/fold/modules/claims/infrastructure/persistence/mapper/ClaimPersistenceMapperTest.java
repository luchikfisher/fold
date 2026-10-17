package com.fold.modules.claims.infrastructure.persistence.mapper;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimRecord;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimRevisionRecord;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimPersistenceMapperTest {

    private ClaimPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper =
                new ClaimPersistenceMapper(
                        new ClaimObjectJsonCodec(
                                new ObjectMapper()
                        )
                );
    }

    @Test
    void shouldRoundTripCompleteActiveClaim() {
        Claim original =
                ClaimTestFixtures.claim();

        Claim restored =
                mapper.toDomain(
                        mapper.toRecord(original),
                        mapper.toOriginRecords(original),
                        mapper.toRevisionRecords(original)
                );

        assertEquivalent(
                original,
                restored
        );
    }

    @Test
    void shouldRoundTripClaimWithoutOptionalTemporalMetadata() {
        Claim original =
                Claim.create(
                        ClaimId.random(),
                        ClaimTestFixtures.proposition(),
                        ClaimTestFixtures.origin(),
                        ClaimTestFixtures.derivation(),
                        null,
                        null,
                        null,
                        ClaimTestFixtures.CREATED_AT
                ).claim();

        Claim restored =
                mapper.toDomain(
                        mapper.toRecord(original),
                        mapper.toOriginRecords(original),
                        mapper.toRevisionRecords(original)
                );

        assertThat(restored.confidence())
                .isEmpty();

        assertThat(restored.observedAt())
                .isEmpty();

        assertThat(restored.validTime())
                .isEmpty();
    }

    @Test
    void shouldRoundTripInvalidatedAndReinstatedHistory() {
        Claim original =
                ClaimTestFixtures.claim();

        original.invalidate(
                new ClaimLifecycleReason(
                        "RETRACTED",
                        "Source retracted statement"
                ),
                Timestamp.parse(
                        "2026-10-04T10:00:00Z"
                )
        );

        original.reinstate(
                new ClaimLifecycleReason(
                        "RETRACTION_REVERSED",
                        "Retraction was withdrawn"
                ),
                Timestamp.parse(
                        "2026-10-05T10:00:00Z"
                )
        );

        Claim restored =
                mapper.toDomain(
                        mapper.toRecord(original),
                        mapper.toOriginRecords(original),
                        mapper.toRevisionRecords(original)
                );

        assertEquivalent(
                original,
                restored
        );

        assertThat(restored.revisionHistory())
                .hasSize(3);
    }

    @Test
    void persistedCurrentStatusMustMatchHistory() {
        Claim original =
                ClaimTestFixtures.claim();

        ClaimRecord valid =
                mapper.toRecord(original);

        ClaimRecord corrupted =
                new ClaimRecord(
                        valid.id(),
                        valid.subjectNamespace(),
                        valid.subjectValue(),
                        valid.predicate(),
                        valid.objectPayload(),
                        valid.derivationKind(),
                        valid.derivationMethod(),
                        valid.derivationVersion(),
                        valid.confidence(),
                        valid.observedAt(),
                        valid.validFrom(),
                        valid.validTo(),
                        valid.createdAt(),
                        "INVALIDATED",
                        valid.revision()
                );

        assertThatThrownBy(
                () -> mapper.toDomain(
                        corrupted,
                        mapper.toOriginRecords(
                                original
                        ),
                        mapper.toRevisionRecords(
                                original
                        )
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "status"
                );
    }

    @Test
    void persistedCurrentRevisionMustMatchHistory() {
        Claim original =
                ClaimTestFixtures.claim();

        ClaimRecord valid =
                mapper.toRecord(original);

        ClaimRecord corrupted =
                new ClaimRecord(
                        valid.id(),
                        valid.subjectNamespace(),
                        valid.subjectValue(),
                        valid.predicate(),
                        valid.objectPayload(),
                        valid.derivationKind(),
                        valid.derivationMethod(),
                        valid.derivationVersion(),
                        valid.confidence(),
                        valid.observedAt(),
                        valid.validFrom(),
                        valid.validTo(),
                        valid.createdAt(),
                        valid.status(),
                        99
                );

        assertThatThrownBy(
                () -> mapper.toDomain(
                        corrupted,
                        mapper.toOriginRecords(
                                original
                        ),
                        mapper.toRevisionRecords(
                                original
                        )
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "revision"
                );
    }

    @Test
    void corruptRevisionHistoryShouldStillBeRejectedByAggregate() {
        Claim original =
                ClaimTestFixtures.claim();

        List<ClaimRevisionRecord> revisions =
                new ArrayList<>(
                        mapper.toRevisionRecords(
                                original
                        )
                );

        revisions.add(
                new ClaimRevisionRecord(
                        original.id().value(),
                        3,
                        "INVALIDATED",
                        "INVALIDATED",
                        Timestamp.parse(
                                "2026-10-04T10:00:00Z"
                        ).value(),
                        null,
                        "BAD",
                        "Bad revision gap"
                )
        );

        assertThatThrownBy(
                () -> mapper.toDomain(
                        mapper.toRecord(original),
                        mapper.toOriginRecords(original),
                        revisions
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "contiguous"
                );
    }

    private static void assertEquivalent(
            Claim expected,
            Claim actual
    ) {
        assertThat(actual.id())
                .isEqualTo(expected.id());

        assertThat(actual.proposition())
                .isEqualTo(expected.proposition());

        assertThat(actual.origin())
                .isEqualTo(expected.origin());

        assertThat(actual.derivation())
                .isEqualTo(expected.derivation());

        assertThat(actual.confidence())
                .isEqualTo(expected.confidence());

        assertThat(actual.observedAt())
                .isEqualTo(expected.observedAt());

        assertThat(actual.validTime())
                .isEqualTo(expected.validTime());

        assertThat(actual.createdAt())
                .isEqualTo(expected.createdAt());

        assertThat(actual.status())
                .isEqualTo(expected.status());

        assertThat(actual.revision())
                .isEqualTo(expected.revision());

        assertThat(actual.revisionHistory())
                .containsExactlyElementsOf(
                        expected.revisionHistory()
                );
    }
}