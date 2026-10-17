ALTER TABLE claim_lifecycle_revisions
    ADD CONSTRAINT fk_claim_revision_successor
        FOREIGN KEY (related_claim_id) REFERENCES claims(id) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_claim_revision_not_self_successor
        CHECK (related_claim_id IS NULL OR related_claim_id <> claim_id);
