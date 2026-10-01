package com.fold.kernel.ids;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UuidDomainIdTest {

    @Test
    void shouldCreateRandomId() {
        UuidDomainId id = UuidDomainId.random();

        assertThat(id.id()).isNotNull();
        assertThat(id.value()).isEqualTo(id.id().toString());
    }

    @Test
    void shouldCreateIdFromString() {
        UUID uuid = UUID.randomUUID();

        UuidDomainId id = UuidDomainId.from(uuid.toString());

        assertThat(id.id()).isEqualTo(uuid);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> new UuidDomainId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id must not be null");
    }

    @Test
    void shouldRejectBlankString() {
        assertThatThrownBy(() -> UuidDomainId.from(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must not be blank");
    }

    @Test
    void shouldPreserveValueEquality() {
        UUID uuid = UUID.randomUUID();

        UuidDomainId first = new UuidDomainId(uuid);
        UuidDomainId second = new UuidDomainId(uuid);

        assertThat(first).isEqualTo(second);
    }
}