import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  ViewChild,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RuleCatalogService } from '../../services/rule-catalog.service';
import {
  RULE_CATALOG_SECTION_LABEL,
  RULE_CATALOG_SECTION_ORDER,
  RuleCatalogSectionId,
} from '../../models/rule-catalog.model';
import { primaryEngineForClearRule, rulesInSection } from '../../models/rule-catalog.data';

@Component({
  selector: 'rule-catalog-dialog',
  standalone: true,
  templateUrl: './rule-catalog-dialog.component.html',
  styleUrl: './rule-catalog-dialog.component.scss',
})
export class RuleCatalogDialogComponent implements AfterViewInit {
  private readonly catalog = inject(RuleCatalogService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  @ViewChild('dlg') protected dialogRef?: ElementRef<HTMLDialogElement>;

  protected readonly sectionOrder = RULE_CATALOG_SECTION_ORDER;
  protected readonly sectionLabel = RULE_CATALOG_SECTION_LABEL;

  /** Card to outline when opening from the rule feed (resolves clear rules to their primary card). */
  protected highlightEngineName: string | null = null;
  /** When the user opened from a clearing rule, outline the expanded clear block. */
  protected focusedClearRuleName: string | null = null;

  protected expandedClears = new Set<string>();

  ngAfterViewInit(): void {
    this.catalog.openRequests$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((name) => {
      if (name) {
        const primary = primaryEngineForClearRule(name);
        if (primary) {
          this.highlightEngineName = primary;
          this.focusedClearRuleName = name;
          this.expandedClears = new Set([...this.expandedClears, primary]);
        } else {
          this.highlightEngineName = name;
          this.focusedClearRuleName = null;
        }
      } else {
        this.highlightEngineName = null;
        this.focusedClearRuleName = null;
      }
      this.cdr.markForCheck();
      const dlg = this.dialogRef?.nativeElement;
      if (dlg && !dlg.open) {
        dlg.showModal();
      }
      const scrollTarget = name ? primaryEngineForClearRule(name) ?? name : null;
      if (scrollTarget) {
        const id = this.ruleDomId(scrollTarget);
        setTimeout(() => document.getElementById(id)?.scrollIntoView({ block: 'center', behavior: 'smooth' }), 0);
      }
    });
  }

  entriesFor(section: RuleCatalogSectionId) {
    return rulesInSection(section);
  }

  ruleDomId(engineName: string): string {
    return (
      'rule-catalog-' +
      engineName
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-|-$/g, '')
    );
  }

  protected toggleClear(primaryEngineName: string): void {
    const next = new Set(this.expandedClears);
    if (next.has(primaryEngineName)) {
      next.delete(primaryEngineName);
    } else {
      next.add(primaryEngineName);
    }
    this.expandedClears = next;
    this.cdr.markForCheck();
  }

  protected isClearExpanded(primaryEngineName: string): boolean {
    return this.expandedClears.has(primaryEngineName);
  }

  protected onBackdrop(ev: MouseEvent, dlg: HTMLDialogElement): void {
    if (ev.target === dlg) {
      dlg.close();
    }
  }
}
