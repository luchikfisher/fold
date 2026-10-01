package com.fold.kernel.pagination;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorTest {

    @Test
    void shouldPreserveOpaqueValue() {
        Cursor cursor =
                new Cursor("opaque-value");

        assertThat(cursor.value())
                .isEqualTo("opaque-value");

        assertThat(cursor.toString())
                .isEqualTo("opaque-value");
    }

    @Test
    void shouldRejectBlankCursor() {
        assertThatThrownBy(
                () -> new Cursor(" ")
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}