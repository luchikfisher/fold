אני מניח כאן את אותה החלטה ארכיטקטונית שכבר קבענו: repository אחד, deployable מרכזי אחד, Modular Monolith קשיח, DDD +
TDD, bounded contexts נפרדים, ו־UI שנבנה ונארז כחלק מאותו מוצר.

# Knowledge State Engine

## Final Mature Repository Structure

```text
knowledge-state-engine/
│
├── README.md
├── LICENSE
├── CHANGELOG.md
├── CODEOWNERS
├── SECURITY.md
├── CONTRIBUTING.md
├── ARCHITECTURE.md
│
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
│
├── .editorconfig
├── .gitignore
├── .gitattributes
├── .dockerignore
├── .env.example
│
├── .github/
│   ├── workflows/
│   │   ├── build.yml
│   │   ├── test.yml
│   │   ├── architecture-tests.yml
│   │   ├── integration-tests.yml
│   │   ├── performance-tests.yml
│   │   ├── security-scan.yml
│   │   ├── dependency-scan.yml
│   │   ├── container-build.yml
│   │   ├── release.yml
│   │   └── docs.yml
│   │
│   ├── ISSUE_TEMPLATE/
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── dependabot.yml
│
├── docs/
│   │
│   ├── architecture/
│   │   ├── overview.md
│   │   ├── system-context.md
│   │   ├── container-view.md
│   │   ├── component-view.md
│   │   ├── runtime-view.md
│   │   ├── deployment-view.md
│   │   ├── module-map.md
│   │   ├── dependency-rules.md
│   │   ├── event-topology.md
│   │   ├── data-ownership.md
│   │   ├── consistency-model.md
│   │   ├── temporal-model.md
│   │   ├── identity-model.md
│   │   ├── epistemic-model.md
│   │   ├── graph-model.md
│   │   ├── security-model.md
│   │   ├── multi-tenancy.md
│   │   ├── failure-model.md
│   │   ├── replay-model.md
│   │   ├── scaling-model.md
│   │   └── performance-model.md
│   │
│   ├── domain/
│   │   ├── ubiquitous-language.md
│   │   ├── bounded-contexts.md
│   │   ├── aggregates.md
│   │   ├── domain-events.md
│   │   ├── invariants.md
│   │   ├── state-machines.md
│   │   └── ontology-boundaries.md
│   │
│   ├── adr/
│   │   ├── 0001-modular-monolith.md
│   │   ├── 0002-single-deployable.md
│   │   ├── 0003-domain-first-modules.md
│   │   ├── 0004-event-driven-internal-integration.md
│   │   ├── 0005-no-cross-module-repository-access.md
│   │   ├── 0006-cross-module-ids-not-object-references.md
│   │   ├── 0007-append-oriented-authoritative-state.md
│   │   ├── 0008-bitemporal-knowledge.md
│   │   ├── 0009-graph-as-derived-projection.md
│   │   ├── 0010-composition-layer.md
│   │   ├── 0011-polyglot-derived-storage.md
│   │   ├── 0012-transactional-outbox.md
│   │   ├── 0013-effectively-once-processing.md
│   │   ├── 0014-small-aggregate-boundaries.md
│   │   ├── 0015-versioned-integration-events.md
│   │   ├── 0016-incremental-recomputation.md
│   │   ├── 0017-logical-identity-indirection.md
│   │   └── README.md
│   │
│   ├── api/
│   │   ├── public-api.md
│   │   ├── internal-api.md
│   │   ├── event-contracts.md
│   │   ├── error-model.md
│   │   ├── pagination.md
│   │   ├── filtering.md
│   │   ├── consistency-options.md
│   │   └── versioning.md
│   │
│   ├── operations/
│   │   ├── runbook.md
│   │   ├── incident-response.md
│   │   ├── replay.md
│   │   ├── source-quarantine.md
│   │   ├── model-quarantine.md
│   │   ├── restore.md
│   │   ├── disaster-recovery.md
│   │   ├── capacity-planning.md
│   │   └── slo.md
│   │
│   └── product/
│       ├── command-center.md
│       ├── entity-intelligence.md
│       ├── investigation-workspace.md
│       ├── graph-explorer.md
│       ├── evidence-timeline.md
│       ├── conflicts.md
│       ├── hypotheses.md
│       ├── knowledge-gaps.md
│       ├── source-intelligence.md
│       ├── watchlists-alerts.md
│       └── reports.md
│
├── config/
│   ├── application.yml
│   ├── application-local.yml
│   ├── application-test.yml
│   ├── application-dev.yml
│   ├── application-staging.yml
│   ├── application-prod.yml
│   │
│   ├── logging/
│   │   ├── logback.xml
│   │   └── structured-logging.yml
│   │
│   ├── observability/
│   │   ├── metrics.yml
│   │   ├── tracing.yml
│   │   └── health.yml
│   │
│   ├── security/
│   │   ├── authorization.yml
│   │   ├── data-classification.yml
│   │   └── encryption.yml
│   │
│   └── features/
│       ├── defaults.yml
│       ├── experimental.yml
│       └── production.yml
│
├── database/
│   │
│   ├── migrations/
│   │   ├── common/
│   │   ├── sources/
│   │   ├── evidence/
│   │   ├── observations/
│   │   ├── claims/
│   │   ├── identity/
│   │   ├── entities/
│   │   ├── events/
│   │   ├── relations/
│   │   ├── temporal/
│   │   ├── provenance/
│   │   ├── beliefs/
│   │   ├── conflicts/
│   │   ├── hypotheses/
│   │   ├── knowledgegaps/
│   │   ├── decisions/
│   │   ├── investigations/
│   │   ├── graph/
│   │   ├── search/
│   │   ├── watchlist/
│   │   ├── alerts/
│   │   ├── reports/
│   │   ├── security/
│   │   ├── tenancy/
│   │   ├── audit/
│   │   └── administration/
│   │
│   ├── fixtures/
│   │   ├── local/
│   │   ├── test/
│   │   └── demo/
│   │
│   └── maintenance/
│       ├── indexes/
│       ├── partitions/
│       ├── retention/
│       ├── compaction/
│       └── diagnostics/
│
├── schemas/
│   ├── events/
│   ├── api/
│   ├── import/
│   └── ontology/
│
├── scripts/
│   ├── dev/
│   ├── database/
│   ├── migrations/
│   ├── replay/
│   ├── benchmarks/
│   ├── synthetic-data/
│   ├── release/
│   └── diagnostics/
│
├── docker/
│   ├── Dockerfile
│   ├── Dockerfile.dev
│   ├── compose.yml
│   ├── compose.test.yml
│   └── compose.observability.yml
│
├── tools/
│   ├── architecture-checker/
│   ├── event-inspector/
│   ├── replay-cli/
│   ├── synthetic-generator/
│   ├── graph-inspector/
│   ├── benchmark-runner/
│   └── migration-validator/
│
├── benchmarks/
│   ├── ingestion/
│   ├── claims/
│   ├── identity-resolution/
│   ├── belief-recalculation/
│   ├── graph-traversal/
│   ├── search/
│   ├── investigation-refresh/
│   └── replay/
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── kse/
    │   │           │
    │   │           ├── bootstrap/
    │   │           ├── kernel/
    │   │           ├── platform/
    │   │           ├── composition/
    │   │           └── modules/
    │   │
    │   ├── resources/
    │   └── ui/
    │
    └── test/
        ├── java/
        └── resources/
```

