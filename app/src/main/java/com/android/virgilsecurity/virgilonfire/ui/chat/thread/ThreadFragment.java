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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultChatThread;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultMessage;
import com.android.virgilsecurity.virgilonfire.data.model.Message;
import com.android.virgilsecurity.virgilonfire.data.model.exception.CardsNotFoundException;
import com.android.virgilsecurity.virgilonfire.data.model.exception.ServiceException;
import com.android.virgilsecurity.virgilonfire.ui.base.BaseFragmentDi;
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivity;
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor;
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver;
import com.android.virgilsecurity.virgilonfire.util.UiUtils;
import com.android.virgilsecurity.virgilonfire.util.UserUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.virgilsecurity.sdk.cards.Card;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

public class ThreadFragment extends BaseFragmentDi<ChatControlActivity>
        implements DataReceivedInteractor<Message>, OnMessageSentInteractor, SearchCardsInteractor,
        GetMessagesInteractor {

    private static final int THRESHOLD_SCROLL = 1;

    @Inject protected ThreadRVAdapter adapter;
    @Inject protected ThreadFragmentPresenter presenter;
    @Inject protected ErrorResolver errorResolver;
    @Inject protected UserManager userManager;
    @Inject protected FirebaseAuth firebaseAuth;

    private ChatThread chatThread;
    private List<Card> interlocutorCards;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.rvChat) protected RecyclerView rvChat;
    @BindView(R.id.etMessage) EditText etMessage;
    @BindView(R.id.btnSend) ImageButton btnSend;
    @BindView(R.id.tvEmpty) View tvEmpty;
    @BindView(R.id.tvError) View tvError;
    @BindView(R.id.pbLoading) View pbLoading;
    @BindView(R.id.srlRefresh) SwipeRefreshLayout srlRefresh;

    @Override protected int getLayout() {
        return R.layout.fragment_thread;
    }
    @Override protected void postButterInit() {
        rvChat.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setReverseLayout(false);
        rvChat.setLayoutManager(layoutManager);
        initMessageInput();
        rvChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                if (rvChat.getAdapter().getItemCount() > THRESHOLD_SCROLL) {
                    rvChat.postDelayed(() -> rvChat.smoothScrollToPosition(
                            rvChat.getAdapter().getItemCount() - 1), 100);
                }
            }
        });
        srlRefresh.setOnRefreshListener(() -> {
//            presenter.turnOffMessageListener();
            presenter.disposeAll();
            presenter.turnOnMessageListener(chatThread);
        });
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden)
            presenter.disposeAll();
    }

    public void disposeAll() {
        presenter.disposeAll();
    }

    @Override public void onDataReceived(Message receivedData) {
        adapter.addItem((DefaultMessage) receivedData);
        activity.showBaseLoading(false);
        showProgress(false);
    }

    @Override public void onDataReceivedError(Throwable t) {
        lockSendUi(false);
        UiUtils.toast(this, errorResolver.resolve(t));
    }

    @Override
    public void onSendMessageSuccess() {
        lockSendUi(false);
    }

    @Override
    public void onSendMessageError(Throwable t) {
        etMessage.setText("");
        lockSendUi(false);
        UiUtils.toast(this, errorResolver.resolve(t));
    }

    private void initMessageInput() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lockSendUi(charSequence.toString().isEmpty());
            }

            @Override public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void lockSendUi(boolean lockButton) {
        if (!lockButton) {
            btnSend.setEnabled(true);
            btnSend.setBackground(ContextCompat.getDrawable(activity,
                                                            R.drawable.bg_btn_chat_send));
        } else {
            btnSend.setEnabled(false);
            btnSend.setBackground(ContextCompat.getDrawable(activity,
                                                            R.drawable.bg_btn_chat_send_pressed));
        }
    }

    @OnClick({R.id.btnSend}) void onInterfaceClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                String text = etMessage.getText().toString().trim();

                if (!text.isEmpty()) {
                    if (interlocutorCards != null) {
                        lockSendUi(true);
                        sendMessage(text);
                    } else {
                        UiUtils.toast(this, "No interlocutor cards");
                    }
                }
                break;
        }
    }

    private void sendMessage(String text) {
        lockSendUi(true);
        Message message = new DefaultMessage(UserUtils.currentIdentity(firebaseAuth),
                                             chatThread.getReceiver(),
                                             text,
                                             new Timestamp(new Date()));

        presenter.requestSendMessage(interlocutorCards, message, chatThread);
        etMessage.setText("");
    }

    public void setChatThread(@NonNull ChatThread chatThread) {
        this.chatThread = chatThread;

        showProgress(true);
        lockSendUi(true);
        activity.changeToolbarTitleExposed(this.chatThread.getReceiver());

        adapter.clearItems();
        interlocutorCards = null;
        presenter.requestSearchCards(chatThread.getReceiver());
    }

    @Override public void onSearchSuccess(List<Card> cards) {
        if (cards.size() == 0) {
            presenter.disposeAll();
            UiUtils.toast(this, R.string.no_cards_found);
            throw new CardsNotFoundException("LogInFragment -> No cards was found");
        }

        interlocutorCards = cards;
        presenter.turnOnMessageListener(chatThread);
        lockSendUi(false);
    }

    @Override public void onSearchError(Throwable t) {
        String error = errorResolver.resolve(t, resolvedError -> {
            if (t instanceof ServiceException)
                return t.getMessage();

            return null;
        }); // If we can't resolve error here -
        // then it's normal behaviour. Proceed.
        if (error != null) {
            UiUtils.toast(this, error);
            presenter.disposeAll();
        }

        lockSendUi(false);

        showProgress(false);
    }

    private void showProgress(boolean show) {
        rvChat.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        pbLoading.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override public void onGetMessagesSuccess(List<DefaultMessage> messages) {
        lockSendUi(false);
        srlRefresh.setRefreshing(false);

        messages.sort(Comparator.comparingLong(DefaultMessage::getMessageId));
        adapter.setItems(messages);
        ((DefaultChatThread) chatThread).setMessagesCount(messages.size());

        if (rvChat.getAdapter().getItemCount() > THRESHOLD_SCROLL) {
            rvChat.postDelayed(() -> {
                rvChat.scrollToPosition(adapter.getItemCount() - 1);
                showProgress(false);
            }, 400);
        }

    }

    @Override public void onGetMessagesError(Throwable t) {
        lockSendUi(false);
        srlRefresh.setRefreshing(false);

        showProgress(false);

        String err = errorResolver.resolve(t);
        UiUtils.toast(this, err != null ? err : t.getMessage());
    }
}
