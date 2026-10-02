
# FOLD — Observations Bounded Context

## Complete Implementation and Integration Report

### 1. Purpose of the module

The `observations` bounded context is the ingestion boundary between normalized external information and FOLD's internal knowledge model.

Its central semantic distinction is:

> An Observation records what FOLD received, not what FOLD believes to be true.

An Observation is therefore not an Entity, Claim, Belief, Inference, Relation, Event, Conflict, or resolved fact about the world.

It represents normalized semantic input before FOLD performs higher-level interpretation.

Conceptually:

```text
External / normalized information
              ↓
         Observation
              ↓
   later interpretation
              ↓
Claims / Identity / Events / Beliefs / ...
```

This distinction is foundational. Future modules must not treat an accepted Observation as truth merely because it was accepted.

Acceptance means:

> This input is admissible for downstream knowledge processing.

It does not mean:

> The content of this input is true.

---

# 2. Architectural position

The module follows the FOLD modular-monolith architecture:

```text
modules/observations/
├── api/
├── domain/
├── application/
└── infrastructure/
```

The dependency direction is inward:

```text
Infrastructure
      ↓
Application
      ↓
Domain
      ↓
Kernel
```

The domain does not know Spring, JDBC, PostgreSQL, Jackson, HTTP, or infrastructure.

The application layer does not know Spring, JDBC, Jackson, or infrastructure.

Other bounded contexts must not import internal Observation domain classes.

The intended cross-module boundary is:

```text
other module
     ↓
observations.api.*
```

plus universal identifiers from:

```text
com.fold.kernel.ids.*
```

This rule is enforced with ArchUnit tests.

---

# 3. Slice 1 — Domain Vocabulary

Slice 1 established the language of the bounded context.

## ObservationType

`ObservationType` is an open value object representing the semantic type of an observation.

Examples might conceptually be:

```text
organization-registration
person-employment
asset-ownership
company-directorship
```

It is deliberately not an enum.

The system must be able to receive new semantic observation types without changing compiled source code merely to add another category.

It rejects invalid blank values and preserves explicit normalized semantics rather than silently rewriting input.

A future module should normally not depend directly on this domain type. If another module needs observation type information, it should receive it through a public API/view/event contract.

---

# 4. ObservationStatus

The lifecycle vocabulary is:

```text
RECEIVED
ACCEPTED
REJECTED
```

There is deliberately no:

```text
DUPLICATE
```

status.

A duplicate submission does not create a second Observation with a special state.

Instead:

```text
new submission
      ↓
same fingerprint already exists
      ↓
resolve to existing Observation
```

This distinction matters to future consumers.

A duplicate is a submission-processing result, not an Observation lifecycle state.

---

# 5. ObservationSubject

`ObservationSubject` identifies the source-facing or normalized subject to which the observation refers.

It exists before FOLD identity resolution.

Therefore this is explicitly not:

```text
EntityId
```

That distinction must remain intact.

An Observation may say roughly:

```text
subject type: organization
external key: registry:company:123
```

Later, the Identity bounded context may decide that this subject corresponds to a canonical Entity.

The Observations module itself does not make that decision.

Future identity-related code must therefore never interpret `ObservationSubject` as canonical identity.

---

# 6. ObservationOrigin

`ObservationOrigin` records provenance references associated with the incoming Observation.

It contains the source identity and, where available, references such as:

```text
SourceId
EvidenceId
external record identifier
```

The Observations module does not own Source or Evidence lifecycle.

It stores references only.

In particular, it does not reach into another bounded context's repository.

There are deliberately no cross-context ORM relationships.

---

# 7. ObservedAt and ArrivedAt

We separated two different temporal meanings.

`ObservedAt` means approximately:

> When the source says the information was observed, reported, or applicable in the source-facing observation context.

`ArrivedAt` means:

> When FOLD received this Observation.

These are deliberately separate.

There is no universal invariant:

```text
observedAt <= arrivedAt
```

because information may describe future-dated events or future-effective facts.

The higher-level temporal model will later distinguish additional temporal semantics such as valid time and knowledge/system time.

`ObservedAt` is canonicalized to microsecond precision.

This is intentional because the authoritative PostgreSQL persistence representation stores `observed_at` with microsecond precision.

The canonicalization occurs at the `ObservedAt` value-object boundary so that the domain representation, fingerprint representation, and persisted representation remain consistent.

Conceptually:

```text
source timestamp
2026-10-02T10:00:00.123456789Z
              ↓
ObservedAt
2026-10-02T10:00:00.123456Z
              ↓
fingerprint
2026-10-02T10:00:00.123456Z
              ↓
PostgreSQL
2026-10-02T10:00:00.123456Z
```

This prevents the system from fingerprinting temporal precision that the authoritative store cannot retain.

The kernel-level `Timestamp` abstraction itself remains capable of representing the full precision supported by Java `Instant`.

Microsecond canonicalization is therefore not a universal FOLD timestamp rule.

It is specifically part of the `ObservedAt` semantic contract.

---

# 8. ObservationPayload

