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

package com.android.virgilsecurity.virgilonfire.ui.chat.threadList;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultUser;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    4/17/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */
public class ThreadsListFragmentPresenter implements BasePresenter {

    private static final String COLLECTION_CHANNELS = "Channels";
    private static final String COLLECTION_USERS = "Users";
    private static final String KEY_PROPERTY_MEMBERS = "members";

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private DataReceivedInteractor<List<DefaultChatThread>> onMessageReceivedInteractor;
    private CompositeDisposable compositeDisposable;

    @Inject
    public ThreadsListFragmentPresenter(FirebaseFirestore firebaseFirestore,
                                        FirebaseAuth firebaseAuth,
                                        DataReceivedInteractor<List<DefaultChatThread>> onMessageReceivedInteractor) {
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseAuth = firebaseAuth;
        this.onMessageReceivedInteractor = onMessageReceivedInteractor;

        compositeDisposable = new CompositeDisposable();
    }

    public void requestThreadsList() {
        Disposable requestUsersDisposable =
                Single.zip(getCurrentUser(), getChannels(),
                           (defaultUser, documentSnapshots) -> {
                               List<DefaultChatThread> threads = new ArrayList<>();

                               for (String channelId : defaultUser.getChannels()) {
                                   for (DocumentSnapshot document : documentSnapshots) {
                                       if (document.getId().equals(channelId)) {
                                           String[] members = (String[]) document.get(KEY_PROPERTY_MEMBERS);
                                           String senderId = members[0].equals(firebaseAuth.getCurrentUser()
                                                                                           .getEmail()
                                                                                           .toLowerCase())
                                                             ? members[0] : members[1];
                                           String receiverId = members[0].equals(senderId) ? members[1] : members[0];
                                           threads.add(new DefaultChatThread(document.getId(),
                                                                             senderId,
                                                                             receiverId));
                                       }
                                   }
                               }

                               return threads;
                           })
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(Schedulers.io())
                      .subscribe((threads, throwable) -> {
                          onMessageReceivedInteractor.onDataReceived(threads);
                      });

        compositeDisposable.add(requestUsersDisposable);
//
    }

    private Single<DefaultUser> getCurrentUser() {
        return Single.create(emitter -> {
            firebaseFirestore.collection(COLLECTION_USERS)
                             .document(firebaseAuth.getCurrentUser()
                                                   .getEmail()
                                                   .toLowerCase())
                             .get()
                             .addOnCompleteListener(task -> {
                                 if (task.isSuccessful()) {
                                     DocumentSnapshot documentSnapshot = task.getResult();

                                     DefaultUser user = documentSnapshot.toObject(DefaultUser.class);
                                     user.setName(documentSnapshot.getId());

                                     emitter.onSuccess(user);
                                 } else {
                                     emitter.onError(task.getException());
                                 }
                             });
        });
    }

    private Single<List<DocumentSnapshot>> getChannels() {
        return Single.create(emitter -> {
            firebaseFirestore.collection(COLLECTION_CHANNELS)
                             .get()
                             .addOnCompleteListener(task -> {
                                 if (task.isSuccessful()) {
                                     QuerySnapshot querySnapshot = task.getResult();
                                     emitter.onSuccess(querySnapshot.getDocuments());
                                 } else {
                                     emitter.onError(task.getException());
                                 }
                             });
        });
    }

    @Override
    public void disposeAll() {
        compositeDisposable.clear();
    }
}
