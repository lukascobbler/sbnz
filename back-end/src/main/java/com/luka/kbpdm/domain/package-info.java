/**
 * Domain model grouped by concern:
 *
 * <ul>
 *   <li>{@code domain.machine} — equipment identity ({@code Machine}, types) and {@code ComponentStatus}</li>
 *   <li>{@code domain.telemetry} — readings, ticks, clock, and current metric snapshots</li>
 *   <li>{@code domain.diagnosis} — live {@code Anomaly} / {@code Intervention} facts</li>
 *   <li>{@code domain.safety} — unsafe reasons and safety check / result</li>
 *   <li>{@code domain.health} — append-only {@code Recorded*} audit facts</li>
 * </ul>
 */
package com.luka.kbpdm.domain;
