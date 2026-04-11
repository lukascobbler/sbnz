import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { MachineHealthReport } from '../../models/simulation.types';
import { SimulationService } from '../../services/simulation.service';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';

/** 100, 98, …, 2 — top row is 100%; one tick every 2%. */
export const HEALTH_PERCENT_LEVELS_DESC: readonly number[] = Array.from({ length: 50 }, (_, i) => 100 - 2 * i);

@Component({
  selector: 'machine-health-dialog',
  standalone: true,
  imports: [PrettyDatePipe],
  templateUrl: './machine-health-dialog.component.html',
  styleUrl: './machine-health-dialog.component.scss',
})
export class MachineHealthDialogComponent {
  private readonly sim = inject(SimulationService);

  @ViewChild('dlg') private dialogRef?: ElementRef<HTMLDialogElement>;

  protected readonly selectedMachineId = signal('');
  protected readonly report = signal<MachineHealthReport | null>(null);
  protected readonly loadError = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected readonly percentLevelsDesc = HEALTH_PERCENT_LEVELS_DESC;

  openFor(machineId: string): void {
    const id = machineId.trim();
    if (!id) {
      return;
    }
    this.selectedMachineId.set(id);
    this.dialogRef?.nativeElement.showModal();
    this.fetchHealth();
  }

  protected close(): void {
    this.dialogRef?.nativeElement.close();
  }

  protected onBackdrop(ev: MouseEvent, dlg: HTMLDialogElement): void {
    if (ev.target === dlg) {
      dlg.close();
    }
  }

  protected rev<T>(items: T[] | undefined): T[] {
    return items?.length ? [...items].reverse() : [];
  }

  /** Health mapped to 2% steps (floor); ladder on/off uses this. */
  protected flooredHealth(health: number): number {
    return Math.floor(health / 2) * 2;
  }

  /**
   * Bar length: floor at 2%, 100% at 100%, centered; u^γ shapes how fast width grows up the ladder.
   */
  protected barWidthPercent(level: number): number {
    const u = Math.max(0, Math.min(1, (level - 2) / 98));
    const minPct = 11;
    const gamma = 1.28;
    return minPct + (100 - minPct) * Math.pow(u, gamma);
  }

  /**
   * Blend red (low %) → green (high %), muted. Inactive levels stay darker but hue-matched.
   */
  protected tickColor(level: number, health: number): string {
    const active = level <= this.flooredHealth(health);
    const t = (level - 2) / 98;
    const dim = 0.74;
    const r = (210 * (1 - t) + 42 * t) * dim;
    const g = (48 * (1 - t) + 200 * t) * dim;
    const b = (56 * (1 - t) + 104 * t) * dim;
    if (!active) {
      const base = 14;
      const mix = 0.36;
      return `rgb(${Math.round(r * mix + base * (1 - mix))}, ${Math.round(g * mix + base * (1 - mix))}, ${Math.round(b * mix + base * (1 - mix))})`;
    }
    return `rgb(${Math.round(r)}, ${Math.round(g)}, ${Math.round(b)})`;
  }

  private fetchHealth(): void {
    const id = this.selectedMachineId();
    if (!id) {
      return;
    }
    this.loading.set(true);
    this.loadError.set(null);
    this.sim.getMachineHealth(id).subscribe({
      next: (r) => {
        this.report.set(r);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Could not load machine health.');
        this.report.set(null);
        this.loading.set(false);
      },
    });
  }
}
