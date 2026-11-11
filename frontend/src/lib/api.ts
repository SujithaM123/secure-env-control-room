import type {
  AccessAuditEntry,
  ConfigInfo,
  ConfigSnapshot,
  Deployment,
  DriftMatrix,
  EnvironmentSummary,
  HealthResponse,
  HistoryDetail,
  HistoryRow,
  RotationEvent,
  ScanFinding,
} from "../types/api";

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

export const fetchEnvironmentSummary = (env: string) =>
  getJson<EnvironmentSummary>(`/api/environments/${env}/summary`);

export const fetchAllEnvironmentSummaries = () =>
  getJson<EnvironmentSummary[]>("/api/environments/summary");

export const fetchConfigSnapshot = (env: string) =>
  getJson<ConfigSnapshot>(`/api/environments/${env}/config-snapshot`);

export const fetchRotations = (env: string) =>
  getJson<RotationEvent[]>(`/api/environments/${env}/rotations`);

export const fetchDeployments = (env: string) =>
  getJson<Deployment[]>(`/api/environments/${env}/deployments`);

export const fetchAccessAudit = (env: string) =>
  getJson<AccessAuditEntry[]>(`/api/environments/${env}/access-audit`);

export const fetchDriftMatrix = () => getJson<DriftMatrix>("/api/drift-matrix");

export const fetchScanFindings = () => getJson<ScanFinding[]>("/api/scan-findings");

export const fetchHistory = () => getJson<HistoryRow[]>("/api/history");

export const fetchHistoryDetail = (commitSha: string) =>
  getJson<HistoryDetail>(`/api/history/${commitSha}`);
