import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { SensorPoint } from '../../models/sensor-point.model';

@Component({
  selector: 'sensor-sparkline',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sensor-sparkline.component.html',
  styleUrl: './sensor-sparkline.component.scss',
})
export class SensorSparklineComponent {
  readonly points = input.required<SensorPoint[]>();
  readonly metricLabel = input<string>('');

  private static uid = 0;
  readonly gradId = `spark-fill-${++SensorSparklineComponent.uid}`;

  protected readonly fillUrl = computed(() => `url(#${this.gradId})`);

  protected readonly label = computed(() => {
    const n = this.points().length;
    const m = this.metricLabel();
    return m ? `${m} trend, ${n} samples` : `Sensor trend, ${n} samples`;
  });

  protected readonly linePoints = computed(() => {
    const pts = this.points();
    if (!pts.length) return null;
    const padX = 3;
    const padY = 4;
    const w = 100 - padX * 2;
    const h = 36 - padY * 2;
    if (pts.length === 1) {
      const y = padY + h / 2;
      return `${padX},${y.toFixed(2)} ${(padX + w).toFixed(2)},${y.toFixed(2)}`;
    }
    const vs = pts.map((p) => p.v);
    let min = Math.min(...vs);
    let max = Math.max(...vs);
    if (min === max) {
      min -= 1;
      max += 1;
    }
    const t0 = pts[0].t;
    const t1 = pts[pts.length - 1].t;
    const tr = Math.max(1, t1 - t0);
    return pts
      .map((p) => {
        const x = padX + ((p.t - t0) / tr) * w;
        const y = padY + (1 - (p.v - min) / (max - min)) * h;
        return `${x.toFixed(2)},${y.toFixed(2)}`;
      })
      .join(' ');
  });

  protected readonly areaPoints = computed(() => {
    const line = this.linePoints();
    if (!line) return null;
    const coords = line.split(' ').map((pair) => pair.split(',').map(Number));
    if (!coords.length) return null;
    const yBottom = 36 - 2;
    const firstX = coords[0][0];
    const lastX = coords[coords.length - 1][0];
    const top = coords.map(([x, y]) => `${x},${y}`).join(' ');
    return `${firstX},${yBottom} ${top} ${lastX},${yBottom}`;
  });
}
