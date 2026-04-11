import { Component, inject } from '@angular/core';
import { RuleCatalogService } from '../../services/rule-catalog.service';

@Component({
  selector: 'rule-catalog-header-btn',
  standalone: true,
  templateUrl: './rule-catalog-header.component.html',
  styleUrl: './rule-catalog-header.component.scss',
})
export class RuleCatalogHeaderComponent {
  private readonly catalog = inject(RuleCatalogService);

  open(): void {
    this.catalog.open(null);
  }
}
