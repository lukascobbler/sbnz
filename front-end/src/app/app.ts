import { Component, computed, inject, signal } from '@angular/core';
import { Observable, finalize } from 'rxjs';
import { SimControlsComponent } from './components/sim-controls/sim-controls.component';
import { SensorsPanelComponent } from './components/sensors-panel/sensors-panel.component';
import { RuleFeedComponent } from './components/rule-feed/rule-feed.component';
import { ResultsPanelComponent } from './components/results-panel/results-panel.component';
import { RuleCatalogHeaderComponent } from './components/rule-catalog-header/rule-catalog-header.component';
import { MachineWorkload } from './models/simulation.types';
import { WorkloadCyclePayload } from './components/sensors-panel/sensors-panel.component';
import { SimulationService } from './services/simulation.service';
import { SensorHistoryService } from './services/sensor-history.service';

@Component({
  selector: 'app-root',
  imports: [
    SimControlsComponent,
    SensorsPanelComponent,
    RuleFeedComponent,
    ResultsPanelComponent,
    RuleCatalogHeaderComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly sim = inject(SimulationService);
  private readonly sensorHistory = inject(SensorHistoryService);

  protected readonly title = signal('Knowledge-based PdM');
  protected readonly error = this.sim.error;
  protected readonly report = this.sim.report;
  protected readonly connected = this.sim.connected;

  protected readonly anomalies = computed(() => this.report()?.anomalies ?? []);
  protected readonly interventions = computed(() => this.report()?.interventions ?? []);
  protected readonly safetyResults = computed(() => this.report()?.safetyResults ?? []);
  protected readonly sensors = computed(() => this.report()?.sensors ?? []);
  protected readonly tickMinutes = computed(() => this.report()?.tickMinutes ?? 30);
  protected readonly simulatedTime = computed(() => this.report()?.simulatedTime ?? null);
  protected readonly rulesFiredThisTick = computed(() => this.report()?.rulesFiredThisTick ?? []);
  protected readonly safetyHaltedMachineIds = computed(() => this.report()?.safetyHaltedMachineIds ?? []);
  protected readonly machineProfiles = computed(() => this.report()?.machineProfiles ?? []);
  protected readonly machineMetricWorkloads = computed(() => this.report()?.machineMetricWorkloads ?? {});

  private readonly pendingHttp = signal(0);
  protected readonly controlsBusy = computed(() => this.pendingHttp() > 0);

  private static readonly WORKLOAD_ORDER: MachineWorkload[] = ['NORMAL', 'OVERWORKED', 'REST'];

  constructor() {
    this.sim.connect();
  }

  private trackHttp<T>(source: Observable<T>): Observable<T> {
    this.pendingHttp.update((n) => n + 1);
    return source.pipe(finalize(() => this.pendingHttp.update((n) => Math.max(0, n - 1))));
  }

  protected reset() {
    this.sensorHistory.clear();
    this.sim.resetSensorHistoryDedupe();
    this.trackHttp(this.sim.reset()).subscribe({
      next: (r) => this.sim.ingestReport(r),
      error: () => this.error.set('Reset failed'),
    });
  }

  protected step() {
    if (this.controlsBusy()) return;
    this.error.set(null);
    this.trackHttp(this.sim.step(this.tickMinutes())).subscribe({
      next: (r) => this.sim.ingestReport(r),
      error: () => this.error.set('Step failed'),
    });
  }

  protected safetyFix(machineId: string) {
    if (this.controlsBusy()) return;
    this.error.set(null);
    this.trackHttp(this.sim.safetyFix(machineId)).subscribe({
      error: () => this.error.set('Safety fix failed'),
    });
  }

  protected adjustTick(deltaSteps: number) {
    if (this.controlsBusy()) return;
    const cur = this.tickMinutes();
    const next = Math.max(30, Math.min(900, cur + deltaSteps * 30));
    if (next === cur) return;
    this.error.set(null);
    this.trackHttp(this.sim.setTickMinutes(next)).subscribe({
      next: (r) => this.sim.ingestReport(r),
      error: () => this.error.set('Tick update failed'),
    });
  }

  protected cycleWorkload(payload: WorkloadCyclePayload) {
    if (this.controlsBusy()) return;
    const { machineId, metricKey } = payload;
    const machineMap = this.machineMetricWorkloads()[machineId] ?? {};
    const w = machineMap[metricKey] ?? 'NORMAL';
    let idx = App.WORKLOAD_ORDER.indexOf(w);
    if (idx < 0) idx = 0;
    const next = App.WORKLOAD_ORDER[(idx + 1) % App.WORKLOAD_ORDER.length];
    this.error.set(null);
    this.trackHttp(this.sim.setWorkload(machineId, next, metricKey)).subscribe({
      next: (r) => this.sim.ingestReport(r),
      error: () => this.error.set('Workload update failed'),
    });
  }
}
