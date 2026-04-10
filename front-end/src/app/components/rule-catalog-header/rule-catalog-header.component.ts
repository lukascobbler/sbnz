import { Component } from '@angular/core';
import { RULE_CATALOG_ENTRIES } from '../../models/rule-catalog.data';

@Component({
  selector: 'rule-catalog-header-btn',
  standalone: true,
  templateUrl: './rule-catalog-header.component.html',
  styleUrl: './rule-catalog-header.component.scss',
})
export class RuleCatalogHeaderComponent {
  protected readonly rules = RULE_CATALOG_ENTRIES;

  open(dlg: HTMLDialogElement) {
    dlg.showModal();
  }

  onBackdrop(ev: MouseEvent, dlg: HTMLDialogElement) {
    if (ev.target === dlg) {
      dlg.close();
    }
  }
}
