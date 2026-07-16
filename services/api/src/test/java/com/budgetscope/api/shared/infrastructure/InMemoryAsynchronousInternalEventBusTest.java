package com.budgetscope.api.shared.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.budgetscope.api.shared.application.InternalEvent;
import com.budgetscope.api.shared.application.InternalEventHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class InMemoryAsynchronousInternalEventBusTest {
    @Test
    void dispatchesEventToSubscribedHandlersAsynchronously() throws InterruptedException {
        var executor = Executors.newSingleThreadExecutor();
        try {
            var bus = new InMemoryAsynchronousInternalEventBus(executor);
            var handledEvent = new AtomicReference<TestEvent>();
            var handled = new CountDownLatch(1);
            bus.subscribe(new InternalEventHandler<TestEvent>() {
                @Override
                public Class<TestEvent> eventType() {
                    return TestEvent.class;
                }

                @Override
                public void handle(TestEvent event) {
                    handledEvent.set(event);
                    handled.countDown();
                }
            });
            var event = new TestEvent("registered");

            bus.publish(event);

            assertTrue(handled.await(2, TimeUnit.SECONDS));
            assertEquals(event, handledEvent.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void requiresExecutorHandlerAndEvent() {
        var bus = new InMemoryAsynchronousInternalEventBus(Runnable::run);

        assertThrows(NullPointerException.class, () -> new InMemoryAsynchronousInternalEventBus(null));
        assertThrows(NullPointerException.class, () -> bus.subscribe(null));
        assertThrows(NullPointerException.class, () -> bus.publish(null));
    }

    private record TestEvent(String value) implements InternalEvent {
    }
}
