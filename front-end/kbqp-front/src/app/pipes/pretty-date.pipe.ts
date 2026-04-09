import { Pipe, PipeTransform } from '@angular/core';

const MO = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function toDate(value: unknown): Date | null {
  if (value == null) return null;
  if (typeof value === 'string') {
    const d = new Date(value);
    return Number.isNaN(d.getTime()) ? null : d;
  }
  if (typeof value === 'number' && Number.isFinite(value)) {
    const d = new Date(value);
    return Number.isNaN(d.getTime()) ? null : d;
  }
  if (Array.isArray(value) && value.length >= 2) {
    const sec = Number(value[0]);
    const nano = Number(value[1]);
    if (!Number.isFinite(sec) || !Number.isFinite(nano)) return null;
    return new Date(sec * 1000 + Math.floor(nano / 1_000_000));
  }
  if (typeof value === 'object' && value !== null && 'epochSecond' in value) {
    const o = value as { epochSecond?: unknown; nano?: unknown };
    const sec = Number(o.epochSecond);
    const nano = Number(o.nano ?? 0);
    if (!Number.isFinite(sec)) return null;
    return new Date(sec * 1000 + Math.floor(nano / 1_000_000));
  }
  return null;
}

@Pipe({
  name: 'prettyDate',
  standalone: true,
})
export class PrettyDatePipe implements PipeTransform {
  transform(value: string | number | number[] | Record<string, unknown> | null | undefined): string {
    const d = toDate(value);
    if (!d) {
      if (value == null || value === '') return '—';
      return String(value);
    }

    const p = (n: number) => String(n).padStart(2, '0');
    return `${p(d.getDate())} ${MO[d.getMonth()]} ${String(d.getFullYear()).slice(-2)} ${p(d.getHours())}:${p(
      d.getMinutes()
    )}:${p(d.getSeconds())}`;
  }
}
