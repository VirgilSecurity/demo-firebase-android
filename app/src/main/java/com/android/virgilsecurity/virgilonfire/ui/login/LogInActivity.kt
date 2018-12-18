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

package com.android.virgilsecurity.virgilonfire.ui.login

import android.app.Activity
import android.app.Fragment
import android.content.Intent

import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.ui.base.BaseActivityDi
import com.android.virgilsecurity.virgilonfire.ui.chat.ChatControlActivity
import com.android.virgilsecurity.virgilonfire.util.UiUtils
import com.android.virgilsecurity.virgilonfire.util.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import javax.inject.Inject

import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    12/17/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

/**
 * LogInActivity class.
 */
class LogInActivity : BaseActivityDi(), HasFragmentInjector {

    @Inject
    protected var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>? = null
    @Inject
    protected var firebaseAuth: FirebaseAuth? = null

    protected override val layout: Int
        get() = R.layout.activity_log_in

    override fun postButterInit() {
        val user = firebaseAuth!!.currentUser
        if (user != null)
            startChatControlActivity(UserUtils.currentUsername(firebaseAuth!!))

        UiUtils.replaceFragmentNoTag(fragmentManager,
                                     R.id.flBaseContainer,
                                     LogInFragment.newInstance())
    }

    override fun fragmentInjector(): AndroidInjector<Fragment>? {
        return fragmentDispatchingAndroidInjector
    }

    fun startChatControlActivity(username: String) {
        ChatControlActivity.startWithFinish(this, username)
    }

    companion object {

        fun start(from: Activity) {
            from.startActivity(Intent(from, LogInActivity::class.java))
        }

        fun startWithFinish(from: Activity) {
            from.startActivity(Intent(from, LogInActivity::class.java))
            from.finish()
        }

        fun startClearTop(from: Activity) {
            from.startActivity(Intent(from, LogInActivity::class.java)
                                       .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
