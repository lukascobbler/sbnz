import { RuleCatalogEntry } from './rule-catalog.model';

export const RULE_CATALOG_ENTRIES: RuleCatalogEntry[] = [
  {
    name: 'Threshold: LIN high temperature',
    desc: 'Level 1 (template, per machineId from MachineProcessRegistry): LIN + TEMPERATURE_C ≥ profile anomaly threshold → TEMPERATURE_ABOVE_THRESHOLD Anomaly. Thresholds are generated from the same profile as telemetry.',
  },
  {
    name: 'Threshold: LIN high vibration',
    desc: 'Level 1: LIN + VIBRATION_RMS ≥ profile anomaly threshold → VIBRATION_ABOVE_THRESHOLD Anomaly.',
  },
  {
    name: 'Threshold: CNC high temperature',
    desc: 'Level 1: CNC + TEMPERATURE_C ≥ profile anomaly threshold → TEMPERATURE_ABOVE_THRESHOLD Anomaly.',
  },
  {
    name: 'Threshold: CNC high vibration',
    desc: 'Level 1: CNC + VIBRATION_RMS ≥ profile anomaly threshold → VIBRATION_ABOVE_THRESHOLD Anomaly.',
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
    name: 'Halt: 5 consecutive ticks in thermal or vibration stress band (OR)',
    desc: 'Level 2 (TickStatus stream, engine-flagged from MachineProcessProfile): sustainedStressPresent when temp ≥ profile stress OR vib ≥ profile stress. Current defaults: LIN 78°C / 3.85 RMS; CNC 67.5°C / 5.35 RMS (each strictly above NORMAL drift envelope, below anomaly thresholds). Five consecutive ticks → halt + CRITICAL.',
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
