package com.wawi.api.delegate

import android.content.Context
import android.content.SharedPreferences
import com.wawi.api.CommonModule
import kotlin.reflect.KProperty

class Preference<T>(val name: String, private val default: T) {
    private val prefs: SharedPreferences by lazy { CommonModule.getContext().applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE) }
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getSharedPreference(name, default)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putSharedPreference(name, value)
    }

    private fun putSharedPreference(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Boolean -> putBoolean(name, value)
            is Int -> putInt(name, value)
            is Float -> putFloat(name, value)
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            else -> throw IllegalArgumentException("SharedPreferences can't put $value")
        }.apply() // 链式调用
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSharedPreference(name: String, default: T): T = with(prefs) {
        val res: Any = when(default) {
            is Boolean -> getBoolean(name, default)
            is Int -> getInt(name, default)
            is Float -> getFloat(name, default)
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            else -> throw IllegalArgumentException("SharedPreferences can't get this type")
        }
        return@with res as T
    }
}