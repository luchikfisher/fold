# Observations

## Purpose

The observations bounded context records normalized semantic information
received by FOLD before that information is interpreted as knowledge about the
world.

An observation answers:

> What did FOLD receive?

It does not answer:

> What is true?

That distinction is fundamental.

## Owns

The module owns:

- observation identity;
- normalized semantic payloads;
- source-facing subjects;
- observation origin references;
- observation and arrival times;
- observation lifecycle state;
- deterministic observation fingerprints;
- duplicate detection;
- observation acceptance and rejection.

## Does Not Own

The module does not own:

- source metadata;
- evidence content;
- canonical entities;
- entity resolution;
- claims;
- beliefs;
- conflicts;
- hypotheses;
- graph projections;
- source reliability.

References to sources and evidence are identifiers only.

## Lifecycle

An observation begins in:

    RECEIVED

and transitions exactly once to either:

    ACCEPTED

or:

    REJECTED

Both are terminal states.

Duplicate submissions do not create observations with a DUPLICATE state.
Instead, duplicate detection resolves the submission to an existing
observation.

## Fingerprint Semantics

Observation fingerprints are deterministic semantic identifiers used only for
duplicate detection.

Fingerprint version 1 includes:

- observation type;
- subject type;
- subject external key;
- source identity;
- external record identity when present;
- observed-at time;
- normalized semantic payload.

Fingerprint version 1 excludes:

- observation ID;
- arrival time;
- evidence ID;
- lifecycle status;
- rejection reason;
- decision time.

Payload field order is not significant.

List value order is significant.

Fingerprint version and digest algorithm are stored explicitly. A future
canonicalization change must introduce a new fingerprint version rather than
silently changing version-one behavior.

## Deduplication

The domain performs an advisory duplicate lookup before an observation is
created.

This lookup alone is not sufficient for concurrency safety.

The eventual persistence implementation must enforce uniqueness of the complete
fingerprint identity:

    version + algorithm + digest

Two concurrent submissions may both observe the fingerprint as absent before
either transaction commits. Storage-level uniqueness is therefore mandatory.

## Framework Boundary

The observations domain is independent of:

- Spring;
- JPA;
- Hibernate;
- Jackson;
- HTTP;
- PostgreSQL;
- messaging technology.

Infrastructure implements domain-owned ports from outside the domain.

## Submission

Submission is the primary write use case of the observations module.

The application workflow is:

    normalized semantic input
        ↓
    deterministic fingerprint
        ↓
    advisory duplicate lookup
        ↓
    received Observation
        ↓
    acceptance policy
        ↓
    ACCEPTED or REJECTED
        ↓
    authoritative atomic insert
        ↓
    integration event

If the advisory lookup already finds the fingerprint, no new observation is
created.

If the advisory lookup reports the fingerprint as absent but another
transaction inserts the same fingerprint before persistence completes, the
authoritative insertion returns the existing observation and the submission is
resolved as a duplicate.

Therefore duplicate correctness does not depend on timing.

### Submission outcomes

A submission has exactly one final outcome:

- `Accepted`: a new accepted observation was stored;
- `Rejected`: a new rejected observation was stored;
- `Duplicate`: no new observation was stored and an existing observation was
  returned.

A duplicate is not an observation lifecycle state.

### Event publication

Successful new observations publish either:

    observations.observation-accepted v1

or:

    observations.observation-rejected v1

Duplicate submissions publish:

    observations.duplicate-observation-detected v1

Domain events and integration events are separate contracts.

Production persistence and integration-event publication must share a durable
transactional reliability boundary. The intended implementation is a
transactional outbox stored in the same database transaction as the
observation.

## Persistence

PostgreSQL is the authoritative observation store.

The domain does not depend on PostgreSQL, JDBC, JSON, or Spring. The
persistence adapter maps domain objects to an infrastructure-specific row
representation.

Observation payloads are stored as JSONB because their normalized semantic
shape is intentionally open. JSONB is a physical storage choice only; the
domain model remains strongly typed and independent of JSON.

Fingerprint uniqueness is enforced by the database across:

    fingerprint_version
    fingerprint_algorithm
    fingerprint_value

No physical foreign keys are created from observations to source or evidence
storage. Those concepts are owned by other bounded contexts.

## Transactional Event Publication

Observation persistence and integration-event publication belong to one
transactional reliability boundary.

Integration events are written to the `integration_outbox` table in the same
database transaction as the authoritative observation write.

The submission transaction therefore has two possible outcomes:

    observation + outbox event commit

or:

    neither commits

Direct broker publication inside the request transaction is intentionally not
used.

Outbox dispatch to an external broker is a later infrastructure concern and
does not change the observations-domain contract.

## HTTP API

Version one exposes:

    POST /api/v1/observations
    GET  /api/v1/observations/{id}

POST returns:

- `201 Created` when a new ACCEPTED observation is stored;
- `201 Created` when a new REJECTED observation is stored;
- `200 OK` when the submission resolves to an existing duplicate.

The `Location` header always identifies the authoritative observation.

Observations are immutable semantic records. Version one therefore exposes no
PUT, PATCH, or DELETE endpoint.