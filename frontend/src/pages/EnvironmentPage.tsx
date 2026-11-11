import { useQuery } from "@tanstack/react-query";
import {
  fetchAccessAudit,
  fetchConfigSnapshot,
  fetchDeployments,
  fetchEnvironmentSummary,
  fetchRotations,
} from "../lib/api";
import { Tile } from "../components/Tile";
import { StatusDot, type StatusTone } from "../components/StatusDot";
import { Fingerprint } from "../components/Fingerprint";
import { RotationTimelineChart } from "../components/charts/RotationTimelineChart";
import { envLabel } from "../lib/env";
import type { EnvironmentSummary } from "../types/api";

function validationTone(status: string): StatusTone {
  if (status === "PASS") return "success";
  if (status === "FAIL") return "danger";
  return "neutral";
}

function StatusBadge({ summary }: { summary: EnvironmentSummary }) {
  return (
    <div className="flex flex-wrap items-center gap-6">
      <StatusDot tone={validationTone(summary.latestValidationStatus)} label={`Validation: ${summary.latestValidationStatus}`} />
      <StatusDot tone={summary.driftDetected ? "danger" : "success"} label={summary.driftDetected ? "Drift detected" : "No drift"} />
      <StatusDot
        tone={summary.rotationOverdue ? "danger" : "success"}
        label={summary.secretAgeDays == null ? "Never rotated" : `Secret age: ${summary.secretAgeDays}d`}
      />
      <span className="font-mono text-sm text-text-muted">Health score: {summary.healthScore}</span>
    </div>
  );
}

export function EnvironmentPage({ env }: { env: "dev" | "test" | "prod" }) {
  const summaryQuery = useQuery({ queryKey: ["summary", env], queryFn: () => fetchEnvironmentSummary(env) });
  const snapshotQuery = useQuery({ queryKey: ["config-snapshot", env], queryFn: () => fetchConfigSnapshot(env), retry: false });
  const rotationsQuery = useQuery({ queryKey: ["rotations", env], queryFn: () => fetchRotations(env) });
  const deploymentsQuery = useQuery({ queryKey: ["deployments", env], queryFn: () => fetchDeployments(env) });
  const accessAuditQuery = useQuery({ queryKey: ["access-audit", env], queryFn: () => fetchAccessAudit(env) });

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold">{envLabel(env)}</h1>

      <Tile title="Status">
        {summaryQuery.data ? <StatusBadge summary={summaryQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
      </Tile>

      <Tile title="Config snapshot">
        {snapshotQuery.isError ? (
          <p className="text-sm text-text-muted">No config snapshot recorded yet.</p>
        ) : snapshotQuery.data ? (
          <div className="flex flex-col gap-2">
            <p className="text-xs text-text-muted">
              Captured <Fingerprint value={snapshotQuery.data.capturedAt} />
            </p>
            <table className="w-full text-sm">
              <tbody>
                {Object.entries(snapshotQuery.data.settings).map(([key, value]) => (
                  <tr key={key} className="border-t border-border">
                    <td className="py-1 pr-4 text-text-muted">{key}</td>
                    <td className="py-1 font-mono text-text">{String(value)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-sm text-text-muted">Loading…</p>
        )}
      </Tile>

      <Tile title="Rotation timeline">
        {rotationsQuery.data ? <RotationTimelineChart events={rotationsQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
      </Tile>

      <Tile title="Deployment history">
        {deploymentsQuery.data && deploymentsQuery.data.length > 0 ? (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-text-muted">
                <th className="pb-2">Version</th>
                <th className="pb-2">Deployed</th>
                <th className="pb-2">Verified</th>
                <th className="pb-2">Fingerprint</th>
              </tr>
            </thead>
            <tbody>
              {deploymentsQuery.data.map((d) => (
                <tr key={d.id} className="border-t border-border">
                  <td className="py-1.5"><Fingerprint value={d.imageVersion.slice(0, 7)} /></td>
                  <td className="py-1.5"><Fingerprint value={d.deployedAt} /></td>
                  <td className="py-1.5">
                    <StatusDot tone={d.verified ? "success" : "danger"} label={d.verified ? "Verified" : "Failed"} />
                  </td>
                  <td className="py-1.5">
                    <Fingerprint value={(d.verificationDetails?.secretFingerprint as string) ?? null} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-sm text-text-muted">No deployments recorded yet.</p>
        )}
      </Tile>

      <Tile title="Access audit log">
        {accessAuditQuery.data && accessAuditQuery.data.length > 0 ? (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-text-muted">
                <th className="pb-2">IAM role</th>
                <th className="pb-2">Permission</th>
                <th className="pb-2">Justification</th>
                <th className="pb-2">Captured</th>
              </tr>
            </thead>
            <tbody>
              {accessAuditQuery.data.map((a) => (
                <tr key={a.id} className="border-t border-border">
                  <td className="py-1.5 font-mono">{a.iamRole}</td>
                  <td className="py-1.5 font-mono">{a.permission}</td>
                  <td className="py-1.5">{a.justification}</td>
                  <td className="py-1.5"><Fingerprint value={a.capturedAt} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-sm text-text-muted">No access audit entries recorded yet.</p>
        )}
      </Tile>
    </div>
  );
}
