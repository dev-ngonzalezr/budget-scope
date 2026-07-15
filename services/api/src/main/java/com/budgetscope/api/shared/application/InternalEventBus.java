package com.budgetscope.api.shared.application;

public interface InternalEventBus {
    void publish(InternalEvent event);
}
