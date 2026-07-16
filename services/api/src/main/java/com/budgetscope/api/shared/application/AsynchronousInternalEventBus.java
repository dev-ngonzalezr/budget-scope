package com.budgetscope.api.shared.application;

/**
 * Publishes an internal event for eventually consistent handling outside the caller's immediate flow.
 * Infrastructure can back this port with an outbox, queue, or after-commit dispatcher.
 */
public interface AsynchronousInternalEventBus {
    void publish(InternalEvent event);
}
