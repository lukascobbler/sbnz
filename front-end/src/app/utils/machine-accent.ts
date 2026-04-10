/** Number of distinct machine accent slots (must match `_machine-accents.scss`). */
export const MACHINE_ACCENT_COUNT = 8;

/** Stable index in [0, MACHINE_ACCENT_COUNT) from machine id (FNV-1a). */
export function machineAccentIndex(machineId: string | null | undefined): number {
  if (machineId == null || machineId === '') {
    return 0;
  }
  let h = 2166136261;
  for (let i = 0; i < machineId.length; i++) {
    h ^= machineId.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return Math.abs(h) % MACHINE_ACCENT_COUNT;
}

/** CSS class e.g. `machine-accent-3` for rows, pills, sensor groups. */
export function machineAccentClass(machineId: string | null | undefined): string {
  return `machine-accent-${machineAccentIndex(machineId)}`;
}
