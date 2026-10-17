package com.fold.modules.claims.infrastructure.persistence.mapper;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.TimeRange;
import com.fold.kernel.time.Timestamp;
import com.fold.kernel.time.ValidTime;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.*;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimOriginRecord;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimRecord;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimRevisionRecord;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Maps between the Claims aggregate and persistence representations.
 */
public final class ClaimPersistenceMapper {

    private final ClaimObjectJsonCodec objectCodec;

    public ClaimPersistenceMapper(
            ClaimObjectJsonCodec objectCodec
    ) {
        this.objectCodec = Objects.requireNonNull(
                objectCodec,
                "objectCodec must not be null"
        );
    }

    public ClaimRecord toRecord(
            Claim claim
    ) {
        Objects.requireNonNull(
                claim,
                "claim must not be null"
        );

        ClaimReference subject =
                claim.proposition()
                        .subject()
                        .reference();

        ValidTime validTime =
                claim.validTime()
                        .orElse(null);

        return new ClaimRecord(
                claim.id().value(),
                subject.namespace(),
                subject.value(),
                claim.proposition()
                        .predicate()
                        .value(),
                objectCodec.encode(
                        claim.proposition().object()
                ),
                claim.derivation()
                        .kind()
                        .name(),
                claim.derivation()
                        .method()
                        .name(),
                claim.derivation()
                        .method()
                        .version(),
                claim.confidence()
                        .map(ClaimConfidence::decimal)
                        .orElse(null),
                claim.observedAt()
                        .map(ClaimObservedAt::value)
                        .map(Timestamp::value)
                        .orElse(null),
                validTime == null
                        ? null
                        : validTime.range()
                        .startInclusive()
                        .value(),
                validTime == null
                        ? null
                        : validTime.range()
                        .end()
                        .map(Timestamp::value)
                        .orElse(null),
                claim.createdAt().value(),
                claim.status().name(),
                claim.revision().value()
        );
    }

    public List<ClaimOriginRecord> toOriginRecords(
            Claim claim
    ) {
        return claim.origin()
                .references()
                .stream()
                .map(reference ->
                        toOriginRecord(
                                claim.id(),
                                reference
                        )
                )
                .toList();
    }

    private ClaimOriginRecord toOriginRecord(
            ClaimId claimId,
            ClaimOriginReference reference
    ) {
        if (reference
                instanceof ClaimOriginReference.Observation observation) {

            return new ClaimOriginRecord(
                    claimId.value(),
                    "OBSERVATION",
                    observation.observationId().value()
            );
        }

        if (reference
                instanceof ClaimOriginReference.Source source) {

            return new ClaimOriginRecord(
                    claimId.value(),
                    "SOURCE",
                    source.sourceId().value()
            );
        }

        if (reference
                instanceof ClaimOriginReference.Evidence evidence) {

            return new ClaimOriginRecord(
                    claimId.value(),
                    "EVIDENCE",
                    evidence.evidenceId().value()
            );
        }

        ClaimOriginReference.Claim upstream =
                (ClaimOriginReference.Claim) reference;

        return new ClaimOriginRecord(
                claimId.value(),
                "CLAIM",
                upstream.claimId().value()
        );
    }

    public List<ClaimRevisionRecord> toRevisionRecords(
            Claim claim
    ) {
        return claim.revisionHistory()
                .stream()
                .map(revision ->
                        new ClaimRevisionRecord(
                                claim.id().value(),
                                revision.revision().value(),
                                revision.transition().name(),
                                revision.status().name(),
                                revision.changedAt().value(),
                                revision.relatedClaimId()
                                        .map(ClaimId::value)
                                        .orElse(null),
                                revision.reason()
                                        .map(
                                                ClaimLifecycleReason::code
                                        )
                                        .orElse(null),
                                revision.reason()
                                        .map(
                                                ClaimLifecycleReason::message
                                        )
                                        .orElse(null)
                        )
                )
                .toList();
    }

