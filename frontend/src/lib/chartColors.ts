/** Reads the live Signal theme tokens so ECharts (which needs literal colors) stays in sync with light/dark. */
export function getChartColors() {
  const style = getComputedStyle(document.documentElement);
  const get = (name: string) => style.getPropertyValue(name).trim();
  return {
    bg: get("--color-bg"),
    surface: get("--color-surface"),
    border: get("--color-border"),
    text: get("--color-text"),
    textMuted: get("--color-text-muted"),
    accent: get("--color-accent"),
    success: get("--color-success"),
    warning: get("--color-warning"),
    danger: get("--color-danger"),
  };
}
