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

/**
 * Created by Danylo Oliinyk on 11/23/17 at Virgil Security.
 * -__o
 */

public class Const {

    static class Http {
        static final int BAD_REQUEST = 400;
        static final int UNAUTHORIZED = 401;
        static final int FORBIDDEN = 403;
        static final int NOT_ACCEPTABLE = 406;
        static final int UNPROCESSABLE_ENTITY = 422;
        static final int SERVER_ERROR = 500;
    }

    public static class TableNames {
        public static final String USER_NAME = "username";
        public static final String OBJECT_ID = "objectId";
        public static final String SENDER_USERNAME = "senderUsername";
        public static final String SENDER_ID = "senderId";
        public static final String RECIPIENT_USERNAME = "recipientUsername";
        public static final String RECIPIENT_ID = "recipientId";
        public static final String THREAD_ID = "threadId";
        public static final String MESSAGE_BODY = "body";
        public static final String CREATED_AT_CRITERIA = "createdAt";
        public static final String CREATED_AT_DATE = "createdAtDate";
        public static final String TIMESTAMP = "timestamp";
    }
}
