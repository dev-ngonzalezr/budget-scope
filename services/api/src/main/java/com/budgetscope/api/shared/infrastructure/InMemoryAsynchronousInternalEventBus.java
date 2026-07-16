package com.budgetscope.api.shared.infrastructure;

import com.budgetscope.api.shared.application.AsynchronousInternalEventBus;
import com.budgetscope.api.shared.application.InternalEvent;
import com.budgetscope.api.shared.application.InternalEventHandler;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class InMemoryAsynchronousInternalEventBus implements AsynchronousInternalEventBus, AutoCloseable {
    private final Executor executor;
    private final AutoCloseable closeableExecutor;
    private final ConcurrentMap<Class<? extends InternalEvent>, List<InternalEventHandler<? extends InternalEvent>>> handlers;

    public InMemoryAsynchronousInternalEventBus() {
        this(Executors.newCachedThreadPool(), true);
    }

    public InMemoryAsynchronousInternalEventBus(Executor executor) {
        this(executor, false);
    }

    private InMemoryAsynchronousInternalEventBus(Executor executor, boolean closeExecutor) {
        this.executor = Objects.requireNonNull(executor, "Asynchronous event executor is required");
        closeableExecutor = closeExecutor && executor instanceof AutoCloseable autoCloseable ? autoCloseable : null;
        handlers = new ConcurrentHashMap<>();
    }

    public <E extends InternalEvent> void subscribe(InternalEventHandler<E> handler) {
        Objects.requireNonNull(handler, "Internal event handler is required");
        handlers.compute(handler.eventType(), (eventType, existingHandlers) -> {
            var updatedHandlers = existingHandlers == null
                    ? new java.util.ArrayList<InternalEventHandler<? extends InternalEvent>>()
                    : new java.util.ArrayList<>(existingHandlers);
            updatedHandlers.add(handler);
            return List.copyOf(updatedHandlers);
        });
    }

    @Override
    public void publish(InternalEvent event) {
        Objects.requireNonNull(event, "Internal event is required");
        for (var handler : handlers.getOrDefault(event.getClass(), List.of())) {
            executor.execute(() -> handle(handler, event));
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends InternalEvent> void handle(InternalEventHandler<? extends InternalEvent> handler, InternalEvent event) {
        ((InternalEventHandler<E>) handler).handle((E) event);
    }

    @Override
    public void close() throws Exception {
        if (closeableExecutor != null) {
            closeableExecutor.close();
        }
    }
}
