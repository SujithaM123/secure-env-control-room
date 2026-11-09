import { useEffect, useRef, useState } from "react";
import { Lock } from "lucide-react";
import type { EnvTheme } from "../lib/env";

const SCRAMBLE_CHARS = "0123456789abcdef";

function randomChar() {
  return SCRAMBLE_CHARS[Math.floor(Math.random() * SCRAMBLE_CHARS.length)];
}

export function FingerprintCapsule({
  fingerprint,
  theme,
}: {
  fingerprint: string;
  theme: EnvTheme;
}) {
  const [display, setDisplay] = useState(fingerprint);
  const lastValue = useRef<string>("");

  useEffect(() => {
    if (!fingerprint || fingerprint === lastValue.current) return;
    lastValue.current = fingerprint;

    const length = fingerprint.length;
    const revealDelayPerChar = 70;
    const scrambleFrame = 40;
    let frame = 0;
    const totalFrames = Math.ceil((length * revealDelayPerChar) / scrambleFrame) + 4;

    const interval = window.setInterval(() => {
      frame += 1;
      const lockedCount = Math.min(
        length,
        Math.floor((frame * scrambleFrame) / revealDelayPerChar)
      );
      const next = fingerprint
        .split("")
        .map((char, i) => (i < lockedCount ? char : randomChar()))
        .join("");
      setDisplay(next);

      if (frame >= totalFrames) {
        setDisplay(fingerprint);
        window.clearInterval(interval);
      }
    }, scrambleFrame);

    return () => window.clearInterval(interval);
  }, [fingerprint]);

  return (
    <div
      className="glass inline-flex items-center gap-2 rounded-full px-4 py-2"
      style={{ border: `1px solid ${theme.color}55` }}
    >
      <Lock size={14} style={{ color: theme.color }} />
      <span className="font-mono text-sm tracking-wider text-slate-100">
        {display}
      </span>
    </div>
  );
}
