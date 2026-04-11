import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RuleCatalogService {
  private readonly openSubject = new Subject<string | null>();

  /** Emits the engine rule name to scroll to and highlight, or `null` to open without focus. */
  readonly openRequests$ = this.openSubject.asObservable();

  open(focusEngineName?: string | null): void {
    this.openSubject.next(focusEngineName ?? null);
  }
}