---

# 1. Bootstrap

```text
src/main/java/com/kse/bootstrap/
│
├── KnowledgeStateEngineApplication.java
│
├── configuration/
│   ├── ApplicationConfiguration.java
│   ├── ModuleConfiguration.java
│   ├── PersistenceConfiguration.java
│   ├── MessagingConfiguration.java
│   ├── SchedulingConfiguration.java
│   ├── SecurityConfiguration.java
│   ├── ObservabilityConfiguration.java
│   ├── WebConfiguration.java
│   ├── SerializationConfiguration.java
│   ├── FeatureFlagConfiguration.java
│   └── ClockConfiguration.java
│
├── lifecycle/
│   ├── StartupValidator.java
│   ├── ModuleBootstrapper.java
│   ├── WarmupRunner.java
│   ├── ShutdownCoordinator.java
│   └── ReadinessCoordinator.java
│
└── diagnostics/
    ├── StartupDiagnostics.java
    ├── ConfigurationDiagnostics.java
    └── ModuleHealthReporter.java
```

---

# 2. Kernel

ה־Kernel נשאר קטן גם אחרי שנים.

```text
src/main/java/com/kse/kernel/
│
├── ids/
│   ├── DomainId.java
│   ├── EntityId.java
│   ├── ObservationId.java
│   ├── EvidenceId.java
│   ├── ClaimId.java
│   ├── SourceId.java
│   ├── EventId.java
│   ├── RelationId.java
│   ├── BeliefId.java
│   ├── ConflictId.java
│   ├── HypothesisId.java
│   ├── KnowledgeGapId.java
│   ├── DecisionId.java
│   ├── InvestigationId.java
│   ├── AlertId.java
│   ├── ReportId.java
│   └── TenantId.java
│
├── time/
│   ├── Clock.java
│   ├── SystemClock.java
│   ├── Timestamp.java
│   ├── TimeRange.java
│   ├── ValidTime.java
│   ├── KnowledgeTime.java
│   └── Duration.java
│
├── events/
│   ├── DomainEvent.java
│   ├── IntegrationEvent.java
│   ├── EventEnvelope.java
│   ├── EventMetadata.java
│   ├── EventVersion.java
│   ├── EventPublisher.java
│   ├── EventSubscriber.java
│   └── EventHandler.java
│
├── result/
│   ├── Result.java
│   ├── Success.java
│   ├── Failure.java
│   ├── DomainError.java
│   └── ValidationError.java
│
├── pagination/
│   ├── Page.java
│   ├── PageRequest.java
│   ├── Cursor.java
│   └── Sort.java
│
├── money/
│   ├── Money.java
│   └── Currency.java
│
├── probability/
│   ├── Probability.java
│   ├── Confidence.java
│   └── Score.java
│
└── collections/
    ├── NonEmptyList.java
    └── NonEmptySet.java
```

---

# 3. Platform

אלו יכולות טכניות כלליות. הן אינן חלק מה־domain העסקי.

```text
src/main/java/com/kse/platform/
│
├── messaging/
│   ├── bus/
│   │   ├── InternalEventBus.java
│   │   ├── EventDispatcher.java
│   │   └── EventHandlerRegistry.java
│   │
│   ├── outbox/
│   │   ├── OutboxMessage.java
│   │   ├── OutboxRepository.java
│   │   ├── OutboxPublisher.java
│   │   ├── OutboxProcessor.java
│   │   └── OutboxCleanup.java
│   │
│   └── inbox/
│       ├── InboxMessage.java
│       ├── InboxRepository.java
│       ├── IdempotencyGuard.java
│       └── DuplicateMessagePolicy.java
│
├── persistence/
│   ├── transactions/
│   │   ├── TransactionRunner.java
│   │   └── TransactionBoundary.java
│   │
│   ├── jdbc/
│   ├── json/
│   ├── locks/
│   │   ├── AdvisoryLock.java
│   │   └── LockManager.java
│   │
│   └── batching/
│       ├── BatchWriter.java
│       └── BatchReader.java
│
├── cache/
│   ├── Cache.java
│   ├── CacheKey.java
│   ├── CacheEntry.java
│   ├── CachePolicy.java
│   ├── DependencyAwareCache.java
│   └── CacheInvalidator.java
│
├── jobs/
│   ├── Job.java
│   ├── JobId.java
│   ├── JobScheduler.java
│   ├── JobRunner.java
│   ├── JobRetryPolicy.java
│   ├── JobLease.java
│   └── JobRepository.java
│
├── featureflags/
│   ├── FeatureFlag.java
│   ├── FeatureFlagService.java
│   └── FeatureFlagRepository.java
│
├── observability/
│   ├── metrics/
│   ├── tracing/
│   ├── logging/
│   ├── health/
│   └── diagnostics/
│
├── serialization/
│   ├── JsonSerializer.java
│   ├── EventSerializer.java
│   └── SchemaVersionResolver.java
│
├── resilience/
│   ├── RetryPolicy.java
│   ├── CircuitBreaker.java
│   ├── RateLimiter.java
│   ├── Bulkhead.java
│   └── BackpressurePolicy.java
│
└── security/
    ├── encryption/
    ├── hashing/
    ├── redaction/
    └── secrets/
```

---

# 4. Standard Module Anatomy

כל bounded context תחת `modules/` משתמש באותה אנטומיה.

בפעם הראשונה בלבד אני מפרט אותה במלואה:

```text
modules/<module>/
│
├── api/
│   ├── command/
│   ├── query/
│   ├── event/
│   ├── view/
│   └── error/
│
├── domain/
│   ├── model/
│   ├── value/
│   ├── policy/
│   ├── service/
│   ├── repository/
│   ├── event/
│   ├── specification/
│   └── error/
│
├── application/
│   ├── feature/
│   │   └── <feature>/
│   │       ├── <Feature>Command.java
│   │       ├── <Feature>Handler.java
│   │       ├── <Feature>Result.java
│   │       └── <Feature>Validator.java
│   │
│   ├── query/
│   ├── projection/
│   ├── port/
│   └── listener/
│
├── infrastructure/
│   ├── persistence/
│   │   ├── record/
│   │   ├── repository/
│   │   ├── mapper/
│   │   └── query/
│   │
│   ├── messaging/
│   │   ├── publisher/
│   │   └── consumer/
│   │
│   ├── web/
│   │   ├── controller/
│   │   ├── request/
│   │   ├── response/
│   │   └── mapper/
│   │
│   ├── integration/
│   └── configuration/
│
└── README.md
```

