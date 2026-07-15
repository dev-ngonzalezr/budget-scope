# 0001 Use a modular monolith

## Status

Accepted

## Context

BudgetScope needs clear domain boundaries for household finance workflows without taking on early distributed-system complexity.

## Decision

Start with one Micronaut deployable application organized into domain-oriented modules and explicit API, application, domain, and infrastructure layers.

## Alternatives considered

- Microservices from the start: rejected because it would add operational overhead before product boundaries are proven.
- Single unstructured application: rejected because financial and authorization rules need maintainable boundaries.

## Consequences

- Local development and deployment remain simple.
- Module boundaries must be enforced through code review and tests.
- Asynchronous messaging or service extraction can be introduced later when a measured need exists.
