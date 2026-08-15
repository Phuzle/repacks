package com.phuzle.labs.repacks.data.remote.providers

import android.net.Uri
import android.util.Xml
import java.io.StringReader
import java.time.Instant
import java.time.OffsetDateTime
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/** Atom parser for KaOsKrew's release feed (PRD §6.1). */
class AtomFeedParser : FeedParser {

    override fun parse(body: String): List<ParsedRepackItem> {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(DescriptionExtractor.sanitizeXml(body)))
        }

        val items = mutableListOf<ParsedRepackItem>()
        var eventType = parser.eventType
        var currentTag: String? = null
        var insideEntry = false

        var title = StringBuilder()
        var id = StringBuilder()
        var updated = StringBuilder()
        var content = StringBuilder()
        var link = ""
        val categories = mutableListOf<String>()

        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        when (currentTag) {
                            "entry" -> {
                                insideEntry = true
                                title = StringBuilder(); id = StringBuilder(); updated = StringBuilder(); content = StringBuilder()
                                link = ""; categories.clear()
                            }
                            "link" -> if (insideEntry && link.isEmpty()) {
                                link = parser.getAttributeValue(null, "href").orEmpty()
                            }
                            "category" -> if (insideEntry) {
                                parser.getAttributeValue(null, "term")?.let(categories::add)
                            }
                        }
                    }
                    XmlPullParser.TEXT -> if (insideEntry) {
                        when (currentTag) {
                            "title" -> title.append(parser.text)
                            "id" -> id.append(parser.text)
                            "updated", "published" -> updated.append(parser.text)
                            "summary", "content" -> content.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "entry" && insideEntry) {
                            insideEntry = false
                            buildEntry(
                                title = title.toString().trim(),
                                link = link,
                                idRaw = id.toString().trim(),
                                updatedRaw = updated.toString().trim(),
                                contentHtml = content.toString().trim(),
                                categories = categories.toList(),
                            )?.let(items::add)
                        }
                    }
                    else -> Unit
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            // Return the items parsed so far instead of losing the whole feed.
        }
        return items
    }

    private fun buildEntry(
        title: String,
        link: String,
        idRaw: String,
        updatedRaw: String,
        contentHtml: String,
        categories: List<String>,
    ): ParsedRepackItem? {
        if (title.isEmpty() || link.isEmpty()) return null
        val plainText = DescriptionExtractor.extractPlainText(contentHtml)
        val (originalSize, repackSize) = DescriptionExtractor.extractSizes(plainText)
        val genres = DescriptionExtractor.extractGenres(plainText).ifEmpty { categories }
        val slugSource = Uri.parse(link).lastPathSegment?.takeIf { it.isNotBlank() } ?: title
        return ParsedRepackItem(
            guid = idRaw.ifEmpty { link },
            slug = DescriptionExtractor.slugify(slugSource),
            title = title,
            bannerUrl = DescriptionExtractor.extractBannerUrl(contentHtml, baseUri = link),
            originalUrl = link,
            originalSize = originalSize,
            repackSize = repackSize,
            genres = genres,
            description = plainText.take(4000),
            timestamp = parseIso8601(updatedRaw) ?: System.currentTimeMillis(),
            isNsfw = DescriptionExtractor.isLikelyNsfw(title, categories),
        )
    }

    private fun parseIso8601(value: String): Long? {
        if (value.isEmpty()) return null
        return runCatching { Instant.parse(value).toEpochMilli() }
            .getOrElse { runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull() }
    }
}
