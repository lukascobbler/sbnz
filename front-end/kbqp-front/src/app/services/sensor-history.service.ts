import { Injectable, signal } from '@angular/core';
import { SensorStatus } from '../models/simulation.types';
import { SensorPoint } from '../models/sensor-point.model';

export type { SensorPoint } from '../models/sensor-point.model';

@Injectable({ providedIn: 'root' })
export class SensorHistoryService {
  private static readonly MAX_POINTS = 80;
  private readonly data = signal<Map<string, SensorPoint[]>>(new Map());

  readonly series = this.data.asReadonly();

  clear(): void {
    this.data.set(new Map());
  }

  appendFromSnapshot(sensors: SensorStatus[]): void {
    if (!sensors.length) return;
    const next = new Map(this.data());
    for (const s of sensors) {
      const key = `${s.machineId}|${s.metric}`;
      const t = Date.parse(s.ts);
      if (!Number.isFinite(t)) continue;
      const prev = next.get(key) ?? [];
      const last = prev[prev.length - 1];
      let row: SensorPoint[];
      if (last && last.t === t) {
        row = [...prev.slice(0, -1), { t, v: s.value }];
      } else {
        row = [...prev, { t, v: s.value }];
      }
      while (row.length > SensorHistoryService.MAX_POINTS) {
        row.shift();
      }
      next.set(key, row);
    }
    this.data.set(next);
  }

  points(machineId: string, metric: string): SensorPoint[] {
    return this.data().get(`${machineId}|${metric}`) ?? [];
  }
}
