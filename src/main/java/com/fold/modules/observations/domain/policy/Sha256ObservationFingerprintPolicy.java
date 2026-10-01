package com.fold.modules.observations.domain.policy;

import com.fold.modules.observations.domain.model.ObservationField;
import com.fold.modules.observations.domain.value.ObservationFingerprint;
import com.fold.modules.observations.domain.value.ObservationValue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Version-one observation fingerprint policy using SHA-256 over a canonical
 * binary representation.
 *
 * <p>The canonical representation is independent of JSON serialization,
 * database representation, JVM object identity, and source field ordering.</p>
 *
 * <p>Version one fingerprints the following semantic properties:</p>
 *
 * <ul>
 *     <li>observation type;</li>
 *     <li>subject type and external key;</li>
 *     <li>source identity;</li>
 *     <li>external source record identity, when present;</li>
 *     <li>source-reported observation time;</li>
 *     <li>all normalized semantic payload fields.</li>
 * </ul>
 *
 * <p>The following properties intentionally do not participate:</p>
 *
 * <ul>
 *     <li>observation aggregate ID;</li>
 *     <li>arrival time;</li>
 *     <li>evidence ID;</li>
 *     <li>acceptance status;</li>
 *     <li>rejection information;</li>
 *     <li>decision time.</li>
 * </ul>
 *
 * <p>Payload field order is ignored. Fields are sorted by semantic field name
 * before hashing. Ordering inside an {@link ObservationValue.ListValue}
 * remains significant.</p>
 */
public final class Sha256ObservationFingerprintPolicy
        implements ObservationFingerprintPolicy {

    private static final int VERSION = 1;

    private static final String SCHEME =
            "fold.observation.fingerprint";

    @Override
    public ObservationFingerprint calculate(
            Input input
    ) {
        Objects.requireNonNull(
                input,
                "input must not be null"
        );

        byte[] canonical =
                canonicalize(input);

        byte[] digest =
                sha256(canonical);

        return ObservationFingerprint.sha256(
                VERSION,
                HexFormat.of().formatHex(digest)
        );
    }

    private static byte[] canonicalize(
            Input input
    ) {
        try {
            ByteArrayOutputStream bytes =
                    new ByteArrayOutputStream();

            DataOutputStream output =
                    new DataOutputStream(bytes);

            writeString(output, "scheme", SCHEME);
            writeInteger(output, "version", VERSION);

            writeString(
                    output,
                    "observation-type",
                    input.type().value()
            );

            writeString(
                    output,
                    "subject-type",
                    input.subject().type()
            );

            writeString(
                    output,
                    "subject-key",
                    input.subject().externalKey()
            );

            writeString(
                    output,
                    "source-id",
                    input.origin()
                            .sourceId()
                            .asString()
            );

            writeOptionalString(
                    output,
                    "external-record-id",
                    input.origin()
                            .externalRecord()
                            .orElse(null)
            );

            writeString(
                    output,
                    "observed-at",
                    input.observedAt()
                            .value()
                            .toString()
            );

            List<ObservationField> fields =
                    new ArrayList<>(
                            input.payload().asList()
                    );

            fields.sort(
                    Comparator.comparing(
                            ObservationField::name
                    )
            );

            writeInteger(
                    output,
                    "field-count",
                    fields.size()
            );

            for (ObservationField field : fields) {
                writeString(
                        output,
                        "field-name",
                        field.name()
                );

                writeObservationValue(
                        output,
                        field.value()
                );
            }

            output.flush();

            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to build canonical observation fingerprint representation",
                    exception
            );
        }
    }

    private static void writeObservationValue(
            DataOutputStream output,
            ObservationValue value
    ) throws IOException {

        switch (value) {
            case ObservationValue.Text text -> {
                writeString(
                        output,
                        "value-type",
                        "text"
                );

                writeString(
                        output,
                        "text-value",
                        text.value()
                );
            }

            case ObservationValue.Number number -> {
                writeString(
                        output,
                        "value-type",
                        "number"
                );

                writeString(
                        output,
                        "number-value",
                        canonicalNumber(number.value())
                );
            }

            case ObservationValue.BooleanValue booleanValue -> {
                writeString(
                        output,
                        "value-type",
                        "boolean"
                );

                writeBoolean(
                        output,
                        "boolean-value",
                        booleanValue.value()
                );
            }

            case ObservationValue.TimestampValue timestampValue -> {
                writeString(
                        output,
                        "value-type",
                        "timestamp"
                );

                writeString(
                        output,
                        "timestamp-value",
                        timestampValue
                                .value()
                                .toString()
                );
            }

            case ObservationValue.Identifier identifier -> {
                writeString(
                        output,
                        "value-type",
                        "identifier"
                );

                writeString(
                        output,
                        "identifier-scheme",
                        identifier.scheme()
                );

                writeString(
                        output,
                        "identifier-value",
                        identifier.value()
                );
            }

            case ObservationValue.ListValue listValue -> {
                writeString(
                        output,
                        "value-type",
                        "list"
                );

                writeInteger(
                        output,
                        "list-size",
                        listValue.values().size()
                );

                for (ObservationValue nested :
                        listValue.values()) {
                    writeObservationValue(
                            output,
                            nested
                    );
                }
            }
        }
    }

    private static String canonicalNumber(
            BigDecimal value
    ) {
        return value.toPlainString();
    }

    private static void writeOptionalString(
            DataOutputStream output,
            String label,
            String value
    ) throws IOException {

        writeString(
                output,
                label + ".presence",
                value == null ? "absent" : "present"
        );

        if (value != null) {
            writeString(
                    output,
                    label,
                    value
            );
        }
    }

    private static void writeString(
            DataOutputStream output,
            String label,
            String value
    ) throws IOException {

        Objects.requireNonNull(
                label,
                "label must not be null"
        );

        Objects.requireNonNull(
                value,
                "value must not be null"
        );

        writeRawString(output, label);
        writeRawString(output, value);
    }

    private static void writeInteger(
            DataOutputStream output,
            String label,
            int value
    ) throws IOException {

        writeRawString(output, label);
        output.writeInt(value);
    }

    private static void writeBoolean(
            DataOutputStream output,
            String label,
            boolean value
    ) throws IOException {

        writeRawString(output, label);
        output.writeBoolean(value);
    }

    private static void writeRawString(
            DataOutputStream output,
            String value
    ) throws IOException {

        byte[] encoded =
                value.getBytes(StandardCharsets.UTF_8);

        output.writeInt(encoded.length);
        output.write(encoded);
    }

    private static byte[] sha256(
            byte[] value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            ObservationFingerprint.SHA_256
                    );

            return digest.digest(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available in the current Java runtime",
                    exception
            );
        }
    }
}