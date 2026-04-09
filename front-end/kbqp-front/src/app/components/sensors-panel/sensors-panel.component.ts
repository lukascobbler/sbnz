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
import { MachineWorkload, SensorStatus, TelemetryMetricKey } from '../../models/simulation.types';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';
import { InfoDialogButtonComponent } from '../info-dialog-button/info-dialog-button.component';
import { SensorHistoryService } from '../../services/sensor-history.service';
import { SensorSparklineComponent } from '../sensor-sparkline/sensor-sparkline.component';

export interface WorkloadCyclePayload {
  machineId: string;
  metric: TelemetryMetricKey;
}

interface SensorGroup {
  machineId: string;
  slug: string;
  rows: SensorStatus[];
}

@Component({
  selector: 'sensors-panel',
  standalone: true,
  imports: [PrettyDatePipe, InfoDialogButtonComponent, SensorSparklineComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sensors-panel.component.html',
  styleUrl: './sensors-panel.component.scss',
})
export class SensorsPanelComponent implements OnChanges {
  private readonly sensorHistory = inject(SensorHistoryService);
  protected readonly history = this.sensorHistory.series;
  protected readonly emptyPoints: { t: number; v: number }[] = [];

  @Input({ required: true }) sensors: SensorStatus[] = [];
  @Input() haltedMachineIds: string[] = [];
  @Input() temperatureWorkloads: Record<string, MachineWorkload> = {};
  @Input() vibrationWorkloads: Record<string, MachineWorkload> = {};
  @Input() controlsBusy = false;
  @Output() workloadCycle = new EventEmitter<WorkloadCyclePayload>();

  private readonly sensorsSig = signal<SensorStatus[]>([]);
  private readonly haltedSig = signal<string[]>([]);
  private readonly temperatureWorkloadsSig = signal<Record<string, MachineWorkload>>({});
  private readonly vibrationWorkloadsSig = signal<Record<string, MachineWorkload>>({});

  protected readonly groups = computed((): SensorGroup[] => {
    void this.sensorHistory.series();
    const sensors = this.sensorsSig();
    const map = new Map<string, SensorStatus[]>();
    for (const s of sensors) {
      const list = map.get(s.machineId) ?? [];
      list.push(s);
      map.set(s.machineId, list);
    }
    const out: SensorGroup[] = [];
    for (const [machineId, rows] of map) {
      rows.sort((a, b) => a.metric.localeCompare(b.metric));
      const slug = machineId.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase();
      out.push({ machineId, slug, rows });
    }
    out.sort((a, b) => a.machineId.localeCompare(b.machineId));
    return out;
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['sensors']) this.sensorsSig.set(this.sensors);
    if (changes['haltedMachineIds']) this.haltedSig.set(this.haltedMachineIds ?? []);
    if (changes['temperatureWorkloads']) this.temperatureWorkloadsSig.set(this.temperatureWorkloads ?? {});
    if (changes['vibrationWorkloads']) this.vibrationWorkloadsSig.set(this.vibrationWorkloads ?? {});
  }

  isHalted(machineId: string): boolean {
    return this.haltedSig().includes(machineId);
  }

  temperatureWorkloadOf(machineId: string): MachineWorkload {
    return this.temperatureWorkloadsSig()[machineId] ?? 'NORMAL';
  }

  vibrationWorkloadOf(machineId: string): MachineWorkload {
    return this.vibrationWorkloadsSig()[machineId] ?? 'NORMAL';
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

  temperatureTitle(): string {
    return 'Temperature driver: cycle Normal → Overworked → Rest (affects °C only)';
  }

  vibrationTitle(): string {
    return 'Vibration driver: cycle Normal → Overworked → Rest (affects RMS only)';
  }

  machineGroupClass(slug: string): string {
    return 'sensor-group machine-group machine-' + slug;
  }
}
