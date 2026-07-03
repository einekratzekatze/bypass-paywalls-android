package net.bypasspaywalls.android

import android.content.Context
import android.content.SharedPreferences

/** Persists the user's custom bypass services and which service is currently selected. */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCustomServices(): List<BypassService> =
        parseBypassServiceList(prefs.getString(KEY_CUSTOM_SERVICES, null))

    fun getAllServices(): List<BypassService> = BypassService.DEFAULTS + getCustomServices()

    fun addCustomService(service: BypassService) {
        val updated = getCustomServices() + service
        prefs.edit().putString(KEY_CUSTOM_SERVICES, updated.toJsonArray()).apply()
    }

    fun removeCustomService(id: String) {
        val updated = getCustomServices().filterNot { it.id == id }
        prefs.edit().putString(KEY_CUSTOM_SERVICES, updated.toJsonArray()).apply()
        if (getSelectedServiceId() == id) {
            setSelectedServiceId(BypassService.DEFAULTS.first().id)
        }
    }

    fun getSelectedServiceId(): String =
        prefs.getString(KEY_SELECTED_SERVICE, null) ?: BypassService.DEFAULTS.first().id

    fun setSelectedServiceId(id: String) {
        prefs.edit().putString(KEY_SELECTED_SERVICE, id).apply()
    }

    fun getSelectedService(): BypassService =
        getAllServices().find { it.id == getSelectedServiceId() } ?: BypassService.DEFAULTS.first()

    companion object {
        private const val PREFS_NAME = "skip_the_paywall_prefs"
        private const val KEY_CUSTOM_SERVICES = "custom_services"
        private const val KEY_SELECTED_SERVICE = "selected_service_id"
    }
}
