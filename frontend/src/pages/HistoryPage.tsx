import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { fetchHistory } from "../lib/api";
import { Tile } from "../components/Tile";
import { StatusDot, type StatusTone } from "../components/StatusDot";
import { Fingerprint } from "../components/Fingerprint";

function resultTone(result: string): StatusTone {
  if (result === "PASS" || result === "SUCCESS") return "success";
  if (result === "FAIL" || result === "FAILED") return "danger";
  return "neutral";
}

export function HistoryPage() {
  const historyQuery = useQuery({ queryKey: ["history"], queryFn: fetchHistory });

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold">History</h1>
      <Tile>
        {historyQuery.data && historyQuery.data.length > 0 ? (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-text-muted">
                <th className="pb-2">Commit</th>
                <th className="pb-2">Date</th>
                <th className="pb-2">Pipeline</th>
                <th className="pb-2">Scan</th>
                <th className="pb-2">Validation</th>
                <th className="pb-2">Drift</th>
                <th className="pb-2">Deployed to</th>
              </tr>
            </thead>
            <tbody>
              {historyQuery.data.map((row) => (
                <tr key={row.commitSha} className="border-t border-border">
                  <td className="py-1.5">
                    <Link to={`/history/${row.commitSha}`} className="font-mono text-accent hover:underline">
                      {row.commitSha.slice(0, 7)}
                    </Link>
                  </td>
                  <td className="py-1.5"><Fingerprint value={row.date} /></td>
                  <td className="py-1.5"><StatusDot tone={resultTone(row.pipelineResult)} label={row.pipelineResult} /></td>
                  <td className="py-1.5"><StatusDot tone={resultTone(row.scanResult)} label={row.scanResult} /></td>
                  <td className="py-1.5"><StatusDot tone={resultTone(row.validationResult)} label={row.validationResult} /></td>
                  <td className="py-1.5"><StatusDot tone={resultTone(row.driftResult)} label={row.driftResult} /></td>
                  <td className="py-1.5">{row.deployedTo.length > 0 ? row.deployedTo.join(", ") : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-sm text-text-muted">No commit history recorded yet.</p>
        )}
      </Tile>
    </div>
  );
}
