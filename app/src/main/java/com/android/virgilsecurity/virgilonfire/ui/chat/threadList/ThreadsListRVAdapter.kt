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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread
import java.util.*

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
 * ThreadsListRVAdapter class.
 */
class ThreadsListRVAdapter internal constructor() : RecyclerView.Adapter<ThreadsListRVAdapter.ThreadHolder>() {

    private var items: List<ChatThread>? = null
    private var clickListener: ClickListener? = null

    init {
        items = emptyList<ChatThread>()
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ThreadHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_threads, parent, false)

        return ThreadHolder(view, clickListener!!)
    }

    override fun onBindViewHolder(holder: ThreadHolder, position: Int) {
        holder.bind(items!![position])
    }

    override fun getItemCount(): Int {
        return if (items != null) items!!.size else -1
    }

    internal fun setItems(items: MutableList<ChatThread>?) {
        if (items != null) {
            items.removeAll(this.items!!)
            this.items = ArrayList(items)
        } else {
            this.items = emptyList()
        }

        notifyDataSetChanged()
    }

    internal fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    fun getItemById(interlocutor: String): ChatThread? {
        for (thread in items!!) {
            if (thread.receiver == interlocutor)
                return thread
        }

        return null
    }

    class ThreadHolder(view: View, private val listener: ClickListener) : RecyclerView.ViewHolder(
        view) {

        @BindView(R.id.rlItemRoot)
        var rlItemRoot: View? = null
        @BindView(R.id.ivUserPhoto)
        var ivUserPhoto: ImageView? = null
        @BindView(R.id.tvUsername)
        var tvUsername: TextView? = null

        init {

            ButterKnife.bind(this, view)
        }

        fun bind(thread: ChatThread) {
            tvUsername!!.text = thread.receiver

            rlItemRoot!!.setOnClickListener { v ->
                listener.onItemClicked(adapterPosition,
                                       thread)
            }
        }
    }

    interface ClickListener {

        fun onItemClicked(position: Int, thread: ChatThread)
    }
}
