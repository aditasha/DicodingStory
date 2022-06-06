package com.aditasha.myapplication.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aditasha.myapplication.api.LoginResult

internal class UserPreference(context: Context) {
    private val mainKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS,
        mainKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setCred(user: LoginResult) {
        sharedPreferences.edit().apply {
            putString(NAME, user.name)
            putString(USERID, user.userId)
            putString(TOKEN, user.token)
            apply()
        }
    }

    fun getCred(): LoginResult {
        return LoginResult(
            sharedPreferences.getString(NAME, ""),
            sharedPreferences.getString(USERID, ""),
            sharedPreferences.getString(TOKEN, "")
        )
    }

    fun wipeCred() {
        sharedPreferences.edit().apply {
            putString(NAME, "")
            putString(USERID, "")
            putString(TOKEN, "")
            apply()
        }
    }

    companion object {
        private const val PREFS = "userCredential"
        private const val NAME = "name"
        private const val USERID = "userId"
        private const val TOKEN = "token"
    }
}