/*
 * Copyright (c) 2015-2018, Virgil Security, Inc.
 *
 * Lead Maintainer: Virgil Security Inc. <support@virgilsecurity.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     (1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *     (2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     (3) Neither the name of virgil nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.virgilsecurity.virgilonfire.data.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.StringDef

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    12/17/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

/**
 * PropertyManager class.
 */
open class PropertyManager(context: Context) {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SupportedTypes.STRING,
               SupportedTypes.BOOLEAN,
               SupportedTypes.INTEGER,
               SupportedTypes.FLOAT)
    annotation class SupportedTypes {
        companion object {
            const val STRING = "STRING"
            const val BOOLEAN = "BOOLEAN"
            const val INTEGER = "INTEGER"
            const val FLOAT = "FLOAT"
        }
    }

    private val preferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    private fun edit(performer: (SharedPreferences.Editor) -> Unit) {
        val editor = preferences.edit()
        performer(editor)
        editor.apply()
    }

    internal fun <T> setValue(key: String, value: T) =
            when (value) {
                is String -> edit { editor -> editor.putString(key, value as String) }
                is Boolean -> edit { editor -> editor.putBoolean(key, value as Boolean) }
                is Int -> edit { editor -> editor.putInt(key, value as Int) }
                is Float -> edit { editor -> editor.putFloat(key, value as Float) }
                else -> throw UnsupportedOperationException("Not yet implemented.")
            }

    /**
     * Returns value from shared preferences with provided [key]. Choose one of [SupportedTypes]
     * (for example [SupportedTypes.BOOLEAN]). Default value will be used if no value is stored.
     */
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    internal fun <T> getValue(key: String,
                              @PropertyManager.SupportedTypes type:
                              String, defaultValue: T?): T = when (type) {
        SupportedTypes.STRING -> preferences.getString(key, defaultValue as String)
        SupportedTypes.BOOLEAN -> preferences.getBoolean(key, defaultValue as Boolean)
        SupportedTypes.INTEGER -> preferences.getInt(key, defaultValue as Int)
        SupportedTypes.FLOAT -> preferences.getFloat(key, defaultValue as Float)
        else -> throw UnsupportedOperationException("Not implemented yet.")
    } as T

    internal fun clearValue(key: String) = edit { editor -> editor.remove(key) }
}
