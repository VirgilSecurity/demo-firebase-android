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

package com.android.virgilsecurity.virgilonfire.ui.chat.threadList.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.util.DefaultSymbolsInputFilter;
import com.android.virgilsecurity.virgilonfire.util.Validator;
import com.android.virgilsecurity.virgilonfire.util.common.OnFinishTimer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.internal.Utils;

/**
 * Created by Danylo Oliinyk on 11/26/17 at Virgil Security.
 * -__o
 */

public class CreateThreadDialog extends Dialog {

    private OnCreateThreadDialogListener onCreateThreadDialogListener;
    @Nullable private String title;
    private String message;

    @BindView(R.id.flRoot) FrameLayout flRoot;
    @BindView(R.id.llContentRoot) View llContentRoot;
    @BindView(R.id.llLoadingRoot) View llLoadingRoot;
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvMessage) TextView tvMessage;
    @BindView(R.id.etUsername) EditText etUsername;

    public CreateThreadDialog(@NonNull Context context, @NonNull String message) {
        super(context);

        this.message = message;
    }

    public CreateThreadDialog(@NonNull Context context, int themeResId,
                              @Nullable String title, @NonNull String message) {
        super(context, themeResId);

        this.title = title;
        this.message = message;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_thread);
        setCancelable(true);
        ButterKnife.bind(this);

        etUsername.setFilters(new InputFilter[]{new DefaultSymbolsInputFilter()});
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                etUsername.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        if (title != null)
            tvTitle.setText(title);

        tvMessage.setText(message);
    }

    @OnClick({R.id.btnCancel, R.id.btnOk})
    void onInterfaceClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                cancel();
                break;
            case R.id.btnOk:
                String error;

                error = Validator.validate(etUsername, Validator.FieldType.ID_WITH_NO_AT);
                if (error != null) {
                    etUsername.setError(error);
                    break;
                }

                onCreateThreadDialogListener.onCreateThread(etUsername.getText().toString());
                break;
        }
    }

    public void setOnCreateThreadDialogListener(OnCreateThreadDialogListener onCreateThreadDialogListener) {
        this.onCreateThreadDialogListener = onCreateThreadDialogListener;
    }

    public void showProgress(boolean show) {
        if (show) {
            setCancelable(false);
            llContentRoot.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(flRoot);
            llLoadingRoot.setVisibility(View.VISIBLE);
        } else {
            new OnFinishTimer(1000, 100) {
                @Override public void onFinish() {
                    setCancelable(true);
                    llLoadingRoot.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(flRoot);
                    llContentRoot.setVisibility(View.VISIBLE);
                }
            }.start();
        }
    }

    public interface OnCreateThreadDialogListener {
        void onCreateThread(String username);
    }
}
