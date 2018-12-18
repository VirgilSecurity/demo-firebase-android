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

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import butterknife.BindView
import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread
import com.android.virgilsecurity.virgilonfire.ui.CompleteInteractor
import com.android.virgilsecurity.virgilonfire.ui.base.BaseFragmentDi
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivity
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver
import com.android.virgilsecurity.virgilonfire.util.UiUtils
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
 * ThreadsListFragment class.
 */
class ThreadsListFragment
    : BaseFragmentDi<ChatControlActivity>(),
        DataReceivedInteractor<MutableList<ChatThread>>,
        CompleteInteractor<ThreadListFragmentPresenterReturnTypes> {

    @Inject
    protected var adapter: ThreadsListRVAdapter? = null
    @Inject
    protected var presenter: ThreadsListFragmentPresenter? = null
    @Inject
    protected var errorResolver: ErrorResolver? = null

    @BindView(R.id.rvContacts)
    protected var rvContacts: RecyclerView? = null
    @BindView(R.id.pbLoading)
    internal var pbLoading: View? = null
    @BindView(R.id.tvEmpty)
    internal var tvEmpty: View? = null
    @BindView(R.id.srlRefresh)
    internal var srlRefresh: SwipeRefreshLayout? = null

    private var interlocutor: String? = null
    private var isCreateNewThread: Boolean = false

    protected override val layout: Int
        get() = R.layout.fragment_threads_list

    override fun postButterInit() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = false
        rvContacts!!.layoutManager = layoutManager
        rvContacts!!.adapter = adapter
        adapter!!.setClickListener(object : ThreadsListRVAdapter.ClickListener {
            override fun onItemClicked(position: Int, thread: ChatThread) {
                rootActivity!!.changeFragmentWithThread(ChatControlActivity.ChatState.THREAD,
                                                        thread)
            }
        })
        srlRefresh!!.setOnRefreshListener {
            presenter!!.turnOffThreadsListener()
            presenter!!.turnOnThreadsListener()
        }
    }

    override fun onResume() {
        super.onResume()

        presenter!!.turnOnThreadsListener()
    }

    override fun onPause() {
        super.onPause()

        presenter!!.turnOffThreadsListener()
    }

    override fun onDetach() {
        super.onDetach()

        presenter!!.disposeAll()
    }

    fun disposeAll() {

    }

    override fun onDataReceived(receivedData: MutableList<ChatThread>) {
        srlRefresh!!.isRefreshing = false

        receivedData.sortWith(Comparator { o1, o2 -> o1.receiver.compareTo(o2.receiver) })
        adapter!!.setItems(receivedData)
        rootActivity!!.showBaseLoading(false)
        showProgress(false)

        if (isCreateNewThread) {
            isCreateNewThread = false
            rootActivity!!.changeFragmentWithThread(ChatControlActivity.ChatState.THREAD,
                                                    adapter!!.getItemById(interlocutor!!))
        }

        if (receivedData.isEmpty())
            tvEmpty!!.visibility = View.VISIBLE
        else
            tvEmpty!!.visibility = View.INVISIBLE
    }

    override fun onDataReceivedError(t: Throwable) {
        srlRefresh!!.isRefreshing = false

        val err: String? = errorResolver!!.resolve(t)
        UiUtils.toast(this, err ?: t.message ?: "No error message")
    }

    private fun showProgress(show: Boolean) {
        pbLoading!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    fun issueCreateThread(interlocutor: String?) {
        if (interlocutor == null || interlocutor.isEmpty())
            throw IllegalArgumentException("Interlocutor should not be null or empty")

        this.interlocutor = interlocutor
        presenter!!.requestCreateThread(interlocutor)

        isCreateNewThread = true
    }

    override fun onComplete(type: ThreadListFragmentPresenterReturnTypes) {
        when (type) {
            ThreadListFragmentPresenterReturnTypes.CREATE_THREAD -> {
                tvEmpty!!.visibility = View.GONE
                rootActivity!!.newThreadDialogDismiss()
            }
            ThreadListFragmentPresenterReturnTypes.REMOVE_CHAT_THREAD ->
                rootActivity!!.newThreadDialogShowProgress(false)
        }
    }

    override fun onError(t: Throwable) {
        presenter!!.requestRemoveThread(interlocutor!!)
        val err: String? = errorResolver!!.resolve(t)
        UiUtils.toast(this, err ?: t.message ?: "No error message")
    }
}
