package com.github.devlucasjava.socialklyp.domain.valueobject;

/**
 * Value object representing the current capacity state of a chat.
 * Encapsulates the business rule that a chat can have at most MAX_MEMBERS members.
 */
public final class ChatCapacity {

    public static final int MAX_MEMBERS = 50;

    private final int current;

    private ChatCapacity(int current) {
        if (current < 0) {
            throw new IllegalArgumentException("Member count cannot be negative");
        }
        this.current = current;
    }

    public static ChatCapacity of(int current) {
        return new ChatCapacity(current);
    }

    public int getCurrent() {
        return current;
    }

    /** Alias for getCurrent() — returns the current member count. */
    public int count() {
        return current;
    }

    public int getMax() {
        return MAX_MEMBERS;
    }

    public boolean isFull() {
        return current >= MAX_MEMBERS;
    }

    public boolean hasRoom() {
        return current < MAX_MEMBERS;
    }

    public int remaining() {
        return Math.max(0, MAX_MEMBERS - current);
    }

    /** Alias for remaining() — returns how many more members can be added. */
    public int remainingCapacity() {
        return remaining();
    }

    @Override
    public String toString() {
        return current + "/" + MAX_MEMBERS;
    }
}
