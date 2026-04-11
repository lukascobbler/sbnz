import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RuleCatalogService {
  private readonly openSubject = new Subject<string | null>();

  readonly openRequests$ = this.openSubject.asObservable();

  open(focusEngineName?: string | null): void {
    this.openSubject.next(focusEngineName ?? null);
  }
}
