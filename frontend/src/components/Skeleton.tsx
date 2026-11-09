export function Skeleton({ className = "" }: { className?: string }) {
  return (
    <div
      className={`skeleton-shimmer rounded-md bg-white/5 ${className}`}
      aria-hidden="true"
    />
  );
}
