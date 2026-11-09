import { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";
import { useConfigInfo } from "./hooks/useConfigInfo";
import { useHealth } from "./hooks/useHealth";
import { useToast } from "./components/ToastHost";
import { ENV_THEME, envIdFromName, type EnvId } from "./lib/env";
import { containerVariants, itemVariants } from "./lib/motion";
import { AnimatedBackground } from "./components/AnimatedBackground";
import { Header } from "./components/Header";
import { EnvironmentSwitcher } from "./components/EnvironmentSwitcher";
import { StatusCard } from "./components/StatusCard";
import { RotationTimeline } from "./components/RotationTimeline";
import { DriftStrip } from "./components/DriftStrip";
import { Footer } from "./components/Footer";
import type { HealthState } from "./types/api";

export default function App() {
  const configQuery = useConfigInfo();
  const healthQuery = useHealth();
  const { push } = useToast();

  const [selectedEnv, setSelectedEnv] = useState<EnvId>("dev");
  const userPickedEnv = useRef(false);
  const liveEnv = configQuery.data ? envIdFromName(configQuery.data.env) : null;

  useEffect(() => {
    if (!userPickedEnv.current && liveEnv) {
      setSelectedEnv(liveEnv);
    }
  }, [liveEnv]);

  const previousStatus = useRef<HealthState | null>(null);
  useEffect(() => {
    const status = healthQuery.data?.status;
    if (!status) return;
    if (previousStatus.current && previousStatus.current !== status) {
      push(
        status === "UP"
          ? "Backend is back UP"
          : "Backend health check failed — reporting DOWN",
        status === "UP" ? "success" : "error"
      );
    }
    previousStatus.current = status;
  }, [healthQuery.data?.status, push]);

  const theme = ENV_THEME[selectedEnv];
  const health: HealthState | "UNKNOWN" = healthQuery.isError
    ? "DOWN"
    : healthQuery.data?.status ?? "UNKNOWN";
  const isLoading = configQuery.isLoading;

  return (
    <div className="min-h-screen px-4 py-6 sm:px-8 sm:py-10">
      <AnimatedBackground theme={theme} />

      <motion.div
        variants={containerVariants}
        initial="hidden"
        animate="visible"
        className="mx-auto flex max-w-5xl flex-col gap-6"
      >
        <motion.div variants={itemVariants}>
          <Header connected={!healthQuery.isError} />
        </motion.div>

        <motion.div variants={itemVariants} className="flex justify-center sm:justify-start">
          <EnvironmentSwitcher
            active={selectedEnv}
            liveEnv={liveEnv}
            onChange={(id) => {
              userPickedEnv.current = true;
              setSelectedEnv(id);
            }}
          />
        </motion.div>

        <motion.div variants={itemVariants}>
          <StatusCard
            theme={theme}
            configInfo={configQuery.data}
            health={health}
            isLoading={isLoading}
          />
        </motion.div>

        <motion.div variants={itemVariants} className="grid gap-6 lg:grid-cols-2">
          <RotationTimeline theme={theme} />
          <DriftStrip current={selectedEnv} />
        </motion.div>

        <motion.div variants={itemVariants}>
          <Footer />
        </motion.div>
      </motion.div>
    </div>
  );
}
