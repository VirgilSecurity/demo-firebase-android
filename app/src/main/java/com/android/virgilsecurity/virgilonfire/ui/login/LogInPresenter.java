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

import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.virgilsecurity.sdk.storage.PrivateKeyStorage;

import javax.inject.Inject;

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
    private final PrivateKeyStorage privateKeyStorage;
    private final RefreshUserCardsInteractor refreshUserCardsInteractor;

    @Inject
    public LogInPresenter(VirgilRx virgilRx,
                          PrivateKeyStorage privateKeyStorage,
                          LogInVirgilInteractor logInVirgilInteractor,
                          LogInKeyStorageInteractor logInKeyStorageInteractor,
                          RefreshUserCardsInteractor refreshUserCardsInteractor) {
        this.virgilRx = virgilRx;
        this.privateKeyStorage = privateKeyStorage;
        this.logInVirgilInteractor = logInVirgilInteractor;
        this.logInKeyStorageInteractor = logInKeyStorageInteractor;
        this.refreshUserCardsInteractor = refreshUserCardsInteractor;

        compositeDisposable = new CompositeDisposable();
    }

    public void requestSearchCards(String identity) {
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

    public void requestPublishCard(String identity) {
        Disposable publishCardDisposable =
                virgilRx.publishCard(identity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
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

    public void requestIfKeyExists(String keyName) {
        if (privateKeyStorage.exists(keyName))
            logInKeyStorageInteractor.onKeyExists();
        else
            logInKeyStorageInteractor.onKeyNotExists();
    }

    public void requestRefreshUserCards(String username) {
        Disposable refreshUserCardsDisposable =
                virgilRx.searchCards(username)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(refreshUserCardsInteractor::onRefreshUserCardsSuccess,
                                   refreshUserCardsInteractor::onRefreshUserCardsError);

        compositeDisposable.add(refreshUserCardsDisposable);
    }

    @Override public void disposeAll() {
        compositeDisposable.clear();
    }

}
