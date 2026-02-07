package com.tdfanta.game.util.iterator

fun interface Predicate<T> {
    fun apply(value: T): Boolean
}
