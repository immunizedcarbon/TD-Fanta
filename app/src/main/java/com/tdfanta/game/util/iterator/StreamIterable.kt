package com.tdfanta.game.util.iterator

interface StreamIterable<T> : Iterable<T> {
    override fun iterator(): StreamIterator<T>
}
