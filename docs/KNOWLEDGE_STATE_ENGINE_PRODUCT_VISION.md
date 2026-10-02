# Knowledge State Engine

## Product Vision and System Definition

**Document status:** Foundational product specification  
**Role:** Primary product reference for implementation, design, architecture, and prioritization  
**Language:** English  
**Audience:** Engineering, product, design, data, research, operations, and future contributors

---

## 1. Purpose of This Document

Knowledge State Engine is a product for continuously constructing, revising, explaining, and investigating a coherent
model of reality from heterogeneous evidence. This document defines the product as it is intended to exist at maturity.
It is not a project diary, a record of design discussion, or a list of alternatives. It is the governing product
description against which implementation decisions should be tested.

The system is built around a simple but strict premise: complex data environments do not contain a single clean truth.
They contain observations, claims, records, documents, events, partial identities, contradictions, stale information,
inferred relationships, uncertain hypotheses, and unresolved gaps. A useful system must preserve these distinctions
instead of flattening them into a single database row or graph edge.

Knowledge State Engine therefore maintains an evolving model of what is known, what is believed, why it is believed,
what conflicts with it, what remains uncertain, and what evidence could most efficiently reduce that uncertainty. The
end product is not a graph database, not an entity-resolution library, not a search engine, and not an AI assistant. It
is an operational environment for understanding entities and their surrounding reality across many data sources while
retaining provenance, time, uncertainty, and reversibility.

This document places the final product experience first. The internal system exists to make the end-user experience
precise, fast, explainable, and dependable. A feature that cannot be traced to a meaningful product capability should
not acquire architectural importance merely because it is technically interesting.

---

## 2. Product Definition

Knowledge State Engine continuously transforms normalized observations from many sources into a live knowledge state
composed of entities, claims, relations, events, evidence, beliefs, conflicts, hypotheses, knowledge gaps, and
decisions.

The system does not claim to store absolute truth. It stores the evolving case for what is true.

For any important statement, relationship, identity, or conclusion, the system should be capable of answering the
following questions:

- What exactly is being asserted?
- Which source or sources asserted it?
- What evidence supports it?
- When was it true in the world, if known?
- When did the system learn it?
- How reliable are the supporting sources?
- Is the evidence independent or duplicated through source dependency?
- Is there contradictory evidence?
- What is the current confidence in the conclusion?
- Is the conclusion observed, derived, inferred, disputed, stale, superseded, or unresolved?
- Which downstream conclusions depend on it?
- What changed when new evidence arrived?
- What information is still missing?
- Which next action would most improve the current understanding?

The product presents this state through investigation-oriented interfaces rather than through raw schemas or storage
structures. A user should be able to begin with a person, organization, event, address, document, account, or question
and quickly understand the relevant connected context without manually joining datasets or reconciling incompatible
records.

---

## 3. The Product Promise

The product promise is:

> **Understand any entity across all available evidence, including what is known, uncertain, changing, disputed, and
worth investigating next.**

A stronger internal formulation is:

> **Build a continuously revisable model of reality from heterogeneous evidence, where every entity, relationship, and
conclusion remains traceable, temporal, uncertain when necessary, and open to investigation.**

The system must be useful before advanced machine learning is introduced. Rules, deterministic resolution, explicit
claims, event history, provenance, and graph projection should already produce a coherent product. Machine learning and
language models are accelerators and reasoning aids, not structural dependencies.

---

## 4. Product Boundaries

Knowledge State Engine begins after basic ingestion and semantic normalization. Upstream systems may accept CSV, JSON,
APIs, relational data, streams, documents, extracted text, and other formats. Those systems are responsible for
obtaining the data and producing semantically interpretable observations.

The engine assumes it can receive normalized items such as:

- observations;
- candidate entities;
- candidate relations;
- candidate events;
- attributes;
- evidence references;
- source metadata;
- validity timestamps;
- confidence metadata where available.

The engine is not intended to become a universal ETL platform, document-management system, data warehouse, generic
workflow product, or general-purpose graph database. It can integrate with all of those categories, but its product
identity remains knowledge-state construction and investigation.

The system may eventually provide convenience ingestion adapters, source connectors, schema mapping, extraction, or
ontology assistance, but these remain peripheral to the core product.

---

## 5. Core Product Principles

### 5.1 Claims, Not Flattened Facts

The foundational unit of asserted knowledge is a claim, not a supposedly final fact.

A source does not cause the system to store simply:

`Alice — CEO_OF → Acme`

Instead, the system stores a structured claim stating that a particular source, supported by particular evidence,
asserted that Alice held the role of CEO of Acme during a particular validity interval, observed by the system at a
particular time.

This distinction permits multiple sources to disagree, allows information to be corrected retroactively, preserves
historical assertions, and prevents the system from silently overwriting the evidentiary record.

### 5.2 Entities Are Resolved Identities, Not Rows

An entity is not merely a row copied from a source. It is the system's current identity model for a real-world object.

Several observations may refer to the same person or organization. The system must preserve the distinction between
source records and resolved identity. Identity may remain uncertain. Alternative hypotheses may coexist until stronger
evidence arrives. A merge must remain reversible. A split must be possible without reconstructing the original data from
scratch.

