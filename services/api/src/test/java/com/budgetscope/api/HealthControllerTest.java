package com.budgetscope.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
final class HealthControllerTest {
    @Test
    void statusReturnsServiceHealth() {
        var response = new HealthController().status();

        assertEquals("ok", response.status());
        assertEquals("budget-scope-api", response.service());
    }
}
