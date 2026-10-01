package com.fold.kernel.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResultTest {

    private static final DomainError ERROR =
            new ValidationError(
                    "invalid-value",
                    "Value is invalid"
            );

    @Test
    void successShouldReportSuccess() {
        Result<String> result =
                Result.success("fold");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
    }

    @Test
    void failureShouldReportFailure() {
        Result<String> result =
                Result.failure(ERROR);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void successShouldFoldUsingSuccessFunction() {
        Result<String> result =
                Result.success("fold");

        Integer value = result.fold(
                String::length,
                error -> -1
        );

        assertThat(value).isEqualTo(4);
    }

    @Test
    void failureShouldFoldUsingFailureFunction() {
        Result<String> result =
                Result.failure(ERROR);

        String value = result.fold(
                success -> success,
                DomainError::code
        );

        assertThat(value)
                .isEqualTo("invalid-value");
    }

    @Test
    void mapShouldTransformSuccess() {
        Result<Integer> result =
                Result.success("fold")
                        .map(String::length);

        Integer value = result.fold(
                success -> success,
                error -> -1
        );

        assertThat(value).isEqualTo(4);
    }

    @Test
    void mapShouldPreserveFailure() {
        Result<Integer> mapped =
                Result.<String>failure(ERROR)
                        .map(String::length);

        assertThat(mapped.isFailure()).isTrue();

        DomainError error = mapped.fold(
                value -> null,
                failure -> failure
        );

        assertThat(error).isSameAs(ERROR);
    }

    @Test
    void successShouldRejectNullValue() {
        assertThatThrownBy(
                () -> Result.success(null)
        )
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void failureShouldRejectNullError() {
        assertThatThrownBy(
                () -> Result.failure(null)
        )
                .isInstanceOf(NullPointerException.class);
    }
}