### 5.3 Uncertainty Is First-Class State

Uncertainty is not treated as an error to be hidden. Confidence, ambiguity, disagreement, freshness, evidence
independence, and unresolved identity are explicit parts of the model.

The UI must not create false precision. A relationship with moderate support should look and behave differently from a
verified relationship. A disputed claim should remain visibly disputed. A stale claim should remain distinguishable from
a recent claim of equal historical reliability.

### 5.4 Time Is Part of Meaning

Every important assertion should be understood on at least two time axes where possible:

**Valid time** describes when the statement was true in the world.

**Knowledge time** describes when the system knew or believed the statement.

These must not be conflated. The system should answer both “Who was the director on March 1?” and “Who did we believe
was the director on March 1?”

### 5.5 Evidence and Inference Must Remain Distinct

Observed evidence, deterministic derivation, probabilistic inference, and analyst hypothesis must never collapse into
the same state.

A model-generated relationship can become useful and prominent without being disguised as observed evidence. Every
derived conclusion must retain the path back to the claims, evidence, algorithm, model version, rules, and decisions
that produced it.

### 5.6 Contradiction Is Valuable Information

Contradiction is not automatically noise. It can reveal stale sources, identity errors, changing states, jurisdictional
differences, reporting delay, source copying, or genuinely disputed facts.

The system therefore treats conflicts as explicit objects that can be investigated, resolved, deferred, reopened, and
analyzed across sources and entities.

### 5.7 Missing Knowledge Is Also Knowledge

The system should identify what is missing, not only what is present. Missing beneficial ownership, missing independent
corroboration, unknown transfer purpose, incomplete historical coverage, or unresolved identity should be represented as
knowledge gaps with impact and priority.

The product should make these gaps actionable by recommending evidence-acquisition paths and next-best investigative
actions.

### 5.8 Reversibility Is Mandatory

Derived conclusions must be reversible. Entity merges, identity resolutions, belief calculations, conflict resolutions,
hypotheses, graph projections, and reports must retain enough lineage to be revised when new evidence arrives or when a
model changes.

No model output should irreversibly mutate reality representation without retaining its previous state and cause.

---

## 6. Canonical Knowledge Objects

### 6.1 Source

A Source represents the origin of information. It may be a registry, database, website, API, internal system, analyst,
document collection, sensor, public filing system, or derived system feed.

The product tracks source category, jurisdiction, update cadence, freshness, coverage, field-level reliability, known
limitations, dependencies on other sources, historical agreement with resolved outcomes, and ingestion health.

Reliability is contextual rather than universal. A source may be excellent for corporate addresses and weak for
beneficial ownership. Reliability may also decay or improve over time.

### 6.2 Evidence

Evidence is the addressable material from which a claim originates. It may be a document section, filing, row,
transaction record, structured response, paragraph, image region, message, or other concrete observation.

Evidence must remain retrievable where permissions permit. Evidence snapshots should be immutable where feasible so that
future users can inspect what was actually available when a conclusion was formed.

### 6.3 Observation

An Observation represents what the system received before it decides what the information means in the broader world
model.

An observation retains its original semantic payload, origin, fingerprint, arrival time, observed time, evidence
reference, and processing metadata. It provides a strict boundary between incoming information and later interpretation.

### 6.4 Claim

A Claim is a structured assertion derived from an observation or explicitly entered by an analyst or trusted process.

A claim includes a subject, predicate, object or value, source, evidence, valid time, observed time, derivation type,
confidence metadata, lifecycle status, and revision history.

Claims may be observed, derived, inferred, analyst-entered, superseded, invalidated, reinstated, stale, disputed, or
active.

### 6.5 Identity Hypothesis

An Identity Hypothesis expresses the possibility that several candidate records or observations refer to the same
real-world entity.

It carries supporting and opposing evidence, a score, model or rule version, contextual conditions, and lifecycle state.
Multiple identity hypotheses may coexist. The system must not force a premature single answer where the evidence does
not justify one.

### 6.6 Entity

An Entity is the canonical system identity for a real-world object at a given point in the system's knowledge history.

Entity types may include person, organization, location, address, account, asset, product, device, vehicle, document,
legal instrument, or ontology-defined extensions.

An entity has aliases, attributes, identity confidence, canonical handles, state history, and relationships to source
observations. Merges and splits are revisions of identity, not destructive rewrites.

### 6.7 Relation

A Relation represents a binary relationship where a two-ended structure is natural and sufficient.

Examples include parent-of, director-of, headquartered-at, owns, member-of, registered-at, or related-to. Relations
include validity, confidence, provenance, status, and revision history.

### 6.8 Event

An Event represents something that happened. Events are first-class objects because many important facts cannot be
represented correctly as a simple edge.

Transactions, appointments, resignations, acquisitions, meetings, filings, visits, shipments, transfers, court actions,
communications, and ownership changes may all be events with multiple participants, roles, amounts, locations,
documents, and temporal properties.

### 6.9 Belief

A Belief is the system's current assessed position regarding a proposition or state.

