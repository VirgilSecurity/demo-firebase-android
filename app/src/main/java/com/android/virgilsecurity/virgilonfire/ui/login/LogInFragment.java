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

package com.android.virgilsecurity.virgilonfire.ui.login;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.InputFilter;
import android.view.View;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultToken;
import com.android.virgilsecurity.virgilonfire.data.model.exception.ServiceException;
import com.android.virgilsecurity.virgilonfire.ui.base.BaseFragmentDi;
import com.android.virgilsecurity.virgilonfire.ui.login.dialog.NewKeyDialog;
import com.android.virgilsecurity.virgilonfire.util.DefaultSymbolsInputFilter;
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver;
import com.android.virgilsecurity.virgilonfire.util.UiUtils;
import com.android.virgilsecurity.virgilonfire.util.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.virgilsecurity.sdk.cards.Card;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;

import static com.android.virgilsecurity.virgilonfire.di.InjectionConstants.REQUEST_ID_TOKEN;

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

public final class LogInFragment
        extends BaseFragmentDi<LogInActivity>
        implements LogInVirgilInteractor, LogInKeyStorageInteractor {

    private static final String REQUEST_SERVER_AUTH_CODE = "snubKcPLscvA9owuCtlAZAOv";
    private static final String ALLOWED_SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,-()/='+:?!%&*<>;{}@#_";

    private static final int RC_SIGN_IN = 42;

    @Inject protected FirebaseAuth firebaseAuth;
    @Inject protected LogInPresenter presenter;
    @Inject protected UserManager userManager;
    @Inject protected ErrorResolver errorResolver;
    @Inject @Named(REQUEST_ID_TOKEN) @Nullable protected String requestIdToken;

    @BindView(R.id.etEmail)
    protected TextInputEditText etEmail;
    @BindView(R.id.etPassword)
    protected TextInputEditText etPassword;
    @BindView(R.id.btnSignIn)
    protected View btnSignIn;
    @BindView(R.id.btnSignUp)
    protected View btnSignUp;

    public static LogInFragment newInstance() {

        Bundle args = new Bundle();

        LogInFragment fragment = new LogInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override protected int getLayout() {
        return R.layout.fragment_log_in;
    }

    @Override protected void postButterInit() {
        initInputFields();
        initFirebaseAuth();
    }

    private void initInputFields() {
        etEmail.setFilters(new InputFilter[]{new DefaultSymbolsInputFilter()});
        etPassword.setFilters(new InputFilter[]{new DefaultSymbolsInputFilter()});
    }


    private void initFirebaseAuth() {
        btnSignIn.setOnClickListener(v -> {
            String error;

            error = Validator.validate(etEmail, Validator.FieldType.EMAIL);
            if (error != null) {
                etEmail.setError(error);
                return;
            }

            error = Validator.validate(etPassword, Validator.FieldType.PASSWORD);
            if (error != null) {
                etPassword.setError(error);
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(etEmail.getText()
                                                               .toString(),
                                                        etPassword.getText()
                                                                  .toString())
                        .addOnCompleteListener(this::handleSignInResult);
        });

        btnSignUp.setOnClickListener(v -> {
            String error;

            error = Validator.validate(etEmail, Validator.FieldType.EMAIL);
            if (error != null) {
                etEmail.setError(error);
                return;
            }

            error = Validator.validate(etPassword, Validator.FieldType.PASSWORD);
            if (error != null) {
                etPassword.setError(error);
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(etEmail.getText()
                                                               .toString(),
                                                        etPassword.getText()
                                                                  .toString())
                        .addOnCompleteListener(this::handleSignInResult);
        });
    }

    private void handleSignInResult(Task<AuthResult> task) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            presenter.requestSearchCards(user.getEmail());
        } else {
            String error = errorResolver.resolve(task.getException());
            if (error == null && task.getException() != null)
                error = task.getException()
                            .getMessage();

            UiUtils.toast(this, error == null ? "Try again" : error);
        }
    }

    @Override public void onSearchCardSuccess(List<Card> cards) {
        presenter.requestIfKeyExists(cards.get(0)
                                          .getIdentity());
    }

    @SuppressLint("RestrictedApi") @Override public void onSearchCardError(Throwable t) {
        String error = errorResolver.resolve(t, resolvedError -> {
            if (t instanceof ServiceException)
                return t.getMessage();

            return null;
        }); // If we can't resolve error here -
        // then it's normal behaviour. Proceed.
        if (error != null) {
            UiUtils.toast(this, error);
            firebaseAuth.signOut();
            presenter.disposeAll();

            return;
        }

        presenter.requestPublishCard(firebaseAuth.getCurrentUser()
                                                 .getEmail().toLowerCase());
    }

    @Override public void onPublishCardSuccess(Card card) {
        userManager.setUserCard(card);
        firebaseAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userManager.setToken(new DefaultToken(task.getResult().getToken()));

                activity.startChatControlActivity(firebaseAuth.getCurrentUser()
                                                              .getEmail().toLowerCase());
            } else {
                String error = errorResolver.resolve(task.getException());
                if (error == null && task.getException() != null)
                    error = task.getException()
                                .getMessage();

                UiUtils.toast(this, error == null ? "Error getting token" : error);
            }
        });
    }

    @Override public void onPublishCardError(Throwable t) {
        UiUtils.toast(this, errorResolver.resolve(t));
    }

    @Override public void onKeyExists() {
        firebaseAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userManager.setToken(new DefaultToken(task.getResult().getToken()));

                activity.startChatControlActivity(firebaseAuth.getCurrentUser()
                                                              .getEmail().toLowerCase());
            } else {
                String error = errorResolver.resolve(task.getException());
                if (error == null && task.getException() != null)
                    error = task.getException()
                                .getMessage();

                UiUtils.toast(this, error == null ? "Error getting token" : error);
            }
        });
    }

    @Override public void onKeyNotExists() {
        presenter.disposeAll();
        NewKeyDialog newKeyDialog = new NewKeyDialog(activity,
                                                     R.style.NotTransBtnsDialogTheme,
                                                     getString(R.string.new_private_key),
                                                     getString(R.string.new_private_key_generation));

        newKeyDialog.setOnNewKeysDialogListener(() -> {
            presenter.requestPublishCard(firebaseAuth.getCurrentUser()
                                                     .getEmail().toLowerCase());
        });
    }

    @Override public void onStop() {
        super.onStop();

        presenter.disposeAll();
    }
}
