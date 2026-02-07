package com.tdfanta.game.util.container

import android.util.SparseArray
import com.tdfanta.game.util.iterator.LazyIterator
import com.tdfanta.game.util.iterator.StreamIterable
import com.tdfanta.game.util.iterator.StreamIterator

class SafeMultiMap<T> : StreamIterable<T> {
    private inner class KeyIterator : LazyIterator<T>() {
        var mKeyIndex = 0
        var mCollectionIterator: StreamIterator<T>? = null

        override fun fetchNext(): T? {
            while (mCollectionIterator == null || !mCollectionIterator!!.hasNext()) {
                mCollectionIterator = if (mKeyIndex < mLayers.size()) {
                    mLayers.valueAt(mKeyIndex++).iterator()
                } else {
                    null
                }

                if (mCollectionIterator == null) {
                    close()
                    return null
                }
            }

            return mCollectionIterator!!.next()
        }

        override fun close() {
            mCollectionIterator?.close()
            mCollectionIterator = null
        }
    }

    private val mLayers = SparseArray<SafeCollection<T>>()

    override fun iterator(): StreamIterator<T> = KeyIterator()

    fun get(key: Int): SafeCollection<T> {
        var collection = mLayers[key]
        if (collection == null) {
            collection = SafeCollection()
            mLayers.put(key, collection)
        }
        return collection
    }

    fun add(key: Int, value: T): Boolean = get(key).add(value)

    fun remove(key: Int, value: T): Boolean = get(key).remove(value)

    fun clear() {
        mLayers.clear()
    }
}