A belief aggregates supporting claims, contradictory claims, source reliability, evidence independence, temporal
consistency, freshness, analyst decisions, and model output. It is explicitly distinct from evidence.

Beliefs can strengthen, weaken, become stale, be superseded, or be invalidated. Historical belief states remain
queryable.

### 6.10 Conflict

A Conflict represents incompatible claims or interpretations that cannot simultaneously hold under the applicable
ontology and temporal constraints.

Conflicts have type, severity, involved claims, temporal overlap, possible explanations, current resolution status,
analyst decisions, and impact on downstream beliefs or investigations.

### 6.11 Hypothesis

A Hypothesis is an explicit proposition under investigation. It contains a statement, supporting signals,
counter-signals, assumptions, confidence, competing hypotheses, dependencies, and impact surface.

A hypothesis may be proposed, active, under review, strengthened, weakened, verified, disproven, superseded, or
archived.

### 6.12 Knowledge Gap

A Knowledge Gap represents an unresolved absence of information that materially limits understanding.

It includes the missing information, affected entities, blocked hypotheses, impacted investigations, expected
information gain, acquisition effort, candidate evidence sources, priority, and gap plan.

### 6.13 Decision

A Decision captures a human or approved system judgment. Examples include accepting or rejecting an entity merge,
resolving a conflict, promoting a hypothesis, deferring a gap, overriding a source assessment, or approving a report.

Every decision retains actor, time, available evidence, alternatives, rationale, and outcome. Decisions do not silently
mutate other domains; they create explicit state transitions that other domains can apply.

### 6.14 Investigation

An Investigation is a persistent analytical workspace centered on a question or objective. It contains selected
entities, hypotheses, findings, notes, bookmarks, evidence, unresolved gaps, decisions, scope, collaborators, and time
window.

An investigation is not a generic task-management object. It exists to structure knowledge and reasoning around a real
investigative question.

---

## 7. The Final Product Experience

The final product is a desktop-first professional analytical environment. It should feel closer to a high-end
intelligence and research operating system than to a generic business dashboard.

The user does not begin with a database schema. The user begins with an entity, a question, an investigation, a
watchlist, an alert, a report, or a meaningful change.

The interface uses a stable application shell containing global search, primary navigation, environment status,
notifications, user context, and system freshness indicators. The product is information-dense but restrained. Density
is deliberate: the target user needs to inspect many related signals at once without losing trust in where they came
from.

The central product areas are described below.

---

## 8. Command Center

The Command Center is the default operational home page. It answers one question: **What deserves attention now?**

It presents the current state of active work rather than a generic analytics dashboard.

The page includes:

- active investigations;
- watched entities;
- open conflicts;
- new evidence;
- belief changes;
- strengthened hypotheses;
- average convergence latency;
- source health;
- recent investigation activity;
- followed entities;
- significant knowledge changes;
- high-value knowledge gaps;
- merge and conflict decisions requiring review;
- source reliability summaries;
- belief-change trends;
- ingestion activity;
- graph activity;
- investigation status;
- knowledge-processing watermarks.

The Command Center should surface meaningful changes, not raw event volume. A shift in ownership confidence, a newly
opened contradiction, or a hypothesis becoming verified should appear; an internal cache refresh should not.

Its purpose is triage, orientation, and rapid entry into deeper work.

---

## 9. Entity Intelligence

Entity Intelligence is the canonical entity page and one of the most important product surfaces.

A user searching for a person, organization, address, account, or other entity lands here. The page must provide a
coherent answer to “What do we currently know about this entity?”

The header presents the canonical identity, aliases, entity ID, type, confidence, status, freshness, number of claims,
number of sources, open conflicts, knowledge gaps, and last significant change.

The main page contains:

- concise current-state summary;
- key attributes;
- relationships;
- ownership and control;
- directors or participants where applicable;
- events;
- major timeline changes;
- source coverage;
- open conflicts;
- active hypotheses;
- knowledge gaps;
- related investigations;
- evidence quality indicators;
- recent state changes.

The page supports tabs such as Overview, Relationships, Events, Claims, Evidence, Timeline, and Related Investigations.

Every meaningful relationship or assertion provides an explanation action. Selecting “Why?” opens a structured evidence
drawer showing supporting claims, source reliability, provenance, confidence contribution, counter-evidence, and
processing lineage.

The user should be able to start an investigation, watch the entity, export an evidence-linked summary, open the graph
around the entity, or compare current state to a historical state.

---

## 10. Investigation Workspace

The Investigation Workspace is the central professional working surface.

An investigation has a clear question, owner, collaborators, priority, status, time window, selected entities, and live
update state.

The workspace combines:

- investigation summary and central question;
- selected entities;
- active hypotheses;
- findings;
- analyst notes;
- bookmarks;
- evidence;
- conflicts;
- knowledge gaps;
- decisions;
- recent activity;
- review requests;
- next-best investigative actions.

The system continually updates the workspace when relevant knowledge changes. A new source that strengthens an active
hypothesis should be reflected automatically. A resolved identity split that changes the graph should propagate into the
investigation. The user should not need to manually rebuild the case after every new data arrival.

The workspace offers views such as Overview, Graph, Timeline, Evidence, Hypotheses, Decisions, and Report.

