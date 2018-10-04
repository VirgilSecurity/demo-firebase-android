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

package com.android.virgilsecurity.virgilonfire.ui.chat.thread;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.virgilsecurity.virgilonfire.data.local.RoomDb;
import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultMessage;
import com.android.virgilsecurity.virgilonfire.data.model.Message;
import com.android.virgilsecurity.virgilonfire.data.model.exception.ServiceException;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.virgilsecurity.sdk.cards.Card;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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
public class ThreadFragmentPresenter implements BasePresenter {

    private static final String COLLECTION_CHANNELS = "Channels";
    private static final String COLLECTION_MESSAGES = "Messages";
    private static final String KEY_PROPERTY_COUNT = "count";
    private static final String KEY_PROPERTY_BODY = "body";

    private final Context context;
    private final DataReceivedInteractor<Message> messageReceivedInteractor;
    private final OnMessageSentInteractor onMessageSentInteractor;
    private final GetMessagesInteractor getMessagesInteractor;
    private final SearchCardsInteractor searchCardsInteractor;
    private final CompositeDisposable compositeDisposable;
    private final VirgilRx virgilRx;
    private final UserManager userManager;
    private final VirgilHelper virgilHelper;
    private final FirebaseFirestore firestore;
    private final RoomDb roomDb;

    private ListenerRegistration listenerRegistration;

    @Inject
    public ThreadFragmentPresenter(Context context,
                                   DataReceivedInteractor<Message> messageReceivedInteractor,
                                   OnMessageSentInteractor onMessageSentInteractor,
                                   GetMessagesInteractor getMessagesInteractor,
                                   VirgilRx virgilRx,
                                   SearchCardsInteractor searchCardsInteractor,
                                   UserManager userManager,
                                   VirgilHelper virgilHelper,
                                   FirebaseFirestore firestore,
                                   RoomDb roomDb) {
        this.context = context;
        this.messageReceivedInteractor = messageReceivedInteractor;
        this.onMessageSentInteractor = onMessageSentInteractor;
        this.virgilRx = virgilRx;
        this.searchCardsInteractor = searchCardsInteractor;
        this.userManager = userManager;
        this.virgilHelper = virgilHelper;
        this.firestore = firestore;
        this.getMessagesInteractor = getMessagesInteractor;
        this.roomDb = roomDb;

        compositeDisposable = new CompositeDisposable();
    }

    void requestSendMessage(List<Card> interlocutorCards, Message message, ChatThread chatThread) {
        List<VirgilPublicKey> publicKeys = new ArrayList<>();

            publicKeys.add((VirgilPublicKey) userManager.getUserCard().getPublicKey());

        for (Card card : interlocutorCards)
            publicKeys.add((VirgilPublicKey) card.getPublicKey());

        String encryptedText = virgilHelper.encrypt(message.getBody(), publicKeys);
        Message encryptedMessage = new DefaultMessage(message.getSender(),
                                                      message.getReceiver(),
                                                      encryptedText,
                                                      new Timestamp(new Date()));

        Disposable sendMessageRequest = sendMessage((DefaultMessage) encryptedMessage,
                                                    chatThread.getThreadId(),
                                                    ((DefaultChatThread) chatThread).getMessagesCount())
                .subscribe(onMessageSentInteractor::onSendMessageSuccess,
                           onMessageSentInteractor::onSendMessageError);

        compositeDisposable.add(sendMessageRequest);
    }

    public void requestSearchCards(String identity) {
        Disposable searchCardDisposable =
                virgilRx.searchCards(identity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe((cards, throwable) -> {
                            if (throwable == null && cards.size() > 0)
                                searchCardsInteractor.onSearchSuccess(cards);
                            else
                                searchCardsInteractor.onSearchError(throwable);
                        });

        compositeDisposable.add(searchCardDisposable);
    }

    private Completable sendMessage(DefaultMessage message, String channelId, Long messagesCount) {
        return Completable.create(emitter -> {
            firestore.collection(COLLECTION_CHANNELS)
                     .document(channelId)
                     .collection(COLLECTION_MESSAGES)
                     .document(messagesCount + "")
                     .set(message)
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             emitter.onComplete();
                         } else {
                             emitter.onError(task.getException());
                         }
                     });
        }).andThen(Completable.create(emitter -> {
            firestore.collection(COLLECTION_CHANNELS)
                     .document(channelId)
                     .update(KEY_PROPERTY_COUNT, messagesCount + 1)
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful())
                             emitter.onComplete();
                         else
                             emitter.onError(task.getException());
                     });
        })).observeOn(Schedulers.io())
                          .andThen(Completable.create(insertMessageEmitter -> {
                              message.setChannelId(channelId);
                              roomDb.messageDao().inertMessage(message);
                              insertMessageEmitter.onComplete();
                          })).subscribeOn(Schedulers.io())
                          .observeOn(AndroidSchedulers.mainThread());
    }

    @Override public void disposeAll() {
        compositeDisposable.clear();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @SuppressLint("CheckResult") void turnOnMessageListener(ChatThread chatThread) {
        listenerRegistration =
                firestore.collection(COLLECTION_CHANNELS)
                         .document(chatThread.getThreadId())
                         .collection(COLLECTION_MESSAGES)
                         .addSnapshotListener((queryDocumentSnapshots, e) -> {
                             if (e != null) {
                                 getMessagesInteractor.onGetMessagesError(e);
                                 return;
                             }

                             List<DefaultMessage> messages = new ArrayList<>();

                             for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                 DefaultMessage message = snapshot.toObject(DefaultMessage.class);
                                 message.setMessageId(Long.parseLong(snapshot.getId()));
                                 message.setChannelId(chatThread.getThreadId());
                                 messages.add(message);
                             }

                             if (!messages.isEmpty()) {
                                 Single.fromCallable(() -> {
                                     return roomDb.messageDao().insertAll(messages);
                                 }).subscribeOn(Schedulers.io())
                                            .observeOn(Schedulers.io())
                                            .flatMap((insertedIdentifiers) -> {

                                                for (DefaultMessage message : messages) {
                                                    if (!message.getBody().isEmpty())
                                                        consumeMessage(message, chatThread);
                                                }

                                                return roomDb.messageDao().getAllById(chatThread.getThreadId());
                                            })
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(getMessagesInteractor::onGetMessagesSuccess,
                                                       getMessagesInteractor::onGetMessagesError);
                             } else {
                                 roomDb.messageDao()
                                       .getAllById(chatThread.getThreadId())
                                       .subscribeOn(Schedulers.io())
                                       .observeOn(AndroidSchedulers.mainThread())
                                       .subscribe(getMessagesInteractor::onGetMessagesSuccess,
                                                  getMessagesInteractor::onGetMessagesError);
                             }
                         });
    }

    private void consumeMessage(DefaultMessage message, ChatThread chatThread) {
        if (message.getSender().equals(chatThread.getReceiver())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            firestore.collection(COLLECTION_CHANNELS)
                     .document(chatThread.getThreadId())
                     .collection(COLLECTION_MESSAGES)
                     .document(String.valueOf(message.getMessageId()))
                     .update(KEY_PROPERTY_BODY, "")
                     .addOnCompleteListener(executor, task -> {
                         if (!task.isSuccessful())
                             throw new ServiceException("Consuming message error");
                     });

            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    void turnOffMessageListener() {
//        if (listenerRegistration != null) {
//            listenerRegistration.remove();
//            listenerRegistration = null;
//        }
//    }
}
