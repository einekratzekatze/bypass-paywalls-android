package net.bypasspaywalls.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

/**
 * Transparent, history-less activity. Receives a shared link (Share sheet) or a directly
 * viewed http(s) link (via "Open with"), rewrites it through the currently selected bypass
 * service, forwards it to a real browser, and immediately finishes.
 */
class RedirectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val originalUrl = extractUrl(urlFromIntent(intent))
        if (originalUrl == null) {
            Toast.makeText(this, R.string.error_no_url_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val service = SettingsRepository(this).getSelectedService()
        val redirectUrl = service.buildRedirectUrl(originalUrl)
        openInBrowser(this, redirectUrl)
        finish()
    }

    private fun urlFromIntent(intent: Intent?): String? = when (intent?.action) {
        Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
        Intent.ACTION_VIEW -> intent.dataString
        else -> null
    }
}
