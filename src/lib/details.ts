// Mirrors data/remote/providers/RepackDetails.kt's toJson() shape exactly.
export interface SystemRequirements {
  os?: string;
  processor?: string;
  memory?: string;
  graphics?: string;
  directX?: string;
  storage?: string;
  notes?: string;
}

export interface LabeledValue {
  label: string;
  value: string;
}

export interface RepackDetails {
  developer?: string;
  publisher?: string;
  franchise?: string;
  languages?: string;
  releaseDate?: string;
  systemRequirements?: SystemRequirements;
  installSteps?: string[];
  notes?: LabeledValue[];
}

export function parseDetails(json: string | null | undefined): RepackDetails | null {
  if (!json) return null;
  try {
    return JSON.parse(json) as RepackDetails;
  } catch {
    return null;
  }
}
