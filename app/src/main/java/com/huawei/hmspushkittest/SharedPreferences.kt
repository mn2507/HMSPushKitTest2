package com.huawei.hmspushkittest
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


class LocalStorage {

    private val sInstance: LocalStorage = LocalStorage()
    private var mSharedPreferences: SharedPreferences? = null
    private val PREF_LOGGED_IN = "logged_in"

    public val SHARED = "shared"

}