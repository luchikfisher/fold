CREATE TABLE observations
(
    id                    UUID PRIMARY KEY,

    type                  TEXT        NOT NULL,

    subject_type          TEXT        NOT NULL,
    subject_external_key  TEXT        NOT NULL,

    source_id             UUID        NOT NULL,
    evidence_id           UUID NULL,
    external_record_id    TEXT NULL,

    fingerprint_version   INTEGER     NOT NULL,
    fingerprint_algorithm TEXT        NOT NULL,
    fingerprint_value     TEXT        NOT NULL,

    observed_at           TIMESTAMPTZ(6) NOT NULL,
    arrived_at            TIMESTAMPTZ NOT NULL,

    status                TEXT        NOT NULL,
    decided_at            TIMESTAMPTZ NULL,

    rejection_code        TEXT NULL,
    rejection_message     TEXT NULL,

    payload               JSONB       NOT NULL,

    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_observation_fingerprint_version
        CHECK (fingerprint_version > 0),

    CONSTRAINT chk_observation_status
        CHECK (status IN ('RECEIVED', 'ACCEPTED', 'REJECTED')),

    CONSTRAINT chk_observation_payload_array
        CHECK (jsonb_typeof(payload) = 'array'),

    CONSTRAINT chk_observation_lifecycle
        CHECK (
            (
                status = 'RECEIVED'
                    AND decided_at IS NULL
                    AND rejection_code IS NULL
                    AND rejection_message IS NULL
                )
                OR
            (
                status = 'ACCEPTED'
                    AND decided_at IS NOT NULL
                    AND rejection_code IS NULL
                    AND rejection_message IS NULL
                )
                OR
            (
                status = 'REJECTED'
                    AND decided_at IS NOT NULL
                    AND rejection_code IS NOT NULL
                    AND rejection_message IS NOT NULL
                )
            ),

    CONSTRAINT chk_observation_decision_time
        CHECK (
            decided_at IS NULL
                OR decided_at >= arrived_at
            )
);

CREATE UNIQUE INDEX ux_observations_fingerprint
    ON observations
        (
         fingerprint_version,
         fingerprint_algorithm,
         fingerprint_value
            );

CREATE INDEX ix_observations_source
    ON observations (source_id);

CREATE INDEX ix_observations_arrived_at
    ON observations (arrived_at);

CREATE INDEX ix_observations_status
    ON observations (status);


CREATE TABLE integration_outbox
(
    event_id       UUID PRIMARY KEY,
    correlation_id UUID        NOT NULL,
    causation_id   UUID NULL,

    event_type     TEXT        NOT NULL,
    event_version  INTEGER     NOT NULL,

    occurred_at    TIMESTAMPTZ NOT NULL,
    emitted_at     TIMESTAMPTZ NOT NULL,

    producer       TEXT        NOT NULL,
    payload        JSONB       NOT NULL,

    state          TEXT        NOT NULL DEFAULT 'PENDING',
    attempts       INTEGER     NOT NULL DEFAULT 0,
    available_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   TIMESTAMPTZ NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_outbox_event_version
        CHECK (event_version > 0),

    CONSTRAINT chk_outbox_state
        CHECK (state IN ('PENDING', 'PUBLISHED', 'FAILED')),

    CONSTRAINT chk_outbox_attempts
        CHECK (attempts >= 0)
);

CREATE INDEX ix_outbox_pending
    ON integration_outbox
        (
         state,
         available_at,
         created_at
            ) WHERE state = 'PENDING';