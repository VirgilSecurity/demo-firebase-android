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

package com.android.virgilsecurity.virgilonfire.di;

import android.app.Application;
import android.content.Context;

import com.android.virgilsecurity.virgilonfire.R;
import com.android.virgilsecurity.virgilonfire.data.local.PropertyManager;
import com.android.virgilsecurity.virgilonfire.data.local.UserManager;
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivityComponent;
import com.android.virgilsecurity.virgilonfire.util.DefaultErrorResolver;
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.android.virgilsecurity.virgilonfire.di.InjectionConstants.REQUEST_ID_TOKEN;

/**
 * Created by Danylo Oliinyk on 3/26/18 at Virgil Security.
 * -__o
 */

@Module(subcomponents = ChatControlActivityComponent.class)
public class UtilModule {

    @Provides @Singleton static Context provideContext(Application application) {
        return application;
    }

    @Provides @Singleton static PropertyManager providePropertyManager(Context context) {
        return new PropertyManager(context);
    }

    @Provides @Singleton static UserManager provideUserManager(Context context) {
        return new UserManager(context);
    }

    @Provides @Singleton ErrorResolver provideErrorResolver() {
        return new DefaultErrorResolver();
    }

    @Provides @Singleton @Named(REQUEST_ID_TOKEN) @Nullable String provideRequestIdToken(Context context) {
        return context.getString(R.string.requestIdToken);
    }
}
