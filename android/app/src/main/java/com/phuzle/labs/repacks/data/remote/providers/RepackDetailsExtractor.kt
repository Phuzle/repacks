package com.phuzle.labs.repacks.data.remote.providers

/** Turns a feed item's block-aware plain text (see [DescriptionExtractor.extractPlainText]) into
 * a [RepackDetails] plus a separate cleaned prose description. Built against real FitGirl and DODI
 * posts, but deliberately label-driven rather than hardcoded per provider — any "Label: value"
 * line falls into a known field when recognized, or a generic note otherwise, so a provider we
 * haven't specifically tuned for still gets a reasonable result instead of nothing. */
object RepackDetailsExtractor {

    private val NOISE_HEADING_REGEX = Regex("""(?i)^download (mirrors|links)\b""")
    private val SYSREQ_HEADING_REGEX = Regex("""(?i)^(minimum|recommended)?\s*system requirements$""")
    private val DESCRIPTION_HEADING_REGEX = Regex("""(?i)^description$""")
    private val INSTALL_HEADING_REGEX = Regex("""(?i)^how to install$|^installation( instructions)?$""")
    private val REPACK_FEATURES_HEADING_REGEX = Regex("""(?i)^repack features$""")
    private val INFORMATION_HEADING_REGEX = Regex("""(?i)^information$""")
    private val INSTALL_STEP_REGEX = Regex("""^\d+[-.)]\s*(.+)$""")
    private val LABEL_LINE_REGEX = Regex("""^([A-Za-z][A-Za-z0-9 /&+.'-]{1,40}):\s?(.*)$""")

    private val SYSREQ_FIELD_LABELS = setOf("os", "processor", "memory", "graphics", "directx", "direct x", "storage")
    private val SYSREQ_NOTE_LABELS = setOf("additional notes", "notes")

    private val IGNORED_LABELS = setOf(
        "title", "genre", "genres", "genres/tags", "genre/tags", "tags", "original size", "repack size",
    )
    private val MAX_NOTES = 12
    private val MAX_SECTION_LINES = 20

    /** Returns (details, descriptionProse). [rawLines] should already be block-aware (one logical
     * line per paragraph/list item/heading) and free of the "Download Mirrors" section onward. */
    fun extract(rawLines: List<String>): Pair<RepackDetails, String?> {
        val noiseIndex = rawLines.indexOfFirst { NOISE_HEADING_REGEX.containsMatchIn(it) }
        val lines = if (noiseIndex >= 0) rawLines.subList(0, noiseIndex) else rawLines

        var developer: String? = null
        var publisher: String? = null
        var franchise: String? = null
        var languages: String? = null
        var releaseDate: String? = null
        var systemRequirements: SystemRequirements? = null
        var installSteps: List<String> = emptyList()
        var descriptionProse: String? = null
        val notes = mutableListOf<LabeledValue>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                SYSREQ_HEADING_REGEX.matches(line) -> {
                    val (req, next) = consumeSystemRequirements(lines, i + 1)
                    systemRequirements = req
                    i = next
                }
                DESCRIPTION_HEADING_REGEX.matches(line) -> {
                    val (prose, next) = consumeUntilHeading(lines, i + 1)
                    descriptionProse = prose.joinToString(" ").trim().takeIf { it.isNotEmpty() }
                    i = next
                }
                INSTALL_HEADING_REGEX.matches(line) -> {
                    val (steps, next) = consumeInstallSteps(lines, i + 1)
                    installSteps = steps
                    i = next
                }
                REPACK_FEATURES_HEADING_REGEX.matches(line) || INFORMATION_HEADING_REGEX.matches(line) -> i++
                else -> {
                    val match = LABEL_LINE_REGEX.matchEntire(line)
                    if (match != null) {
                        val label = match.groupValues[1].trim()
                        val value = match.groupValues[2].trim()
                        val key = label.lowercase()
                        when {
                            value.isEmpty() || key in IGNORED_LABELS -> Unit
                            key == "developer" -> developer = value
                            key == "publisher" -> publisher = value
                            key == "companies" -> developer = value
                            key == "franchise" -> franchise = value
                            key == "languages" || key == "language" -> languages = value
                            key == "release date" -> releaseDate = value
                            notes.size < MAX_NOTES && value.length <= 300 -> notes += LabeledValue(label, value)
                        }
                    }
                    i++
                }
            }
        }

        val details = RepackDetails(
            developer = developer,
            publisher = publisher,
            franchise = franchise,
            languages = languages,
            releaseDate = releaseDate,
            systemRequirements = systemRequirements,
            installSteps = installSteps,
            notes = notes,
        )
        return details to descriptionProse
    }

    private fun isSectionBoundary(line: String): Boolean =
        NOISE_HEADING_REGEX.containsMatchIn(line) || SYSREQ_HEADING_REGEX.matches(line) ||
            DESCRIPTION_HEADING_REGEX.matches(line) || INSTALL_HEADING_REGEX.matches(line) ||
            REPACK_FEATURES_HEADING_REGEX.matches(line) || INFORMATION_HEADING_REGEX.matches(line)

    private fun consumeSystemRequirements(lines: List<String>, start: Int): Pair<SystemRequirements?, Int> {
        var os: String? = null
        var processor: String? = null
        var memory: String? = null
        var graphics: String? = null
        var directX: String? = null
        var storage: String? = null
        val notesLines = mutableListOf<String>()

        var i = start
        val end = minOf(lines.size, start + MAX_SECTION_LINES)
        while (i < end) {
            val line = lines[i]
            if (isSectionBoundary(line)) break
            val match = LABEL_LINE_REGEX.matchEntire(line)
            if (match != null) {
                val key = match.groupValues[1].trim().lowercase()
                val value = match.groupValues[2].trim()
                when {
                    key in SYSREQ_NOTE_LABELS -> notesLines += value
                    key == "os" -> os = value
                    key == "processor" -> processor = value
                    key == "memory" -> memory = value
                    key == "graphics" -> graphics = value
                    key == "directx" || key == "direct x" -> directX = value
                    key == "storage" -> storage = value
                    else -> break // an unrelated "Label: value" line means we've left this section
                }
                i++
            } else {
                // Preamble lines like "Requires a 64-bit processor and operating system".
                notesLines += line
                i++
            }
        }
        val req = SystemRequirements(os, processor, memory, graphics, directX, storage, notesLines.joinToString(" ").trim().ifEmpty { null })
        return (if (req.isEmpty) null else req) to i
    }

    private fun consumeInstallSteps(lines: List<String>, start: Int): Pair<List<String>, Int> {
        val steps = mutableListOf<String>()
        var i = start
        val end = minOf(lines.size, start + MAX_SECTION_LINES)
        while (i < end) {
            val match = INSTALL_STEP_REGEX.matchEntire(lines[i]) ?: break
            steps += match.groupValues[1].trim()
            i++
        }
        return steps to i
    }

    private fun consumeUntilHeading(lines: List<String>, start: Int): Pair<List<String>, Int> {
        val collected = mutableListOf<String>()
        var i = start
        val end = minOf(lines.size, start + MAX_SECTION_LINES)
        while (i < end && !isSectionBoundary(lines[i])) {
            collected += lines[i]
            i++
        }
        return collected to i
    }
}
