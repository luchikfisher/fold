# Knowledge State Engine
## Product and Engineering Vision

### Status of this document

This document is the governing product and engineering vision for the Knowledge State Engine. It defines the system that is being built, the end-state product experience, the conceptual model of knowledge, the operational characteristics of the platform, and the design principles that constrain implementation. It is intentionally written as a durable statement of intent rather than as a record of design discussions. When implementation choices are ambiguous, this document has priority unless a later architecture decision record explicitly supersedes a specific section.

The system is not defined by a particular database, framework, graph technology, cloud provider, or machine-learning model. Those are implementation details. The system is defined by the behavior it guarantees to users and by the information model that makes those guarantees possible.

---

# 1. Product Definition

The Knowledge State Engine is a continuously updated system for constructing, maintaining, interrogating, and explaining a coherent model of reality from heterogeneous evidence.

The input to the system is normalized semantic information. The system does not require the upstream information to originate from any particular physical representation. It may have been derived from relational tables, JSON payloads, document extraction, streams, APIs, registries, sensor feeds, logs, human analysis, or other sources. The ingestion and schema-normalization layers may evolve independently. The Knowledge State Engine begins at the point where the system can receive observations, candidate entities, candidate relations, candidate events, evidence references, and source metadata in a stable semantic envelope.

From those inputs, the system constructs and continuously revises a world model composed of entities, events, relations, claims, evidence, beliefs, conflicts, hypotheses, knowledge gaps, decisions, investigations, temporal history, provenance, and source reliability. It exposes that model through an investigation-oriented product rather than through raw datasets.

The system does not claim to store an unquestionable source of truth. It stores the evolving case for what is true. Every meaningful conclusion remains traceable to evidence, situated in time, revisable when new information arrives, and explicit about uncertainty when certainty is not justified.

The defining product promise is:

> **Understand any entity or question across all available data, including what is known, what is uncertain, how the evidence connects, what changed, why the system currently believes what it believes, and what is most valuable to investigate next.**

The defining engineering promise is:

> **Durable immediately, visible almost immediately, locally convergent very quickly, globally convergent asynchronously, and historically reconstructable.**

---

# 2. The Product Is an Investigation System, Not a Graph Viewer

The graph is a foundational representation of connected knowledge, but it is not the product boundary. The end product is an environment in which a person can begin with a name, identifier, event, organization, address, document, account, question, or existing investigation and progressively build a defensible understanding of the relevant world.

A user does not enter the product in order to “open a graph.” A user enters in order to understand something.

The product must therefore support a complete loop of value. Information arrives, is resolved and incorporated into the knowledge state, becomes visible through a coherent entity or investigation context, is explored and challenged by the user, produces findings or decisions, records those decisions as part of system history, and continues to evolve as new evidence arrives.

The central user actions are not database operations. They are:

- find an entity or subject;
- understand what the system currently knows about it;
- inspect why the system believes specific claims or relations;
- see contradictions and uncertainty rather than having them silently flattened;
- compare competing interpretations;
- navigate relationships and events;
- reconstruct what was known at an earlier point in time;
- identify missing information;
- evaluate hypotheses;
- decide what to investigate next;
- monitor meaningful future change;
- record human decisions and findings;
- publish a traceable report whose statements remain linked to underlying evidence.

The graph, timeline, evidence explorer, conflict workspace, hypothesis workbench, watchlists, and reports are all different projections of the same underlying knowledge state.

---

# 3. Core Product Philosophy

## 3.1 Knowledge is represented as claims, not flattened facts

The atomic epistemic unit is the claim.

A relation such as:

`Alice -> CEO_OF -> Acme`

is not stored conceptually as an unquestionable fact. The system records that a source, evidence item, analytical process, or human actor asserted that Alice held the role of CEO of Acme under a particular temporal interpretation and with a particular provenance.

A claim minimally identifies a subject, predicate, object or scalar value, source, evidence, observed time, valid time, origin, status, and relevant confidence metadata.

This distinction is fundamental because sources disagree, sources become stale, multiple statements may be valid in different periods, a source may be wrong, an inference may later be rejected, and the meaning of an assertion can depend on context. The system preserves these distinctions instead of collapsing them prematurely.

## 3.2 Entities are resolved identities, not merged records

An entity is not a row selected as the “golden record.” It is the system’s current identity interpretation of observations that appear to refer to the same real-world object.

Entity resolution is therefore probabilistic, reversible, and historically recorded. The system supports candidate identities, competing identity hypotheses, positive matching signals, negative evidence, merge proposals, split proposals, human decisions, and later revision.

A merge is never destructive. A split is always possible because the observations, claims, evidence, and decision history that produced the earlier identity remain available.

## 3.3 Uncertainty is first-class state

The system does not force binary certainty where the evidence does not justify it. Confidence is not a cosmetic score added after the fact; uncertainty is represented in the information model and surfaced in the product.

The product distinguishes, as appropriate, states such as observed, supported, corroborated, likely, verified, inferred, disputed, stale, unresolved, rejected, and superseded. Exact terminology may vary by object type, but the distinction between evidence, interpretation, and certainty is mandatory.

## 3.4 Contradiction is information

Conflicting sources are not merely a data-quality nuisance to be cleaned away. Contradictions can reveal stale registries, overlapping periods, ambiguous identities, hidden structural changes, unreliable sources, or materially different interpretations.

A conflict is therefore a first-class domain object. It has participants, type, severity, temporal scope, supporting evidence, possible interpretations, resolution state, and decision history. Conflicts can be searched, ranked, monitored, assigned, resolved, reopened, and analyzed statistically.

## 3.5 Missing knowledge is represented explicitly

