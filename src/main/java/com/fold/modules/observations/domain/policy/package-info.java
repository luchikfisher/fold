/**
 * Domain policies governing observation admission mechanics that do not belong
 * to a single observation aggregate.
 *
 * <p>Fingerprinting defines deterministic semantic identity for duplicate
 * detection. Deduplication determines whether that identity is already present
 * in the authoritative observation store.</p>
 *
 * <p>Policies in this package remain independent of Spring, HTTP, JSON, JPA,
 * and database-specific behavior.</p>
 */
package com.fold.modules.observations.domain.policy;