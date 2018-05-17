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

package com.android.virgilsecurity.virgilonfire.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


import com.android.virgilsecurity.virgilonfire.R;

/**
 * Created by Danylo Oliinyk on 11/17/17 at Virgil Security.
 * -__o
 */

public class UiUtils {

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Fragment fragment, String text) {
        Toast.makeText(fragment.getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, int stringResId) {
        Toast.makeText(context,
                       context.getString(stringResId),
                       Toast.LENGTH_SHORT).show();
    }

    public static void toast(Fragment fragment, int stringResId) {
        Toast.makeText(fragment.getActivity(),
                       fragment.getActivity().getString(stringResId),
                       Toast.LENGTH_SHORT).show();
    }

    public static void log(String tag, String text) {
        Log.d(tag, text);
    }

    public static void replaceFragmentNoTag(FragmentManager fm, int containerId, Fragment fragment) {
        fm.beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          .replace(containerId, fragment)
          .commit();
    }

    public static void replaceFragmentNoBackStack(FragmentManager fm, int containerId, Fragment fragment, String tag) {
        fm.beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          .replace(containerId, fragment, tag)
          .commit();
    }

    public static void replaceFragment(FragmentManager fm, int containerId, Fragment fragment, String tag) {
        fm.beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          .replace(containerId, fragment, tag)
          .addToBackStack(tag)
          .commit();
    }

    public static void showFragment(FragmentManager fm, Fragment fragment) {
        fm.beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
          .show(fragment)
          .commit();
    }

    public static void hideFragment(FragmentManager fm, Fragment fragment) {
        fm.beginTransaction()
          .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
          .hide(fragment)
          .commit();
    }
}
