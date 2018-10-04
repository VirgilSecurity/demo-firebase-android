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

package com.android.virgilsecurity.virgilonfire.ui.login;

import android.util.Pair;

import com.android.virgilsecurity.virgilonfire.data.model.SyncKeyStorageWrapper;
import com.android.virgilsecurity.virgilonfire.data.model.exception.AccountResetedException;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.virgilsecurity.keyknox.storage.SyncKeyStorage;
import com.virgilsecurity.pythia.brainkey.BrainKey;
import com.virgilsecurity.pythia.model.exception.VirgilPythiaServiceException;
import com.virgilsecurity.sdk.cards.Card;
import com.virgilsecurity.sdk.crypto.PrivateKey;
import com.virgilsecurity.sdk.crypto.PublicKey;
import com.virgilsecurity.sdk.crypto.VirgilKeyPair;
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey;
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException;
import com.virgilsecurity.sdk.crypto.exceptions.KeyEntryNotFoundException;
import com.virgilsecurity.sdk.storage.KeyEntry;
import com.virgilsecurity.sdk.storage.PrivateKeyStorage;
import com.virgilsecurity.sdk.utils.Tuple;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Danylo Oliinyk on 3/22/18 at Virgil Security.
 * -__o
 */

public class LogInPresenter implements BasePresenter {

    private final CompositeDisposable compositeDisposable;
    private final LogInVirgilInteractor logInVirgilInteractor;
    private final LogInKeyStorageInteractor logInKeyStorageInteractor;
    private final VirgilRx virgilRx;
    private final VirgilHelper virgilHelper;
    private final PrivateKeyStorage privateKeyStorage;
    private final KeyknoxSyncInteractor keyknoxSyncInteractor;
    private final BrainKey brainKey;
    private final SyncKeyStorageWrapper syncKeyStorageWrapper;

    @Inject
    public LogInPresenter(VirgilRx virgilRx,
                          VirgilHelper virgilHelper,
                          PrivateKeyStorage privateKeyStorage,
                          LogInVirgilInteractor logInVirgilInteractor,
                          LogInKeyStorageInteractor logInKeyStorageInteractor,
                          KeyknoxSyncInteractor keyknoxSyncInteractor,
                          BrainKey brainKey,
                          SyncKeyStorageWrapper syncKeyStorageWrapper) {
        this.virgilRx = virgilRx;
        this.virgilHelper = virgilHelper;
        this.privateKeyStorage = privateKeyStorage;
        this.logInVirgilInteractor = logInVirgilInteractor;
        this.logInKeyStorageInteractor = logInKeyStorageInteractor;
        this.keyknoxSyncInteractor = keyknoxSyncInteractor;
        this.brainKey = brainKey;
        this.syncKeyStorageWrapper = syncKeyStorageWrapper;

        compositeDisposable = new CompositeDisposable();
    }

    void requestSearchCards(String identity) {
        Disposable searchCardDisposable =
                virgilRx.searchCards(identity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe((cards, throwable) -> {
                            if (throwable == null && cards.size() > 0)
                                logInVirgilInteractor.onSearchCardSuccess(cards);
                            else
                                logInVirgilInteractor.onSearchCardError(throwable);
                        });

        compositeDisposable.add(searchCardDisposable);
    }

    void requestPublishCard(String identity, String password) {
        Disposable publishCardDisposable =
                generateBrainKey(password)
                        .subscribeOn(Schedulers.io())
                        .flatMap(keyPair -> {
                            syncKeyStorageWrapper.setIdentity(identity);
                            syncKeyStorageWrapper.setPrivateKey(keyPair.getPrivateKey());
                            syncKeyStorageWrapper.setPublicKey(keyPair.getPublicKey());
                            SyncKeyStorage syncKeyStorage = syncKeyStorageWrapper.initSyncKeyStorage();

                            return Single.zip(virgilRx.publishCard(identity),
                                              Single.just(syncKeyStorage),
                                              syncKeyknox(syncKeyStorage),
                                              (card, ignored1, ignored2) -> {
                                                  return new Pair<Card, SyncKeyStorage>(card, syncKeyStorage);
                                              });
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .flatMap(pair -> Single.zip(Single.just(pair.first),
                                                    storeInKeyknox(pair.second,
                                                                   identity).subscribeOn(Schedulers.io()),
                                                    (cardTemp, aVoid) -> cardTemp))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((card, throwable) -> {
                            if (throwable == null) {
                                logInVirgilInteractor.onPublishCardSuccess(card);
                            } else {
                                privateKeyStorage.delete(identity);
                                logInVirgilInteractor.onPublishCardError(throwable);
                            }
                        });

        compositeDisposable.add(publishCardDisposable);
    }

    void requestIfKeyExists(String keyName) {
        if (privateKeyStorage.exists(keyName))
            logInKeyStorageInteractor.onKeyExists();
        else
            logInKeyStorageInteractor.onKeyNotExists();
    }

    @Override public void disposeAll() {
        compositeDisposable.clear();
    }

    void requestSyncWithKeyknox(String identity, String password) {
        Disposable syncKeyknoxDisposable =
                generateBrainKey(password)
                        .subscribeOn(Schedulers.io())
                        .flatMap(keyPair -> {
                            syncKeyStorageWrapper.setIdentity(identity);
                            syncKeyStorageWrapper.setPrivateKey(keyPair.getPrivateKey());
                            syncKeyStorageWrapper.setPublicKey(keyPair.getPublicKey());
                            SyncKeyStorage syncKeyStorage = syncKeyStorageWrapper.initSyncKeyStorage();

                            return syncKeyknox(syncKeyStorage).subscribeOn(Schedulers.io());
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(syncKeyStorageTemp -> {
                                       KeyEntry keyEntry = null;
                                       try {
                                           keyEntry = syncKeyStorageTemp.retrieve(identity +
                                                                                          VirgilHelper.KEYKNOX_POSTFIX);
                                       } catch (KeyEntryNotFoundException e) {
                                            e.printStackTrace();
                                       }

                                       if (keyEntry == null) {
                                           keyknoxSyncInteractor.onKeyknoxSyncError(new AccountResetedException());
                                           return;
                                       }

                                       virgilHelper.storeKey(keyEntry);
                                       keyknoxSyncInteractor.onKeyknoxSyncSuccess();
                                   },
                                   keyknoxSyncInteractor::onKeyknoxSyncError);

        compositeDisposable.add(syncKeyknoxDisposable);
    }

    private Single<SyncKeyStorage> syncKeyknox(SyncKeyStorage syncKeyStorage) {
        return Completable.fromAction(syncKeyStorage::sync).toSingleDefault(syncKeyStorage);
    }

    private Single<Object> storeInKeyknox(SyncKeyStorage syncKeyStorage, String identity) {
        return Completable.fromAction(() -> {
            Tuple<PrivateKey, Map<String, String>> keyData = virgilHelper.load();
            syncKeyStorage.store(identity + VirgilHelper.KEYKNOX_POSTFIX,
                                 ((VirgilPrivateKey) keyData.getLeft()).getRawKey(),
                                 keyData.getRight());
        }).toSingleDefault(new Object());
    }

    private Single<VirgilKeyPair> generateBrainKey(String password) {
        return Single.fromCallable(() -> brainKey.generateKeyPair(password));
    }
}
