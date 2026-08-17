export function relativeTime(timestampMillis: number): string {
  const diffMs = Math.max(0, Date.now() - timestampMillis);
  const minutes = Math.floor(diffMs / 60000);
  const hours = Math.floor(diffMs / 3600000);
  const days = Math.floor(diffMs / 86400000);
  if (minutes < 1) return "just now";
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  return `${days}d ago`;
}

// Mirrors data/remote/providers/SizeUnits.kt's parseToGb — kept in sync by hand.
const SIZE_REGEX = /([0-9.]+)\s*([KMGT]B)/i;

export function parseSizeToGb(sizeText: string | null | undefined): number | null {
  if (!sizeText) return null;
  const match = SIZE_REGEX.exec(sizeText);
  if (!match) return null;
  const value = parseFloat(match[1]);
  if (Number.isNaN(value)) return null;
  switch (match[2].toUpperCase()) {
    case "KB":
      return value / (1024 * 1024);
    case "MB":
      return value / 1024;
    case "GB":
      return value;
    case "TB":
      return value * 1024;
    default:
      return null;
  }
}

export function parseGenres(genresJson: string | null | undefined): string[] {
  if (!genresJson) return [];
  try {
    const parsed = JSON.parse(genresJson);
    return Array.isArray(parsed) ? parsed.filter((g): g is string => typeof g === "string") : [];
  } catch {
    return [];
  }
}
