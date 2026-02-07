package com.tdfanta.game.util.iterator

class FilteringIterator<T>(
    private val mOriginal: StreamIterator<T>,
    private val mFilter: Predicate<in T>,
) : LazyIterator<T>() {
    override fun close() {
        mOriginal.close()
    }

    override fun fetchNext(): T? {
        while (mOriginal.hasNext()) {
            val next = mOriginal.next()
            if (mFilter.apply(next)) {
                return next
            }
        }

        return null
    }
}
