package com.mostafa.ping.app.data

private const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"

object PairCode {
    const val LENGTH = 6

    fun random(): String = buildString(LENGTH) {
        repeat(LENGTH) {
            append(ALPHABET.random())
        }
    }

    fun normalize(raw: String): String =
        raw.trim().uppercase().replace(" ", "")

    fun isValid(code: String): Boolean =
        code.length == LENGTH && code.all { it in ALPHABET }

    fun topic(code: String): String = "ping_$code"
}
