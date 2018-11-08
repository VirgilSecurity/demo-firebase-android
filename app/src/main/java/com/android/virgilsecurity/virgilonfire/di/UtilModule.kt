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

package com.android.virgilsecurity.virgilonfire.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context

import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.data.local.PropertyManager
import com.android.virgilsecurity.virgilonfire.data.local.RoomDb
import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivityComponent
import com.android.virgilsecurity.virgilonfire.util.DefaultErrorResolver
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

import javax.inject.Named
import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * Created by Danylo Oliinyk on 3/26/18 at Virgil Security.
 * -__o
 */

@Module(subcomponents = arrayOf(ChatControlActivityComponent::class))
class UtilModule {

    @Provides @Singleton internal fun provideErrorResolver(): ErrorResolver {
        return DefaultErrorResolver()
    }

    @Provides internal fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides internal fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = settings

        return firestore
    }

    @Provides
    @Singleton
    @Named(InjectionConstants.ROOM_DB_NAME)
    internal fun provideRoomDbName(context: Context): String {
        return context.getString(R.string.room_db_name)
    }

    @Provides
    @Singleton
    internal fun provideRoom(context: Context, @Named(InjectionConstants.ROOM_DB_NAME) roomDbName: String): RoomDb {
        return Room.databaseBuilder<RoomDb>(context.applicationContext,
                                            RoomDb::class.java!!,
                                            roomDbName)
                .fallbackToDestructiveMigration()
                .build()
    }

    companion object {

        @Provides @Singleton internal fun provideContext(application: Application): Context {
            return application
        }

        @Provides
        @Singleton
        internal fun providePropertyManager(context: Context): PropertyManager {
            return PropertyManager(context)
        }

        @Provides @Singleton internal fun provideUserManager(context: Context): UserManager {
            return UserManager(context)
        }
    }
}
