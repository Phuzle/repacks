"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Repacks, { RepackItem } from "@/lib/repacks-plugin";
import { providerAccentVar, providerDisplayName } from "@/lib/providers";
import { relativeTime } from "@/lib/format";

function DetailContent() {
  const params = useSearchParams();
  const router = useRouter();
  const provider = params.get("provider") ?? "";
  const slug = params.get("slug") ?? "";
  const [item, setItem] = useState<Partial<RepackItem> | null>(null);

  useEffect(() => {
    if (!provider || !slug) return;
    Repacks.getItem({ provider, slug }).then(setItem);
  }, [provider, slug]);

  if (!item) {
    return (
      <p className="px-5 py-10 text-center text-sm" style={{ color: "var(--text-secondary)" }}>
        Loading…
      </p>
    );
  }

  const accentVar = providerAccentVar(provider);

  return (
    <div>
      <header className="flex items-center gap-3 px-4 pt-3 pb-2">
        <button onClick={() => router.back()} aria-label="Back" style={{ color: `var(${accentVar})` }}>
          ←
        </button>
        <span className="text-xs font-semibold uppercase tracking-wide" style={{ color: `var(${accentVar})` }}>
          {providerDisplayName(provider)}
        </span>
      </header>

      {item.bannerUrl && (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={item.bannerUrl} alt={item.title ?? ""} className="w-full aspect-video object-cover" />
      )}

      <div className="px-4 py-4">
        <h1 className="text-xl font-bold" style={{ color: "var(--text-primary)" }}>
          {item.title}
        </h1>
        <p className="mt-2 text-xs uppercase tracking-wide" style={{ color: `var(${accentVar})` }}>
          {item.timestamp ? relativeTime(item.timestamp) : ""}
          {item.repackSize ? `  //  ${item.repackSize}` : ""}
        </p>
        {item.description && (
          <p className="mt-4 text-sm leading-relaxed" style={{ color: "var(--text-primary)" }}>
            {item.description}
          </p>
        )}
        {item.originalUrl && (
          <a
            href={item.originalUrl}
            target="_blank"
            rel="noreferrer"
            className="mt-6 block text-center py-3 rounded-xl text-sm font-semibold uppercase tracking-wide"
            style={{ background: `var(${accentVar})`, color: "var(--background)" }}
          >
            Open on {providerDisplayName(provider)}
          </a>
        )}
      </div>
    </div>
  );
}

export default function DetailPage() {
  return (
    <Suspense fallback={null}>
      <DetailContent />
    </Suspense>
  );
}
