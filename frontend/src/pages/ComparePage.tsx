import { useQuery } from "@tanstack/react-query";
import { fetchAllEnvironmentSummaries, fetchDeployments, fetchDriftMatrix, fetchScanFindings } from "../lib/api";
import { Tile } from "../components/Tile";
import { Fingerprint } from "../components/Fingerprint";
import { DriftHeatmap } from "../components/charts/DriftHeatmap";
import { HealthGauges } from "../components/charts/HealthGauges";
import { SecretAgeBarChart } from "../components/charts/SecretAgeBarChart";
import { HealthCompositionDonuts } from "../components/charts/HealthCompositionDonuts";
import { DeploymentFrequencyChart } from "../components/charts/DeploymentFrequencyChart";
import { ENV_IDS } from "../lib/env";

const CHART_GRID_STYLE = { gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))" };

export function ComparePage() {
  const summariesQuery = useQuery({ queryKey: ["summaries"], queryFn: fetchAllEnvironmentSummaries });
  const driftQuery = useQuery({ queryKey: ["drift-matrix"], queryFn: fetchDriftMatrix });
  const scanFindingsQuery = useQuery({ queryKey: ["scan-findings"], queryFn: fetchScanFindings });
  const deploymentCountsQuery = useQuery({
    queryKey: ["deployment-counts"],
    queryFn: async () => {
      const perEnv = await Promise.all(ENV_IDS.map((env) => fetchDeployments(env)));
      return ENV_IDS.map((env, i) => ({ env, count: perEnv[i].length }));
    },
  });

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold">Compare</h1>

      <Tile title="Drift matrix">
        {driftQuery.data ? <DriftHeatmap matrix={driftQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
      </Tile>

      <div className="grid gap-6" style={CHART_GRID_STYLE}>
        <Tile title="Health by environment">
          {summariesQuery.data ? <HealthGauges summaries={summariesQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
        </Tile>
        <Tile title="Health score composition">
          {summariesQuery.data ? <HealthCompositionDonuts summaries={summariesQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
        </Tile>
        <Tile title="Secret age by environment">
          {summariesQuery.data ? <SecretAgeBarChart summaries={summariesQuery.data} /> : <p className="text-sm text-text-muted">Loading…</p>}
        </Tile>
        <Tile title="Deployment frequency by environment">
          {deploymentCountsQuery.data ? (
            <DeploymentFrequencyChart counts={deploymentCountsQuery.data} />
          ) : (
            <p className="text-sm text-text-muted">Loading…</p>
          )}
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
