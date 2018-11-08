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

package com.android.virgilsecurity.virgilonfire.ui.chat

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.support.annotation.StringDef
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.widget.TextView

import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.data.model.ChatThread
import com.android.virgilsecurity.virgilonfire.ui.base.BaseActivityDi
import com.android.virgilsecurity.virgilonfire.ui.chat.thread.ThreadFragment
import com.android.virgilsecurity.virgilonfire.ui.chat.threadList.ThreadsListFragment
import com.android.virgilsecurity.virgilonfire.ui.chat.threadList.dialog.CreateThreadDialog
import com.android.virgilsecurity.virgilonfire.ui.login.LogInActivity
import com.android.virgilsecurity.virgilonfire.util.UiUtils
import com.android.virgilsecurity.virgilonfire.util.common.OnFinishTimer
import com.google.firebase.auth.FirebaseAuth

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

import javax.inject.Inject

import butterknife.BindView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

class ChatControlActivity : BaseActivityDi(), HasFragmentInjector {

    private var createThreadDialog: CreateThreadDialog? = null
    private var threadsListFragment: ThreadsListFragment? = null
    private var threadFragment: ThreadFragment? = null
    private var secondPress: Boolean = false

    @Inject
    protected var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>? = null
    @Inject
    internal var userManager: UserManager? = null
    @Inject
    internal var firebaseAuth: FirebaseAuth? = null

    @BindView(R.id.toolbar)
    protected var toolbar: Toolbar? = null
    @BindView(R.id.nvNavigation)
    protected var nvNavigation: NavigationView? = null
    @BindView(R.id.dlDrawer)
    protected var dlDrawer: DrawerLayout? = null

    protected override val layout: Int
        get() = R.layout.activity_chat_control

    @Retention(RetentionPolicy.SOURCE)
    @StringDef(ChatState.THREADS_LIST, ChatState.THREAD)
    annotation class ChatState {
        companion object {
            val THREADS_LIST = "THREADS_LIST"
            val THREAD = "THREAD"
        }
    }

    override fun postButterInit() {
        initToolbar(toolbar!!, getString(R.string.threads_list_name))
        initDrawer()

        threadsListFragment = fragmentManager.findFragmentById(R.id.threadsListFragment) as ThreadsListFragment
        threadFragment = fragmentManager.findFragmentById(R.id.threadFragment) as ThreadFragment

        changeFragment(ChatState.THREADS_LIST)

        hideKeyboard()
    }

    fun changeFragment(@ChatState tag: String) {
        changeFragmentWithThread(tag, null)
    }

    fun changeFragmentWithThread(@ChatState tag: String, chatThread: ChatThread?) {
        if (tag == ChatState.THREADS_LIST) {
            dlDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            showBackButton(false) { view -> onBackPressed() }
            showHamburger(true) { view ->
                if (!dlDrawer!!.isDrawerOpen(Gravity.START))
                    dlDrawer!!.openDrawer(Gravity.START)
                else
                    dlDrawer!!.closeDrawer(Gravity.START)
            }

            changeToolbarTitle(getString(R.string.app_name))

            UiUtils.hideFragment(fragmentManager, threadFragment)
            UiUtils.showFragment(fragmentManager, threadsListFragment)
        } else {
            if (chatThread != null)
                threadFragment!!.setChatThread(chatThread)

            dlDrawer!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            showBackButton(true) { view -> onBackPressed() }
            showHamburger(false) { view -> }

            UiUtils.hideFragment(fragmentManager, threadsListFragment)
            UiUtils.showFragment(fragmentManager, threadFragment)
        }
    }

    fun changeToolbarTitleExposed(text: String) {
        changeToolbarTitle(text)
    }

    private fun initDrawer() {
        val tvUsernameDrawer = nvNavigation!!.getHeaderView(0)
                .findViewById<TextView>(R.id.tvUsernameDrawer)
        tvUsernameDrawer.setText(firebaseAuth!!.currentUser!!
                                         .email!!
                                         .toLowerCase()
                                         .split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])

        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.setHomeButtonEnabled(true)
        }

        nvNavigation!!.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemNewChat -> {
                    dlDrawer!!.closeDrawer(Gravity.START)
                    createThreadDialog = CreateThreadDialog(this, R.style.NotTransBtnsDialogTheme,
                                                            getString(R.string.create_thread),
                                                            getString(R.string.enter_username))

                    createThreadDialog!!.setOnCreateThreadDialogListener { username ->
                        if (firebaseAuth!!.currentUser!!.email!!.toLowerCase().split("@".toRegex()).dropLastWhile(
                                    { it.isEmpty() }).toTypedArray()[0] == username) {
                            UiUtils.toast(this, R.string.no_chat_with_yourself)
                        } else {
                            createThreadDialog!!.showProgress(true)
                            threadsListFragment!!.issueCreateThread(username.toLowerCase())
                        }
                    }

                    createThreadDialog!!.show()

                    return@nvNavigation.setNavigationItemSelectedListener true
                }
                R.id.itemLogOut -> {
                    dlDrawer!!.closeDrawer(Gravity.START)

                    showBaseLoading(true)

                    userManager!!.clearUserCard()

                    firebaseAuth!!.signOut()

                    threadFragment!!.disposeAll()
                    threadsListFragment!!.disposeAll()
                    LogInActivity.startClearTop(this)
                    return@nvNavigation.setNavigationItemSelectedListener true
                }
                else -> return@nvNavigation.setNavigationItemSelectedListener
                false
            }
        }
    }

    fun newThreadDialogShowProgress(show: Boolean) {
        if (createThreadDialog != null)
            createThreadDialog!!.showProgress(show)
    }

    fun newThreadDialogDismiss() {
        if (createThreadDialog != null)
            createThreadDialog!!.dismiss()
    }

    override fun onBackPressed() {
        hideKeyboard()

        if (threadFragment!!.isVisible) {
            changeFragment(ChatState.THREADS_LIST)
            return
        }

        if (secondPress)
            super.onBackPressed()
        else
            UiUtils.toast(this, getString(R.string.press_exit_once_more))

        secondPress = true

        object : OnFinishTimer(2000, 100) {

            override fun onFinish() {
                secondPress = false
            }
        }.start()
    }

    override fun fragmentInjector(): AndroidInjector<Fragment>? {
        return fragmentDispatchingAndroidInjector
    }

    companion object {

        val USERNAME = "USERNAME"

        fun start(from: Activity) {
            from.startActivity(Intent(from, ChatControlActivity::class.java))
        }

        fun startWithFinish(from: Activity) {
            from.startActivity(Intent(from, ChatControlActivity::class.java))
            from.finish()
        }

        fun startWithFinish(from: Activity, username: String) {
            from.startActivity(Intent(from, ChatControlActivity::class.java).putExtra(USERNAME,
                                                                                      username))
            from.finish()
        }
    }
}
