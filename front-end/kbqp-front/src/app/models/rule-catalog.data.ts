import { RuleCatalogEntry } from './rule-catalog.model';

export const RULE_CATALOG_ENTRIES: RuleCatalogEntry[] = [
  {
    name: 'Threshold: CNC high temperature',
    desc: 'Level 1 (template): if a CNC machine’s latest TEMPERATURE_C reading ≥ 80, insert HIGH_TEMPERATURE Condition for that machine (once per state).',
  },
  {
    name: 'Threshold: CNC high vibration',
    desc: 'Level 1 (template): CNC + VIBRATION_RMS ≥ 5.0 → HIGH_VIBRATION Condition.',
  },
  {
    name: 'Threshold: Conveyor high temperature',
    desc: 'Level 1 (template): Conveyor (LIN) + TEMPERATURE_C ≥ 70 → HIGH_TEMPERATURE Condition.',
  },
  {
    name: 'Threshold: Conveyor high vibration',
    desc: 'Level 1 (template): Conveyor + VIBRATION_RMS ≥ 4.0 → HIGH_VIBRATION Condition.',
  },
  {
    name: 'Threshold: Press high temperature',
    desc: 'Level 1 (template): Press type + TEMPERATURE_C ≥ 75 → HIGH_TEMPERATURE (no Press machine in current sim, but rule exists in KB).',
  },
  {
    name: 'Threshold: Press high vibration',
    desc: 'Level 1 (template): Press + VIBRATION_RMS ≥ 4.5 → HIGH_VIBRATION.',
  },
  {
    name: 'L2: Potential bearing failure anomaly',
    desc: 'Level 2: same machine has both HIGH_TEMPERATURE and HIGH_VIBRATION Conditions, and no existing bearing-failure Anomaly → insert POTENTIAL_BEARING_FAILURE Anomaly (timestamp = simulated clock). Applies to all machines (LIN and CNC).',
  },
  {
    name: 'CEP: Temperature rising trend',
    desc: 'Level 2 (TickStatus stream): 10 strict tick-to-tick temperature increases ending at the machine’s latest tickIndex → insert one TEMPERATURE_RISING_TREND Anomaly if none exists (one match per machine per evaluation, not every overlapping window).',
  },
  {
    name: 'CEP: Temperature rising trend cleared',
    desc: 'Level 2: only when TEMPERATURE_RISING_TREND already exists; 5 strict decreases ending at the latest tickIndex (6 ticks) → delete that anomaly (same single-window anchoring as the rise rule).',
  },
  {
    name: 'CEP: Vibration rising trend',
    desc: 'Level 2 (TickStatus.vibrationRms): same pattern as temperature — 10 strict increases ending at latest tick → VIBRATION_RISING_TREND Anomaly if missing.',
  },
  {
    name: 'CEP: Vibration rising trend cleared',
    desc: 'Level 2: only when VIBRATION_RISING_TREND exists; 5 strict decreases on vibration RMS ending at latest tick → delete that anomaly.',
  },
  {
    name: 'Simulation: bearing streak & CRITICAL intervention',
    desc: 'Engine (not DRL): counts consecutive sub-steps with POTENTIAL_BEARING_FAILURE; at 5, halts the machine, sets ComponentStatus to HealthStatus.FAIL, inserts CRITICAL intervention if missing.',
  },
  {
    name: 'Safety: operator halt required',
    desc: 'If MachineHalted fact exists for a machine and no OPERATOR_HALT UnsafeReason yet → insert UnsafeReason (halt is only lifted by POST /fix).',
  },
  {
    name: 'Safety: unsafe result',
    desc: 'If SafetyCheck exists for a machine and any UnsafeReason for that machine, and no SafetyResult yet → insert UNSAFE SafetyResult.',
  },
  {
    name: 'Safety: safe result',
    desc: 'If SafetyCheck exists, no UnsafeReason for that machine, and no SafetyResult yet → insert SAFE SafetyResult.',
  },
];
