package com.phuzle.labs.repacks.data.remote.providers

import android.net.Uri
import android.util.Xml
import java.io.StringReader
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/** RSS 2.0 parser (FitGirl, DODI, SteamRIP per PRD §6.1) using Android's built-in XmlPullParser,
 * with [DescriptionExtractor] cleaning up each item's HTML description. */
class RssFeedParser : FeedParser {

    override fun parse(body: String): List<ParsedRepackItem> {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(DescriptionExtractor.sanitizeXml(body)))
        }

        val items = mutableListOf<ParsedRepackItem>()
        var eventType = parser.eventType
        var currentTag: String? = null
        var insideItem = false

        var title = StringBuilder()
        var link = StringBuilder()
        var guid = StringBuilder()
        var pubDate = StringBuilder()
        var description = StringBuilder()
        var categoryBuffer = StringBuilder()
        val categories = mutableListOf<String>()

        // Wrapped defensively: some feeds (DODI's, at least) leak malformed markup that can trip
        // up even a sanitized parse. Rather than lose every item from a provider over one bad
        // item, this returns whatever was already parsed up to the failure point.
        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") {
                            insideItem = true
                            title = StringBuilder(); link = StringBuilder(); guid = StringBuilder()
                            pubDate = StringBuilder(); description = StringBuilder()
                            categories.clear()
                        }
                        if (currentTag == "category") categoryBuffer = StringBuilder()
                    }
                    XmlPullParser.TEXT -> if (insideItem) {
                        when (currentTag) {
                            "title" -> title.append(parser.text)
                            "link" -> link.append(parser.text)
                            "guid" -> guid.append(parser.text)
                            "pubDate" -> pubDate.append(parser.text)
                            "description", "content:encoded" -> description.append(parser.text)
                            "category" -> categoryBuffer.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "category" && insideItem) {
                            categoryBuffer.toString().trim().takeIf { it.isNotEmpty() }?.let(categories::add)
                        }
                        if (parser.name == "item" && insideItem) {
                            insideItem = false
                            buildItem(
                                title = title.toString().trim(),
                                link = link.toString().trim(),
                                guidRaw = guid.toString().trim(),
                                pubDateRaw = pubDate.toString().trim(),
                                descriptionHtml = description.toString().trim(),
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

    private fun buildItem(
        title: String,
        link: String,
        guidRaw: String,
        pubDateRaw: String,
        descriptionHtml: String,
        categories: List<String>,
    ): ParsedRepackItem? {
        if (title.isEmpty() || link.isEmpty()) return null
        val plainText = DescriptionExtractor.extractPlainText(descriptionHtml)
        val (originalSize, repackSize) = DescriptionExtractor.extractSizes(plainText)
        val genres = DescriptionExtractor.extractGenres(plainText).ifEmpty { categories }
        val slugSource = Uri.parse(link).lastPathSegment?.takeIf { it.isNotBlank() } ?: title
        return ParsedRepackItem(
            guid = guidRaw.ifEmpty { link },
            slug = DescriptionExtractor.slugify(slugSource),
            title = title,
            bannerUrl = DescriptionExtractor.extractBannerUrl(descriptionHtml, baseUri = link),
            originalUrl = link,
            originalSize = originalSize,
            repackSize = repackSize,
            genres = genres,
            description = plainText.take(4000),
            timestamp = parseRfc822(pubDateRaw) ?: System.currentTimeMillis(),
            isNsfw = DescriptionExtractor.isLikelyNsfw(title, categories),
        )
    }

    private fun parseRfc822(value: String): Long? {
        if (value.isEmpty()) return null
        return runCatching {
            ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.getOrNull()
    }
}
