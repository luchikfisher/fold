package com.fold.modules.claims.infrastructure.persistence.query;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.query.FindClaimsQuery;
import com.fold.modules.claims.api.view.ClaimSearchPage;
import com.fold.modules.claims.api.view.ClaimSummaryView;
import com.fold.modules.claims.api.view.ClaimView;
import com.fold.modules.claims.application.query.ClaimDiscoveryReader;
import com.fold.modules.claims.application.query.ClaimViewMapper;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/** Single-statement reads from Claims-owned authoritative tables; no aggregate hydration. */
@Repository
public class PostgresClaimDiscoveryReader implements ClaimDiscoveryReader {
    private final NamedParameterJdbcTemplate jdbc;
    private final ClaimObjectJsonCodec objects;
    private final ClaimCursorCodec cursors = new ClaimCursorCodec();

    public PostgresClaimDiscoveryReader(NamedParameterJdbcTemplate jdbc, ClaimObjectJsonCodec objects) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.objects = Objects.requireNonNull(objects);
    }

    @Override
    public ClaimSearchPage find(FindClaimsQuery query) {
        Objects.requireNonNull(query);
        var filters = query.filters();
        var parameters = new MapSqlParameterSource("limit", query.limit() + 1);
        StringBuilder sql = new StringBuilder("""
                SELECT c.id, c.subject_namespace, c.subject_value, c.predicate,
                       c.object_payload::text AS object_payload, c.derivation_kind,
                       c.derivation_method, c.derivation_version, c.confidence,
                       c.observed_at, c.valid_from, c.valid_to, c.created_at, c.status, c.revision
                FROM claims c
                WHERE true
                """);
        if (filters.subject() != null) {
            sql.append(" AND c.subject_namespace = :namespace AND c.subject_value = :subject");
            parameters.addValue("namespace", filters.subject().namespace())
                    .addValue("subject", filters.subject().value());
        }
        if (filters.predicate() != null) {
            sql.append(" AND c.predicate = :predicate");
            parameters.addValue("predicate", filters.predicate());
        }
        if (filters.status() != null) {
            sql.append(" AND c.status = :status");
            parameters.addValue("status", filters.status());
        }
        if (filters.origin() != null) {
            sql.append("""
                     AND EXISTS (SELECT 1 FROM claim_origin_references o
                                 WHERE o.claim_id = c.id AND o.origin_kind = :originKind
                                   AND o.reference_id = :originId)
                    """);
            parameters.addValue("originKind", filters.origin().kind())
                    .addValue("originId", filters.origin().referenceId());
        }
        if (filters.successorClaimId() != null) {
            sql.append("""
                     AND EXISTS (SELECT 1 FROM claim_lifecycle_revisions r
                                 WHERE r.claim_id = c.id AND r.transition = 'SUPERSEDED'
                                   AND r.related_claim_id = :successorId)
                    """);
            parameters.addValue("successorId", filters.successorClaimId().value());
        }
        if (query.cursor() != null) {
            var position = cursors.decode(filters, query.cursor());
            sql.append(" AND (c.created_at, c.id) < (:createdAt, :id)");
            parameters.addValue("createdAt", position.createdAt().value().atOffset(ZoneOffset.UTC))
                    .addValue("id", position.id().value());
        }
        sql.append(" ORDER BY c.created_at DESC, c.id DESC LIMIT :limit");
        var found = jdbc.query(sql.toString(), parameters, (rs, row) -> summary(rs));
        boolean hasMore = found.size() > query.limit();
        var items = hasMore ? found.subList(0, query.limit()) : found;
        var last = items.isEmpty() ? null : items.getLast();
        var next = hasMore ? cursors.encode(filters,
                new ClaimCursorCodec.Position(last.createdAt(), last.id())) : null;
        return new ClaimSearchPage(items, next);
    }

    private ClaimSummaryView summary(ResultSet rs) throws SQLException {
        Timestamp start = time(rs, "valid_from");
        return new ClaimSummaryView(new ClaimId(rs.getObject("id", UUID.class)),
                new ClaimView.Subject(rs.getString("subject_namespace"), rs.getString("subject_value")),
                rs.getString("predicate"), ClaimViewMapper.mapObject(objects.decode(rs.getString("object_payload"))),
                new ClaimView.Derivation(rs.getString("derivation_kind"), rs.getString("derivation_method"),
                        rs.getInt("derivation_version")),
                (Double) rs.getObject("confidence"), time(rs, "observed_at"),
                start == null ? null : new ClaimView.Validity(start, time(rs, "valid_to")),
                // Read the ordering key through the same proleptic calendar as its cursor binding.
                Timestamp.of(rs.getObject("created_at", OffsetDateTime.class).toInstant()),
                rs.getString("status"), rs.getLong("revision"));
    }

    private Timestamp time(ResultSet rs, String column) throws SQLException {
        var value = rs.getTimestamp(column);
        return value == null ? null : Timestamp.of(value.toInstant());
    }
}
