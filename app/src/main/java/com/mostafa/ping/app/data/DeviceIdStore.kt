package com.mostafa.ping.app.data

import android.content.Context
import java.util.UUID

/** Stable per-install IDs stored on device (no Firebase Auth). */
object DeviceIdStore {
    private const val PREFS = "ping_device"
    private const val KEY_UID = "device_uid"
    private const val KEY_CODE = "ping_code"

    fun getOrCreateUid(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_UID, null)
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_UID, created).apply()
        return created
    }

    fun getSavedCode(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CODE, null)
            ?.takeIf { PairCode.isValid(it) }

    fun saveCode(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CODE, code)
            .apply()
    }
}