The product should preserve analyst reasoning without forcing the user into a heavyweight workflow system. Notes,
findings, reviews, and decisions exist in direct relation to knowledge objects.

---

## 11. Graph Explorer

Graph Explorer is a major analytical surface, but the graph is not the product's source of truth. It is a projection of
current or historical knowledge state.

The Graph Explorer supports three primary modes:

**Verified** shows only highly supported state.

**Likely** includes probable relationships and identity resolutions.

**Exploratory** includes weaker links, alternative identities, inferred relations, and open hypotheses.

The user can control time range, relation types, graph depth, minimum confidence, layout, perspective, and selected
entities.

The graph distinguishes organizations, people, locations, accounts, documents, sources, and other entity types visually.
It distinguishes observed, inferred, disputed, and exploratory edges. Confidence is visible without overwhelming the
canvas.

The graph must avoid becoming an unreadable hairball. It uses context-aware projection, bounded expansion,
interestingness ranking, neighborhood reduction, and perspective-specific filtering.

Key actions include:

- expand neighbors;
- explain an edge;
- compare paths;
- pin nodes;
- save a perspective;
- identify communities;
- inspect suspicious motifs;
- compare paths;
- add a path to an investigation;
- create a hypothesis from a pattern;
- watch a node or path;
- export a subgraph.

The user should be able to select a path and see exactly why it matters, which evidence supports it, what
counter-evidence exists, when the path became visible, and how its confidence evolved.

---

## 12. Evidence and Timeline

The Evidence and Timeline area makes the temporal and evidentiary model visible.

It distinguishes what happened from when the system learned it. The user can compare valid-time events and
knowledge-time changes on the same visual timeline.

The page supports views for Timeline, Claims, Evidence, Provenance, and Historical State.

The claims table includes statement, validity period, observed time, source, confidence, status, and derivation type.
Selecting a claim opens a provenance panel with source metadata, evidence excerpt, processing path, dependency chain,
and explanation of how the claim affected belief state.

Historical State Diff allows the user to compare two points in knowledge time and see exactly what changed: ownership,
addresses, directors, entity identity, conflicts, beliefs, and derived relations.

This page is the primary defense against black-box reasoning. The user should be able to inspect not merely a
conclusion, but the sequence by which the system arrived there.

---

## 13. Conflicts and Resolution

The Conflict Explorer is a dedicated working surface for contradictions.

The main table includes conflict ID, entity, type, severity, sources involved, temporal overlap, current belief, status,
and review action.

Selecting a conflict opens a side-by-side resolution panel that compares source A and source B, evidence excerpts,
source reliability, dates, temporal interpretation, and current confidence.

Resolution options include accepting one side, accepting both for different periods, keeping the conflict unresolved, or
recording a more complex decision.

The UI must make the consequences of a resolution explicit. Before applying a major decision, the product should show
the impact on affected entities, relations, beliefs, hypotheses, investigations, alerts, and reports where feasible.

Conflict resolution becomes training data and source-quality evidence, but the decision itself remains preserved as a
first-class object.

---

## 14. Hypotheses Workbench

The Hypotheses Workbench is the structured reasoning environment for propositions that are not yet accepted as current
knowledge.

The page displays active, verified, under-review, and disproven hypotheses, with confidence, supporting evidence count,
counter-evidence count, scope, owner, status, and last change.

Selecting a hypothesis displays:

- statement;
- confidence history;
- owner;
- related investigation;
- supporting evidence;
- counter-signals;
- critical assumptions;
- affected entities;
- competing hypotheses;
- impact surface;
- next-best tests.

The system should support both analyst-created and machine-suggested hypotheses. Machine-suggested hypotheses must
always remain clearly identified as such until accepted through the appropriate lifecycle.

The user may promote a hypothesis to verified, keep it under review, weaken it, reject it, create a knowledge gap from a
missing prerequisite, or add a formal decision note.

---

## 15. Knowledge Gaps

Knowledge Gaps provides a systematic view of missing information.

The Gap Explorer ranks gaps by impact, expected information gain, affected investigations, affected hypotheses,
acquisition effort, and priority.

Examples include:

- missing beneficial owner;
- unknown purpose of a transfer;
- unresolved service-provider relationship;
- lack of independent corroboration;
- missing current financial statements;
- unknown source provenance;
- insufficient temporal coverage.

Selecting a gap shows what is known, what is missing, why it matters, which hypotheses and investigations are blocked,
and which sources or actions may close it.

The system supports Gap Plans with states such as Open, In Progress, Waiting, Deferred, and Closed.

A mature deployment should calculate expected information gain and use it to rank recommended actions. This makes the
system proactive: it does not merely describe uncertainty; it helps reduce it efficiently.

---

## 16. Source Intelligence

Source Intelligence turns source quality into a visible product surface.

The Source Catalog tracks source category, coverage, reliability, freshness, update cadence, dependency risk, claim
volume, last ingest time, and status.

A selected source profile includes:

- overall reliability;
- field-level reliability;
- freshness and latency;
- ingestion activity;
- known strengths;
- known limitations;
- dependent entities;
- dependent investigations;
- source-to-source dependencies;
- historical conflict contribution;
- historical conflict resolution contribution;
- coverage by domain.

