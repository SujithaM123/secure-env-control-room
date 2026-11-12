import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import type { EnvironmentSummary } from "../../types/api";
import { envLabel } from "../../lib/env";
import { healthTone } from "../../lib/health";

export function HealthGauges({ summaries }: { summaries: EnvironmentSummary[] }) {
  const colors = useChartColors();

  const option = {
    backgroundColor: "transparent",
    series: summaries.map((summary, index) => ({
      type: "gauge",
      center: [`${(index + 0.5) * (100 / summaries.length)}%`, "55%"],
      radius: "80%",
      startAngle: 200,
      endAngle: -20,
      min: 0,
      max: 100,
      progress: { show: true, width: 10, itemStyle: { color: colors[toneColorKey(summary.healthScore)] } },
      axisLine: { lineStyle: { width: 10, color: [[1, colors.border]] } },
      pointer: { show: false },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: {
        valueAnimation: true,
        formatter: () => `${summary.healthScore}`,
        color: colors.text,
        fontFamily: "IBM Plex Mono",
        fontSize: 22,
        offsetCenter: [0, "0%"],
      },
      title: {
        offsetCenter: [0, "70%"],
        color: colors.textMuted,
        fontFamily: "IBM Plex Sans",
        fontSize: 13,
      },
      data: [{ value: summary.healthScore, name: envLabel(summary.name) }],
    })),
  };

  return <ReactECharts option={option} style={{ height: 220 }} />;
}

function toneColorKey(score: number): "success" | "warning" | "danger" {
  const tone = healthTone(score);
  return tone === "neutral" ? "danger" : tone;
}
