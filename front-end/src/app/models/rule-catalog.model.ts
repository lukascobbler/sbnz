export type RuleCatalogSectionId = 'L1' | 'L2' | 'L3' | 'CEP';

export type RuleCatalogProduceLabel = string;

export interface RuleCatalogClearCounterpart {
  engineName: string;
  title: string;
  description: string;
  removes: RuleCatalogProduceLabel[];
}

export interface RuleCatalogEntry {
  engineName: string;
  section: RuleCatalogSectionId;
  title: string;
  description: string;
  fromTemplate: boolean;
  catalogHidden?: boolean;
  produces?: RuleCatalogProduceLabel[];
  clearCounterpart?: RuleCatalogClearCounterpart;
}

export const RULE_CATALOG_SECTION_ORDER: RuleCatalogSectionId[] = [
  'L1',
  'L2',
  'L3',
  'CEP',
];

export const RULE_CATALOG_SECTION_LABEL: Record<RuleCatalogSectionId, string> = {
  L1: 'L1 rules',
  L2: 'L2 rules',
  L3: 'L3 rules',
  CEP: 'CEP rules',
};
