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

package com.android.virgilsecurity.virgilonfire.ui.chat;
/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    10/3/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

import com.android.virgilsecurity.virgilonfire.data.model.SyncKeyStorageWrapper;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.virgilsecurity.keyknox.storage.SyncKeyStorage;
import com.virgilsecurity.pythia.brainkey.BrainKey;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.storage.PrivateKeyStorage;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ChatControlActivityPresenter
 */
public class ChatControlActivityPresenter implements BasePresenter {

    private CompositeDisposable compositeDisposable;
    private final BrainKey brainKey;
    private final SyncKeyStorageWrapper syncKeyStorageWrapper;
    private final PrivateKeyStorage privateKeyStorage;
    private final ResetAccountInteractor resetAccountInteractor;

    @Inject
    public ChatControlActivityPresenter(BrainKey brainKey,
                                        SyncKeyStorageWrapper syncKeyStorageWrapper,
                                        PrivateKeyStorage privateKeyStorage,
                                        ResetAccountInteractor resetAccountInteractor) {
        this.brainKey = brainKey;
        this.syncKeyStorageWrapper = syncKeyStorageWrapper;
        this.privateKeyStorage = privateKeyStorage;
        this.resetAccountInteractor = resetAccountInteractor;

        compositeDisposable = new CompositeDisposable();
    }

    void requestResetAccount(String identity, String password) {
        Disposable resetAccountDisposable =
                generateBrainKey(password)
                        .subscribeOn(Schedulers.io())
                        .flatMap(keyPair -> {
                            syncKeyStorageWrapper.setIdentity(identity);
                            syncKeyStorageWrapper.setPrivateKey(keyPair.getPrivateKey());
                            syncKeyStorageWrapper.setPublicKey(keyPair.getPublicKey());
                            SyncKeyStorage syncKeyStorage = syncKeyStorageWrapper.initSyncKeyStorage();

                            return syncKeyknox(syncKeyStorage);
                        })
                        .flatMap(syncKeyStorage -> {
                            return deleteKeyknoxEntry(identity + VirgilHelper.KEYKNOX_POSTFIX,
                                                      syncKeyStorage).subscribeOn(Schedulers.io())
                                                                     .toSingleDefault(new Object());
                        })
                        .map(object -> {
                            privateKeyStorage.delete(identity);
                            return object;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ignored -> resetAccountInteractor.onResetAccountSuccess(),
                                   resetAccountInteractor::onResetAccountError);

        compositeDisposable.add(resetAccountDisposable);
    }

    private Single<VirgilKeyPair> generateBrainKey(String password) {
        return Single.fromCallable(() -> brainKey.generateKeyPair(password));
    }

    private Completable deleteKeyknoxEntry(String identity, SyncKeyStorage syncKeyStorage) {
        return Completable.fromAction(() -> syncKeyStorage.delete(identity));
    }

    private Single<SyncKeyStorage> syncKeyknox(SyncKeyStorage syncKeyStorage) {
        return Completable.fromAction(syncKeyStorage::sync).toSingleDefault(syncKeyStorage);
    }

    @Override public void disposeAll() {
        compositeDisposable.clear();
    }
}
