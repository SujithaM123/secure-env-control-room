import type { ConfigInfo, HealthResponse } from "../types/api";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

if (!BASE_URL) {
  console.warn(
    "VITE_API_BASE_URL is not set — the dashboard has no backend to call."
  );
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new Error(`${path} responded with ${response.status}`);
  }
  return (await response.json()) as T;
}

export const fetchConfigInfo = () => getJson<ConfigInfo>("/api/config-info");

export const fetchHealth = () => getJson<HealthResponse>("/actuator/health");