The system is expected to know what it does not know.

A knowledge gap is a first-class object that identifies missing information whose acquisition would materially improve an entity, belief, hypothesis, investigation, or decision. A gap may indicate an unknown beneficial owner, a missing purpose for a transfer, insufficient independent corroboration, an unresolved identity, a missing time interval, a weak source dependency, or any other absence that is operationally meaningful.

Knowledge gaps are ranked by impact, expected information gain, effort, urgency, and the number of downstream objects they block or destabilize.

## 3.6 The system distinguishes the observed world from the inferred world

Observed information and machine-generated inference are never silently mixed.

The user can conceptually move among at least three projections:

- an observed view showing what sources explicitly asserted;
- a believed-world view showing the system’s current best supported interpretation;
- an exploratory view that also includes weaker hypotheses, inferred relations, alternative identity interpretations, and unresolved possibilities.

AI output, graph inference, statistical models, and link prediction may enrich the exploratory and belief layers, but they cannot masquerade as directly observed evidence.

## 3.7 Time is part of meaning

The system is bitemporal.

It distinguishes when something was true in the world from when the system learned, believed, revised, or stopped believing it. This enables two fundamentally different questions:

“What was true on 15 March?”

and

“What did the system believe on 15 March?”

Historical reconstruction, late-arriving information, retroactive corrections, temporal overlap, stale data, and revisions are native behavior, not later extensions.

## 3.8 Every conclusion is explainable by lineage

Every material derived object must be able to answer: “Why?”

A belief can identify the claims, sources, evidence, weighting policy, freshness effects, contradictory signals, model version, and human decisions that produced it. A relation can identify the claims and events that justify it. A hypothesis can identify supporting evidence, counter-evidence, assumptions, and missing tests. A report statement can navigate back to the evidence chain that supports it.

Explanations are structured data first. Natural-language explanations may be generated from that structure, but prose is never a substitute for provenance.

---

# 4. Conceptual Knowledge Model

The durable product model is composed of the following concepts.

## 4.1 Source

A source represents the origin or provider of information. A source has identity, category, jurisdiction, coverage, update cadence, reliability characteristics, freshness characteristics, known dependencies, operational health, and historical performance.

Reliability is contextual rather than universally scalar. A source can be excellent for corporate addresses, mediocre for directors, and systematically delayed for ownership changes. The system learns source performance from resolved outcomes over time.

## 4.2 Evidence

Evidence is the addressable material on which a claim rests. It may reference a row, field, document passage, transaction record, API response, image region, log entry, file, registry extract, human note, or another concrete artifact.

Evidence is retained or snapshotted sufficiently to support later explanation and historical reconstruction. It has provenance and a lifecycle independent from the claims that interpret it.

## 4.3 Observation

An observation records what the system received before the system has fully interpreted it as knowledge about the world. It separates source output from system interpretation.

Observations have stable identities, arrival times, observed times, fingerprints, source references, evidence references, semantic payloads, and deduplication state. Duplicate delivery is expected and handled idempotently.

## 4.4 Claim

A claim is an assertion about a subject. It has a subject, predicate, object or value, temporal validity, evidence, source, origin, status, confidence metadata, and lifecycle.

Claims are append-oriented. Supersession and invalidation are recorded as explicit state transitions rather than silent overwrites.

## 4.5 Identity Candidate and Identity Hypothesis

Identity candidates are possible real-world identities derived from observations. Identity hypotheses represent propositions that multiple observations or candidates refer to the same object.

The system records positive signals, negative evidence, model scores, alternative hypotheses, decisions, and revisions. Identity resolution is contextual and can operate at different certainty thresholds when the use case requires it.

## 4.6 Entity

An entity is the current canonical representation of a resolved real-world object. It has a stable internal handle, type, aliases, attributes, identity history, lifecycle, and relationships to other knowledge objects.

Canonicalization never erases the underlying source representations from which the entity was constructed.

## 4.7 Relation

A relation represents a meaningful binary connection between two entities or comparable endpoints. A relation has type, direction, temporal validity, status, confidence, and explanatory support.

Relations are knowledge objects. Graph edges are projections of relations rather than the canonical ownership of relation semantics.

## 4.8 Event

An event represents something that happened. Events are first-class and can be n-ary, with multiple participants playing explicit roles. Transactions, appointments, acquisitions, meetings, filings, resignations, registrations, visits, shipments, and communications are naturally modeled as events when an ordinary binary edge would lose semantics.

An event can subsequently produce several graph relations without being reduced to those relations.

## 4.9 Provenance and Dependency

Provenance describes how an object came to exist. Dependencies identify which upstream objects, decisions, algorithms, model versions, and processing steps contributed to a derived state.

The dependency graph is also operationally important: when upstream evidence changes, the system uses dependencies to invalidate and recompute the smallest possible downstream region rather than rebuilding the entire world model.

## 4.10 Belief

A belief is the system’s current conclusion about an assertion or state of reality. It is derived from claims, evidence strength, source reliability, independence, freshness, temporal consistency, contradictory information, algorithmic inference, and human decisions.

Beliefs are versioned and historically reconstructable. A belief changing from 0.91 to 0.62 is itself a meaningful state transition that can affect conflicts, hypotheses, investigations, watch rules, and user-facing change feeds.

Confidence is multidimensional. Truth confidence, identity confidence, source reliability, freshness, evidence strength, inference confidence, and current relevance are not collapsed blindly into one number.

## 4.11 Conflict

A conflict captures claims or beliefs that cannot simultaneously hold under the same interpretation or temporal constraints. It records the conflicting objects, type, severity, source context, temporal overlap, candidate explanations, current resolution, and decision history.

