import type { StatusTone } from "../components/StatusDot";
import type { EnvironmentSummary } from "../types/api";

export function healthTone(score: number): StatusTone {
  if (score >= 70) return "success";
  if (score >= 40) return "warning";
  return "danger";
}

/** Mirrors EnvironmentsController#computeHealthScore's three components exactly (same thresholds), so the donut shows the real composition behind the backend's own healthScore — not a new metric. */
const ROTATION_FRESH_DAYS = 30;
const ROTATION_OVERDUE_DAYS = 90;

export interface HealthComponents {
  validation: number;
  drift: number;
  rotation: number;
}

export function healthComponents(summary: EnvironmentSummary): HealthComponents {
  const validation =
    summary.latestValidationStatus === "PASS" ? 100 : summary.latestValidationStatus === "FAIL" ? 0 : 50;
  const drift = summary.driftDetected ? 0 : 100;

  const days = summary.secretAgeDays;
  let rotation: number;
  if (days == null) {
    rotation = 0;
  } else if (days <= ROTATION_FRESH_DAYS) {
    rotation = 100;
  } else if (days >= ROTATION_OVERDUE_DAYS) {
    rotation = 0;
  } else {
    rotation = Math.round((100 * (ROTATION_OVERDUE_DAYS - days)) / (ROTATION_OVERDUE_DAYS - ROTATION_FRESH_DAYS));
  }

  return { validation, drift, rotation };
}
