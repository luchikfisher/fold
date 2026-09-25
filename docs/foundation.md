# FOLD Foundation

## Purpose

The FOLD foundation contains the smallest set of concepts that must behave
consistently across the entire system.

It is not a general-purpose utility layer.

A type belongs in the foundation only when its meaning is independent of every
specific FOLD bounded context and when using different definitions in different
modules would make the system internally inconsistent.

The foundation currently defines three concerns:

- domain identity;
- time;
- domain and integration event semantics.

Everything else belongs to the bounded context that owns it.

---

## Design Rule

The foundation must remain smaller than the domains built on top of it.

Adding a type to the foundation creates a dependency that may eventually be
shared by every module in FOLD. For that reason, convenience is not sufficient
justification for placing code here.

A type belongs here only when it represents a genuine system-wide semantic
primitive.

The foundation must not contain:

- business entities;
- repositories;
- domain services;
- application services;
- persistence models;
- framework-specific APIs;
- generic helper collections created only for convenience;
- base classes intended to reduce duplication;
- speculative abstractions for future features.

---

## Identity

A domain identifier represents identity and nothing else.

A domain identifier must be:

- stable for the lifetime of the object it identifies;
- immutable;
- suitable for equality comparison;
- serializable into a stable external representation;
- semantically opaque to consumers.

Consumers must not infer domain information from the internal structure of an
identifier.

`DomainId` defines the common identifier contract.

`UuidDomainId` is the initial UUID-backed implementation. The presence of this
implementation does not make UUID part of the universal domain contract.

Domain-specific identifiers such as `ObservationId`, `ClaimId`, and `EntityId`
are introduced only when their owning domain is implemented.

---

## Time

FOLD represents absolute machine time using `Instant`.

Domain and application code must not obtain the current time directly from the
operating system. Code whose behavior depends on the current time receives a
`FoldClock`.

This rule exists so that time-dependent behavior remains deterministic,
reproducible, and testable.

`TimeRange` uses half-open interval semantics:

    [start, end)

The start is included and the end is excluded.

Therefore:

    [10:00, 11:00)
    [11:00, 12:00)

are adjacent but do not overlap.

An absent end represents an interval that continues indefinitely.

Zero-length and inverted ranges are invalid.

These semantics must remain consistent anywhere FOLD represents intervals of
time unless a bounded context explicitly models a different concept.

---

## Domain Events

A domain event is a fact that has already occurred inside a domain model.

Examples:

    ClaimSuperseded
    EntityMerged
    ConflictResolved

A domain event describes domain history. It is not automatically an external
or cross-module contract.

Internal domain events may evolve together with the implementation of their
owning bounded context.

---

## Integration Events

An integration event is a versioned contract through which one bounded context
communicates a fact to other bounded contexts.

An integration event must describe something that has already happened.

Commands and requests are not integration events.

Every integration event exposes:

- a stable logical event type;
- an explicit contract version;
- the time at which the represented fact occurred.

A consumer may depend on the integration event contract. It must not depend on
the publisher's internal domain model.

Integration events should contain enough information for a normal consumer to
react without immediately querying the publishing module, but they should not
duplicate the publisher's complete aggregate.

---

## Event Envelope

The event payload and message metadata are separate concepts.

The payload describes the fact:

    what happened

The envelope metadata describes the emitted message:

    which emitted event is this
    when was it emitted
    which causal chain does it belong to
    what immediately caused it
    which component emitted it

Every emitted integration event has a unique `eventId`.

`correlationId` identifies the wider causal chain.

`causationId` identifies the immediate event or action that caused the current
event.

For the root event of a causal chain, the event ID is also used as the
correlation ID and no causation ID exists.

This distinction is fundamental to replay, provenance, diagnostics, and
event-chain analysis.

---

## Framework Boundary

The kernel is framework-independent.

Code under:

    com.fold.kernel

must not depend on Spring or on any infrastructure implementation.

Framework adapters implement kernel contracts from outside the kernel.

For example:

    com.fold.kernel.time.FoldClock

is a foundation abstraction, while:

    com.fold.platform.time.SystemFoldClock

is the Spring-backed production adapter.

This boundary is enforced by architecture tests.

---

## Documentation Rule

Public foundation types require Javadoc when their semantic contract is not
fully obvious from their declaration.

Javadoc describes:

- what the concept means;
- its invariants;
- important boundary semantics;
- behavior that callers are allowed to rely on.

Javadoc should not restate implementation details that are already obvious
from the code.

Inline comments are reserved for non-obvious implementation decisions whose
reason cannot be expressed clearly through naming, structure, or Javadoc.

The preferred order is:

1. clear code;
2. precise naming;
3. Javadoc for semantic contracts;
4. inline comments only when the reason would otherwise remain unclear.