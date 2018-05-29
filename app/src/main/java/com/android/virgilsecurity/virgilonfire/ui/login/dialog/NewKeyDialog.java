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

package com.android.virgilsecurity.virgilonfire.ui.login.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.virgilsecurity.virgilonfire.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Danylo Oliinyk on 11/26/17 at Virgil Security.
 * -__o
 */

public class NewKeyDialog extends Dialog {

    private OnCreateNewKeysListener onCreateNewKeysListener;
    @Nullable private String title;
    private String message;

    @BindView(R.id.flRoot) protected FrameLayout flRoot;
    @BindView(R.id.llContentRoot) protected View llContentRoot;
    @BindView(R.id.tvTitle) protected TextView tvTitle;
    @BindView(R.id.tvMessage) protected TextView tvMessage;

    public NewKeyDialog(@NonNull Context context, @NonNull String message) {
        super(context);

        this.message = message;
    }

    public NewKeyDialog(@NonNull Context context, int themeResId,
                        @Nullable String title, @NonNull String message) {
        super(context, themeResId);

        this.title = title;
        this.message = message;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_key);
        setCancelable(true);
        ButterKnife.bind(this);

        if (title != null)
            tvTitle.setText(title);

        tvMessage.setText(message);
    }

    @OnClick({R.id.btnCancel, R.id.btnOk})
    void onInterfaceClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                onCreateNewKeysListener.onCancelCreateNewKeys(v, this);
                break;
            case R.id.btnOk:
                onCreateNewKeysListener.onCreateNewKeys(v, this);
                break;
        }
    }

    public void setOnNewKeysDialogListener(OnCreateNewKeysListener onCreateNewKeysListener) {
        this.onCreateNewKeysListener = onCreateNewKeysListener;
    }

    public interface OnCreateNewKeysListener {
        void onCreateNewKeys(View v, Dialog dialog);

        void onCancelCreateNewKeys(View v, Dialog dialog);
    }
}
