// Mirrors data/remote/providers/FeedProvider.kt — kept in sync by hand since it's tiny and rarely
// changes. If a provider is added/removed there, mirror it here too.
export interface ProviderInfo {
  id: string;
  displayName: string;
  accentVar: string; // CSS custom property name, see globals.css
}

export const PROVIDERS: ProviderInfo[] = [
  { id: "fitgirl", displayName: "FitGirl Repacks", accentVar: "--accent-magenta" },
  { id: "dodi", displayName: "DODI Repacks", accentVar: "--accent-cyan" },
  { id: "steamrip", displayName: "SteamRIP", accentVar: "--accent-violet" },
  // kaoskrew stays disabled until a real feed URL is confirmed — see FeedProvider.kt.
];

export function providerAccentVar(providerId: string): string {
  return PROVIDERS.find((p) => p.id === providerId)?.accentVar ?? "--accent-cyan";
}

export function providerDisplayName(providerId: string): string {
  return PROVIDERS.find((p) => p.id === providerId)?.displayName ?? providerId;
}
