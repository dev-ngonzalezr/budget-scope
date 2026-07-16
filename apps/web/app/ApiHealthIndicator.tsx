"use client";

import { useEffect, useState } from "react";

type ApiHealth = "checking" | "healthy" | "unavailable";

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

export function ApiHealthIndicator() {
  const [health, setHealth] = useState<ApiHealth>("checking");

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const response = await fetch(`${apiBaseUrl}/status`, { cache: "no-store" });
        const body = (await response.json()) as { status?: string };

        setHealth(response.ok && body.status === "ok" ? "healthy" : "unavailable");
      } catch {
        setHealth("unavailable");
      }
    };

    void checkHealth();
    const interval = window.setInterval(checkHealth, 30_000);

    return () => window.clearInterval(interval);
  }, []);

  const label =
    health === "healthy"
      ? "API healthy"
      : health === "checking"
        ? "Checking API"
        : "API unavailable";

  return (
    <div className={`api-health api-health--${health}`} role="status" aria-live="polite">
      <span className="api-health__dot" aria-hidden="true" />
      {label}
    </div>
  );
}
