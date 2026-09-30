package com.example.security

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.CancellationSignal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom

object AppLockManager {

    private const val PREFS_NAME = "salim_app_lock_prefs"
    private const val KEY_LOCK_ENABLED = "key_lock_enabled"
    private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    private const val KEY_PIN_HASH = "key_pin_hash"
    private const val KEY_PIN_SALT = "key_pin_salt"
    private const val KEY_TIMEOUT_MINUTES = "key_timeout_minutes"
    private const val KEY_LAST_UNLOCK_TIME = "key_last_unlock_time"

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun isAppLockEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOCK_ENABLED, false) && prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun getTimeoutMinutes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TIMEOUT_MINUTES, 0) // 0 = Immediately
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.packageManager
            pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        } else false
    }

    fun setPin(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, salt)
            .putBoolean(KEY_LOCK_ENABLED, true)
            .apply()
        _isLocked.value = false
    }

    fun disableAppLock(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_LOCK_ENABLED, false)
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .apply()
        _isLocked.value = false
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setTimeoutMinutes(context: Context, minutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_TIMEOUT_MINUTES, minutes).apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false

        val testHash = hashPin(pin, storedSalt)
        val matched = testHash == storedHash
        if (matched) {
            recordUnlock(context)
        }
        return matched
    }

    fun recordUnlock(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_UNLOCK_TIME, System.currentTimeMillis()).apply()
        _isLocked.value = false
    }

    fun onAppResume(context: Context) {
        if (!isAppLockEnabled(context)) {
            _isLocked.value = false
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastUnlock = prefs.getLong(KEY_LAST_UNLOCK_TIME, 0L)
        val timeoutMins = prefs.getInt(KEY_TIMEOUT_MINUTES, 0)
        val timeoutMillis = timeoutMins * 60 * 1000L

        val elapsed = System.currentTimeMillis() - lastUnlock
        if (elapsed > timeoutMillis) {
            _isLocked.value = true
        }
    }

    fun authenticateWithBiometrics(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val executor = activity.mainExecutor
                val prompt = android.hardware.biometrics.BiometricPrompt.Builder(activity)
                    .setTitle("Unlock Salim")
                    .setSubtitle("Use your fingerprint or face to open")
                    .setNegativeButton("Use PIN", executor) { _, _ -> onError("Cancelled") }
                    .build()

                prompt.authenticate(
                    CancellationSignal(),
                    executor,
                    object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            recordUnlock(activity)
                            onSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            onError(errString?.toString() ?: "Biometric error")
                        }
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Biometrics unavailable")
            }
        } else {
            onError("Biometrics require Android 9 or higher")
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$pin".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
