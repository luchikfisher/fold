# Claims — Lifecycle and Discovery (Slices 6–7)

Claims represent assertion occurrences. Lifecycle decisions change their participation
in the knowledge model; they do not determine epistemic truth or rewrite propositions.

## Transition contract

| Command | Required current status | Result | Reason |
| --- | --- | --- | --- |
| Invalidate | ACTIVE | INVALIDATED | Required |
| Reinstate | INVALIDATED | ACTIVE | Required |
| Supersede | ACTIVE | SUPERSEDED | Optional |

These are the existing `Claim` domain contracts. Reinstatement is an explicit,
reasoned reversal of an invalidation and preserves both revisions. Supersession is
terminal. Its optional reason is retained for compatibility: the explicit successor
and recorded revision establish lineage even when no explanation is supplied.
Callers should supply a reason when they have one.

A successor must already exist and be ACTIVE when the decision is applied. Creation
and supersession are separate operations. No same-subject, same-predicate, proposition
equivalence, or confidence restriction is imposed. Multiple predecessors may name
the same successor. Later invalidation or supersession of the successor does not
reverse the predecessor's terminal decision or rewrite its historical link.
`ClaimOriginReference.Claim` remains derivation provenance, not replacement lineage.

The existing `ClaimView.lifecycle` exposes the complete history, including successor
IDs and reasons. There is no additional lineage projection or HTTP API in this slice.

## Application boundary and time

`ClaimLifecycleUseCase` has typed invalidate, reinstate, and supersede commands.
These are internal application contracts. They contain a Claim ID, expected revision,
reason, successor where applicable, and optional upstream `EventMetadata` for tracing.
They do not introduce a new public cross-module command API.

The Spring-free `ClaimLifecycleHandler` coordinates the domain and persistence ports.
`TransactionalClaimLifecycleUseCase` supplies the production transaction. Callers use
that wired interface, not the raw handler. Business outcomes distinguish `Applied`,
`AlreadyApplied`, `NotFound`, `RevisionConflict`, and `Rejected`. Invalid command
structure fails at construction; persistence/publication failures propagate and roll
back rather than becoming successful or rejected business outcomes.

The handler samples `FoldClock` after acquiring locks and resolving retries. The
canonical microsecond `Timestamp` is used for the domain revision and occurrence/emission
times. Equal times remain valid; revision numbers establish Claim-local ordering.
Clock regression is rejected, never silently clamped. Supersession also cannot predate
the successor's latest ACTIVE revision. Observed time and valid time are unchanged.

## Revision-bound idempotency

The semantic operation is a requested transition **from a particular Claim revision**,
including the exact reason code/message and successor. Revision history is its durable
receipt; there is no second idempotency store.

For a command with expected revision `r`:

1. If current revision is `r`, validate eligibility and append revision `r + 1`.
2. If current revision is later, compare the recorded revision `r + 1` with the requested
   transition, reason, and successor. An exact match returns `AlreadyApplied` with the
   original recorded revision/time, even if the Claim or successor changed again.
3. Any mismatch, or an expected revision ahead of current state, is `RevisionConflict`.

Replay checking precedes current status/successor eligibility checks. Replays do not
sample the clock, write history, or enqueue another event. Tracing metadata and event
IDs do not identify the semantic operation. Independently submitted identical decisions
against the same revision deliberately share an outcome.

Callers must retain the original expected revision on retry. Fetching the latest
revision and resubmitting requests a **new decision**. For example, retrying an
invalidation of revision 1 after reinstatement returns the original invalidation;
submitting against the reinstated revision explicitly requests another invalidation.

## Persistence, locking, and reads

Every lifecycle operation runs in one PostgreSQL transaction at the normal READ
COMMITTED isolation level. It locks all participating Claim rows with `FOR UPDATE`
in ascending canonical UUID-string order, regardless of target/successor roles.
The repository returns only identities actually locked. A row created after the lock
pass is not used merely because a later SELECT can see it; callers may retry after
its creation commits. Missing targets and successors cause no lifecycle writes.

The target's expected revision is checked under lock. Persistence also guards the
header update with `WHERE revision = :expectedRevision`, updates only status/revision,
and inserts exactly one history row. It never rewrites creation context or earlier
history. Lifecycle locks and writes require an active transaction. Status, history,
and the outbox entry commit or roll back together.

All operations that change successor eligibility participate in the same locking
protocol. Thus reciprocal supersession cannot commit both directions. More generally,
new links point only to ACTIVE Claims, while linked predecessors become terminal;
starting from valid lineage this prevents cycles without graph traversal. This is a
guarantee of the lifecycle workflow, not arbitrary SQL or imported corrupt history.

Lock ordering applies to the complete set of IDs in one operation. A caller composing
multiple operations inside an existing transaction must lock their full combined ID
set first in that same order. Otherwise PostgreSQL may detect a deadlock and roll back
one transaction. The transaction decorator joins an existing transaction, matching
Slice 5; it does not independently commit work belonging to its caller.

