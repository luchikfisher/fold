package com.fold.modules.observations.domain.repository;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.*;
import com.fold.modules.observations.domain.value.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryObservationRepositoryTest {

    @Test
    void shouldAddAndRetrieveObservationById() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        Observation observation =
                observation(
                        ObservationFingerprint.sha256V1(
                                "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                        )
                );

        ObservationAddResult result =
                repository.add(observation);

        assertThat(result.wasAdded()).isTrue();
        assertThat(result.isDuplicate()).isFalse();

        assertThat(
                repository.findById(
                        observation.id()
                )
        ).contains(observation);
    }

    @Test
    void shouldRetrieveObservationByFingerprint() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                );

        Observation observation =
                observation(fingerprint);

        ObservationAddResult result =
                repository.add(observation);

        assertThat(result.wasAdded()).isTrue();

        assertThat(
                repository.findByFingerprint(
                        fingerprint
                )
        ).contains(observation);
    }

    @Test
    void unknownObservationIdShouldReturnEmpty() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        assertThat(
                repository.findById(
                        ObservationId.random()
                )
        ).isEmpty();
    }

    @Test
    void unknownFingerprintShouldReturnEmpty() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        "abcdef0123456789"
                                + "abcdef0123456789"
                                + "abcdef0123456789"
                                + "abcdef0123456789"
                );

        assertThat(
                repository.findByFingerprint(
                        fingerprint
                )
        ).isEmpty();
    }

    @Test
    void sameObservationMayBeAddedAgainIdempotently() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        Observation observation =
                observation(
                        ObservationFingerprint.sha256V1(
                                "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                        )
                );

        ObservationAddResult first =
                repository.add(observation);

        ObservationAddResult second =
                repository.add(observation);

        assertThat(first.wasAdded()).isTrue();
        assertThat(first.isDuplicate()).isFalse();

        assertThat(second.wasAdded()).isTrue();
        assertThat(second.isDuplicate()).isFalse();

        assertThat(repository.size())
                .isEqualTo(1);

        assertThat(
                repository.findById(
                        observation.id()
                )
        ).contains(observation);
    }

    @Test
    void sameFingerprintOwnedByDifferentObservationShouldReturnDuplicate() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                );

        Observation first =
                observation(fingerprint);

        Observation second =
                observation(fingerprint);

        ObservationAddResult firstResult =
                repository.add(first);

        ObservationAddResult secondResult =
                repository.add(second);

        assertThat(firstResult.wasAdded()).isTrue();

        assertThat(secondResult)
                .isInstanceOf(
                        ObservationAddResult.Duplicate.class
                );

        ObservationAddResult.Duplicate duplicate =
                (ObservationAddResult.Duplicate) secondResult;

        assertThat(
                duplicate.existingObservationId()
        ).isEqualTo(
                first.id()
        );

        assertThat(repository.size())
                .isEqualTo(1);

        assertThat(
                repository.findById(
                        first.id()
                )
        ).contains(first);

        assertThat(
                repository.findById(
                        second.id()
                )
        ).isEmpty();

        assertThat(
                repository.findByFingerprint(
                        fingerprint
                )
        ).contains(first);
    }

    @Test
    void sameObservationIdWithDifferentFingerprintShouldFail() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        ObservationId id =
                ObservationId.random();

        Observation first =
                observation(
                        id,
                        ObservationFingerprint.sha256V1(
                                "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                        )
                );

        Observation second =
                observation(
                        id,
                        ObservationFingerprint.sha256V1(
                                "abcdef0123456789"
                                        + "abcdef0123456789"
                                        + "abcdef0123456789"
                                        + "abcdef0123456789"
                        )
                );

        repository.add(first);

        assertThatThrownBy(
                () -> repository.add(second)
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "observation ID already exists with a different fingerprint"
                );

        assertThat(repository.size())
                .isEqualTo(1);

        assertThat(
                repository.findById(id)
        ).contains(first);
    }

    @Test
    void existsByFingerprintShouldReturnTrueForKnownFingerprint() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                );

        repository.add(
                observation(fingerprint)
        );

        assertThat(
                repository.existsByFingerprint(
                        fingerprint
                )
        ).isTrue();
    }

    @Test
    void existsByFingerprintShouldReturnFalseForUnknownFingerprint() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                                + "0123456789abcdef"
                );

        assertThat(
                repository.existsByFingerprint(
                        fingerprint
                )
        ).isFalse();
    }

    @Test
    void shouldRejectNullObservation() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        assertThatThrownBy(
                () -> repository.add(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "observation must not be null"
                );
    }

    @Test
    void findByIdShouldRejectNullId() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        assertThatThrownBy(
                () -> repository.findById(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "id must not be null"
                );
    }

    @Test
    void findByFingerprintShouldRejectNullFingerprint() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        assertThatThrownBy(
                () -> repository.findByFingerprint(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "fingerprint must not be null"
                );
    }

    @Test
    void clearShouldRemoveAllStoredObservations() {
        InMemoryObservationRepository repository =
                new InMemoryObservationRepository();

        Observation first =
                observation(
                        ObservationFingerprint.sha256V1(
                                "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                                        + "0123456789abcdef"
                        )
                );

        Observation second =
                observation(
                        ObservationFingerprint.sha256V1(
                                "abcdef0123456789"
                                        + "abcdef0123456789"
                                        + "abcdef0123456789"
                                        + "abcdef0123456789"
                        )
                );

        repository.add(first);
        repository.add(second);

        assertThat(repository.size())
                .isEqualTo(2);

        repository.clear();

        assertThat(repository.size())
                .isZero();

        assertThat(
                repository.findById(first.id())
        ).isEmpty();

        assertThat(
                repository.findById(second.id())
        ).isEmpty();

        assertThat(
                repository.findByFingerprint(
                        first.fingerprint()
                )
        ).isEmpty();

        assertThat(
                repository.findByFingerprint(
                        second.fingerprint()
                )
        ).isEmpty();
    }

    private static Observation observation(
            ObservationFingerprint fingerprint
    ) {
        return observation(
                ObservationId.random(),
                fingerprint
        );
    }

    private static Observation observation(
            ObservationId id,
            ObservationFingerprint fingerprint
    ) {
        return Observation.receive(
                id,
                ObservationType.of("test"),
                ObservationSubject.of(
                        "subject",
                        "subject-1"
                ),
                ObservationPayload.of(
                        ObservationField.of(
                                "name",
                                ObservationValue.Text.of(
                                        "value"
                                )
                        )
                ),
                ObservationOrigin.fromSource(
                        SourceId.random()
                ),
                fingerprint,
                ObservedAt.of(
                        Timestamp.parse(
                                "2026-01-01T00:00:00Z"
                        )
                ),
                ArrivedAt.of(
                        Timestamp.parse(
                                "2026-01-01T00:00:01Z"
                        )
                )
        );
    }
}