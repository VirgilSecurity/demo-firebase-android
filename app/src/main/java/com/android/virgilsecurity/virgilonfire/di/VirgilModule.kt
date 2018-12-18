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

package com.android.virgilsecurity.virgilonfire.di

import android.content.Context

import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.data.remote.ServiceHelper
import com.android.virgilsecurity.virgilonfire.data.virgil.GetTokenCallbackImpl
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivityComponent
import com.google.firebase.auth.FirebaseAuth
import com.virgilsecurity.sdk.cards.ModelSigner
import com.virgilsecurity.sdk.cards.validation.CardVerifier
import com.virgilsecurity.sdk.cards.validation.VirgilCardVerifier
import com.virgilsecurity.sdk.client.CardClient
import com.virgilsecurity.sdk.client.VirgilCardClient
import com.virgilsecurity.sdk.crypto.CardCrypto
import com.virgilsecurity.sdk.crypto.PrivateKeyExporter
import com.virgilsecurity.sdk.crypto.VirgilCardCrypto
import com.virgilsecurity.sdk.crypto.VirgilCrypto
import com.virgilsecurity.sdk.crypto.VirgilPrivateKeyExporter
import com.virgilsecurity.sdk.jwt.accessProviders.CallbackJwtProvider
import com.virgilsecurity.sdk.jwt.contract.AccessTokenProvider
import com.virgilsecurity.sdk.storage.JsonFileKeyStorage
import com.virgilsecurity.sdk.storage.KeyStorage
import com.virgilsecurity.sdk.storage.PrivateKeyStorage

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

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
 * Virgil module class
 */
@Module(subcomponents = [ChatControlActivityComponent::class])
class VirgilModule {

    @Provides fun provideCardVerifier(cardCrypto: CardCrypto): CardVerifier {
        return VirgilCardVerifier(cardCrypto)
    }

    @Provides fun provideVirgilHelper(cardClient: CardClient,
                                               modelSigner: ModelSigner,
                                               cardCrypto: CardCrypto,
                                               tokenProvider: AccessTokenProvider,
                                               cardVerifier: CardVerifier,
                                               privateKeyStorage: PrivateKeyStorage,
                                               firebaseAuth: FirebaseAuth): VirgilHelper {
        return VirgilHelper(cardClient,
                            modelSigner,
                            cardCrypto,
                            tokenProvider,
                            cardVerifier,
                            privateKeyStorage,
                            firebaseAuth)
    }

    companion object {

        @Provides @Singleton fun provideVirgilCrypto(): VirgilCrypto {
            return VirgilCrypto()
        }

        @Provides @Singleton fun provideCardClient(): CardClient {
            return VirgilCardClient()
        }

        @Provides @Singleton fun provideModelSigner(cardCrypto: CardCrypto): ModelSigner {
            return ModelSigner(cardCrypto)
        }

        @Provides @Singleton fun provideCardCrypto(): CardCrypto {
            return VirgilCardCrypto()
        }

        @Provides @Singleton fun provideGetTokenCallback(
                serviceHelper: ServiceHelper,
                userManager: UserManager,
                firebaseAuth: FirebaseAuth): CallbackJwtProvider.GetTokenCallback {

            return GetTokenCallbackImpl(serviceHelper, userManager, firebaseAuth)
        }

        @Provides @Singleton fun provideAccessTokenProvider(
                getTokenCallback: CallbackJwtProvider.GetTokenCallback): AccessTokenProvider {

            return CallbackJwtProvider(getTokenCallback)
        }

        @Provides @Singleton fun providePrivateKeyExporter(): PrivateKeyExporter {
            return VirgilPrivateKeyExporter()
        }

        @Provides @Singleton fun provideKeyStorage(context: Context): KeyStorage {
            return JsonFileKeyStorage(context.filesDir.absolutePath)
        }

        @Provides @Singleton fun providePrivateKeyStorage(
                privateKeyExporter: PrivateKeyExporter,
                keyStorage: KeyStorage): PrivateKeyStorage {

            return PrivateKeyStorage(privateKeyExporter, keyStorage)
        }

        @Provides @Singleton fun provideVirgilRx(virgilHelper: VirgilHelper): VirgilRx {
            return VirgilRx(virgilHelper)
        }
    }
}
