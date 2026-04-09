import { RuleCatalogEntry } from './rule-catalog.model';

export const RULE_CATALOG_ENTRIES: RuleCatalogEntry[] = [
  {
    name: 'Threshold: LIN TEMPERATURE_C high',
    desc: 'Generated from machine profile: anomaly rule for LIN/TEMPERATURE_C using the profile threshold.',
  },
  {
    name: 'Threshold: LIN VIBRATION_RMS high',
    desc: 'Generated from machine profile: anomaly rule for LIN/VIBRATION_RMS using the profile threshold.',
  },
  {
    name: 'Threshold: CNC TEMPERATURE_C high',
    desc: 'Generated from machine profile: anomaly rule for CNC/TEMPERATURE_C using the profile threshold.',
  },
  {
    name: 'Threshold: CNC VIBRATION_RMS high',
    desc: 'Generated from machine profile: anomaly rule for CNC/VIBRATION_RMS using the profile threshold.',
  },
  {
    name: 'CEP: LIN TEMPERATURE_C rising trend',
    desc: 'Generated trend rule: 10 strictly increasing metric ticks for LIN/TEMPERATURE_C.',
  },
  {
    name: 'CEP: LIN TEMPERATURE_C rising trend cleared',
    desc: 'Generated trend clear rule: 5 strictly decreasing metric ticks clears LIN/TEMPERATURE_C rising anomaly.',
  },
  {
    name: 'CEP: LIN VIBRATION_RMS rising trend',
    desc: 'Generated trend rule: 10 strictly increasing metric ticks for LIN/VIBRATION_RMS.',
  },
  {
    name: 'CEP: LIN VIBRATION_RMS rising trend cleared',
    desc: 'Generated trend clear rule: 5 strictly decreasing metric ticks clears LIN/VIBRATION_RMS rising anomaly.',
  },
  {
    name: 'CEP: CNC TEMPERATURE_C rising trend',
    desc: 'Generated trend rule: 10 strictly increasing metric ticks for CNC/TEMPERATURE_C.',
  },
  {
    name: 'CEP: CNC TEMPERATURE_C rising trend cleared',
    desc: 'Generated trend clear rule: 5 strictly decreasing metric ticks clears CNC/TEMPERATURE_C rising anomaly.',
  },
  {
    name: 'CEP: CNC VIBRATION_RMS rising trend',
    desc: 'Generated trend rule: 10 strictly increasing metric ticks for CNC/VIBRATION_RMS.',
  },
  {
    name: 'CEP: CNC VIBRATION_RMS rising trend cleared',
    desc: 'Generated trend clear rule: 5 strictly decreasing metric ticks clears CNC/VIBRATION_RMS rising anomaly.',
  },
  {
    name: 'Halt: 5 consecutive ticks in critical metric stress band (OR)',
    desc: 'Machine halts after 5 consecutive ticks with sustained stressPresent=true. stressPresent is computed from machine profile critical metric stress bands (OR over metrics).',
  },
  {
    name: 'Safety: operator halt required',
    desc: 'If MachineHalted exists and OPERATOR_HALT reason does not, insert UnsafeReason.',
  },
  {
    name: 'Safety: unsafe result',
    desc: 'SafetyCheck + any UnsafeReason -> UNSAFE SafetyResult.',
  },
  {
    name: 'Safety: safe result',
    desc: 'SafetyCheck + no UnsafeReason -> SAFE SafetyResult.',
  },
];
