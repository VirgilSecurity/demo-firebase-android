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

package com.android.virgilsecurity.virgilonfire.ui.chat.threadList

import com.android.virgilsecurity.virgilonfire.data.model.ChatThread
import com.android.virgilsecurity.virgilonfire.data.model.CreateChannelRequest
import com.android.virgilsecurity.virgilonfire.data.model.GenerateHashException
import com.android.virgilsecurity.virgilonfire.data.model.User
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper
import com.android.virgilsecurity.virgilonfire.ui.CompleteInteractor
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor
import com.android.virgilsecurity.virgilonfire.util.UserUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.virgilsecurity.sdk.crypto.HashAlgorithm
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException
import com.virgilsecurity.sdk.utils.ConvertionUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    12/17/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

/**
 * ThreadsListFragmentPresenter class.
 */
class ThreadsListFragmentPresenter @Inject
constructor(
        private val firebaseFirestore: FirebaseFirestore,
        private val firebaseAuth: FirebaseAuth,
        private val virgilHelper: VirgilHelper,
        private val onDataReceivedInteractor: DataReceivedInteractor<List<ChatThread>>,
        private val completeInteractor: CompleteInteractor<ThreadListFragmentPresenterReturnTypes>
) : BasePresenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var listenerRegistration: ListenerRegistration? = null

    fun turnOnThreadsListener() {
        listenerRegistration = firebaseFirestore.collection(COLLECTION_CHANNELS)
                .addSnapshotListener { documentSnapshots, _ ->
                    firebaseFirestore.collection(COLLECTION_USERS)
                            .document(UserUtils.currentUsername(firebaseAuth))
                            .get()
                            .addOnCompleteListener { task ->
                                updateThreads(documentSnapshots!!, task)
                            }
                }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateThreads(documentSnapshots: QuerySnapshot, task: Task<DocumentSnapshot>) {
        if (task.isSuccessful) {
            val documentSnapshot = task.result

            val defaultUser = documentSnapshot!!.toObject<User>(User::class.java)
            defaultUser!!.name = documentSnapshot.id

            val threads = ArrayList<ChatThread>()

            for (channelId in defaultUser.channels) {
                for (document in documentSnapshots) {
                    if (document.id == channelId) {
                        val members = document.get(KEY_PROPERTY_MEMBERS) as List<String>?
                        val messagesCount = (document.get(KEY_PROPERTY_COUNT) as Long?)!!

                        val senderId =
                                if (members!![0] == UserUtils.currentUsername(firebaseAuth))
                                    members[0]
                                else
                                    members[1]

                        val receiverId =
                                if (members[0] == senderId)
                                    members[1]
                                else
                                    members[0]
                        threads.add(ChatThread(document.id, senderId, receiverId, messagesCount))
                    }
                }
            }

            onDataReceivedInteractor.onDataReceived(threads)
        } else {
            onDataReceivedInteractor.onDataReceivedError(task.exception!!)
        }
    }

    fun turnOffThreadsListener() {
        if (listenerRegistration != null)
            listenerRegistration!!.remove()
    }

    fun requestCreateThread(interlocutor: String) {
        val newThreadId = generateNewChannelId(interlocutor)

        val requestCreateThreadDisposable = updateUserMe(newThreadId)
                .andThen(updateUserInterlocutor(interlocutor, newThreadId))
                .andThen(createThread(interlocutor, newThreadId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onComplete = {
                        completeInteractor.onComplete(ThreadListFragmentPresenterReturnTypes.CREATE_THREAD)
                    },
                    onError = {
                        completeInteractor.onError(it)
                    })

        compositeDisposable.add(requestCreateThreadDisposable)
    }

    fun requestRemoveThread(interlocutor: String) {
        val newThreadId = generateNewChannelId(interlocutor)

        val requestRemoveThreadDisposable = Completable.create { emitter ->
            firebaseFirestore.collection(COLLECTION_CHANNELS)
                    .document(newThreadId)
                    .delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            emitter.onComplete()
                        else
                            emitter.onError(task.exception!!)
                    }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onComplete = {
                        completeInteractor.onComplete(ThreadListFragmentPresenterReturnTypes.REMOVE_CHAT_THREAD)
                    },
                    onError = {
                        completeInteractor.onError(it)
                    })

        compositeDisposable.add(requestRemoveThreadDisposable)
    }

    private fun createThread(interlocutor: String, newThreadId: String): Completable {
        return Completable.create { emitter ->
            val members = ArrayList<String>()
            members.add(UserUtils.currentUsername(firebaseAuth))
            members.add(interlocutor)
            val createChannelRequest = CreateChannelRequest(
                members,
                0)

            firebaseFirestore.collection(COLLECTION_CHANNELS)
                    .document(newThreadId)
                    .set(createChannelRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            emitter.onComplete()
                        else
                            emitter.onError(task.exception!!)
                    }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getUserChannels(username: String): Single<MutableList<String>> {
        return Single.create { emitter ->
            firebaseFirestore.collection(COLLECTION_USERS)
                    .document(username)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onSuccess(task.result!!.get(KEY_PROPERTY_CHANNELS) as MutableList<String>)
                        } else {
                            emitter.onError(task.exception!!)
                        }
                    }
        }
    }

    private fun updateUserMe(newThreadId: String): Completable {
        return getUserChannels(UserUtils.currentUsername(firebaseAuth))
                .flatMapCompletable { channels ->
                    Completable.create { emitter ->
                        channels.add(newThreadId)
                        firebaseFirestore.collection(COLLECTION_USERS)
                                .document(UserUtils.currentUsername(firebaseAuth))
                                .update("channels", channels)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful)
                                        emitter.onComplete()
                                    else
                                        emitter.onError(task.exception!!)
                                }
                    }
                }
    }

    private fun updateUserInterlocutor(interlocutor: String, newThreadId: String): Completable {
        return getUserChannels(interlocutor)
                .flatMapCompletable { channels ->
                    Completable.create { emitter ->
                        channels.add(newThreadId)
                        firebaseFirestore.collection(COLLECTION_USERS)
                                .document(interlocutor)
                                .update("channels", channels)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful)
                                        emitter.onComplete()
                                    else
                                        emitter.onError(task.exception!!)
                                }
                    }
                }
    }

    private fun generateNewChannelId(interlocutor: String): String {
        val userMe = UserUtils.currentUsername(firebaseAuth)
        val concatenatedHashedUsersData: ByteArray

        concatenatedHashedUsersData = try {
            if (userMe >= interlocutor) {
                virgilHelper.virgilCrypto
                        .generateHash((userMe + interlocutor).toByteArray(),
                                      HashAlgorithm.SHA256)
            } else {
                virgilHelper.virgilCrypto
                        .generateHash((interlocutor + userMe).toByteArray(),
                                      HashAlgorithm.SHA256)
            }
        } catch (e: CryptoException) {
            e.printStackTrace()
            throw GenerateHashException()
        }

        return ConvertionUtils.toHex(concatenatedHashedUsersData).toLowerCase()
    }

    override fun disposeAll() {
        compositeDisposable.clear()
    }

    companion object {

        private const val COLLECTION_CHANNELS = "Channels"
        private const val COLLECTION_USERS = "Users"
        private const val KEY_PROPERTY_MEMBERS = "members"
        private const val KEY_PROPERTY_CHANNELS = "channels"
        private const val KEY_PROPERTY_COUNT = "count"
    }
}
