"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useFeed } from "@/lib/useFeed";
import RepackCard from "@/components/RepackCard";
import FilterSheet from "@/components/FilterSheet";

export default function FeedPage() {
  const { items, filter, setFilter, isSyncing, refresh, syncError, loaded } = useFeed();
  const [filterOpen, setFilterOpen] = useState(false);
  const router = useRouter();

  return (
    <div>
      <header className="flex items-center justify-between px-5 pt-3 pb-1">
        <div className="flex items-center gap-2">
          <span
            className="w-2 h-2 rounded-sm"
            style={{
              background: isSyncing ? "var(--accent-violet)" : "var(--accent-cyan)",
              opacity: isSyncing ? 0.6 : 1,
            }}
          />
          <h1 className="text-2xl font-bold tracking-wide" style={{ color: "var(--accent-cyan)" }}>
            REPACKS
          </h1>
        </div>
        <button
          onClick={() => setFilterOpen(true)}
          aria-label="Filter"
          className="p-2 rounded-full"
          style={{ color: "var(--accent-cyan)" }}
        >
          <FilterIcon />
        </button>
      </header>

      {syncError && (
        <div className="mx-4 mb-2 px-3 py-2 rounded-lg text-sm" style={{ background: "var(--surface-raised)", color: "var(--accent-red)" }}>
          {syncError}
        </div>
      )}

      <div className="flex justify-end px-4 pb-2">
        <button
          onClick={refresh}
          disabled={isSyncing}
          className="text-xs font-semibold uppercase tracking-wide px-3 py-1.5 rounded-full"
          style={{ background: "var(--surface-raised)", color: "var(--text-secondary)" }}
        >
          {isSyncing ? "Syncing…" : "Refresh"}
        </button>
      </div>

      {!loaded ? (
        <p className="px-5 py-10 text-center text-sm" style={{ color: "var(--text-secondary)" }}>
          Loading…
        </p>
      ) : items.length === 0 ? (
        <div className="px-5 py-16 text-center">
          <p className="text-sm font-semibold uppercase tracking-wide" style={{ color: "var(--accent-cyan)" }}>
            No signal
          </p>
          <p className="mt-2 text-sm" style={{ color: "var(--text-secondary)" }}>
            Refresh, or check Settings → Providers.
          </p>
        </div>
      ) : (
        <div>
          {items.map((item) => (
            <RepackCard
              key={item.id}
              item={item}
              onClick={() => router.push(`/detail?provider=${encodeURIComponent(item.provider)}&slug=${encodeURIComponent(item.slug)}`)}
            />
          ))}
        </div>
      )}

      {filterOpen && <FilterSheet selected={filter} onSelect={setFilter} onClose={() => setFilterOpen(false)} />}
    </div>
  );
}

function FilterIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
    </svg>
  );
}
