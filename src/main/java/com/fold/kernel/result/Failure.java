package com.fold.kernel.result;

import java.util.Objects;
import java.util.function.Function;

/**
 * A failed {@link Result} containing an expected domain error.
 *
 * @param error the represented domain failure
 * @param <T>   the success type that would otherwise have been produced
 */
public record Failure<T>(DomainError error)
        implements Result<T> {

    public Failure {
        Objects.requireNonNull(error, "error must not be null");
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super DomainError, ? extends R> onFailure
    ) {
        Objects.requireNonNull(onSuccess, "onSuccess must not be null");
        Objects.requireNonNull(onFailure, "onFailure must not be null");

        return onFailure.apply(error);
    }

    @Override
    public <R> Result<R> map(
            Function<? super T, ? extends R> mapper
    ) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return Result.failure(error);
    }
}