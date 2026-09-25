package com.fold.kernel.ids;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainIdsTest {

    @Test
    void entityIdShouldPreserveValue() {
        UUID value = UUID.randomUUID();

        EntityId id = new EntityId(value);

        assertThat(id.value()).isEqualTo(value);
        assertThat(id.asString()).isEqualTo(value.toString());
        assertThat(id.toString()).isEqualTo(value.toString());
    }

    @Test
    void entityIdShouldParseCanonicalUuid() {
        UUID value = UUID.randomUUID();

        EntityId id = EntityId.from(value.toString());

        assertThat(id.value()).isEqualTo(value);
    }

    @Test
    void entityIdShouldRejectNullValue() {
        assertThatThrownBy(() -> new EntityId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("value must not be null");
    }

    @Test
    void entityIdShouldRejectBlankString() {
        assertThatThrownBy(() -> EntityId.from(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must not be blank");
    }

    @Test
    void entityIdShouldRejectInvalidUuid() {
        assertThatThrownBy(() -> EntityId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void randomEntityIdsShouldBeDifferent() {
        assertThat(EntityId.random())
                .isNotEqualTo(EntityId.random());
    }

    @Test
    void idsOfSameTypeAndValueShouldBeEqual() {
        UUID value = UUID.randomUUID();

        assertThat(new ClaimId(value))
                .isEqualTo(new ClaimId(value));
    }

    @Test
    void idsOfDifferentDomainTypesRemainDistinctTypes() {
        UUID value = UUID.randomUUID();

        EntityId entityId = new EntityId(value);
        ClaimId claimId = new ClaimId(value);

        assertThat((Object) entityId)
                .isNotEqualTo(claimId);
    }

    @Test
    void allUuidBackedIdsShouldExposeStableTextRepresentation() {
        UUID value = UUID.randomUUID();

        DomainId<?>[] ids = {
                new EntityId(value),
                new ObservationId(value),
                new EvidenceId(value),
                new ClaimId(value),
                new SourceId(value),
                new EventId(value),
                new CorrelationId(value),
                new RelationId(value),
                new BeliefId(value),
                new ConflictId(value),
                new HypothesisId(value),
                new KnowledgeGapId(value),
                new DecisionId(value),
                new InvestigationId(value),
                new AlertId(value),
                new ReportId(value),
                new TenantId(value)
        };

        for (DomainId<?> id : ids) {
            assertThat(id.asString())
                    .isEqualTo(value.toString());
        }
    }
}