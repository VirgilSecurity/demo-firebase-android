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

package com.android.virgilsecurity.virgilonfire.data.model;
/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    10/2/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

import com.virgilsecurity.keyknox.cloud.CloudKeyStorage;
import com.virgilsecurity.keyknox.cloud.CloudKeyStorageProtocol;
import com.virgilsecurity.keyknox.storage.SyncKeyStorage;
import com.virgilsecurity.sdk.crypto.PrivateKey;
import com.virgilsecurity.sdk.crypto.PublicKey;
import com.virgilsecurity.sdk.jwt.contract.AccessTokenProvider;
import com.virgilsecurity.sdk.storage.DefaultKeyStorage;
import com.virgilsecurity.sdk.storage.KeyStorage;

import java.util.Collections;

/**
 * SyncKeyStorageWrapper
 */
public class SyncKeyStorageWrapper {

    private static final String KEYSTORE_NAME = "virgil.keystore";

    private static SyncKeyStorageWrapper instance;

    private AccessTokenProvider accessTokenProvider;
    private String identity;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String storagePath;

    public static SyncKeyStorageWrapper getInstance(AccessTokenProvider accessTokenProvider, String storagePath) {
        if (instance != null)
            return instance;

        return instance = new SyncKeyStorageWrapper(accessTokenProvider, storagePath);
    }

    private SyncKeyStorageWrapper(AccessTokenProvider accessTokenProvider, String storagePath) {
        this.accessTokenProvider = accessTokenProvider;
        this.storagePath = storagePath;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public SyncKeyStorage initSyncKeyStorage() {
        if (identity == null) throw new IllegalArgumentException("Identity should not be null. Set it first.");
        if (privateKey == null) throw new IllegalArgumentException("PrivateKey should not be null. Set it first.");
        if (publicKey == null) throw new IllegalArgumentException("PublicKey should not be null. Set it first.");

        CloudKeyStorageProtocol cloudKeyStorage = new CloudKeyStorage(accessTokenProvider,
                                                                      Collections.singletonList(publicKey),
                                                                      privateKey);
        KeyStorage keyStorage = new DefaultKeyStorage(storagePath, KEYSTORE_NAME);

        return new SyncKeyStorage(identity, keyStorage, cloudKeyStorage);
    }
}