## 4.12 Hypothesis

A hypothesis is a structured proposition that may explain a pattern, relationship, control path, identity, event, or other aspect of the world model. It contains a statement, supporting signals, counter-signals, assumptions, confidence, affected entities, competing hypotheses, unresolved questions, and lifecycle.

Hypotheses may be proposed manually or by the system. Machine-generated hypotheses remain explicitly identified as such. Hypotheses progress through states such as proposed, active, strengthened, weakened, verified, disproven, superseded, or archived.

## 4.13 Knowledge Gap

A knowledge gap identifies missing information that materially constrains understanding. It has a target, category, impact, information-gain estimate, affected hypotheses or investigations, acquisition suggestions, effort estimate, owner, and lifecycle.

## 4.14 Decision

A decision records an explicit human or authorized system choice. Merge approval, split approval, conflict resolution, hypothesis promotion, manual override, source quarantine, and other consequential actions are recorded as decisions with actor, options, context, rationale, outcome, time, and revision history.

A decision module records the decision; the domain owner executes its consequences. A merge decision does not directly mutate an entity from outside the entity domain.

## 4.15 Investigation

An investigation is a durable workspace centered on a question. It references entities, hypotheses, conflicts, evidence, findings, notes, knowledge gaps, timelines, graph perspectives, decisions, collaborators, and reports.

An investigation does not duplicate or own the underlying knowledge. It organizes references to knowledge objects and maintains the human analytical context around them.

---

# 5. End-State Product Experience

The final product is a coherent desktop investigation environment. Every major screen must feel like a projection of one live knowledge system rather than a set of disconnected administrative pages. Navigation, terminology, confidence semantics, temporal behavior, status colors, evidence access, and explanatory interactions must remain consistent across the product.

The visual standard is a dense but calm enterprise interface. The product is information-rich without becoming visually chaotic. Data density is high, but hierarchy is strong. Tables, cards, side drawers, timeline tracks, graphs, sparklines, badges, confidence bars, and activity feeds are used only when they provide meaningful analytical value.

The end state includes the following primary product surfaces.

## 5.1 Command Center

The Command Center is the operational home page. It is not a generic business-intelligence dashboard. It is an intelligence inbox summarizing what changed in the knowledge system and what deserves attention.

The page presents active investigations, watched entities, open conflicts, newly received evidence, belief changes, strengthened hypotheses, convergence latency, and source health. It includes recent investigation activity, significant knowledge changes, entities followed by the user, high-value knowledge gaps, pending merge and conflict decisions, source reliability summaries, ingest and graph activity, and watermarks indicating how current each derived layer is.

The user must be able to open any important change directly into its entity, claim, conflict, evidence chain, hypothesis, or investigation context.

## 5.2 Entity Intelligence

Entity Intelligence is the canonical entry point for understanding a specific entity.

The page presents the canonical identity, aliases, entity type, identity confidence, current status, freshness, claim count, source count, conflict count, knowledge-gap count, and last significant change. The overview summarizes the system’s current belief about the entity without presenting inference as raw fact.

Dedicated sections expose key attributes, relationships, events, claims, evidence, timeline, conflicts, hypotheses, knowledge gaps, source coverage, related investigations, and change history.

Every meaningful assertion supports a “Why?” interaction. Selecting a relationship opens an explanation drawer containing supporting claims, evidence, source reliability, temporal context, confidence contributors, counter-evidence, derivation path, and relevant model or decision history.

The page supports immediate actions such as watching the entity, starting an investigation, adding it to an existing investigation, opening the graph around it, exporting a traceable view, or reviewing unresolved identity questions.

## 5.3 Investigation Workspace

The Investigation Workspace is the primary human analytical environment.

An investigation has a clear question, scope, owner, collaborators, priority, classification, followed entities, time window, hypotheses, pinned findings, notes, bookmarks, unresolved gaps, review tasks, recent activity, and next-best investigative actions.

The workspace is live. When knowledge changes in a way that materially affects the investigation, the investigation receives an incremental update rather than requiring the user to rerun the entire analysis.

The workspace provides coordinated views for overview, graph, timeline, evidence, hypotheses, decisions, and report generation. It is not a generic project-management board. Collaboration is attached to knowledge objects and analytical decisions.

## 5.4 Graph Explorer

The Graph Explorer provides an interactive projection of the knowledge state for connected analysis.

It supports verified, likely, and exploratory modes; confidence thresholds; temporal windows; relation filtering; depth limits; saved perspectives; community overlays; path analysis; motif detection; suspicious patterns; and bounded neighbor expansion.

The graph is never allowed to degrade into an undifferentiated hairball. The system ranks relevance and interestingness, treats supernodes explicitly, and favors contextual graph construction over rendering every known edge.

Selecting a path or edge opens a structured explanation showing why the path matters, which evidence supports it, counter-evidence, confidence, first-seen time, and available actions. Users can pin paths, compare alternatives, add subgraphs to investigations, create hypotheses, or watch significant connections.

## 5.5 Evidence & Timeline

The Evidence & Timeline surface distinguishes what happened from when the system learned it.

A dual temporal visualization displays valid-time events and belief-time changes. Users can filter by entity, source, claim type, confidence, status, and time window. Claims are shown chronologically with their valid periods, observed times, sources, confidence, state, and origin type.

Selecting a claim exposes source metadata, evidence excerpts, retrieval timestamps, processing path, dependency chain, and the reason the claim changed the belief state.

Historical State Diff allows a user to compare two knowledge snapshots and see which attributes, relations, conflicts, identities, or beliefs changed, when the system learned about the change, and what caused it.

## 5.6 Conflicts

The Conflict Workspace is a dedicated environment for contradiction management.

