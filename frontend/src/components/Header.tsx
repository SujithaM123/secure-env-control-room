import { motion } from "framer-motion";
import { ShieldCheck } from "lucide-react";
import { useClock } from "../hooks/useClock";

export function Header({ connected }: { connected: boolean }) {
  const now = useClock();
  const time = now.toLocaleTimeString("en-US", { hour12: false });
  const date = now.toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  });

  return (
    <header className="flex flex-wrap items-center justify-between gap-4">
      <div className="flex items-center gap-3">
        <div className="glass flex h-11 w-11 items-center justify-center rounded-xl">
          <ShieldCheck size={22} className="text-slate-200" />
        </div>
        <div>
          <h1 className="text-lg font-semibold tracking-tight text-slate-50 sm:text-xl">
            CAP-48 Control Room
          </h1>
          <p className="text-xs text-slate-400">
            Secure environment configuration &amp; secret monitoring
          </p>
        </div>
      </div>

      <div className="glass flex items-center gap-4 rounded-xl px-4 py-2.5">
        <div className="font-mono text-sm leading-tight text-slate-200">
          <div className="text-base tracking-wider">{time}</div>
          <div className="text-[11px] text-slate-500">{date}</div>
        </div>
        <div className="h-6 w-px bg-white/10" />
        <div className="flex items-center gap-2">
          <span className="relative flex h-2.5 w-2.5">
            {connected && (
              <motion.span
                className="absolute inline-flex h-full w-full rounded-full bg-emerald-400"
                animate={{ scale: [1, 2.2], opacity: [0.6, 0] }}
                transition={{ duration: 1.8, repeat: Infinity, ease: "easeOut" }}
              />
            )}
            <span
              className={`relative inline-flex h-2.5 w-2.5 rounded-full ${
                connected ? "bg-emerald-400" : "bg-rose-400"
              }`}
            />
          </span>
          <span className="text-xs font-medium text-slate-300">
            {connected ? "Connected" : "Unreachable"}
          </span>
        </div>
      </div>
    </header>
  );
}
