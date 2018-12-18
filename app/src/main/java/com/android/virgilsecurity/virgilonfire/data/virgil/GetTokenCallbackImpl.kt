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

package com.android.virgilsecurity.virgilonfire.data.virgil

import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.data.model.ServiceException
import com.android.virgilsecurity.virgilonfire.data.model.Token
import com.android.virgilsecurity.virgilonfire.data.remote.ServiceHelper
import com.android.virgilsecurity.virgilonfire.util.UserUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.virgilsecurity.sdk.jwt.TokenContext
import com.virgilsecurity.sdk.jwt.accessProviders.CallbackJwtProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
 * GetTokenCallbackImpl class.
 */
class GetTokenCallbackImpl(
        private val helper: ServiceHelper,
        private val userManager: UserManager,
        private val firebaseAuth: FirebaseAuth
) : CallbackJwtProvider.GetTokenCallback {

    override fun onGetToken(tokenContext: TokenContext): String {
        try {
            var response = helper.getToken(
                userManager.token,
                UserUtils.currentUsername(firebaseAuth)
            ).execute()

            if (response.errorBody() != null && response.code() == 401) {
                runBlocking {
                    GlobalScope.async {
                        firebaseAuth.currentUser!!
                                .getIdToken(true)
                                .result
                    }.await().run {
                        userManager.setToken(Token(this!!.token!!))
                    }
                }

                response = helper.getToken(
                    userManager.token,
                    UserUtils.currentUsername(firebaseAuth)
                ).execute()
            }

            return response.body()!!.token
        } catch (t: Throwable) {
            t.printStackTrace()
            throw ServiceException("Failed on get token")
        }
    }
}