The payload contains normalized semantic fields.

It is strongly typed at the domain level.

We explicitly avoided:

```text
Map<String, Object>
```

and we avoided putting Jackson's `JsonNode` inside the domain.

An Observation payload consists of semantic fields:

```text
ObservationPayload
    └── ObservationField
            ├── name
            └── ObservationValue
```

Field names must be unique inside a payload.

Payload field order is preserved as representation, but fingerprint canonicalization does not treat field order as semantic identity.

---

# 9. ObservationValue

Observation values are explicitly typed.

The supported semantic categories currently include:

```text
Text
Number
Boolean
Timestamp
Identifier
List
```

This allows FOLD to distinguish, for example:

```text
"1"
```

from:

```text
1
```

and an arbitrary text string from a structured identifier.

Numbers use `BigDecimal` semantics rather than floating-point identity.

Identifiers contain both a scheme and a value.

Lists preserve order.

List order is considered semantically significant for fingerprinting.

The infrastructure JSON representation is not the domain model. JSON exists only as a persistence/HTTP representation.

---

# 10. ObservationRejection

A rejected Observation is not discarded.

A rejection carries structured information such as:

```text
code
message
```

The code exists for stable machine interpretation.

The message exists for explanation.

Rejected Observations remain part of FOLD's ingestion history.

This supports auditability, replay, debugging, and future rule changes.

---

# 11. Slice 2 — Observation Aggregate

Slice 2 introduced the actual aggregate root:

```text
Observation
```

The immutable semantic core includes concepts such as:

```text
ObservationId
ObservationType
ObservationSubject
ObservationPayload
ObservationOrigin
ObservationFingerprint
ObservedAt
ArrivedAt
```

Its controlled mutable state is the admission lifecycle.

---

# 12. Observation lifecycle

The allowed lifecycle is:

```text
             ┌──→ ACCEPTED
RECEIVED ────┤
             └──→ REJECTED
```

`ACCEPTED` and `REJECTED` are terminal in v1.

Forbidden transitions include:

```text
ACCEPTED → REJECTED
REJECTED → ACCEPTED
ACCEPTED → ACCEPTED
REJECTED → REJECTED
```

This protects the authoritative history of what FOLD decided at ingestion time.

Replay, if added later, must not silently mutate this historical state.

---

# 13. decidedAt

When an Observation reaches a terminal admission state, we preserve:

```text
decidedAt
```

This is part of authoritative Observation state.

It is not stored only inside an event.

That means reconstructing an Observation from PostgreSQL does not require replaying an event log merely to know when the decision happened.

A decision cannot precede `ArrivedAt`.

---

# 14. Aggregate restoration

The aggregate has a persistence reconstruction path.

Conceptually:

```text
Observation.restore(...)
```

This is different from receiving a new Observation.

Restoration does not create new lifecycle events or perform a new admission decision.

Crucially, restored state is validated against the same lifecycle invariants.

Persistence cannot reconstruct impossible states such as:

```text
ACCEPTED + rejection reason
```

or:

```text
REJECTED + no rejection reason
```

Value-object invariants and canonicalization rules also remain active during reconstruction.

Persistence restoration is not allowed to bypass domain validity.

---

# 15. Aggregate identity

Observation entity equality is based on:

```text
ObservationId
```

rather than all fields.

Lifecycle changes therefore do not change aggregate identity.

The fingerprint is not the Observation ID.

This is important:

```text
ObservationId          = aggregate identity
ObservationFingerprint = semantic duplicate identity
```

They serve different purposes.

---

# 16. Domain events

Slice 2 introduced internal domain events:

```text
ObservationAccepted
ObservationRejected
```

These represent facts inside the Observations domain.

They are distinct from public integration events.

That separation is intentional:

```text
Domain Event
    ≠
Integration Event
```

Internal domain evolution must not silently mutate a public cross-module contract.

---

# 17. Slice 3 — Fingerprinting

Slice 3 answered one of the most important questions in ingestion:

> When do two submissions represent the same semantic Observation?

We introduced:

```text
ObservationFingerprint
ObservationFingerprintPolicy
Sha256ObservationFingerprintPolicy
```

Fingerprinting is deterministic and versioned.

The fingerprint value object itself also protects the canonical representation of supported fingerprint algorithms.

This means fingerprint correctness does not depend on callers remembering to use a particular factory method.

---

# 18. Fingerprint versioning

A fingerprint carries conceptually:

```text
canonicalization version
digest algorithm
digest
```

For example:

```text
v1
SHA-256
<64 hexadecimal characters>
```

This is critical because SHA-256 alone does not describe what was hashed.

If canonicalization rules change in the future, FOLD must create a new fingerprint version rather than silently changing the meaning of existing hashes.

For SHA-256, `ObservationFingerprint` enforces the algorithm-specific representation invariant itself.

A SHA-256 fingerprint must contain exactly 64 hexadecimal characters.

Its hexadecimal representation is canonicalized to lowercase.

For example, equivalent inputs such as:

```text
ABCDEF...
```

