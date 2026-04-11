export const MACHINE_ACCENT_COUNT = 8;

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

export function machineAccentClass(machineId: string | null | undefined): string {
  return `machine-accent-${machineAccentIndex(machineId)}`;
}
