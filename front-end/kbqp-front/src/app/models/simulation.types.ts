export type MachineWorkload = 'NORMAL' | 'OVERWORKED' | 'REST';

export interface RuleFiring {
  ruleName: string;
  machineId: string | null;
  output?: string | null;
  firedAt?: string | null;
}

export interface SensorStatus {
  machineId: string;
  metric: string;
  value: number;
  ts: string;
}

export interface Anomaly {
  machineId: string;
  type: string;
  description: string;
  detectedAt: string;
}

export interface Intervention {
  machineId: string;
  priority: string;
  recommendation: string;
  decidedAt: string;
}

export interface SafetyResult {
  machineId: string;
  safe: boolean;
  reason: string;
  evaluatedAt: string;
}

export interface SimulationReport {
  simulatedTime: string;
  tickMinutes: number;
  safetyHaltedMachineIds?: string[];
  machineWorkloads?: Record<string, MachineWorkload>;
  rulesFiredThisTick: RuleFiring[];

  sensors: SensorStatus[];
  sensorSnapshotsThisTick?: SensorStatus[][];
  anomalies: Anomaly[];
  interventions: Intervention[];
  safetyResults: SafetyResult[];
}
