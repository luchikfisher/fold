/**
 * Domain objects representing observations received by FOLD.
 *
 * <p>An observation preserves normalized semantic input before FOLD resolves
 * identity, creates claims, calculates beliefs, detects conflicts, or infers
 * relationships.</p>
 *
 * <p>{@link com.fold.modules.observations.domain.model.Observation} is the
 * aggregate root of the bounded context. Its semantic content is immutable
 * after receipt. The aggregate owns only the admission lifecycle from
 * {@code RECEIVED} to either {@code ACCEPTED} or {@code REJECTED}.</p>
 *
 * <p>Objects in this package describe what FOLD received. They do not describe
 * what FOLD ultimately believes to be true about the modeled world.</p>
 */
package com.fold.modules.observations.domain.model;