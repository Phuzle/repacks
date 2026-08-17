"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import Repacks, { RepackItem, UserPreferences } from "./repacks-plugin";
import { parseSizeToGb } from "./format";

export type FeedFilter = { kind: "all" } | { kind: "provider"; id: string } | { kind: "watchlist" };

/** Client-side port of FeedViewModel's combine() — filtering is a display concern, so it stays in
 * the React layer even though the raw data + preferences come from the native bridge. */
export function useFeed() {
  const [items, setItems] = useState<RepackItem[]>([]);
  const [prefs, setPrefs] = useState<UserPreferences | null>(null);
  const [watchlistKeywords, setWatchlistKeywords] = useState<string[]>([]);
  const [filter, setFilter] = useState<FeedFilter>({ kind: "all" });
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncError, setSyncError] = useState<string | null>(null);
  const [loaded, setLoaded] = useState(false);

  const reload = useCallback(async () => {
    const [feedResult, prefsResult, watchlistResult] = await Promise.all([
      Repacks.getFeed(),
      Repacks.getPreferences(),
      Repacks.getWatchlist(),
    ]);
    setItems(feedResult.items);
    setPrefs(prefsResult);
    setWatchlistKeywords(watchlistResult.keywords.map((k) => k.keyword.trim().toLowerCase()).filter(Boolean));
    setLoaded(true);
  }, []);

  useEffect(() => {
    reload().catch((e) => setSyncError(e?.message ?? "Failed to load feed"));
  }, [reload]);

  const refresh = useCallback(async () => {
    if (isSyncing) return;
    setIsSyncing(true);
    setSyncError(null);
    try {
      await Repacks.sync();
      await reload();
    } catch (e: unknown) {
      setSyncError(e instanceof Error ? e.message : "Refresh failed — check your connection and try again.");
    } finally {
      setIsSyncing(false);
    }
  }, [isSyncing, reload]);

  const visibleItems = useMemo(() => {
    if (!prefs) return [];
    return items.filter((item) => {
      if (prefs.nsfwFilterEnabled && item.isNsfw) return false;
      if (prefs.maxSizeGb != null) {
        const gb = parseSizeToGb(item.repackSize);
        if (gb != null && gb > prefs.maxSizeGb) return false;
      }
      if (filter.kind === "provider" && item.provider !== filter.id) return false;
      if (filter.kind === "watchlist") {
        const title = item.title.toLowerCase();
        if (!watchlistKeywords.some((kw) => title.includes(kw))) return false;
      }
      return true;
    });
  }, [items, prefs, filter, watchlistKeywords]);

  return { items: visibleItems, prefs, filter, setFilter, isSyncing, refresh, syncError, loaded };
}
