package net.bypasspaywalls.android

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * A rewrite rule that turns an original article URL into a bypass URL.
 *
 * [urlTemplate] must contain the token `{url}` marking where the original URL is inserted.
 * [encodeUrl] controls whether the original URL is percent-encoded before substitution:
 * services that take the URL as a query parameter need it encoded, services that expect
 * a raw URL appended to the path need it left alone.
 */
data class BypassService(
    val id: String,
    val name: String,
    val urlTemplate: String,
    val encodeUrl: Boolean,
    val isCustom: Boolean = false,
) {
    fun buildRedirectUrl(originalUrl: String): String {
        val inserted = if (encodeUrl) URLEncoder.encode(originalUrl, "UTF-8") else originalUrl
        return urlTemplate.replace(URL_TOKEN, inserted)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("urlTemplate", urlTemplate)
        put("encodeUrl", encodeUrl)
        put("isCustom", isCustom)
    }

    companion object {
        const val URL_TOKEN = "{url}"

        fun fromJson(json: JSONObject): BypassService = BypassService(
            id = json.getString("id"),
            name = json.getString("name"),
            urlTemplate = json.getString("urlTemplate"),
            encodeUrl = json.optBoolean("encodeUrl", true),
            isCustom = json.optBoolean("isCustom", true),
        )

        /**
         * Built-in, well-known paywall bypass / archive services, ordered by how well
         * they worked when last verified (July 2026). removepaywall.com is the default
         * (confirmed working end-to-end). 12ft.io and 1ft.io were removed (both shut
         * down), and Freedium moved off the dead freedium.cfd domain.
         */
        val DEFAULTS: List<BypassService> = listOf(
            BypassService(
                id = "removepaywall",
                name = "removepaywall.com",
                urlTemplate = "https://removepaywall.com/{url}",
                encodeUrl = false,
            ),
            BypassService(
                id = "smry",
                name = "smry.ai",
                urlTemplate = "https://smry.ai/{url}",
                encodeUrl = false,
            ),
            BypassService(
                id = "wayback",
                name = "Wayback Machine (Internet Archive)",
                urlTemplate = "https://web.archive.org/web/2999/{url}",
                encodeUrl = false,
            ),
            BypassService(
                id = "freedium",
                name = "Freedium (Medium articles)",
                urlTemplate = "https://freedium-mirror.cfd/{url}",
                encodeUrl = false,
            ),
            BypassService(
                id = "archive_ph",
                name = "archive.today (archive.ph)",
                urlTemplate = "https://archive.ph/newest/{url}",
                encodeUrl = false,
            ),
        )
    }
}

fun List<BypassService>.toJsonArray(): String {
    val array = JSONArray()
    forEach { array.put(it.toJson()) }
    return array.toString()
}

fun parseBypassServiceList(json: String?): List<BypassService> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { BypassService.fromJson(array.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }
}
