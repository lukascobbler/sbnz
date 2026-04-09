import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';

@Component({
  selector: 'sim-controls',
  standalone: true,
  imports: [PrettyDatePipe],
  templateUrl: './sim-controls.component.html',
  styleUrl: './sim-controls.component.scss',
})
export class SimControlsComponent {
  @Input({ required: true }) simulatedTime: string | null = null;
  @Input({ required: true }) tickMinutes = 30;
  @Input({ required: true }) connected = false;
  @Input({ required: true }) controlsBusy = false;

  @Output() reset = new EventEmitter<void>();
  @Output() step = new EventEmitter<void>();
  @Output() tickStep = new EventEmitter<number>();
}
