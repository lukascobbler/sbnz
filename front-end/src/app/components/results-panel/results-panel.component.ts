import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgClass } from '@angular/common';
import { Anomaly, Intervention, SafetyResult } from '../../models/simulation.types';
import { machineAccentClass } from '../../utils/machine-accent';
import { InfoDialogButtonComponent } from '../info-dialog-button/info-dialog-button.component';
import { MachineHealthDialogComponent } from '../machine-health-dialog/machine-health-dialog.component';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';

@Component({
  selector: 'results-panel',
  standalone: true,
  imports: [NgClass, InfoDialogButtonComponent, MachineHealthDialogComponent, PrettyDatePipe],
  templateUrl: './results-panel.component.html',
  styleUrl: './results-panel.component.scss',
})
export class ResultsPanelComponent {
  protected readonly machineAccentClass = machineAccentClass;

  @Input({ required: true }) anomalies: Anomaly[] = [];
  @Input({ required: true }) interventions: Intervention[] = [];
  @Input({ required: true }) safetyResults: SafetyResult[] = [];
  @Input() haltedMachineIds: string[] = [];
  @Input() controlsBusy = false;
  @Output() safetyFix = new EventEmitter<string>();

  isHalted(machineId: string): boolean {
    return this.haltedMachineIds.includes(machineId);
  }
}
