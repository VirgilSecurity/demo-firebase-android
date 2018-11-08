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
 * Created by Danylo Oliinyk on 3/23/18 at Virgil Security.
 * -__o
 */

open class PropertyManager(context: Context) {

    @StringDef(SupportedTypes.STRING,
               SupportedTypes.BOOLEAN,
               SupportedTypes.INTEGER,
               SupportedTypes.FLOAT)
    annotation class SupportedTypes {
        companion object {
            val STRING = "STRING"
            val BOOLEAN = "BOOLEAN"
            val INTEGER = "INTEGER"
            val FLOAT = "FLOAT"
        }
    }

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private fun edit(performer: Performer<SharedPreferences.Editor>) {
        val editor = preferences.edit()
        performer.performOperation(editor)
        editor.apply()
    }

    internal fun <T> setValue(key: String, value: T) {

        if (value is String) {
            edit({ editor -> editor.putString(key, value as String) })
        } else if (value is Boolean) {
            edit({ editor -> editor.putBoolean(key, value as Boolean) })
        } else if (value is Int) {
            edit({ editor -> editor.putInt(key, value as Int) })
        } else if (value is Float) {
            edit({ editor -> editor.putFloat(key, value as Float) })
        } else {
            throw UnsupportedOperationException("Not yet implemented.")
        }
    }

    internal fun <T> getValue(key: String, @SupportedTypes type: String, defaultValue: T): T {

        val value: Any?
        if (type == SupportedTypes.STRING) {
            value = preferences.getString(key, defaultValue as String)
        } else if (type == SupportedTypes.BOOLEAN) {
            value = preferences.getBoolean(key, defaultValue as Boolean)
        } else if (type == SupportedTypes.INTEGER) {
            value = preferences.getInt(key, defaultValue as Int)
        } else if (type == SupportedTypes.FLOAT) {
            value = preferences.getFloat(key, defaultValue as Float)
        } else {
            throw UnsupportedOperationException("Not yet implemented.")
        }
        return value as T?
    }

    internal fun clearValue(key: String) {
        edit({ editor -> editor.remove(key) })
    }

    interface Performer<T> {
        fun performOperation(victim: T)
    }

    companion object {

        private var preferences: SharedPreferences
    }
}
