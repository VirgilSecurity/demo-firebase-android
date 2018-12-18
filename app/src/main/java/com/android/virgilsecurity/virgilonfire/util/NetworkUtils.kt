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

package com.android.virgilsecurity.virgilonfire.util

import com.virgilsecurity.sdk.client.exceptions.VirgilCardIsNotFoundException
import com.virgilsecurity.sdk.client.exceptions.VirgilKeyIsAlreadyExistsException
import com.virgilsecurity.sdk.client.exceptions.VirgilKeyIsNotFoundException
import com.virgilsecurity.sdk.crypto.exceptions.KeyEntryNotFoundException

import retrofit2.HttpException

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
 * NetworkUtils class.
 */
object NetworkUtils {

    fun resolveError(t: Throwable): String {
        return when (t) {
            is HttpException -> when (t.code()) {
                Const.Http.BAD_REQUEST -> "Bad Request"
                Const.Http.UNAUTHORIZED -> "Unauthorized"
                Const.Http.FORBIDDEN -> "Forbidden"
                Const.Http.NOT_ACCEPTABLE -> "Not acceptable"
                Const.Http.UNPROCESSABLE_ENTITY -> "Unprocessable entity"
                Const.Http.SERVER_ERROR -> "Server error"
                else -> "Oops.. Something went wrong ):"
            }
            is VirgilKeyIsNotFoundException ->
                "Username is not registered yet"
            is VirgilKeyIsAlreadyExistsException ->
                "Username is already registered. Please, try another one."
            is KeyEntryNotFoundException ->
                "Username is not found on this device. Maybe you deleted your private key"
            is VirgilCardIsNotFoundException ->
                "Virgil Card is not found.\nYou can not start chat with user without Virgil Card."
            else -> "Something went wrong"
        }
    }
}
