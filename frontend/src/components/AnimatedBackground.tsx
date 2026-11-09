import { motion } from "framer-motion";
import type { EnvTheme } from "../lib/env";

export function AnimatedBackground({ theme }: { theme: EnvTheme }) {
  return (
    <div className="pointer-events-none fixed inset-0 -z-10 overflow-hidden bg-[#0a0e1a]">
      <div
        className="absolute inset-0 opacity-[0.08]"
        style={{
          backgroundImage:
            "linear-gradient(rgba(255,255,255,0.4) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.4) 1px, transparent 1px)",
          backgroundSize: "44px 44px",
        }}
      />
      <motion.div
        animate={{
          x: ["-10%", "10%", "-10%"],
          y: ["-5%", "8%", "-5%"],
        }}
        transition={{ duration: 26, repeat: Infinity, ease: "easeInOut" }}
        className="absolute left-[-10%] top-[-10%] h-[50vw] w-[50vw] rounded-full blur-3xl"
        style={{ backgroundColor: theme.color, opacity: 0.12 }}
      />
      <motion.div
        animate={{
          x: ["8%", "-8%", "8%"],
          y: ["6%", "-6%", "6%"],
        }}
        transition={{ duration: 32, repeat: Infinity, ease: "easeInOut" }}
        className="absolute bottom-[-15%] right-[-10%] h-[45vw] w-[45vw] rounded-full blur-3xl"
        style={{ backgroundColor: theme.color, opacity: 0.08 }}
      />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_transparent_0%,_#0a0e1a_75%)]" />
    </div>
  );
}
