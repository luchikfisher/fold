CREATE TABLE claims
(
    id                  UUID PRIMARY KEY,

    subject_namespace   TEXT        NOT NULL,
    subject_value       TEXT        NOT NULL,
    predicate           TEXT        NOT NULL,

    object_payload      JSONB       NOT NULL,

    derivation_kind     TEXT        NOT NULL,
    derivation_method   TEXT        NOT NULL,
    derivation_version  INTEGER     NOT NULL,

    confidence          DOUBLE PRECISION NULL,

    observed_at         TIMESTAMPTZ NULL,

    valid_from          TIMESTAMPTZ NULL,
    valid_to            TIMESTAMPTZ NULL,

    created_at          TIMESTAMPTZ NOT NULL,

    status              TEXT        NOT NULL,
    revision            BIGINT      NOT NULL,

    CONSTRAINT chk_claim_object_payload
        CHECK (jsonb_typeof(object_payload) = 'object'),

    CONSTRAINT chk_claim_derivation_kind
        CHECK (
            derivation_kind IN
            (
             'OBSERVED',
             'DERIVED',
             'INFERRED',
             'ANALYST_ENTERED'
                )
            ),

    CONSTRAINT chk_claim_derivation_version
        CHECK (derivation_version > 0),

    CONSTRAINT chk_claim_confidence
        CHECK (
            confidence IS NULL
                OR
            (confidence >= 0.0 AND confidence <= 1.0)
            ),

    CONSTRAINT chk_claim_valid_time
        CHECK (
            (
                valid_from IS NULL
                    AND valid_to IS NULL
                )
                OR
            (
                valid_from IS NOT NULL
                    AND
                (
                    valid_to IS NULL
                        OR valid_from < valid_to
                    )
                )
            ),

    CONSTRAINT chk_claim_status
        CHECK (
            status IN
            (
             'ACTIVE',
             'SUPERSEDED',
             'INVALIDATED'
                )
            ),

    CONSTRAINT chk_claim_revision
        CHECK (revision > 0)
);


CREATE TABLE claim_origin_references
(
    claim_id       UUID NOT NULL,
    origin_kind    TEXT NOT NULL,
    reference_id   UUID NOT NULL,

    PRIMARY KEY
        (
         claim_id,
         origin_kind,
         reference_id
            ),

    CONSTRAINT fk_claim_origin_claim
        FOREIGN KEY (claim_id)
            REFERENCES claims(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_claim_origin_kind
        CHECK (
            origin_kind IN
            (
             'OBSERVATION',
             'SOURCE',
             'EVIDENCE',
             'CLAIM'
                )
            )
);


CREATE TABLE claim_lifecycle_revisions
(
    claim_id           UUID        NOT NULL,
    revision           BIGINT      NOT NULL,

    transition         TEXT        NOT NULL,
    status             TEXT        NOT NULL,

    changed_at         TIMESTAMPTZ NOT NULL,

    related_claim_id   UUID NULL,

    reason_code        TEXT NULL,
    reason_message     TEXT NULL,

    PRIMARY KEY
        (
         claim_id,
         revision
            ),

    CONSTRAINT fk_claim_revision_claim
        FOREIGN KEY (claim_id)
            REFERENCES claims(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_claim_lifecycle_revision
        CHECK (revision > 0),

    CONSTRAINT chk_claim_lifecycle_transition
        CHECK (
            transition IN
            (
             'CREATED',
             'SUPERSEDED',
             'INVALIDATED',
             'REINSTATED'
                )
            ),

    CONSTRAINT chk_claim_lifecycle_status
        CHECK (
            status IN
            (
             'ACTIVE',
             'SUPERSEDED',
             'INVALIDATED'
                )
            ),

    CONSTRAINT chk_claim_lifecycle_reason_pair
        CHECK (
            (
                reason_code IS NULL
                    AND reason_message IS NULL
                )
                OR
            (
                reason_code IS NOT NULL
                    AND reason_message IS NOT NULL
                )
            ),

    CONSTRAINT chk_claim_lifecycle_semantics
        CHECK (
            (
                transition = 'CREATED'
                    AND revision = 1
                    AND status = 'ACTIVE'
                    AND related_claim_id IS NULL
                    AND reason_code IS NULL
                    AND reason_message IS NULL
                )
                OR
            (
                transition = 'SUPERSEDED'
                    AND status = 'SUPERSEDED'
                    AND related_claim_id IS NOT NULL
                )
                OR
            (
                transition = 'INVALIDATED'
                    AND status = 'INVALIDATED'
                    AND related_claim_id IS NULL
                    AND reason_code IS NOT NULL
                    AND reason_message IS NOT NULL
                )
                OR
            (
                transition = 'REINSTATED'
                    AND status = 'ACTIVE'
                    AND related_claim_id IS NULL
                    AND reason_code IS NOT NULL
                    AND reason_message IS NOT NULL
                )
            )
);


CREATE TABLE claim_processing
(
    observation_id      UUID        NOT NULL,

    derivation_kind     TEXT        NOT NULL,
    derivation_method   TEXT        NOT NULL,
    derivation_version  INTEGER     NOT NULL,

    state               TEXT        NOT NULL,

    processed_at        TIMESTAMPTZ NULL,
    produced_claim_ids  UUID[] NULL,

    created_at          TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY
        (
         observation_id,
         derivation_kind,
         derivation_method,
         derivation_version
            ),

    CONSTRAINT chk_claim_processing_derivation_kind
        CHECK (
            derivation_kind IN
            (
             'OBSERVED',
             'DERIVED',
             'INFERRED',
             'ANALYST_ENTERED'
                )
            ),

    CONSTRAINT chk_claim_processing_version
        CHECK (derivation_version > 0),

    CONSTRAINT chk_claim_processing_state
        CHECK (
            state IN
            (
             'PENDING',
             'COMPLETED'
                )
            ),

    CONSTRAINT chk_claim_processing_completion
        CHECK (
            (
                state = 'PENDING'
                    AND processed_at IS NULL
                    AND produced_claim_ids IS NULL
                )
                OR
            (
                state = 'COMPLETED'
                    AND processed_at IS NOT NULL
                    AND produced_claim_ids IS NOT NULL
                )
            )
);


CREATE INDEX ix_claims_subject
    ON claims
        (
         subject_namespace,
         subject_value
            );

CREATE INDEX ix_claims_predicate
    ON claims (predicate);

CREATE INDEX ix_claims_status
    ON claims (status);

CREATE INDEX ix_claims_created_at
    ON claims (created_at);

CREATE INDEX ix_claims_observed_at
    ON claims (observed_at)
    WHERE observed_at IS NOT NULL;

CREATE INDEX ix_claim_origins_reference
    ON claim_origin_references
        (
         origin_kind,
         reference_id
            );

CREATE INDEX ix_claim_revisions_changed_at
    ON claim_lifecycle_revisions
        (
         changed_at
            );