export interface ConfigInfo {
  env: string;
  version: string;
  secretFingerprint: string;
}

export type HealthState = "UP" | "DOWN";

export interface HealthResponse {
  status: HealthState;
}
