import { Component, Input, inject } from '@angular/core';
import { RuleCatalogService } from '../../services/rule-catalog.service';

@Component({
  selector: 'rule-catalog-inline',
  standalone: true,
  templateUrl: './rule-catalog-inline.component.html',
  styleUrl: './rule-catalog-inline.component.scss',
})
export class RuleCatalogInlineComponent {
  private readonly catalog = inject(RuleCatalogService);

  @Input() ruleName: string | null = null;

  open(): void {
    this.catalog.open(this.ruleName);
  }
}
