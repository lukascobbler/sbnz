import { RuleCatalogEntry, RuleCatalogSectionId } from './rule-catalog.model';

const L1 = 'L1' as RuleCatalogSectionId;
const L2 = 'L2' as RuleCatalogSectionId;
const L3 = 'L3' as RuleCatalogSectionId;
const CEP = 'CEP' as RuleCatalogSectionId;
const QUERY = 'QUERY' as RuleCatalogSectionId;

function trendPair(
  riseTitle: string,
  clearTitle: string,
  riseWhen: string,
  clearWhen: string
): RuleCatalogEntry[] {
  return [
    {
      engineName: riseTitle,
      section: CEP,
      title: riseTitle,
      description: riseWhen,
      fromTemplate: true,
      produces: ['Anomaly'],
      clearCounterpart: {
        engineName: clearTitle,
        title: clearTitle,
        description: clearWhen,
        removes: ['Anomaly'],
      },
    },
    {
      engineName: clearTitle,
      section: CEP,
      title: clearTitle,
      description: clearWhen,
      fromTemplate: true,
      catalogHidden: true,
    },
  ];
}

function highBandPair(
  machine: string,
  metric: string,
  riseWhen: string,
  clearWhen: string
): RuleCatalogEntry[] {
  const riseTitle = `${machine} — ${metric} above high band`;
  const clearTitle = `${machine} — ${metric} no longer above high band`;
  return [
    {
      engineName: riseTitle,
      section: L1,
      title: riseTitle,
      description: riseWhen,
      fromTemplate: true,
      produces: ['Anomaly'],
      clearCounterpart: {
        engineName: clearTitle,
        title: clearTitle,
        description: clearWhen,
        removes: ['Anomaly'],
      },
    },
    {
      engineName: clearTitle,
      section: L1,
      title: clearTitle,
      description: clearWhen,
      fromTemplate: true,
      catalogHidden: true,
    },
  ];
}

