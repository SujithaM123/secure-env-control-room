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
  RotationResult,
  ScanFinding,
} from "../types/api";

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

// An empty BASE_URL is correct and expected in production: the built app is
// served by the same Spring Boot app it calls, so relative paths are
// same-origin. It's only a misconfiguration in local dev, where the Vite
// dev server's own origin isn't the backend - warn there only.
if (import.meta.env.DEV && !BASE_URL) {
  console.warn(
    "VITE_API_BASE_URL is not set — requests will go to this Vite dev server's own origin, not a backend. Set it in frontend/.env for local development."
  );
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new ApiError(response.status, `${path} responded with ${response.status}`);
  }
  return (await response.json()) as T;
}

/**
 * No credential is attached in JS here - that's intentional, not a gap.
 * The rotation endpoint is gated by HTTP Basic Auth (BasicAuthFilter), and
 * a 401 with WWW-Authenticate: Basic triggers the browser's own native
 * credential prompt; the browser then retries with the Authorization
 * header itself. The real credential never needs to touch frontend code.
 * The other 5 write endpoints (WriteApiKeyFilter, the write-API key) are
 * pipeline-only and never called from this dashboard at all.
 */
async function postJson<T>(path: string): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    throw new ApiError(response.status, `${path} responded with ${response.status}`);
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

export const rotateWriteApiKey = (env: string) =>
  postJson<RotationResult>(`/api/rotate/write-api-key/${env}`);
