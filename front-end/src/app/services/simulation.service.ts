import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, tap, throwError } from 'rxjs';
import { MachineHealthReport, SimulationReport } from '../models/simulation.types';
import { SensorHistoryService } from './sensor-history.service';

function stripMachineDiagnoses(r: SimulationReport, machineId: string): SimulationReport {
  return {
    ...r,
    anomalies: r.anomalies.filter((a) => a.machineId !== machineId),
    interventions: r.interventions.filter((i) => i.machineId !== machineId),
  };
}

@Injectable({ providedIn: 'root' })
export class SimulationService {
  private sse: EventSource | null = null;
  private reconnectTimer: number | null = null;

  private pendingStripDiagnosesFor: string | null = null;

  private lastSensorSnapshotSig: string | null = null;
  private pendingReport: SimulationReport | null | undefined = undefined;
  private rafId: number | null = null;

  readonly connected = signal(false);
  readonly error = signal<string | null>(null);
  readonly report = signal<SimulationReport | null>(null);

  constructor(
    private readonly http: HttpClient,
    private readonly sensorHistory: SensorHistoryService,
  ) {}

  resetSensorHistoryDedupe(): void {
    this.lastSensorSnapshotSig = null;
  }

  private sensorSnapshotSig(r: SimulationReport): string {
    const trace = r.sensorSnapshotsThisTick;
    if (trace && trace.length > 0) {
      return `${r.simulatedTime}\n${JSON.stringify(trace)}`;
    }
    return `${r.simulatedTime}\n${JSON.stringify(r.sensors ?? [])}`;
  }

  private appendSensorHistoryFromReport(next: SimulationReport): void {
    const trace = next.sensorSnapshotsThisTick;
    if (trace && trace.length > 0) {
      for (const batch of trace) {
        if (batch?.length) {
          this.sensorHistory.appendFromSnapshot(batch);
        }
      }
      return;
    }
    const sensors = next.sensors;
    if (sensors?.length) {
      this.sensorHistory.appendFromSnapshot(sensors);
    }
  }

  private flushPendingReport(): void {
    this.rafId = null;
    if (this.pendingReport !== undefined) {
      this.report.set(this.pendingReport);
      this.pendingReport = undefined;
    }
  }

  ingestReport(next: SimulationReport | null): void {
    if (next == null) {
      this.lastSensorSnapshotSig = null;
      this.pendingReport = null;
      if (this.rafId != null) {
        cancelAnimationFrame(this.rafId);
        this.rafId = null;
      }
      this.report.set(null);
      return;
    }

    const sig = this.sensorSnapshotSig(next);
    const hasTrace = next.sensorSnapshotsThisTick && next.sensorSnapshotsThisTick.length > 0;
    const hasSensors = next.sensors?.length;
    if (hasTrace || hasSensors) {
      if (sig !== this.lastSensorSnapshotSig) {
        this.lastSensorSnapshotSig = sig;
        this.appendSensorHistoryFromReport(next);
      }
    }

    this.pendingReport = next;
    if (this.rafId == null) {
      this.rafId = requestAnimationFrame(() => this.flushPendingReport());
    }
  }

  connect() {
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    this.error.set(null);
    this.connected.set(false);
    this.sse?.close();

    this.sse = new EventSource('/api/v1/sim/stream');
    this.sse.addEventListener('snapshot', (ev: MessageEvent) => {
      try {
        let next = JSON.parse(ev.data) as SimulationReport;
        const id = this.pendingStripDiagnosesFor;
        if (id) {
          next = stripMachineDiagnoses(next, id);
        }
        this.ingestReport(next);
        this.connected.set(true);
        this.error.set(null);
      } catch {
      }
    });
    this.sse.onerror = () => {
      this.connected.set(false);
      this.error.set('Stream disconnected. Is the backend running?');
      this.sse?.close();
      this.sse = null;
      if (!this.reconnectTimer) {
        this.reconnectTimer = window.setTimeout(() => {
          this.reconnectTimer = null;
          this.connect();
        }, 5000);
      }
    };
  }

  disconnect() {
    this.sse?.close();
    this.sse = null;
    this.connected.set(false);
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  reset() {
    return this.http.post<SimulationReport>('/api/v1/sim/reset', {});
  }

  setTickMinutes(tickMinutes: number) {
    return this.http.post<SimulationReport>('/api/v1/sim/tick', { tickMinutes });
  }

  step(stepMinutes: number) {
    return this.http.post<SimulationReport>('/api/v1/sim/step', { stepMinutes });
  }

  setWorkload(machineId: string, workload: string, metricKey: string) {
    return this.http.post<SimulationReport>('/api/v1/sim/workload', { machineId, workload, metricKey });
  }

  getMachineHealth(machineId: string) {
    const id = encodeURIComponent(machineId.trim());
    return this.http.get<MachineHealthReport>(`/api/v1/sim/machines/${id}/health`);
  }

  safetyFix(machineId: string) {
    const trimmed = machineId.trim();
    this.pendingStripDiagnosesFor = trimmed;
    const cur = this.report();
    if (cur) {
      this.ingestReport(stripMachineDiagnoses(cur, trimmed));
    }
    return this.http.post<SimulationReport>('/api/v1/sim/fix', { machineId: trimmed }).pipe(
      tap((r) => {
        this.pendingStripDiagnosesFor = null;
        this.ingestReport(r);
      }),
      catchError((e) => {
        this.pendingStripDiagnosesFor = null;
        return throwError(() => e);
      })
    );
  }
}