The system should learn reliability contextually. A source can be reliable for legal names but unreliable for ownership.
It can be accurate but stale. It can appear independent while actually syndicating another source.

The product must therefore distinguish source count from evidence independence.

---

## 17. Watchlists and Alerts

Watchlists make the knowledge state operational over time.

Users can watch entities, investigations, hypotheses, paths, conflicts, sources, or specific belief conditions.

Rules may trigger on events such as:

- confidence falling below a threshold;
- new ownership connection;
- identity split;
- new sanctions-related evidence;
- hypothesis promotion;
- conflict opening;
- independent corroboration arriving;
- new relationship to a watched cluster;
- source reliability deterioration;
- knowledge gap resolution.

Alerts should be meaningful state changes, not raw data notifications.

Each alert shows what changed, why the rule fired, old versus new state, affected investigations, evidence diff,
confidence change, and suggested response.

Users may acknowledge, escalate, snooze, open an investigation, change the rule, or mark the alert resolved.

The system must include noise control, deduplication, threshold hysteresis, and meaningful-change policies so that live
monitoring remains usable.

---

## 18. Reports and Briefings

Reports convert investigations and knowledge state into publishable outputs while preserving evidence traceability.

The reporting center supports executive briefs, entity summaries, due-diligence packs, investigation deep dives,
periodic digests, analytical reports, and export packages.

Each report contains versioned sections with evidence coverage indicators. Typical sections include:

- Executive Summary;
- Key Findings;
- Network Overview;
- Timeline;
- Supporting Evidence;
- Contradictions;
- Open Questions;
- Recommendations;
- Appendix.

The report builder links findings to evidence and claims. A reader should be able to trace an assertion back to its
supporting material.

Reports support review, comments, approval, publication, scheduling, recipients, templates, PDF, DOCX, slide-deck, and
structured-data exports as appropriate.

A report may snapshot the state used at publication time to ensure reproducibility even if the live knowledge state
later changes.

---

## 19. Search and Natural Language Investigation

Global search must operate on resolved knowledge rather than merely raw source records.

Users may search by name, identifier, address, source, event, claim text, relation, document, or semantic concept.

Natural-language queries should translate user intent into formal queries rather than bypassing the knowledge model.

Examples include:

- “Show everything connecting Alice to Company Z between 2023 and 2025.”
- “Only include relationships supported by at least two independent sources.”
- “What changed around this company in the six months before the acquisition?”
- “Which entities connect these two clusters?”
- “Why are these two records not merged?”
- “What evidence would most reduce uncertainty around ownership?”

The language layer must never invent supporting knowledge. It should cite or link to the structured objects used to
produce the answer.

---

## 20. Explanations: Why and Why Not

Explainability is part of the data model, not a cosmetic AI feature.

Every important belief, relation, entity resolution, hypothesis, conflict outcome, or alert should support an
explanation request.

A “Why?” explanation includes:

- supporting claims;
- evidence;
- source reliability;
- temporal validity;
- confidence contribution;
- derivation or inference method;
- counter-evidence;
- decision history;
- model version where relevant.

A “Why not?” explanation is equally important. The system should explain why two records remain separate, why a relation
is below threshold, why a hypothesis was not verified, or why a source was not treated as independent.

Structured explanations are authoritative. Natural-language summaries are generated from them, not used as substitutes
for them.

---

## 21. Product Behavior Under Continuous Updates

Knowledge State Engine is designed as a continuous system rather than a batch-generated knowledge graph.

New information should become durable immediately, visible almost immediately, locally convergent quickly, and globally
convergent asynchronously.

The system distinguishes:

**Knowledge Commit Latency:** time until new information is safely accepted and visible in direct state.

**Knowledge Convergence Latency:** time until all relevant derived consequences have been recomputed.

A typical simple update should behave approximately as follows in a mature optimized deployment:

- observation accepted within milliseconds to tens of milliseconds;
- claim visible within tens of milliseconds;
- direct entity and belief state updated within tens to low hundreds of milliseconds;
- local conflicts and relation effects updated within hundreds of milliseconds;
- investigation projections refreshed within sub-second to low-second range;
- broader hypotheses and graph analytics converge asynchronously;
- global analytics may take seconds or longer.

These are directional product goals rather than unconditional guarantees. Complexity class matters. A simple attribute
update is different from a merge affecting millions of relations.

The product should show convergence state where relevant rather than pretending every projection is instantaneously
final.

---

## 22. Hot, Warm, and Deep Processing

The processing model is divided conceptually into three latency tiers.

### Hot Path

The hot path contains bounded, predictable work:

- validation;
- idempotency;
- durable acceptance;
- claim creation;
- direct lookup;
- bounded candidate resolution;
- direct belief update;
- state-change publication.

Unbounded graph traversal, large historical scans, global analytics, and language-model calls do not belong in the hot
path.

### Warm Path

The warm path handles local derived intelligence:

- conflict detection;
- deeper identity scoring;
- local graph updates;
- temporal reconciliation;
- local anomaly detection;
- hypothesis re-evaluation;
- active investigation refresh;
- dependency invalidation.

