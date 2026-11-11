import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { fetchHistoryDetail } from "../lib/api";
import { Tile } from "../components/Tile";
import { StatusDot, type StatusTone } from "../components/StatusDot";
import { Fingerprint } from "../components/Fingerprint";

const REPO_URL = "https://github.com/SujithaM123/secure-env-control-room";

function statusTone(status: string): StatusTone {
  if (status === "PASS" || status === "switched") return "success";
  if (status === "FAIL" || status === "failed" || status === "rolled_back") return "danger";
  return "neutral";
}

export function HistoryDetailPage() {
  const { commitSha = "" } = useParams<{ commitSha: string }>();
  const detailQuery = useQuery({
    queryKey: ["history-detail", commitSha],
    queryFn: () => fetchHistoryDetail(commitSha),
    retry: false,
  });

  if (detailQuery.isError) {
    return <p className="text-sm text-text-muted">No history recorded for commit {commitSha}.</p>;
  }
  if (!detailQuery.data) {
    return <p className="text-sm text-text-muted">Loading…</p>;
  }

  const detail = detailQuery.data;

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold">Commit <Fingerprint value={detail.commitSha} /></h1>
      <a
        href={`${REPO_URL}/commit/${detail.commitSha}/checks`}
        target="_blank"
        rel="noreferrer"
        className="text-sm text-accent hover:underline"
      >
        View Actions run for this commit →
      </a>

      <Tile title="Scan findings">
        {detail.scanFindings.length > 0 ? (
          <ul className="flex flex-col gap-1 text-sm">
            {detail.scanFindings.map((f, i) => (
              <li key={i} className="font-mono">
                {f.filePath}:{f.lineNumber} — {f.ruleMatched}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-text-muted">No findings for this commit.</p>
        )}
      </Tile>

      <Tile title="Validation results">
        {detail.validationResults.length > 0 ? (
          <ul className="flex flex-col gap-2 text-sm">
            {detail.validationResults.map((v, i) => (
              <li key={i}>
                <StatusDot tone={statusTone(v.status)} label={`${v.environment}: ${v.status}`} />
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-text-muted">No validation results for this commit.</p>
        )}
      </Tile>

      <Tile title="Drift snapshot">
        {detail.driftFindings.length > 0 ? (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-text-muted">
                <th className="pb-2">Pair</th>
                <th className="pb-2">Setting</th>
                <th className="pb-2">Allowed</th>
              </tr>
            </thead>
            <tbody>
              {detail.driftFindings.map((d, i) => (
                <tr key={i} className="border-t border-border">
                  <td className="py-1.5">{d.envA} vs {d.envB}</td>
                  <td className="py-1.5 font-mono">{d.settingKey}</td>
                  <td className="py-1.5">
                    <StatusDot tone={d.allowed ? "success" : "danger"} label={d.allowed ? "Allowed" : "Unexpected"} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-sm text-text-muted">No drift recorded for this commit.</p>
        )}
      </Tile>

      <Tile title="Deploy outcome by environment">
        {detail.deployments.length > 0 ? (
          <ul className="flex flex-col gap-2 text-sm">
            {detail.deployments.map((d) => (
              <li key={d.id}>
                <StatusDot tone={d.verified ? "success" : "danger"} label={`${d.environment}: ${d.verified ? "verified" : "failed"}`} />
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-text-muted">No deployments recorded for this commit.</p>
        )}
      </Tile>

      <Tile title="Rotation fingerprint change">
        {detail.rotationEvents.length > 0 ? (
          <ul className="flex flex-col gap-2 text-sm">
            {detail.rotationEvents.map((r) => (
              <li key={r.id} className="flex items-center gap-2">
                <StatusDot tone={statusTone(r.status)} label={r.status} />
                <span className="font-mono text-text-muted">
                  <Fingerprint value={r.oldFingerprint} /> → <Fingerprint value={r.newFingerprint} />
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-text-muted">No rotation occurred at this commit.</p>
        )}
      </Tile>
    </div>
  );
}