and:

```text
abcdef...
```

cannot become distinct domain fingerprints merely because of hexadecimal case.

These rules are enforced in the value object's canonical construction path itself.

They are not enforced only by convenience factories such as:

```text
sha256(...)
sha256V1(...)
```

This is important because persistence reconstruction and other legitimate callers may directly construct an `ObservationFingerprint`.

There must be no alternate construction path capable of creating a non-canonical SHA-256 fingerprint.

---

# 19. Fingerprint canonicalization

Fingerprint generation does not hash arbitrary JSON serialization.

That would create coupling to Jackson configuration and potentially unstable representation.

Instead, the policy builds a deterministic canonical representation.

The canonical representation differentiates types explicitly and uses unambiguous encoding.

For fingerprint version 1, semantic inputs include concepts such as:

```text
ObservationType
subject type
subject external key
SourceId
external source record identifier
canonical ObservedAt
normalized semantic payload
```

Properties intentionally excluded include:

```text
ObservationId
ArrivedAt
EvidenceId
status
rejection
decidedAt
```

The reasoning is important.

`ArrivedAt` must not make the exact same semantic submission unique merely because it arrived again five seconds later.

`EvidenceId` is provenance of the submission rather than semantic Observation identity.

Lifecycle state obviously cannot participate because fingerprinting happens before admission.

`ObservedAt` participates in fingerprinting after its own value-object canonicalization.

For fingerprint version 1, this means microsecond precision.

Consequently, timestamps such as:

```text
2026-10-02T10:00:00.1234561Z
2026-10-02T10:00:00.1234569Z
```

canonicalize to the same `ObservedAt` value:

```text
2026-10-02T10:00:00.123456Z
```

and therefore do not produce different fingerprints merely because of sub-microsecond differences that the authoritative store cannot preserve.

By contrast:

```text
2026-10-02T10:00:00.123456Z
2026-10-02T10:00:00.123457Z
```

remain semantically distinct timestamps for fingerprint v1.

This precision rule is part of the fingerprint-v1 semantic contract.

---

# 20. Payload fingerprint semantics

Payload fields are canonicalized independent of field order.

Therefore:

```text
name, country
```

and:

```text
country, name
```

can produce the same semantic fingerprint when all values are identical.

List ordering remains significant.

Thus:

```text
[first, second]
```

is not assumed equivalent to:

```text
[second, first]
```

Numbers are canonicalized so equivalent decimal scale does not create artificial differences where appropriate.

Typed values also prevent collisions such as textual `"1"` versus numeric `1`.

---

# 21. ObservationRepository domain port

The domain owns:

```text
ObservationRepository
```

This is not Spring Data.

It is not a generic repository.

It describes exactly what Observations needs.

Important operations include conceptually:

```text
add observation
find by ObservationId
find by fingerprint
```

The interface belongs to the domain.

PostgreSQL implements it from infrastructure.

---

# 22. Deduplication policy

`ObservationDeduplicationPolicy` performs the early/advisory duplicate check.

Conceptually:

```text
fingerprint
    ↓
repository lookup
    ↓
Unique | Duplicate(existingObservationId)
```

The result is explicit through `ObservationDeduplicationResult`.

It is not merely a boolean because downstream logic needs to know which Observation already owns that fingerprint.

---

# 23. Advisory vs authoritative deduplication

This is a crucial system guarantee.

The first lookup is not authoritative under concurrency.

Two requests may do:

```text
Request A: fingerprint absent
Request B: fingerprint absent
```

simultaneously.

Therefore correctness is:

```text
application duplicate check
        +
database unique constraint
        =
correct deduplication
```

The database is the final authority.

Canonical fingerprint representation is therefore also important for database-level uniqueness.

Equivalent fingerprints must not be able to reach persistence using alternate textual representations.

---

# 24. Slice 4 — Submission use case

Slice 4 introduced the primary write workflow.

The application boundary is now conceptually:

```text
SubmitObservationUseCase
```

implemented by:

```text
SubmitObservationHandler
```

The handler orchestrates domain behavior but should not contain domain rules itself.

---

# 25. SubmitObservationCommand

The command contains normalized input supplied to the use case.

It deliberately does not accept externally generated values such as:

```text
ObservationId
ArrivedAt
fingerprint
status
```

Those belong to FOLD's processing.

The caller supplies semantic input.

FOLD creates authoritative processing metadata.

---

# 26. Submission pipeline

The workflow is:

```text
SubmitObservationCommand
        ↓
fingerprint calculation
        ↓
advisory duplicate check
        ↓
if duplicate:
    return existing Observation
        ↓
otherwise:
    Observation.receive(...)
        ↓
acceptance policy
        ↓
ACCEPT or REJECT
        ↓
authoritative repository add
        ↓
database may still report duplicate race
        ↓
integration event
        ↓
SubmitObservationResult
```

Input temporal values participating in semantic fingerprint identity are already represented through their domain value objects before fingerprint calculation.

For `ObservedAt`, this means fingerprinting sees the canonical microsecond representation.

