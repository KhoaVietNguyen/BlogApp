package com.example.blogapp

import android.content.Context
import android.content.SharedPreferences
import com.example.blogapp.Model.User
import com.google.gson.Gson

//KEY WORD
const val USER_PROFILE = "USER"

class PrefUtil constructor(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val gSon: Gson
) {

    fun clearAllData() = prefs.edit().clear().commit()

    var user: User?
        get() {
            return try {
                gSon.fromJson(
                    prefs.getString(USER_PROFILE, null),
                    User::class.java
                )
            } catch (e: Exception) {
                null
            }
        }
        set(value) = prefs.edit().putString(
            USER_PROFILE,
            gSon.toJson(value)
        ).apply()
}