The product lists open conflicts by entity, type, severity, source set, temporal overlap, current belief, status, and review requirement. The selected conflict view compares sources and evidence side by side, shows confidence and reliability, explains temporal interpretations, identifies downstream impact, and provides explicit resolution options.

Resolutions are recorded as decisions. The original conflict and evidence remain historically visible.

## 5.7 Hypotheses Workbench

The Hypotheses Workbench is where explanatory propositions are formed, tested, compared, strengthened, weakened, verified, or disproven.

The product displays active hypotheses with scope, confidence, supporting evidence count, counter-evidence count, status, owner, and recent change. A selected hypothesis exposes the full argument: supporting evidence, counterpoints, critical assumptions, competing hypotheses, confidence history, affected entities, impact surface, and recommended next tests.

Hypothesis promotion is not a cosmetic status change. It is a decision whose consequences are traceable to evidence and which may affect investigations, alerts, reports, and knowledge gaps.

## 5.8 Knowledge Gaps

The Knowledge Gaps product surface makes missing information actionable.

Gaps are ranked by impact and expected information gain. The user can see the entity or scope affected, category, blocked hypotheses, affected investigations, owner, status, acquisition options, expected effort, and next step.

Selected gaps explain what is already known, exactly what remains unknown, why the missing information matters, which downstream objects are blocked, and which sources or acquisition channels are most likely to close the gap.

The system supports gap plans, ranked next-best actions, acquisition-channel analytics, recently resolved gaps, burn-down trends, and impact analysis.

## 5.9 Source Intelligence

Source Intelligence makes the information supply chain observable and analytically meaningful.

Each source has coverage, reliability, freshness, update cadence, dependency level, claim volume, ingest health, field-level quality, known limitations, downstream dependency counts, and historical conflict contribution.

The product includes a source catalog, field reliability views, source agreement matrices, coverage by domain, ingestion activity, latency and freshness trends, conflict contribution, top impacted entities, and source dependency networks.

Source reliability is learned and contextual, never treated as a static universal score.

## 5.10 Watchlist & Alerts

Watchlists express what users want the system to monitor. Alerts express that a meaningful condition occurred.

A watch rule can target entities, investigations, hypotheses, belief thresholds, conflicts, source states, or relationship patterns. Alerts are deduplicated, severity-ranked, explainable, and tied directly to the change that triggered them.

A selected alert shows the old and new states, the exact rule condition, evidence differences, affected investigations and entities, recommended response, and alert history. Users can acknowledge, escalate, snooze, open an investigation, or change the rule.

Notifications favor semantic change over raw event volume. A confidence shift from 0.731 to 0.734 is normally noise. Crossing a meaningful threshold or receiving independent corroboration can be significant.

## 5.11 Reports & Briefings

Reports convert investigations and entity intelligence into evidence-linked outputs.

Reports are versioned. They may contain executive summaries, key findings, network overviews, timelines, supporting evidence, contradictions, unresolved questions, recommendations, and appendices. Every material statement can remain linked to the evidence and knowledge objects that support it.

The reporting product includes report templates, evidence coverage, reviewer comments, scheduled distribution, export formats, recent exports, section-level traceability, and publication history.

A report does not become an isolated static document. It is a snapshot of a specific knowledge state with traceable lineage.

## 5.12 Settings and Administration

Settings configure workspace behavior, graph defaults, confidence display preferences, notifications, investigation defaults, export policies, security, integrations, and team behavior.

Administration provides privileged operational controls for replay, recomputation, quarantine, snapshots, processor state, and diagnostics without bypassing domain boundaries.

---

# 6. Product Interaction Principles

The product consistently favors understandable analytical interactions over infrastructure vocabulary.

Users see entities, claims, evidence, conflicts, hypotheses, gaps, findings, investigations, and decisions. They should not need to understand which database contains the object or which stream processor generated it.

The following interaction principles are mandatory.

First, every displayed confidence must have interpretable provenance. Confidence is not decoration.

Second, evidence and source information must be no more than a small number of interactions away from any meaningful conclusion.

Third, observed, inferred, disputed, and exploratory information must be visually distinguishable.

Fourth, time must be explicit when historical ambiguity matters.

Fifth, all destructive-looking knowledge operations such as merge, split, override, and conflict resolution are decisions with history, not silent state mutations.

Sixth, the product must make state freshness visible. A user must be able to distinguish committed knowledge from derived layers that are still converging.

Seventh, information density is acceptable; ambiguity is not. Dense screens must use hierarchy, grouping, consistent terminology, and progressive disclosure rather than reducing the amount of useful information.

---

# 7. Real-Time Knowledge Runtime

The system is designed for continuous knowledge processing rather than periodic graph construction.

A new observation enters a durable processing path, becomes queryable as a claim, affects local entity resolution and beliefs, updates relevant graph and search projections, and propagates to conflicts, hypotheses, gaps, investigations, and alerts according to dependency and priority.

The system explicitly separates commit latency from convergence latency.

Knowledge Commit Latency measures how quickly authoritative information becomes durable and visible.

Knowledge Convergence Latency measures how long it takes for the relevant derived consequences to become current.

This distinction prevents expensive global inference from blocking the arrival of knowledge.

The runtime is divided conceptually into three processing classes.

The hot path handles validation, idempotency, authoritative persistence, claim creation, direct identity lookup, bounded local resolution, direct belief updates, and publication of state-change events. Its runtime must be bounded and predictable. Unbounded graph traversal, large historical scans, LLM calls, and global analytics are prohibited from the hot path.

The warm path handles conflict detection, richer local entity resolution, temporal reconciliation, neighborhood recomputation, investigation refresh, local anomaly detection, hypothesis adjustment, and dependency invalidation.

