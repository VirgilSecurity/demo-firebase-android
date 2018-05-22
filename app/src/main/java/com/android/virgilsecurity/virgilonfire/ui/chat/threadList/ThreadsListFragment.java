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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread;
import com.android.virgilsecurity.virgilonfire.ui.CompleteInteractor;
import com.android.virgilsecurity.virgilonfire.ui.base.BaseFragmentDi;
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivity;
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor;
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver;
import com.android.virgilsecurity.virgilonfire.util.UiUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

public class ThreadsListFragment extends BaseFragmentDi<ChatControlActivity>
        implements DataReceivedInteractor<List<DefaultChatThread>>,
        CompleteInteractor<ThreadListFragmentPresenterReturnTypes> {

    @Inject protected ThreadsListRVAdapter adapter;
    @Inject protected ThreadsListFragmentPresenter presenter;
    @Inject protected ErrorResolver errorResolver;

    @BindView(R.id.rvContacts) protected RecyclerView rvContacts;
    @BindView(R.id.pbLoading) View pbLoading;
    @BindView(R.id.tvEmpty) View tvEmpty;

    private String interlocutor;

    @Override protected int getLayout() {
        return R.layout.fragment_threads_list;
    }

    @Override protected void postButterInit() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setReverseLayout(false);
        rvContacts.setLayoutManager(layoutManager);
        rvContacts.setAdapter(adapter);
        adapter.setClickListener((position, thread) -> {
            activity.changeFragmentWithData(ChatControlActivity.ChatState.THREAD, thread);
        });
        presenter.requestThreadsList();
    }

    @Override public void onResume() {
        super.onResume();

        activity.changeToolbarTitleExposed(getString(R.string.app_name));
    }

    @Override public void onDetach() {
        super.onDetach();

        presenter.disposeAll();
    }

    public void disposeAll() {

    }

    @Override public void onDataReceived(List<DefaultChatThread> receivedData) {
        adapter.setItems(receivedData);
        activity.showBaseLoading(false);
        showProgress(false);

        if (receivedData.isEmpty())
            tvEmpty.setVisibility(View.VISIBLE);
    }

    @Override public void onDataReceivedError(Throwable t) {
        String err = errorResolver.resolve(t);
        UiUtils.toast(this, err != null ? err : t.getMessage());
    }

    private void showProgress(boolean show) {
        pbLoading.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void issueCreateThread(String interlocutor) {
        if (interlocutor == null || interlocutor.isEmpty())
            throw new IllegalArgumentException("Interlocutor should not be null or empty");

        this.interlocutor = interlocutor;
        presenter.requestCreateThread(interlocutor);
    }

    @Override public void onComplete(ThreadListFragmentPresenterReturnTypes type) {
        switch (type) {
            case CREATE_THREAD:
                tvEmpty.setVisibility(View.GONE);
                activity.newThreadDialogDismiss();
                activity.changeFragmentWithData(ChatControlActivity.ChatState.THREAD, interlocutor);
                break;
        }
    }

    @Override public void onError(Throwable t) {
        activity.newThreadDialogShowProgress(false);
        String err = errorResolver.resolve(t);
        UiUtils.toast(this, err != null ? err : t.getMessage());
    }
}
