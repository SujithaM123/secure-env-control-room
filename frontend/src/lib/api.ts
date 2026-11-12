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
 * Base64-encodes a UTF-8 string for a Basic Auth header. Plain btoa() only
 * handles Latin1, so this goes through TextEncoder first rather than the
 * older unescape(encodeURIComponent(...)) trick.
 */
function base64EncodeUtf8(value: string): string {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
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

/**
 * Builds the Authorization: Basic header in JS from an in-page form
 * (RotateButton) instead of relying on the browser's native Basic Auth
 * popup - that popup auto-saves and re-sends stale credentials regardless
 * of what's retyped, and gives no way to show a clear error in the UI.
 * The server-side check (BasicAuthFilter, reading the password from
 * Secrets Manager) is unchanged - only how the credential reaches it
 * changes. On failure, throws with the server's actual response body as
 * the message (SpaFallbackController now returns the real error text for
 * /api paths instead of an empty body) rather than a generic one.
 */
export async function rotateWriteApiKey(env: string, username: string, password: string): Promise<RotationResult> {
  const response = await fetch(`${BASE_URL}/api/rotate/write-api-key/${env}`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      Authorization: `Basic ${base64EncodeUtf8(`${username}:${password}`)}`,
    },
  });
  if (!response.ok) {
    const text = await response.text().catch(() => "");
    throw new ApiError(response.status, text || `${response.status} ${response.statusText}`);
  }
  return (await response.json()) as RotationResult;
}
