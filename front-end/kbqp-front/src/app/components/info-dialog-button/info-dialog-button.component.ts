import { Component, Input } from '@angular/core';

@Component({
  selector: 'info-dialog-btn',
  standalone: true,
  templateUrl: './info-dialog-button.component.html',
  styleUrl: './info-dialog-button.component.scss',
})
export class InfoDialogButtonComponent {
  @Input({ required: true }) title = '';
  @Input({ required: true }) body = '';

  open(dlg: HTMLDialogElement) {
    dlg.showModal();
  }

  onBackdrop(ev: MouseEvent, dlg: HTMLDialogElement) {
    if (ev.target === dlg) {
      dlg.close();
    }
  }
}
