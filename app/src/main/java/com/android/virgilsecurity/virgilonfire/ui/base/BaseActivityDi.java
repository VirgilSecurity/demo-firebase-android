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

package com.android.virgilsecurity.virgilonfire.ui.base;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.virgilsecurity.virgilonfire.R;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Danylo Oliinyk on 16.11.17 at Virgil Security.
 * -__o
 */

public abstract class BaseActivityDi extends AppCompatActivity {

    private TextView tvToolbarTitle;
    private View ibToolbarBack;
    private View ibToolbarHamburger;
    @Nullable private Toolbar toolbar;
    private View llBaseLoadingTextNoNetwork;
    private View llBaseLoading;

    protected abstract int getLayout();

    protected abstract void postButterInit();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        View baseView = inflater.inflate(R.layout.activity_base, null);

        FrameLayout flBaseContainer = baseView.findViewById(R.id.flBaseContainer);
        llBaseLoading = baseView.findViewById(R.id.llBaseLoading);
        llBaseLoadingTextNoNetwork = baseView.findViewById(R.id.tvBaseNoNetwork);

        View childView = inflater.inflate(getLayout(), null);
        flBaseContainer.removeAllViews();
        flBaseContainer.addView(childView);

        setContentView(baseView);

        ButterKnife.bind(this);

        postButterInit();
    }

    protected final void changeToolbarTitle(String titlePage) {
        if (toolbar != null) {
            tvToolbarTitle.setText(titlePage);
        } else {
            throw new NullPointerException("Init Toolbar first");
        }
    }

    protected final void initToolbar(Toolbar toolbar, String titlePage) {
        this.toolbar = toolbar;
        this.tvToolbarTitle = toolbar.findViewById(R.id.tvToolbarTitle);
        this.ibToolbarBack = toolbar.findViewById(R.id.ibToolbarBack);
        this.ibToolbarHamburger = toolbar.findViewById(R.id.ibToolbarHamburger);

        setSupportActionBar(toolbar);

        tvToolbarTitle.setText(titlePage);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");
    }

    protected final void showBackButton(boolean show, @Nullable View.OnClickListener listener) {
        if (show) {
            ibToolbarBack.setVisibility(View.VISIBLE);
            ibToolbarBack.setOnClickListener(listener);
        } else {
            ibToolbarBack.setVisibility(View.INVISIBLE);
        }
    }

    protected final void showHamburger(boolean show, @Nullable View.OnClickListener listener) {
        if (show) {
            ibToolbarHamburger.setVisibility(View.VISIBLE);
            ibToolbarHamburger.setOnClickListener(listener);
        } else {
            ibToolbarHamburger.setVisibility(View.INVISIBLE);
        }
    }

    protected final void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                       .debounce(1000, TimeUnit.MILLISECONDS)
                       .distinctUntilChanged()
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribe((connectivity) -> {
                           showNoNetwork(!(connectivity.getState() == NetworkInfo.State.CONNECTED));
                       });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showNoNetwork(boolean show) {
        showBaseLoading(show);
        llBaseLoadingTextNoNetwork.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showBaseLoading(boolean show) {
        llBaseLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        llBaseLoading.requestFocus();
    }
}