package com.tdfanta.game.util.container

import com.tdfanta.game.util.iterator.LazyIterator
import com.tdfanta.game.util.iterator.StreamIterable
import com.tdfanta.game.util.iterator.StreamIterator
import java.util.ArrayList

class SafeCollection<T> : MutableCollection<T>, StreamIterable<T> {
    private inner class SafeIterator : LazyIterator<T>() {
        var mNextIndex = 0

        init {
            mIterators.add(this)
        }

        override fun fetchNext(): T? {
            val ret = if (mNextIndex < mItems.size) {
                mItems[mNextIndex++]
            } else {
                close()
                null
            }
            return ret
        }

        override fun close() {
            mIterators.remove(this)
        }

        override fun remove() {
            this@SafeCollection.remove(mNextIndex - 1)
        }
    }

    private val mItems = ArrayList<T>()
    private val mIterators = ArrayList<SafeIterator>()

    override val size: Int
        get() = mItems.size

    override fun add(element: T): Boolean = mItems.add(element)

    override fun addAll(elements: Collection<T>): Boolean = mItems.addAll(elements)

    override fun clear() {
        mItems.clear()
        for (it in mIterators) {
            it.mNextIndex = 0
        }
    }

    override fun contains(element: T): Boolean = mItems.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = mItems.containsAll(elements)

    override fun isEmpty(): Boolean = mItems.isEmpty()

    override fun iterator(): StreamIterator<T> = SafeIterator()

    override fun remove(element: T): Boolean {
        val index = mItems.indexOf(element)
        if (index >= 0) {
            remove(index)
            return true
        }

        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var ret = false
        for (item in elements) {
            if (remove(item)) {
                ret = true
            }
        }
        return ret
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var ret = false
        for (item in this) {
            if (!elements.contains(item)) {
                remove(item)
                ret = true
            }
        }
        return ret
    }

    private fun remove(index: Int): T {
        val ret = mItems.removeAt(index)

        for (it in mIterators) {
            if (it.mNextIndex > index) {
                it.mNextIndex--
            }
        }

        return ret
    }
}
