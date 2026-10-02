package com.fold.modules.observations.domain.policy;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.*;
import com.fold.modules.observations.domain.repository.InMemoryObservationRepository;
import com.fold.modules.observations.domain.repository.ObservationAddResult;
import com.fold.modules.observations.domain.value.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationDeduplicationPolicyTest {

    private static final ObservationFingerprint FINGERPRINT =
            ObservationFingerprint.sha256V1(
                    "0123456789abcdef"
                            + "0123456789abcdef"
                            + "0123456789abcdef"
                            + "0123456789abcdef"
            );

    private InMemoryObservationRepository repository;
    private ObservationDeduplicationPolicy policy;

    @BeforeEach
    void setUp() {
        repository =
                new InMemoryObservationRepository();

        policy =
                new ObservationDeduplicationPolicy(
                        repository
                );
    }

    @Test
    void unknownFingerprintShouldBeUnique() {
        ObservationDeduplicationResult result =
                policy.evaluate(FINGERPRINT);

        assertThat(result.isUnique()).isTrue();
        assertThat(result.isDuplicate()).isFalse();

        assertThat(result)
                .isInstanceOf(
                        ObservationDeduplicationResult.Unique.class
                );
    }

    @Test
    void knownFingerprintShouldBeDuplicate() {
        Observation existing =
                observationWithFingerprint(
                        FINGERPRINT
                );

        ObservationAddResult addResult =
                repository.add(existing);

        assertThat(addResult.wasAdded()).isTrue();

        ObservationDeduplicationResult result =
                policy.evaluate(FINGERPRINT);

        assertThat(result.isDuplicate()).isTrue();
        assertThat(result.isUnique()).isFalse();

        assertThat(result)
                .isInstanceOf(
                        ObservationDeduplicationResult.Duplicate.class
                );

        ObservationDeduplicationResult.Duplicate duplicate =
                (ObservationDeduplicationResult.Duplicate) result;

        assertThat(
                duplicate.existingObservationId()
        ).isEqualTo(
                existing.id()
        );
    }

    @Test
    void differentFingerprintShouldRemainUnique() {
        Observation existing =
                observationWithFingerprint(
                        FINGERPRINT
                );

        ObservationAddResult addResult =
                repository.add(existing);

        assertThat(addResult.wasAdded()).isTrue();

        ObservationFingerprint another =
                ObservationFingerprint.sha256V1(
                        "abcdef0123456789"
                                + "abcdef0123456789"
                                + "abcdef0123456789"
                                + "abcdef0123456789"
                );

        ObservationDeduplicationResult result =
                policy.evaluate(another);

        assertThat(result.isUnique()).isTrue();
        assertThat(result.isDuplicate()).isFalse();
    }

    @Test
    void policyShouldReturnExistingObservationRegardlessOfItsLifecycleState() {
        Observation existing =
                observationWithFingerprint(
                        FINGERPRINT
                );

        existing.accept(
                Timestamp.parse(
                        "2026-09-27T10:02:00Z"
                )
        );

        repository.add(existing);

        ObservationDeduplicationResult result =
                policy.evaluate(FINGERPRINT);

        assertThat(result)
                .isInstanceOf(
                        ObservationDeduplicationResult.Duplicate.class
                );

        ObservationDeduplicationResult.Duplicate duplicate =
                (ObservationDeduplicationResult.Duplicate) result;

        assertThat(
                duplicate.existingObservationId()
        ).isEqualTo(
                existing.id()
        );
    }

    @Test
    void policyShouldRejectNullFingerprint() {
        assertThatThrownBy(
                () -> policy.evaluate(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "fingerprint must not be null"
                );
    }

    @Test
    void policyShouldRequireRepository() {
        assertThatThrownBy(
                () -> new ObservationDeduplicationPolicy(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "repository must not be null"
                );
    }

    private static Observation observationWithFingerprint(
            ObservationFingerprint fingerprint
    ) {
        return Observation.receive(
                ObservationId.random(),
                ObservationType.of(
                        "organization-registration"
                ),
                ObservationSubject.of(
                        "organization",
                        "registry:company:123"
                ),
                ObservationPayload.of(
                        ObservationField.of(
                                "organization.name",
                                ObservationValue.Text.of(
                                        "Acme Ltd"
                                )
                        )
                ),
                ObservationOrigin.fromRecord(
                        SourceId.random(),
                        "record-123"
                ),
                fingerprint,
                ObservedAt.of(
                        Timestamp.parse(
                                "2026-09-27T10:00:00Z"
                        )
                ),
                ArrivedAt.of(
                        Timestamp.parse(
                                "2026-09-27T10:01:00Z"
                        )
                )
        );
    }
}