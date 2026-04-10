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
  metricKey?: string | null;
  description: string;
  detectedAt: string;
}

export interface Intervention {
  machineId: string;
  priority: string;
  sourceRule?: string | null;
  recommendation: string;
  decidedAt: string;
}

export interface SafetyResult {
  machineId: string;
  safe: boolean;
  reason: string;
  evaluatedAt: string;
}

export interface MetricProfileView {
  metricKey: string;
  displayName: string;
  unit: string;
  decimals: number;
  workloadEnabled: boolean;
  trendEnabled: boolean;
  anomalyEnabled: boolean;
  stressEnabled: boolean;
}

export interface MachineProfileView {
  machineId: string;
  displayName: string;
  machineType: string;
  metrics: MetricProfileView[];
}

export interface SimulationReport {
  simulatedTime: string;
  tickMinutes: number;
  safetyHaltedMachineIds?: string[];
  machineProfiles?: MachineProfileView[];
  machineMetricWorkloads?: Record<string, Record<string, MachineWorkload>>;
  rulesFiredThisTick: RuleFiring[];
  sensors: SensorStatus[];
  sensorSnapshotsThisTick?: SensorStatus[][];
  anomalies: Anomaly[];
  interventions: Intervention[];
  safetyResults: SafetyResult[];
}
