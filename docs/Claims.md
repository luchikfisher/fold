# Claims — Lifecycle Operations (Slice 6)

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
