package com.tdfanta.game.util.iterator

import java.util.NoSuchElementException

abstract class LazyIterator<T> : StreamIterator<T>() {
    private var mNextElement: T? = null
    private var mNextFetched = false

    protected abstract fun fetchNext(): T?

    override fun hasNext(): Boolean {
        if (!mNextFetched) {
            mNextElement = fetchNext()
            mNextFetched = true
        }

        return mNextElement != null
    }

    override fun next(): T {
        if (hasNext()) {
            mNextFetched = false
            @Suppress("UNCHECKED_CAST")
            return mNextElement as T
        }
        throw NoSuchElementException()
    }
}
