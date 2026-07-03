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

        /** Built-in, well-known paywall bypass / archive services. */
        val DEFAULTS: List<BypassService> = listOf(
            BypassService(
                id = "12ft",
                name = "12ft.io",
                urlTemplate = "https://12ft.io/proxy?q={url}",
                encodeUrl = true,
            ),
            BypassService(
                id = "freedium",
                name = "Freedium (Medium articles)",
                urlTemplate = "https://freedium.cfd/{url}",
                encodeUrl = false,
            ),
            BypassService(
                id = "removepaywall",
                name = "removepaywall.com",
                urlTemplate = "https://removepaywall.com/{url}",
                encodeUrl = false,
            ),
            BypassService(
                id = "archive_ph",
                name = "archive.ph (latest snapshot)",
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