    public Claim toDomain(
            ClaimRecord record,
            List<ClaimOriginRecord> origins,
            List<ClaimRevisionRecord> revisions
    ) {
        Objects.requireNonNull(
                record,
                "record must not be null"
        );

        Objects.requireNonNull(
                origins,
                "origins must not be null"
        );

        Objects.requireNonNull(
                revisions,
                "revisions must not be null"
        );

        ClaimOrigin origin =
                new ClaimOrigin(
                        origins.stream()
                                .map(this::toOriginReference)
                                .collect(
                                        Collectors.toCollection(
                                                java.util.LinkedHashSet::new
                                        )
                                )
                );

        ValidTime validTime =
                record.validFrom() == null
                        ? null
                        : new ValidTime(
                        new TimeRange(
                                Timestamp.of(
                                        record.validFrom()
                                ),
                                record.validTo() == null
                                ? null
                                : Timestamp.of(
                                        record.validTo()
                                )
                        )
                );

        List<ClaimLifecycleRevision> history =
                revisions.stream()
                        .map(this::toLifecycleRevision)
                        .toList();

        Claim claim =
                Claim.restore(
                        new ClaimId(record.id()),
                        new ClaimProposition(
                                ClaimSubject.of(
                                        record.subjectNamespace(),
                                        record.subjectValue()
                                ),
                                ClaimPredicate.of(
                                        record.predicate()
                                ),
                                objectCodec.decode(
                                        record.objectPayload()
                                )
                        ),
                        origin,
                        new ClaimDerivation(
                                ClaimDerivationKind.valueOf(
                                        record.derivationKind()
                                ),
                                new ClaimDerivationMethod(
                                        record.derivationMethod(),
                                        record.derivationVersion()
                                )
                        ),
                        record.confidence() == null
                                ? null
                                : ClaimConfidence.of(
                                record.confidence()
                        ),
                        record.observedAt() == null
                                ? null
                                : ClaimObservedAt.of(
                                Timestamp.of(
                                        record.observedAt()
                                )
                        ),
                        validTime,
                        Timestamp.of(
                                record.createdAt()
                        ),
                        history
                );

        if (!claim.status()
                .name()
                .equals(record.status())) {
            throw new IllegalStateException(
                    "Persisted Claim status does not match lifecycle history"
            );
        }

        if (claim.revision().value()
                != record.revision()) {
            throw new IllegalStateException(
                    "Persisted Claim revision does not match lifecycle history"
            );
        }

        return claim;
    }

    private ClaimOriginReference toOriginReference(
            ClaimOriginRecord record
    ) {
        return switch (record.kind()) {
            case "OBSERVATION" ->
                    new ClaimOriginReference.Observation(
                            new ObservationId(
                                    record.referenceId()
                            )
                    );

            case "SOURCE" ->
                    new ClaimOriginReference.Source(
                            new SourceId(
                                    record.referenceId()
                            )
                    );

            case "EVIDENCE" ->
                    new ClaimOriginReference.Evidence(
                            new EvidenceId(
                                    record.referenceId()
                            )
                    );

            case "CLAIM" ->
                    new ClaimOriginReference.Claim(
                            new ClaimId(
                                    record.referenceId()
                            )
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported Claim origin kind: "
                                    + record.kind()
                    );
        };
    }

    private ClaimLifecycleRevision toLifecycleRevision(
            ClaimRevisionRecord record
    ) {
        ClaimLifecycleReason reason =
                record.reasonCode() == null
                        ? null
                        : new ClaimLifecycleReason(
                        record.reasonCode(),
                        record.reasonMessage()
                );

        return ClaimLifecycleRevision.restore(
                new ClaimRevision(
                        record.revision()
                ),
                ClaimLifecycleTransition.valueOf(
                        record.transition()
                ),
                ClaimStatus.valueOf(
                        record.status()
                ),
                Timestamp.of(
                        record.changedAt()
                ),
                record.relatedClaimId() == null
                        ? null
                        : new ClaimId(
                        record.relatedClaimId()
                ),
                reason
        );
    }
}