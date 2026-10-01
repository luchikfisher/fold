package com.fold.kernel.result;

import java.util.Objects;
import java.util.function.Function;

/**
 * A successful {@link Result}.
 *
 * @param value the produced value
 * @param <T>   the value type
 */
public record Success<T>(T value) implements Result<T> {

    public Success {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super DomainError, ? extends R> onFailure
    ) {
        Objects.requireNonNull(onSuccess, "onSuccess must not be null");
        Objects.requireNonNull(onFailure, "onFailure must not be null");

        return onSuccess.apply(value);
    }

    @Override
    public <R> Result<R> map(
            Function<? super T, ? extends R> mapper
    ) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return Result.success(mapper.apply(value));
    }
}