מכאן ואילך, בכל מודול אני מציין רק את התוכן הדומייני הייחודי. ארבע השכבות האלו קיימות בכל אחד מהם.

---

# 5. Sources

```text
modules/sources/
│
├── domain/
│   ├── model/
│   │   ├── Source.java
│   │   ├── SourceProfile.java
│   │   ├── SourceFieldProfile.java
│   │   ├── SourceDependency.java
│   │   ├── SourceReliabilityProfile.java
│   │   └── SourceFreshnessProfile.java
│   │
│   ├── value/
│   │   ├── SourceType.java
│   │   ├── SourceCategory.java
│   │   ├── SourceStatus.java
│   │   ├── Coverage.java
│   │   ├── Freshness.java
│   │   ├── Reliability.java
│   │   ├── UpdateCadence.java
│   │   └── DependencyStrength.java
│   │
│   ├── policy/
│   │   ├── SourceReliabilityPolicy.java
│   │   ├── SourceFreshnessPolicy.java
│   │   ├── SourceQuarantinePolicy.java
│   │   └── SourceDependencyPolicy.java
│   │
│   └── event/
│       ├── SourceRegistered.java
│       ├── SourceStatusChanged.java
│       ├── SourceReliabilityChanged.java
│       ├── SourceBecameStale.java
│       ├── SourceRecovered.java
│       └── SourceQuarantined.java
│
└── application/feature/
    ├── registersource/
    ├── updatesource/
    ├── adjustreliability/
    ├── marksourcestale/
    ├── quarantinesource/
    ├── resumesource/
    └── recalculatereliability/
```

---

# 6. Evidence

```text
modules/evidence/
│
├── domain/model/
│   ├── Evidence.java
│   ├── EvidenceSnapshot.java
│   ├── EvidenceReference.java
│   ├── EvidenceFragment.java
│   ├── EvidenceLocation.java
│   └── EvidenceChecksum.java
│
├── domain/value/
│   ├── EvidenceType.java
│   ├── EvidenceFormat.java
│   ├── EvidenceStatus.java
│   ├── EvidenceStrength.java
│   └── EvidenceIndependence.java
│
├── domain/event/
│   ├── EvidenceRegistered.java
│   ├── EvidenceSnapshotCreated.java
│   ├── EvidenceInvalidated.java
│   └── EvidenceDeleted.java
│
└── application/feature/
    ├── registerevidence/
    ├── createevidencesnapshot/
    ├── linkevidencetosource/
    ├── invalidateevidence/
    └── retrieveevidence/
```

---

# 7. Observations

```text
modules/observations/
│
├── domain/model/
│   ├── Observation.java
│   ├── ObservationPayload.java
│   ├── ObservationField.java
│   ├── ObservationSubject.java
│   └── ObservationOrigin.java
│
├── domain/value/
│   ├── ObservationType.java
│   ├── ObservationStatus.java
│   ├── ObservationFingerprint.java
│   ├── ObservedAt.java
│   └── ArrivedAt.java
│
├── domain/policy/
│   ├── ObservationDeduplicationPolicy.java
│   ├── ObservationAcceptancePolicy.java
│   └── ObservationFingerprintPolicy.java
│
├── domain/event/
│   ├── ObservationReceived.java
│   ├── ObservationAccepted.java
│   ├── ObservationRejected.java
│   └── DuplicateObservationDetected.java
│
└── application/feature/
    ├── submitobservation/
    ├── deduplicateobservation/
    ├── rejectobservation/
    └── replayobservation/
```

---

# 8. Claims

```text
modules/claims/
│
├── domain/model/
│   ├── Claim.java
│   ├── ClaimSubject.java
│   ├── ClaimObject.java
│   ├── ClaimValue.java
│   └── ClaimRevision.java
│
├── domain/value/
│   ├── Predicate.java
│   ├── ClaimType.java
│   ├── ClaimStatus.java
│   ├── ClaimOrigin.java
│   ├── ClaimConfidence.java
│   └── ClaimValidity.java
│
├── domain/policy/
│   ├── ClaimCreationPolicy.java
│   ├── ClaimValidityPolicy.java
│   ├── ClaimSupersessionPolicy.java
│   └── ClaimInvalidationPolicy.java
│
├── domain/event/
│   ├── ClaimCreated.java
│   ├── ClaimUpdated.java
│   ├── ClaimSuperseded.java
│   ├── ClaimInvalidated.java
│   └── ClaimReinstated.java
│
└── application/feature/
    ├── createclaim/
    ├── supersedeclaim/
    ├── invalidateclaim/
    ├── reinstateclaim/
    └── rebuildclaim/
```

---

# 9. Identity

```text
modules/identity/
│
├── domain/model/
│   ├── IdentityCandidate.java
│   ├── IdentityHypothesis.java
│   ├── IdentityCluster.java
│   ├── IdentityComparison.java
│   ├── MatchEvidence.java
│   ├── NegativeMatchEvidence.java
│   ├── MergeProposal.java
│   ├── SplitProposal.java
│   └── IdentityRevision.java
│
├── domain/value/
│   ├── IdentityScore.java
│   ├── MatchFeature.java
│   ├── MatchDecision.java
│   ├── IdentityConfidence.java
│   ├── CandidateRank.java
│   └── IdentityState.java
│
├── domain/service/
│   ├── CandidateGenerator.java
│   ├── IdentityScorer.java
│   ├── IdentityResolver.java
│   └── IdentityClusterer.java
│
├── domain/policy/
│   ├── CandidateBlockingPolicy.java
│   ├── MergeThresholdPolicy.java
│   ├── SplitThresholdPolicy.java
│   ├── NegativeEvidencePolicy.java
│   └── ContextualResolutionPolicy.java
│
├── domain/event/
│   ├── IdentityCandidateCreated.java
│   ├── IdentityHypothesisCreated.java
│   ├── IdentityResolved.java
│   ├── MergeProposed.java
│   ├── SplitProposed.java
│   └── IdentityResolutionRevised.java
│
└── application/feature/
    ├── generatecandidates/
    ├── scorecandidate/
    ├── resolveidentity/
    ├── proposemerge/
    ├── proposesplit/
    ├── reconsideridentity/
    └── bulkresolve/
```

---

# 10. Entities

