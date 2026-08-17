"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Repacks, { RepackItem } from "@/lib/repacks-plugin";
import { providerAccentVar, providerDisplayName } from "@/lib/providers";
import { relativeTime, parseGenres } from "@/lib/format";
import { parseDetails } from "@/lib/details";

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null;
  return (
    <div className="flex justify-between gap-4 py-1.5 text-sm border-b last:border-b-0" style={{ borderColor: "var(--outline)" }}>
      <span style={{ color: "var(--text-secondary)" }}>{label}</span>
      <span className="text-right" style={{ color: "var(--text-primary)" }}>
        {value}
      </span>
    </div>
  );
}

function SectionCard({ title, accentVar, children }: { title: string; accentVar: string; children: React.ReactNode }) {
  return (
    <div className="mx-4 mt-4 rounded-xl border overflow-hidden" style={{ borderColor: "var(--outline)", background: "var(--surface)" }}>
      <div className="flex items-center gap-2 px-4 pt-3 pb-1">
        <span className="w-1 h-3.5 rounded-full" style={{ background: `var(${accentVar})` }} />
        <h2 className="text-xs font-semibold uppercase tracking-wide" style={{ color: `var(${accentVar})` }}>
          {title}
        </h2>
      </div>
      <div className="px-4 pb-3">{children}</div>
    </div>
  );
}

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
  const genres = parseGenres(item.genres);
  const details = parseDetails(item.details);
  const sysreq = details?.systemRequirements;
  const hasSysreq = sysreq && Object.values(sysreq).some(Boolean);
  const hasInfo = details?.developer || details?.publisher || details?.franchise || details?.languages || details?.releaseDate;

  return (
    <div className="pb-8">
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

      <div className="px-4 pt-4">
        <h1 className="text-xl font-bold" style={{ color: "var(--text-primary)" }}>
          {item.title}
        </h1>
        <p className="mt-2 text-xs uppercase tracking-wide" style={{ color: `var(${accentVar})` }}>
          {item.timestamp ? relativeTime(item.timestamp) : ""}
          {item.repackSize ? `  //  ${item.repackSize}` : ""}
          {item.originalSize ? ` (from ${item.originalSize})` : ""}
        </p>
        {genres.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1.5">
            {genres.map((g) => (
              <span
                key={g}
                className="px-2 py-0.5 rounded-full text-[11px] uppercase tracking-wide"
                style={{ background: "var(--surface-raised)", color: "var(--text-secondary)" }}
              >
                {g}
              </span>
            ))}
          </div>
        )}
      </div>

      {hasInfo && (
        <SectionCard title="Info" accentVar={accentVar}>
          <InfoRow label="Developer" value={details?.developer} />
          <InfoRow label="Publisher" value={details?.publisher} />
          <InfoRow label="Franchise" value={details?.franchise} />
          <InfoRow label="Languages" value={details?.languages} />
          <InfoRow label="Release Date" value={details?.releaseDate} />
        </SectionCard>
      )}

      {item.description && (
        <SectionCard title="Description" accentVar={accentVar}>
          <p className="text-sm leading-relaxed" style={{ color: "var(--text-primary)" }}>
            {item.description}
          </p>
        </SectionCard>
      )}

      {hasSysreq && (
        <SectionCard title="System Requirements" accentVar={accentVar}>
          <InfoRow label="OS" value={sysreq?.os} />
          <InfoRow label="Processor" value={sysreq?.processor} />
          <InfoRow label="Memory" value={sysreq?.memory} />
          <InfoRow label="Graphics" value={sysreq?.graphics} />
          <InfoRow label="DirectX" value={sysreq?.directX} />
          <InfoRow label="Storage" value={sysreq?.storage} />
          {sysreq?.notes && (
            <p className="mt-2 text-xs" style={{ color: "var(--text-secondary)" }}>
              {sysreq.notes}
            </p>
          )}
        </SectionCard>
      )}

      {details?.notes && details.notes.length > 0 && (
        <SectionCard title="Repack Notes" accentVar={accentVar}>
          {details.notes.map((n) => (
            <InfoRow key={n.label} label={n.label} value={n.value} />
          ))}
        </SectionCard>
      )}

      {details?.installSteps && details.installSteps.length > 0 && (
        <SectionCard title="How to Install" accentVar={accentVar}>
          <ol className="space-y-1.5 text-sm list-decimal list-inside" style={{ color: "var(--text-primary)" }}>
            {details.installSteps.map((step, i) => (
              <li key={i}>{step}</li>
            ))}
          </ol>
        </SectionCard>
      )}

      {item.originalUrl && (
        <div className="px-4 mt-5">
          <a
            href={item.originalUrl}
            target="_blank"
            rel="noreferrer"
            className="block text-center py-3 rounded-xl text-sm font-semibold uppercase tracking-wide"
            style={{ background: `var(${accentVar})`, color: "var(--background)" }}
          >
            Open on {providerDisplayName(provider)}
          </a>
        </div>
      )}
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
