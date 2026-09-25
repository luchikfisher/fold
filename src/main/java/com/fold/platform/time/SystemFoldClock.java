package com.fold.platform.time;

import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Objects;

/**
 * Production implementation of {@link FoldClock} backed by a UTC system clock.
 *
 * <p>This class is an infrastructure adapter. Domain and application code
 * depend only on {@link FoldClock} and therefore remain independent of both
 * the Java system clock and Spring.</p>
 *
 * <p>The default constructor uses {@link Clock#systemUTC()}. A package-private
 * constructor accepts a custom {@link Clock} to allow deterministic testing.</p>
 */
@Component
public final class SystemFoldClock implements FoldClock {

    private final Clock clock;

    /**
     * Creates a clock backed by the system UTC clock.
     */
    public SystemFoldClock() {
        this(Clock.systemUTC());
    }

    SystemFoldClock(Clock clock) {
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
    }

    /**
     * Returns the current absolute timestamp supplied by the underlying clock.
     *
     * @return the current timestamp
     */
    @Override
    public Timestamp now() {
        return Timestamp.of(clock.instant());
    }
}