### Deep Path

The deep path handles expensive global work:

- large community analysis;
- global embeddings;
- model training;
- global recalibration;
- large historical recomputation;
- broad pattern mining;
- deep hypothesis discovery.

Failure or delay in the deep path must not stop ingestion or direct knowledge availability.

---

## 23. Scalability Principles

Scale is achieved primarily through locality, incremental computation, bounded reasoning, and derived-state separation.

A new claim about one company should not cause the entire graph to be recomputed. The system should identify the
smallest affected dependency region and propagate change only where necessary.

Candidate generation must reduce identity comparisons before expensive scoring. Graph operations must use budgets.
Supernodes require special handling. Expensive global analytics must not sit on the write path.

The architecture should support very large eventual deployments, including billions of entities and much larger claim
and relation counts, without requiring the initial implementation to deploy distributed complexity prematurely.

The initial implementation should preserve abstraction boundaries that allow storage, messaging, search, graph, cache,
and model adapters to evolve independently when evidence justifies additional infrastructure.

---

## 24. Consistency Model

The authoritative core favors strong local correctness. Derived intelligence converges asynchronously.

Strong correctness is required for:

- accepted observation durability;
- claim identity;
- decision identity;
- canonical entity handles;
- merge and split decisions;
- provenance lineage;
- event idempotency.

Eventual convergence is acceptable for:

- graph projections;
- search indexes;
- community detection;
- hypothesis suggestions;
- knowledge-gap rankings;
- analytical metrics;
- global source statistics.

The product should prefer “strong core, eventual intelligence” over distributed transactions spanning the entire system.

---

## 25. Historical Reconstruction and Versioned Reality

The system must support historical reconstruction as a native capability.

Users and operators should be able to inspect:

- how an entity appeared at a past valid time;
- what the system believed at a past knowledge time;
- what changed between two knowledge states;
- which evidence caused a belief change;
- when an identity merge occurred;
- what a report or investigation saw at publication time.

This capability makes the product function conceptually like version control for knowledge. Changes are not merely
overwritten; they remain reconstructable and attributable.

---

## 26. Source Reliability and Evidence Independence

Evidence quantity cannot be treated as evidence quality.

Three sources may represent one underlying source syndicated three times. The system should model source dependency and
reduce artificial confidence amplification.

Source reliability should be learned from resolved outcomes where possible and remain domain-sensitive. Reliability may
be expressed by field or assertion type, jurisdiction, freshness, historical delay, and other contextual dimensions.

A source can therefore be:

- highly accurate but stale;
- fresh but weak;
- reliable for one attribute and poor for another;
- dependent on another source;
- historically inconsistent during a defined period.

These distinctions must be visible in the product.

---

## 27. Confidence Model

No single confidence number should carry every meaning.

The system distinguishes at least:

- identity confidence;
- claim confidence;
- belief confidence;
- source reliability;
- evidence strength;
- evidence independence;
- freshness;
- inference confidence;
- relevance to the current investigation.

The UI may summarize these when necessary, but internal semantics must remain distinct.

Confidence should be calibrated where models permit. A reported probability should eventually correspond to observed
outcome frequencies rather than functioning as a purely aesthetic score.

---

## 28. Entity Resolution

Entity resolution is continuous rather than one-time.

Candidate generation narrows the search space using exact identifiers, normalized names, aliases, addresses, temporal
context, relations, phonetic keys, vector similarity, graph context, and domain-specific signals.

Candidate scoring uses both positive and negative evidence. The absence of contradiction is not sufficient. Impossible
temporal overlap, incompatible identifiers, conflicting geography, or mutually exclusive constraints should reduce match
confidence.

Identity hypotheses remain revisable. Merges and splits must preserve prior observations and lineage. Large merges may
become logically effective before all physical projections are compacted.

The system should be able to answer both:

“Why did these records merge?”

and

“Why did these records remain separate?”

---

## 29. Hypothesis Generation and Reasoning

The mature product may generate hypotheses automatically from patterns, but generated hypotheses remain explicit objects
rather than silent graph edges.

A machine-generated hypothesis should contain:

- proposition;
- supporting signals;
- counter-signals;
- assumptions;
- confidence;
- missing evidence;
- generating algorithm or model;
- model version;
- affected entities;
- expected impact;
- recommended validation tests.

Hypothesis generation may use rules, graph analytics, statistical models, embeddings, or language models. The product
does not privilege one technique. It privileges traceability.

---

## 30. Next-Best Investigation

One of the product's defining advanced capabilities is the ability to recommend the next investigative action based on
expected information gain.

The engine should eventually estimate how much uncertainty a particular action could reduce and what downstream
knowledge it could affect.

Examples include:

- obtaining a beneficial ownership record;
- resolving an identity ambiguity;
- verifying an address;
- acquiring an independent source;
- inspecting a transaction cluster;
- retrieving a missing filing;
- interviewing a relevant person;
- validating a disputed source.

Recommendations should consider expected information gain, impact, cost, effort, availability, urgency, and active
investigation priority.

The objective is not to replace analyst judgment. It is to focus attention on the highest-value unresolved questions.

---