```text
modules/entities/
│
├── domain/model/
│   ├── Entity.java
│   ├── EntityIdentity.java
│   ├── EntityAlias.java
│   ├── EntityAttribute.java
│   ├── EntityAttributeSet.java
│   ├── EntityLifecycle.java
│   └── CanonicalEntity.java
│
├── domain/value/
│   ├── EntityType.java
│   ├── EntityStatus.java
│   ├── EntityConfidence.java
│   ├── EntityName.java
│   └── CanonicalEntityHandle.java
│
├── domain/policy/
│   ├── EntityCreationPolicy.java
│   ├── EntityMergePolicy.java
│   ├── EntitySplitPolicy.java
│   └── CanonicalizationPolicy.java
│
├── domain/event/
│   ├── EntityCreated.java
│   ├── EntityUpdated.java
│   ├── EntityMerged.java
│   ├── EntitySplit.java
│   ├── EntityReclassified.java
│   └── EntityArchived.java
│
└── application/feature/
    ├── createentity/
    ├── updateentity/
    ├── mergeentities/
    ├── splitentity/
    ├── resolvecanonicalentity/
    ├── addalias/
    └── archiveentity/
```

---

# 11. Events

```text
modules/events/
│
├── domain/model/
│   ├── Event.java
│   ├── EventParticipant.java
│   ├── EventRole.java
│   ├── EventAttribute.java
│   └── EventRevision.java
│
├── domain/value/
│   ├── EventType.java
│   ├── EventStatus.java
│   ├── EventConfidence.java
│   └── EventValidity.java
│
├── domain/event/
│   ├── EventCreated.java
│   ├── EventRevised.java
│   ├── EventInvalidated.java
│   └── EventParticipantChanged.java
│
└── application/feature/
    ├── createevent/
    ├── reviseevent/
    ├── addparticipant/
    ├── removeparticipant/
    └── invalidateevent/
```

---

# 12. Relations

```text
modules/relations/
│
├── domain/model/
│   ├── Relation.java
│   ├── RelationEndpoint.java
│   ├── RelationRevision.java
│   └── RelationEvidenceSummary.java
│
├── domain/value/
│   ├── RelationType.java
│   ├── RelationStatus.java
│   ├── RelationDirection.java
│   ├── RelationConfidence.java
│   └── RelationValidity.java
│
├── domain/policy/
│   ├── RelationCreationPolicy.java
│   ├── RelationAggregationPolicy.java
│   └── RelationSupersessionPolicy.java
│
├── domain/event/
│   ├── RelationCreated.java
│   ├── RelationChanged.java
│   ├── RelationInvalidated.java
│   └── RelationConfidenceChanged.java
│
└── application/feature/
    ├── createrelation/
    ├── updaterelation/
    ├── invalidaterelation/
    └── recomputerelation/
```

---

# 13. Temporal

```text
modules/temporal/
│
├── domain/model/
│   ├── TemporalFact.java
│   ├── TemporalRevision.java
│   ├── TemporalInterval.java
│   └── TemporalState.java
│
├── domain/value/
│   ├── ValidFrom.java
│   ├── ValidTo.java
│   ├── KnownFrom.java
│   ├── KnownTo.java
│   └── TemporalOverlap.java
│
├── domain/service/
│   ├── TemporalReconciler.java
│   ├── HistoricalStateResolver.java
│   └── TemporalDiffCalculator.java
│
└── application/feature/
    ├── reconcilehistory/
    ├── resolvehistoricalstate/
    ├── comparehistoricalstates/
    └── correctretroactively/
```

---

# 14. Provenance

```text
modules/provenance/
│
├── domain/model/
│   ├── ProvenanceRecord.java
│   ├── Derivation.java
│   ├── Dependency.java
│   ├── ProcessingStep.java
│   ├── ProcessingPath.java
│   └── ProvenanceChain.java
│
├── domain/value/
│   ├── DerivationType.java
│   ├── DependencyType.java
│   ├── ProcessingVersion.java
│   └── ProvenanceStatus.java
│
├── domain/event/
│   ├── ProvenanceRecorded.java
│   ├── DependencyRecorded.java
│   └── DependencyInvalidated.java
│
└── application/feature/
    ├── recordprovenance/
    ├── adddependency/
    ├── traceprovenance/
    ├── calculateblastRadius/
    └── invalidateprovenancebranch/
```

---

# 15. Beliefs

```text
modules/beliefs/
│
├── domain/model/
│   ├── Belief.java
│   ├── BeliefState.java
│   ├── BeliefAccumulator.java
│   ├── SupportingSignal.java
│   ├── ContradictingSignal.java
│   ├── BeliefRevision.java
│   └── BeliefExplanation.java
│
├── domain/value/
│   ├── BeliefConfidence.java
│   ├── BeliefStatus.java
│   ├── BeliefStrength.java
│   ├── FreshnessFactor.java
│   └── EvidenceWeight.java
│
├── domain/service/
│   ├── BeliefCalculator.java
│   ├── EvidenceAggregator.java
│   └── BeliefCalibrator.java
│
├── domain/policy/
│   ├── BeliefAggregationPolicy.java
│   ├── EvidenceIndependencePolicy.java
│   ├── BeliefDecayPolicy.java
│   └── BeliefTransitionPolicy.java
│
├── domain/event/
│   ├── BeliefCreated.java
│   ├── BeliefChanged.java
│   ├── BeliefStrengthened.java
│   ├── BeliefWeakened.java
│   ├── BeliefSuperseded.java
│   └── BeliefInvalidated.java
│
└── application/feature/
    ├── createbelief/
    ├── recalculatebelief/
    ├── recalibratebelief/
    ├── applydecay/
    └── explainbelief/
```

---

# 16. Conflicts

```text
modules/conflicts/
│
├── domain/model/
│   ├── Conflict.java
│   ├── ConflictParticipant.java
│   ├── ConflictEvidence.java
│   ├── ConflictResolution.java
│   └── ConflictRevision.java
│
├── domain/value/
│   ├── ConflictType.java
│   ├── ConflictSeverity.java
│   ├── ConflictStatus.java
│   └── ResolutionType.java
│
├── domain/policy/
│   ├── ConflictDetectionPolicy.java
│   ├── ConflictSeverityPolicy.java
│   └── ConflictResolutionPolicy.java
│
├── domain/event/
│   ├── ConflictOpened.java
│   ├── ConflictChanged.java
│   ├── ConflictResolved.java
│   ├── ConflictReopened.java
│   └── ConflictDismissed.java
│
└── application/feature/
    ├── detectconflict/
    ├── openconflict/
    ├── resolveconflict/
    ├── keepunresolved/
    ├── reopenconflict/
    └── dismissconflict/
```

---

# 17. Hypotheses

