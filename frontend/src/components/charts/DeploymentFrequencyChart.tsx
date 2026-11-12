import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import { envLabel } from "../../lib/env";

export interface DeploymentCount {
  env: string;
  count: number;
}

export function DeploymentFrequencyChart({ counts }: { counts: DeploymentCount[] }) {
  const colors = useChartColors();

  const option = {
    backgroundColor: "transparent",
    textStyle: { color: colors.text, fontFamily: "IBM Plex Sans" },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    tooltip: {
      backgroundColor: colors.surface,
      borderColor: colors.border,
      textStyle: { color: colors.text, fontFamily: "IBM Plex Mono" },
      formatter: (p: { name: string; value: number }[]) => `${p[0].name}<br/>${p[0].value} deployment(s)`,
    },
    xAxis: {
      type: "category",
      data: counts.map((c) => envLabel(c.env)),
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted },
    },
    yAxis: {
      type: "value",
      name: "Deployments recorded",
      nameTextStyle: { color: colors.textMuted },
      minInterval: 1,
      axisLine: { lineStyle: { color: colors.border } },
      splitLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted },
    },
    series: [
      {
        name: "Deployments",
        type: "bar",
        barWidth: "45%",
        data: counts.map((c) => c.count),
        itemStyle: { color: colors.accent },
      },
    ],
  };

  return <ReactECharts option={option} style={{ height: 240 }} />;
}