## 31. Live Investigations

Investigations are live materialized analytical contexts.

When relevant knowledge changes, the investigation updates automatically. The system calculates a meaningful diff rather
than simply announcing that “new data exists.”

A live update may say:

- a new independent source corroborated ownership;
- confidence increased from 0.62 to 0.78;
- a contradiction was opened;
- a merge changed three paths;
- a hypothesis crossed the Verified threshold;
- a gap was resolved;
- a watched source became stale.

This makes an investigation a continuously maintained analytical object rather than a static case folder.

---

## 32. Human-in-the-Loop Operation

Human decisions are essential where uncertainty cannot be resolved automatically or where policy requires review.

The product provides dedicated review experiences for:

- entity merge;
- entity split;
- conflict resolution;
- hypothesis promotion or rejection;
- gap deferral;
- source reliability override;
- report approval;
- major blast-radius operations.

Review interfaces should present the evidence required for a defensible decision. A merge review should show matching
attributes, contradictory attributes, timeline conflicts, shared relations, negative evidence, and model score.

A human decision becomes part of system history and may contribute to future model training, calibration, and source
assessment.

---

## 33. Collaboration

Collaboration is knowledge-centered rather than task-centered.

Users can comment on entities, claims, evidence, conflicts, hypotheses, gaps, findings, and reports. They can request
review, mention colleagues, assign specific knowledge tasks, and preserve decision rationale.

The product should not evolve into generic project-management software. Collaboration exists only where it helps
understand, validate, or communicate knowledge.

---

## 34. Security and Authorization

Knowledge fusion creates unusual security risks because inferred relationships can reveal information even when
underlying evidence is restricted.

Authorization must therefore operate below the page level and support controls on entities, attributes, claims,
evidence, relations, investigations, sources, and derived outputs where required.

Derived objects retain permission lineage. An inference built from restricted evidence cannot automatically become
unrestricted merely because the inference itself contains no explicit source text.

The product supports tenant and workspace boundaries, data classification, audit trails, encryption, retention rules,
and controlled deletion.

Security rules must not be implemented as ad hoc UI hiding. Authorization is enforced at the system boundary and
domain-access layer.

---

## 35. Deletion, Retention, and Auditability

The product must reconcile historical reproducibility with deletion requirements.

Where data must be physically removed, the system may retain non-sensitive tombstones or structural lineage where
legally permissible, while removing restricted payloads. Derived beliefs depending on deleted evidence must be
invalidated or recomputed.

Retention policies apply differently to raw evidence, observations, claims, projections, audit logs, reports, and
operational telemetry.

The authoritative history should be append-oriented, but append-oriented design is not an excuse to violate deletion
obligations.

---

## 36. Role of Graph Technology

Graph is a central representation and interaction model, but no particular graph database defines the product.

The logical model contains entities, relations, events, claims, hypotheses, conflicts, and provenance. Physical storage
may combine relational storage, event logs, graph indexes, search indexes, object storage, vector indexes, and
analytical stores.

Graph projections are reconstructable. If the graph store is deleted, authoritative knowledge remains intact and the
graph can be rebuilt.

This principle prevents storage technology from dictating domain semantics.

---

## 37. Role of Language Models and Machine Learning

Language models are useful for:

- natural-language query interpretation;
- evidence summarization;
- explanation rendering;
- semantic retrieval;
- hypothesis suggestion;
- report drafting;
- upstream relation extraction;
- investigation narrative generation.

Machine learning is useful for:

- entity resolution;
- link prediction;
- anomaly detection;
- source reliability estimation;
- interestingness ranking;
- semantic matching;
- information-gain prediction.

Neither category is a source of authority by itself.

Every model-derived output must be typed as derived or inferred, retain provenance, identify the producing model and
version, and remain separable from observed evidence.

A model failure must degrade intelligence, not corrupt the authoritative evidence record.

---

## 38. Operational Resilience

The system must degrade gracefully.

If global graph analytics fail, claims still enter.

If semantic search is unavailable, entity state remains readable through direct indexes.

If a language model is unavailable, evidence and beliefs remain valid.

If hypothesis generation is overloaded, it can lag while the hot path continues.

The system prioritizes:

1. durable information acceptance;
2. claim creation;
3. direct identity handling;
4. direct belief updates;
5. conflict detection;
6. active investigation refresh;
7. hypothesis generation;
8. deep global analytics.

Under load, intelligence may become temporarily less fresh. The system must not silently lose accepted knowledge.

---

## 39. Observability

Operational observability includes normal system telemetry and epistemic telemetry.

Technical telemetry includes latency, throughput, queue depth, resource use, retries, failures, saturation, and storage
health.

Epistemic telemetry includes:

- observations per second;
- claims per second;
- entity merges and splits;
- unresolved identity hypotheses;
- belief transitions;
- conflicts opened and resolved;
- hypotheses created, strengthened, verified, and disproven;
- knowledge gaps opened and resolved;
- stale sources;
- source disagreement;
- convergence lag;
- active investigation refresh lag;
- alert noise and acknowledgement rates.

The product should expose processing watermarks so operators can distinguish accepted knowledge from fully converged
derived state.

---

