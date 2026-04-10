import { Component, Input } from '@angular/core';
import { RULE_CATALOG_ENTRIES } from '../../models/rule-catalog.data';

@Component({
  selector: 'rule-catalog-inline',
  standalone: true,
  templateUrl: './rule-catalog-inline.component.html',
  styleUrl: './rule-catalog-inline.component.scss',
})
export class RuleCatalogInlineComponent {
  @Input() ruleName: string | null = null;
  protected readonly rules = RULE_CATALOG_ENTRIES;

  open(dlg: HTMLDialogElement) {
    dlg.showModal();
    setTimeout(() => {
      const el = dlg.querySelector('.rules-list-item.rule-focused') as HTMLElement | null;
      el?.scrollIntoView({ block: 'center' });
    }, 0);
  }

  onBackdrop(ev: MouseEvent, dlg: HTMLDialogElement) {
    if (ev.target === dlg) {
      dlg.close();
    }
  }
}
