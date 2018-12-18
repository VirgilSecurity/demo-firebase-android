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

package com.android.virgilsecurity.virgilonfire.ui.chat.threadList.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.transition.TransitionManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView

import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.util.DefaultSymbolsInputFilter
import com.android.virgilsecurity.virgilonfire.util.Validator
import com.android.virgilsecurity.virgilonfire.util.common.OnFinishTimer

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.internal.Utils

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
 * CreateThreadDialog class.
 */
class CreateThreadDialog : Dialog {

    private var onCreateThreadDialogListener: OnCreateThreadDialogListener? = null
    private var title: String? = null
    private var message: String? = null

    @BindView(R.id.flRoot)
    internal var flRoot: FrameLayout? = null
    @BindView(R.id.llContentRoot)
    internal var llContentRoot: View? = null
    @BindView(R.id.llLoadingRoot)
    internal var llLoadingRoot: View? = null
    @BindView(R.id.tvTitle)
    internal var tvTitle: TextView? = null
    @BindView(R.id.tvMessage)
    internal var tvMessage: TextView? = null
    @BindView(R.id.etUsername)
    internal var etUsername: EditText? = null

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
        setContentView(R.layout.dialog_create_thread)
        setCancelable(true)
        ButterKnife.bind(this)

        etUsername!!.filters = arrayOf<InputFilter>(DefaultSymbolsInputFilter())
        etUsername!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                etUsername!!.error = null
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        etUsername!!.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        if (title != null)
            tvTitle!!.text = title

        tvMessage!!.text = message
    }

    @OnClick(R.id.btnCancel, R.id.btnOk)
    internal fun onInterfaceClick(v: View) {
        when (v.id) {
            R.id.btnCancel -> cancel()
            R.id.btnOk -> {
                val error: String? = Validator.validate(etUsername!!,
                                                        Validator.FieldType.ID_WITH_NO_AT)

                if (error != null) {
                    etUsername!!.error = error
                    return
                }

                onCreateThreadDialogListener!!.onCreateThread(etUsername!!.text.toString())
            }
        }
    }

    fun setOnCreateThreadDialogListener(onCreateThreadDialogListener: OnCreateThreadDialogListener) {
        this.onCreateThreadDialogListener = onCreateThreadDialogListener
    }

    fun showProgress(show: Boolean) {
        if (show) {
            setCancelable(false)
            llContentRoot!!.visibility = View.GONE
            TransitionManager.beginDelayedTransition(flRoot!!)
            llLoadingRoot!!.visibility = View.VISIBLE
        } else {
            object : OnFinishTimer(1000, 100) {
                override fun onFinish() {
                    setCancelable(true)
                    llLoadingRoot!!.visibility = View.GONE
                    TransitionManager.beginDelayedTransition(flRoot!!)
                    llContentRoot!!.visibility = View.VISIBLE
                }
            }.start()
        }
    }

    interface OnCreateThreadDialogListener {
        fun onCreateThread(username: String)
    }
}
