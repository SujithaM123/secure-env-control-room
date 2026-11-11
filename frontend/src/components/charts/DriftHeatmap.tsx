import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import type { DriftMatrix } from "../../types/api";
import { ENV_IDS, envLabel } from "../../lib/env";

export function DriftHeatmap({ matrix }: { matrix: DriftMatrix }) {
  const colors = useChartColors();

  if (!matrix.comparedAt) {
    return <p className="text-sm text-text-muted">No drift comparison recorded yet.</p>;
  }

  const data = matrix.pairs.map((pair) => [
    ENV_IDS.indexOf(pair.envA as (typeof ENV_IDS)[number]),
    ENV_IDS.indexOf(pair.envB as (typeof ENV_IDS)[number]),
    pair.driftCount,
  ]);
  const maxDrift = Math.max(1, ...matrix.pairs.map((p) => p.driftCount));

  const option = {
    backgroundColor: "transparent",
    textStyle: { color: colors.text, fontFamily: "IBM Plex Sans" },
    tooltip: {
      backgroundColor: colors.surface,
      borderColor: colors.border,
      textStyle: { color: colors.text, fontFamily: "IBM Plex Mono" },
      formatter: (p: { data: [number, number, number] }) => {
        const [x, y, value] = p.data;
        return `${envLabel(ENV_IDS[x])} vs ${envLabel(ENV_IDS[y])}<br/>${value} unexpected drift item(s)`;
      },
    },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: "category",
      data: ENV_IDS.map(envLabel),
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted },
      splitArea: { show: false },
    },
    yAxis: {
      type: "category",
      data: ENV_IDS.map(envLabel),
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted },
      splitArea: { show: false },
    },
    visualMap: {
      min: 0,
      max: maxDrift,
      show: false,
      inRange: { color: [colors.surface, colors.warning, colors.danger] },
    },
    series: [
      {
        type: "heatmap",
        data,
        label: { show: true, color: colors.text, fontFamily: "IBM Plex Mono" },
        itemStyle: { borderColor: colors.border, borderWidth: 1 },
      },
    ],
  };

  return <ReactECharts option={option} style={{ height: 260 }} />;
}
