"use client";

import { RepackItem } from "@/lib/repacks-plugin";
import { providerAccentVar, providerDisplayName } from "@/lib/providers";
import { relativeTime } from "@/lib/format";

export default function RepackCard({ item, onClick }: { item: RepackItem; onClick: () => void }) {
  const accentVar = providerAccentVar(item.provider);

  return (
    <button
      onClick={onClick}
      className="block w-full text-left border-b"
      style={{ borderColor: "var(--outline)", opacity: 0.99 }}
    >
      <div className="relative w-full aspect-video overflow-hidden" style={{ background: "var(--surface-raised)" }}>
        {item.bannerUrl && (
          // eslint-disable-next-line @next/next/no-img-element -- static export, no Image optimizer on-device
          <img src={item.bannerUrl} alt={item.title} className="w-full h-full object-cover" loading="lazy" />
        )}
        <div
          className="absolute inset-0 pointer-events-none"
          style={{
            background: `linear-gradient(to bottom, transparent 0%, transparent 60%, var(--surface) 100%)`,
          }}
        />
        <span
          className="absolute top-2.5 left-2.5 px-2.5 py-1 rounded-full text-[11px] font-semibold tracking-wide uppercase"
          style={{ background: `var(${accentVar})`, color: "var(--background)" }}
        >
          {providerDisplayName(item.provider)}
        </span>
      </div>
      <div className="px-4 py-3">
        <h3 className="text-base font-semibold leading-snug line-clamp-2" style={{ color: "var(--text-primary)" }}>
          {item.title}
        </h3>
        <div className="mt-1.5 text-xs flex items-center gap-1">
          <span className="font-semibold uppercase tracking-wide" style={{ color: `var(${accentVar})` }}>
            {relativeTime(item.timestamp)}
          </span>
          {item.repackSize && (
            <span style={{ color: "var(--text-secondary)" }}>&nbsp;//&nbsp;{item.repackSize}</span>
          )}
        </div>
      </div>
    </button>
  );
}
