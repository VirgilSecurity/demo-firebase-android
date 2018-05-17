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

import com.virgilsecurity.sdk.client.exceptions.VirgilCardIsNotFoundException;
import com.virgilsecurity.sdk.client.exceptions.VirgilKeyIsAlreadyExistsException;
import com.virgilsecurity.sdk.client.exceptions.VirgilKeyIsNotFoundException;
import com.virgilsecurity.sdk.crypto.exceptions.KeyEntryNotFoundException;

import retrofit2.HttpException;

/**
 * Created by Danylo Oliinyk on 3/21/18 at Virgil Security.
 * -__o
 */

public class NetworkUtils {

    public static String resolveError(Throwable t) {
        if (t instanceof HttpException) {
            HttpException exception = (HttpException) t;

            switch (exception.code()) {
                case Const.Http.BAD_REQUEST:
                    return "Bad Request";
                case Const.Http.UNAUTHORIZED:
                    return "Unauthorized";
                case Const.Http.FORBIDDEN:
                    return "Forbidden";
                case Const.Http.NOT_ACCEPTABLE:
                    return "Not acceptable";
                case Const.Http.UNPROCESSABLE_ENTITY:
                    return "Unprocessable entity";
                case Const.Http.SERVER_ERROR:
                    return "Server error";
                default:
                    return "Oops.. Something went wrong ):";
            }
        } else if (t instanceof VirgilKeyIsNotFoundException) {
            return "Username is not registered yet";
        } else if (t instanceof VirgilKeyIsAlreadyExistsException) {
            return "Username is already registered. Please, try another one.";
        } else if (t instanceof KeyEntryNotFoundException) {
            return "Username is not found on this device. Maybe you deleted your private key";
        } else if (t instanceof VirgilCardIsNotFoundException) {
            return "Virgil Card is not found.\nYou can not start chat with user without Virgil Card.";
        } else {
            return "Something went wrong";
        }
    }
}
