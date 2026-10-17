package com.fold.modules.claims.infrastructure.persistence.query;

import com.fold.kernel.ids.*;
import com.fold.kernel.pagination.Cursor;
import com.fold.kernel.time.Timestamp;
import com.fold.kernel.time.ValidTime;
import com.fold.modules.claims.api.query.*;
import com.fold.modules.claims.api.view.*;
import com.fold.modules.claims.application.feature.lifecycle.*;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.*;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.testsupport.claims.ClaimLifecycleIntegrationTestSupport;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClaimDiscoveryIntegrationTest extends ClaimLifecycleIntegrationTestSupport {
    @Test
    void unsupportedCursorTimestampsShouldFailBeforeExecutingSqlIncludingSilentJdbcOverflow() {
        Instant ordinary = Instant.parse("2026-10-03T10:00:00Z");
        Instant overflow = ordinary.plusSeconds(18_446_744_073_709_552L);
        // Reproduce the reviewed defect: a valid far-future Instant silently aliases a modern date.
        assertThat(java.sql.Timestamp.from(overflow).toInstant()).isEqualTo(ordinary);
        var template = mock(NamedParameterJdbcTemplate.class);
        var reader = new PostgresClaimDiscoveryReader(template, new ClaimObjectJsonCodec(objectMapper));
        var codec = new ClaimCursorCodec();
        var valid = codec.encode(ClaimFilters.all(), new ClaimCursorCodec.Position(Timestamp.of(ordinary), ClaimId.random()));
        for (Instant unsupported : List.of(overflow, Instant.MIN, Instant.MAX,
                Instant.parse("-4713-12-31T23:59:59.999999Z"),
                Instant.parse("+294277-01-01T00:00:00Z"), Instant.parse("+300000-01-01T00:00:00Z"))) {
            byte[] bytes = Base64.getUrlDecoder().decode(valid.value());
            ByteBuffer.wrap(bytes).putLong(33, unsupported.getEpochSecond()).putInt(41, unsupported.getNano() / 1_000);
            var cursor = new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
            assertThatThrownBy(() -> reader.find(new FindClaimsQuery(ClaimFilters.all(), 1, cursor)))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid or unsupported Claim cursor");
        }
        verifyNoInteractions(template);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-4712-01-01T00:00:00Z", "-4712-01-01T00:00:00.000001Z",
            "0000-01-01T00:00:00.123456Z", "1500-01-01T00:00:00.123456Z",
            "1582-10-15T00:00:00Z", "1969-12-31T23:59:59.999999Z",
            "+294276-12-31T23:59:59.999998Z", "+294276-12-31T23:59:59.999999Z"})
    void cursorBoundariesShouldRemainExactThroughPostgresBindingAndContinuation(String value) {
        Timestamp time = Timestamp.parse(value);
        var bound = time.value().atOffset(ZoneOffset.UTC);
        // Check the exact PostgreSQL wire value (signed microseconds since 2000),
        // without EXTRACT(epoch)'s numeric division rounding near the upper limit.
        long micros = ChronoUnit.MICROS.between(Instant.parse("2000-01-01T00:00:00Z"), time.value());
        String expectedBytes = HexFormat.of().formatHex(ByteBuffer.allocate(Long.BYTES).putLong(micros).array());
        assertThat(jdbc.queryForObject("SELECT encode(timestamptz_send(CAST(? AS timestamptz)), 'hex')",
                String.class, bound)).isEqualTo(expectedBytes);
        // Seed the database key directly: legacy aggregate persistence is not the cursor binding path.
        var ids = List.of(new ClaimId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")),
                new ClaimId(UUID.fromString("80000000-0000-0000-0000-000000000000")),
                new ClaimId(UUID.fromString("00000000-0000-0000-0000-000000000001")));
        for (var id : ids) {
            jdbc.update("""
                    INSERT INTO claims (id, subject_namespace, subject_value, predicate, object_payload,
                        derivation_kind, derivation_method, derivation_version, created_at, status, revision)
                    VALUES (?, 'registry', 'subject', 'predicate', '{"type":"text","value":"value"}'::jsonb,
                        'OBSERVED', 'cursor-fixture', 1, ?, 'ACTIVE', 1)
                    """, id.value(), bound);
        }
        Cursor cursor = null;
        for (int i = 0; i < ids.size(); i++) {
            var page = find(ClaimFilters.all(), 1, cursor);
            assertThat(page.items()).extracting(ClaimSummaryView::id).containsExactly(ids.get(i));
            assertThat(page.items().getFirst().createdAt()).isEqualTo(time);
            cursor = page.nextCursor();
            if (i < ids.size() - 1) {
                assertThat(cursor).isNotNull();
            } else {
                assertThat(cursor).isNull();
            }
        }
    }

    @Test
    void emptyAndExactLimitPagesShouldHaveNoContinuationAndItemsShouldBeImmutable() {
        assertThat(find(ClaimFilters.all(), 1, null).items()).isEmpty();
        assertThat(find(ClaimFilters.all(), 1, null).nextCursor()).isNull();
        persistClaim();
        var page = find(ClaimFilters.all(), 1, null);
        assertThat(page.items()).hasSize(1);
        assertThat(page.nextCursor()).isNull();
        assertThatThrownBy(() -> page.items().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldPreserveDistinctOccurrencesAndPostgresUuidOrderAcrossTimestampTies() {
        var ids = List.of("00000000-0000-0000-0000-000000000001", "7fffffff-ffff-ffff-ffff-ffffffffffff",
                "80000000-0000-0000-0000-000000000000", "ffffffff-ffff-ffff-ffff-ffffffffffff");
        Timestamp time = Timestamp.parse("2026-10-03T10:00:00.123456789Z");
        for (String id : ids) {
            save(new ClaimId(UUID.fromString(id)), time, ClaimTestFixtures.proposition(), ClaimTestFixtures.origin());
        }
        Claim newer = save(ClaimId.random(), Timestamp.parse("2026-10-03T10:00:00.123457Z"),
                ClaimTestFixtures.proposition(), ClaimTestFixtures.origin());
        List<ClaimId> found = new ArrayList<>();
        Cursor cursor = null;
        do {
            var page = find(ClaimFilters.all(), found.isEmpty() ? 1 : 2, cursor);
            found.addAll(page.items().stream().map(ClaimSummaryView::id).toList());
            cursor = page.nextCursor();
        } while (cursor != null);
        assertThat(found).containsExactly(newer.id(), new ClaimId(UUID.fromString(ids.get(3))),
                new ClaimId(UUID.fromString(ids.get(2))), new ClaimId(UUID.fromString(ids.get(1))),
                new ClaimId(UUID.fromString(ids.get(0))));
    }

    @Test
    void summariesShouldMatchDetailForEveryObjectTypeAndNullableCreationContext() {
        var types = List.of(new ClaimObject.Text(""), ClaimObject.NumberValue.of("1.0000"),
                new ClaimObject.BooleanValue(false), ClaimTestFixtures.referenceObject(),
                new ClaimObject.TimestampValue(Timestamp.parse("1960-01-01T00:00:00.123456789Z")),
                new ClaimObject.Identifier("scheme", "id"));
        for (var object : types) {
            var proposition = new ClaimProposition(ClaimTestFixtures.proposition().subject(),
                    ClaimTestFixtures.proposition().predicate(), object);
            save(ClaimId.random(), ClaimTestFixtures.CREATED_AT, proposition, ClaimTestFixtures.origin());
        }
        Claim populated = persistClaim();
        Claim openEnded = Claim.createNew(ClaimTestFixtures.proposition(), ClaimTestFixtures.origin(),
                ClaimTestFixtures.derivation(), null, null, ValidTime.from(ClaimTestFixtures.CREATED_AT),
                ClaimTestFixtures.CREATED_AT).claim();
        tx().executeWithoutResult(status -> repository.add(openEnded));
        var page = find(ClaimFilters.all(), 100, null);
        assertThat(page.items()).hasSize(8);
        for (var summary : page.items()) {
            var detail = queries.get(new GetClaimQuery(summary.id())).orElseThrow();
            assertThat(summary).usingRecursiveComparison().isEqualTo(new ClaimSummaryView(detail.id(), detail.subject(),
                    detail.predicate(), detail.object(), detail.derivation(), detail.confidence(), detail.observedAt(),
                    detail.validTime(), detail.createdAt(), detail.status(), detail.revision()));
        }
        assertThat(page.items().stream().filter(s -> s.id().equals(populated.id())).findFirst().orElseThrow().observedAt())
                .isEqualTo(ClaimTestFixtures.OBSERVED_AT);
    }

    @Test
    void subjectPredicateAndOriginShouldBeExactDirectAndConjunctiveWithoutDuplicates() {
        UUID sharedId = UUID.randomUUID();
        var origin = ClaimOrigin.of(new ClaimOriginReference.Source(new SourceId(sharedId)),
                new ClaimOriginReference.Evidence(new EvidenceId(sharedId)),
                new ClaimOriginReference.Observation(ObservationId.random()));
        Claim a = save(ClaimId.random(), ClaimTestFixtures.CREATED_AT, ClaimTestFixtures.proposition(), origin);
        Claim b = save(ClaimId.random(), ClaimTestFixtures.CREATED_AT,
                new ClaimProposition(ClaimSubject.of("company-registry", "company-123"), ClaimPredicate.of("other"),
                        new ClaimObject.Text("Germany")), ClaimOrigin.of(new ClaimOriginReference.Claim(a.id())));
        save(ClaimId.random(), ClaimTestFixtures.CREATED_AT,
                new ClaimProposition(ClaimSubject.of("other-namespace", "company-123"),
                        ClaimTestFixtures.proposition().predicate(), new ClaimObject.Text("Germany")),
                ClaimOrigin.of(new ClaimOriginReference.Evidence(new EvidenceId(sharedId))));
        var source = new ClaimOriginReferenceView("SOURCE", sharedId);
        var subject = new ClaimView.Subject("company-registry", "company-123");
        assertThat(find(new ClaimFilters(null, null, null, source, null), 100, null).items())
                .extracting(ClaimSummaryView::id).containsExactly(a.id());
        assertThat(find(new ClaimFilters(subject, "registered-country", "ACTIVE", source, null), 100, null).items())
                .extracting(ClaimSummaryView::id).containsExactly(a.id());
        assertThat(find(new ClaimFilters(subject, "REGISTERED-COUNTRY", null, null, null), 100, null).items()).isEmpty();
        assertThat(find(new ClaimFilters(subject, "registered-country ", null, null, null), 100, null).items()).isEmpty();
        assertThat(find(new ClaimFilters(new ClaimView.Subject("COMPANY-REGISTRY", "company-123"),
                null, null, null, null), 100, null).items()).isEmpty();
        assertThat(find(new ClaimFilters(null, null, null, new ClaimOriginReferenceView("CLAIM", a.id().value()), null),
                100, null).items()).extracting(ClaimSummaryView::id).containsExactly(b.id());
        assertThat(find(new ClaimFilters(null, "x' OR true --", null, null, null), 100, null).items()).isEmpty();
    }

    @Test
    void successorFilterShouldCombineAllFiveFiltersAndRemainHistorical() {
        Claim a = persistClaim(); Claim b = persistClaim(); Claim c = persistClaim(); Claim d = persistClaim();
        useCase.handle(new SupersedeClaimCommand(a.id(), INITIAL, b.id(), null, null));
        useCase.handle(new SupersedeClaimCommand(d.id(), INITIAL, b.id(), REASON, null));
        useCase.handle(new SupersedeClaimCommand(b.id(), INITIAL, c.id(), null, null));
        useCase.handle(new InvalidateClaimCommand(c.id(), INITIAL, REASON, null));
        var detail = queries.get(new GetClaimQuery(a.id())).orElseThrow();
        var filters = new ClaimFilters(detail.subject(), detail.predicate(), "SUPERSEDED", detail.origin().getFirst(), b.id());
        var page = find(filters, 10, null);
        assertThat(page.items()).extracting(ClaimSummaryView::id).containsExactly(a.id());
        assertThat(page.items().getFirst().revision()).isEqualTo(2);
        assertThat(find(new ClaimFilters(null, null, null, null, b.id()), 10, null).items())
                .extracting(ClaimSummaryView::id).containsExactlyInAnyOrder(a.id(), d.id());
        assertThat(find(new ClaimFilters(null, null, null, null, c.id()), 10, null).items())
                .extracting(ClaimSummaryView::id).containsExactly(b.id());
        assertThat(find(new ClaimFilters(null, null, "ACTIVE", null, b.id()), 10, null).items()).isEmpty();
        assertThat(find(new ClaimFilters(null, null, null, null, ClaimId.random()), 10, null).items()).isEmpty();
        assertThat(find(ClaimFilters.all(), 10, null).items()).hasSize(4);
    }

    @Test
    void cursorMisuseShouldFailBeforeExecutingSqlAndAllowDifferentPageSize() {
        persistClaim(); persistClaim(); persistClaim();
        var first = find(ClaimFilters.all(), 1, null);
        assertThat(find(ClaimFilters.all(), 100, first.nextCursor()).items()).hasSize(2);
        var template = mock(NamedParameterJdbcTemplate.class);
        var reader = new PostgresClaimDiscoveryReader(template, new ClaimObjectJsonCodec(objectMapper));
        assertThatThrownBy(() -> reader.find(new FindClaimsQuery(ClaimFilters.all(), 1, new Cursor("x"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reader.find(new FindClaimsQuery(new ClaimFilters(null, null, "ACTIVE", null, null),
                1, first.nextCursor()))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(template);
    }

    @Test
    void continuationShouldBeLiveAndPositionShouldSurviveAnchorLifecycleChanges() {
        Claim older = save(ClaimId.random(), Timestamp.parse("2026-10-01T00:00:00Z"),
                ClaimTestFixtures.proposition(), ClaimTestFixtures.origin());
        Claim anchor = save(ClaimId.random(), Timestamp.parse("2026-10-03T00:00:00Z"),
                ClaimTestFixtures.proposition(), ClaimTestFixtures.origin());
        var active = new ClaimFilters(null, null, "ACTIVE", null, null);
        var first = find(active, 1, null);
        assertThat(first.items().getFirst().id()).isEqualTo(anchor.id());
        useCase.handle(new InvalidateClaimCommand(anchor.id(), INITIAL, REASON, null));
        useCase.handle(new InvalidateClaimCommand(older.id(), INITIAL, REASON, null));
        save(ClaimId.random(), Timestamp.parse("2026-10-04T00:00:00Z"),
                ClaimTestFixtures.proposition(), ClaimTestFixtures.origin());
        Claim lateCommit = save(ClaimId.random(), Timestamp.parse("2026-10-02T00:00:00Z"),
                ClaimTestFixtures.proposition(), ClaimTestFixtures.origin());
        var next = find(active, 10, first.nextCursor());
        assertThat(next.items()).extracting(ClaimSummaryView::id).containsExactly(lateCommit.id());
        assertThat(next.nextCursor()).isNull();
        useCase.handle(new ReinstateClaimCommand(older.id(), new ClaimRevision(2), REASON, null));
        assertThat(find(active, 10, first.nextCursor()).items()).extracting(ClaimSummaryView::id)
                .containsExactly(lateCommit.id(), older.id());
    }

    @Test
    void oneStatementShouldKeepWholePageCoherentWhileLifecycleCommitsDuringMapping() throws Exception {
        Claim a = persistClaim(); Claim b = persistClaim();
        CountDownLatch firstMapped = new CountDownLatch(1);
        CountDownLatch committed = new CountDownLatch(1);
        AtomicInteger statements = new AtomicInteger();
        var paused = spy(new NamedParameterJdbcTemplate(jdbc));
        doAnswer(invocation -> {
            statements.incrementAndGet();
            RowMapper<ClaimSummaryView> original = invocation.getArgument(2);
            invocation.getArguments()[2] = (RowMapper<ClaimSummaryView>) (rs, row) -> {
                var summary = original.mapRow(rs, row);
                if (row == 0) {
                    firstMapped.countDown();
                    try {
                        assertThat(committed.await(10, TimeUnit.SECONDS)).isTrue();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                }
                return summary;
            };
            return invocation.callRealMethod();
        }).when(paused).query(anyString(), any(SqlParameterSource.class), ArgumentMatchers.<RowMapper<ClaimSummaryView>>any());
        var reader = new PostgresClaimDiscoveryReader(paused, new ClaimObjectJsonCodec(objectMapper));
        var filters = new ClaimFilters(null, null, "ACTIVE", null, null);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var future = executor.submit(() -> reader.find(new FindClaimsQuery(filters, 10, null)));
            try {
                assertThat(firstMapped.await(10, TimeUnit.SECONDS)).isTrue();
                useCase.handle(new InvalidateClaimCommand(a.id(), INITIAL, REASON, null));
                useCase.handle(new InvalidateClaimCommand(b.id(), INITIAL, REASON, null));
                committed.countDown();
                var page = future.get(10, TimeUnit.SECONDS);
                assertThat(page.items()).hasSize(2).allSatisfy(item -> {
                    assertThat(item.status()).isEqualTo("ACTIVE");
                    assertThat(item.revision()).isEqualTo(1);
                });
                assertThat(statements).hasValue(1);
                assertThat(find(filters, 10, null).items()).isEmpty();
                assertThat(find(new ClaimFilters(null, null, "INVALIDATED", null, null), 10, null).items())
                        .hasSize(2).allSatisfy(item -> assertThat(item.revision()).isEqualTo(2));
            } finally {
                committed.countDown();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void discoveryMustNotExposeUncommittedStatusOrSuccessorAndMustRespectRollback(boolean rollback) throws Exception {
        Claim predecessor = persistClaim(); Claim successor = persistClaim();
        CountDownLatch applied = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        var lineage = new ClaimFilters(null, null, null, null, successor.id());
        try (var executor = Executors.newSingleThreadExecutor()) {
            var writer = executor.submit(() -> tx().executeWithoutResult(status -> {
                assertThat(useCase.handle(new SupersedeClaimCommand(predecessor.id(), INITIAL, successor.id(), null, null)))
                        .isInstanceOf(ClaimLifecycleResult.Applied.class);
                applied.countDown();
                try {
                    assertThat(release.await(10, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                if (rollback) status.setRollbackOnly();
            }));
            try {
                assertThat(applied.await(10, TimeUnit.SECONDS)).isTrue();
                assertThat(find(lineage, 10, null).items()).isEmpty();
                assertThat(find(ClaimFilters.all(), 10, null).items()).hasSize(2).allSatisfy(item -> {
                    assertThat(item.status()).isEqualTo("ACTIVE");
                    assertThat(item.revision()).isEqualTo(1);
                });
                release.countDown();
                writer.get(10, TimeUnit.SECONDS);
                var after = find(lineage, 10, null);
                if (rollback) {
                    assertThat(after.items()).isEmpty();
                } else {
                    assertThat(after.items()).extracting(ClaimSummaryView::id).containsExactly(predecessor.id());
                    assertThat(after.items().getFirst().status()).isEqualTo("SUPERSEDED");
                    assertThat(after.items().getFirst().revision()).isEqualTo(2);
                }
            } finally {
                release.countDown();
            }
        }
    }

    private ClaimSearchPage find(ClaimFilters filters, int limit, Cursor cursor) {
        return queries.find(new FindClaimsQuery(filters, limit, cursor));
    }

    private Claim save(ClaimId id, Timestamp createdAt, ClaimProposition proposition, ClaimOrigin origin) {
        Claim claim = Claim.create(id, proposition, origin, ClaimTestFixtures.derivation(), null, null, null, createdAt).claim();
        tx().executeWithoutResult(status -> repository.add(claim));
        return claim;
    }
}
