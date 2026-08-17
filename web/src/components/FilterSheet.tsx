"use client";

import { PROVIDERS } from "@/lib/providers";
import { FeedFilter } from "@/lib/useFeed";

export default function FilterSheet({
  selected,
  onSelect,
  onClose,
}: {
  selected: FeedFilter;
  onSelect: (filter: FeedFilter) => void;
  onClose: () => void;
}) {
  const isSelected = (filter: FeedFilter) =>
    filter.kind === selected.kind && (filter.kind !== "provider" || (selected.kind === "provider" && selected.id === filter.id));

  const row = (filter: FeedFilter, label: string, accentVar: string) => (
    <button
      key={label}
      onClick={() => {
        onSelect(filter);
        onClose();
      }}
      className="w-full flex items-center justify-between px-4 py-3.5 text-left"
      style={{ color: "var(--text-primary)" }}
    >
      <span className="text-base">{label}</span>
      <span
        className="w-3 h-3 rounded-full border"
        style={{
          background: isSelected(filter) ? `var(${accentVar})` : "transparent",
          borderColor: `var(${accentVar})`,
        }}
      />
    </button>
  );

  return (
    <div className="fixed inset-0 z-50 flex flex-col justify-end" onClick={onClose}>
      <div className="absolute inset-0" style={{ background: "rgba(0,0,0,0.5)" }} />
      <div
        className="relative rounded-t-2xl overflow-hidden pb-6"
        style={{ background: "var(--surface)" }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-4 pt-4 pb-2 text-xs font-semibold uppercase tracking-wide" style={{ color: "var(--text-secondary)" }}>
          Filter feed
        </div>
        <div className="divide-y" style={{ borderColor: "var(--outline)" }}>
          {row({ kind: "all" }, "All", "--accent-cyan")}
          {PROVIDERS.map((p) => row({ kind: "provider", id: p.id }, p.displayName, p.accentVar))}
          {row({ kind: "watchlist" }, "Watchlist", "--accent-violet")}
        </div>
      </div>
    </div>
  );
}