Public reads capture the Claim header's revision and load history only through that
revision. Immutable creation data and append-only history make the result coherent
even if a writer commits between the header and history SELECTs. Such a read may
return the earlier complete revision; it must never combine an old header with newer
history. Existing mapper and aggregate consistency checks remain in place. Physical
Claim deletion and arbitrary historical edits are not application operations.

V3 adds a foreign key for lifecycle successor references with restricted deletion and
a database check rejecting self-reference. It does not rewrite V1/V2. Migration fails
rather than silently repairing any pre-existing dangling or self-referential lineage;
such data must be investigated before rollout.

## Public integration events

`ClaimInvalidatedV1`, `ClaimReinstatedV1`, and `ClaimSupersededV1` publish Claim ID,
resulting revision, reason, occurrence time, and successor ID for supersession.
`ClaimLifecycleReasonV1` is an API-owned representation. Domain types stay internal.
The existing `ClaimCreatedV1` contract is unchanged.

Publication uses `EventEnvelope`, `EventMetadata`, and the existing JDBC transactional
outbox. A supplied cause preserves its correlation ID and uses its event ID as
causation; an uncaused decision starts a root chain. Producer is `claims`. Events
record the committed transition, not a promise about the Claim's current state at
consumer delivery time. Retry suppression guarantees one enqueue per applied
revision; it does not promise exactly-once broker delivery or global event ordering.

## Verification

PostgreSQL integration tests exercise the wired transactional boundary, all three
transitions, immutable creation context, exact microsecond round-trips, public history,
lineage chains and multiple predecessors, historical retries, conflicting commands,
and failure after a real outbox INSERT for each operation.

Concurrency tests observe actual `pg_blocking_pids` waits before releasing the first
transaction. They cover identical and conflicting decisions, reciprocal supersession,
concurrent successor invalidation, rollback followed by a waiting retry, and a NOWAIT
probe proving the higher ID remains unlocked while supersession waits on the lower ID.

Further tests force a lifecycle commit between header/history reads and commit a new
successor after a lock pass missed it. Repository tests verify revision compare-and-set,
transaction requirements, insertion rollback, and database lineage constraints.
Domain, application, JSON contract, and architecture tests run in the full suite:

```sh
./mvnw clean test
```


## Bounded Claim discovery (Slice 7)

`ClaimQueries.find(FindClaimsQuery)` adds discovery alongside the unchanged
`get(GetClaimQuery)` detail contract. Discovery reads authoritative Claims tables;
there is no stored projection, event consumer, cache, HTTP endpoint, or new write path.

All five `ClaimFilters` fields are optional. Supplied filters are combined with AND:

| Filter | Meaning |
| --- | --- |
| `subject` | Exact namespace/value pair, with no identity resolution or normalization |
| `predicate` | Exact predicate, preserving case and whitespace |
| `status` | Current `ACTIVE`, `INVALIDATED`, or `SUPERSEDED` status |
| `origin` | One directly recorded `OBSERVATION`, `SOURCE`, `EVIDENCE`, or `CLAIM` kind/UUID pair |
| `successorClaimId` | Predecessors explicitly superseded by this Claim |

Omitted status includes **all** statuses. No filters lists all Claim occurrences.
Unknown statuses/origin kinds and blank subject/predicate values are rejected.
A subject requires both its namespace and value. A missing referenced identity simply
matches no rows. Claim provenance is distinct from supersession; neither filter
traverses transitive relationships or reads other modules' schemas. The successor's
current status is irrelevant: historical predecessor links remain visible after the
successor is invalidated or superseded. Conflicting filters, such as ACTIVE predecessors
of a successor, naturally return an empty page.

Every occurrence remains a separate result, even when propositions are equivalent.
ACTIVE is a lifecycle state, not a declaration of truth, and Claim confidence is not
a computed belief. Discovery performs no proposition grouping or epistemic evaluation.

### Summary and detail

`ClaimSummaryView` includes ID, typed subject/predicate/object, derivation, optional
confidence/observed time/valid time, creation time, current status, and revision.
Existing API value representations are reused. Origins and lifecycle history are
available through detail lookup rather than expanded into every discovery result.
A detail lookup may return a newer revision than the summary. The existing captured-
revision detail-read guarantee remains unchanged.

`DefaultClaimQueries` composes the existing `GetClaimHandler` and the application-owned
`ClaimDiscoveryReader` port. `PostgresClaimDiscoveryReader` implements that port in
infrastructure. It selects summaries directly without loading aggregates, traversing
history per result, or changing the domain repository. The object persistence codec
and typed object-view mapping are reused.

### Ordering and continuation

