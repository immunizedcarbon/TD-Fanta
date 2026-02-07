package com.tdfanta.game.util.iterator

fun interface Function<F, T> {
    fun apply(input: F): T
}
