package com.fold.modules.observations.domain.policy;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.ObservationField;
import com.fold.modules.observations.domain.model.ObservationOrigin;
import com.fold.modules.observations.domain.model.ObservationPayload;
import com.fold.modules.observations.domain.model.ObservationSubject;
import com.fold.modules.observations.domain.value.ObservationFingerprint;
import com.fold.modules.observations.domain.value.ObservationType;
import com.fold.modules.observations.domain.value.ObservationValue;
import com.fold.modules.observations.domain.value.ObservedAt;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256ObservationFingerprintPolicyTest {

    private static final SourceId SOURCE_ID =
            new SourceId(
                    java.util.UUID.fromString(
                            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                    )
            );

    private static final EvidenceId EVIDENCE_ID =
            new EvidenceId(
                    java.util.UUID.fromString(
                            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
                    )
            );

    private final Sha256ObservationFingerprintPolicy policy =
            new Sha256ObservationFingerprintPolicy();

    @Test
    void sameSemanticInputShouldProduceSameFingerprint() {
        ObservationFingerprint first =
                policy.calculate(defaultInput());

        ObservationFingerprint second =
                policy.calculate(defaultInput());

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void fingerprintShouldUseVersionOneSha256() {
        ObservationFingerprint fingerprint =
                policy.calculate(defaultInput());

        assertThat(fingerprint.version())
                .isEqualTo(1);

        assertThat(fingerprint.algorithm())
                .isEqualTo("SHA-256");

        assertThat(fingerprint.value())
                .matches("[0-9a-f]{64}");
    }

    @Test
    void payloadFieldOrderShouldNotAffectFingerprint() {
        ObservationPayload firstPayload =
                ObservationPayload.of(
                        nameField(),
                        countryField()
                );

        ObservationPayload secondPayload =
                ObservationPayload.of(
                        countryField(),
                        nameField()
                );

        ObservationFingerprint first =
                policy.calculate(
                        inputWithPayload(firstPayload)
                );

        ObservationFingerprint second =
                policy.calculate(
                        inputWithPayload(secondPayload)
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void changingObservationTypeShouldChangeFingerprint() {
        ObservationFingerprint original =
                policy.calculate(defaultInput());

        ObservationFingerprint changed =
                policy.calculate(
                        new ObservationFingerprintPolicy.Input(
                                ObservationType.of(
                                        "organization-profile"
                                ),
                                defaultSubject(),
                                defaultPayload(),
                                defaultOrigin(),
                                defaultObservedAt()
                        )
                );

        assertThat(changed)
                .isNotEqualTo(original);
    }

    @Test
    void changingSubjectShouldChangeFingerprint() {
        ObservationFingerprint original =
                policy.calculate(defaultInput());

        ObservationFingerprint changed =
                policy.calculate(
                        new ObservationFingerprintPolicy.Input(
                                defaultType(),
                                ObservationSubject.of(
                                        "organization",
                                        "registry:company:999"
                                ),
                                defaultPayload(),
                                defaultOrigin(),
                                defaultObservedAt()
                        )
                );

        assertThat(changed)
                .isNotEqualTo(original);
    }

    @Test
    void changingSourceShouldChangeFingerprint() {
        ObservationFingerprint original =
                policy.calculate(defaultInput());

        SourceId anotherSource =
                new SourceId(
                        java.util.UUID.fromString(
                                "cccccccc-cccc-cccc-cccc-cccccccccccc"
                        )
                );

        ObservationFingerprint changed =
                policy.calculate(
                        new ObservationFingerprintPolicy.Input(
                                defaultType(),
                                defaultSubject(),
                                defaultPayload(),
                                ObservationOrigin.fromRecord(
                                        anotherSource,
                                        "record-123"
                                ),
                                defaultObservedAt()
                        )
                );

        assertThat(changed)
                .isNotEqualTo(original);
    }

    @Test
    void changingExternalRecordShouldChangeFingerprint() {
        ObservationFingerprint original =
                policy.calculate(defaultInput());

        ObservationFingerprint changed =
                policy.calculate(
                        new ObservationFingerprintPolicy.Input(
                                defaultType(),
                                defaultSubject(),
                                defaultPayload(),
                                ObservationOrigin.fromRecord(
                                        SOURCE_ID,
                                        "record-999"
                                ),
                                defaultObservedAt()
                        )
                );

        assertThat(changed)
                .isNotEqualTo(original);
    }

    @Test
    void changingObservedAtShouldChangeFingerprint() {
        ObservationFingerprint original =
                policy.calculate(defaultInput());

        ObservationFingerprint changed =
                policy.calculate(
                        new ObservationFingerprintPolicy.Input(
                                defaultType(),
                                defaultSubject(),
                                defaultPayload(),
                                defaultOrigin(),
                                ObservedAt.of(
                                        Timestamp.parse(
                                                "2026-09-28T10:00:00Z"
                                        )
                                )
                        )
                );

        assertThat(changed)
                .isNotEqualTo(original);
    }

    @Test
    void changingPayloadValueShouldChangeFingerprint() {
        ObservationFingerprint original =
                policy.calculate(defaultInput());

        ObservationPayload changedPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "organization.name",
                                ObservationValue.Text.of(
                                        "Different Company"
                                )
                        ),
                        countryField()
                );

        ObservationFingerprint changed =
                policy.calculate(
                        inputWithPayload(
                                changedPayload
                        )
                );

        assertThat(changed)
                .isNotEqualTo(original);
    }

    @Test
    void evidenceIdentityShouldNotAffectFingerprint() {
        ObservationOrigin withoutEvidence =
                ObservationOrigin.fromRecord(
                        SOURCE_ID,
                        "record-123"
                );

        ObservationOrigin withEvidence =
                ObservationOrigin.fromRecordEvidence(
                        SOURCE_ID,
                        EVIDENCE_ID,
                        "record-123"
                );

        ObservationFingerprint first =
                policy.calculate(
                        inputWithOrigin(
                                withoutEvidence
                        )
                );

        ObservationFingerprint second =
                policy.calculate(
                        inputWithOrigin(
                                withEvidence
                        )
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void numericScaleShouldNotAffectFingerprint() {
        ObservationPayload firstPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "transaction.amount",
                                ObservationValue.Number.of(
                                        new BigDecimal("10.0")
                                )
                        )
                );

        ObservationPayload secondPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "transaction.amount",
                                ObservationValue.Number.of(
                                        new BigDecimal("10.000")
                                )
                        )
                );

        ObservationFingerprint first =
                policy.calculate(
                        inputWithPayload(firstPayload)
                );

        ObservationFingerprint second =
                policy.calculate(
                        inputWithPayload(secondPayload)
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void listOrderShouldAffectFingerprint() {
        ObservationPayload firstPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "organization.aliases",
                                ObservationValue.ListValue.of(
                                        ObservationValue.Text.of(
                                                "first"
                                        ),
                                        ObservationValue.Text.of(
                                                "second"
                                        )
                                )
                        )
                );

        ObservationPayload secondPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "organization.aliases",
                                ObservationValue.ListValue.of(
                                        ObservationValue.Text.of(
                                                "second"
                                        ),
                                        ObservationValue.Text.of(
                                                "first"
                                        )
                                )
                        )
                );

        ObservationFingerprint first =
                policy.calculate(
                        inputWithPayload(firstPayload)
                );

        ObservationFingerprint second =
                policy.calculate(
                        inputWithPayload(secondPayload)
                );

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void differentValueTypesMustNotCollide() {
        ObservationPayload textPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "value",
                                ObservationValue.Text.of("1")
                        )
                );

        ObservationPayload numberPayload =
                ObservationPayload.of(
                        ObservationField.of(
                                "value",
                                ObservationValue.Number.of(1)
                        )
                );

        assertThat(
                policy.calculate(
                        inputWithPayload(textPayload)
                )
        ).isNotEqualTo(
                policy.calculate(
                        inputWithPayload(numberPayload)
                )
        );
    }

    @Test
    void missingExternalRecordMustDifferFromLiteralTextAbsent() {
        ObservationOrigin missing =
                ObservationOrigin.fromSource(
                        SOURCE_ID
                );

        ObservationOrigin literal =
                ObservationOrigin.fromRecord(
                        SOURCE_ID,
                        "absent"
                );

        assertThat(
                policy.calculate(
                        inputWithOrigin(missing)
                )
        ).isNotEqualTo(
                policy.calculate(
                        inputWithOrigin(literal)
                )
        );
    }

    private static ObservationFingerprintPolicy.Input defaultInput() {
        return new ObservationFingerprintPolicy.Input(
                defaultType(),
                defaultSubject(),
                defaultPayload(),
                defaultOrigin(),
                defaultObservedAt()
        );
    }

    private static ObservationFingerprintPolicy.Input inputWithPayload(
            ObservationPayload payload
    ) {
        return new ObservationFingerprintPolicy.Input(
                defaultType(),
                defaultSubject(),
                payload,
                defaultOrigin(),
                defaultObservedAt()
        );
    }

    private static ObservationFingerprintPolicy.Input inputWithOrigin(
            ObservationOrigin origin
    ) {
        return new ObservationFingerprintPolicy.Input(
                defaultType(),
                defaultSubject(),
                defaultPayload(),
                origin,
                defaultObservedAt()
        );
    }

    private static ObservationType defaultType() {
        return ObservationType.of(
                "organization-registration"
        );
    }

    private static ObservationSubject defaultSubject() {
        return ObservationSubject.of(
                "organization",
                "registry:company:123"
        );
    }

    private static ObservationPayload defaultPayload() {
        return ObservationPayload.of(
                nameField(),
                countryField()
        );
    }

    private static ObservationField nameField() {
        return ObservationField.of(
                "organization.name",
                ObservationValue.Text.of(
                        "Acme Ltd"
                )
        );
    }

    private static ObservationField countryField() {
        return ObservationField.of(
                "organization.country",
                ObservationValue.Text.of("GB")
        );
    }

    private static ObservationOrigin defaultOrigin() {
        return ObservationOrigin.fromRecordEvidence(
                SOURCE_ID,
                EVIDENCE_ID,
                "record-123"
        );
    }

    private static ObservedAt defaultObservedAt() {
        return ObservedAt.of(
                Timestamp.parse(
                        "2026-09-27T10:00:00Z"
                )
        );
    }
}