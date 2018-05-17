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

package com.android.virgilsecurity.virgilonfire.ui.chat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.StringDef;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.TextView;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.ui.base.BaseActivityDi;
import com.android.virgilsecurity.virgilonfire.ui.chat.thread.ThreadFragment;
import com.android.virgilsecurity.virgilonfire.ui.chat.threadList.ThreadsListFragment;
import com.android.virgilsecurity.virgilonfire.ui.login.LogInActivity;
import com.android.virgilsecurity.virgilonfire.util.UiUtils;
import com.android.virgilsecurity.virgilonfire.util.common.OnFinishTimer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;

import static com.android.virgilsecurity.virgilonfire.di.InjectionConstants.REQUEST_ID_TOKEN;

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

public class ChatControlActivity extends BaseActivityDi implements HasFragmentInjector {

    public static final String USERNAME = "USERNAME";

    private ThreadsListFragment threadsListFragment;
    private ThreadFragment threadFragment;
    private boolean secondPress;

    @Inject protected DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject UserManager userManager;
    @Inject @Named(REQUEST_ID_TOKEN) @Nullable String requestIdToken;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.nvNavigation)
    protected NavigationView nvNavigation;
    @BindView(R.id.dlDrawer)
    protected DrawerLayout dlDrawer;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ChatState.THREADS_LIST, ChatState.THREAD})
    public @interface ChatState {
        String THREADS_LIST = "THREADS_LIST";
        String THREAD = "THREAD";
    }

    public static void start(Activity from) {
        from.startActivity(new Intent(from, ChatControlActivity.class));
    }

    public static void startWithFinish(Activity from) {
        from.startActivity(new Intent(from, ChatControlActivity.class));
        from.finish();
    }

    public static void startWithFinish(Activity from, String username) {
        from.startActivity(new Intent(from, ChatControlActivity.class).putExtra(USERNAME,
                                                                                username));
        from.finish();
    }

    @Override protected int getLayout() {
        return R.layout.activity_chat_control;
    }

    @Override protected void postButterInit() {
        initToolbar(toolbar, getString(R.string.threads_list_name));
        initDrawer();

        threadsListFragment = (ThreadsListFragment) getFragmentManager().findFragmentById(R.id.threadsListFragment);
        threadFragment = (ThreadFragment) getFragmentManager().findFragmentById(R.id.threadFragment);

        changeFragment(ChatState.THREADS_LIST);

        hideKeyboard();
    }

    public void changeFragment(@ChatState String tag) {
        changeFragmentWithData(tag, null);
    }

    public void changeFragmentWithData(@ChatState String tag, @Nullable String data) {
        if (tag.equals(ChatState.THREADS_LIST)) {
            showBackButton(false, (view) -> onBackPressed());
            showHamburger(true, view -> {
                if (!dlDrawer.isDrawerOpen(Gravity.START))
                    dlDrawer.openDrawer(Gravity.START);
                else
                    dlDrawer.closeDrawer(Gravity.START);
            });

            UiUtils.hideFragment(getFragmentManager(), threadFragment);
            UiUtils.showFragment(getFragmentManager(), threadsListFragment);
        } else {
            if (data != null)
                threadFragment.setInterlocutorName(data);

            showBackButton(true, (view) -> onBackPressed());
            showHamburger(false, (view) -> {});

            UiUtils.hideFragment(getFragmentManager(), threadsListFragment);
            UiUtils.showFragment(getFragmentManager(), threadFragment);
        }
    }

    public void changeToolbarTitleExposed(String text) {
        changeToolbarTitle(text);
    }

    private void initDrawer() {
        TextView tvUsernameDrawer =
                nvNavigation.getHeaderView(0)
                            .findViewById(R.id.tvUsernameDrawer);
        tvUsernameDrawer.setText(userManager.getCurrentUser()
                                            .getName());

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        nvNavigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.itemLogOut:
                    dlDrawer.closeDrawer(Gravity.START);

                    showBaseLoading(true);

                    userManager.clearCurrentUser();
                    userManager.clearUserCard();

                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(requestIdToken)
                            .requestEmail()
                            .build();

                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                    googleSignInClient.signOut();

                    threadFragment.disposeAll();
                    threadsListFragment.disposeAll();
                    LogInActivity.startClearTop(this);
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override public void onBackPressed() {

        if (threadFragment.isVisible()) {
            changeFragmentWithData(ChatState.THREADS_LIST, getString(R.string.threads_list_name));
            return;
        }

        if (secondPress)
            super.onBackPressed();
        else
            UiUtils.toast(this, getString(R.string.press_exit_once_more));

        secondPress = true;

        new OnFinishTimer(2000, 100) {

            @Override public void onFinish() {
                secondPress = false;
            }
        }.start();
    }

    @Override public AndroidInjector<Fragment> fragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
