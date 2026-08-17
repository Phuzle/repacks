package com.phuzle.labs.repacks.data.remote.providers

import org.jsoup.Jsoup

/** Jsoup/regex helpers shared by [RssFeedParser] and [AtomFeedParser] for pulling clean metadata
 * out of a feed item's raw HTML description (PRD §6.1's "Jsoup description extraction"). */
object DescriptionExtractor {

    private val SIZE_REGEX = Regex("""(?i)(original|repack)\s*size[:\-]?\s*([0-9.,]+\s*[KMGT]B)""")
    private val GENRES_LINE_REGEX = Regex("(?im)^genres?(?:/tags)?[:\\-]\\s*(.+)$")
    private val NSFW_KEYWORDS = listOf("nsfw", "adult only", "hentai", "+18", "18+", "xxx")

    /** [baseUri] (the item's own article URL) lets Jsoup resolve relative `src` values — without
     * it, "abs:src" silently resolves against no base and produces a blank/broken URL. */
    fun extractBannerUrl(html: String, baseUri: String): String? =
        Jsoup.parse(html, baseUri).select("img[src]").firstOrNull()?.attr("abs:src")?.takeIf { it.isNotBlank() }

    /** Some feeds (DODI's, at least) leak raw, invalidly-escaped HTML fragments (backslash-escaped
     * quotes from a JS string literal) directly into the RSS XML outside any CDATA section, which
     * breaks strict XML parsing entirely. Neither backslash-quote nor backslash-apostrophe is a
     * legal XML escape, so stripping the stray backslashes is a safe normalization. */
    fun sanitizeXml(raw: String): String = raw.replace("\\\"", "\"").replace("\\'", "'")

    fun extractPlainText(html: String): String = Jsoup.parse(html).text()

    /** Returns (originalSize, repackSize), either of which may be null if not present in the text. */
    fun extractSizes(plainText: String): Pair<String?, String?> {
        var original: String? = null
        var repack: String? = null
        for (match in SIZE_REGEX.findAll(plainText)) {
            val value = match.groupValues[2].trim()
            if (match.groupValues[1].equals("original", ignoreCase = true)) original = value else repack = value
        }
        return original to repack
    }

    fun extractGenres(plainText: String): List<String> {
        val line = GENRES_LINE_REGEX.find(plainText)?.groupValues?.get(1) ?: return emptyList()
        return line.split(",", "/", "|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun isLikelyNsfw(title: String, categories: List<String>): Boolean {
        val haystack = (listOf(title) + categories).joinToString(" ").lowercase()
        return NSFW_KEYWORDS.any { haystack.contains(it) }
    }

    fun slugify(input: String): String = input.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .take(80)
        .ifEmpty { "item-${System.nanoTime()}" }
}