---

# 27. Acceptance policy

We created:

```text
ObservationAcceptancePolicy
ObservationAcceptanceDecision
DefaultObservationAcceptancePolicy
```

Structural invalidity is handled by domain construction invariants.

The acceptance policy exists for semantic admission rules.

Currently the default policy accepts structurally valid Observations because no genuine semantic rejection rule has yet been introduced.

This was intentional.

We did not invent fake business rules merely to make the abstraction look busy.

Future admission rules belong here rather than inside the HTTP controller or application handler.

---

# 28. SubmitObservationResult

Submission has exactly three meaningful outcomes:

```text
Accepted
Rejected
Duplicate
```

`Accepted` means a new authoritative Observation was stored and admitted.

`Rejected` means a new authoritative Observation was stored but not admitted downstream.

`Duplicate` means no new semantic Observation was created; an existing Observation already represents this submission.

Again:

```text
Duplicate != ObservationStatus
```

---

# 29. ObservationAddResult

Repository insertion has its own result:

```text
Added
Duplicate(existingObservationId)
```

This exists specifically because a submission may pass the preliminary deduplication check and then lose a race at database insertion time.

The repository therefore returns authoritative storage outcome rather than throwing a generic uniqueness exception into application code.

You additionally corrected the in-memory implementation so re-adding the same Observation itself behaves idempotently rather than being incorrectly interpreted as a distinct duplicate.

---

# 30. Public integration events

The public API currently includes versioned integration events:

```text
ObservationAcceptedV1
ObservationRejectedV1
DuplicateObservationDetectedV1
```

These belong under:

```text
com.fold.modules.observations.api.event
```

not under the domain.

They are public contracts.

The `V1` suffix is intentional.

Consumers must not depend on internal domain event classes.

---

# 31. Meaning of ObservationAcceptedV1

This event means:

> An authoritative Observation has been created and accepted for downstream processing.

A downstream module such as Claims may use this event as its trigger.

It does not mean that the Observation content has been proven true.

---

# 32. Meaning of ObservationRejectedV1

This announces that an Observation was retained but rejected by the admission process.

Most knowledge-producing downstream consumers should normally not process rejected Observations.

The Observation still exists for auditability and potentially future replay or investigation.

---

# 33. Meaning of DuplicateObservationDetectedV1

This event represents submission-processing behavior.

No new Observation lifecycle transition occurred.

It identifies the already existing Observation.

A consumer should not interpret it as a second piece of independent evidence unless some later provenance design explicitly says otherwise.

That distinction will matter greatly for evidence independence.

---

# 34. Event metadata

Integration events travel inside the kernel event envelope and metadata model.

Relevant concepts include:

```text
EventId
CorrelationId
CausationId
emittedAt
producer
event type
event version
```

This creates lineage for future distributed/event-driven processing.

Correlation and causation are intended to support tracing a chain such as:

```text
ObservationAccepted
      ↓
ClaimGenerated
      ↓
BeliefUpdated
      ↓
ConflictDetected
```

without losing causal provenance.

---

# 35. Slice 5 — PostgreSQL persistence

PostgreSQL is now the authoritative Observation store.

The `observations` table stores the aggregate state.

It includes conceptual columns for:

```text
ObservationId
type
subject
origin references
fingerprint
ObservedAt
ArrivedAt
status
decidedAt
rejection
payload
```

`observed_at` is persisted explicitly as:

```text
TIMESTAMPTZ(6)
```

The explicit precision matches the `ObservedAt` domain canonicalization contract.

This prevents fingerprint semantics from depending on sub-microsecond temporal precision that the authoritative Observation store cannot preserve.

The relationship is therefore intentionally:

```text
ObservedAt domain representation
        =
fingerprint temporal representation
        =
PostgreSQL observed_at representation
```

for supported temporal precision.

---

# 36. Database lifecycle constraints

The database does not simply trust Java.

It also protects Observation lifecycle consistency.

Conceptually:

```text
RECEIVED:
    decidedAt = null
    rejection = null

ACCEPTED:
    decidedAt != null
    rejection = null

REJECTED:
    decidedAt != null
    rejection != null
```

The database also enforces decision-time consistency.

This is intentional defense in depth.

The domain remains the primary semantics owner, while the database prevents corrupted persistent states.

---

# 37. Fingerprint uniqueness in PostgreSQL

The database enforces uniqueness across the complete fingerprint identity:

```text
fingerprint_version
fingerprint_algorithm
fingerprint_value
```

This closes the concurrency race that application-level lookup cannot eliminate.

For SHA-256, the domain representation is canonical lowercase hexadecimal.

The `ObservationFingerprint` value object validates and canonicalizes SHA-256 values before they can exist as valid domain fingerprints.

This includes direct construction paths used during persistence reconstruction.

Therefore equivalent uppercase and lowercase SHA-256 representations cannot legitimately become different Observation fingerprints merely because of textual case.

The database unique index remains the authoritative concurrency barrier, while domain canonicalization ensures that semantically equivalent digest representations reach that barrier consistently.

