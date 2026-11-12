import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import { healthComponents } from "../../lib/health";
import type { EnvironmentSummary } from "../../types/api";
import { envLabel } from "../../lib/env";

/** The only pie/donut in the app — genuine part-to-whole composition of each environment's health score (validation / drift / rotation-freshness), mirroring the backend's own formula. */
export function HealthCompositionDonuts({ summaries }: { summaries: EnvironmentSummary[] }) {
  const colors = useChartColors();

  const option = {
    backgroundColor: "transparent",
    textStyle: { fontFamily: "IBM Plex Sans" },
    tooltip: {
      backgroundColor: colors.surface,
      borderColor: colors.border,
      textStyle: { color: colors.text, fontFamily: "IBM Plex Mono" },
    },
    series: summaries.map((summary, index) => {
      const components = healthComponents(summary);
      return {
        type: "pie",
        radius: ["42%", "62%"],
        center: [`${(index + 0.5) * (100 / summaries.length)}%`, "50%"],
        avoidLabelOverlap: true,
        label: {
          show: true,
          position: "center",
          formatter: () => `${envLabel(summary.name)}\n${summary.healthScore}`,
          color: colors.text,
          fontFamily: "IBM Plex Mono",
          fontSize: 13,
          lineHeight: 16,
        },
        labelLine: { show: false },
        data: [
          { name: "Validation", value: components.validation, itemStyle: { color: colors.success } },
          { name: "Drift-free", value: components.drift, itemStyle: { color: colors.accent } },
          { name: "Rotation freshness", value: components.rotation, itemStyle: { color: colors.warning } },
        ],
      };
    }),
    legend: {
      bottom: 0,
      textStyle: { color: colors.textMuted, fontFamily: "IBM Plex Sans", fontSize: 12 },
    },
  };

  return <ReactECharts option={option} style={{ height: 280 }} />;
}
