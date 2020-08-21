package com.example.blogapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.koin.dsl.module

const val PREFS_FILENAME = "com.example.sharedpreferencesdemo"

val localModule = module {
    single { Gson() }
    single { provideSharedPreference(get()) }
    single { providePreferenceHelper(get(), get(), get()) }
}

fun providePreferenceHelper(context: Context, sharedPreferences: SharedPreferences, gSon: Gson) =
    PrefUtil(context, sharedPreferences, gSon)

fun provideSharedPreference(context: Context): SharedPreferences =
    context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)