---

# 38. No cross-context foreign keys

`SourceId` and `EvidenceId` may be stored in the Observation row, but the Observations table does not create physical foreign-key coupling to another bounded context's persistence model.

This is deliberate.

Cross-module references are IDs, not ORM associations.

Future modules should follow the same rule unless a specific architecture decision changes it.

---

# 39. JSONB payload persistence

Observation payload is stored as PostgreSQL `JSONB`.

This is a storage representation only.

The domain still uses:

```text
ObservationPayload
ObservationField
ObservationValue
```

Jackson does not leak into domain code.

The infrastructure codec handles translation.

---

# 40. ObservationPayloadJsonCodec

The codec maps:

```text
domain ObservationPayload
        ↕
persistence JSON
```

It handles the supported typed values explicitly.

It must reject malformed persisted structures rather than silently coerce corrupted data.

This is important because persisted JSON is now durable system state.

The project is currently using Jackson 3, with imports under:

```text
tools.jackson.*
```

rather than legacy Jackson 2 imports.

We also corrected the relevant exception handling to Jackson 3's API, including use of `JacksonException`.

Where applicable, deprecated `JsonNode.textValue()` usage is being replaced by the current Jackson 3 API such as `stringValue()`.

---

# 41. ObservationPersistenceMapper

The persistence mapper converts between:

```text
Observation aggregate
        ↕
ObservationRecord
```

It must preserve the semantic state of the aggregate while reconstructing domain values through their normal invariants and canonicalization rules.

It handles:

```text
identity
type
subject
origin
fingerprint
payload
timestamps
status
decision time
rejection
```

Persistence reconstruction does not bypass domain value-object invariants.

For example:

```text
persisted SHA-256 fingerprint
        ↓
ObservationFingerprint
        ↓
validate 64 hexadecimal characters
        ↓
canonical lowercase representation
```

Likewise:

```text
persisted observed_at
        ↓
ObservedAt
        ↓
canonical microsecond representation
```

Reconstruction ultimately calls the aggregate restoration path so invalid lifecycle states are still rejected by the domain.

The mapper is not responsible for duplicating fingerprint or temporal canonicalization logic.

Those rules remain owned by the relevant domain value objects.

---

# 42. PostgresObservationRepository

`PostgresObservationRepository` implements the domain repository port using Spring JDBC/PostgreSQL.

Its responsibilities include:

```text
atomic insert
find by ObservationId
find by fingerprint
database-level duplicate resolution
domain reconstruction
```

The implementation was adjusted to work correctly with Spring's proxying requirements in your current setup, including removing `final` where proxying required it.

---

# 43. Transactional outbox

We introduced:

```text
integration_outbox
```

and:

```text
JdbcOutboxEventPublisher
OutboxEventSerializer
```

The important system guarantee is intended to be:

```text
Observation database write
+
integration event outbox write
=
same transaction
```

The desired failure semantics are:

```text
both commit
or
neither commits
```

This avoids the classic failure:

```text
Observation persisted
event lost
```

---

# 44. Outbox state

Outbox records include concepts such as:

```text
event_id
correlation_id
causation_id
event_type
event_version
occurred_at
emitted_at
producer
payload
state
attempts
available_at
published_at
created_at
```

Current initial state is conceptually:

```text
PENDING
```

A broker dispatcher has deliberately not yet been implemented.

There is currently no need to introduce Kafka merely to satisfy architectural aesthetics.

The durable boundary exists already.

A future dispatcher can consume pending outbox entries without changing the Observations domain.

---

# 45. Transactional application decorator

The application handler itself remains Spring-free.

Spring transaction management is applied externally through an infrastructure-level decorator around the submit use case.

Conceptually:

```text
@Transactional infrastructure wrapper
             ↓
pure SubmitObservationHandler
```

This preserves:

```text
Spring → application
```

rather than:

```text
application → Spring
```

---

# 46. Query/read side

We added a simple read use case for retrieving an Observation by ID.

Conceptually:

```text
GetObservationQuery
GetObservationHandler
ObservationView
```

The query returns a public view rather than exposing the domain aggregate.

This is important for module boundaries.

A consumer should not receive an internal mutable/behavioral aggregate merely because it wants read data.

---

# 47. ObservationView

`ObservationView` is a read representation intended for module/API boundaries.

It represents relevant Observation information without exporting domain implementation details.

Future modules that genuinely need Observation content should prefer an appropriate public query/view contract rather than importing:

```text
observations.domain.*
```

---

# 48. HTTP API

The v1 API currently exposes:

```text
POST /api/v1/observations
GET  /api/v1/observations/{id}
```

There is deliberately no:

```text
PUT
PATCH
DELETE
```

for Observation semantic content.

Observations are immutable ingestion records.

---

# 49. POST semantics

A successful new accepted or rejected Observation is created as a new authoritative resource.

Duplicate submission resolves to the existing resource.

Conceptually:

```text
new Observation
→ 201 Created

duplicate
→ 200 OK
```

The `Location` header points to the authoritative Observation.

