import { RuleCatalogEntry } from './rule-catalog.model';

/** Threshold / CEP rules are generated from MachineProcessRegistry at backend startup; names must match DRL. */
export const RULE_CATALOG_ENTRIES: RuleCatalogEntry[] = [
  { name: 'Threshold: CLM AMBIENT_C high', desc: 'Profile anomaly: CLM ambient temperature at or above configured high band.' },
  { name: 'Threshold: CLM HUMIDITY_PCT high', desc: 'Profile anomaly: CLM humidity at or above configured high band.' },
  { name: 'Threshold: LIN VIBRATION_RMS high', desc: 'Profile anomaly: LIN vibration at or above configured high band.' },
  { name: 'Threshold: CNC TEMPERATURE_C high', desc: 'Profile anomaly: CNC temperature at or above configured high band.' },
  { name: 'Threshold: CNC VIBRATION_RMS high', desc: 'Profile anomaly: CNC vibration at or above configured high band.' },
  { name: 'Threshold: CNC SPINDLE_LOAD_PCT high', desc: 'Profile anomaly: CNC spindle load at or above configured high band.' },
  { name: 'Threshold: PKG REJECT_PCT high', desc: 'Profile anomaly: PKG reject rate at or above configured high band.' },
  { name: 'Threshold: PKG SEAL_TEMP_C high', desc: 'Profile anomaly: PKG seal temperature at or above configured high band.' },

  { name: 'CEP: CLM AMBIENT_C rising trend', desc: 'Ten strictly increasing metric ticks for CLM AMBIENT_C.' },
  { name: 'CEP: CLM AMBIENT_C rising trend cleared', desc: 'Five decreasing ticks clear CLM ambient rising-trend anomaly.' },
  { name: 'CEP: CLM HUMIDITY_PCT rising trend', desc: 'Ten strictly increasing metric ticks for CLM HUMIDITY_PCT.' },
  { name: 'CEP: CLM HUMIDITY_PCT rising trend cleared', desc: 'Five decreasing ticks clear CLM humidity rising-trend anomaly.' },
  { name: 'CEP: LIN VIBRATION_RMS rising trend', desc: 'Ten strictly increasing metric ticks for LIN VIBRATION_RMS.' },
  { name: 'CEP: LIN VIBRATION_RMS rising trend cleared', desc: 'Five decreasing ticks clear LIN vibration rising-trend anomaly.' },
  { name: 'CEP: LIN BELT_SPEED_PCT rising trend', desc: 'Ten strictly increasing metric ticks for LIN BELT_SPEED_PCT.' },
  { name: 'CEP: LIN BELT_SPEED_PCT rising trend cleared', desc: 'Five decreasing ticks clear LIN belt speed rising-trend anomaly.' },
  { name: 'CEP: CNC TEMPERATURE_C rising trend', desc: 'Ten strictly increasing metric ticks for CNC TEMPERATURE_C.' },
  { name: 'CEP: CNC TEMPERATURE_C rising trend cleared', desc: 'Five decreasing ticks clear CNC temperature rising-trend anomaly.' },
  { name: 'CEP: CNC VIBRATION_RMS rising trend', desc: 'Ten strictly increasing metric ticks for CNC VIBRATION_RMS.' },
  { name: 'CEP: CNC VIBRATION_RMS rising trend cleared', desc: 'Five decreasing ticks clear CNC vibration rising-trend anomaly.' },
  { name: 'CEP: CNC SPINDLE_LOAD_PCT rising trend', desc: 'Ten strictly increasing metric ticks for CNC SPINDLE_LOAD_PCT.' },
  { name: 'CEP: CNC SPINDLE_LOAD_PCT rising trend cleared', desc: 'Five decreasing ticks clear CNC spindle load rising-trend anomaly.' },
  { name: 'CEP: PKG CASES_PER_MIN rising trend', desc: 'Ten strictly increasing metric ticks for PKG CASES_PER_MIN.' },
  { name: 'CEP: PKG CASES_PER_MIN rising trend cleared', desc: 'Five decreasing ticks clear PKG throughput rising-trend anomaly.' },
  { name: 'CEP: PKG REJECT_PCT rising trend', desc: 'Ten strictly increasing metric ticks for PKG REJECT_PCT.' },
  { name: 'CEP: PKG REJECT_PCT rising trend cleared', desc: 'Five decreasing ticks clear PKG reject rate rising-trend anomaly.' },
  { name: 'CEP: PKG SEAL_TEMP_C rising trend', desc: 'Ten strictly increasing metric ticks for PKG SEAL_TEMP_C.' },
  { name: 'CEP: PKG SEAL_TEMP_C rising trend cleared', desc: 'Five decreasing ticks clear PKG seal temperature rising-trend anomaly.' },

  {
    name: 'Cross-machine: Shared environment (humidity + CNC thermal stress)',
    desc: 'Same simulation sub-step: CLM humidity ≥ profile high band and CNC temperature in stress band → MEDIUM intervention on CNC.',
  },
  {
    name: 'Cross-machine: Upstream coupling (slow belt + low pack throughput)',
    desc: 'Same simulation sub-step: LIN belt speed ≤ 84% and PKG throughput ≤ 98 cases/min → HIGH (non-critical) intervention on PKG.',
  },
  {
    name: 'Halt: 5 consecutive ticks in critical metric stress band (OR)',
    desc: 'Machine halts after five consecutive ticks with sustained stress; stress uses profile metrics that define a stress threshold (OR).',
  },
  {
    name: 'Safety: halted machine is unsafe',
    desc: 'If MachineHalted exists and OPERATOR_HALT UnsafeReason is missing, insert UnsafeReason.',
  },
  { name: 'Safety: unsafe result', desc: 'SafetyCheck plus any UnsafeReason yields UNSAFE SafetyResult.' },
  { name: 'Safety: safe result', desc: 'SafetyCheck with no UnsafeReason yields SAFE SafetyResult.' },
];
