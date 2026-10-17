package com.fold.modules.claims.application.query;

import com.fold.kernel.ids.*;
import com.fold.modules.claims.api.view.ClaimObjectView;
import com.fold.modules.claims.api.view.ClaimOriginReferenceView;
import com.fold.modules.claims.api.view.ClaimView;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.*;

import java.util.UUID;

public final class ClaimViewMapper {

    private ClaimViewMapper() {
    }

    public static ClaimView map(
            Claim claim
    ) {
        ClaimReference subject =
                claim.proposition()
                        .subject()
                        .reference();

        ClaimView.Validity validity =
                claim.validTime()
                        .map(validTime ->
                                new ClaimView.Validity(
                                        validTime.range()
                                                .startInclusive(),
                                        validTime.range()
                                                .end()
                                                .orElse(null)
                                )
                        )
                        .orElse(null);

        return new ClaimView(
                claim.id(),
                new ClaimView.Subject(
                        subject.namespace(),
                        subject.value()
                ),
                claim.proposition()
                        .predicate()
                        .value(),
                mapObject(
                        claim.proposition().object()
                ),
                claim.origin()
                        .references()
                        .stream()
                        .map(
                                ClaimViewMapper::mapOrigin
                        )
                        .toList(),
                new ClaimView.Derivation(
                        claim.derivation()
                                .kind()
                                .name(),
                        claim.derivation()
                                .method()
                                .name(),
                        claim.derivation()
                                .method()
                                .version()
                ),
                claim.confidence()
                        .map(ClaimConfidence::decimal)
                        .orElse(null),
                claim.observedAt()
                        .map(ClaimObservedAt::value)
                        .orElse(null),
                validity,
                claim.createdAt(),
                claim.status().name(),
                claim.revision().value(),
                claim.revisionHistory()
                        .stream()
                        .map(revision -> {
                            ClaimLifecycleReason reason =
                                    revision.reason()
                                            .orElse(null);

                            return new ClaimView.LifecycleRevision(
                                    revision.revision()
                                            .value(),
                                    revision.transition()
                                            .name(),
                                    revision.status()
                                            .name(),
                                    revision.changedAt(),
                                    revision.relatedClaimId()
                                            .orElse(null),
                                    reason == null
                                            ? null
                                            : new ClaimView.Reason(
                                            reason.code(),
                                            reason.message()
                                    )
                            );
                        })
                        .toList()
        );
    }

    /** Maps a typed object for both detail and summary reads without aggregate hydration. */
    public static ClaimObjectView mapObject(
            ClaimObject object
    ) {
        return switch (object) {
            case ClaimObject.Reference reference ->
                    new ClaimObjectView.Reference(
                            reference.value()
                                    .namespace(),
                            reference.value()
                                    .value()
                    );

            case ClaimObject.Text text ->
                    new ClaimObjectView.Text(
                            text.value()
                    );

            case ClaimObject.NumberValue number ->
                    new ClaimObjectView.NumberValue(
                            number.value()
                    );

            case ClaimObject.BooleanValue booleanValue ->
                    new ClaimObjectView.BooleanValue(
                            booleanValue.value()
                    );

            case ClaimObject.TimestampValue timestamp ->
                    new ClaimObjectView.TimestampValue(
                            timestamp.value()
                    );

            case ClaimObject.Identifier identifier ->
                    new ClaimObjectView.Identifier(
                            identifier.scheme(),
                            identifier.value()
                    );
        };
    }

    private static ClaimOriginReferenceView mapOrigin(
            ClaimOriginReference reference
    ) {
        if (reference
                instanceof ClaimOriginReference.Observation observation) {
            return new ClaimOriginReferenceView(
                    "OBSERVATION",
                    observation.observationId()
                            .value()
            );
        }

        if (reference
                instanceof ClaimOriginReference.Source source) {
            return new ClaimOriginReferenceView(
                    "SOURCE",
                    source.sourceId()
                            .value()
            );
        }

        if (reference
                instanceof ClaimOriginReference.Evidence evidence) {
            return new ClaimOriginReferenceView(
                    "EVIDENCE",
                    evidence.evidenceId()
                            .value()
            );
        }

        ClaimOriginReference.Claim upstream =
                (ClaimOriginReference.Claim) reference;

        return new ClaimOriginReferenceView(
                "CLAIM",
                upstream.claimId()
                        .value()
        );
    }
}