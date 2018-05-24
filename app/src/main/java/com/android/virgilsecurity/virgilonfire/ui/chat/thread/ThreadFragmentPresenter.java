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

import android.content.Context;

import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultMessage;
import com.android.virgilsecurity.virgilonfire.data.model.Message;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.virgilsecurity.sdk.cards.Card;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
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

    private Context context;
    private DataReceivedInteractor<Message> messageReceivedInteractor;
    private OnMessageSentInteractor onMessageSentInteractor;
    private GetMessagesInteractor getMessagesInteractor;
    private SearchCardsInteractor searchCardsInteractor;
    private CompositeDisposable compositeDisposable;
    private VirgilRx virgilRx;
    private UserManager userManager;
    private VirgilHelper virgilHelper;
    private FirebaseFirestore firestore;
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
                                   FirebaseFirestore firestore) {
        this.context = context;
        this.messageReceivedInteractor = messageReceivedInteractor;
        this.onMessageSentInteractor = onMessageSentInteractor;
        this.virgilRx = virgilRx;
        this.searchCardsInteractor = searchCardsInteractor;
        this.userManager = userManager;
        this.virgilHelper = virgilHelper;
        this.firestore = firestore;
        this.getMessagesInteractor = getMessagesInteractor;

        compositeDisposable = new CompositeDisposable();
    }

    public void requestSendMessage(List<Card> interlocutorCards, Message message, ChatThread chatThread) {
        List<VirgilPublicKey> publicKeys = new ArrayList<>();

        for (Card card : userManager.getUserCards())
            publicKeys.add((VirgilPublicKey) card.getPublicKey());

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
                .subscribe(() -> {
                               onMessageSentInteractor.onSendMessageSuccess();
                           },
                           error -> {
                               onMessageSentInteractor.onSendMessageError(error);
                           });

        compositeDisposable.add(sendMessageRequest);
    }

    public void requestGetMessages(ChatThread chatThread) {
        Disposable getMessagesRequest = getMessagesByChannelId(chatThread.getThreadId())
                .subscribe(messages -> getMessagesInteractor.onGetMessagesSuccess(messages),
                           error -> getMessagesInteractor.onGetMessagesError(error));

        compositeDisposable.add(getMessagesRequest);
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

    private Single<List<DefaultMessage>> getMessagesByChannelId(String channelId) {
        return Single.create(emitter -> {
            firestore.collection(COLLECTION_CHANNELS)
                     .document(channelId)
                     .collection(COLLECTION_MESSAGES)
                     .get()
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             List<DefaultMessage> messages = new ArrayList<>();
                             for (DocumentSnapshot snapshot : task.getResult())
                                 messages.add(snapshot.toObject(DefaultMessage.class));

                             emitter.onSuccess(messages);
                         } else {
                             emitter.onError(task.getException());
                         }
                     });
        });
    }

    private Completable sendMessage(DefaultMessage message, String threadId, Long messagesCount) {
        return Completable.create(emitter -> {
            firestore.collection(COLLECTION_CHANNELS)
                     .document(threadId)
                     .collection(COLLECTION_MESSAGES)
                     .document(messagesCount + 1 + "")
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
                     .document(threadId)
                     .update(KEY_PROPERTY_COUNT, messagesCount + 1)
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful())
                             emitter.onComplete();
                         else
                             emitter.onError(task.getException());
                     });
        }));
    }

    @Override public void disposeAll() {
        compositeDisposable.clear();
    }

    public void turnOnMessageListener(ChatThread chatThread) {
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
                                 messages.add(message);
                             }

                             getMessagesInteractor.onGetMessagesSuccess(messages);
                         });
    }

    public void turnOffMessageListener() {
        if (listenerRegistration != null)
            listenerRegistration.remove();
    }
}
