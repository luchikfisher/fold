/**
 * Claims-domain value objects.
 *
 * <p>The Claims bounded context represents explicit assertions about the
 * world. A Claim is conceptually distinct from both an Observation and a
 * Belief:</p>
 *
 * <pre>
 * Observation = what FOLD received
 * Claim       = an assertion extracted, derived, inferred, or entered
 * Belief      = FOLD's assessed position about a proposition
 * </pre>
 *
 * <p>The value model in this package preserves several foundational
 * distinctions:</p>
 *
 * <ul>
 *     <li>claim identity is not proposition equivalence;</li>
 *     <li>proposition equivalence is not processing idempotency;</li>
 *     <li>claim references do not require canonical entity resolution;</li>
 *     <li>origin is distinct from derivation;</li>
 *     <li>derivation confidence is distinct from aggregate belief confidence;</li>
 *     <li>observed time is distinct from valid time and creation time;</li>
 *     <li>lifecycle state is distinct from derived epistemic conditions;</li>
 *     <li>claim proposition semantics are immutable.</li>
 * </ul>
 *
 * <p>This package must remain independent of Observations internals,
 * persistence technologies, web frameworks, serialization libraries, and
 * Spring.</p>
 */
package com.fold.modules.claims.domain.value;