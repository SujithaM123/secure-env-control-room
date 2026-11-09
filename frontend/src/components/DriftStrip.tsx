import { motion } from "framer-motion";
import { GitCompareArrows, CheckCircle2, CircleDot } from "lucide-react";
import { ENV_IDS, ENV_THEME, type EnvId } from "../lib/env";

interface SettingRow {
  key: string;
  allowedToDiffer: boolean;
  values: Record<EnvId, string>;
}

// Placeholder comparison — wire to GET /api/kpis drift data once that endpoint ships.
const SETTINGS: SettingRow[] = [
  {
    key: "LOG_LEVEL",
    allowedToDiffer: true,
    values: { dev: "DEBUG", test: "INFO", prod: "WARN" },
  },
  {
    key: "DB_HOST",
    allowedToDiffer: true,
    values: { dev: "dev-db.cap48.internal", test: "test-db.cap48.internal", prod: "prod-db.cap48.internal" },
  },
  {
    key: "SECRETS_PROVIDER",
    allowedToDiffer: false,
    values: { dev: "aws", test: "aws", prod: "aws" },
  },
  {
    key: "DB_PORT",
    allowedToDiffer: false,
    values: { dev: "3306", test: "3306", prod: "3306" },
  },
];

export function DriftStrip({ current }: { current: EnvId }) {
  const others = ENV_IDS.filter((id) => id !== current);
  const theme = ENV_THEME[current];

  return (
    <div className="glass rounded-3xl border border-white/10 p-6 sm:p-8">
      <div className="mb-6 flex items-center gap-2 text-sm font-semibold text-slate-200">
        <GitCompareArrows size={16} style={{ color: theme.color }} />
        Drift Status — {theme.label} vs. others
      </div>

      <div className="flex flex-col gap-3">
        {SETTINGS.map((row, i) => (
          <motion.div
            key={row.key}
            initial={{ opacity: 0, y: 8 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, amount: 0.4 }}
            transition={{ duration: 0.35, delay: i * 0.05 }}
            className="flex flex-wrap items-center justify-between gap-3 rounded-xl bg-white/[0.03] px-4 py-3"
          >
            <div className="font-mono text-xs text-slate-300 sm:text-sm">
              {row.key}
              <span
                className="ml-2 font-mono text-[11px]"
                style={{ color: theme.color }}
              >
                {row.values[current]}
              </span>
            </div>

            <div className="flex items-center gap-4">
              {others.map((otherId) => {
                const matches = row.values[otherId] === row.values[current];
                const otherTheme = ENV_THEME[otherId];
                return (
                  <div
                    key={otherId}
                    className="flex items-center gap-1.5 text-xs text-slate-400"
                    title={`${otherTheme.label}: ${row.values[otherId]}`}
                  >
                    {matches ? (
                      <CheckCircle2 size={14} className="text-emerald-400" />
                    ) : row.allowedToDiffer ? (
                      <CircleDot size={14} className="text-amber-400" />
                    ) : (
                      <CircleDot size={14} className="text-rose-400" />
                    )}
                    <span style={{ color: otherTheme.color }}>
                      {otherTheme.label}
                    </span>
                  </div>
                );
              })}
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
