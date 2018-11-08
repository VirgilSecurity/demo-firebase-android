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

package com.android.virgilsecurity.virgilonfire.ui.chat.thread

import android.annotation.SuppressLint
import android.content.Context

import com.android.virgilsecurity.virgilonfire.data.local.RoomDb
import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread
import com.android.virgilsecurity.virgilonfire.data.model.DefaultMessage
import com.android.virgilsecurity.virgilonfire.data.model.Message
import com.android.virgilsecurity.virgilonfire.data.model.exception.ServiceException
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.virgilsecurity.sdk.cards.Card
import com.virgilsecurity.sdk.crypto.VirgilPublicKey

import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

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
class ThreadFragmentPresenter @Inject
constructor(private val context: Context,
            private val messageReceivedInteractor: DataReceivedInteractor<Message>,
            private val onMessageSentInteractor: OnMessageSentInteractor,
            private val getMessagesInteractor: GetMessagesInteractor,
            private val virgilRx: VirgilRx,
            private val searchCardsInteractor: SearchCardsInteractor,
            private val userManager: UserManager,
            private val virgilHelper: VirgilHelper,
            private val firestore: FirebaseFirestore,
            private val roomDb: RoomDb) : BasePresenter {
    private val compositeDisposable: CompositeDisposable

    private var listenerRegistration: ListenerRegistration? = null

    init {

        compositeDisposable = CompositeDisposable()
    }

    fun requestSendMessage(interlocutorCards: List<Card>, message: Message, chatThread: ChatThread) {
        val publicKeys = ArrayList<VirgilPublicKey>()

        for (card in userManager.userCards)
            publicKeys.add(card.publicKey as VirgilPublicKey)

        for (card in interlocutorCards)
            publicKeys.add(card.publicKey as VirgilPublicKey)

        val encryptedText = virgilHelper.encrypt(message.body, publicKeys)
        val encryptedMessage = DefaultMessage(message.sender,
                                              message.receiver,
                                              encryptedText,
                                              Timestamp(Date()))

        val sendMessageRequest = sendMessage(encryptedMessage,
                                             chatThread.threadId,
                                             (chatThread as DefaultChatThread).messagesCount)
                .subscribe(Action { onMessageSentInteractor.onSendMessageSuccess() },
                           Consumer<Throwable> { onMessageSentInteractor.onSendMessageError(it) })

        compositeDisposable.add(sendMessageRequest)
    }

    fun requestSearchCards(identity: String) {
        val searchCardDisposable = virgilRx.searchCards(identity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { cards, throwable ->
                    if (throwable == null && cards.size > 0)
                        searchCardsInteractor.onSearchSuccess(cards)
                    else
                        searchCardsInteractor.onSearchError(throwable)
                }

        compositeDisposable.add(searchCardDisposable)
    }

    private fun sendMessage(message: DefaultMessage, channelId: String, messagesCount: Long?): Completable {
        return Completable.create { emitter ->
            firestore.collection(COLLECTION_CHANNELS)
                    .document(channelId)
                    .collection(COLLECTION_MESSAGES)
                    .document(messagesCount!!.toString() + "")
                    .set(message)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                        } else {
                            emitter.onError(task.getException())
                        }
                    }
        }.andThen(Completable.create { emitter ->
            firestore.collection(COLLECTION_CHANNELS)
                    .document(channelId)
                    .update(KEY_PROPERTY_COUNT, messagesCount!! + 1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            emitter.onComplete()
                        else
                            emitter.onError(task.getException())
                    }
        }).observeOn(Schedulers.io())
                .andThen(Completable.create { insertMessageEmitter ->
                    message.channelId = channelId
                    roomDb.messageDao().inertMessage(message)
                    insertMessageEmitter.onComplete()
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun disposeAll() {
        compositeDisposable.clear()
    }

    @SuppressLint("CheckResult") fun turnOnMessageListener(chatThread: ChatThread) {
        listenerRegistration = firestore.collection(COLLECTION_CHANNELS)
                .document(chatThread.threadId)
                .collection(COLLECTION_MESSAGES)
                .addSnapshotListener { queryDocumentSnapshots, e ->
                    if (e != null) {
                        getMessagesInteractor.onGetMessagesError(e)
                        return@firestore.collection(COLLECTION_CHANNELS)
                                .document(chatThread.threadId)
                                .collection(COLLECTION_MESSAGES)
                                .addSnapshotListener
                    }

                    val messages = ArrayList<DefaultMessage>()

                    for (snapshot in queryDocumentSnapshots) {
                        val message = snapshot.toObject(DefaultMessage::class.java!!)
                        message!!.messageId = java.lang.Long.parseLong(snapshot.getId())
                        message!!.channelId = chatThread.threadId
                        messages.add(message)
                    }

                    if (!messages.isEmpty()) {
                        Single.fromCallable { roomDb.messageDao().insertAll(messages) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap { insertedIdentifiers ->

                                    for (message in messages) {
                                        if (!message.body!!.isEmpty())
                                            consumeMessage(message, chatThread)
                                    }

                                    roomDb.messageDao().getAllById(chatThread.threadId)
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Consumer<List<DefaultMessage>> {
                                    getMessagesInteractor.onGetMessagesSuccess(it)
                                },
                                           Consumer<Throwable> {
                                               getMessagesInteractor.onGetMessagesError(it)
                                           })
                    } else {
                        roomDb.messageDao().getAllById(chatThread.threadId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Consumer<List<DefaultMessage>> {
                                    getMessagesInteractor.onGetMessagesSuccess(it)
                                },
                                           Consumer<Throwable> {
                                               getMessagesInteractor.onGetMessagesError(it)
                                           })
                    }
                }
    }

    private fun consumeMessage(message: DefaultMessage, chatThread: ChatThread) {
        if (message.sender == chatThread.receiver) {
            val executor = Executors.newSingleThreadExecutor()

            firestore.collection(COLLECTION_CHANNELS)
                    .document(chatThread.threadId)
                    .collection(COLLECTION_MESSAGES)
                    .document(message.messageId.toString())
                    .update(KEY_PROPERTY_BODY, "")
                    .addOnCompleteListener(executor) { task ->
                        if (!task.isSuccessful())
                            throw ServiceException("Consuming message error")
                    }

            try {
                executor.awaitTermination(2, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

    fun turnOffMessageListener() {
        if (listenerRegistration != null)
            listenerRegistration!!.remove()
    }

    companion object {
        private val COLLECTION_CHANNELS = "Channels"
        private val COLLECTION_MESSAGES = "Messages"
        private val KEY_PROPERTY_COUNT = "count"
        private val KEY_PROPERTY_BODY = "body"
    }
}