## 40. Product Quality Bar

The mature product must satisfy the following qualitative standard.

A professional user should be able to search for an unfamiliar entity and, within seconds, understand who or what it is,
its aliases, its important relations, recent changes, significant events, evidence quality, source coverage, major
contradictions, active hypotheses, missing information, and related investigations.

The user should be able to challenge the system. Every important conclusion should support inspection. The product must
not demand blind trust.

The user should be able to move naturally from overview to graph, from graph to evidence, from evidence to timeline,
from contradiction to decision, from hypothesis to missing information, and from missing information to next action.

The product should remain useful under incomplete data. It should become more valuable as sources accumulate, but it
should not require perfect normalization, complete coverage, or global certainty before providing insight.

The product must remain visually coherent despite high information density. It should prioritize hierarchy, context,
direct manipulation, and progressive disclosure over decorative dashboards.

---

## 41. Non-Goals

The following are explicitly not primary product goals:

- replacing every upstream ingestion platform;
- becoming a generic data warehouse;
- building a graph database from scratch;
- becoming a generic BI dashboard;
- becoming a generic workflow system;
- becoming a generic AI-agent framework;
- hiding uncertainty to produce simpler-looking answers;
- forcing all domains into a single static ontology;
- making all processing synchronous;
- adopting distributed infrastructure before scale requires it;
- optimizing for maximum feature count at the expense of epistemic clarity.

---

## 42. Development Philosophy

The implementation follows a strict incremental philosophy.

The final system is large, but daily work remains small.

Each capability should be introduced as the smallest coherent vertical slice that produces observable behavior. The
codebase is designed so that individual bounded contexts can be understood and tested in isolation. Domain behavior is
developed with TDD. Boundaries are modeled with DDD. Cross-module knowledge is minimized. Public contracts are explicit.

The engineering objective is not to build every final subsystem before the product works. The objective is to keep the
final destination visible while delivering functioning increments continuously.

A typical progression is:

- observation accepted;
- claim created;
- claim linked to evidence;
- identity candidate resolved;
- entity created;
- belief calculated;
- conflict detected;
- hypothesis managed;
- knowledge gap identified;
- investigation assembled;
- graph projected;
- UI composed;
- monitoring and reporting added;
- scalable adapters introduced only when justified.

At every stage the system should be executable, testable, and conceptually consistent with the final product.

---

## 43. Architectural Product Constraints

Although this document is product-centered, several architectural constraints are intrinsic to the product promise.

The system must preserve domain ownership. Claims, entities, beliefs, conflicts, hypotheses, knowledge gaps,
investigations, and reports are separate concepts with separate lifecycles.

The system must preserve provenance. Derived state must know what produced it.

The system must preserve history. Important state changes must remain reconstructable.

The system must preserve reversibility. Model and analyst errors must be correctable.

The system must preserve modularity. A module may know another module's contract but not its implementation.

The system must preserve product composition. End-user pages may combine many domains without forcing those domains to
depend directly on one another.

The system must preserve derived-state replaceability. Graph, search, caches, embeddings, and materialized views are not
the sole copy of authoritative knowledge.

---

## 44. Final Product State

At maturity, Knowledge State Engine operates as a continuous knowledge runtime and investigation environment.

A large organization may connect hundreds or thousands of information sources. New observations enter continuously.
Claims are created and revised. Identity is resolved. Entities merge and split. Relations and events evolve. Beliefs
strengthen and weaken. Conflicts open and close. Hypotheses are created and tested. Knowledge gaps are ranked.
Investigations update automatically. Alerts notify users only when something materially changes. Reports remain
evidence-linked and reproducible.

The user sees one coherent product rather than a collection of engines.

The system remains capable of showing the distinction between observation and belief, evidence and inference, identity
and record, event time and knowledge time, contradiction and error, confidence and truth, missing data and irrelevant
data.

The final product therefore provides something more valuable than a large connected dataset. It provides a disciplined,
revisable, inspectable model of understanding.

---

## 45. Governing Product Test

When evaluating a new feature, data structure, model, integration, or interface, the following questions should be
asked:

Does it help the system understand or explain an entity, event, relation, or investigation more accurately?

Does it preserve the distinction between evidence, claim, belief, and inference?

Does it preserve provenance and time?

Does it improve the user's ability to understand what changed and why?

Does it help reduce uncertainty or identify missing information?

Can its conclusions be challenged and inspected?

Can its derived state be revised or rebuilt?

Does it strengthen the final end-user experience rather than merely adding technical machinery?

If the answer is no, the feature should not become central to the product.

---

## 46. Final Statement

Knowledge State Engine is a continuously revisable model of reality built from heterogeneous evidence and exposed
through a professional investigation product.

Its purpose is not to collect the most data, draw the largest graph, or generate the most automated conclusions. Its
purpose is to turn fragmented, incomplete, changing, and sometimes contradictory information into a coherent analytical
state that remains explainable at every level.

The system should always be able to distinguish:

what was observed;

what was asserted;

what was resolved;

what is currently believed;

what conflicts with that belief;

what remains hypothetical;

what is missing;

what changed;

why it changed;

and what should be investigated next.

That is the product.
