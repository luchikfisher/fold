package com.fold.kernel.result;

import java.util.function.Function;

/**
 * Represents either a successful value or an expected domain failure.
 *
 * <p>Result is intended for operations whose failure is part of the normal
 * domain contract. It must not be used to hide programming errors or
 * unexpected infrastructure exceptions.</p>
 *
 * @param <T> the success value type
 */
public sealed interface Result<T>
        permits Success, Failure {

    /**
     * Creates a successful result.
     *
     * @param value the success value
     * @param <T> the value type
     * @return a successful result
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed result.
     *
     * @param error the domain error
     * @param <T> the expected success type
     * @return a failed result
     */
    static <T> Result<T> failure(DomainError error) {
        return new Failure<>(error);
    }

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Reduces this result to one value by handling the success and failure
     * cases explicitly.
     *
     * @param onSuccess function applied to a successful value
     * @param onFailure function applied to a domain error
     * @param <R> the resulting type
     * @return the mapped result
     */
    <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super DomainError, ? extends R> onFailure
    );

    /**
     * Transforms the success value while preserving a failure unchanged.
     *
     * @param mapper success-value transformation
     * @param <R> the transformed success type
     * @return the mapped result
     */
    <R> Result<R> map(
            Function<? super T, ? extends R> mapper
    );
}