The deep path handles global communities, broad motif discovery, model training, large embedding refreshes, global recalibration, historical reprocessing, and expensive analytical searches.

Under load, the system degrades intelligence before durability. Evidence, observations, claims, and authoritative state continue to be accepted even if deeper analytical layers fall behind.

---

# 8. Performance and Scalability Objectives

Performance is defined by user-visible semantic latency rather than database write latency alone.

The mature platform is designed toward the following class of behavior for ordinary local updates under healthy operating conditions. Exact numerical SLOs may evolve after measurement, but the architecture must preserve the possibility of these targets.

Ingest acknowledgement is targeted in the tens-of-milliseconds range at high percentiles. Claim visibility is targeted below roughly 50 milliseconds for simple cases. Direct local knowledge updates are targeted around or below 100 milliseconds. Simple conflicts and investigation-relevant local effects should normally converge within hundreds of milliseconds. Broader local graph consequences may converge within sub-second to low-single-second ranges. Global analytical freshness may operate on longer intervals when appropriate.

These are percentile objectives, not averages. p95, p99, and where relevant p99.9 behavior matter more than a misleadingly low mean.

The system scales primarily through locality and incrementality. A new claim about one company should not require reconsidering an unrelated billion-node world. Processing identifies the smallest affected subgraph and dependency region.

Global algorithms are isolated from the operational hot path. Candidate generation prevents entity resolution from becoming an O(N) comparison problem. Materialized read models prevent every UI query from reconstructing state from raw history. Hot, warm, and cold epistemic tiers allow expensive projections and caches to follow user attention and analytical importance rather than treating every entity identically.

The mature architecture must be capable of evolving toward billions of entities, tens or hundreds of billions of claims and relations, high sustained update rates, and large concurrent read workloads without changing the conceptual product model.

Scale is not a reason to introduce distributed complexity before measurement demonstrates the need. Stable ports and contracts are designed early; distributed adapters are introduced when actual workload justifies them.

---

# 9. Incremental Computation and Dependency Propagation

The system never recomputes the world when it can recompute the consequence.

Every derived state that materially depends on upstream knowledge records its dependency lineage. When a claim, source score, identity interpretation, relation, or decision changes, the system follows dependency edges to mark the smallest set of affected derived objects as dirty.

Recomputation propagates only when the upstream change materially changes the downstream result. Semantic thresholds prevent meaningless confidence jitter from cascading through the system.

Derived objects expose convergence state such as current, dirty, recomputing, partially converged, stale, or failed where operationally useful.

Large identity merges and splits use logical indirection before physical rewriting. A canonical identity decision becomes visible quickly, while expensive physical compaction or projection rebuilding can proceed asynchronously.

---

# 10. Storage and Representation Principles

No storage engine defines the product model.

The system may use multiple physical stores over its lifetime, but the logical concepts remain stable. A claim is not a PostgreSQL row. An entity is not a graph node. An event is not a Kafka message. A belief is not a cache entry.

Authoritative state is append-oriented and versioned where necessary. Derived read models are rebuildable.

The likely mature storage topology includes a transactional authoritative store for bounded-context data, durable event or outbox infrastructure, an object or document store for evidence payloads, a search index, a graph-oriented projection, caches, and analytical storage. The project remains operationally centralized until workload justifies physical separation.

The graph is a derived projection. Search is a derived projection. Embeddings are derived artifacts. Losing a derived projection must not mean losing knowledge.

Cross-domain ownership remains strict even when domains share a physical database. One bounded context never directly mutates another bounded context’s schema.

---

# 11. Modular Monolith and Domain Boundaries

The codebase is a modular monolith: one repository, one primary deployable, one coherent product, and many bounded contexts with enforced boundaries.

Operational centralization is deliberate. It reduces deployment overhead, distributed failure modes, network complexity, local development friction, and premature infrastructure cost.

Logical independence is equally deliberate. A bounded context owns its domain model, persistence, application behavior, tests, contracts, and lifecycle. Other contexts depend only on explicit API contracts and integration events.

The shared kernel is intentionally small and limited to universally meaningful primitives such as identifiers, time, event envelopes, result types, pagination, and similar neutral concepts. Business behavior is never moved into a generic “common” package for convenience.

The default cross-module communication mechanism is an event describing something that already happened. Read-only queries are allowed through explicit API contracts when synchronous information is genuinely required. Cross-module commands are exceptional and require a clear domain reason.

Compile-time circular dependencies are prohibited. Causal cycles through events are permitted when semantically correct and must carry correlation and causation metadata to prevent uncontrolled feedback loops.

Composition is a separate product-facing read layer. It may query multiple bounded contexts to assemble screens such as Entity Intelligence or the Command Center. Composition does not own business truth and does not contain domain policy.

---

# 12. Domain-Driven Design Standard

DDD is used to protect domain meaning, not to maximize pattern count.

The model uses aggregates where genuine transactional consistency boundaries exist. Aggregates remain small. No entity aggregate is allowed to contain thousands of claims, relations, conflicts, or other independently evolving objects.

Value objects encode domain invariants where they improve correctness and readability. Domain services are used only when behavior genuinely belongs to no single aggregate. Repositories abstract ownership boundaries, not every database call. Factories, specifications, and policies are introduced when they express real domain semantics.

Names are taken from the ubiquitous language of the product. Names such as `KnowledgeManager`, `DataHelper`, `GraphProcessor`, and `CommonService` are avoided when a precise domain term exists.

The domain model remains independent from Spring, HTTP, serialization, persistence technology, graph technology, and AI providers.

---

