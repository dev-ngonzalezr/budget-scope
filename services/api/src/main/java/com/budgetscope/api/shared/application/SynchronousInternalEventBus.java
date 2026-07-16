package com.budgetscope.api.shared.application;

/**
 * Dispatches an internal event to in-process handlers before returning to the caller.
 * Use this path when module communication must participate in the caller's transaction.
 */
public interface SynchronousInternalEventBus {
    void publish(InternalEvent event);
}
