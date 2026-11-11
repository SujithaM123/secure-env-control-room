import ReactECharts from "echarts-for-react";
import { useChartColors } from "../../lib/useChartColors";
import type { RotationEvent } from "../../types/api";

const STATUS_CODE: Record<string, number> = {
  failed: -2,
  rolled_back: -1,
  in_progress: 0,
  switched: 1,
};

const STATUS_LABEL: Record<number, string> = {
  [-2]: "Failed",
  [-1]: "Rolled back",
  [0]: "In progress",
  [1]: "Switched",
};

export function RotationTimelineChart({ events }: { events: RotationEvent[] }) {
  const colors = useChartColors();

  if (events.length === 0) {
    return (
      <p className="text-sm text-text-muted">No rotation events recorded for this environment yet.</p>
    );
  }

  const sorted = [...events].sort(
    (a, b) => new Date(a.startedAt).getTime() - new Date(b.startedAt).getTime()
  );
  const data = sorted.map((event) => [event.startedAt, STATUS_CODE[event.status] ?? 0]);

  const option = {
    backgroundColor: "transparent",
    textStyle: { color: colors.text, fontFamily: "IBM Plex Sans" },
    grid: { left: 90, right: 20, top: 20, bottom: 40 },
    tooltip: {
      trigger: "axis",
      backgroundColor: colors.surface,
      borderColor: colors.border,
      textStyle: { color: colors.text, fontFamily: "IBM Plex Mono" },
      formatter: (params: { data: [string, number] }[]) => {
        const [time, code] = params[0].data;
        return `${time}<br/>${STATUS_LABEL[code] ?? "Unknown"}`;
      },
    },
    xAxis: {
      type: "time",
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.textMuted, fontFamily: "IBM Plex Mono" },
    },
    yAxis: {
      type: "value",
      min: -2,
      max: 1,
      interval: 1,
      axisLine: { lineStyle: { color: colors.border } },
      splitLine: { lineStyle: { color: colors.border } },
      axisLabel: {
        color: colors.textMuted,
        formatter: (value: number) => STATUS_LABEL[value] ?? "",
      },
    },
    series: [
      {
        type: "line",
        step: "end",
        data,
        symbolSize: 8,
        lineStyle: { color: colors.accent, width: 2 },
        itemStyle: { color: colors.accent },
      },
    ],
  };

  return <ReactECharts option={option} style={{ height: 260 }} />;
}
