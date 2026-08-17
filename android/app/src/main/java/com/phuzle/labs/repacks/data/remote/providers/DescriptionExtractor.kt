package com.phuzle.labs.repacks.data.remote.providers

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

/** Jsoup/regex helpers shared by [RssFeedParser] and [AtomFeedParser] for pulling clean metadata
 * out of a feed item's raw HTML description (PRD §6.1's "Jsoup description extraction"). */
object DescriptionExtractor {

    private val SIZE_REGEX = Regex("""(?i)(original|repack)\s*size[:\-]?\s*([0-9.,]+\s*[KMGT]B)""")
    private val GENRES_LINE_REGEX = Regex("(?im)^genres?(?:/tags)?[:\\-]\\s*(.+)$")
    private val NSFW_KEYWORDS = listOf("nsfw", "adult only", "hentai", "+18", "18+", "xxx")
    private val COLLAPSE_TOKEN_REGEX = Regex("""(?i)\[collapse]""")
    private val WORDPRESS_EXCERPT_TAIL_REGEX = Regex("""(?i)continue reading.*$|→?\s*the post .* appeared first on .*$""")
    private val BLOCK_TAGS = setOf(
        "p", "div", "li", "h1", "h2", "h3", "h4", "h5", "h6", "tr", "br", "section", "article", "blockquote", "ul", "ol",
    )

    /** [baseUri] (the item's own article URL) lets Jsoup resolve relative `src` values — without
     * it, "abs:src" silently resolves against no base and produces a blank/broken URL. */
    fun extractBannerUrl(html: String, baseUri: String): String? =
        Jsoup.parse(html, baseUri).select("img[src]").firstOrNull()?.attr("abs:src")?.takeIf { it.isNotBlank() }

    /** Some feeds (DODI's, at least) leak raw, invalidly-escaped HTML fragments (backslash-escaped
     * quotes from a JS string literal) directly into the RSS XML outside any CDATA section, which
     * breaks strict XML parsing entirely. Neither backslash-quote nor backslash-apostrophe is a
     * legal XML escape, so stripping the stray backslashes is a safe normalization. */
    fun sanitizeXml(raw: String): String = raw.replace("\\\"", "\"").replace("\\'", "'")

    /** Renders [html] to plain text with a real newline at every block-level boundary, unlike
     * Jsoup's own [Element.text] which joins the entire document into one space-separated line —
     * that flattening is why the multi-line "Label: value" regexes below (and the structured
     * extraction in [RepackDetailsExtractor]) need actual line breaks to anchor on. Also drops the
     * literal "[collapse]" tokens some sites' spoiler widgets leave behind as text. */
    fun extractPlainText(html: String): String {
        val sb = StringBuilder()
        Jsoup.parse(html).body().traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                if (node is TextNode) sb.append(node.text())
            }

            override fun tail(node: Node, depth: Int) {
                if (node is Element && node.tagName().lowercase() in BLOCK_TAGS && sb.isNotEmpty() && sb.last() != '\n') {
                    sb.append('\n')
                }
            }
        })
        return sb.toString()
            .replace(COLLAPSE_TOKEN_REGEX, "")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !WORDPRESS_EXCERPT_TAIL_REGEX.matches(it) }
            .joinToString("\n")
    }

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
