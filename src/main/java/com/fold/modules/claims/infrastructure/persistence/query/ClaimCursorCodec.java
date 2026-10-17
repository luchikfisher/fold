package com.fold.modules.claims.infrastructure.persistence.query;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.pagination.Cursor;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.query.ClaimFilters;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Fixed-size v1 position plus a SHA-256 binding to exact filters and ordering.
 * This is not an authorization token or an authenticated snapshot. Clients
 * must treat it as opaque; valid forged positions confer no additional access.
 */
final class ClaimCursorCodec {
    private static final byte VERSION = 1;
    private static final int BYTES = 61;
    static final int ENCODED_LENGTH = 82;
    private static final String ORDER = "claims:createdAt:desc,id:desc";
    // Finite UTC OffsetDateTime range shared by PGJDBC and PostgreSQL TIMESTAMPTZ.
    // PGJDBC maps earlier dates to -infinity; PostgreSQL's upper bound is exclusive.
    private static final Instant MIN_TIME = Instant.parse("-4712-01-01T00:00:00Z");
    private static final Instant END_TIME = Instant.parse("+294277-01-01T00:00:00Z");

    record Position(Timestamp createdAt, ClaimId id) {}

    Cursor encode(ClaimFilters filters, Position position) {
        Instant time = position.createdAt().value();
        validateTime(time);
        UUID id = position.id().value();
        byte[] bytes = ByteBuffer.allocate(BYTES).put(VERSION).put(fingerprint(filters))
                .putLong(time.getEpochSecond()).putInt(time.getNano() / 1_000)
                .putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).array();
        return new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    Position decode(ClaimFilters filters, Cursor cursor) {
        if (cursor.value().length() != ENCODED_LENGTH) {
            throw invalid();
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor.value());
            if (bytes.length != BYTES || !Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(bytes).equals(cursor.value())) {
                throw invalid();
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            if (buffer.get() != VERSION) {
                throw invalid();
            }
            byte[] binding = new byte[32];
            buffer.get(binding);
            if (!MessageDigest.isEqual(binding, fingerprint(filters))) {
                throw new IllegalArgumentException("Claim cursor does not match query filters or ordering");
            }
            long seconds = buffer.getLong();
            int micros = buffer.getInt();
            if (micros < 0 || micros >= 1_000_000) {
                throw invalid();
            }
            Timestamp time = Timestamp.of(Instant.ofEpochSecond(seconds, micros * 1_000L));
            validateTime(time.value());
            return new Position(time, new ClaimId(new UUID(buffer.getLong(), buffer.getLong())));
        } catch (DateTimeException exception) {
            throw invalid();
        }
    }

    private void validateTime(Instant time) {
        if (time.isBefore(MIN_TIME) || !time.isBefore(END_TIME)) {
            throw invalid();
        }
    }

    private byte[] fingerprint(ClaimFilters filters) {
        try {
            var bytes = new ByteArrayOutputStream();
            var out = new DataOutputStream(bytes);
            write(out, ORDER);
            write(out, filters.subject() == null ? null : filters.subject().namespace());
            write(out, filters.subject() == null ? null : filters.subject().value());
            write(out, filters.predicate());
            write(out, filters.status());
            write(out, filters.origin() == null ? null : filters.origin().kind());
            write(out, filters.origin() == null ? null : filters.origin().referenceId().toString());
            write(out, filters.successorClaimId() == null ? null : filters.successorClaimId().asString());
            return MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray());
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Cannot encode Claim cursor", exception);
        }
    }

    private void write(DataOutputStream out, String value) throws IOException {
        if (value == null) {
            out.writeInt(-1);
        } else {
            // Length framing distinguishes null, empty and delimiter-containing values.
            out.writeInt(value.length());
            out.writeChars(value);
        }
    }

    private IllegalArgumentException invalid() {
        return new IllegalArgumentException("Invalid or unsupported Claim cursor");
    }
}
