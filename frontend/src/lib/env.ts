export const ENV_IDS = ["dev", "test", "prod"] as const;
export type EnvId = (typeof ENV_IDS)[number];

export function envLabel(id: string): string {
  return id.charAt(0).toUpperCase() + id.slice(1);
}
