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

package com.android.virgilsecurity.virgilonfire.ui.base

import android.content.Context
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView

import com.android.virgilsecurity.virgilonfire.R
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork

import java.util.concurrent.TimeUnit

import butterknife.ButterKnife
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Danylo Oliinyk on 16.11.17 at Virgil Security.
 * -__o
 */

abstract class BaseActivityDi : AppCompatActivity() {

    private var tvToolbarTitle: TextView? = null
    private var ibToolbarBack: View? = null
    private var ibToolbarHamburger: View? = null
    private var toolbar: Toolbar? = null
    private var llBaseLoadingTextNoNetwork: View? = null
    private var llBaseLoading: View? = null

    protected abstract val layout: Int

    protected abstract fun postButterInit()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        val inflater = layoutInflater
        val baseView = inflater.inflate(R.layout.activity_base, null)

        val flBaseContainer = baseView.findViewById<FrameLayout>(R.id.flBaseContainer)
        llBaseLoading = baseView.findViewById(R.id.llBaseLoading)
        llBaseLoadingTextNoNetwork = baseView.findViewById(R.id.tvBaseNoNetwork)

        val childView = inflater.inflate(layout, null)
        flBaseContainer.removeAllViews()
        flBaseContainer.addView(childView)

        setContentView(baseView)

        ButterKnife.bind(this)

        postButterInit()
    }

    protected fun changeToolbarTitle(titlePage: String) {
        if (toolbar != null) {
            tvToolbarTitle!!.text = titlePage
        } else {
            throw NullPointerException("Init Toolbar first")
        }
    }

    protected fun initToolbar(toolbar: Toolbar, titlePage: String) {
        this.toolbar = toolbar
        this.tvToolbarTitle = toolbar.findViewById(R.id.tvToolbarTitle)
        this.ibToolbarBack = toolbar.findViewById(R.id.ibToolbarBack)
        this.ibToolbarHamburger = toolbar.findViewById(R.id.ibToolbarHamburger)

        setSupportActionBar(toolbar)

        tvToolbarTitle!!.text = titlePage

        if (supportActionBar != null)
            supportActionBar!!.title = ""
    }

    protected fun showBackButton(show: Boolean, listener: View.OnClickListener?) {
        if (show) {
            ibToolbarBack!!.visibility = View.VISIBLE
            ibToolbarBack!!.setOnClickListener(listener)
        } else {
            ibToolbarBack!!.visibility = View.INVISIBLE
        }
    }

    protected fun showHamburger(show: Boolean, listener: View.OnClickListener?) {
        if (show) {
            ibToolbarHamburger!!.visibility = View.VISIBLE
            ibToolbarHamburger!!.setOnClickListener(listener)
        } else {
            ibToolbarHamburger!!.visibility = View.INVISIBLE
        }
    }

    protected fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onResume() {
        super.onResume()

        ReactiveNetwork.observeNetworkConnectivity(applicationContext)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { connectivity -> showNoNetwork(connectivity.state != NetworkInfo.State.CONNECTED) }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showNoNetwork(show: Boolean) {
        showBaseLoading(show)
        llBaseLoadingTextNoNetwork!!.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showBaseLoading(show: Boolean) {
        llBaseLoading!!.visibility = if (show) View.VISIBLE else View.GONE
        llBaseLoading!!.requestFocus()
    }
}