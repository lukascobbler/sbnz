import { NgClass } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  Output,
  signal,
  SimpleChanges,
} from '@angular/core';
import {
  MachineProfileView,
  MachineWorkload,
  MetricProfileView,
  SensorStatus,
} from '../../models/simulation.types';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';
import { InfoDialogButtonComponent } from '../info-dialog-button/info-dialog-button.component';
import { SensorHistoryService } from '../../services/sensor-history.service';
import { SensorSparklineComponent } from '../sensor-sparkline/sensor-sparkline.component';
import { machineAccentClass } from '../../utils/machine-accent';

export interface WorkloadCyclePayload {
  machineId: string;
  metricKey: string;
}

interface SensorRowView {
  status: SensorStatus;
  metricProfile: MetricProfileView | null;
}

interface SensorGroup {
  machineId: string;
  slug: string;
  displayName: string;
  rows: SensorRowView[];
  workloadMetrics: MetricProfileView[];
}

@Component({
  selector: 'sensors-panel',
  standalone: true,
  imports: [NgClass, PrettyDatePipe, InfoDialogButtonComponent, SensorSparklineComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sensors-panel.component.html',
  styleUrl: './sensors-panel.component.scss',
})
export class SensorsPanelComponent implements OnChanges {
  protected readonly machineAccentClass = machineAccentClass;

  private readonly sensorHistory = inject(SensorHistoryService);
  protected readonly history = this.sensorHistory.series;
  protected readonly emptyPoints: { t: number; v: number }[] = [];

  @Input({ required: true }) sensors: SensorStatus[] = [];
  @Input() haltedMachineIds: string[] = [];
  @Input() machineProfiles: MachineProfileView[] = [];
  @Input() machineMetricWorkloads: Record<string, Record<string, MachineWorkload>> = {};
  @Input() controlsBusy = false;
  @Output() workloadCycle = new EventEmitter<WorkloadCyclePayload>();

  private readonly sensorsSig = signal<SensorStatus[]>([]);
  private readonly haltedSig = signal<string[]>([]);
  private readonly profilesSig = signal<MachineProfileView[]>([]);
  private readonly workloadsSig = signal<Record<string, Record<string, MachineWorkload>>>({});

  protected readonly groups = computed((): SensorGroup[] => {
    void this.sensorHistory.series();
    const profileMap = new Map<string, MachineProfileView>();
    for (const p of this.profilesSig()) {
      profileMap.set(p.machineId, p);
    }
    const sensors = this.sensorsSig();
    const byMachine = new Map<string, SensorStatus[]>();
    for (const s of sensors) {
      const list = byMachine.get(s.machineId) ?? [];
      list.push(s);
      byMachine.set(s.machineId, list);
    }
    const out: SensorGroup[] = [];
    for (const [machineId, rows] of byMachine) {
      const profile = profileMap.get(machineId);
      const metricMap = new Map<string, MetricProfileView>();
      for (const m of profile?.metrics ?? []) {
        metricMap.set(m.metricKey, m);
      }
      const rowViews: SensorRowView[] = rows
        .map((status) => ({ status, metricProfile: metricMap.get(status.metric) ?? null }))
        .sort((a, b) => this.metricDisplayName(a).localeCompare(this.metricDisplayName(b)));
      const workloadMetrics = (profile?.metrics ?? []).filter((m) => m.workloadEnabled);
      const slug = machineId.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase();
      out.push({
        machineId,
        slug,
        displayName: profile?.displayName ?? machineId,
        rows: rowViews,
        workloadMetrics,
      });
    }
    out.sort((a, b) => a.machineId.localeCompare(b.machineId));
    return out;
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sensors']) this.sensorsSig.set(this.sensors);
    if (changes['haltedMachineIds']) this.haltedSig.set(this.haltedMachineIds ?? []);
    if (changes['machineProfiles']) this.profilesSig.set(this.machineProfiles ?? []);
    if (changes['machineMetricWorkloads']) this.workloadsSig.set(this.machineMetricWorkloads ?? {});
  }

  isHalted(machineId: string): boolean {
    return this.haltedSig().includes(machineId);
  }

  workloadOf(machineId: string, metricKey: string): MachineWorkload {
    return this.workloadsSig()[machineId]?.[metricKey] ?? 'NORMAL';
  }

  workloadLabel(w: MachineWorkload): string {
    switch (w) {
      case 'OVERWORKED':
        return '↑ Overworked';
      case 'REST':
        return '↓ Rest';
      default:
        return '→ Normal';
    }
  }

  workloadTitle(metric: MetricProfileView): string {
    return `${metric.displayName}: cycle Normal → Overworked → Rest`;
  }

  machineGroupClass(machineId: string, slug: string): string {
    return `sensor-group machine-group machine-${slug} ${machineAccentClass(machineId)}`;
  }

  metricDisplayName(row: SensorRowView): string {
    return row.metricProfile?.displayName ?? row.status.metric;
  }

  metricDecimals(row: SensorRowView): number {
    return row.metricProfile?.decimals ?? 2;
  }

  metricUnit(row: SensorRowView): string {
    return row.metricProfile?.unit ?? '';
  }
}
