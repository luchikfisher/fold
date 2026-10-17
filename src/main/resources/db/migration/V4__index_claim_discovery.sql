-- Replace the single-column indexes with indexes that also support the
-- deterministic keyset order. Their leading columns preserve existing lookups.
CREATE INDEX ix_claims_discovery_order ON claims (created_at DESC, id DESC);
CREATE INDEX ix_claims_status_discovery_order ON claims (status, created_at DESC, id DESC);
DROP INDEX ix_claims_created_at;
DROP INDEX ix_claims_status;

-- Direct historical predecessor lookup. The foreign key alone supplies no index.
CREATE INDEX ix_claim_revisions_successor
    ON claim_lifecycle_revisions (related_claim_id, claim_id)
    WHERE transition = 'SUPERSEDED';
