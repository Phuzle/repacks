"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const TABS = [
  { href: "/", label: "Feed" },
  { href: "/configure", label: "Configure" },
  { href: "/settings", label: "Settings" },
];

export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 flex border-t"
      style={{ background: "var(--surface)", borderColor: "var(--outline)" }}
    >
      {TABS.map((tab) => {
        const active = pathname === tab.href;
        return (
          <Link
            key={tab.href}
            href={tab.href}
            className="flex-1 flex flex-col items-center gap-1 py-3 text-xs font-medium uppercase tracking-wide"
            style={{ color: active ? "var(--accent-cyan)" : "var(--text-secondary)" }}
          >
            {tab.label}
            <span
              className="block h-0.5 w-5 rounded-full"
              style={{ background: active ? "var(--accent-cyan)" : "transparent" }}
            />
          </Link>
        );
      })}
    </nav>
  );
}