Results always use `createdAt DESC, id DESC`, with PostgreSQL UUID ordering for ties.
Creation time and identity are immutable. Canonical microseconds are retained exactly;
many Claims can share a timestamp. No sort parameter or offset is accepted.

`FindClaimsQuery` requires a limit of 1–100 and a non-null filter record; use
`ClaimFilters.all()` for unrestricted discovery. Null cursor starts a traversal.
`ClaimSearchPage` contains immutable items and a nullable `nextCursor`, without a
mandatory count. The reader fetches at most limit + 1 matches to determine continuation.
The cursor identifies the last **returned** item, not the lookahead item. Page size may
change on continuation; filters may not.

Cursor v1 is an 82-character unpadded Base64url token encoding a version, SHA-256 binding
to the exact five filters and fixed order, and the creation-time/UUID position. Framed
fields distinguish null, empty, and delimiter-containing values. The size is fixed even
for long filters; malformed, oversized, noncanonical, unsupported-version, invalid-time,
and filter-mismatched tokens are rejected. Decoding happens before executing SQL.
Cursor timestamps must lie in the finite UTC range supported by both PGJDBC and
PostgreSQL: `-4712-01-01T00:00:00Z` inclusive to `+294277-01-01T00:00:00Z` exclusive.
The creation-time ordering key is read and bound using `OffsetDateTime`, preserving
its exact microseconds without legacy JDBC millisecond overflow or calendar conversion.
Java `Instant` validity alone does not imply a supported database cursor position.
Cursors are opaque navigation positions, not credentials, cryptographic authentication,
or snapshot handles. They are not encrypted or signed; a constructed valid position
can skip results but cannot relax the query filters. No security policy is delegated
to a cursor. Future incompatible formats require explicit version handling.

### Live consistency

Each page executes one SQL SELECT. Header values and origin/successor predicates use
the same PostgreSQL statement snapshot; EXISTS predicates avoid multiplying results.
Ordinary reads take no Claim row locks and cannot see uncommitted lifecycle changes.
Within an enclosing transaction, the caller's isolation level still applies.

Separate calls do **not** retain a database snapshot. New commits with keys behind the
cursor may appear later; keys ahead of it will not. Status changes may remove pending
matches or introduce matches behind the cursor. Matches introduced ahead of it will
be missed until a new traversal. An anchor's status changing does not invalidate its
immutable position. Reusing a cursor can therefore return different results. A null
next cursor means exhaustion at that statement, not permanent exhaustion.

Creation time is application time, not transaction commit order. Neither the cursor nor
a timestamp cutoff promises a complete export or an as-of knowledge snapshot. Observed
time, valid time, creation time, and lifecycle time retain their distinct meanings;
this slice adds no temporal filters or historical reconstruction API.

### Indexes and measured plans

V4 replaces the single-column creation/status indexes with `(created_at DESC, id DESC)`
and `(status, created_at DESC, id DESC)`. Their leading columns preserve the earlier
lookup capabilities. It adds a partial `(related_claim_id, claim_id)` index for
SUPERSEDED revisions. Existing subject, predicate, and direct-origin indexes remain.
No JSONB, generic filter-combination, or materialized-view indexes are introduced.

`ClaimDiscoveryQueryPlanTest` runs EXPLAIN (ANALYZE, BUFFERS) on the actual adapter SQL
against 30,000 generated occurrences plus a successor, 1,000 subjects/origins, two
predicates, and deliberately skewed lifecycle states. It examines first/deep pages,
subject/predicate, origin, successor, all five filters, and a large equal-time batch.
Plans are printed as diagnostic evidence rather than asserted as fixed optimizer choices.

During implementation, the old status index allowed a creation-order plan to discard
29,002 rows to obtain 21 INVALIDATED matches, touching 781 shared buffers. The composite
status/order index reduced that sample to 21 index-scan rows and 4 buffer hits. Final
first and deep pages also used the composite order index; equal-time continuation used
the full tuple as its index condition. Subject/predicate, origin, and successor workloads
selected 30 matching candidates before a small sort, using their respective indexes.

These fixture results justify these indexes, not a universal latency or scan bound.
A large provenance fan-out or unusual combined-filter selectivity can still require
substantial work. Additional indexing should follow real cardinalities and plans.
Index creation and replacement use the existing transactional Flyway convention;
large deployed tables require the usual migration scheduling consideration.

### Verification

Focused tests cover exact/conjunctive filtering, direct versus inherited provenance,
historical successors, distinct equivalent occurrences, all typed objects and nullable
creation fields, microsecond ties across UUID ordering boundaries, bounds and cursor
misuse, immutable pages, and live membership changes. Real PostgreSQL tests hold a
lifecycle transaction open to verify visibility on commit and rollback, and allow a
writer to commit while a page is being mapped to verify a coherent single-statement
result without per-item reads. Existing lifecycle, detail, outbox, and architecture tests
remain part of `./mvnw clean test`.
