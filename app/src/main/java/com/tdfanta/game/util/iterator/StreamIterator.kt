package com.tdfanta.game.util.iterator

import java.util.ArrayList
import java.util.NoSuchElementException
import java.util.Random

abstract class StreamIterator<T> : MutableIterator<T> {
    override fun remove() {
        throw UnsupportedOperationException()
    }

    abstract fun close()

    fun first(): T? {
        var first: T? = null

        if (hasNext()) {
            first = next()
        }

        close()
        return first
    }

    fun last(): T? {
        var last: T? = null

        while (hasNext()) {
            last = next()
        }

        return last
    }

    fun random(random: Random): T? {
        val list = toList()
        if (list.isEmpty()) {
            return null
        }

        val index = random.nextInt(list.size)
        return list[index]
    }

    fun count(): Int {
        var count = 0
        while (hasNext()) {
            next()
            count++
        }
        return count
    }

    fun isEmpty(): Boolean {
        val empty = !hasNext()
        close()
        return empty
    }

    fun toList(): List<T> {
        val ret: MutableList<T> = ArrayList()
        while (hasNext()) {
            ret.add(next())
        }
        return ret
    }

    fun toString(delim: String): String {
        val sb = StringBuilder()

        if (hasNext()) {
            sb.append(next().toString())
        }

        while (hasNext()) {
            sb.append(delim)
            sb.append(next().toString())
        }

        return sb.toString()
    }

    fun min(scoreFunction: Function<in T, Float>): T? {
        var minObject: T? = null
        var minValue = 0f

        while (hasNext()) {
            val obj = next()
            val value = scoreFunction.apply(obj)

            if (minObject == null || value < minValue) {
                minObject = obj
                minValue = value
            }
        }

        return minObject
    }

    fun max(scoreFunction: Function<in T, Float>): T? {
        var maxObject: T? = null
        var maxValue = 0f

        while (hasNext()) {
            val obj = next()
            val value = scoreFunction.apply(obj)

            if (maxObject == null || value > maxValue) {
                maxObject = obj
                maxValue = value
            }
        }

        return maxObject
    }

    fun <F> map(transformation: Function<in T, out F>): StreamIterator<F> =
        MappingIterator(this, transformation)

    fun filter(filter: Predicate<in T>): StreamIterator<T> = FilteringIterator(this, filter)

    fun filter(obj: T): StreamIterator<T> = FilteringIterator(this) { value -> value != obj }

    fun filter(collection: Collection<T>): StreamIterator<T> =
        FilteringIterator(this) { value -> !collection.contains(value) }

    fun <F : Any> filter(klass: Class<F>): StreamIterator<F> =
        FilteringIterator(this) { value -> klass.isInstance(value) }.cast(klass)

    fun <F : Any> cast(castTo: Class<F>): StreamIterator<F> =
        MappingIterator(this) { value -> castTo.cast(value) }

    fun <F : Any> ofType(type: Class<F>): StreamIterator<F> {
        val predicate = Predicate<T> { value -> type.isInstance(value) }
        return filter(predicate).cast(type)
    }

    companion object {
        @JvmStatic
        fun <T> fromIterator(it: Iterator<T>): StreamIterator<T> = object : StreamIterator<T>() {
            override fun close() {
            }

            override fun hasNext(): Boolean = it.hasNext()

            override fun next(): T = it.next()
        }

        @JvmStatic
        fun <T> fromIterable(it: Iterable<T>): StreamIterator<T> = fromIterator(it.iterator())

        @JvmStatic
        fun <T> fromArray(array: Array<T>): StreamIterator<T> = object : StreamIterator<T>() {
            private var mIndex = 0

            override fun close() {
            }

            override fun hasNext(): Boolean = mIndex < array.size

            override fun next(): T {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }
                return array[mIndex++]
            }
        }
    }
}
