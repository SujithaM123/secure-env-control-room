import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import { healthComponents } from "../../lib/health";
import type { EnvironmentSummary } from "../../types/api";
import { envLabel } from "../../lib/env";

const LEGEND_ITEMS = (colors: ReturnType<typeof useChartColors>) => [
  { label: "Validation", color: colors.success },
  { label: "Drift-free", color: colors.accent },
  { label: "Rotation freshness", color: colors.warning },
];

/**
 * The only pie in the app — genuine part-to-whole composition of each
 * environment's health score (validation / drift / rotation-freshness),
 * mirroring the backend's own formula. Solid pies, not donuts - and one
 * independent chart instance per environment (not 3 series sharing one
 * canvas), since ECharts sizes pie radius as a % of the shared canvas's
 * own min(width, height), not of a per-series slot - three large radii on
 * one canvas overlapped regardless of center position.
 */
export function HealthCompositionDonuts({ summaries }: { summaries: EnvironmentSummary[] }) {
  const colors = useChartColors();

  return (
    <div className="flex flex-col gap-3">
      <div className="grid gap-2" style={{ gridTemplateColumns: `repeat(${summaries.length}, minmax(0, 1fr))` }}>
        {summaries.map((summary) => {
          const components = healthComponents(summary);
          const option = {
            backgroundColor: "transparent",
            tooltip: {
              backgroundColor: colors.surface,
              borderColor: colors.border,
              textStyle: { color: colors.text, fontFamily: "IBM Plex Mono" },
            },
            series: [
              {
                type: "pie",
                radius: "70%",
                center: ["50%", "50%"],
                label: { show: false },
                labelLine: { show: false },
                data: [
                  { name: "Validation", value: components.validation, itemStyle: { color: colors.success } },
                  { name: "Drift-free", value: components.drift, itemStyle: { color: colors.accent } },
                  { name: "Rotation freshness", value: components.rotation, itemStyle: { color: colors.warning } },
                ],
              },
            ],
          };
          return (
            <div key={summary.name} className="flex flex-col items-center gap-1">
              <ReactECharts option={option} style={{ height: 160, width: "100%" }} />
              <p className="font-mono text-sm text-text">
                {envLabel(summary.name)} · {summary.healthScore}
              </p>
            </div>
          );
        })}
      </div>
      <div className="flex flex-wrap justify-center gap-4">
        {LEGEND_ITEMS(colors).map((item) => (
          <span key={item.label} className="inline-flex items-center gap-1.5 text-xs text-text-muted">
            <span className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: item.color }} aria-hidden="true" />
            {item.label}
          </span>
        ))}
      </div>
    </div>
  );
}
