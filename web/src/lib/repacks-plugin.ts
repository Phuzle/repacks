import { registerPlugin } from "@capacitor/core";

export interface RepackItem {
  id: number;
  guid: string;
  provider: string;
  slug: string;
  title: string;
  bannerUrl: string | null;
  originalUrl: string;
  originalSize: string | null;
  repackSize: string | null;
  genres: string | null; // JSON-encoded string array — JSON.parse before use
  description: string | null;
  timestamp: number;
  isNsfw: boolean;
  isFavorited: boolean;
}

export type ThemeMode = "SYSTEM" | "LIGHT" | "DARK_AMOLED";

export interface UserPreferences {
  themeMode: ThemeMode;
  enabledProviderIds: string[];
  nsfwFilterEnabled: boolean;
  maxSizeGb?: number;
  syncIntervalHours: number;
  wifiOnly: boolean;
  quietHoursEnabled: boolean;
  quietHoursStartHour: number;
  quietHoursEndHour: number;
  silentNotificationsOnly: boolean;
  proxyListRaw: string;
  autoRotateOnBlock: boolean;
  lastRotationStatus?: string;
  autoUpdateCheckEnabled: boolean;
}

export interface WatchlistKeyword {
  id: number;
  keyword: string;
}

export interface SyncResult {
  watchlistMatches: RepackItem[];
  otherNewItems: RepackItem[];
  shouldBackoff: boolean;
}

export interface UpdateCheckResult {
  available: boolean;
  versionName?: string;
}

/** The whole surface of the native bridge — every method here is a 1:1 call into
 * RepacksPlugin.kt, which itself just forwards to the unchanged native repositories.
 * See web/android/app/src/main/java/com/phuzle/labs/repacks/RepacksPlugin.kt. */
export interface RepacksPluginApi {
  getFeed(): Promise<{ items: RepackItem[] }>;
  getItem(options: { provider: string; slug: string }): Promise<Partial<RepackItem>>;
  sync(): Promise<SyncResult>;
  setFavorited(options: { id: number; favorited: boolean }): Promise<void>;

  getWatchlist(): Promise<{ keywords: WatchlistKeyword[] }>;
  addWatchlistKeyword(options: { keyword: string }): Promise<void>;
  removeWatchlistKeyword(options: { id: number }): Promise<void>;

  getPreferences(): Promise<UserPreferences>;
  setThemeMode(options: { mode: ThemeMode }): Promise<void>;
  setProviderEnabled(options: { id: string; enabled: boolean }): Promise<void>;
  setNsfwFilterEnabled(options: { enabled: boolean }): Promise<void>;
  setMaxSizeGb(options: { value?: number }): Promise<void>;
  setSyncIntervalHours(options: { hours: number }): Promise<void>;
  setWifiOnly(options: { enabled: boolean }): Promise<void>;
  setQuietHours(options: { enabled: boolean; startHour: number; endHour: number }): Promise<void>;
  setSilentNotificationsOnly(options: { silent: boolean }): Promise<void>;
  setProxyListRaw(options: { raw: string }): Promise<void>;
  setAutoRotateOnBlock(options: { enabled: boolean }): Promise<void>;
  setAutoUpdateCheckEnabled(options: { enabled: boolean }): Promise<void>;

  checkForUpdate(options: { force: boolean }): Promise<UpdateCheckResult>;
  canRequestInstallPackages(): Promise<{ canInstall: boolean }>;
  requestInstallPermission(): Promise<void>;
  installUpdate(): Promise<void>;
}

const Repacks = registerPlugin<RepacksPluginApi>("Repacks");

export default Repacks;
