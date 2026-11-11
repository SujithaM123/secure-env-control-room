export type StatusTone = "success" | "warning" | "danger" | "neutral";

const TONE_CLASS: Record<StatusTone, string> = {
  success: "bg-success",
  warning: "bg-warning",
  danger: "bg-danger",
  neutral: "bg-text-muted",
};

/** Every status indicator shows a colored dot plus text — never color alone. */
export function StatusDot({ tone, label }: { tone: StatusTone; label: string }) {
  return (
    <span className="inline-flex items-center gap-2">
      <span className={`h-2 w-2 shrink-0 rounded-full ${TONE_CLASS[tone]}`} aria-hidden="true" />
      <span>{label}</span>
    </span>
  );
}