```text
modules/hypotheses/
│
├── domain/model/
│   ├── Hypothesis.java
│   ├── HypothesisStatement.java
│   ├── HypothesisSupport.java
│   ├── CounterSignal.java
│   ├── Assumption.java
│   ├── HypothesisRevision.java
│   ├── CompetingHypothesis.java
│   └── HypothesisImpactSurface.java
│
├── domain/value/
│   ├── HypothesisStatus.java
│   ├── HypothesisConfidence.java
│   ├── HypothesisPriority.java
│   └── AssumptionRisk.java
│
├── domain/service/
│   ├── HypothesisEvaluator.java
│   ├── HypothesisGenerator.java
│   └── HypothesisComparator.java
│
├── domain/policy/
│   ├── PromotionPolicy.java
│   ├── RejectionPolicy.java
│   ├── HypothesisConfidencePolicy.java
│   └── CompetingHypothesisPolicy.java
│
├── domain/event/
│   ├── HypothesisCreated.java
│   ├── HypothesisChanged.java
│   ├── HypothesisStrengthened.java
│   ├── HypothesisWeakened.java
│   ├── HypothesisVerified.java
│   ├── HypothesisDisproven.java
│   └── HypothesisSuperseded.java
│
└── application/feature/
    ├── createhypothesis/
    ├── evaluatehypothesis/
    ├── addsupport/
    ├── addcountersignal/
    ├── promotehypothesis/
    ├── disprovehypothesis/
    ├── comparehypotheses/
    └── suggestnexttest/
```

---

# 18. Knowledge Gaps

```text
modules/knowledgegaps/
│
├── domain/model/
│   ├── KnowledgeGap.java
│   ├── GapDependency.java
│   ├── GapPlan.java
│   ├── GapAction.java
│   ├── AcquisitionSuggestion.java
│   └── GapImpactSurface.java
│
├── domain/value/
│   ├── GapType.java
│   ├── GapStatus.java
│   ├── GapImpact.java
│   ├── InformationGain.java
│   ├── AcquisitionEffort.java
│   └── GapPriority.java
│
├── domain/service/
│   ├── GapDetector.java
│   ├── InformationGainCalculator.java
│   └── NextBestActionRanker.java
│
├── domain/event/
│   ├── KnowledgeGapOpened.java
│   ├── KnowledgeGapChanged.java
│   ├── KnowledgeGapResolved.java
│   ├── KnowledgeGapDeferred.java
│   └── GapPlanCreated.java
│
└── application/feature/
    ├── detectgap/
    ├── creategap/
    ├── resolvegap/
    ├── defergap/
    ├── creategapplan/
    ├── recommendacquisition/
    └── ranknextactions/
```

---

# 19. Decisions

```text
modules/decisions/
│
├── domain/model/
│   ├── Decision.java
│   ├── DecisionOption.java
│   ├── DecisionRationale.java
│   ├── DecisionContext.java
│   ├── DecisionEvidence.java
│   └── DecisionRevision.java
│
├── domain/value/
│   ├── DecisionType.java
│   ├── DecisionStatus.java
│   ├── DecisionOutcome.java
│   ├── ActorType.java
│   └── DecisionConfidence.java
│
├── domain/event/
│   ├── DecisionRequested.java
│   ├── DecisionRecorded.java
│   ├── DecisionRevised.java
│   └── DecisionReverted.java
│
└── application/feature/
    ├── requestdecision/
    ├── recorddecision/
    ├── revisedecision/
    ├── revertdecision/
    └── explaindecision/
```

---

# 20. Investigations

```text
modules/investigations/
│
├── domain/model/
│   ├── Investigation.java
│   ├── InvestigationQuestion.java
│   ├── InvestigationScope.java
│   ├── InvestigationSubject.java
│   ├── InvestigationMember.java
│   ├── InvestigationFinding.java
│   ├── InvestigationNote.java
│   ├── InvestigationBookmark.java
│   ├── InvestigationArtifact.java
│   ├── InvestigationTimeline.java
│   └── InvestigationRevision.java
│
├── domain/value/
│   ├── InvestigationStatus.java
│   ├── InvestigationPriority.java
│   ├── InvestigationRole.java
│   ├── InvestigationVisibility.java
│   └── InvestigationClassification.java
│
├── domain/event/
│   ├── InvestigationCreated.java
│   ├── InvestigationChanged.java
│   ├── SubjectAddedToInvestigation.java
│   ├── FindingPinned.java
│   ├── InvestigationClosed.java
│   └── InvestigationReopened.java
│
└── application/feature/
    ├── createinvestigation/
    ├── addsubject/
    ├── removesubject/
    ├── addfinding/
    ├── addnote/
    ├── bookmarkitem/
    ├── assignmember/
    ├── closeinvestigation/
    └── reopeninvestigation/
```

---

# 21. Graph

```text
modules/graph/
│
├── domain/model/
│   ├── GraphProjection.java
│   ├── GraphNode.java
│   ├── GraphEdge.java
│   ├── GraphPath.java
│   ├── GraphNeighborhood.java
│   ├── GraphPerspective.java
│   ├── GraphCommunity.java
│   ├── GraphMotif.java
│   └── GraphSnapshot.java
│
├── domain/value/
│   ├── GraphMode.java
│   ├── GraphDepth.java
│   ├── EdgeStyle.java
│   ├── GraphConfidenceThreshold.java
│   └── TraversalBudget.java
│
├── domain/service/
│   ├── GraphProjector.java
│   ├── PathFinder.java
│   ├── NeighborhoodExpander.java
│   ├── CommunityDetector.java
│   ├── MotifDetector.java
│   └── GraphInterestingnessRanker.java
│
├── domain/policy/
│   ├── ProjectionPolicy.java
│   ├── TraversalPolicy.java
│   ├── SupernodePolicy.java
│   └── GraphBudgetPolicy.java
│
└── application/feature/
    ├── projectgraph/
    ├── expandneighborhood/
    ├── findpath/
    ├── comparepaths/
    ├── detectcommunity/
    ├── detectmotif/
    └── buildperspective/
```

---

# 22. Search

```text
modules/search/
│
├── domain/model/
│   ├── SearchQuery.java
│   ├── SearchResult.java
│   ├── SearchDocument.java
│   ├── SearchFacet.java
│   ├── SearchRanking.java
│   └── SearchSuggestion.java
│
├── domain/value/
│   ├── SearchMode.java
│   ├── SearchScope.java
│   ├── SearchScore.java
│   └── SearchFilter.java
│
├── domain/service/
│   ├── SearchPlanner.java
│   ├── SearchRanker.java
│   └── SemanticSearchService.java
│
└── application/feature/
    ├── globalsearch/
    ├── entitysearch/
    ├── evidenceSearch/
    ├── semanticsearch/
    ├── facetedsearch/
    └── suggestsearch/
```

---

# 23. Watchlist

