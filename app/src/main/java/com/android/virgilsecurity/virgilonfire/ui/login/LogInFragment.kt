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

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View

import com.android.virgilsecurity.virgilonfire.R
import com.android.virgilsecurity.virgilonfire.data.local.UserManager
import com.android.virgilsecurity.virgilonfire.data.model.DefaultToken
import com.android.virgilsecurity.virgilonfire.data.model.DefaultUser
import com.android.virgilsecurity.virgilonfire.data.model.exception.ServiceException
import com.android.virgilsecurity.virgilonfire.ui.base.BaseFragmentDi
import com.android.virgilsecurity.virgilonfire.ui.login.dialog.NewKeyDialog
import com.android.virgilsecurity.virgilonfire.util.DefaultSymbolsInputFilter
import com.android.virgilsecurity.virgilonfire.util.ErrorResolver
import com.android.virgilsecurity.virgilonfire.util.UiUtils
import com.android.virgilsecurity.virgilonfire.util.Validator
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.virgilsecurity.sdk.cards.Card

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import butterknife.BindView

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

class LogInFragment : BaseFragmentDi<LogInActivity>(), LogInVirgilInteractor, LogInKeyStorageInteractor, RefreshUserCardsInteractor {

    @Inject
    protected var firebaseAuth: FirebaseAuth? = null
    @Inject
    protected var firestore: FirebaseFirestore? = null
    @Inject
    protected var presenter: LogInPresenter? = null
    @Inject
    protected var userManager: UserManager? = null
    @Inject
    protected var errorResolver: ErrorResolver? = null

    @BindView(R.id.etId)
    protected var etId: TextInputEditText? = null
    @BindView(R.id.tilId)
    protected var tilId: TextInputLayout? = null
    @BindView(R.id.etPassword)
    protected var etPassword: TextInputEditText? = null
    @BindView(R.id.tilPassword)
    protected var tilPassword: TextInputLayout? = null
    @BindView(R.id.btnSignIn)
    protected var btnSignIn: View? = null
    @BindView(R.id.btnSignUp)
    protected var btnSignUp: View? = null
    @BindView(R.id.pbLoading)
    protected var pbLoading: View? = null

    protected override val layout: Int
        get() = R.layout.fragment_log_in

    override fun postButterInit() {
        initInputFields()
        initFirebaseAuth()
    }

    private fun initInputFields() {
        etId!!.filters = arrayOf<InputFilter>(DefaultSymbolsInputFilter())
        etPassword!!.filters = arrayOf<InputFilter>(DefaultSymbolsInputFilter())

        etId!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                tilId!!.error = null
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        etPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                tilPassword!!.error = null
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }


    private fun initFirebaseAuth() {
        btnSignIn!!.setOnClickListener { v ->
            showProgress(true)

            var error: String?

            error = Validator.validate(etId!!, Validator.FieldType.ID_WITH_NO_AT)
            if (error != null) {
                showProgress(false)
                tilId!!.error = error
                return@btnSignIn.setOnClickListener
            }

            error = Validator.validate(etPassword!!, Validator.FieldType.PASSWORD)
            if (error != null) {
                showProgress(false)
                tilPassword!!.error = error
                return@btnSignIn.setOnClickListener
            }

            firebaseAuth!!.signInWithEmailAndPassword(etId!!.text!!
                                                              .toString()
                                                              .toLowerCase() + DEFAULT_POSTFIX,
                                                      etPassword!!.text!!
                                                              .toString())
                    .addOnCompleteListener(OnCompleteListener<AuthResult> {
                        this.handleSignInResult(it)
                    })
        }

        btnSignUp!!.setOnClickListener { v ->
            showProgress(true)

            var error: String?

            error = Validator.validate(etId!!, Validator.FieldType.ID_WITH_NO_AT)
            if (error != null) {
                showProgress(false)
                tilId!!.error = error
                return@btnSignUp.setOnClickListener
            }

            error = Validator.validate(etPassword!!, Validator.FieldType.PASSWORD)
            if (error != null) {
                showProgress(false)
                tilPassword!!.error = error
                return@btnSignUp.setOnClickListener
            }

            firebaseAuth!!.createUserWithEmailAndPassword(etId!!.text!!
                                                                  .toString()
                                                                  .toLowerCase() + DEFAULT_POSTFIX,
                                                          etPassword!!.text!!
                                                                  .toString())
                    .addOnCompleteListener { task ->
                        val defaultUser = DefaultUser()
                        defaultUser.createdAt = Timestamp(Date())
                        defaultUser.channels = ArrayList()

                        if (task.isSuccessful) {
                            firestore!!.collection(COLLECTION_USERS).document(etId!!.text!!
                                                                                      .toString()
                                                                                      .toLowerCase())
                                    .set(defaultUser)
                                    .addOnCompleteListener { taskCreateUser ->
                                        if (taskCreateUser.isSuccessful) {
                                            handleSignInResult(taskCreateUser)
                                        } else {
                                            activity.runOnUiThread {
                                                UiUtils.toast(this,
                                                              "Create user in firestore was not successful")
                                            }
                                        }
                                    }
                        } else {
                            activity.runOnUiThread {
                                showProgress(false)
                                UiUtils.toast(this, "Create user was not successful")
                            }
                        }
                    }
        }
    }

