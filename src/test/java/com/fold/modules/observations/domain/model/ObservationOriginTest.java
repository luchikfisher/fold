package com.fold.modules.observations.domain.model;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.SourceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationOriginTest {

    @Test
    void sourceOnlyOriginShouldContainSource() {
        SourceId sourceId = SourceId.random();

        ObservationOrigin origin =
                ObservationOrigin.fromSource(sourceId);

        assertThat(origin.sourceId())
                .isEqualTo(sourceId);

        assertThat(origin.evidence()).isEmpty();
        assertThat(origin.externalRecord()).isEmpty();
    }

    @Test
    void originMayReferenceEvidence() {
        SourceId sourceId = SourceId.random();
        EvidenceId evidenceId = EvidenceId.random();

        ObservationOrigin origin =
                ObservationOrigin.fromEvidence(
                        sourceId,
                        evidenceId
                );

        assertThat(origin.evidence())
                .contains(evidenceId);
    }

    @Test
    void originMayReferenceExternalRecord() {
        ObservationOrigin origin =
                ObservationOrigin.fromRecord(
                        SourceId.random(),
                        "record-123"
                );

        assertThat(origin.externalRecord())
                .contains("record-123");
    }

    @Test
    void originMayReferenceEvidenceAndExternalRecord() {
        EvidenceId evidenceId = EvidenceId.random();

        ObservationOrigin origin =
                ObservationOrigin.fromRecordEvidence(
                        SourceId.random(),
                        evidenceId,
                        "record-123"
                );

        assertThat(origin.evidence())
                .contains(evidenceId);

        assertThat(origin.externalRecord())
                .contains("record-123");
    }

    @Test
    void sourceIsMandatory() {
        assertThatThrownBy(
                () -> ObservationOrigin.fromSource(null)
        )
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void presentExternalRecordMustNotBeBlank() {
        assertThatThrownBy(
                () -> ObservationOrigin.fromRecord(
                        SourceId.random(),
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}