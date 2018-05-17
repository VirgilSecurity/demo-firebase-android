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

package com.android.virgilsecurity.virgilonfire.ui.chat.threadList;

import com.android.virgilsecurity.virgilonfire.data.model.DefaultUser;
import com.android.virgilsecurity.virgilonfire.data.model.ResponseType;
import com.android.virgilsecurity.virgilonfire.data.model.response.UsersResponse;
import com.android.virgilsecurity.virgilonfire.data.remote.WebSocketHelper;
import com.android.virgilsecurity.virgilonfire.ui.base.BasePresenter;
import com.android.virgilsecurity.virgilonfire.ui.chat.DataReceivedInteractor;
import com.android.virgilsecurity.virgilonfire.util.SerializationUtils;
import com.appunite.websocket.rx.messages.RxEventStringMessage;
import com.google.gson.JsonObject;

import java.util.List;

import javax.inject.Inject;

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    4/17/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */
public class ThreadsListFragmentPresenter implements BasePresenter {

    private WebSocketHelper helper;
    private DataReceivedInteractor<List<DefaultUser>> onMessageReceivedInteractor;

    @Inject
    public ThreadsListFragmentPresenter(WebSocketHelper helper,
                                        DataReceivedInteractor<List<DefaultUser>> onMessageReceivedInteractor) {
        this.helper = helper;
        this.onMessageReceivedInteractor = onMessageReceivedInteractor;
    }

    public void requestUsersList() {
        helper.setOnMessageReceiveListener(rxEvent -> {
            if (rxEvent instanceof RxEventStringMessage) {
                JsonObject jsonObject =
                        SerializationUtils.fromJson(((RxEventStringMessage) rxEvent).message(),
                                                    JsonObject.class);

                if (jsonObject.get("type")
                              .getAsString()
                              .equals(ResponseType.USERS_LIST.getType())) {
                    UsersResponse usersResponse =
                            SerializationUtils.fromJson(jsonObject.get("responseObject")
                                                                  .toString(),
                                                        UsersResponse.class);

                    onMessageReceivedInteractor.onDataReceived(usersResponse.getUsers());
                }
            }
        });
    }

    @Override
    public void disposeAll() {
        helper.unsubscribe();
    }
}