    private fun showProgress(show: Boolean) {
        pbLoading!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        btnSignIn!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
        btnSignUp!!.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun handleSignInResult(task: Task<*>) {
        val user = firebaseAuth!!.currentUser

        if (user != null) {
            firebaseAuth!!.currentUser!!.getIdToken(false).addOnCompleteListener { taskGetIdToken ->
                if (taskGetIdToken.isSuccessful) {
                    userManager!!.setToken(DefaultToken(taskGetIdToken.result!!.token))
                    presenter!!.requestSearchCards(user.email!!.toLowerCase().split("@".toRegex()).dropLastWhile(
                        { it.isEmpty() }).toTypedArray()[0])
                } else {
                    var error: String? = errorResolver!!.resolve(taskGetIdToken.exception)
                    if (error == null && taskGetIdToken.exception != null)
                        error = taskGetIdToken.exception!!
                                .message

                    UiUtils.toast(this, if (error == null) "Error getting token" else error)
                }
            }
        } else {
            firebaseAuth!!.signOut()
            showProgress(false)

            var error: String? = errorResolver!!.resolve(task.exception)
            if (error == null && task.exception != null)
                error = task.exception!!
                        .message

            UiUtils.toast(this, if (error == null) "Try again" else error)
        }
    }

    override fun onSearchCardSuccess(cards: List<Card>) {
        presenter!!.requestIfKeyExists(cards[0]
                                               .identity)
    }

    @SuppressLint("RestrictedApi") override fun onSearchCardError(t: Throwable) {
        val error = errorResolver!!.resolve(t) { resolvedError ->
            if (t is ServiceException)
                return@errorResolver.resolve t . message
                        null
        } // If we can't resolve error here -
        // then it's normal behaviour. Proceed.
        if (error != null) {
            showProgress(false)

            UiUtils.toast(this, error)
            firebaseAuth!!.signOut()
            presenter!!.disposeAll()

            return
        }

        presenter!!.requestPublishCard(firebaseAuth!!.currentUser!!
                                               .email!!
                                               .toLowerCase()
                                               .split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
    }


    override fun onPublishCardSuccess(card: Card) {
        presenter!!.requestRefreshUserCards(firebaseAuth!!.currentUser!!
                                                    .email!!
                                                    .toLowerCase()
                                                    .split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
    }

    override fun onPublishCardError(t: Throwable) {
        showProgress(false)

        firebaseAuth!!.signOut()

        UiUtils.toast(this, errorResolver!!.resolve(t))
    }

    override fun onKeyExists() {
        presenter!!.requestRefreshUserCards(firebaseAuth!!.currentUser!!
                                                    .email!!
                                                    .toLowerCase()
                                                    .split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
    }

    override fun onKeyNotExists() {
        showProgress(false)

        presenter!!.disposeAll()
        val newKeyDialog = NewKeyDialog(activity,
                                        R.style.NotTransBtnsDialogTheme,
                                        getString(R.string.new_private_key),
                                        getString(R.string.new_private_key_generation))

        newKeyDialog.setOnNewKeysDialogListener(object : NewKeyDialog.OnCreateNewKeysListener {
            override fun onCreateNewKeys(v: View, dialog: Dialog) {
                showProgress(true)
                dialog.dismiss()
                presenter!!.requestPublishCard(firebaseAuth!!.currentUser!!
                                                       .email!!
                                                       .toLowerCase()
                                                       .split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
            }

            override fun onCancelCreateNewKeys(v: View, dialog: Dialog) {
                firebaseAuth!!.signOut()
                dialog.cancel()
            }
        })

        newKeyDialog.show()
    }

    override fun onStop() {
        super.onStop()

        presenter!!.disposeAll()
    }

    override fun onRefreshUserCardsSuccess(cards: List<Card>) {
        userManager!!.userCards = cards
        activity.startChatControlActivity(firebaseAuth!!.currentUser!!
                                                  .email!!
                                                  .toLowerCase()
                                                  .split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
    }

    override fun onRefreshUserCardsError(t: Throwable) {

        firebaseAuth!!.signOut()
        UiUtils.toast(this, errorResolver!!.resolve(t))
    }

    companion object {

        private val DEFAULT_POSTFIX = "@virgilfirebase.com"
        private val COLLECTION_USERS = "Users"

        fun newInstance(): LogInFragment {

            val args = Bundle()

            val fragment = LogInFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
