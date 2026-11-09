export const ENV_IDS = ["dev", "test", "prod"] as const;
export type EnvId = (typeof ENV_IDS)[number];

export interface EnvTheme {
  id: EnvId;
  label: string;
  color: string;
  glow: string;
  soft: string;
}

export const ENV_THEME: Record<EnvId, EnvTheme> = {
  dev: {
    id: "dev",
    label: "Dev",
    color: "#22d3ee",
    glow: "rgba(34, 211, 238, 0.45)",
    soft: "rgba(34, 211, 238, 0.12)",
  },
  test: {
    id: "test",
    label: "Test",
    color: "#fbbf24",
    glow: "rgba(251, 191, 36, 0.45)",
    soft: "rgba(251, 191, 36, 0.12)",
  },
  prod: {
    id: "prod",
    label: "Prod",
    color: "#fb4570",
    glow: "rgba(251, 69, 112, 0.5)",
    soft: "rgba(251, 69, 112, 0.14)",
  },
};

export function envIdFromName(name: string | undefined): EnvId {
  const normalized = (name ?? "").toLowerCase();
  if (normalized.startsWith("prod")) return "prod";
  if (normalized.startsWith("test")) return "test";
  return "dev";
}