---

# 50. GET semantics

Retrieving an existing Observation returns its public representation.

Unknown valid UUID:

```text
404 Not Found
```

Invalid UUID syntax:

```text
400 Bad Request
```

---

# 51. HTTP value mapping

The HTTP boundary translates JSON values into strongly typed domain values.

HTTP/Jackson concepts stop at infrastructure.

The HTTP mapper is responsible for interpreting transport structures such as:

```json
{
  "type": "text",
  "value": "Acme Ltd"
}
```

and converting them into domain types.

Invalid transport values become request errors, not malformed domain objects.

When HTTP input contains an `ObservedAt` timestamp with precision finer than microseconds, construction of the domain `ObservedAt` produces its canonical microsecond representation before that value participates in fingerprinting or persistence.

---

# 52. HTTP error handling

The infrastructure maps invalid Observation requests into HTTP errors using `ProblemDetail`.

Malformed JSON and semantically invalid request structures are differentiated from internal server bugs.

We deliberately do not catch every `RuntimeException` and turn it into `400`.

Programming/infrastructure failures must remain visible as server failures.

---

# 53. Spring Boot and runtime stack

The project has since been aligned with:

```text
Spring Boot 4.1.1
Jackson 3
PostgreSQL 17 test container
Spring Boot ServiceConnection support
```

You also upgraded/security-aligned relevant dependencies, including:

```text
Tomcat 11.0.26
Jackson BOM 3.1.7
```

This should be considered the current implementation baseline rather than the older APIs from the initial Slice 5 draft.

---

# 54. Testcontainers infrastructure

Reusable PostgreSQL integration support now exists under:

```text
com.fold.testsupport.PostgresIntegrationTest
```

It owns a real PostgreSQL Testcontainer.

The base class provides:

```text
@Testcontainers
@Container
@ServiceConnection
```

around the PostgreSQL container.

Important nuance:

`@ServiceConnection` does not itself create a Spring context.

Therefore there are two valid forms of database integration test.

A pure adapter integration test can create its own DataSource and run Flyway manually.

A Spring integration test additionally uses:

```text
@SpringBootTest
```

and may then use `@Autowired`.

---

# 55. Spring context integration testing

`ObservationHttpIntegrationTest` runs a full Spring Boot test context with real PostgreSQL.

It exercises a path approximately like:

```text
HTTP
 ↓
controller
 ↓
HTTP mapper
 ↓
application
 ↓
domain
 ↓
Postgres repository
 ↓
outbox
 ↓
database
```

It verifies not only response JSON but actual DB effects.

---

# 56. Existing test coverage by layer

The module now has tests across several layers.

Domain vocabulary tests cover the Observation value model.

Aggregate tests cover lifecycle transitions and invariants.

Fingerprint value-object tests cover both factory-based and direct-constructor behavior.

For SHA-256, tests verify:

```text
valid 64-character hexadecimal representation
lowercase canonicalization
rejection of invalid length
rejection of non-hexadecimal content
equivalence between factory and direct construction
```

This specifically ensures that persistence reconstruction cannot bypass SHA-256 invariants.

Temporal value-object tests cover `ObservedAt` microsecond canonicalization.

They verify both:

```text
nanosecond input
→ canonical microsecond ObservedAt
```

and:

```text
two timestamps differing only within the same microsecond
→ equal ObservedAt values
```

Fingerprint-policy tests cover deterministic identity and semantic differences.

They additionally verify that:

```text
sub-microsecond ObservedAt differences
→ same fingerprint
```

while:

```text
different canonical microseconds
→ different fingerprints
```

Deduplication tests cover unique/duplicate resolution.

Repository-domain fake tests cover the port contract.

Application handler tests cover submission orchestration.

Integration-event tests cover public event contracts.

Persistence codec tests cover JSON representation.

Persistence mapper tests cover aggregate reconstruction and domain canonicalization during reconstruction.

PostgreSQL integration tests cover the real adapter, database constraints, authoritative fingerprint uniqueness, and timestamp round-trip behavior.

HTTP mapper tests cover transport conversion.

HTTP integration tests cover the complete submission/retrieval path.

Architecture tests protect dependency direction.

Outbox integration tests validate durable event writing.

---

# 57. Architecture tests

We split architecture testing conceptually into:

```text
KernelArchitectureTest
ObservationsArchitectureTest
```

rather than allowing the kernel test to become a giant all-system rules file.

Production architecture imports exclude test classes using ArchUnit's test exclusion.

Rules include protections such as:

```text
kernel !→ modules
kernel !→ Spring
kernel !→ persistence frameworks

observations.domain !→ Spring
observations.domain !→ infrastructure
observations.domain !→ application
observations.domain !→ api
observations.domain !→ Jackson

observations.application !→ infrastructure
observations.application !→ Spring
observations.application !→ Jackson

observations.api.event !→ domain internals
```

These rules are important future guardrails.

---

# 58. What another bounded context is allowed to know

This is perhaps the most important section for future development.

A new bounded context should conceptually be allowed to know:

