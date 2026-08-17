import type { NextConfig } from "next";

// Capacitor loads the built web assets straight off disk on-device (no Node
// server), so this has to be a fully static export — no SSR, no API routes,
// no dynamic-route params computed at request time. Dynamic screens (e.g.
// the item detail page) read state from the URL's query string / client
// state instead of a Next.js dynamic route segment.
const nextConfig: NextConfig = {
  output: "export",
  images: {
    unoptimized: true,
  },
};

export default nextConfig;
