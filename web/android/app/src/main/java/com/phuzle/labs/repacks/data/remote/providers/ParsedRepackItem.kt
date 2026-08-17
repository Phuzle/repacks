package com.phuzle.labs.repacks.data.remote.providers

/** Feed-agnostic result of parsing one RSS `<item>` or Atom `<entry>` into repack metadata. */
data class ParsedRepackItem(
    val guid: String,
    val slug: String,
    val title: String,
    val bannerUrl: String?,
    val originalUrl: String,
    val originalSize: String?,
    val repackSize: String?,
    val genres: List<String>,
    val description: String?,
    val timestamp: Long,
    val isNsfw: Boolean,
)

fun interface FeedParser {
    fun parse(body: String): List<ParsedRepackItem>
}