```text
ObservationId
public observations API contracts
versioned integration events
public observation views/queries when needed
```

It should not know:

```text
Observation aggregate implementation
ObservationRepository
PostgresObservationRepository
ObservationPayloadJsonCodec
ObservationPersistenceMapper
fingerprint canonicalization internals
database schema
outbox implementation
Spring configuration
HTTP request DTOs
```

---

# 59. Example: future Claims module

A future Claims bounded context may consume:

```text
ObservationAcceptedV1
```

The event currently gives it an Observation identity and event context.

If Claims needs more Observation data, we should explicitly decide how that public data crosses the boundary.

Possible legitimate designs are:

```text
Claims consumes event
      ↓
queries Observations public API/view
```

or a deliberately richer future integration event.

What Claims must not do is:

```text
import com.fold.modules.observations.domain.model.Observation;
```

or:

```text
@Autowired
ObservationRepository observationRepository;
```

or:

```text
SELECT * FROM observations;
```

Cross-module table reads are prohibited.

---

# 60. What Claims must understand semantically

Even without knowing Observations internals, Claims must understand one conceptual distinction:

```text
Observation = what was received
Claim       = proposition about the world
```

A Claim may be derived from an Observation.

It must not collapse the two concepts.

For example:

```text
Observation:
"Registry X reported that Company A has director B."

Claim:
Company A --hasDirector--> Person B
```

The Observation preserves what the source said.

The Claim represents FOLD's normalized epistemic proposition.

This separation is one of the core foundations of the entire system.

---

# 61. What Identity must understand

Identity should know that `ObservationSubject` is unresolved/source-facing.

It must not assume:

```text
ObservationSubject == Entity
```

Identity may later associate one or many source-facing subjects with a canonical entity.

That process must remain revisable.

---

# 62. What Evidence/Source components must understand

Observations refers to sources and evidence by ID.

It does not own their detailed metadata.

A Source component may eventually own concepts such as:

```text
coverage
reliability
freshness
dependencies
source metadata
```

Evidence may own immutable retrievable evidence objects.

Observations should continue referring to those objects rather than absorbing their ownership.

---

# 63. What event consumers must understand

Consumers should treat an integration event as a notification of a public fact, not as permission to reach into module internals.

For example:

```text
ObservationAcceptedV1
```

means that the Observation is available for downstream processing.

Consumers should be idempotent because outbox/event delivery may eventually be at-least-once.

That requirement becomes especially important when the dispatcher is implemented.

---

# 64. What future replay functionality must understand

Replay was deliberately not implemented yet.

The planned semantic rule is:

> Replay reprocesses an immutable historical Observation downstream; it does not edit the Observation or pretend the original ingestion happened again.

This distinction must be retained when replay arrives.

---

# 65. What future rejection functionality must understand

Rejected Observations are retained.

If acceptance rules improve later, we may create explicit re-evaluation/replay workflows.

We should not simply mutate historical rejection decisions invisibly.

FOLD is intended to preserve epistemic and processing history.

---

# 66. What future fingerprint changes must understand

Fingerprint v1 is now a contract.

Do not casually change its canonicalization.

The contract includes not only the digest algorithm, but the semantic representation that is hashed.

For v1, this includes the current canonical treatment of:

```text
ObservationType
subject identity
SourceId
external source record identity
ObservedAt
payload field semantics
numeric representation
typed values
field ordering rules
list ordering rules
```

`ObservedAt` microsecond precision is part of this contract.

SHA-256 lowercase hexadecimal representation is also part of the canonical fingerprint representation.

If semantic identity rules change in the future, create:

```text
fingerprint version 2
```

rather than silently changing version 1.

Otherwise old and new Observations could become inconsistently deduplicated.

A seemingly small refactoring of canonicalization logic may therefore be a fingerprint-versioning change rather than an implementation detail.

---

# 67. What future database migrations must understand

The database stores authoritative Observation history.

Changes to:

```text
payload representation
fingerprint fields
timestamp precision
lifecycle semantics
outbox structure
```

must therefore be migration-aware.

In particular, `observed_at` precision must remain aligned with the semantic precision used by fingerprint v1.

Changing its effective precision independently from the domain/fingerprint contract could reintroduce a mismatch between:

```text
what was fingerprinted
```

and:

```text
what was durably retained
```

Jackson serialization format should not be treated as disposable implementation trivia once persisted.

The codec effectively defines a durable persistence format.

---

# 68. What future HTTP evolution must understand

HTTP is an infrastructure adapter.

The domain must not evolve around convenience of JSON clients.

Transport DTOs may change/version independently from internal domain structures.

A new API version should not require exposing domain classes directly.

External timestamps may arrive with precision greater than the canonical `ObservedAt` precision.

The HTTP boundary does not own that temporal rule.

It passes the semantic timestamp into the domain representation, where `ObservedAt` performs canonicalization.

---

# 69. What future broker integration must understand

The outbox is the durable publication boundary.

When Kafka or another broker is eventually introduced, the intended direction is approximately:

