import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import type { EnvironmentSummary } from "../../types/api";
import { envLabel } from "../../lib/env";

export function SecretAgeBarChart({ summaries }: { summaries: EnvironmentSummary[] }) {
  const colors = useChartColors();

  const option = {
    backgroundColor: "transparent",
    textStyle: { color: colors.text, fontFamily: "IBM Plex Sans" },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    tooltip: {
      backgroundColor: colors.surface,
      borderColor: colors.border,
      textStyle: { color: colors.text, fontFamily: "IBM Plex Mono" },
      formatter: (p: { name: string; value: number | null }[]) =>
        `${p[0].name}<br/>${p[0].value == null ? "Never rotated" : `${p[0].value} day(s)`}`,
    },
    xAxis: {
      type: "category",
      data: summaries.map((s) => envLabel(s.name)),
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted },
    },
    yAxis: {
      type: "value",
      name: "Days since last rotation",
      nameTextStyle: { color: colors.textMuted },
      axisLine: { lineStyle: { color: colors.border } },
      splitLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted },
    },
    series: [
      {
        name: "Secret age",
        type: "bar",
        barWidth: "45%",
        data: summaries.map((s) => ({
          // null (never rotated) is left as null, not coerced to 0, so it
          // renders as no bar rather than a misleading zero-height one, and
          // the tooltip's `value == null` check below actually fires.
          value: s.secretAgeDays,
          itemStyle: { color: s.rotationOverdue ? colors.danger : colors.accent },
        })),
      },
    ],
  };

  return <ReactECharts option={option} style={{ height: 240 }} />;
}