export const RULE_CATALOG_ENTRIES: RuleCatalogEntry[] = [
  ...highBandPair(
    'Plant climate',
    'Ambient temperature',
    'Runs when the latest ambient temperature reading for plant climate reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any ambient temperature readings for plant climate reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'Plant climate',
    'Humidity',
    'Runs when the latest humidity reading for plant climate reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any humidity readings for plant climate reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'Conveyor line',
    'Vibration',
    'Runs when the latest vibration reading for the conveyor reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any vibration readings for the conveyor reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'CNC mill',
    'Temperature',
    'Runs when the latest temperature reading for the CNC mill reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any temperature readings for the CNC mill reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'CNC mill',
    'Vibration',
    'Runs when the latest vibration reading for the CNC mill reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any vibration readings for the CNC mill reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'CNC mill',
    'Spindle load',
    'Runs when the latest spindle load reading for the CNC mill reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any spindle load readings for the CNC mill reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'Auto packer',
    'Reject rate',
    'Runs when the latest reject rate for the auto packer reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any reject rate readings for the auto packer reaching or passing the high limit, clearing the open anomaly.'
  ),
  ...highBandPair(
    'Auto packer',
    'Seal temperature',
    'Runs when the latest seal temperature for the auto packer reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    'Runs when there are no longer any seal temperature readings for the auto packer reaching or passing the high limit, clearing the open anomaly.'
  ),

  {
    engineName: 'Safety evaluation: not safe',
    section: L2,
    title: 'Safety evaluation: not safe',
    description:
      'Runs when a safety check is requested for a machine that already has a MachineOverworked fact and no safety outcome exists yet.',
    fromTemplate: false,
    produces: ['Safety result'],
  },
  {
    engineName: 'Safety evaluation: safe',
    section: L2,
    title: 'Safety evaluation: safe',
    description:
      'Runs when a safety check is requested, no MachineOverworked fact is present, and no safety outcome exists yet.',
    fromTemplate: false,
    produces: ['Safety result'],
  },

  {
    engineName: 'Pushed conveyor line (belt speed, vibration, reject rate)',
    section: L3,
    title: 'Pushed conveyor line (belt speed, vibration, reject rate)',
    description:
      'Runs when belt speed, conveyor vibration, and reject rate are all in their raised bands at the same time, and the matching medium-priority recommendation is not already active.',
    fromTemplate: false,
    produces: ['Intervention'],
    clearCounterpart: {
      engineName: 'Clear pushed-line intervention',
      title: 'Clear pushed-line intervention',
      description:
        'Runs once simulated time has moved past the moment the pushed-line intervention was created, and belt speed, vibration, and rejection rate are no longer all in their raised band together.',
      removes: ['Intervention'],
    },
  },

  {
    engineName: 'CNC high spindle load and line speed causing high seal temperature (spindle load, line belt speed, seal temperature)',
    section: L3,
    title: 'CNC high spindle load and line speed causing high seal temperature (spindle load, line belt speed, seal temperature)',
    description:
      'Runs when spindle load and line speed are raised while the seal temperature is high, being of the high intervention priority.',
    fromTemplate: false,
    produces: ['Intervention'],
    clearCounterpart: {
      engineName: 'Clear CNC spindle load and belt speed causing high seal temperature intervention',
      title: 'Clear CNC spindle load and belt speed causing high seal temperature intervention',
      description:
        'Runs once simulated time has moved past the moment the cnc-line high seal temperature intervention was created, and the combination of raised spindle load, raised belt speed, and raised seal temperature no longer all holds.',
      removes: ['Intervention'],
    },
  },
  {
    engineName: 'Clear conveyor-instability intervention',
    section: L2,
    title: 'Clear conveyor-instability intervention',
    description:
      'Runs once simulated time has moved past the moment the conveyor-instability intervention was created, and the combination of raised belt speed, raised vibration, and raised reject rate no longer all holds.',
    fromTemplate: false,
    catalogHidden: true,
  },

  {
    engineName: 'Stop machine after repeated stress on critical sensors',
    section: CEP,
    title: 'Stop machine after repeated stress on critical sensors',
    description:
      'Runs when this machine does not yet have MachineOverworked, no critical halt recommendation exists for it yet, and its stress indicator stayed true across five consecutive linked checks on the timeline for any of its critical sensors.',
    fromTemplate: false,
    produces: ['MachineOverworked', 'Intervention'],
  },

  ...trendPair(
    'Conveyor line — Vibration rising over 10 steps',
    'Conveyor line — Vibration rising trend ended',
    'Runs when the ten most recent vibration samples on the conveyor each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a conveyor vibration rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'Conveyor line — Belt speed rising over 10 steps',
    'Conveyor line — Belt speed rising trend ended',
    'Runs when the ten most recent belt speed samples on the conveyor each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a conveyor belt-speed rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'CNC mill — Temperature rising over 10 steps',
    'CNC mill — Temperature rising trend ended',
    'Runs when the ten most recent temperature samples on the CNC mill each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a CNC temperature rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'CNC mill — Vibration rising over 10 steps',
    'CNC mill — Vibration rising trend ended',
    'Runs when the ten most recent vibration samples on the CNC mill each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a CNC vibration rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'CNC mill — Spindle load rising over 10 steps',
    'CNC mill — Spindle load rising trend ended',
    'Runs when the ten most recent spindle load samples each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a CNC spindle-load rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'Auto packer — Throughput rising over 10 steps',
    'Auto packer — Throughput rising trend ended',
    'Runs when the ten most recent throughput samples each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a throughput rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'Auto packer — Reject rate rising over 10 steps',
    'Auto packer — Reject rate rising trend ended',
    'Runs when the ten most recent reject rate samples each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a reject-rate rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
  ...trendPair(
    'Auto packer — Seal temperature rising over 10 steps',
    'Auto packer — Seal temperature rising trend ended',
    'Runs when the ten most recent seal temperature samples each read higher than the one before, and no rising-trend anomaly is already open for this signal.',
    'Runs when a seal-temperature rising-trend anomaly is open and the last five samples each read lower than the previous.'
  ),
];

const byEngineName = new Map<string, RuleCatalogEntry>(RULE_CATALOG_ENTRIES.map((r) => [r.engineName, r]));

const primaryEngineByClearEngine = new Map<string, string>();
for (const r of RULE_CATALOG_ENTRIES) {
  if (r.clearCounterpart) {
    primaryEngineByClearEngine.set(r.clearCounterpart.engineName, r.engineName);
  }
}

export function ruleCatalogEntry(engineName: string): RuleCatalogEntry | undefined {
  return byEngineName.get(engineName);
}

export function primaryEngineForClearRule(engineName: string): string | undefined {
  return primaryEngineByClearEngine.get(engineName);
}

export function ruleFeedLabel(engineName: string): string {
  return ruleCatalogEntry(engineName)?.title ?? engineName;
}

export function rulesInSection(section: RuleCatalogSectionId): RuleCatalogEntry[] {
  return RULE_CATALOG_ENTRIES.filter((r) => r.section === section && !r.catalogHidden);
}
