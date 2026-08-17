package com.phuzle.labs.repacks.data.remote.providers

import org.json.JSONArray
import org.json.JSONObject

/** Provider-agnostic "middle ground" schema that every source's release-post HTML gets flattened
 * into (see [RepackDetailsExtractor]). Known fields (developer, system requirements, ...) get
 * proper structure; anything a site includes that doesn't map to a known field — "Based on Steam
 * Release Build...", "Lossless repack", install time, greetings, and the like — still survives as
 * a generic label/value pair in [notes] instead of being silently dropped. Deliberately excludes
 * download mirrors/filehoster links and part-file lists: the app's own disclaimer (About screen)
 * says it doesn't index or distribute direct download links, so those sections are stripped before
 * this even runs, not just left out of the schema. */
data class RepackDetails(
    val developer: String? = null,
    val publisher: String? = null,
    val franchise: String? = null,
    val languages: String? = null,
    val releaseDate: String? = null,
    val systemRequirements: SystemRequirements? = null,
    val installSteps: List<String> = emptyList(),
    val notes: List<LabeledValue> = emptyList(),
) {
    val isEmpty: Boolean
        get() = developer == null && publisher == null && franchise == null && languages == null &&
            releaseDate == null && systemRequirements == null && installSteps.isEmpty() && notes.isEmpty()

    fun toJson(): String = JSONObject().apply {
        developer?.let { put("developer", it) }
        publisher?.let { put("publisher", it) }
        franchise?.let { put("franchise", it) }
        languages?.let { put("languages", it) }
        releaseDate?.let { put("releaseDate", it) }
        systemRequirements?.let { put("systemRequirements", it.toJsonObject()) }
        if (installSteps.isNotEmpty()) put("installSteps", JSONArray(installSteps))
        if (notes.isNotEmpty()) {
            put("notes", JSONArray(notes.map { JSONObject().apply { put("label", it.label); put("value", it.value) } }))
        }
    }.toString()
}

data class SystemRequirements(
    val os: String? = null,
    val processor: String? = null,
    val memory: String? = null,
    val graphics: String? = null,
    val directX: String? = null,
    val storage: String? = null,
    val notes: String? = null,
) {
    val isEmpty: Boolean
        get() = os == null && processor == null && memory == null && graphics == null &&
            directX == null && storage == null && notes == null

    fun toJsonObject(): JSONObject = JSONObject().apply {
        os?.let { put("os", it) }
        processor?.let { put("processor", it) }
        memory?.let { put("memory", it) }
        graphics?.let { put("graphics", it) }
        directX?.let { put("directX", it) }
        storage?.let { put("storage", it) }
        notes?.let { put("notes", it) }
    }
}

data class LabeledValue(val label: String, val value: String)
