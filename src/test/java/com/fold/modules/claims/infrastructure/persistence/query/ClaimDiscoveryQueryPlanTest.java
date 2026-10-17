package com.fold.modules.claims.infrastructure.persistence.query;

import com.fold.modules.claims.api.query.*;
import com.fold.modules.claims.api.view.*;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.testsupport.claims.ClaimLifecycleIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the actual adapter SQL against selective and deliberately skewed data. */
class ClaimDiscoveryQueryPlanTest extends ClaimLifecycleIntegrationTestSupport {
    @Test
    void inspectRepresentativePlansIncludingSparseStatusAndDeepContinuation() {
        var successor = persistClaim();
        // 30,000 distinct occurrences, 1,000 subjects/origins, two predicates.
        // INVALIDATED occurs only near the oldest end, defeating an assumption
        // that a small LIMIT always means little work in creation-time order.
        jdbc.update("""
                INSERT INTO claims (id, subject_namespace, subject_value, predicate, object_payload,
                    derivation_kind, derivation_method, derivation_version, created_at, status, revision)
                SELECT md5('discovery-plan-' || g)::uuid, 'registry', 'subject-' || (g % 1000),
                    CASE WHEN g % 2 = 0 THEN 'country' ELSE 'name' END,
                    '{"type":"text","value":"value"}'::jsonb, 'OBSERVED', 'plan-fixture', 1,
                    TIMESTAMPTZ '2026-10-01 00:00:00+00' + g * INTERVAL '1 second',
                    CASE WHEN g % 1000 = 0 THEN 'SUPERSEDED' WHEN g <= 1000 THEN 'INVALIDATED' ELSE 'ACTIVE' END,
                    CASE WHEN g % 1000 = 0 OR g <= 1000 THEN 2 ELSE 1 END
                FROM generate_series(1, 30000) g
                """);
        jdbc.update("""
                INSERT INTO claim_origin_references (claim_id, origin_kind, reference_id)
                SELECT md5('discovery-plan-' || g)::uuid, 'SOURCE', md5('origin-' || (g % 1000))::uuid
                FROM generate_series(1, 30000) g
                """);
        jdbc.update("""
                INSERT INTO claim_lifecycle_revisions (claim_id, revision, transition, status, changed_at)
                SELECT id, 1, 'CREATED', 'ACTIVE', created_at FROM claims WHERE derivation_method = 'plan-fixture'
                """);
        jdbc.update("""
                INSERT INTO claim_lifecycle_revisions
                    (claim_id, revision, transition, status, changed_at, related_claim_id, reason_code, reason_message)
                SELECT id, 2, status, status, TIMESTAMPTZ '2026-11-01 00:00:00+00',
                    CASE WHEN status = 'SUPERSEDED' THEN ?::uuid ELSE NULL END, 'REVIEW', 'Explicit decision'
                FROM claims WHERE derivation_method = 'plan-fixture' AND revision = 2
                """, successor.id().value());
        jdbc.execute("ANALYZE claims");
        jdbc.execute("ANALYZE claim_origin_references");
        jdbc.execute("ANALYZE claim_lifecycle_revisions");

        var actualJdbc = new NamedParameterJdbcTemplate(jdbc);
        var capturedJdbc = spy(actualJdbc);
        AtomicReference<String> sql = new AtomicReference<>();
        AtomicReference<SqlParameterSource> parameters = new AtomicReference<>();
        doAnswer(invocation -> {
            sql.set(invocation.getArgument(0));
            parameters.set(invocation.getArgument(1));
            return invocation.callRealMethod();
        }).when(capturedJdbc).query(anyString(), any(SqlParameterSource.class), ArgumentMatchers.<RowMapper<ClaimSummaryView>>any());
        var reader = new PostgresClaimDiscoveryReader(capturedJdbc, new ClaimObjectJsonCodec(objectMapper));
        UUID origin = jdbc.queryForObject("SELECT md5('origin-0')::uuid", UUID.class);
        var source = new ClaimOriginReferenceView("SOURCE", origin);
        var subject = new ClaimView.Subject("registry", "subject-0");
        var workloads = new LinkedHashMap<String, ClaimFilters>();
        workloads.put("first page", ClaimFilters.all());
        workloads.put("sparse oldest status", new ClaimFilters(null, null, "INVALIDATED", null, null));
        workloads.put("subject and predicate", new ClaimFilters(subject, "country", null, null, null));
        workloads.put("direct origin", new ClaimFilters(null, null, null, source, null));
        workloads.put("historical successor", new ClaimFilters(null, null, null, null, successor.id()));
        workloads.put("all five", new ClaimFilters(subject, "country", "SUPERSEDED", source, successor.id()));
        for (var workload : workloads.entrySet()) {
            var page = reader.find(new FindClaimsQuery(workload.getValue(), 20, null));
            assertThat(page.items()).hasSize(20);
            explain(actualJdbc, workload.getKey(), sql.get(), parameters.get());
        }
        // Obtain a real late-page cursor, rather than inventing a token in a test.
        com.fold.kernel.pagination.Cursor cursor = null;
        for (int page = 0; page < 290; page++) {
            cursor = reader.find(new FindClaimsQuery(ClaimFilters.all(), 100, cursor)).nextCursor();
        }
        assertThat(cursor).isNotNull();
        assertThat(reader.find(new FindClaimsQuery(ClaimFilters.all(), 20, cursor)).items()).hasSize(20);
        explain(actualJdbc, "deep continuation", sql.get(), parameters.get());

        // Large equal-time batches require the UUID component in the index,
        // not an incremental sort of every row sharing the timestamp.
        jdbc.update("UPDATE claims SET created_at = TIMESTAMPTZ '2026-10-02 00:00:00+00'");
        jdbc.update("UPDATE claim_lifecycle_revisions SET changed_at = TIMESTAMPTZ '2026-10-02 00:00:00+00' WHERE revision = 1");
        jdbc.execute("ANALYZE claims");
        var ties = reader.find(new FindClaimsQuery(ClaimFilters.all(), 20, null));
        assertThat(ties.items()).hasSize(20);
        explain(actualJdbc, "large equal-timestamp batch", sql.get(), parameters.get());
        assertThat(reader.find(new FindClaimsQuery(ClaimFilters.all(), 20, ties.nextCursor())).items()).hasSize(20);
        explain(actualJdbc, "equal-timestamp continuation", sql.get(), parameters.get());
    }

    private void explain(NamedParameterJdbcTemplate template, String workload, String sql, SqlParameterSource parameters) {
        var lines = template.queryForList("EXPLAIN (ANALYZE, BUFFERS) " + sql, parameters, String.class);
        // Plans are diagnostic evidence, not brittle assertions about optimizer choices.
        System.out.println("Claim discovery plan: " + workload + "\n" + String.join("\n", lines));
        assertThat(lines).anyMatch(line -> line.contains("Execution Time"));
    }
}
