export type RuleCatalogSectionId = 'L1' | 'L2' | 'L3' | 'CEP' | 'QUERY';

/** Human-readable outputs shown as “Produces: …” lines on the card. */
export type RuleCatalogProduceLabel = string;

export interface RuleCatalogClearCounterpart {
  /** Backend rule name (for lookups / rule feed). */
  engineName: string;
  title: string;
  /** When the clearing rule runs (no “produces” prose here—use removes). */
  description: string;
  /** Shown as “Removes: …” lines inside the expandable block. */
  removes: RuleCatalogProduceLabel[];
}

export interface RuleCatalogEntry {
  /** Exact name when the rule fires (matches backend). */
  engineName: string;
  section: RuleCatalogSectionId;
  title: string;
  /** When this rule runs (conditions only—no “produces” wording). */
  description: string;
  fromTemplate: boolean;
  /** If true, no standalone card; still in the index for the rule feed and ? focus. */
  catalogHidden?: boolean;
  /** Lines shown at the bottom as “Produces: …”. Omit or empty if nothing is asserted. */
  produces?: RuleCatalogProduceLabel[];
  /** Optional paired rule that clears the effect of this one; hidden until “Clearable” is expanded. */
  clearCounterpart?: RuleCatalogClearCounterpart;
}

export const RULE_CATALOG_SECTION_ORDER: RuleCatalogSectionId[] = [
  'L1',
  'L2',
  'L3',
  'CEP',
  'QUERY',
];

export const RULE_CATALOG_SECTION_LABEL: Record<RuleCatalogSectionId, string> = {
  L1: 'L1 rules',
  L2: 'L2 rules',
  L3: 'L3 rules',
  CEP: 'CEP rules',
  QUERY: 'Query (backward chaining) rules',
};
