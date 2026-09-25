package com.fold.kernel.ids;

import java.io.Serializable;

/**
 * Identifies a domain object by an immutable, strongly typed value.
 *
 * <p>A domain identifier carries identity only. It must not contain mutable
 * state, business behavior, or information derived from the object it
 * identifies.</p>
 *
 * <p>The concrete representation is intentionally left to the implementation.
 * Consumers must treat the value as an opaque identifier and must not infer
 * domain meaning from its internal structure.</p>
 *
 * @param <T> the concrete identifier value type
 */
public interface DomainId<T> extends Serializable {

    /**
     * Returns the immutable value that represents this identifier.
     *
     * @return the identifier value
     */
    T value();

    /**
     * Returns the stable textual representation of this identifier.
     *
     * @return the identifier as text
     */
    default String asString() {
        return value().toString();
    }
}