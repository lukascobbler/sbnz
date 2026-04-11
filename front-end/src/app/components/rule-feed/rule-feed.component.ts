import { NgClass } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ruleFeedLabel } from '../../models/rule-catalog.data';
import { RuleFiring } from '../../models/simulation.types';
import { machineAccentClass } from '../../utils/machine-accent';
import { PrettyDatePipe } from '../../pipes/pretty-date.pipe';
import { InfoDialogButtonComponent } from '../info-dialog-button/info-dialog-button.component';
import { RuleCatalogInlineComponent } from '../rule-catalog-inline/rule-catalog-inline.component';

interface MachineRuleSubgroup {
  machineId: string;
  rows: RuleFiring[];
}

interface TickRuleGroup {
  firedAtKey: string;
  machineGroups: MachineRuleSubgroup[];
}

@Component({
  selector: 'rule-feed',
  standalone: true,
  imports: [NgClass, PrettyDatePipe, InfoDialogButtonComponent, RuleCatalogInlineComponent],
  templateUrl: './rule-feed.component.html',
  styleUrl: './rule-feed.component.scss',
})
export class RuleFeedComponent {
  protected readonly machineAccentClass = machineAccentClass;
  protected readonly ruleLabel = ruleFeedLabel;

  @Input({ required: true }) rules: RuleFiring[] = [];
  @Input({ required: true }) tickEndTime: string | null = null;

  displayGroups(): TickRuleGroup[] {
    const sorted = [...this.rules].sort((a, b) => {
      const ta = a.firedAt ?? '';
      const tb = b.firedAt ?? '';
      if (tb !== ta) return tb.localeCompare(ta);
      const ma = a.machineId ?? '';
      const mb = b.machineId ?? '';
      if (ma !== mb) return ma.localeCompare(mb);
      return (b.ruleName ?? '').localeCompare(a.ruleName ?? '');
    });

    const order: string[] = [];
    const map = new Map<string, RuleFiring[]>();
    for (const r of sorted) {
      const k = r.firedAt ?? '—';
      if (!map.has(k)) {
        order.push(k);
        map.set(k, []);
      }
      map.get(k)!.push(r);
    }

    return order.map((firedAtKey) => {
      const rows = map.get(firedAtKey)!;
      const machineOrder: string[] = [];
      const byMachine = new Map<string, RuleFiring[]>();
      for (const r of rows) {
        const mid = r.machineId ?? '—';
        if (!byMachine.has(mid)) {
          machineOrder.push(mid);
          byMachine.set(mid, []);
        }
        byMachine.get(mid)!.push(r);
      }
      const machineGroups = machineOrder.map((machineId) => ({
        machineId,
        rows: byMachine.get(machineId)!,
      }));
      return { firedAtKey, machineGroups };
    });
  }
}
