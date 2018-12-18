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

package com.android.virgilsecurity.virgilonfire.data.virgil

import com.android.virgilsecurity.virgilonfire.data.model.KeyGenerationException
import com.android.virgilsecurity.virgilonfire.util.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.virgilsecurity.sdk.cards.Card
import com.virgilsecurity.sdk.cards.CardManager
import com.virgilsecurity.sdk.cards.ModelSigner
import com.virgilsecurity.sdk.cards.validation.CardVerifier
import com.virgilsecurity.sdk.client.CardClient
import com.virgilsecurity.sdk.client.exceptions.VirgilServiceException
import com.virgilsecurity.sdk.crypto.CardCrypto
import com.virgilsecurity.sdk.crypto.VirgilCardCrypto
import com.virgilsecurity.sdk.crypto.VirgilCrypto
import com.virgilsecurity.sdk.crypto.VirgilKeyPair
import com.virgilsecurity.sdk.crypto.VirgilPrivateKey
import com.virgilsecurity.sdk.crypto.VirgilPublicKey
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException
import com.virgilsecurity.sdk.crypto.exceptions.EncryptionException
import com.virgilsecurity.sdk.jwt.contract.AccessTokenProvider
import com.virgilsecurity.sdk.storage.PrivateKeyStorage
import com.virgilsecurity.sdk.utils.ConvertionUtils

/**
 * Created by Danylo Oliinyk on 3/23/18 at Virgil Security.
 * -__o
 */

class VirgilHelper(cardClient: CardClient,
                   modelSigner: ModelSigner,
                   cardCrypto: CardCrypto,
                   accessTokenProvider: AccessTokenProvider,
                   cardVerifier: CardVerifier,
                   privateKeyStorage: PrivateKeyStorage,
                   firebaseAuth: FirebaseAuth) {

    val cardManager: CardManager
    private val privateKeyStorage: PrivateKeyStorage
    private val firebaseAuth: FirebaseAuth

    val virgilCrypto: VirgilCrypto
        get() = (cardManager.crypto as VirgilCardCrypto).virgilCrypto

    init {

        this.privateKeyStorage = privateKeyStorage
        this.firebaseAuth = firebaseAuth

        cardManager = CardManager(cardCrypto, accessTokenProvider, cardVerifier)

    }

    @Throws(CryptoException::class, VirgilServiceException::class)
    fun publishCard(identity: String): Card {
        val keyPair = generateKeyPair()

        privateKeyStorage.store(keyPair.privateKey, identity, null)

        val cardModel = cardManager.generateRawCard(keyPair.privateKey,
                                                    keyPair.publicKey,
                                                    identity)
        return cardManager.publishCard(cardModel)
    }

    @Throws(CryptoException::class, VirgilServiceException::class)
    fun outdateCard(identity: String,
                    previousCardId: String): Card {
        val keyPair = generateKeyPair()

        privateKeyStorage.store(keyPair.privateKey, identity, null)

        val cardModel = cardManager.generateRawCard(keyPair.privateKey,
                                                    keyPair.publicKey,
                                                    identity,
                                                    previousCardId)
        return cardManager.publishCard(cardModel)
    }

    @Throws(CryptoException::class, VirgilServiceException::class)
    fun getCard(cardId: String): Card {
        return cardManager.getCard(cardId)
    }

    @Throws(CryptoException::class, VirgilServiceException::class)
    fun searchCards(identity: String): List<Card> {
        return cardManager.searchCards(identity)
    }

    fun generateKeyPair(): VirgilKeyPair {
        try {
            return virgilCrypto.generateKeys()
        } catch (e: CryptoException) {
            e.printStackTrace()
            throw KeyGenerationException(e)
        }

    }

    fun decrypt(text: String): String {
        val cipherData = ConvertionUtils.base64ToBytes(text)

        return try {
            val decryptedData =
                    virgilCrypto.decrypt(cipherData,
                                         privateKeyStorage.load(
                                             UserUtils.currentUsername(firebaseAuth)).left
                                                 as VirgilPrivateKey)
            ConvertionUtils.toString(decryptedData)
        } catch (e: CryptoException) {
            if (text.isEmpty()) {
                "Message Deleted"
            } else {
                e.printStackTrace()
                "Message encrypted"
            }
        }

    }

    fun encrypt(data: String, publicKeys: List<VirgilPublicKey>): String {
        val toEncrypt = ConvertionUtils.toBytes(data)
        val encryptedData: ByteArray
        try {
            encryptedData = virgilCrypto.encrypt(toEncrypt, publicKeys)
        } catch (e: EncryptionException) {
            e.printStackTrace()
            throw com.android.virgilsecurity.virgilonfire.data.model.EncryptionException(
                "Failed to encrypt data ):")
        }

        return ConvertionUtils.toBase64String(encryptedData)
    }
}
