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

package com.android.virgilsecurity.virgilonfire.util

/**
 * . _  _
 * .| || | _
 * -| || || |   Created by:
 * .| || || |-  Danylo Oliinyk
 * ..\_  || |   on
 * ....|  _/    3/28/18
 * ...-| | \    at Virgil Security
 * ....|_|-
 */

abstract class ErrorResolver {

    protected abstract fun baseResolve(t: Throwable): String?

    /**
     * Resolves error if it's implemented. Otherwise const `String` is returned.
     *
     * @param t error to resolve
     *
     * @return resolved error as `String`
     */
    fun resolve(t: Throwable): String {
        val resolvedError = baseResolve(t)

        return resolvedError ?: ERROR_RESOLUTION_NOT_IMPLEMENTED
    }

    /**
     * Resolves error. Handler will be called only if error can't be resolved
     * in `baseResolve` method. If you need to proceed with error resolving in handler -
     * please, use overloaded method [.resolve].
     *
     * @param t       error to resolve
     * @param handler main purpose - for handling not implemented errors. Secondary purpose -
     * see `useHandlerAnyway` description.
     *
     * @return resolved error as `String`
     */
    fun resolve(t: Throwable, handler: ErrorNotImplementedHandler): String {
        val resolvedError = baseResolve(t)

        return resolvedError ?: handler.onCustomError(null)
    }

    /**
     * Resolves error. If `useHandlerAnyway` is `true` - you will
     * get resolved error as string in handler to proceed resolving. Otherwise handler
     * will be called only if error can't be resolved in `baseResolve` method.
     *
     * @param t                error to resolve
     * @param handler          main purpose - for handling not implemented errors. Secondary purpose -
     * see `useHandlerAnyway` description.
     * @param useHandlerAnyway whether handler should get resolved error as parameter even if
     * it was successfully resolved.
     *
     * @return resolved error as `String`
     */
    fun resolve(t: Throwable,
                handler: ErrorNotImplementedHandler,
                useHandlerAnyway: Boolean): String {
        if (useHandlerAnyway) {
            return handler.onCustomError(baseResolve(t))
        } else {
            val resolvedError = baseResolve(t)

            return resolvedError ?: handler.onCustomError(null)
        }
    }

    interface ErrorNotImplementedHandler {
        fun onCustomError(resolvedError: String?): String
    }

    companion object {

        private val ERROR_RESOLUTION_NOT_IMPLEMENTED = "Error resolution is not implemented"
    }
}