```text
modules/watchlist/
│
├── domain/model/
│   ├── Watchlist.java
│   ├── WatchTarget.java
│   ├── WatchRule.java
│   ├── WatchCondition.java
│   └── WatchSubscription.java
│
├── domain/value/
│   ├── WatchTargetType.java
│   ├── WatchStatus.java
│   ├── WatchFrequency.java
│   └── WatchScope.java
│
├── domain/event/
│   ├── WatchlistCreated.java
│   ├── WatchTargetAdded.java
│   ├── WatchRuleCreated.java
│   ├── WatchRuleTriggered.java
│   └── WatchRuleMuted.java
│
└── application/feature/
    ├── createwatchlist/
    ├── addwatchtarget/
    ├── removewatchtarget/
    ├── createwatchrule/
    ├── muteRule/
    └── evaluaterules/
```

---

# 24. Alerts

```text
modules/alerts/
│
├── domain/model/
│   ├── Alert.java
│   ├── AlertTrigger.java
│   ├── AlertImpact.java
│   ├── AlertEvidenceDiff.java
│   ├── AlertAcknowledgement.java
│   └── AlertEscalation.java
│
├── domain/value/
│   ├── AlertSeverity.java
│   ├── AlertStatus.java
│   ├── AlertChannel.java
│   └── EscalationLevel.java
│
├── domain/policy/
│   ├── AlertDeduplicationPolicy.java
│   ├── AlertNoisePolicy.java
│   ├── AlertEscalationPolicy.java
│   └── AlertSuppressionPolicy.java
│
├── domain/event/
│   ├── AlertCreated.java
│   ├── AlertAcknowledged.java
│   ├── AlertEscalated.java
│   ├── AlertResolved.java
│   └── AlertSuppressed.java
│
└── application/feature/
    ├── createalert/
    ├── acknowledgealert/
    ├── escalatealert/
    ├── resolvealert/
    ├── snoozealert/
    └── suppressalert/
```

---

# 25. Reports

```text
modules/reports/
│
├── domain/model/
│   ├── Report.java
│   ├── ReportVersion.java
│   ├── ReportSection.java
│   ├── ReportFinding.java
│   ├── ReportEvidenceReference.java
│   ├── ReportDistribution.java
│   ├── ReportRecipient.java
│   ├── ReportSchedule.java
│   └── ReportTemplate.java
│
├── domain/value/
│   ├── ReportType.java
│   ├── ReportStatus.java
│   ├── ReportFormat.java
│   ├── EvidenceCoverage.java
│   └── PublicationStatus.java
│
├── domain/event/
│   ├── ReportCreated.java
│   ├── ReportVersionCreated.java
│   ├── ReportSubmittedForReview.java
│   ├── ReportPublished.java
│   └── ReportScheduled.java
│
└── application/feature/
    ├── createreport/
    ├── updatereport/
    ├── generatereport/
    ├── mapreportevidence/
    ├── submitforreview/
    ├── publishreport/
    ├── schedulereport/
    └── exportreport/
```

---

# 26. Collaboration

```text
modules/collaboration/
│
├── domain/model/
│   ├── Comment.java
│   ├── Mention.java
│   ├── Assignment.java
│   ├── ReviewRequest.java
│   └── ActivityEntry.java
│
├── domain/value/
│   ├── CollaborationTargetType.java
│   ├── ReviewStatus.java
│   └── AssignmentStatus.java
│
└── application/feature/
    ├── addcomment/
    ├── mentionuser/
    ├── assignreview/
    ├── completereview/
    └── recordactivity/
```

---

# 27. Users

```text
modules/users/
│
├── domain/model/
│   ├── User.java
│   ├── UserProfile.java
│   ├── Team.java
│   └── Membership.java
│
├── domain/value/
│   ├── UserStatus.java
│   ├── UserRole.java
│   └── MembershipStatus.java
│
└── application/feature/
    ├── createuser/
    ├── updateprofile/
    ├── addtoteam/
    ├── removefromteam/
    └── deactivateuser/
```

---

# 28. Tenancy

```text
modules/tenancy/
│
├── domain/model/
│   ├── Tenant.java
│   ├── Workspace.java
│   ├── TenantConfiguration.java
│   └── TenantQuota.java
│
├── domain/value/
│   ├── TenantStatus.java
│   ├── WorkspaceType.java
│   └── IsolationMode.java
│
└── application/feature/
    ├── createtenant/
    ├── createworkspace/
    ├── updatequota/
    ├── suspendtenant/
    └── deletetenant/
```

---

# 29. Authorization

```text
modules/authorization/
│
├── domain/model/
│   ├── Permission.java
│   ├── Role.java
│   ├── AccessPolicy.java
│   ├── ResourceGrant.java
│   └── DerivedPermission.java
│
├── domain/value/
│   ├── PermissionType.java
│   ├── ResourceType.java
│   ├── AccessDecision.java
│   └── DataClassification.java
│
├── domain/service/
│   ├── AuthorizationEngine.java
│   └── PermissionPropagationEngine.java
│
└── application/feature/
    ├── authorize/
    ├── grantpermission/
    ├── revokepermission/
    └── recomputederivedpermissions/
```

---

# 30. Audit

```text
modules/audit/
│
├── domain/model/
│   ├── AuditEntry.java
│   ├── AuditActor.java
│   ├── AuditTarget.java
│   └── AuditChangeSet.java
│
├── domain/value/
│   ├── AuditAction.java
│   └── AuditSeverity.java
│
└── application/feature/
    ├── recordauditentry/
    ├── searchauditlog/
    └── exportauditlog/
```

---

# 31. Administration

```text
modules/administration/
│
├── domain/model/
│   ├── ReplayRequest.java
│   ├── RecomputeRequest.java
│   ├── ProcessorState.java
│   ├── QuarantineRequest.java
│   ├── MaintenanceOperation.java
│   ├── SnapshotRequest.java
│   └── BlastRadiusEstimate.java
│
├── domain/value/
│   ├── ReplayScope.java
│   ├── ProcessorStatus.java
│   ├── OperationStatus.java
│   └── MaintenancePriority.java
│
└── application/feature/
    ├── replay/
    ├── recompute/
    ├── quarantine/
    ├── resumeprocessor/
    ├── createsnapshot/
    ├── restoreSnapshot/
    └── estimateblastradius/
```

---

# 32. Composition Layer

זה המקום היחיד שמותר לו “לראות” הרבה bounded contexts בו־זמנית.

