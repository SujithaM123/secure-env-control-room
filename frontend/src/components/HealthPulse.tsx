import { motion, AnimatePresence } from "framer-motion";
import { CheckCircle2, AlertTriangle, Activity } from "lucide-react";
import type { HealthState } from "../types/api";

interface Props {
  status: HealthState | "UNKNOWN";
  size?: number;
}

const PALETTE: Record<string, { ring: string; glow: string; icon: typeof CheckCircle2 }> = {
  UP: { ring: "#34d399", glow: "rgba(52, 211, 153, 0.5)", icon: CheckCircle2 },
  DOWN: { ring: "#fb7185", glow: "rgba(251, 113, 133, 0.5)", icon: AlertTriangle },
  UNKNOWN: { ring: "#94a3b8", glow: "rgba(148, 163, 184, 0.4)", icon: Activity },
};

export function HealthPulse({ status, size = 120 }: Props) {
  const palette = PALETTE[status];
  const Icon = palette.icon;

  return (
    <div
      className="relative flex items-center justify-center"
      style={{ width: size, height: size }}
    >
      {status !== "UNKNOWN" && (
        <motion.span
          className="absolute inset-0 rounded-full"
          style={{ border: `2px solid ${palette.ring}` }}
          animate={{ scale: [1, 1.35, 1], opacity: [0.7, 0, 0.7] }}
          transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
        />
      )}
      <motion.div
        animate={{
          borderColor: palette.ring,
          boxShadow: `0 0 36px ${palette.glow}`,
        }}
        transition={{ duration: 0.6 }}
        className="glass flex items-center justify-center rounded-full border-2"
        style={{ width: size * 0.74, height: size * 0.74 }}
      >
        <AnimatePresence mode="wait">
          <motion.div
            key={status}
            initial={{ opacity: 0, scale: 0.6, rotate: -45 }}
            animate={{ opacity: 1, scale: 1, rotate: 0 }}
            exit={{ opacity: 0, scale: 0.6 }}
            transition={{ type: "spring", stiffness: 260, damping: 18 }}
          >
            <Icon size={size * 0.32} style={{ color: palette.ring }} />
          </motion.div>
        </AnimatePresence>
      </motion.div>
    </div>
  );
}
