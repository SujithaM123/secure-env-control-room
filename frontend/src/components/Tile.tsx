import type { ReactNode } from "react";

export function Tile({ title, children, className = "" }: { title?: string; children: ReactNode; className?: string }) {
  return (
    <div className={`tile rounded-sm p-4 ${className}`}>
      {title && <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-text-muted">{title}</h2>}
      {children}
    </div>
  );
}
