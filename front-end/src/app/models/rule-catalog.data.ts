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

export const RULE_CATALOG_ENTRIES: RuleCatalogEntry[] = [
  {
    engineName: 'Plant climate — Ambient temperature above high band',
    section: L1,
    title: 'Plant climate — Ambient temperature above high band',
    description:
      'Runs when the latest ambient temperature reading for plant climate reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'Plant climate — Humidity above high band',
    section: L1,
    title: 'Plant climate — Humidity above high band',
    description:
      'Runs when the latest humidity reading for plant climate reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'Conveyor line — Vibration above high band',
    section: L1,
    title: 'Conveyor line — Vibration above high band',
    description:
      'Runs when the latest vibration reading for the conveyor reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'CNC mill — Temperature above high band',
    section: L1,
    title: 'CNC mill — Temperature above high band',
    description:
      'Runs when the latest temperature reading for the CNC mill reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'CNC mill — Vibration above high band',
    section: L1,
    title: 'CNC mill — Vibration above high band',
    description:
      'Runs when the latest vibration reading for the CNC mill reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'CNC mill — Spindle load above high band',
    section: L1,
    title: 'CNC mill — Spindle load above high band',
    description:
      'Runs when the latest spindle load reading for the CNC mill reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'Auto packer — Reject rate above high band',
    section: L1,
    title: 'Auto packer — Reject rate above high band',
    description:
      'Runs when the latest reject rate for the auto packer reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },
  {
    engineName: 'Auto packer — Seal temperature above high band',
    section: L1,
    title: 'Auto packer — Seal temperature above high band',
    description:
      'Runs when the latest seal temperature for the auto packer reaches or passes its high limit, and the same issue is not already recorded as an open anomaly.',
    fromTemplate: true,
    produces: ['Anomaly'],
  },

  {
    engineName: 'Safety evaluation: not safe',
    section: L2,
    title: 'Safety evaluation: not safe',
    description:
      'Runs when a safety check is requested for a machine that already has an unsafe reason and no safety outcome exists yet.',
    fromTemplate: false,
    produces: ['Safety result'],
  },
  {
    engineName: 'Safety evaluation: safe',
    section: L2,
    title: 'Safety evaluation: safe',
    description:
      'Runs when a safety check is requested, no unsafe reason is present, and no safety outcome exists yet.',
    fromTemplate: false,
    produces: ['Safety result'],
  },

  {
    engineName: 'Pushed conveyor line (belt speed, vibration, pack throughput)',
    section: L3,
    title: 'Pushed conveyor line (belt speed, vibration, pack throughput)',
    description:
      'Runs when belt speed, conveyor vibration, and pack throughput are all in their raised bands at the same time, and the matching high-priority recommendation is not already active.',
    fromTemplate: false,
    produces: ['Intervention'],
    clearCounterpart: {
      engineName: 'Clear pushed-line intervention',
      title: 'Clear pushed-line intervention',
      description:
        'Runs once simulated time has moved past the moment the pushed-line intervention was created, and belt speed, vibration, and pack throughput are no longer all in their raised band together.',
      removes: ['Intervention'],
    },
  },
  {
    engineName: 'Clear pushed-line intervention',
    section: L2,
    title: 'Clear pushed-line intervention',
    description:
      'Runs once simulated time has moved past the moment the pushed-line intervention was created, and belt speed, vibration, and pack throughput are no longer all in their raised band together.',
    fromTemplate: false,
    catalogHidden: true,
  },

  {
    engineName: 'Conveyor line instability (belt speed, vibration, reject rate)',
    section: L3,
    title: 'Conveyor line instability (belt speed, vibration, reject rate)',
    description:
      'Runs when belt speed and conveyor vibration are raised while reject rate is up, and the matching medium-priority recommendation is not already active.',
    fromTemplate: false,
    produces: ['Intervention'],
    clearCounterpart: {
      engineName: 'Clear conveyor-instability intervention',
      title: 'Clear conveyor-instability intervention',
      description:
        'Runs once simulated time has moved past the moment the conveyor-instability intervention was created, and the combination of raised belt speed, raised vibration, and raised reject rate no longer all holds.',
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
      'Runs when this machine does not yet have an operator-halt unsafe reason, no critical halt recommendation exists for it yet, and its stress indicator stayed true across five consecutive linked checks on the timeline for any of its critical sensors.',
    fromTemplate: false,
    produces: ['Unsafe reason', 'Intervention'],
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

  {
    engineName: 'MachineHealth',
    section: QUERY,
    title: 'Machine health',
    description:
      'Answers “how healthy is this machine right now?” for the one you picked. It looks at what has already been logged for that machine—issues spotted, steps taken, times it had to stop for safety, and times an operator cleared a problem—and turns that into a single score from 0 to 100. Nothing new is written to the log when you ask; it only reads what is already there.',
    fromTemplate: false,
  },
];

const byEngineName = new Map<string, RuleCatalogEntry>(RULE_CATALOG_ENTRIES.map((r) => [r.engineName, r]));

/** Clearing-rule engine name → primary card engine name (for focus / expand). */
const primaryEngineByClearEngine = new Map<string, string>();
for (const r of RULE_CATALOG_ENTRIES) {
  if (r.clearCounterpart) {
    primaryEngineByClearEngine.set(r.clearCounterpart.engineName, r.engineName);
  }
}

export function ruleCatalogEntry(engineName: string): RuleCatalogEntry | undefined {
  return byEngineName.get(engineName);
}

/** If this engine name is a hidden clear rule, returns the primary card to show and highlight. */
export function primaryEngineForClearRule(engineName: string): string | undefined {
  return primaryEngineByClearEngine.get(engineName);
}

export function ruleFeedLabel(engineName: string): string {
  return ruleCatalogEntry(engineName)?.title ?? engineName;
}

export function rulesInSection(section: RuleCatalogSectionId): RuleCatalogEntry[] {
  return RULE_CATALOG_ENTRIES.filter((r) => r.section === section && !r.catalogHidden);
}