```text
src/main/java/com/kse/composition/
│
├── commandcenter/
│   ├── CommandCenterQuery.java
│   ├── CommandCenterHandler.java
│   ├── CommandCenterView.java
│   ├── CommandCenterMetrics.java
│   ├── RecentInvestigationActivityView.java
│   ├── SignificantKnowledgeChangeView.java
│   └── OpenDecisionView.java
│
├── entityintelligence/
│   ├── EntityIntelligenceQuery.java
│   ├── EntityIntelligenceHandler.java
│   ├── EntityIntelligenceView.java
│   ├── EntityOverviewView.java
│   ├── EntityRelationshipView.java
│   ├── EntityTimelineView.java
│   ├── EntityConflictView.java
│   ├── EntityGapView.java
│   └── EntityExplanationView.java
│
├── investigationworkspace/
│   ├── InvestigationWorkspaceQuery.java
│   ├── InvestigationWorkspaceHandler.java
│   ├── InvestigationWorkspaceView.java
│   ├── InvestigationHypothesisView.java
│   ├── InvestigationActivityView.java
│   └── NextBestInvestigationView.java
│
├── graphexplorer/
│   ├── GraphExplorerQuery.java
│   ├── GraphExplorerHandler.java
│   ├── GraphExplorerView.java
│   └── SelectedPathView.java
│
├── evidencetimeline/
│   ├── EvidenceTimelineQuery.java
│   ├── EvidenceTimelineHandler.java
│   ├── EvidenceTimelineView.java
│   └── HistoricalDiffView.java
│
├── conflictworkspace/
├── hypothesisworkbench/
├── knowledgegapworkspace/
├── sourceintelligence/
├── watchlistalerts/
├── reports/
└── settings/
```

---

# 33. Public HTTP/API Surface

```text
src/main/java/com/kse/platform/web/
│
├── api/
│   ├── v1/
│   │   ├── entities/
│   │   ├── investigations/
│   │   ├── graph/
│   │   ├── evidence/
│   │   ├── claims/
│   │   ├── conflicts/
│   │   ├── hypotheses/
│   │   ├── knowledgegaps/
│   │   ├── sources/
│   │   ├── watchlists/
│   │   ├── alerts/
│   │   ├── reports/
│   │   └── administration/
│   │
│   └── internal/
│
├── error/
│   ├── GlobalExceptionHandler.java
│   ├── ApiError.java
│   └── ErrorResponseMapper.java
│
├── pagination/
├── filtering/
├── serialization/
└── streaming/
    ├── SseController.java
    ├── WebSocketController.java
    └── SubscriptionRegistry.java
```

---

# 34. Frontend

אותו repository ואותו release artifact.

```text
src/main/ui/
│
├── package.json
├── tsconfig.json
├── vite.config.ts
├── eslint.config.js
│
├── public/
│
└── src/
    ├── app/
    │   ├── App.tsx
    │   ├── router.tsx
    │   ├── providers.tsx
    │   ├── bootstrap.ts
    │   └── error-boundary.tsx
    │
    ├── design-system/
    │   ├── tokens/
    │   │   ├── colors.ts
    │   │   ├── typography.ts
    │   │   ├── spacing.ts
    │   │   ├── radii.ts
    │   │   ├── shadows.ts
    │   │   └── motion.ts
    │   │
    │   ├── components/
    │   │   ├── Button/
    │   │   ├── Input/
    │   │   ├── Select/
    │   │   ├── Modal/
    │   │   ├── Drawer/
    │   │   ├── Card/
    │   │   ├── Table/
    │   │   ├── Tabs/
    │   │   ├── Badge/
    │   │   ├── Tooltip/
    │   │   ├── Popover/
    │   │   ├── Menu/
    │   │   ├── Avatar/
    │   │   ├── Progress/
    │   │   ├── Slider/
    │   │   ├── Timeline/
    │   │   └── Graph/
    │   │
    │   └── icons/
    │
    ├── shell/
    │   ├── AppShell/
    │   ├── Sidebar/
    │   ├── TopBar/
    │   ├── GlobalSearch/
    │   ├── EnvironmentSelector/
    │   ├── NotificationCenter/
    │   └── SystemWatermarkBar/
    │
    ├── features/
    │   ├── command-center/
    │   ├── entities/
    │   ├── investigations/
    │   ├── graph/
    │   ├── timeline/
    │   ├── evidence/
    │   ├── conflicts/
    │   ├── hypotheses/
    │   ├── knowledge-gaps/
    │   ├── sources/
    │   ├── watchlists/
    │   ├── alerts/
    │   ├── reports/
    │   └── settings/
    │
    ├── api/
    │   ├── client.ts
    │   ├── query-client.ts
    │   ├── streaming-client.ts
    │   └── generated/
    │
    ├── state/
    ├── hooks/
    ├── utilities/
    ├── types/
    └── tests/
```

ובתוך כל feature frontend:

```text
features/<feature>/
├── api/
├── components/
├── pages/
├── panels/
├── drawers/
├── tables/
├── charts/
├── hooks/
├── state/
├── models/
├── mappers/
└── tests/
```

---

# 35. Resources

```text
src/main/resources/
│
├── application.yml
│
├── db/
│   └── migration/
│
├── ontology/
│   ├── core/
│   ├── organization/
│   ├── person/
│   ├── finance/
│   ├── geography/
│   └── extensions/
│
├── rules/
│   ├── beliefs/
│   ├── conflicts/
│   ├── identity/
│   ├── hypotheses/
│   └── gaps/
│
├── report-templates/
│
├── schemas/
│
├── prompts/
│   ├── explanation/
│   ├── hypothesis/
│   ├── summarization/
│   └── report/
│
└── static/
    └── ui/
```

---

# 36. Tests — Final Mature Tree

```text
src/test/java/com/kse/
│
├── architecture/
│   ├── ModuleBoundaryTest.java
│   ├── NoCrossDomainAccessTest.java
│   ├── DomainFrameworkIndependenceTest.java
│   ├── InfrastructureIsolationTest.java
│   ├── ApiOnlyCrossModuleAccessTest.java
│   ├── NoSharedRepositoryTest.java
│   ├── EventVersioningTest.java
│   ├── AggregateSizeRulesTest.java
│   └── CompositionBoundaryTest.java
│
├── modules/
│   ├── sources/
│   ├── evidence/
│   ├── observations/
│   ├── claims/
│   ├── identity/
│   ├── entities/
│   ├── events/
│   ├── relations/
│   ├── temporal/
│   ├── provenance/
│   ├── beliefs/
│   ├── conflicts/
│   ├── hypotheses/
│   ├── knowledgegaps/
│   ├── decisions/
│   ├── investigations/
│   ├── graph/
│   ├── search/
│   ├── watchlist/
│   ├── alerts/
│   ├── reports/
│   ├── collaboration/
│   ├── users/
│   ├── tenancy/
│   ├── authorization/
│   ├── audit/
│   └── administration/
│
├── composition/
│
├── contracts/
│   ├── events/
│   ├── api/
│   └── serialization/
│
├── integration/
│   ├── database/
│   ├── messaging/
│   ├── search/
│   ├── graph/
│   ├── cache/
│   └── objectstore/
│
├── endtoend/
│   ├── ObservationToClaimTest.java
│   ├── ClaimToBeliefTest.java
│   ├── BeliefToConflictTest.java
│   ├── IdentityMergePropagationTest.java
│   ├── IdentitySplitPropagationTest.java
│   ├── HistoricalStateReconstructionTest.java
│   ├── InvestigationLiveRefreshTest.java
│   ├── KnowledgeGapResolutionTest.java
│   ├── WatchRuleAlertTest.java
│   └── ReportTraceabilityTest.java
│
├── replay/
│   ├── DeterministicReplayTest.java
│   ├── SnapshotReplayTest.java
│   └── ModelVersionReplayTest.java
│
├── concurrency/
│   ├── DuplicateObservationRaceTest.java
│   ├── ConcurrentClaimUpdateTest.java
│   ├── MergeRaceTest.java
│   └── EventOrderingTest.java
│
├── temporal/
│   ├── LateArrivalTest.java
│   ├── RetroactiveCorrectionTest.java
│   ├── ValidTimeOverlapTest.java
│   └── BitemporalReconstructionTest.java
│
├── performance/
│   ├── IngestionLatencyTest.java
│   ├── ClaimCommitLatencyTest.java
│   ├── EntityResolutionLatencyTest.java
│   ├── BeliefConvergenceTest.java
│   ├── GraphTraversalBenchmarkTest.java
│   └── InvestigationRefreshLatencyTest.java
│
└── chaos/
    ├── DatabaseFailureTest.java
    ├── EventBusFailureTest.java
    ├── CacheFailureTest.java
    ├── GraphProjectionFailureTest.java
    ├── SearchFailureTest.java
    └── ProcessorRestartTest.java
```

