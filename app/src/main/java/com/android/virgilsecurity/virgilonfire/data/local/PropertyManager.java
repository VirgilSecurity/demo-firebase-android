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

package com.android.virgilsecurity.virgilonfire.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;

/**
 * Created by Danylo Oliinyk on 3/23/18 at Virgil Security.
 * -__o
 */

public class PropertyManager {

    @StringDef({
                       SupportedTypes.STRING,
                       SupportedTypes.BOOLEAN,
                       SupportedTypes.INTEGER,
                       SupportedTypes.FLOAT
               })
    public @interface SupportedTypes {
        String STRING = "STRING";
        String BOOLEAN = "BOOLEAN";
        String INTEGER = "INTEGER";
        String FLOAT = "FLOAT";
    }

    private static SharedPreferences preferences;

    public PropertyManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void edit(Performer<SharedPreferences.Editor> performer) {
        SharedPreferences.Editor editor = preferences.edit();
        performer.performOperation(editor);
        editor.apply();
    }

    <T> void setValue(String key, T value) {

        if (value instanceof String) {
            edit(editor -> editor.putString(key, (String) value));
        } else if (value instanceof Boolean) {
            edit(editor -> editor.putBoolean(key, (Boolean) value));
        } else if (value instanceof Integer) {
            edit(editor -> editor.putInt(key, (Integer) value));
        } else if (value instanceof Float) {
            edit(editor -> editor.putFloat(key, (Float) value));
        } else {
            throw new UnsupportedOperationException("Not yet implemented.");
        }
    }

    <T> T getValue(String key, @SupportedTypes String type, T defaultValue) {

        Object value;
        if (type.equals(SupportedTypes.STRING)) {
            value = preferences.getString(key, (String) defaultValue);
        } else if (type.equals(SupportedTypes.BOOLEAN)) {
            value = preferences.getBoolean(key, (Boolean) defaultValue);
        } else if (type.equals(SupportedTypes.INTEGER)) {
            value = preferences.getInt(key, (Integer) defaultValue);
        } else if (type.equals(SupportedTypes.FLOAT)) {
            value = preferences.getFloat(key, (Float) defaultValue);
        } else {
            throw new UnsupportedOperationException("Not yet implemented.");
        }
        return (T) value;
    }

    void clearValue(String key) {
        edit(editor -> editor.remove(key));
    }

    public interface Performer<T> {
        void performOperation(T victim);
    }
}
