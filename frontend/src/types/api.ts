export interface ConfigInfo {
  env: string;
  version: string;
  secretFingerprint: string;
}

export type HealthState = "UP" | "DOWN";

export interface HealthResponse {
  status: HealthState;
}

export interface EnvironmentSummary {
  name: string;
  latestValidationStatus: string;
  driftDetected: boolean;
  secretAgeDays: number | null;
  rotationOverdue: boolean;
  healthScore: number;
}

export interface ConfigSnapshot {
  capturedAt: string | null;
  settings: Record<string, unknown>;
}

export interface RotationEvent {
  id: number;
  startedAt: string;
  overlapEndsAt: string;
  completedAt: string | null;
  oldFingerprint: string | null;
  newFingerprint: string | null;
  status: string;
  commitSha: string | null;
}

export interface Deployment {
  id: number;
  environment: string;
  imageVersion: string;
  deployedAt: string;
  verified: boolean;
  verificationDetails: Record<string, unknown> | null;
}

export interface AccessAuditEntry {
  id: number;
  iamRole: string;
  permission: string;
  justification: string;
  capturedAt: string | null;
}

export interface DriftPair {
  envA: string;
  envB: string;
  hasDrift: boolean;
  driftCount: number;
}

export interface DriftMatrix {
  comparedAt: string | null;
  pairs: DriftPair[];
}

export interface ScanFinding {
  id: number;
  scannedAt: string;
  filePath: string;
  lineNumber: number;
  ruleMatched: string;
  commitSha: string;
}

export interface ValidationResult {
  environment: string;
  status: string;
  checkedAt: string;
  errors: unknown;
}

export interface DriftFinding {
  envA: string;
  envB: string;
  settingKey: string;
  valueA: string | null;
  valueB: string | null;
  allowed: boolean;
}

export interface RotationResult {
  environment: string;
  status: string;
  oldFingerprint: string;
  newFingerprint: string;
  completedAt: string | null;
}

export interface HistoryRow {
  commitSha: string;
  date: string | null;
  pipelineResult: string;
  scanResult: string;
  validationResult: string;
  driftResult: string;
  deployedTo: string[];
}

export interface HistoryDetail {
  commitSha: string;
  scanFindings: ScanFinding[];
  validationResults: ValidationResult[];
  driftFindings: DriftFinding[];
  deployments: Deployment[];
  rotationEvents: RotationEvent[];
}