```text
DB transaction
    ↓
integration_outbox(PENDING)
    ↓
dispatcher
    ↓
broker
    ↓
mark PUBLISHED
```

Broker code should not be introduced into `SubmitObservationHandler`.

The domain and application workflow should remain unchanged.

---

# 70. Current known remaining work around Observations

The core v1 feature is essentially complete, but there are still infrastructure-hardening tasks rather than major domain design tasks.

The most important test still worth having is explicit proof that:

```text
Observation write
+
outbox write
```

are transactionally atomic.

That means a deliberately failing publisher/outbox path should roll back the Observation write.

Additional useful test completion includes complete lifecycle persistence round-trips, all ObservationValue JSONB round-trips, malformed JSON, invalid path IDs, and complete outbox-envelope persistence.

These are Slice 5 hardening tasks, not new domain functionality.

The recently added fingerprint and temporal canonicalization protections should be preserved as regression coverage because they protect durable semantic contracts rather than incidental implementation behavior.

---

# 71. Current end-to-end behavior

At the end of Slice 5, the system is capable of:

```text
normalized input
      ↓
HTTP request
      ↓
typed domain/application values
      ↓
ObservedAt canonicalization
      ↓
deterministic fingerprint v1
      ↓
canonical SHA-256 fingerprint representation
      ↓
duplicate check
      ↓
Observation(RECEIVED)
      ↓
acceptance decision
      ↓
ACCEPTED / REJECTED
      ↓
authoritative PostgreSQL insert
      ↓
database uniqueness race protection
      ↓
transactional outbox event
      ↓
HTTP result
```

The resulting Observation can then be retrieved through the read endpoint.

For temporal values participating in fingerprint identity, the representation returned from authoritative persistence remains consistent with the representation used during fingerprint generation.

---

# 72. The most important invariants to preserve

If someone works on FOLD six months from now and remembers only a handful of Observation rules, they should remember these:

```text
Observation records what FOLD received.
Observation does not represent truth.

Observation semantic content is immutable.

Lifecycle:
RECEIVED → ACCEPTED | REJECTED.

Duplicate is not a lifecycle state.

Rejected Observations are retained.

ObservationSubject is not EntityId.

Fingerprint is not ObservationId.

Fingerprint semantics are versioned.

Known fingerprint algorithms have canonical representations.

SHA-256 fingerprints are exactly 64 hexadecimal characters
and are represented canonically in lowercase.

Fingerprint invariants apply to every construction path,
including persistence reconstruction.

ObservedAt is canonicalized to microsecond precision.

Sub-microsecond differences therefore do not create distinct
ObservedAt values or fingerprint identities in v1.

Timestamp itself is not globally restricted to microseconds.

Application deduplication is advisory.
Database uniqueness is authoritative.

The representation fingerprinted by FOLD must remain compatible
with the representation retained by authoritative persistence.

Domain does not know Spring/Jackson/PostgreSQL/HTTP.

Other modules consume public API contracts, not domain internals.

Domain Events and Integration Events are different things.

Observation persistence and event publication belong to one
transactional reliability boundary.
```

That is essentially the constitution of the module.

---

# 73. How the next module should approach Observations

When we begin the next bounded context, we should not begin by studying every class in `observations`.

We should begin with one question:

> What public fact or data does this bounded context require from Observations?

For Claims, for example, the likely starting point is:

```text
ObservationAcceptedV1
```

Then we design the smallest explicit public integration contract necessary.

The next module should be independently modeled from first principles.

Observations is a producer/upstream bounded context, not its superclass.

---

# 74. Final architectural state

The result of the five slices is not simply “an observations table”.

It is a bounded context with a clearly defined responsibility and complete processing boundary:

```text
                 OBSERVATIONS
┌─────────────────────────────────────────────┐
│                                             │
│ Public API                                  │
│   versioned events                          │
│   queries/views                             │
│                                             │
│ Application                                 │
│   submit observation                        │
│   retrieve observation                      │
│                                             │
│ Domain                                      │
│   Observation aggregate                     │
│   semantic vocabulary                       │
│   lifecycle                                 │
│   acceptance                                │
│   fingerprinting                            │
│   deduplication                             │
│   repository port                           │
│                                             │
│ Infrastructure                              │
│   PostgreSQL                                │
│   JSONB codec                               │
│   JDBC repository                           │
│   transactional outbox                      │
│   HTTP                                      │
│   Spring wiring                             │
│                                             │
└─────────────────────────────────────────────┘
```

And the external contract is intentionally much smaller than everything inside that box.

The current implementation also establishes two particularly important canonicalization boundaries:

```text
ObservedAt
→ canonical microsecond temporal representation

ObservationFingerprint(SHA-256)
→ canonical validated lowercase digest representation
```

These are not cosmetic formatting decisions.

They protect the consistency between semantic identity, domain state, deduplication, persistence reconstruction, and authoritative database uniqueness.

That is exactly what we wanted before moving on: Observations is now a coherent upstream capability that later bounded contexts can consume without inheriting its implementation complexity.