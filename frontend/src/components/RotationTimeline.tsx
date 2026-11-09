import { motion } from "framer-motion";
import { ArrowRight, History } from "lucide-react";
import type { EnvTheme } from "../lib/env";

interface RotationEvent {
  id: string;
  when: string;
  oldFingerprint: string;
  newFingerprint: string;
  status: "switched" | "in_progress" | "rolled_back";
}

// Placeholder timeline — swap for GET /api/rotations once that endpoint ships.
const MOCK_EVENTS: RotationEvent[] = [
  { id: "r3", when: "Today, 03:12 UTC", oldFingerprint: "8c1a4f02", newFingerprint: "e5f6a7b8", status: "switched" },
  { id: "r2", when: "14 days ago", oldFingerprint: "2b9d7e11", newFingerprint: "8c1a4f02", status: "switched" },
  { id: "r1", when: "28 days ago", oldFingerprint: "f40c3aa9", newFingerprint: "2b9d7e11", status: "switched" },
];

const STATUS_LABEL: Record<RotationEvent["status"], string> = {
  switched: "Switched",
  in_progress: "In progress",
  rolled_back: "Rolled back",
};

const STATUS_COLOR: Record<RotationEvent["status"], string> = {
  switched: "#34d399",
  in_progress: "#fbbf24",
  rolled_back: "#fb7185",
};

export function RotationTimeline({ theme }: { theme: EnvTheme }) {
  return (
    <div className="glass rounded-3xl border border-white/10 p-6 sm:p-8">
      <div className="mb-6 flex items-center gap-2 text-sm font-semibold text-slate-200">
        <History size={16} style={{ color: theme.color }} />
        Rotation History
      </div>

      <div className="relative ml-2 flex flex-col gap-6 border-l border-white/10 pl-6">
        {MOCK_EVENTS.map((event, i) => (
          <motion.div
            key={event.id}
            initial={{ opacity: 0, x: -16 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, amount: 0.4 }}
            transition={{ duration: 0.4, delay: i * 0.08 }}
            className="relative"
          >
            <span
              className="absolute -left-[31px] top-1 h-3 w-3 rounded-full border-2 border-[#0a0e1a]"
              style={{ backgroundColor: STATUS_COLOR[event.status] }}
            />
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2 font-mono text-sm text-slate-200">
                <span className="rounded-md bg-white/5 px-2 py-0.5">
                  {event.oldFingerprint}
                </span>
                <ArrowRight size={14} className="text-slate-500" />
                <span
                  className="rounded-md px-2 py-0.5"
                  style={{ backgroundColor: theme.soft, color: theme.color }}
                >
                  {event.newFingerprint}
                </span>
              </div>
              <span
                className="text-xs font-medium"
                style={{ color: STATUS_COLOR[event.status] }}
              >
                {STATUS_LABEL[event.status]}
              </span>
            </div>
            <div className="mt-1 text-xs text-slate-500">{event.when}</div>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
