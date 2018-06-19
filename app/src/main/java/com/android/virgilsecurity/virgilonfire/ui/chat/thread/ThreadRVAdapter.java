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

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultMessage;
import com.android.virgilsecurity.virgilonfire.data.model.Message;
import com.android.virgilsecurity.virgilonfire.data.virgil.VirgilHelper;
import com.google.firebase.auth.FirebaseAuth;

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

public class ThreadRVAdapter extends RecyclerView.Adapter<ThreadRVAdapter.HolderMessage> {

    @IntDef({MessageType.ME, MessageType.YOU})
    private @interface MessageType {
        int ME = 0;
        int YOU = 1;
    }

    private final VirgilHelper virgilHelper;
    private final FirebaseAuth firebaseAuth;
    private List<DefaultMessage> items;

    @Inject ThreadRVAdapter(VirgilHelper virgilHelper,
                            FirebaseAuth firebaseAuth) {
        this.virgilHelper = virgilHelper;
        this.firebaseAuth = firebaseAuth;

        items = Collections.emptyList();
    }

    @Override
    public HolderMessage onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        HolderMessage viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case MessageType.ME:
                viewHolder = new HolderMessage(inflater.inflate(R.layout.layout_holder_me,
                                                                viewGroup,
                                                                false));
                break;
            case MessageType.YOU:
                viewHolder = new HolderMessage(inflater.inflate(R.layout.layout_holder_you,
                                                                viewGroup,
                                                                false));
                break;
            default:
                viewHolder = new HolderMessage(inflater.inflate(R.layout.layout_holder_me,
                                                                viewGroup,
                                                                false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HolderMessage viewHolder, int position) {
        viewHolder.bind(virgilHelper.decrypt(items.get(position)
                                                  .getBody()));
    }

    @Override public int getItemViewType(int position) {
        if (items.get(position)
                 .getSender()
                 .equals(firebaseAuth.getCurrentUser()
                                     .getEmail()
                                     .toLowerCase()
                                     .split("@")[0])) {
            return MessageType.ME;
        } else {
            return MessageType.YOU;
        }
    }

    @Override public int getItemCount() {
        return items != null ? items.size() : -1;
    }

    void setItems(List<DefaultMessage> items) {
        if (items != null) {
            items.removeAll(this.items);
            this.items = new ArrayList<>(items);
        } else {
            this.items = Collections.emptyList();
        }

        notifyDataSetChanged();
    }

    void addItem(DefaultMessage item) {
        if (items == null || items.isEmpty())
            items = new ArrayList<>();

        items.add(item);
        notifyDataSetChanged();
    }

    public void clearItems() {
        if (items != null || !items.isEmpty())
            items.clear();
    }

    static class HolderMessage extends RecyclerView.ViewHolder {

        @BindView(R.id.tvMessage) TextView tvMessage;

        HolderMessage(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        void bind(String message) {
            tvMessage.setText(message);
        }
    }
}
