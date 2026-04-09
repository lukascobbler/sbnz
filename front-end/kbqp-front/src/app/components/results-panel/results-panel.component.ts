import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Anomaly, Intervention, SafetyResult } from '../../models/simulation.types';
import { InfoDialogButtonComponent } from '../info-dialog-button/info-dialog-button.component';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';

@Component({
  selector: 'results-panel',
  standalone: true,
  imports: [InfoDialogButtonComponent, PrettyDatePipe],
  templateUrl: './results-panel.component.html',
  styleUrl: './results-panel.component.scss',
})
export class ResultsPanelComponent {
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
