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
import com.android.virgilsecurity.virgilonfire.data.model.Token
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.virgilsecurity.sdk.cards.Card
import com.virgilsecurity.sdk.cards.model.RawSignedModel
import com.virgilsecurity.sdk.crypto.VirgilCardCrypto
import java.util.*

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
 * UserManager class.
 */
class UserManager(context: Context) : PropertyManager(context) {

    var userCards: List<Card>
        get() = getValue<String>(USER_CARDS,
                                 PropertyManager.SupportedTypes.STRING,
                                 null).let { serialized ->
            (deserializeFromJson<List<RawSignedModel>>(serialized) to VirgilCardCrypto())
                    .let { pair ->
                        pair.first.map { cardModel -> Card.parse(pair.second, cardModel) }
                    }
        }
        set(cards) {
            val rawSignedModels = ArrayList<RawSignedModel>()
            for (card in cards)
                rawSignedModels.add(card.rawCard)

            val serialized = Gson().toJson(rawSignedModels)

            setValue(USER_CARDS, serialized)
        }

    val token: Token
        get() = Gson().fromJson(
            getValue<Any>(TOKEN,
                          PropertyManager.SupportedTypes.STRING, null) as String,
            Token::class.java
        )

    fun clearUserCard() {
        clearValue(USER_CARDS)
    }


    fun setToken(token: Token) {
        setValue(TOKEN, Gson().toJson(token))
    }

    fun clearToken() {
        clearValue(TOKEN)
    }

    private fun <T> deserializeFromJson(serialized: String): T =
            Gson().fromJson<T>(serialized, object : TypeToken<T>() {}.type)

    companion object {

        private const val USER_CARDS = "USER_CARDS"
        private const val TOKEN = "TOKEN"
    }
}