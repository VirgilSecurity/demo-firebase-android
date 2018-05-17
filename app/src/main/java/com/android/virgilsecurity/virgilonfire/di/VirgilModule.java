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

package com.android.virgilsecurity.virgilonfire.di;

import android.content.Context;

import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.remote.ServiceHelper;
import com.android.virgilsecurity.virgilonfire.data.virgil.GetTokenCallbackImpl;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx;
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivityComponent;
import com.virgilsecurity.sdk.cards.ModelSigner;
import com.virgilsecurity.sdk.cards.validation.CardVerifier;
import com.virgilsecurity.sdk.cards.validation.VirgilCardVerifier;
import com.virgilsecurity.sdk.client.CardClient;
import com.virgilsecurity.sdk.crypto.CardCrypto;
import com.virgilsecurity.sdk.crypto.PrivateKeyExporter;
import com.virgilsecurity.sdk.crypto.VirgilCardCrypto;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKeyExporter;
import com.virgilsecurity.sdk.jwt.accessProviders.CallbackJwtProvider;
import com.virgilsecurity.sdk.jwt.contract.AccessTokenProvider;
import com.virgilsecurity.sdk.storage.JsonFileKeyStorage;
import com.virgilsecurity.sdk.storage.KeyStorage;
import com.virgilsecurity.sdk.storage.PrivateKeyStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Danylo Oliinyk on 3/23/18 at Virgil Security.
 * -__o
 */

@Module(subcomponents = ChatControlActivityComponent.class)
public class VirgilModule {

    @Provides @Singleton static VirgilCrypto provideVirgilCrypto() {
        return new VirgilCrypto();
    }

    @Provides @Singleton static CardClient provideCardClient() {
        return new CardClient();
    }

    @Provides @Singleton static ModelSigner provideModelSigner(CardCrypto cardCrypto) {
        return new ModelSigner(cardCrypto);
    }

    @Provides @Singleton static CardCrypto provideCardCrypto() {
        return new VirgilCardCrypto();
    }

    @Provides @Singleton static CallbackJwtProvider.GetTokenCallback provideGetTokenCallback(
            ServiceHelper serviceHelper,
            UserManager userManager) {

        return new GetTokenCallbackImpl(serviceHelper, userManager);
    }

    @Provides @Singleton static AccessTokenProvider provideAccessTokenProvider(
            CallbackJwtProvider.GetTokenCallback getTokenCallback) {

        return new CallbackJwtProvider(getTokenCallback);
    }

    @Provides @Singleton static PrivateKeyExporter providePrivateKeyExporter() {
        return new VirgilPrivateKeyExporter();
    }

    @Provides @Singleton static KeyStorage provideKeyStorage(Context context) {
        return new JsonFileKeyStorage(context.getFilesDir().getAbsolutePath());
    }

    @Provides @Singleton static PrivateKeyStorage providePrivateKeyStorage(
            PrivateKeyExporter privateKeyExporter,
            KeyStorage keyStorage) {

        return new PrivateKeyStorage(privateKeyExporter, keyStorage);
    }

    @Provides CardVerifier provideCardVerifier(CardCrypto cardCrypto) {
        return new VirgilCardVerifier(cardCrypto);
    }

    @Provides VirgilHelper provideVirgilHelper(CardClient cardClient,
                                               ModelSigner modelSigner,
                                               CardCrypto cardCrypto,
                                               AccessTokenProvider tokenProvider,
                                               CardVerifier cardVerifier,
                                               PrivateKeyStorage privateKeyStorage,
                                               UserManager userManager) {
        return new VirgilHelper(() -> cardClient,
                                () -> modelSigner,
                                () -> cardCrypto,
                                () -> tokenProvider,
                                () -> cardVerifier,
                                () -> privateKeyStorage,
                                () -> userManager);
    }

    @Provides @Singleton static VirgilRx provideVirgilRx(VirgilHelper virgilHelper) {
        return new VirgilRx(virgilHelper);
    }
}