# 13. Test-Driven Development Standard

TDD is the default implementation method for domain and application behavior.

Development proceeds in small vertical capabilities rather than large horizontal subsystem projects. A change begins with a behavior expressed as a failing test, proceeds to the smallest implementation that satisfies that behavior, and is then refactored while preserving architectural boundaries.

The majority of tests are fast domain tests without framework bootstrapping, databases, or network calls. Application tests use fake ports. Contract tests protect module boundaries and event compatibility. Infrastructure tests verify persistence, serialization, messaging, and adapters. End-to-end tests are fewer and focus on high-value cross-module flows.

Architecture is executable. Automated architecture tests enforce the absence of cross-module domain imports, infrastructure leakage, framework dependencies in domain code, cross-domain ORM relations, and other violations of the dependency constitution.

A feature is not complete merely because it works functionally. It is complete when the behavior is covered, contracts are explicit, observability is adequate, architectural rules still pass, and the change remains understandable in isolation.

---

# 14. Development Method: Kaizen by Vertical Slice

The final product is large. The daily unit of work is intentionally small.

The complete architecture exists as a map, but implementation proceeds through narrow vertical slices that remain deployable at every stage.

A first observation flow may do nothing more than accept a normalized observation, deduplicate it, persist it, emit an event, create one claim, and make that claim queryable. That slice is valuable even before advanced entity resolution exists.

The next slice may add a simple rule-based identity hypothesis. The next may establish a canonical entity. Later slices add beliefs, conflicts, hypotheses, gaps, investigation composition, graph projections, watch rules, and reporting.

Modules are never treated as “finished once and for all.” Each reaches a minimal coherent version, enables the next product capability, and is revisited as the surrounding system matures.

The project always remains runnable. There is no multi-month period during which major subsystems exist only as disconnected scaffolding.

---

# 15. Entity Resolution Standard

Entity resolution is both a product capability and a foundational domain process.

Candidate generation must be sublinear with respect to the entire entity population. The system uses blocking and indexing strategies based on identifiers, normalized names, addresses, geography, dates, embeddings, relationships, and domain-specific keys to produce a bounded candidate set.

Candidate scoring considers positive and negative evidence. Identical names are not enough. Temporal impossibility, incompatible identifiers, mutually exclusive attributes, and contradictory graph context are first-class negative signals.

Resolution produces a probability or calibrated score rather than a blind Boolean. Alternative identity hypotheses can coexist when ambiguity remains material.

Human review is supported for consequential merges and splits. Every human decision becomes durable training and calibration material for later models.

Entity resolution implementations can evolve from deterministic and rule-based methods to statistical or machine-learning models without changing the domain contract.

---

# 16. Belief and Confidence Standard

Belief calculation is explicit, versioned, explainable, and calibrated.

A belief can consider source reliability, source independence, evidence strength, claim confidence, freshness, temporal consistency, contradictory claims, human decisions, domain constraints, and model outputs.

The calculation is incremental. A new claim does not force a full historical scan when sufficient accumulator state exists.

Confidence must be calibrated over time where statistical interpretation is claimed. A class of outputs labeled 0.8 should, under a meaningful validation regime, behave like approximately 80% correctness for that defined population and task.

Confidence decay is domain-sensitive. Birth dates and immutable registration identifiers behave differently from addresses, executives, ownership, balances, or real-time statuses.

---

# 17. Hypothesis and Reasoning Standard

Reasoning is bounded, explicit, and provenance-aware.

A machine-generated hypothesis contains the proposition, supporting evidence, counter-evidence, assumptions, confidence, reasoning or model version, unresolved questions, and expected information required for validation.

The system does not treat graph proximity as causality. Association, correlation, sequence, common ownership, influence, and causation are separate semantic concepts.

Reasoning tasks have explicit computational budgets. Traversal depth, nodes visited, edges considered, time budget, confidence floor, and hypothesis count are bounded. Tasks that exceed interactive budgets move to deeper asynchronous processing rather than blocking the user or destabilizing the hot path.

---

# 18. AI and Machine Learning

AI is an implementation capability, not an authority layer.

Large language models may assist natural-language query interpretation, evidence summarization, explanation rendering, hypothesis suggestion, report drafting, source extraction, or analyst interaction. Statistical models and graph machine learning may assist identity resolution, anomaly detection, ranking, source reliability, link prediction, or hypothesis generation.

Every model output that can affect knowledge state is versioned, classified by origin, and kept distinct from direct observation. Model outputs have provenance, confidence, and dependency links.

A model can be replaced, shadowed, canaried, quarantined, replayed, and compared against previous versions. Model rollout must support semantic rollback: reverting a model means repairing the derived state it created, not merely redeploying an older binary.

The system remains functionally useful when AI subsystems are unavailable. Core evidence, observation, claim, identity history, and authoritative state do not depend on an LLM being online.

---

# 19. Search and Natural-Language Interaction

Global search resolves user intent toward knowledge objects rather than returning raw source rows whenever possible.

The search experience supports exact identifiers, aliases, attributes, full text, semantic similarity, temporal constraints, graph context, claims, evidence, investigations, conflicts, hypotheses, and gaps.

Natural-language questions are translated into formal operations. A user may ask to show the relationship between two entities during a date range, identify recent structural changes, restrict results to independently corroborated evidence, or find connections that became materially stronger after a given event.

Natural language does not bypass the knowledge model. The formal operation and evidence remain authoritative; generated prose is a presentation layer.

---

# 20. Live Investigations and Monitoring

Investigations and watched entities are continuous objects rather than static query results.

A live investigation can maintain a materialized subgraph and a set of relevant dependencies. When an incoming claim changes a relevant belief, conflict, relation, hypothesis, or gap, the system calculates a semantic diff and updates the workspace.

