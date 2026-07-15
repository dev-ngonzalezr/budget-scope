package com.budgetscope.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/v1")
public final class HealthController {
    @Get("/status")
    public StatusResponse status() {
        return new StatusResponse("ok", "budget-scope-api");
    }

    @Serdeable
    public record StatusResponse(String status, String service) {
    }
}
