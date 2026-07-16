package com.budgetscope.api.shared.application;

public interface InternalEventHandler<E extends InternalEvent> {
    Class<E> eventType();

    void handle(E event);
}
