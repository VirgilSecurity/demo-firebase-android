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

package com.android.virgilsecurity.virgilonfire.data.virgil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.android.virgilsecurity.virgilonfire.JwtExampleApp;
import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.data.model.DefaultToken;
import com.android.virgilsecurity.virgilonfire.data.model.TokenResponse;
import com.android.virgilsecurity.virgilonfire.data.model.exception.ServiceException;
import com.android.virgilsecurity.virgilonfire.data.remote.ServiceHelper;
import com.android.virgilsecurity.virgilonfire.util.UiUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.virgilsecurity.sdk.jwt.TokenContext;
import com.virgilsecurity.sdk.jwt.accessProviders.CallbackJwtProvider;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

/**
 * Created by Danylo Oliinyk on 3/23/18 at Virgil Security.
 * -__o
 */

public class GetTokenCallbackImpl implements CallbackJwtProvider.GetTokenCallback {

    private final ServiceHelper helper;
    private final UserManager userManager;
    private final FirebaseAuth firebaseAuth;
    private final Context context;

    public GetTokenCallbackImpl(ServiceHelper helper,
                                UserManager userManager,
                                FirebaseAuth firebaseAuth,
                                Context context) {
        this.helper = helper;
        this.userManager = userManager;
        this.firebaseAuth = firebaseAuth;
        this.context = context;
    }

    @Override public String onGetToken(TokenContext tokenContext) {
        try {
            Response<TokenResponse> response = helper.getToken(userManager.getToken(),
                                                               firebaseAuth.getCurrentUser()
                                                                           .getEmail()
                                                                           .toLowerCase()
                                                                           .split("@")[0])
                                                     .execute();

            if (response.errorBody() != null && response.code() == 401) {
//                new Handler(Looper.getMainLooper()).post(() -> {
//                    UiUtils.toast(context, "Session is ended. Re-signIn please to refresh your token.");
//                });

                ExecutorService executor = Executors.newSingleThreadExecutor();

                Task<GetTokenResult> getTokenResultTask = firebaseAuth.getCurrentUser().getIdToken(true);
                getTokenResultTask.addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful())
                        userManager.setToken(new DefaultToken(task.getResult().getToken()));
                });

                try {
                    executor.awaitTermination(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                response = helper.getToken(userManager.getToken(),
                                           firebaseAuth.getCurrentUser()
                                                       .getEmail()
                                                       .toLowerCase()
                                                       .split("@")[0])
                                 .execute();
            }

            return response.body().getToken();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            throw new ServiceException("Failed on get token");
        }
    }
}
