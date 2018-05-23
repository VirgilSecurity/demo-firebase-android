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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultUser;
import com.android.virgilsecurity.virgilonfire.data.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    4/16/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

public class ThreadsListRVAdapter extends RecyclerView.Adapter<ThreadsListRVAdapter.ThreadHolder> {

    private List<DefaultChatThread> items;
    private ClickListener clickListener;

    ThreadsListRVAdapter() {
        items = Collections.emptyList();
    }

    @Override
    public ThreadHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.item_list_threads, parent, false);

        return new ThreadHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(ThreadHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : -1;
    }

    void setItems(List<DefaultChatThread> items) {
        if (items != null) {
            items.removeAll(this.items);
            this.items = new ArrayList<>(items);
        } else {
            this.items = Collections.emptyList();
        }

        notifyDataSetChanged();
    }

    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ChatThread getItemById(String interlocutor) {
        for (ChatThread thread : items) {
            if (thread.getReceiver().equals(interlocutor))
                return thread;
        }

        return null;
    }

    static class ThreadHolder extends RecyclerView.ViewHolder {

        private ClickListener listener;

        @BindView(R.id.rlItemRoot)
        View rlItemRoot;
        @BindView(R.id.ivUserPhoto)
        ImageView ivUserPhoto;
        @BindView(R.id.tvUsername)
        TextView tvUsername;

        ThreadHolder(View view, ClickListener listener) {
            super(view);

            ButterKnife.bind(this, view);
            this.listener = listener;
        }

        void bind(ChatThread thread) {
            tvUsername.setText(thread.getReceiver());

            rlItemRoot.setOnClickListener((v) -> listener.onItemClicked(getAdapterPosition(),
                                                                        thread));
        }
    }

    public interface ClickListener {

        void onItemClicked(int position, ChatThread thread);
    }
}
