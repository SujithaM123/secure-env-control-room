import { motion } from "framer-motion";
import { Cpu, Tag } from "lucide-react";
import type { ConfigInfo, HealthState } from "../types/api";
import type { EnvTheme } from "../lib/env";
import { HealthPulse } from "./HealthPulse";
import { FingerprintCapsule } from "./FingerprintCapsule";
import { Skeleton } from "./Skeleton";

interface Props {
  theme: EnvTheme;
  configInfo: ConfigInfo | undefined;
  health: HealthState | "UNKNOWN";
  isLoading: boolean;
}

export function StatusCard({ theme, configInfo, health, isLoading }: Props) {
  return (
    <motion.div
      whileHover={{ y: -4 }}
      animate={{
        borderColor: `${theme.color}4d`,
        boxShadow: `0 20px 60px -20px ${theme.glow}`,
      }}
      transition={{ duration: 0.6 }}
      className="glass flex flex-col gap-6 rounded-3xl border p-6 sm:flex-row sm:items-center sm:gap-10 sm:p-8"
    >
      <HealthPulse status={isLoading ? "UNKNOWN" : health} size={128} />

      <div className="flex flex-1 flex-col gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs uppercase tracking-[0.2em] text-slate-500">
            <span
              className="h-1.5 w-1.5 rounded-full"
              style={{ backgroundColor: theme.color }}
            />
            Environment
          </div>
          {isLoading ? (
            <Skeleton className="mt-2 h-10 w-40" />
          ) : (
            <h2 className="font-mono text-4xl font-bold tracking-tight text-slate-50">
              {configInfo?.env ?? "unknown"}
            </h2>
          )}
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="glass flex items-center gap-2 rounded-full px-3 py-1.5 text-xs text-slate-300">
            <Tag size={13} className="text-slate-400" />
            {isLoading ? (
              <Skeleton className="h-3 w-16" />
            ) : (
              <span className="font-mono">{configInfo?.version ?? "n/a"}</span>
            )}
          </div>

          <div className="glass flex items-center gap-2 rounded-full px-3 py-1.5 text-xs text-slate-300">
            <Cpu size={13} className="text-slate-400" />
            {isLoading ? (
              <Skeleton className="h-3 w-10" />
            ) : (
              <span
                className="font-semibold"
                style={{ color: health === "UP" ? "#34d399" : "#fb7185" }}
              >
                {health}
              </span>
            )}
          </div>
        </div>

        <div>
          <div className="mb-1.5 text-xs uppercase tracking-[0.2em] text-slate-500">
            Secret fingerprint
          </div>
          {isLoading ? (
            <Skeleton className="h-8 w-44 rounded-full" />
          ) : (
            <FingerprintCapsule
              fingerprint={configInfo?.secretFingerprint ?? "--------"}
              theme={theme}
            />
          )}
        </div>
      </div>
    </motion.div>
  );
}
