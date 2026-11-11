/** Displays a fingerprint/SHA/ID/timestamp in mono — never a raw secret value. */
export function Fingerprint({ value }: { value: string | null | undefined }) {
  if (!value) return <span className="font-mono text-text-muted">—</span>;
  return <span className="font-mono text-text">{value}</span>;
}