The user is notified only when a change is meaningful according to configured thresholds and context. The system avoids confidence flicker and low-value alert noise.

Priority-aware scheduling gives active investigations and high-importance entities faster convergence while allowing cold regions of the knowledge space to remain lazily materialized.

---

# 21. Security, Authorization, and Information Leakage

Authorization can apply at entity, attribute, claim, evidence, relation, source, investigation, and derived inference levels.

Derived information can itself leak restricted upstream facts. Permission lineage therefore matters. An inference is not automatically safe merely because it contains no direct copy of protected evidence.

The system supports field-level and evidence-level access control where necessary. Encryption is applied in transit and at rest, with stronger controls available for sensitive payloads.

Deletion and retention policies distinguish between preserving structural audit history and physically retaining sensitive content. Where legally required, sensitive content can be physically removed while retaining a non-sensitive tombstone sufficient to preserve system consistency and audit semantics.

Tenant isolation, when enabled, extends beyond database rows to caches, indexes, embeddings, model-learning effects, source reliability, logs, and projections.

---

# 22. Auditability and Historical Reconstruction

The platform must reconstruct not only what the world model says now but also how the system arrived there.

All meaningful events carry event identity, event type, version, occurrence time, tenant or workspace context where relevant, correlation ID, causation ID, actor identity, and producer identity.

Human decisions are always attributable. Model versions are always attributable. Source changes are historically attributable. Replay behavior is deterministic where technically feasible, and nondeterministic dependencies are explicitly recorded.

Historical queries can reconstruct knowledge state from a prior system time and compare it with current state. Snapshots may accelerate replay but never replace durable history.

---

# 23. Resilience and Failure Behavior

Subsystem failure is isolated.

If the hypothesis engine fails, claim ingestion continues. If graph projection fails, authoritative knowledge continues. If vector search is unavailable, entity state and evidence remain accessible. If an LLM provider is unavailable, the product loses optional assistance rather than core correctness.

Accepted authoritative data is never silently dropped because a downstream consumer is unhealthy.

Effectively-once semantic processing is achieved through idempotent operations, stable identifiers, transactional event publication patterns, and duplicate detection rather than assuming a globally transactional distributed system.

Poison events are quarantined rather than blocking an entire ordered stream indefinitely. Backpressure and lag are observable by processing stage.

---

# 24. Observability

The platform exposes both technical observability and epistemic observability.

Technical telemetry includes CPU, memory, storage, queue lag, throughput, retries, failures, p95/p99 latency, cache behavior, database behavior, processor health, and deployment state.

Epistemic telemetry includes observations received, claims created, identity merges and splits, conflicts opened and resolved, beliefs strengthened or weakened, hypotheses promoted or disproven, gaps opened or closed, source reliability changes, active investigation refresh latency, and the age of derived knowledge.

The product exposes processing watermarks such as ingest, claim, belief, and graph watermarks so operators and, where useful, analysts can see exactly how current each layer is.

---

# 25. Product Quality Standard

The end product must feel deliberate at both macro and micro levels.

A mature screen should answer the user’s main question immediately, expose the strongest supporting context without requiring navigation, and provide deeper evidence on demand. Empty space, dense tables, graph elements, color, typography, and interaction patterns are all used in service of analytical clarity.

The UI uses consistent semantics for statuses and confidence. The same conceptual object looks and behaves similarly wherever it appears. The same claim opened from Entity Intelligence, a Conflict, a Hypothesis, or a Report leads to the same underlying object and provenance.

The product must not present fabricated precision. A low-quality probabilistic output is not made more trustworthy by displaying more decimal places. Confidence bars, labels, and numeric scores must match what the underlying model can defend.

No page is considered complete merely because it contains all information. It must expose the correct hierarchy, decisions, next actions, and evidence pathways.

The UI mockups defining Command Center, Entity Intelligence, Investigation Workspace, Graph Explorer, Evidence & Timeline, Conflicts, Hypotheses Workbench, Knowledge Gaps, Source Intelligence, Watchlist & Alerts, Reports & Briefings, and Settings represent the intended product quality bar. Implementation is expected to converge toward that richness and coherence rather than toward a simplified administrative dashboard.

---

# 26. Non-Goals

The system is not a general ETL platform. Upstream ingestion technology may exist, but ingestion is not allowed to consume the identity of the core product.

The system is not a generic graph database. Graph storage and traversal are internal capabilities.

The system is not a generic vector database or search engine.

The system is not a data warehouse or BI replacement.

The system is not a generic workflow or project-management tool.

The system is not an LLM wrapper.

The system is not designed around a single industry such as finance, security, compliance, healthcare, or intelligence. Domain-specific ontologies, constraints, display rules, and reasoning extensions may exist above a generic core.

The system does not attempt to force all uncertainty into a single canonical row. It preserves ambiguity when ambiguity is the correct representation.

---

# 27. Ontology and Domain Extensibility

The core ontology remains intentionally general. Concepts such as Entity, Event, Relation, Role, Claim, Evidence, Source, Time, Hypothesis, and Decision are universal enough to support many domains.

Domain extensions can define entity types, event types, relation types, attributes, constraints, reasoning rules, validation rules, freshness policies, display metadata, and specialized analytical behavior.

Ontology evolution is versioned. Historical knowledge remains interpretable after type or relationship definitions evolve. Migration does not silently rewrite historical meaning.

The system must support heterogeneous information without requiring all future domains to be known in advance.

---

# 28. API and Integration Surface

The external API exposes product capabilities rather than only CRUD primitives.

