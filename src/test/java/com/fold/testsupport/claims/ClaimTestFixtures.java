package com.fold.testsupport.claims;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.kernel.time.ValidTime;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.model.ClaimCreation;
import com.fold.modules.claims.domain.value.ClaimConfidence;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimObservedAt;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimOriginReference;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimReference;
import com.fold.modules.claims.domain.value.ClaimSubject;

public final class ClaimTestFixtures {

    public static final Timestamp CREATED_AT =
            Timestamp.parse(
                    "2026-10-03T10:00:00Z"
            );

    public static final Timestamp OBSERVED_AT =
            Timestamp.parse(
                    "2026-10-02T08:15:30.123456789Z"
            );

    private ClaimTestFixtures() {
    }

    public static ClaimDerivation derivation() {
        return new ClaimDerivation(
                ClaimDerivationKind.OBSERVED,
                new ClaimDerivationMethod(
                        "test-company-country",
                        1
                )
        );
    }

    public static ClaimProposition proposition() {
        return new ClaimProposition(
                ClaimSubject.of(
                        "company-registry",
                        "company-123"
                ),
                ClaimPredicate.of(
                        "registered-country"
                ),
                new ClaimObject.Text(
                        "Germany"
                )
        );
    }

    public static ClaimOrigin origin() {
        return ClaimOrigin.of(
                new ClaimOriginReference.Observation(
                        ObservationId.random()
                ),
                new ClaimOriginReference.Source(
                        SourceId.random()
                ),
                new ClaimOriginReference.Evidence(
                        EvidenceId.random()
                )
        );
    }

    public static ClaimCreation claimCreation() {
        return Claim.create(
                ClaimId.random(),
                proposition(),
                origin(),
                derivation(),
                ClaimConfidence.of(0.91),
                ClaimObservedAt.of(
                        OBSERVED_AT
                ),
                ValidTime.between(
                        Timestamp.parse(
                                "2026-01-01T00:00:00Z"
                        ),
                        Timestamp.parse(
                                "2027-01-01T00:00:00Z"
                        )
                ),
                CREATED_AT
        );
    }

    public static Claim claim() {
        return claimCreation().claim();
    }

    public static ClaimObject.Reference referenceObject() {
        return new ClaimObject.Reference(
                new ClaimReference(
                        "person-registry",
                        "person-456"
                )
        );
    }
}