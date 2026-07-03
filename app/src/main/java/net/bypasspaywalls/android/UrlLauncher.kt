package net.bypasspaywalls.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Pulls the first http(s) URL out of arbitrary shared text (e.g. "Check this out: https://...\n"). */
private val URL_REGEX = Regex("""https?://\S+""")

fun extractUrl(text: String?): String? {
    if (text.isNullOrBlank()) return null
    val match = URL_REGEX.find(text)?.value
    return (match ?: text).trim().trimEnd('.', ',', ')', ']', '"', '\'')
}

/**
 * Opens [url] in a real browser, explicitly targeting an app other than this one so we don't
 * end up back in the share/view chooser that led here.
 */
fun openInBrowser(context: Context, url: String): Boolean {
    val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val ownPackage = context.packageName

    val candidates = context.packageManager
        .queryIntentActivities(viewIntent, 0)
        .map { it.activityInfo.packageName }
        .filter { it != ownPackage }

    if (candidates.isEmpty()) {
        Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_LONG).show()
        return false
    }

    val defaultBrowserPackage = context.packageManager
        .resolveActivity(viewIntent, 0)
        ?.activityInfo
        ?.packageName
        ?.takeIf { it != ownPackage && it in candidates }

    viewIntent.setPackage(defaultBrowserPackage ?: candidates.first())
    viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(viewIntent)
    return true
}
