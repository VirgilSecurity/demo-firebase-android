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

package com.android.virgilsecurity.virgilonfire.ui.login.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView

import com.android.virgilsecurity.virgilonfire.R

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

/**
 * Created by Danylo Oliinyk on 11/26/17 at Virgil Security.
 * -__o
 */

class NewKeyDialog : Dialog {

    private var onCreateNewKeysListener: OnCreateNewKeysListener? = null
    private val title: String?
    private var message: String? = null

    @BindView(R.id.flRoot)
    protected var flRoot: FrameLayout? = null
    @BindView(R.id.llContentRoot)
    protected var llContentRoot: View? = null
    @BindView(R.id.tvTitle)
    protected var tvTitle: TextView? = null
    @BindView(R.id.tvMessage)
    protected var tvMessage: TextView? = null

    constructor(context: Context, message: String) : super(context) {

        this.message = message
    }

    constructor(context: Context, themeResId: Int,
                title: String?, message: String) : super(context, themeResId) {

        this.title = title
        this.message = message
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_new_key)
        setCancelable(true)
        ButterKnife.bind(this)

        if (title != null)
            tvTitle!!.text = title

        tvMessage!!.text = message
    }

    @OnClick(R.id.btnCancel, R.id.btnOk)
    internal fun onInterfaceClick(v: View) {
        when (v.id) {
            R.id.btnCancel -> onCreateNewKeysListener!!.onCancelCreateNewKeys(v, this)
            R.id.btnOk -> onCreateNewKeysListener!!.onCreateNewKeys(v, this)
        }
    }

    fun setOnNewKeysDialogListener(onCreateNewKeysListener: OnCreateNewKeysListener) {
        this.onCreateNewKeysListener = onCreateNewKeysListener
    }

    interface OnCreateNewKeysListener {
        fun onCreateNewKeys(v: View, dialog: Dialog)

        fun onCancelCreateNewKeys(v: View, dialog: Dialog)
    }
}
