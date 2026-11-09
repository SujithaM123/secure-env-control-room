import { motion } from "framer-motion";
import { ENV_IDS, ENV_THEME, type EnvId } from "../lib/env";

interface Props {
  active: EnvId;
  onChange: (id: EnvId) => void;
  liveEnv: EnvId | null;
}

export function EnvironmentSwitcher({ active, onChange, liveEnv }: Props) {
  return (
    <div className="glass inline-flex items-center gap-1 rounded-full p-1.5">
      {ENV_IDS.map((id) => {
        const theme = ENV_THEME[id];
        const isActive = id === active;
        return (
          <motion.button
            key={id}
            type="button"
            onClick={() => onChange(id)}
            whileTap={{ scale: 0.97 }}
            transition={{ type: "spring", stiffness: 400, damping: 20 }}
            className="relative rounded-full px-4 py-2 text-sm font-medium"
          >
            {isActive && (
              <motion.span
                layoutId="env-pill-highlight"
                className="absolute inset-0 rounded-full"
                style={{
                  backgroundColor: theme.soft,
                  boxShadow: `0 0 18px ${theme.glow}`,
                  border: `1px solid ${theme.color}66`,
                }}
                transition={{ type: "spring", stiffness: 350, damping: 30 }}
              />
            )}
            <span
              className="relative z-10 flex items-center gap-1.5"
              style={{ color: isActive ? theme.color : "#94a3b8" }}
            >
              {theme.label}
              {liveEnv === id && (
                <span
                  className="inline-block h-1.5 w-1.5 rounded-full"
                  style={{ backgroundColor: theme.color }}
                  title="Backend is currently reporting this environment"
                />
              )}
            </span>
          </motion.button>
        );
      })}
    </div>
  );
}
