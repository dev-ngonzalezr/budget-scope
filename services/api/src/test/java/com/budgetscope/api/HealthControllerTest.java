package com.budgetscope.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class HealthControllerTest {
    @Test
    void statusReturnsServiceHealth() {
        var response = new HealthController().status();

        assertEquals("ok", response.status());
        assertEquals("budget-scope-api", response.service());
    }
}
