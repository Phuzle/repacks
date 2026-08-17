package com.phuzle.labs.repacks.data.remote.providers

/** Parses free-text sizes like "35.1 GB" (as stored in [com.phuzle.labs.repacks.data.local.RepackEntity])
 * into gigabytes, for the Configure max-size filter and the detail screen's reduction % stat. */
object SizeUnits {
    private val SIZE_REGEX = Regex("""([0-9.]+)\s*([KMGT]B)""", RegexOption.IGNORE_CASE)

    fun parseToGb(sizeText: String?): Float? {
        if (sizeText.isNullOrBlank()) return null
        val match = SIZE_REGEX.find(sizeText) ?: return null
        val value = match.groupValues[1].toFloatOrNull() ?: return null
        return when (match.groupValues[2].uppercase()) {
            "KB" -> value / (1024f * 1024f)
            "MB" -> value / 1024f
            "GB" -> value
            "TB" -> value * 1024f
            else -> null
        }
    }
}
