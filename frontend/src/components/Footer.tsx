import { ShieldAlert } from "lucide-react";

export function Footer() {
  return (
    <footer className="flex justify-center py-6">
      <div className="glass flex items-center gap-2 rounded-full border border-white/10 px-4 py-2 text-xs text-slate-400">
        <ShieldAlert size={14} className="text-slate-500" />
        No secret values are ever displayed — fingerprints only.
      </div>
    </footer>
  );
}
