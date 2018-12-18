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
import com.android.virgilsecurity.virgilonfire.data.model.Message
import com.android.virgilsecurity.virgilonfire.data.model.ServiceException
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilRx
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.virgilsecurity.sdk.cards.Card
import com.virgilsecurity.sdk.crypto.VirgilPublicKey
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    12/18/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

/**
 * ThreadFragmentPresenter class.
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

    fun requestSendMessage(interlocutorCards: List<Card>,
                           message: Message,
                           chatThread: ChatThread) {
        val publicKeys = ArrayList<VirgilPublicKey>()

        for (card in userManager.userCards)
            publicKeys.add(card.publicKey as VirgilPublicKey)

        for (card in interlocutorCards)
            publicKeys.add(card.publicKey as VirgilPublicKey)

        val encryptedText = virgilHelper.encrypt(message.body!!, publicKeys)
        val encryptedMessage = Message(message.sender,
                                       message.receiver,
                                       encryptedText,
                                       Timestamp(Date()))

        val sendMessageRequest =
                sendMessage(encryptedMessage,
                            chatThread.threadId,
                            chatThread.messagesCount).subscribeBy(
                    onComplete = {
                        onMessageSentInteractor.onSendMessageSuccess()
                    },
                    onError = {
                        onMessageSentInteractor.onSendMessageError(it)
                    })

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

    private fun sendMessage(message: Message,
                            channelId: String,
                            messagesCount: Long?): Completable {
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
                            emitter.onError(task.exception!!)
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
                            emitter.onError(task.exception!!)
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

    @SuppressLint("CheckResult") fun turnOnMessageListener(ChatThread: ChatThread) {
        listenerRegistration = firestore.collection(COLLECTION_CHANNELS)
                .document(ChatThread.threadId)
                .collection(COLLECTION_MESSAGES)
                .addSnapshotListener { queryDocumentSnapshots, e ->
                    if (e != null) {
                        getMessagesInteractor.onGetMessagesError(e)
                        return@addSnapshotListener
                    }

                    val messages = ArrayList<Message>()

                    for (snapshot in queryDocumentSnapshots!!.iterator()) {
                        val message = snapshot.toObject(Message::class.java)
                        message.messageId = snapshot.id.toLong()
                        message.channelId = ChatThread.threadId
                        messages.add(message)
                    }

                    if (!messages.isEmpty()) {
                        Single.fromCallable { roomDb.messageDao().insertAll(messages) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap { insertedIdentifiers ->

                                    for (message in messages) {
                                        if (!message.body!!.isEmpty())
                                            consumeMessage(message, ChatThread)
                                    }

                                    roomDb.messageDao().getAllById(ChatThread.threadId)
                                }
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onSuccess = {
                                        getMessagesInteractor.onGetMessagesSuccess(it.toMutableList())
                                    },
                                    onError = {
                                        getMessagesInteractor.onGetMessagesError(it)
                                    })
                    } else {
                        roomDb.messageDao().getAllById(ChatThread.threadId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onSuccess = {
                                        getMessagesInteractor.onGetMessagesSuccess(it.toMutableList())
                                    },
                                    onError = {
                                        getMessagesInteractor.onGetMessagesError(it)
                                    })
                    }
                }
    }

    private fun consumeMessage(message: Message, ChatThread: ChatThread) {
        if (message.sender == ChatThread.receiver) {
            runBlocking {
                GlobalScope.async {
                    firestore.collection(COLLECTION_CHANNELS)
                            .document(ChatThread.threadId)
                            .collection(COLLECTION_MESSAGES)
                            .document(message.messageId.toString())
                            .update(KEY_PROPERTY_BODY, "").isSuccessful
                }.await().run {
                    if (!this)
                        throw ServiceException("Consuming message error")
                }
            }

        }
    }

    fun turnOffMessageListener() {
        if (listenerRegistration != null)
            listenerRegistration!!.remove()
    }

    companion object {
        private const val COLLECTION_CHANNELS = "Channels"
        private const val COLLECTION_MESSAGES = "Messages"
        private const val KEY_PROPERTY_COUNT = "count"
        private const val KEY_PROPERTY_BODY = "body"
    }
}
