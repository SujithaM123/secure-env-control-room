import { useState, type ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ApiError,
  fetchAccessAudit,
  fetchConfigSnapshot,
  fetchDeployments,
  fetchEnvironmentSummary,
  fetchRotations,
  rotateWriteApiKey,
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

function KpiCard({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="rounded-sm border border-border p-3">
      <p className="mb-1 text-xs uppercase tracking-wide text-text-muted">{label}</p>
      {children}
    </div>
  );
}

function StatusBadge({ summary }: { summary: EnvironmentSummary }) {
  return (
    <div className="grid gap-3" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))" }}>
      <KpiCard label="Validation">
        <StatusDot tone={validationTone(summary.latestValidationStatus)} label={summary.latestValidationStatus} />
      </KpiCard>
      <KpiCard label="Drift">
        <StatusDot tone={summary.driftDetected ? "danger" : "success"} label={summary.driftDetected ? "Detected" : "None"} />
      </KpiCard>
      <KpiCard label="Rotation">
        <StatusDot
          tone={summary.rotationOverdue ? "danger" : "success"}
          label={summary.secretAgeDays == null ? "Never rotated" : `${summary.secretAgeDays}d ago`}
        />
      </KpiCard>
      <KpiCard label="Health score">
        <span className="font-mono text-lg text-text">{summary.healthScore}</span>
      </KpiCard>
    </div>
  );
}

/**
 * An in-page form, not the browser's native Basic Auth popup. That popup
 * auto-saves and re-sends stale credentials regardless of what's retyped,
 * gives no way to show a clear error, and is hard to test - building the
 * Authorization: Basic header in JS (api.ts) and submitting it on a plain
 * fetch sidesteps all of that. The server-side check is unchanged.
 */
function RotateButton({ env }: { env: "dev" | "test" | "prod" }) {
  const queryClient = useQueryClient();
  const [formOpen, setFormOpen] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const mutation = useMutation({
    mutationFn: () => rotateWriteApiKey(env, username, password),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["summary", env] });
      queryClient.invalidateQueries({ queryKey: ["rotations", env] });
      setFormOpen(false);
      setPassword("");
    },
  });

  if (!formOpen) {
    return (
      <button
        type="button"
        onClick={() => setFormOpen(true)}
        className="tile w-fit rounded-sm px-3 py-1.5 text-sm font-medium text-text hover:border-accent"
      >
        Rotate now
      </button>
    );
  }

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate();
      }}
      className="tile flex max-w-xs flex-col gap-2 rounded-sm p-3"
    >
      <p className="text-xs text-text-muted">Enter rotation credentials for {envLabel(env)}</p>
      <input
        type="text"
        placeholder="Username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        autoComplete="off"
        className="rounded-sm border border-border bg-bg px-2 py-1 text-sm text-text"
      />
      <input
        type="password"
        placeholder="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        autoComplete="off"
        className="rounded-sm border border-border bg-bg px-2 py-1 text-sm text-text"
      />
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="rounded-sm bg-accent px-3 py-1.5 text-sm font-medium text-bg disabled:opacity-50"
        >
          {mutation.isPending ? "Rotating…" : "Confirm rotate"}
        </button>
        <button
          type="button"
          onClick={() => {
            setFormOpen(false);
            setPassword("");
            mutation.reset();
          }}
          className="rounded-sm border border-border px-3 py-1.5 text-sm text-text-muted hover:text-text"
        >
          Cancel
        </button>
      </div>
      {mutation.isError && (
        <p className="text-xs text-danger">
          {mutation.error instanceof ApiError
            ? `${mutation.error.status}: ${mutation.error.message}`
            : "Rotation failed — see console for details."}
        </p>
      )}
      {mutation.isSuccess && (
        <p className="text-xs text-success">
          Rotated — new fingerprint <Fingerprint value={mutation.data.newFingerprint} />
        </p>
      )}
    </form>
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
        <div className="flex flex-col gap-4">
          {summaryQuery.data ? <StatusBadge summary={summaryQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
          <RotateButton env={env} />
        </div>
      </Tile>

      <Tile title="Config snapshot">
        {snapshotQuery.isError ? (
          <p className="text-sm text-text-muted">No config snapshot recorded yet.</p>
        ) : snapshotQuery.data ? (
          <div className="flex flex-col gap-3">
            <p className="text-xs text-text-muted">
              Captured <Fingerprint value={snapshotQuery.data.capturedAt} />
            </p>
            <div className="grid gap-3" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))" }}>
              {Object.entries(snapshotQuery.data.settings).map(([key, value]) => (
                <div key={key} className="rounded-sm border border-border p-2.5">
                  <p className="mb-1 text-xs text-text-muted">{key}</p>
                  <p className="break-all font-mono text-sm text-text">{String(value)}</p>
                </div>
              ))}
            </div>
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