וכל module test directory:

```text
<module>/
├── domain/
├── application/
├── contract/
├── persistence/
├── messaging/
└── integration/
```

---

# 37. Test Fixtures

```text
src/test/java/com/kse/testing/
│
├── fixtures/
│   ├── SourceFixture.java
│   ├── EvidenceFixture.java
│   ├── ObservationFixture.java
│   ├── ClaimFixture.java
│   ├── EntityFixture.java
│   ├── BeliefFixture.java
│   ├── ConflictFixture.java
│   ├── HypothesisFixture.java
│   └── InvestigationFixture.java
│
├── fakes/
│   ├── FakeClock.java
│   ├── FakeEventBus.java
│   ├── FakeEventPublisher.java
│   ├── FakeRepository.java
│   └── FakeIdGenerator.java
│
├── builders/
├── assertions/
├── matchers/
├── containers/
└── scenarios/
```

---

# 38. Internal Event Contracts

```text
schemas/events/
│
├── source/
│   ├── source-registered-v1.json
│   ├── source-reliability-changed-v1.json
│   └── source-quarantined-v1.json
│
├── observation/
│   ├── observation-accepted-v1.json
│   └── observation-rejected-v1.json
│
├── claim/
│   ├── claim-created-v1.json
│   ├── claim-invalidated-v1.json
│   └── claim-superseded-v1.json
│
├── identity/
│   ├── identity-resolved-v1.json
│   ├── entity-merge-proposed-v1.json
│   └── entity-split-proposed-v1.json
│
├── entity/
├── belief/
├── conflict/
├── hypothesis/
├── knowledgegap/
├── investigation/
├── alert/
└── report/
```

---

# 39. Operational Storage Ownership

גם אם פיזית יש PostgreSQL אחד:

```text
database schemas
│
├── kse_sources
├── kse_evidence
├── kse_observations
├── kse_claims
├── kse_identity
├── kse_entities
├── kse_events
├── kse_relations
├── kse_temporal
├── kse_provenance
├── kse_beliefs
├── kse_conflicts
├── kse_hypotheses
├── kse_knowledge_gaps
├── kse_decisions
├── kse_investigations
├── kse_graph
├── kse_search
├── kse_watchlist
├── kse_alerts
├── kse_reports
├── kse_users
├── kse_tenancy
├── kse_authorization
├── kse_audit
├── kse_administration
├── kse_outbox
└── kse_inbox
```

אין module שנוגע ב־schema של אחר.

---

# 40. Long-Term Generated / Runtime Areas

לא source code, אבל חלק מהמוצר הסופי:

```text
runtime/
├── snapshots/
├── replay/
├── exports/
├── reports/
├── evidence-cache/
├── graph-cache/
├── search-index/
├── dead-letter/
├── quarantined/
└── diagnostics/
```

---

# 41. Final Conceptual Tree

אם מתעלמים מכל infrastructure ורואים רק את ליבת המערכת:

```text
Knowledge State Engine
│
├── Input Interpretation
│   ├── Sources
│   ├── Evidence
│   ├── Observations
│   └── Claims
│
├── Reality Construction
│   ├── Identity
│   ├── Entities
│   ├── Events
│   ├── Relations
│   └── Temporal
│
├── Epistemic State
│   ├── Provenance
│   ├── Beliefs
│   ├── Conflicts
│   ├── Hypotheses
│   ├── Knowledge Gaps
│   └── Decisions
│
├── Investigation
│   ├── Investigations
│   ├── Graph
│   ├── Search
│   └── Collaboration
│
├── Operational Product
│   ├── Watchlists
│   ├── Alerts
│   └── Reports
│
├── Governance
│   ├── Users
│   ├── Tenancy
│   ├── Authorization
│   ├── Audit
│   └── Administration
│
└── Product Composition
    ├── Command Center
    ├── Entity Intelligence
    ├── Investigation Workspace
    ├── Graph Explorer
    ├── Evidence & Timeline
    ├── Conflict Workspace
    ├── Hypotheses Workbench
    ├── Knowledge Gaps
    ├── Source Intelligence
    ├── Watchlist & Alerts
    ├── Reports & Briefings
    └── Settings
```

---

# 42. הדבר החשוב ביותר לגבי העץ הזה

העץ נראה עצום — ובכוונה, כי ביקשת לראות את מצב הסיום.

אבל אסור לפתח אותו כעץ.

מפתחים אותו כ־vertical slices קטנטנים.

היום repository יכול להכיל:

```text
modules/
├── observations/
└── claims/
```

בעוד שהמסמך הזה אומר לאן אנחנו הולכים.

אחרי שנים יכולים להיות כל 25+ ה־bounded contexts שתיארנו, בלי שה־Claim developer צריך לדעת כיצד Graph Explorer עובד.

העץ הסופי גדול.

**יחידת העבודה היומית נשארת קטנה מאוד.**

וזה בדיוק החיבור בין הארכיטקטורה העצומה של המוצר לבין עקרון ה־Kaizen שלך.

אני חושב שהשלב הנכון הבא הוא לבנות מתוך העץ הזה את **Dependency Constitution**: טבלה חד־משמעית לכל מודול — מה הוא owns,
מה הוא consumes, מה הוא publishes, למי מותר לו לקרוא סינכרונית, ומה אסור לו להכיר בכלל. זה יהיה המסמך שמונע מהעץ היפה
הזה להפוך אחרי שנתיים ל־monolith מסובך.