Examples include resolving an entity, retrieving entity intelligence, explaining a belief or relation, querying historical state, comparing states, opening an investigation, listing open conflicts, retrieving knowledge gaps, generating or evaluating hypotheses, ranking next investigative actions, subscribing to significant changes, and exporting evidence-linked reports.

Write APIs are idempotent where applicable. Versioning is explicit. Pagination, filtering, temporal semantics, and consistency requirements are part of the contract.

The same engine can serve the interactive product, customer APIs, and embedded intelligence surfaces without creating separate semantic implementations.

---

# 29. Implementation Order

Implementation follows conceptual dependency without creating unnecessary code dependency.

The foundation is established first: small kernel primitives, architectural fitness tests, event envelopes, clocks, and module boundaries.

The first knowledge slice is Observation to Claim. This establishes acceptance, deduplication, authoritative persistence, claim lifecycle, and queryability.

Source, Evidence, Provenance, and Temporal behavior are then introduced early so that explainability and historical correctness are not retrofitted later.

Identity Resolution follows, first with simple deterministic or rule-based behavior and later with richer candidate generation and scoring. Canonical Entities and reversible merge/split semantics follow immediately.

Events and Relations then create a usable world structure. Beliefs, Conflicts, and Decisions create the epistemic layer. Hypotheses and Knowledge Gaps create higher-order investigation intelligence.

Investigations, Graph, Search, and Product Composition then turn the model into a serious end-user product. Watchlists, Alerts, Reports, and Collaboration extend operational usage. Enterprise authorization, tenancy, auditing, administration, model management, and scale-oriented adapters deepen the platform as actual requirements justify them.

Implementation proceeds spirally rather than by attempting to finish an entire module before touching the next. Each capability reaches the smallest coherent version needed to unlock the next product slice, then matures through later iterations.

---

# 30. Definition of Architectural Success

The architecture is successful when a developer can work deeply inside one bounded context while understanding the contracts of neighboring contexts without needing to understand their internal implementation.

A Claim developer should not need to understand graph storage. A Graph developer should not need to know how belief confidence is computed internally. An Investigation developer should not reimplement identity resolution. A Reporting developer should not query private database tables owned by Evidence or Entities.

A module can be replaced internally without causing system-wide changes as long as its contracts remain stable.

The product still feels like one integrated system because the composition layer, event backbone, shared conceptual language, and unified UX join the independently owned capabilities into a coherent experience.

---

# 31. Definition of Product Success

The product is successful when a user can begin with a fragment of information and quickly reach a defensible understanding that would otherwise require manually navigating many disconnected sources.

For a specific entity, the user can see what is known, who or what supports it, how reliable and fresh that support is, which claims conflict, how identity was resolved, what relationships and events matter, what changed over time, which hypotheses remain open, what information is missing, and what to investigate next.

For an investigation, the user can maintain a live analytical state rather than a collection of static notes. New evidence changes the investigation meaningfully and transparently. The user can understand why a conclusion changed and can reconstruct what was believed at an earlier point.

For an organization, the product turns fragmented datasets into a durable, auditable, explainable layer of connected knowledge without forcing premature certainty or hiding disagreement.

---

# 32. Governing Invariants

The following invariants are non-negotiable unless this document is explicitly amended.

1. Evidence, observation, claim, belief, and inference are distinct concepts.
2. A graph edge is not automatically a fact.
3. Machine inference is never silently represented as direct observation.
4. Entity resolution is reversible and historically recorded.
5. Valid time and system knowledge time are distinct.
6. Contradictions are preserved and modeled.
7. Missing information can be modeled explicitly as a knowledge gap.
8. Every material derived conclusion has provenance.
9. A downstream failure cannot erase accepted authoritative knowledge.
10. Derived projections are rebuildable.
11. Cross-module code access occurs only through explicit contracts.
12. Bounded contexts own their own lifecycle and persistence semantics.
13. Cross-context ORM relationships and direct repository access are prohibited.
14. Large recomputation is replaced by incremental invalidation wherever possible.
15. Global analytical work does not block local knowledge commit.
16. Product views compose knowledge; they do not own truth.
17. Human decisions are durable domain history.
18. AI output is versioned, attributable, and non-authoritative by default.
19. The user can inspect why the system believes a material conclusion.
20. The project remains deployable throughout incremental development.

---

# 33. Final Statement

The Knowledge State Engine is a living, revisable, explainable model of the world constructed from heterogeneous evidence.

It does not reduce reality to a set of imported records. It separates evidence from claims, identity from appearance, belief from observation, contradiction from error, inference from fact, and current understanding from historical understanding. It treats missing information as analytically meaningful and turns uncertainty into something that can be investigated rather than hidden.

Its internal architecture is deliberately simple at the deployment level and deliberately strict at the domain level. One product, one repository, and one coherent runtime contain independently understandable bounded contexts connected by stable contracts and event propagation. This structure allows the codebase to remain concentrated without becoming entangled.

Its end product is an investigation environment of high analytical density and high trust. The user can move from a global Command Center to Entity Intelligence, into an Investigation Workspace, through a Graph Explorer, down to evidence and temporal history, across conflicts and hypotheses, into explicit knowledge gaps, source reliability, live monitoring, and finally evidence-linked reports. Every layer is a different view of the same continuously evolving knowledge state.

The system is complete only when the sophistication of its internal knowledge model is expressed just as clearly in the product surface. The architecture exists to make the end-user experience possible; the end-user experience is the standard against which the architecture is judged.

The permanent design principle is therefore:

> **Build a continuously revisable model of reality from heterogeneous evidence, where every entity, relationship, conclusion, uncertainty, and decision remains traceable, temporal, explainable, and open to investigation.**
