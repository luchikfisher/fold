package com.fold.kernel.events;

/**
 * Identifies the version of an integration-event contract.
 *
 * <p>Versions begin at one and increase only when a contract change requires
 * consumers to distinguish the new schema from an earlier one.</p>
 *
 * @param value the positive event-contract version
 */
public record EventVersion(int value)
        implements Comparable<EventVersion> {

    public EventVersion {
        if (value < 1) {
            throw new IllegalArgumentException(
                    "event version must be greater than zero"
            );
        }
    }

    public static EventVersion initial() {
        return new EventVersion(1);
    }

    public static EventVersion of(int value) {
        return new EventVersion(value);
    }

    @Override
    public int compareTo(EventVersion other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}