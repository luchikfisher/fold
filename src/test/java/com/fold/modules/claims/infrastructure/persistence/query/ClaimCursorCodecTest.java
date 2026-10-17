package com.fold.modules.claims.infrastructure.persistence.query;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.pagination.Cursor;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.query.ClaimFilters;
import com.fold.modules.claims.api.view.ClaimOriginReferenceView;
import com.fold.modules.claims.api.view.ClaimView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ClaimCursorCodecTest {
    private final ClaimCursorCodec codec = new ClaimCursorCodec();
    private final ClaimCursorCodec.Position position = new ClaimCursorCodec.Position(
            Timestamp.parse("1960-01-01T10:00:00.123456789Z"), ClaimId.random());

    @ParameterizedTest
    @ValueSource(strings = {"-4712-01-01T00:00:00Z", "-4712-01-01T00:00:00.000001Z",
            "0000-01-01T00:00:00.123456Z", "1500-01-01T00:00:00.123456Z",
            "1969-12-31T23:59:59.999999Z", "+294276-12-31T23:59:59.999999Z"})
    void shouldRetainExactSupportedBoundaryPositions(String value) {
        var boundary = new ClaimCursorCodec.Position(Timestamp.parse(value), position.id());
        assertThat(codec.decode(ClaimFilters.all(), codec.encode(ClaimFilters.all(), boundary)))
                .isEqualTo(boundary);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-4713-12-31T23:59:59.999999Z", "-4713-11-24T00:00:00Z",
            "-4713-11-23T23:59:59.999999Z", "+294277-01-01T00:00:00Z",
            "+300000-01-01T00:00:00Z", "+584556076-01-04T00:25:52Z",
            "+999999999-01-01T00:00:00Z", "+1000000000-12-31T23:59:59.999999Z",
            "-1000000000-01-01T00:00:00Z"})
    void shouldRejectJavaInstantsOutsideFinitePersistenceRange(String value) {
        var unsupported = new ClaimCursorCodec.Position(Timestamp.parse(value), position.id());
        assertThatThrownBy(() -> codec.encode(ClaimFilters.all(), unsupported))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid or unsupported Claim cursor");
        // Preserve the legitimate v1 envelope/filter binding while replacing its position.
        byte[] bytes = Base64.getUrlDecoder().decode(codec.encode(ClaimFilters.all(), position).value());
        ByteBuffer.wrap(bytes).putLong(33, unsupported.createdAt().value().getEpochSecond())
                .putInt(41, unsupported.createdAt().value().getNano() / 1_000);
        var cursor = new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        assertThatThrownBy(() -> codec.decode(ClaimFilters.all(), cursor))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid or unsupported Claim cursor");
    }

    @Test
    void shouldRoundTripExactMicrosecondPositionAndKeepLengthIndependentOfFilters() {
        for (var filters : List.of(ClaimFilters.all(),
                new ClaimFilters(new ClaimView.Subject("名".repeat(100_000), "v"), "a|b", "ACTIVE", null, null))) {
            Cursor cursor = codec.encode(filters, position);
            assertThat(cursor.value()).hasSize(ClaimCursorCodec.ENCODED_LENGTH);
            assertThat(codec.decode(filters, cursor)).isEqualTo(position);
        }
    }

    @Test
    void shouldBindEveryFilterIncludingKindNamespaceAndSuccessorWithoutDelimiterAmbiguity() {
        UUID originId = UUID.randomUUID();
        ClaimId successor = ClaimId.random();
        var subject = new ClaimView.Subject("a", "b|c");
        var origin = new ClaimOriginReferenceView("SOURCE", originId);
        var filters = new ClaimFilters(subject, "p", "ACTIVE", origin, successor);
        var cursor = codec.encode(filters, position);
        var mismatches = List.of(
                ClaimFilters.all(), new ClaimFilters(null, "p", "ACTIVE", origin, successor),
                new ClaimFilters(new ClaimView.Subject("a|b", "c"), "p", "ACTIVE", origin, successor),
                new ClaimFilters(subject, "P", "ACTIVE", origin, successor),
                new ClaimFilters(subject, "p", "SUPERSEDED", origin, successor),
                new ClaimFilters(subject, "p", "ACTIVE", new ClaimOriginReferenceView("EVIDENCE", originId), successor),
                new ClaimFilters(subject, "p", "ACTIVE", new ClaimOriginReferenceView("SOURCE", UUID.randomUUID()), successor),
                new ClaimFilters(subject, "p", "ACTIVE", origin, ClaimId.random()));
        for (var mismatch : mismatches) {
            assertThatThrownBy(() -> codec.decode(mismatch, cursor)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void shouldRejectMalformedOversizedNoncanonicalAndUnsupportedPositions() {
        var filters = ClaimFilters.all();
        var valid = codec.encode(filters, position);
        for (String value : List.of("x", "x".repeat(100_000), "!".repeat(82), valid.value() + "=",
                valid.value().substring(0, 81))) {
            assertThatThrownBy(() -> codec.decode(filters, new Cursor(value)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        byte[] original = Base64.getUrlDecoder().decode(valid.value());
        byte[] version = original.clone(); version[0] = 2;
        byte[] fraction = original.clone(); ByteBuffer.wrap(fraction).putInt(41, 1_000_000);
        byte[] negative = original.clone(); ByteBuffer.wrap(negative).putInt(41, -1);
        byte[] time = original.clone(); ByteBuffer.wrap(time).putLong(33, Long.MAX_VALUE);
        byte[] orderBinding = original.clone(); orderBinding[1] ^= 1;
        for (byte[] malformed : List.of(version, fraction, negative, time, orderBinding)) {
            var cursor = new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(malformed));
            assertThatThrownBy(() -> codec.decode(filters, cursor)).isInstanceOf(IllegalArgumentException.class);
        }
        // Same decoded bytes but non-zero unused Base64 bits must not be accepted.
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        int last = alphabet.indexOf(valid.value().charAt(81));
        var alias = new Cursor(valid.value().substring(0, 81) + alphabet.charAt(last + 1));
        assertThatThrownBy(() -> codec.decode(filters, alias)).isInstanceOf(IllegalArgumentException.class);
    }
}
