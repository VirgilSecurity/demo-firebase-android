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

import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import butterknife.BindView
import butterknife.OnClick
import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.data.model.*
import com.android.virgilsecurity.virgilonfire.ui.base.BaseFragmentDi
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivity
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver
import com.android.virgilsecurity.virgilonfire.util.UiUtils
import com.android.virgilsecurity.virgilonfire.util.UserUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.virgilsecurity.sdk.cards.Card
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
 * ThreadFragment class.
 */
class ThreadFragment
    : BaseFragmentDi<ChatControlActivity>(),
        DataReceivedInteractor<Message>,
        OnMessageSentInteractor,
        SearchCardsInteractor,
        GetMessagesInteractor {

    @Inject
    protected var adapter: ThreadRVAdapter? = null
    @Inject
    protected var presenter: ThreadFragmentPresenter? = null
    @Inject
    protected var errorResolver: ErrorResolver? = null
    @Inject
    protected var userManager: UserManager? = null
    @Inject
    protected var firebaseAuth: FirebaseAuth? = null

    private var chatThread: ChatThread? = null
    private var interlocutorCards: List<Card>? = null
    private var layoutManager: LinearLayoutManager? = null

    @BindView(R.id.rvChat)
    protected var rvChat: RecyclerView? = null
    @BindView(R.id.etMessage)
    internal var etMessage: EditText? = null
    @BindView(R.id.btnSend)
    internal var btnSend: ImageButton? = null
    @BindView(R.id.tvEmpty)
    internal var tvEmpty: View? = null
    @BindView(R.id.tvError)
    internal var tvError: View? = null
    @BindView(R.id.pbLoading)
    internal var pbLoading: View? = null
    @BindView(R.id.srlRefresh)
    internal var srlRefresh: SwipeRefreshLayout? = null

    protected override val layout: Int
        get() = R.layout.fragment_thread

    override fun postButterInit() {
        rvChat!!.adapter = adapter
        layoutManager = LinearLayoutManager(activity)
        layoutManager!!.reverseLayout = false
        rvChat!!.layoutManager = layoutManager
        initMessageInput()
        rvChat!!.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                if (rvChat!!.adapter!!.itemCount > THRESHOLD_SCROLL) {
                    rvChat!!.postDelayed({
                                             rvChat!!.smoothScrollToPosition(
                                                 rvChat!!.adapter!!.itemCount - 1)
                                         }, 100)
                }
            }
        }
        srlRefresh!!.setOnRefreshListener {
            presenter!!.turnOffMessageListener()
            presenter!!.turnOnMessageListener(chatThread!!)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()

        presenter!!.turnOffMessageListener()
    }

    fun disposeAll() {
        presenter!!.disposeAll()
    }

    override fun onDataReceived(receivedData: Message) {
        adapter!!.addItem(receivedData as Message)
        rootActivity!!.showBaseLoading(false)
        showProgress(false)
    }

    override fun onDataReceivedError(t: Throwable) {
        lockSendUi(false)
        UiUtils.toast(this, errorResolver!!.resolve(t))
    }

    override fun onSendMessageSuccess() {
        lockSendUi(false)
    }

    override fun onSendMessageError(t: Throwable) {
        etMessage!!.setText("")
        lockSendUi(false)
        UiUtils.toast(this, errorResolver!!.resolve(t))
    }

    private fun initMessageInput() {
        etMessage!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                lockSendUi(charSequence.toString()
                                   .isEmpty())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
    }

    private fun lockSendUi(lockButton: Boolean) {
        if (!lockButton) {
            btnSend!!.isEnabled = true
            btnSend!!.background = ContextCompat.getDrawable(activity,
                                                             R.drawable.bg_btn_chat_send)
        } else {
            btnSend!!.isEnabled = false
            btnSend!!.background = ContextCompat.getDrawable(activity,
                                                             R.drawable.bg_btn_chat_send_pressed)
        }
    }

    @OnClick(R.id.btnSend) internal fun onInterfaceClick(v: View) {
        when (v.id) {
            R.id.btnSend -> {
                val text = etMessage!!.text
                        .toString()
                        .trim { it <= ' ' }
                if (!text.isEmpty()) {
                    if (interlocutorCards != null) {
                        lockSendUi(true)
                        sendMessage(text)
                    } else {
                        UiUtils.toast(this, "No interlocutor cards")
                    }
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        showProgress(true)
        val message = Message(UserUtils.currentUsername(firebaseAuth!!),
                              chatThread!!.receiver,
                              text,
                              Timestamp(Date()))

        presenter!!.requestSendMessage(interlocutorCards!!, message, chatThread!!)
        etMessage!!.setText("")
    }

    fun setChatThread(ChatThread: ChatThread) {
        this.chatThread = ChatThread

        showProgress(true)
        lockSendUi(true)
        rootActivity!!.changeToolbarTitleExposed(this.chatThread!!.receiver)

        adapter!!.clearItems()
        interlocutorCards = null
        presenter!!.requestSearchCards(ChatThread.receiver)
    }

    override fun onSearchSuccess(cards: List<Card>) {
        if (cards.isEmpty()) {
            presenter!!.disposeAll()
            UiUtils.toast(this, R.string.no_cards_found)
            throw CardsNotFoundException("LogInFragment -> No cards was found")
        }

        interlocutorCards = cards
        presenter!!.turnOnMessageListener(chatThread!!)
        lockSendUi(false)
    }

    override fun onSearchError(t: Throwable) {
        val error =
                errorResolver!!.resolve(t, object : ErrorResolver.ErrorNotImplementedHandler {
                    override fun onCustomError(resolvedError: String?): String? {
                        return if (t is ServiceException)
                            t.message
                            ?: "No error message from service."
                        else
                            null
                    }

                }) // If we can't resolve error here -
                   // then it's normal behaviour. Proceed.

        if (error != null) {
            UiUtils.toast(this, error)
            presenter!!.disposeAll()
        }

        lockSendUi(false)

        showProgress(false)
    }

    private fun showProgress(show: Boolean) {
        pbLoading!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    override fun onGetMessagesSuccess(messages: MutableList<Message>) {
        lockSendUi(false)
        srlRefresh!!.isRefreshing = false

        showProgress(false)
        messages.sortWith(Comparator { o1, o2 -> o1.messageId.compareTo(o2.messageId) })
        adapter!!.setItems(messages)
        chatThread!!.messagesCount = messages.size.toLong()

        if (rvChat!!.adapter!!.itemCount > THRESHOLD_SCROLL) {
            rvChat!!.postDelayed({ rvChat!!.smoothScrollToPosition(adapter!!.itemCount - 1) }, 400)
        }

    }

    override fun onGetMessagesError(t: Throwable) {
        lockSendUi(false)
        srlRefresh!!.isRefreshing = false

        showProgress(false)

        val err: String? = errorResolver!!.resolve(t)
        UiUtils.toast(this, err ?: t.message ?: "No error message")
    }

    companion object {

        private val THRESHOLD_SCROLL = 1
    }
}
