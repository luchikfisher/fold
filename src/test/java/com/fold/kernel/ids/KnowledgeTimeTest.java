package com.fold.kernel.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeTimeTest {

    private static final Timestamp T0 =
            Timestamp.parse("2026-01-01T00:00:00Z");

    private static final Timestamp T1 =
            Timestamp.parse("2026-02-01T00:00:00Z");

    private static final Timestamp T2 =
            Timestamp.parse("2026-03-01T00:00:00Z");

    @Test
    void shouldRepresentPeriodInWhichKnowledgeWasHeld() {
        KnowledgeTime knowledgeTime =
                KnowledgeTime.between(T0, T2);

        assertThat(knowledgeTime.contains(T0)).isTrue();
        assertThat(knowledgeTime.contains(T1)).isTrue();
        assertThat(knowledgeTime.contains(T2)).isFalse();
    }

    @Test
    void shouldSupportKnowledgeThatRemainsCurrent() {
        KnowledgeTime knowledgeTime =
                KnowledgeTime.from(T0);

        assertThat(knowledgeTime.contains(T2)).isTrue();
    }

    @Test
    void validTimeAndKnowledgeTimeShouldRemainDifferentTypes() {
        ValidTime validTime = ValidTime.from(T0);
        KnowledgeTime knowledgeTime = KnowledgeTime.from(T0);

        assertThat((Object) validTime)
                .isNotEqualTo(knowledgeTime);
    }
}