import { useQuery } from "@tanstack/react-query";
import { fetchAllEnvironmentSummaries, fetchDriftMatrix, fetchScanFindings } from "../lib/api";
import { Tile } from "../components/Tile";
import { Fingerprint } from "../components/Fingerprint";
import { DriftHeatmap } from "../components/charts/DriftHeatmap";
import { HealthGauges } from "../components/charts/HealthGauges";
import { SecretAgeBarChart } from "../components/charts/SecretAgeBarChart";

export function ComparePage() {
  const summariesQuery = useQuery({ queryKey: ["summaries"], queryFn: fetchAllEnvironmentSummaries });
  const driftQuery = useQuery({ queryKey: ["drift-matrix"], queryFn: fetchDriftMatrix });
  const scanFindingsQuery = useQuery({ queryKey: ["scan-findings"], queryFn: fetchScanFindings });

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold">Compare</h1>

      <Tile title="Drift matrix">
        {driftQuery.data ? <DriftHeatmap matrix={driftQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
      </Tile>

      <div className="grid gap-6 lg:grid-cols-2">
        <Tile title="Health by environment">
          {summariesQuery.data ? <HealthGauges summaries={summariesQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
        </Tile>
        <Tile title="Secret age by environment">
          {summariesQuery.data ? <SecretAgeBarChart summaries={summariesQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
        </Tile>
      </div>

      <Tile title="Scan findings">
        {scanFindingsQuery.data && scanFindingsQuery.data.length > 0 ? (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-text-muted">
                <th className="pb-2">File</th>
                <th className="pb-2">Line</th>
                <th className="pb-2">Rule</th>
                <th className="pb-2">Commit</th>
                <th className="pb-2">Scanned</th>
              </tr>
            </thead>
            <tbody>
              {scanFindingsQuery.data.map((finding) => (
                <tr key={finding.id} className="border-t border-border">
                  <td className="py-1.5 font-mono">{finding.filePath}</td>
                  <td className="py-1.5 font-mono">{finding.lineNumber}</td>
                  <td className="py-1.5">{finding.ruleMatched}</td>
                  <td className="py-1.5"><Fingerprint value={finding.commitSha.slice(0, 7)} /></td>
                  <td className="py-1.5"><Fingerprint value={finding.scannedAt} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-sm text-text-muted">No hardcoded-secret findings recorded.</p>
        )}
      </Tile>
    </div>
  );
}
